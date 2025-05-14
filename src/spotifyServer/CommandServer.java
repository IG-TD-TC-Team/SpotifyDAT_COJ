package spotifyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import spotifyServer.commandProcessor.*;

/**
 * Enhanced CommandServer that implements a pure Chain of Responsibility pattern.
 *
 * This server now passes client connections directly to the processor chain,
 * eliminating the need for intermediate handlers. Each processor in the chain
 * handles its own socket communication and streaming.
 */
public class CommandServer {
    private final int port;
    private final ExecutorService threadPool;
    private final AbstractProcessor commandProcessorChain;
    private ServerSocket serverSocket;
    private boolean running = false;

    // Singleton instance
    private static CommandServer instance;

    /**
     * Private constructor for Singleton pattern.
     *
     * @param port The port number to listen on
     * @param threadPool The thread pool to handle client connections
     * @param commandProcessorChain The first processor in the chain
     */
    private CommandServer(int port, ExecutorService threadPool, AbstractProcessor commandProcessorChain) {
        this.port = port;
        this.threadPool = threadPool;
        this.commandProcessorChain = commandProcessorChain;
    }

    /**
     * Get the singleton instance of CommandServer.
     *
     * @param port The port number to listen on
     * @param threadPool The thread pool to handle client connections
     * @param commandProcessorChain The processor chain to handle commands
     */
    public static synchronized CommandServer getInstance(int port, ExecutorService threadPool,
                                                         AbstractProcessor commandProcessorChain) {
        if (instance == null) {
            instance = new CommandServer(port, threadPool, commandProcessorChain);
        }
        return instance;
    }

    /**
     * Get an existing instance or throw an exception if not initialized.
     */
    public static CommandServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CommandServer not initialized. Call getInstance(port, threadPool, commandProcessor) first.");
        }
        return instance;
    }

    /**
     * Starts the command server, accepting incoming client connections.
     * Each connection is handled directly by the processor chain.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Enhanced Command Server started on port " + port);
            System.out.println("Using pure Chain of Responsibility pattern for command processing");

            while (running) {
                try {
                    // Accept incoming client connections
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connection accepted from " + clientSocket.getInetAddress());

                    // Create a new processor chain instance for this client
                    // This ensures thread safety and client isolation
                    AbstractProcessor clientProcessorChain = createProcessorChainInstance();

                    // Handle the client connection directly through the processor chain
                    threadPool.execute(() -> {
                        try {
                            // Set the client socket on the first processor
                            clientProcessorChain.setClientSocket(clientSocket);

                            // Start processing client commands
                            clientProcessorChain.processClient();

                        } catch (IOException e) {
                            System.err.println("Error setting up client processor: " + e.getMessage());
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // Ignore close errors
                            }
                        }
                    });

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Command server failed to start: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Creates a new instance of the processor chain.
     * This ensures each client gets its own chain instance for thread safety.
     *
     * @return A new processor chain instance
     */
    private AbstractProcessor createProcessorChainInstance() {
        // Create new instances of each processor
        HelpCommandProcessor helpProcessor = new HelpCommandProcessor();
        PlayCommandProcessor playProcessor = new PlayCommandProcessor();
        PlaylistCommandProcessor playlistProcessor = new PlaylistCommandProcessor();
        SearchCommandProcessor searchProcessor = new SearchCommandProcessor();
        DefaultCommandProcessor defaultProcessor = new DefaultCommandProcessor();

        // Connect the chain
        helpProcessor.setNextProcessor(playProcessor);
        playProcessor.setNextProcessor(playlistProcessor);
        playlistProcessor.setNextProcessor(searchProcessor);
        searchProcessor.setNextProcessor(defaultProcessor);

        return helpProcessor;
    }

    /**
     * Stops the command server, closing the server socket and releasing resources.
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Command server stopped");
            } catch (IOException e) {
                System.err.println("Error closing command server: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if the server is currently running.
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}