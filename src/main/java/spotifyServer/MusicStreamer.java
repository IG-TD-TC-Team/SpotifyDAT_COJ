package spotifyServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import services.playbackServices.PlaybackService;
import songsAndArtists.Song;

/**
 * Enhanced MusicStreamer with continuous playlist streaming support.
 * Handles seamless transitions between songs in a playlist.
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
    private OutputStream clientOut;
    private PrintWriter textOut;

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
     * Streams audio file with enhanced playlist support.
     * @param filePath Path to the audio file
     * @param streamingSocket Socket for streaming
     * @param userId User ID
     * @param playlistId Playlist ID (or -1 if not part of a playlist)
     * @return true if streaming started successfully
     */
    public boolean streamAudioFile(String filePath, Socket streamingSocket, Integer userId, int playlistId) {
        this.streamingSocket = streamingSocket;
        this.currentPlaylistId = playlistId;
        this.isPlaylistStreaming = (playlistId > 0);

        // Register with user ID system
        registerForUser(userId);

        try {
            clientOut = streamingSocket.getOutputStream();
            textOut = new PrintWriter(new OutputStreamWriter(clientOut), true);

            // If this is playlist streaming, start the continuous playlist stream
            if (isPlaylistStreaming) {
                return streamPlaylistContinuously();
            } else {
                // Single song streaming
                return streamSingleSong(filePath);
            }

        } catch (Exception e) {
            System.err.println("Streaming error: " + e.getMessage());
            return false;
        } finally {
            cleanup();
        }
    }

    /**
     * Streams a single song (non-playlist mode).
     */
    private boolean streamSingleSong(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            System.err.println("Error: File not accessible: " + filePath);
            sendErrorToClient("File not found: " + filePath);
            return false;
        }

        this.currentFilePath = filePath;
        textOut.println("STREAMING_START|" + file.getName());
        System.out.println("Started streaming single song for user " + userId + ": " + file.getName());

        return streamFileContent(file);
    }

    /**
     * Streams a playlist continuously, transitioning between songs seamlessly.
     */
    private boolean streamPlaylistContinuously() {
        try {
            textOut.println("PLAYLIST_STREAMING_START|" + currentPlaylistId);
            System.out.println("Started continuous playlist streaming for user " + userId + ", playlist " + currentPlaylistId);

            // Get the current song from the playback service
            Song currentSong = playbackService.getCurrentSong(userId);
            if (currentSong == null) {
                // No current song, get the first song
                currentSong = playbackService.getNextSong(userId);
            }

            while (currentSong != null && !stopRequested.get()) {
                // Check for pause
                waitWhilePaused();

                // Stream the current song
                boolean streamSuccess = streamSongInPlaylist(currentSong);
                if (!streamSuccess) {
                    System.err.println("Failed to stream song: " + currentSong.getTitle());
                    break;
                }

                // Check if we should continue to next song
                if (stopRequested.get()) {
                    break;
                }

                // Get next song
                Song nextSong = playbackService.getNextSong(userId);
                if (nextSong != null) {
                    textOut.println("NEXT_SONG|" + nextSong.getTitle() + "|" + nextSong.getFilePath());
                    System.out.println("Transitioning to next song: " + nextSong.getTitle());

                    // Small pause between songs to ensure client is ready
                    Thread.sleep(100);

                    currentSong = nextSong;
                } else {
                    // End of playlist
                    textOut.println("PLAYLIST_COMPLETE");
                    System.out.println("Playlist complete for user: " + userId);
                    break;
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error in continuous playlist streaming: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Streams a single song within a playlist context.
     */
    private boolean streamSongInPlaylist(Song song) {
        try {
            File file = new File(song.getFilePath());
            if (!file.exists() || !file.canRead()) {
                System.err.println("Error: Song file not accessible: " + song.getFilePath());
                textOut.println("SONG_ERROR|File not found: " + song.getTitle());
                return false;
            }

            this.currentFilePath = song.getFilePath();
            textOut.println("SONG_START|" + song.getTitle() + "|" + song.getFilePath());
            System.out.println("Streaming song in playlist: " + song.getTitle() + " for user " + userId);

            boolean success = streamFileContent(file);

            if (success) {
                textOut.println("SONG_END|" + song.getTitle());
                System.out.println("Finished streaming song: " + song.getTitle());
            }

            return success;

        } catch (Exception e) {
            System.err.println("Error streaming song " + song.getTitle() + ": " + e.getMessage());
            textOut.println("SONG_ERROR|" + song.getTitle() + "|" + e.getMessage());
            return false;
        }
    }

    /**
     * Streams the content of a file.
     */
    private boolean streamFileContent(File file) throws IOException {
        try (RandomAccessFile audioFile = new RandomAccessFile(file, "r")) {
            this.audioFile = audioFile;

            byte[] buffer = new byte[4096];
            int bytesRead;
            long fileSize = file.length();
            long totalBytesRead = 0;

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
                totalBytesRead += bytesRead;

                // Optional: Add a small delay to prevent overwhelming the client
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("Finished streaming file: " + file.getName() + " (" + totalBytesRead + " bytes)");
            return true;

        } catch (IOException e) {
            System.err.println("Error streaming file " + file.getName() + ": " + e.getMessage());
            throw e;
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

    /**
     * Pauses streaming for the current user.
     */
    public synchronized void pause() {
        if (!isPaused.get()) {
            isPaused.set(true);
            if (textOut != null) {
                textOut.println("PLAYBACK_PAUSED");
            }
            System.out.println("PAUSE requested for user: " + userId);
        }
    }

    /**
     * Resumes streaming for the current user after a pause.
     */
    public synchronized void resume() {
        if (isPaused.get()) {
            isPaused.set(false);
            if (textOut != null) {
                textOut.println("PLAYBACK_RESUMED");
            }
            System.out.println("RESUME requested for user: " + userId);
        }
    }

    /**
     * Stops the current streaming session completely.
     */
    public synchronized void stopStreaming() {
        stopRequested.set(true);
        isPaused.set(false);

        if (textOut != null) {
            textOut.println("STREAMING_STOPPED");
        }

        System.out.println("STOP requested for user: " + userId);

        // If this was a playlist, also stop the playlist navigation
        if (isPlaylistStreaming && userId != null && playbackService != null) {
            playbackService.stopPlayback(userId);
        }
    }

    /**
     * Skips to the next song in playlist mode.
     */
    public synchronized boolean skipToNext() {
        if (!isPlaylistStreaming || userId == null) {
            return false;
        }

        Song nextSong = playbackService.getNextSong(userId);
        if (nextSong != null && textOut != null) {
            textOut.println("SKIP_TO_NEXT|" + nextSong.getTitle() + "|" + nextSong.getFilePath());
            System.out.println("Skipping to next song: " + nextSong.getTitle());
            return true;
        }

        return false;
    }

    /**
     * Skips to the previous song in playlist mode.
     */
    public synchronized boolean skipToPrevious() {
        if (!isPlaylistStreaming || userId == null) {
            return false;
        }

        Song prevSong = playbackService.getPreviousSong(userId);
        if (prevSong != null && textOut != null) {
            textOut.println("SKIP_TO_PREVIOUS|" + prevSong.getTitle() + "|" + prevSong.getFilePath());
            System.out.println("Skipping to previous song: " + prevSong.getTitle());
            return true;
        }

        return false;
    }

    /**
     * Waits while streaming is paused.
     */
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

    /**
     * Sends an error message to the client.
     */
    private void sendErrorToClient(String message) {
        if (textOut != null) {
            textOut.println("ERROR|" + message);
        }
    }

    // State query methods
    public boolean isPaused() { return isPaused.get(); }
    public boolean isStopped() { return stopRequested.get(); }
    public long getBytesStreamed() { return bytesStreamed.get(); }
    public String getCurrentFile() { return currentFilePath; }
    public Integer getUserId() { return userId; }

    /**
     * Performs resource cleanup after streaming is complete or terminated.
     */
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