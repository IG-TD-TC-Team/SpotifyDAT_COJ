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

    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
        this.playbackService = PlaybackService.getInstance();
    }

    public static synchronized StreamingServer getInstance(int port, ExecutorService threadPool) {
        if (instance == null) {
            instance = new StreamingServer(port, threadPool);
        }
        return instance;
    }

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
     * Starts the streaming server on the dedicated streaming port.
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
     * Inner class to handle individual streaming connections.
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