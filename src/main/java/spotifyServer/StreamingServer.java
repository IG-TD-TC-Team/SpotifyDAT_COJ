package spotifyServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Dedicated streaming server for handling audio streaming requests.
 *
 * <p>This server operates on a separate port from the command server and is responsible
 * for streaming raw MP3 audio data to clients. It implements the Singleton pattern to
 * ensure only one streaming server instance exists in the application.</p>
 *
 * <p>The server accepts streaming requests in the format:
 * {@code STREAM|filepath|title|artistId|userId} and responds with pure audio data
 * without any text protocol mixing.</p>
 *
 * <p>Each client connection is handled in a separate thread to support concurrent
 * streaming to multiple clients simultaneously.</p>
 */
public class StreamingServer {
    private static StreamingServer instance;

    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private boolean running = false;

    /**
     * Private constructor enforcing the Singleton pattern.
     *
     * @param port the port number for the streaming server
     * @param threadPool the thread pool for handling concurrent client connections
     */
    private StreamingServer(int port, ExecutorService threadPool) {
        this.port = port;
        this.threadPool = threadPool;
    }

    /**
     * Returns the singleton instance of StreamingServer.
     *
     * @param port the port number for the streaming server
     * @param threadPool the thread pool for handling concurrent client connections
     * @return the singleton StreamingServer instance
     */
    public static synchronized StreamingServer getInstance(int port, ExecutorService threadPool) {
        if (instance == null) {
            instance = new StreamingServer(port, threadPool);
        }
        return instance;
    }

    /**
     * Returns the existing singleton instance of StreamingServer.
     *
     * @return the singleton StreamingServer instance
     * @throws IllegalStateException if the server has not been initialized
     */
    public static StreamingServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StreamingServer not initialized. Call getInstance(port, threadPool) first.");
        }
        return instance;
    }

    /**
     * Starts the streaming server and begins accepting client connections.
     *
     * <p>This method runs in a blocking loop, accepting client connections and
     * delegating each connection to a separate thread for processing.</p>
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Audio Streaming server started on port " + port);

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
     * Handler for individual audio streaming client connections.
     *
     * <p>This inner class is used because:</p>
     * <ul>
     *   <li>It has access to the outer class's context and resources</li>
     *   <li>It's logically coupled to the StreamingServer and not used elsewhere</li>
     *   <li>It keeps related streaming functionality encapsulated within the server</li>
     *   <li>It can access private members of the outer class if needed</li>
     * </ul>
     *
     * <p>Each instance handles a single client connection, reading the streaming
     * request and delegating to the appropriate streaming method.</p>
     */
    private class PureAudioStreamingHandler implements Runnable {
        private final Socket clientSocket;

        /**
         * Creates a new streaming handler for the given client socket.
         *
         * @param clientSocket the socket connection to the client
         */
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
         * Handles a pure audio streaming request.
         *
         * <p>Parses the request format and initiates audio streaming for the specified
         * file. The request must follow the format:</p>
         * {@code STREAM|filepath|title|artistId|userId}
         *
         * @param request the streaming request string to parse and handle
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

            // Create MusicStreamer for single song streaming
            MusicStreamer streamer = new MusicStreamer();
            boolean success = streamer.streamAudioFile(filePath, clientSocket, userId);

            if (!success) {
                System.err.println("Failed to stream: " + title + " for user: " + userId);
            } else {
                System.out.println("Successfully completed streaming: " + title + " for user: " + userId);
            }
        }
    }

    /**
     * Stops the streaming server and closes all resources.
     *
     * <p>This method gracefully shuts down the server by closing the server socket
     * and setting the running flag to false.</p>
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

        System.out.println("Audio streaming server stopped");
    }
}