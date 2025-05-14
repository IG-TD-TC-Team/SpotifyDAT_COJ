package spotifyServer;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * Enhanced StreamingServer that handles binary MP3 streaming on a dedicated port.
 * Keeps binary streaming completely separate from text-based command processing.
 */
public class StreamingServer {
    private static StreamingServer instance;

    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final MusicStreamer musicStreamer;

    // Cache for prepared content
    private final ConcurrentHashMap<Integer, Song> preparedSongs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, LinkedList<Song>> preparedPlaylists = new ConcurrentHashMap<>();

    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
        this.musicStreamer = new MusicStreamer();
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
            // Format: STREAM|filepath|title|artistId
            String[] parts = request.split("\\|", 4);
            if (parts.length < 4) {
                out.println("ERROR|Invalid STREAM format");
                return;
            }

            String filePath = parts[1];
            String title = parts[2];
            String artistId = parts[3];

            System.out.println("Streaming song: " + title);
            out.println("STREAMING_START|" + title);

            // Force flush before switching to binary mode
            out.flush();

            // Stream the actual MP3 file
            boolean success = musicStreamer.streamAudioFile(filePath, clientSocket);

            if (success) {
                System.out.println("Successfully streamed: " + title);
            } else {
                System.err.println("Failed to stream: " + title);
            }
        }

        private void handlePlaylistStream(String request, PrintWriter out) {
            // Format: PLAYLIST|filepath1,filepath2,...|playlistName
            String[] parts = request.split("\\|", 3);
            if (parts.length < 3) {
                out.println("ERROR|Invalid PLAYLIST format");
                return;
            }

            String[] filePaths = parts[1].split(",");
            String playlistName = parts[2];

            System.out.println("Streaming playlist: " + playlistName + " (" + filePaths.length + " songs)");
            out.println("PLAYLIST_START|" + playlistName + "|" + filePaths.length + " songs");
            out.flush();

            // Stream each song in the playlist
            int songCount = 0;
            for (String filePath : filePaths) {
                songCount++;

                // Send song start message
                out.println("SONG_START|Song " + songCount + " of " + filePaths.length);
                out.flush();

                // Stream the song
                boolean success = musicStreamer.streamAudioFile(filePath, clientSocket);
                if (!success) {
                    out.println("ERROR|Failed to stream song " + songCount);
                    out.flush();
                    break;
                }

                // Small pause between songs
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            out.println("PLAYLIST_COMPLETE");
            out.flush();
        }
    }

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