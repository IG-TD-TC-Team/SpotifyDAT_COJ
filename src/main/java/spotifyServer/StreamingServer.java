package spotifyServer;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import services.playbackServices.PlaybackService;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Enhanced StreamingServer with support for continuous playlist streaming.
 * Handles both single song streams and continuous playlist streams.
 */
public class StreamingServer {
    private static StreamingServer instance;

    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final PlaybackService playbackService;

    // Cache for prepared content
    private final ConcurrentHashMap<Integer, Song> preparedSongs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, LinkedList<Song>> preparedPlaylists = new ConcurrentHashMap<>();

    /**
     * Private constructor enforcing the Singleton pattern.
     */
    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
        this.playbackService = PlaybackService.getInstance();
    }

    /**
     * Returns the singleton instance of StreamingServer.
     */
    public static synchronized StreamingServer getInstance(int port, ExecutorService threadPool) {
        if (instance == null) {
            instance = new StreamingServer(port, threadPool);
        }
        return instance;
    }

    /**
     * Returns the existing singleton instance of StreamingServer.
     */
    public static StreamingServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StreamingServer not initialized. Call getInstance(port, threadPool) first.");
        }
        return instance;
    }

    /**
     * Prepares a single song for streaming.
     */
    public void prepareForStreaming(Song song) {
        if (song != null) {
            preparedSongs.put(song.getSongId(), song);
            System.out.println("Prepared song for streaming: " + song.getTitle());
        }
    }

    /**
     * Prepares a playlist for streaming.
     */
    public void prepareForPlaylistStreaming(Playlist playlist, LinkedList<Song> songs) {
        if (playlist != null && songs != null) {
            preparedPlaylists.put(playlist.getPlaylistID(), new LinkedList<>(songs));
            System.out.println("Prepared playlist for streaming: " + playlist.getName() + " (" + songs.size() + " songs)");
        }
    }

    /**
     * Starts the streaming server.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Enhanced Streaming server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New streaming connection from: " + clientSocket.getInetAddress());

                    // Handle each streaming connection in a separate thread
                    threadPool.execute(new EnhancedStreamingHandler(clientSocket));

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting streaming connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Streaming server failed to start: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Enhanced streaming handler that supports both single songs and continuous playlists.
     */
    private class EnhancedStreamingHandler implements Runnable {
        private final Socket clientSocket;

        public EnhancedStreamingHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Read streaming request from client
                String request = in.readLine();
                System.out.println("Streaming request: " + request);

                if (request == null || request.isEmpty()) {
                    out.println("ERROR|No streaming request provided");
                    return;
                }

                // Parse streaming request
                if (request.startsWith("STREAM|")) {
                    handleSongStream(request, out);
                } else if (request.startsWith("PLAYLIST|")) {
                    handlePlaylistStream(request, out);
                } else if (request.startsWith("CONTROL|")) {
                    handlePlaybackControl(request, out);
                } else {
                    out.println("ERROR|Unknown streaming command: " + request);
                }

            } catch (IOException e) {
                System.err.println("Error handling streaming client: " + e.getMessage());
            } finally {
                try {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    // Ignore close errors
                }
            }
        }

        /**
         * Handles streaming of a single song.
         * Format: STREAM|filepath|title|artistId|userId|playlistId
         */
        private void handleSongStream(String request, PrintWriter out) {
            String[] parts = request.split("\\|", 6);
            int playlistId = -1;

            if (parts.length < 5) {
                out.println("ERROR|Invalid STREAM format - missing user ID");
                return;
            }

            String filePath = parts[1];
            String title = parts[2];
            String artistId = parts[3];
            Integer userId;

            try {
                userId = Integer.parseInt(parts[4]);

                // Check if this is part of a playlist
                if (parts.length >= 6 && !parts[5].isEmpty()) {
                    playlistId = Integer.parseInt(parts[5]);
                }
            } catch (NumberFormatException e) {
                out.println("ERROR|Invalid user ID format");
                return;
            }

            System.out.println("Streaming " + title + " for user ID: " + userId +
                    (playlistId > 0 ? " (part of playlist " + playlistId + ")" : ""));

            // Create MusicStreamer and start streaming
            MusicStreamer streamer = new MusicStreamer();
            boolean success = streamer.streamAudioFile(filePath, clientSocket, userId, playlistId);

            if (!success) {
                System.err.println("Failed to stream: " + title + " for user: " + userId);
                out.println("ERROR|Failed to stream song");
            }
        }

        /**
         * Handles streaming of an entire playlist continuously.
         * Format: PLAYLIST|playlistId|userId
         */
        private void handlePlaylistStream(String request, PrintWriter out) {
            String[] parts = request.split("\\|", 3);
            if (parts.length < 3) {
                out.println("ERROR|Invalid PLAYLIST format - missing user ID");
                return;
            }

            int playlistId;
            int userId;

            try {
                playlistId = Integer.parseInt(parts[1]);
                userId = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                out.println("ERROR|Invalid ID format");
                return;
            }

            // Start the playlist in the PlaybackService
            Song firstSong = playbackService.startPlaylist(playlistId, userId);

            if (firstSong == null) {
                out.println("ERROR|Playlist is empty or not found");
                return;
            }

            Playlist playlist = playbackService.getCurrentPlaylist(userId);
            String playlistName = playlist != null ? playlist.getName() : "Unknown Playlist";

            System.out.println("Starting continuous playlist streaming: " + playlistName + " for user ID: " + userId);
            out.println("PLAYLIST_START|" + playlistName + "|" + playlistId);

            // Create streamer for continuous playlist streaming
            // We pass the first song's path, but the streamer will handle the entire playlist
            MusicStreamer streamer = new MusicStreamer();
            boolean success = streamer.streamAudioFile(firstSong.getFilePath(), clientSocket, userId, playlistId);

            if (!success) {
                out.println("ERROR|Failed to start playlist streaming");
            }
        }

        /**
         * Handles playback control commands.
         * Format: CONTROL|userId|command (where command is PAUSE, RESUME, STOP, NEXT, PREVIOUS)
         */
        private void handlePlaybackControl(String request, PrintWriter out) {
            String[] parts = request.split("\\|", 3);
            if (parts.length < 3) {
                out.println("ERROR|Invalid CONTROL format");
                return;
            }

            int userId;
            String command = parts[2];

            try {
                userId = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                out.println("ERROR|Invalid user ID format");
                return;
            }

            // Get the active streamer for this user
            MusicStreamer streamer = MusicStreamer.getStreamerForUser(userId);
            if (streamer == null) {
                out.println("ERROR|No active streaming session for user");
                return;
            }

            System.out.println("Processing control command: " + command + " for user: " + userId);

            switch (command.toUpperCase()) {
                case "PAUSE":
                    streamer.pause();
                    out.println("CONTROL_SUCCESS|PAUSED");
                    break;
                case "RESUME":
                    streamer.resume();
                    out.println("CONTROL_SUCCESS|RESUMED");
                    break;
                case "STOP":
                    streamer.stopStreaming();
                    out.println("CONTROL_SUCCESS|STOPPED");
                    break;
                case "NEXT":
                    if (streamer.skipToNext()) {
                        out.println("CONTROL_SUCCESS|NEXT");
                    } else {
                        out.println("CONTROL_ERROR|Cannot skip to next");
                    }
                    break;
                case "PREVIOUS":
                    if (streamer.skipToPrevious()) {
                        out.println("CONTROL_SUCCESS|PREVIOUS");
                    } else {
                        out.println("CONTROL_ERROR|Cannot skip to previous");
                    }
                    break;
                default:
                    out.println("ERROR|Unknown control command: " + command);
            }
        }
    }

    /**
     * Stops the streaming server.
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing streaming server: " + e.getMessage());
            }
        }
        preparedSongs.clear();
        preparedPlaylists.clear();
        System.out.println("Enhanced streaming server stopped");
    }
}