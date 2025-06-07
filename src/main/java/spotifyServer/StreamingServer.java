package spotifyServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * MODIFICATIONS: Simplified StreamingServer for PURE AUDIO STREAMING ONLY.
 * Removed playlist logic, control commands, and text protocol mixing.
 * Now handles single song requests with raw MP3 data only.
 */
public class StreamingServer {
    private static StreamingServer instance;

    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean running = false;

    // MODIFICATIONS: Removed caching, playlist support, and PlaybackService dependency

    /**
     * Private constructor enforcing the Singleton pattern.
     */
    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
        // MODIFICATIONS: Removed PlaybackService dependency
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
     * MODIFICATIONS: Removed preparation methods - no longer needed for single-song streaming.
     */

    /**
     * Starts the streaming server.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Pure Audio Streaming server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New audio streaming connection from: " + clientSocket.getInetAddress());

                    // Handle each streaming connection in a separate thread
                    threadPool.execute(new PureAudioStreamingHandler(clientSocket));

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
     * MODIFICATIONS: Simplified streaming handler for PURE AUDIO ONLY.
     * No text protocol mixing, no playlist logic, no control commands.
     */
    private class PureAudioStreamingHandler implements Runnable {
        private final Socket clientSocket;

        public PureAudioStreamingHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                // Read single streaming request from client
                String request = in.readLine();
                System.out.println("Audio streaming request: " + request);

                if (request == null || request.isEmpty()) {
                    System.err.println("No streaming request provided");
                    return;
                }

                // MODIFICATIONS: Only handle STREAM requests - no PLAYLIST or CONTROL
                if (request.startsWith("STREAM|")) {
                    handlePureAudioStream(request);
                } else {
                    System.err.println("Unknown streaming command: " + request);
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
         * MODIFICATIONS: Simplified to handle PURE AUDIO STREAMING only.
         * Format: STREAM|filepath|title|artistId|userId
         * No playlist logic, no text responses - just raw MP3 data.
         */
        private void handlePureAudioStream(String request) {
            String[] parts = request.split("\\|", 5);

            if (parts.length < 5) {
                System.err.println("Invalid STREAM format - expected: STREAM|filepath|title|artistId|userId");
                return;
            }

            String filePath = parts[1];
            String title = parts[2];
            String artistId = parts[3];
            Integer userId;

            try {
                userId = Integer.parseInt(parts[4]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid user ID format");
                return;
            }

            System.out.println("Starting pure audio stream: " + title + " for user ID: " + userId);

            // MODIFICATIONS: Create MusicStreamer for SINGLE SONG streaming only
            MusicStreamer streamer = new MusicStreamer();
            boolean success = streamer.streamAudioFile(filePath, clientSocket, userId);

            if (!success) {
                System.err.println("Failed to stream: " + title + " for user: " + userId);
            } else {
                System.out.println("Successfully completed streaming: " + title + " for user: " + userId);
            }

            // MODIFICATIONS: No response messages - client detects completion via audio stream end
        }

        /**
         * MODIFICATIONS: Removed playlist and control command handlers.
         * All playlist navigation now handled via command port (45000).
         */
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
        // MODIFICATIONS: Removed cache clearing since we don't cache anymore
        System.out.println("Pure audio streaming server stopped");
    }
}