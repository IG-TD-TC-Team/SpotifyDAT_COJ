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
 * Enhanced StreamingServer with better integration with PlaybackService.
 * Handles binary MP3 streaming on a dedicated port.
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
     * Initializes the server with the specified port and thread pool,
     * and obtains a reference to the PlaybackService singleton.
     *
     * @param port The port on which the streaming server will listen
     * @param threadPool The executor service for handling client connections
     */
    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
        this.playbackService = PlaybackService.getInstance();
    }

    /**
     * Returns the singleton instance of StreamingServer, creating it if it doesn't exist.
     * This method must be called with port and thread pool parameters before any other
     * methods can be used.
     *
     * @param port The port on which the streaming server will listen
     * @param threadPool The executor service for handling client connections
     * @return The singleton instance of StreamingServer
     */
    public static synchronized StreamingServer getInstance(int port, ExecutorService threadPool) {
        if (instance == null) {
            instance = new StreamingServer(port, threadPool);
        }
        return instance;
    }

    /**
     * Returns the existing singleton instance of StreamingServer.
     * This method must be called after getInstance(port, threadPool) has been called at least once.
     *
     * @return The singleton instance of StreamingServer
     * @throws IllegalStateException if the server has not been initialized with port and thread pool
     */
    public static StreamingServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StreamingServer not initialized. Call getInstance(port, threadPool) first.");
        }
        return instance;
    }

    /**
     * Prepares a single song for streaming by adding it to the prepared songs cache.
     * This method is typically called before a client connects to stream the song,
     * allowing the server to pre-load the song information.
     *
     * @param song The song to prepare for streaming
     */
    public void prepareForStreaming(Song song) {
        if (song != null) {
            preparedSongs.put(song.getSongId(), song);
            System.out.println("Prepared song for streaming: " + song.getTitle());
        }
    }

    /**
     * Prepares a playlist for streaming by adding it to the prepared playlists cache.
     * This method creates a deep copy of the songs list to prevent modifications
     * to the original playlist affecting the streaming session.
     *
     * @param playlist The playlist to prepare for streaming
     * @param songs The ordered list of songs in the playlist
     */
    public void prepareForPlaylistStreaming(Playlist playlist, LinkedList<Song> songs) {
        if (playlist != null && songs != null) {
            preparedPlaylists.put(playlist.getPlaylistID(), new LinkedList<>(songs));
            System.out.println("Prepared playlist for streaming: " + playlist.getName() + " (" + songs.size() + " songs)");
        }
    }

    /**
     * Starts the streaming server, accepting client connections on the configured port.
     * Each connection is handled in a separate thread from the thread pool.
     *
     * This method blocks until the server is stopped or encounters an error.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Streaming server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New streaming connection from: " + clientSocket.getInetAddress());

                    // Handle each streaming connection in a separate thread
                    threadPool.execute(new StreamingHandler(clientSocket));

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
     * Stops the streaming server, closing the server socket and clearing all caches.
     * This method should be called when shutting down the application or when
     * the server needs to be restarted.
     */
    private class StreamingHandler implements Runnable {
        private final Socket clientSocket;

        public StreamingHandler(Socket clientSocket) {
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
                } else {
                    out.println("ERROR|Unknown streaming command");
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
         * Handles streaming of a single song to the client.
         *
         *
         * @param request The STREAM request string from the client
         * @param out The PrintWriter for sending text responses to the client
         */
        private void handleSongStream(String request, PrintWriter out) {
            // Updated format: STREAM|filepath|title|artistId|userId|playlistId
            String[] parts = request.split("\\|", 6);
            int playlistId = -1; // Default to -1 for non-playlist songs

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

            // Create MusicStreamer and register it with the user ID
            MusicStreamer streamer = new MusicStreamer();

            // Stream the song
            boolean success = streamer.streamAudioFile(filePath, clientSocket, userId, playlistId);

            if (!success) {
                System.err.println("Failed to stream: " + title + " for user: " + userId);
            }
        }

        /**
         * Handles streaming of an entire playlist to the client.
         *
         *
         * @param request The PLAYLIST request string from the client
         * @param out The PrintWriter for sending text responses to the client
         */
        private void handlePlaylistStream(String request, PrintWriter out) {
            // Format: PLAYLIST|playlistId|userId
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

            System.out.println("Starting playlist: " + playlistName + " for user ID: " + userId);
            out.println("PLAYLIST_START|" + playlistName + "|" + playlistId);

            // Stream the first song
            out.println("SONG_START|" + firstSong.getTitle());

            // Create streamer for the first song
            MusicStreamer streamer = new MusicStreamer();
            boolean success = streamer.streamAudioFile(firstSong.getFilePath(), clientSocket, userId, playlistId);

            if (!success) {
                out.println("ERROR|Failed to stream first song");
            }
        }
    }

    /// -----------------------STOP------------------------------------- ///
    /**
     * Stops the streaming server, closing the server socket and clearing all caches.
     * This method should be called when shutting down the application or when
     * the server needs to be restarted.
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
        System.out.println("Streaming server stopped");
    }
}