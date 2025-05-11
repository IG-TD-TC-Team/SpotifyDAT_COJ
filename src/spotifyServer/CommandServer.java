package spotifyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import spotifyServer.commandProcessor.*;

/**
 * CommandServer class is responsible for accepting incoming client connections
 * and processing commands using the provided command processor.
 * It uses a thread pool to handle multiple client connections concurrently.
 */
public class CommandServer {
    private final int port;
    private final ExecutorService threadPool;
    private final AbstractProcessor commandProcessor;
    private ServerSocket serverSocket;
    private boolean running = false;

    /// Singleton instance
    private static CommandServer instance;


    /**
     * Private constructor for Singleton pattern.
     *
     * @param port            the port number to listen on
     * @param threadPool      the thread pool to handle client connections
     * @param commandProcessor the command processor to handle incoming commands
     */
    private CommandServer(int port, ExecutorService threadPool, AbstractProcessor commandProcessor) {
        this.port = port;
        this.threadPool = threadPool;
        this.commandProcessor = commandProcessor;
    }
    /**
     * Get the singleton instance of CommandServer.
     *
     * @param port            the port number to listen on
     * @param threadPool      the thread pool to handle client connections
     * @param commandProcessor the command processor to handle incoming commands
     */
    public static synchronized CommandServer getInstance(int port, ExecutorService threadPool, AbstractProcessor commandProcessor) {
        if (instance == null) {
            instance = new CommandServer(port, threadPool, commandProcessor);
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
     * Each connection is handled in a separate thread using the provided thread pool.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Command server started on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientCommandHandler clientHandler = new ClientCommandHandler(clientSocket, commandProcessor);
                    // Creates a new thread for each client connection
                    threadPool.execute(clientHandler);
                    System.out.println("New command connection accepted from " + clientSocket.getInetAddress());
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
     * Stops the command server, closing the server socket and releasing resources.
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing command server: " + e.getMessage());
            }
        }
    }
}