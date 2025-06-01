package spotifyServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import services.playbackServices.PlaybackService;
import songsAndArtists.Song;

/**
 * Enhanced MusicStreamer with better integration with PlaybackService.
 * Handles streaming of individual songs with awareness of playlist context.
 */
public class MusicStreamer {
    // Track active streamers by user ID
    private static final ConcurrentHashMap<Integer, MusicStreamer> activeStreamersByUserId = new ConcurrentHashMap<>();

    // Playback state flags
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicLong bytesStreamed = new AtomicLong(0);

    // Stream resources
    private Integer userId;
    private String currentFilePath;
    private Socket streamingSocket;
    private RandomAccessFile audioFile;

    // Reference to the PlaybackService
    private PlaybackService playbackService;

    // Playlist context
    private int currentPlaylistId = -1;
    private boolean isPlaylistStreaming = false;

    public MusicStreamer() {
        this.playbackService = PlaybackService.getInstance();
    }

    /**
     * Gets the active streamer for a user ID.
     */
    public static MusicStreamer getStreamerForUser(Integer userId) {
        if (userId == null) {
            return null;
        }
        return activeStreamersByUserId.get(userId);
    }

    /**
     * Registers this streamer with a user ID.
     */
    public void registerForUser(Integer userId) {
        this.userId = userId;
        if (userId != null) {
            activeStreamersByUserId.put(userId, this);
            System.out.println("Registered streamer for user ID: " + userId);
        }
    }

    /**
     * Streams audio file with user context integration.
     * @param filePath Path to the audio file
     * @param streamingSocket Socket for streaming
     * @param userId User ID
     * @param playlistId Playlist ID (or -1 if not part of a playlist)
     * @return true if streaming started successfully
     */
    public boolean streamAudioFile(String filePath, Socket streamingSocket, Integer userId, int playlistId) {
        File file = new File(filePath);

        if (!file.exists() || !file.canRead()) {
            System.err.println("Error: File not accessible: " + filePath);
            sendErrorToClient(streamingSocket, "File not found: " + filePath);
            return false;
        }

        this.currentFilePath = filePath;
        this.streamingSocket = streamingSocket;
        this.currentPlaylistId = playlistId;
        this.isPlaylistStreaming = (playlistId > 0);

        // Register with user ID system
        registerForUser(userId);

        try {
            audioFile = new RandomAccessFile(file, "r");
            OutputStream clientOut = streamingSocket.getOutputStream();

            PrintWriter textOut = new PrintWriter(new OutputStreamWriter(clientOut), true);
            textOut.println("STREAMING_START|" + file.getName() + (isPlaylistStreaming ? "|PLAYLIST:" + playlistId : ""));
            System.out.println("Started streaming for user " + userId + ": " + file.getName() +
                    (isPlaylistStreaming ? " (part of playlist " + playlistId + ")" : ""));

            Thread.sleep(200);

            return streamWithControl(clientOut, file.length());

        } catch (Exception e) {
            System.err.println("Streaming error: " + e.getMessage());
            return false;
        } finally {
            cleanup();
        }
    }

    /**
     * Checks if this streamer is streaming from a playlist.
     */
    public boolean isPlaylistStreaming() {
        return isPlaylistStreaming;
    }

    /**
     * Gets the ID of the playlist being streamed, or -1 if not streaming a playlist.
     */
    public int getCurrentPlaylistId() {
        return currentPlaylistId;
    }

    // The streaming control logic with pause/resume support
    private boolean streamWithControl(OutputStream clientOut, long fileSize) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        boolean endOfStreamDetected = false;

        while ((bytesRead = audioFile.read(buffer)) != -1) {
            // Check for pause
            waitWhilePaused();

            // Check for stop
            if (stopRequested.get()) {
                System.out.println("Streaming stopped for user ID: " + userId);
                return false;
            }

            // Stream to client
            clientOut.write(buffer, 0, bytesRead);
            clientOut.flush();
            bytesStreamed.addAndGet(bytesRead);

            // Check if we're nearing the end of the file (last 4KB)
            if (!endOfStreamDetected && audioFile.getFilePointer() >= fileSize - 4096) {
                endOfStreamDetected = true;

                // If streaming a playlist, prepare the next song in the background
                if (isPlaylistStreaming && userId != null) {
                    prepareNextSongInPlaylist();
                }
            }
        }

        System.out.println("Finished streaming file: " + currentFilePath + " to user ID: " + userId);

        // If this was part of a playlist, notify client that next song is coming
        if (isPlaylistStreaming && userId != null) {
            notifyClientOfNextSong(clientOut);
        }

        return true;
    }

    /**
     * Prepares the next song in the playlist in the background.
     */
    private void prepareNextSongInPlaylist() {
        if (userId == null || !isPlaylistStreaming || playbackService == null) {
            return;
        }

        // Check if we're at the end of the playlist in non-repeating mode
        if (!playbackService.hasActivePlaylist(userId)) {
            return;
        }

        // Peek at the next song without actually advancing
        Song nextSong = playbackService.peekNextSong(userId);
        if (nextSong != null) {
            // Notify the streaming server to prepare this song
            System.out.println("Pre-buffering next song in playlist: " + nextSong.getTitle());
            StreamingServer.getInstance().prepareForStreaming(nextSong);
        }
    }

    /**
     * Notifies the client that the next song in the playlist is coming.
     */
    private void notifyClientOfNextSong(OutputStream clientOut) {
        if (userId == null || !isPlaylistStreaming) {
            return;
        }

        try {
            PrintWriter textOut = new PrintWriter(new OutputStreamWriter(clientOut), true);

            // Advance to the next song in the playlist
            Song nextSong = playbackService.getNextSong(userId);

            if (nextSong != null) {
                textOut.println("NEXT_SONG|" + nextSong.getTitle() + "|" + nextSong.getFilePath());
                System.out.println("Notified client of next song: " + nextSong.getTitle());
            } else {
                // We've reached the end of the playlist
                textOut.println("PLAYLIST_COMPLETE");
                System.out.println("Playlist complete for user: " + userId);
            }
        } catch (Exception e) {
            System.err.println("Error notifying client of next song: " + e.getMessage());
        }
    }

    private void waitWhilePaused() {
        if (isPaused.get()) {
            System.out.println("User " + userId + " stream paused at " + bytesStreamed.get() + " bytes");
            while (isPaused.get() && !stopRequested.get()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Control methods
    public synchronized void pause() {
        if (!isPaused.get()) {
            isPaused.set(true);
            System.out.println("PAUSE requested for user: " + userId);
        }
    }

    public synchronized void resume() {
        if (isPaused.get()) {
            isPaused.set(false);
            System.out.println("RESUME requested for user: " + userId);
        }
    }

    public synchronized void stopStreaming() {
        stopRequested.set(true);
        isPaused.set(false);
        System.out.println("STOP requested for user: " + userId);

        // If this was a playlist, also stop the playlist navigation
        if (isPlaylistStreaming && userId != null && playbackService != null) {
            playbackService.stopPlayback(userId);
        }
    }

    // State query methods
    public boolean isPaused() { return isPaused.get(); }
    public boolean isStopped() { return stopRequested.get(); }
    public long getBytesStreamed() { return bytesStreamed.get(); }
    public String getCurrentFile() { return currentFilePath; }
    public Integer getUserId() { return userId; }

    private void sendErrorToClient(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("ERROR|" + message);
        } catch (IOException e) {
            // Ignore error handling errors
        }
    }

    private void cleanup() {
        try {
            if (audioFile != null) {
                audioFile.close();
            }
            if (streamingSocket != null && !streamingSocket.isClosed()) {
                streamingSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cleaning up resources: " + e.getMessage());
        } finally {
            if (userId != null) {
                activeStreamersByUserId.remove(userId);
            }
        }
    }
}