package spotifyServer;

import songsAndArtists.Song;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * Hybrid server that handles both incoming socket connections and
 * provides direct streaming services to command processors.
 */
public class StreamingServer {
    // Singleton instance
    private static StreamingServer instance;

    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final MusicStreamer musicStreamer;

    /**
     * Private constructor for Singleton pattern.
     *
     * @param port the port number to listen on
     * @param threadPool the thread pool to handle client connections
     * musicStreamer the music streamer instance
     */
    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
        this.musicStreamer = new MusicStreamer();
    }

    /**
     * Get the singleton instance of StreamingServer.
     */
    public static synchronized StreamingServer getInstance(int port, ExecutorService threadPool) {
        if (instance == null) {
            instance = new StreamingServer(port, threadPool);
        }
        return instance;
    }

    /**
     * Get an existing instance or throw an exception if not initialized.
     */
    public static StreamingServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StreamingServer not initialized. Call getInstance(port, threadPool) first.");
        }
        return instance;
    }

    /**
     * Method for streaming a single song directly from command processors.
     */
    public void streamSong(final Socket clientSocket, final Song song) {
        if (!running) {
            System.err.println("Warning: Streaming server is not running, but attempting direct stream anyway");
        }

        threadPool.execute(() -> {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("STREAMING_START|" + song.getTitle());

                boolean success = musicStreamer.streamAudioFile(song.getFilePath(), clientSocket);

                if (success) {
                    out.println("STREAMING_COMPLETE|Song playback finished");
                } else {
                    out.println("ERROR|Failed to stream the song");
                }
            } catch (IOException e) {
                System.err.println("Error streaming song: " + e.getMessage());
            }
        });
    }

    /**
     * Method for streaming a playlist directly from command processors.
     */
    public void streamPlaylist(final Socket clientSocket, final LinkedList<Song> songs, final String playlistName) {
        if (!running) {
            System.err.println("Warning: Streaming server is not running, but attempting direct stream anyway");
        }

        threadPool.execute(() -> {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("PLAYLIST_START|" + playlistName + "|" + songs.size() + " songs");

                int songCount = 0;
                int totalSongs = songs.size();

                for (Song song : songs) {
                    songCount++;
                    out.println("SONG_START|" + song.getTitle() + "|" + songCount + " of " + totalSongs);

                    boolean success = musicStreamer.streamAudioFile(song.getFilePath(), clientSocket);

                    if (!success) {
                        out.println("ERROR|Failed to stream song: " + song.getTitle());
                        return;
                    }

                    try {
                        Thread.sleep(500); // Small pause between songs
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                out.println("PLAYLIST_COMPLETE|Playlist playback finished");
            } catch (IOException e) {
                System.err.println("Error streaming playlist: " + e.getMessage());
            }
        });
    }

    /**
     * Original method to start the server socket and listen for connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Streaming server started on port " + port);

            // Start a new thread to listen for connections
            threadPool.execute(() -> {
                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        MusicStreamHandler streamHandler = new MusicStreamHandler(clientSocket, musicStreamer);
                        threadPool.execute(streamHandler);
                        System.out.println("New streaming connection accepted from " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("Error accepting streaming connection: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Streaming server failed to start: " + e.getMessage());
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
        System.out.println("Streaming server stopped");
    }
}