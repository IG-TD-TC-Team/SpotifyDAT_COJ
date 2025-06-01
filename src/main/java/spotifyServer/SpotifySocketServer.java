package spotifyServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cache.CacheWarmer;
import spotifyServer.commandProcessor.*;

/**
 * SpotifySocketServer with proper two-port architecture while maintaining
 * the Chain of Responsibility pattern for command processing.
 *
 * Architecture:
 * - Port 45000: Command processing (text-based protocol)
 * - Port 45001: Music streaming (binary data)
 *
 * The Chain of Responsibility handles only command processing.
 * Streaming is handled separately to avoid mixing text and binary data.
 */
public class SpotifySocketServer {
    // Port constants
    public static final int COMMAND_PORT = 45000;
    public static final int STREAMING_PORT = 45001;

    private CommandServer commandServer;
    private StreamingServer streamingServer;
    private ExecutorService threadPool;
    private final CacheWarmer cacheWarmer = CacheWarmer.getInstance();

    /**
     * Constructor for SpotifySocketServer.
     * Initializes both command and streaming servers.
     */
    public SpotifySocketServer() {
        // Warm caches before starting the server
        System.out.println("Pre-warming caches...");
        CacheWarmer.warmAllCaches();

        // Create a thread pool to manage client connections
        this.threadPool = Executors.newCachedThreadPool();

        // Initialize StreamingServer first (it's a dependency for some processors)
        this.streamingServer = StreamingServer.getInstance(STREAMING_PORT, threadPool);

        // Initialize the processor chain for command processing
        AbstractProcessor commandProcessorChain = createProcessorChain();

        // Initialize CommandServer with the processor chain
        this.commandServer = CommandServer.getInstance(COMMAND_PORT, threadPool, commandProcessorChain);

        System.out.println("Spotify Server initialized with two-port architecture");
        System.out.println("Command processing uses Chain of Responsibility pattern");
        System.out.println("Streaming handled separately on dedicated port");
    }

    /**
     * Creates the processor chain for handling commands.
     *
     * Each processor handles only command processing.
     * When streaming is needed, processors send instructions to the client
     * to connect to the streaming port.
     *
     * @return The first processor in the chain
     */
    private AbstractProcessor createProcessorChain() {
        // Use the factory to create the processor chain
        // This is like ordering a car from the factory instead of building it yourself
        return CommandProcessorFactory.getInstance().createProcessorChainInstance();
    }

    /**
     * Starts both the command and streaming servers.
     *
     * - Command server handles text-based protocol on port 45000
     * - Streaming server handles binary MP3 data on port 45001
     */
    public void start() {
        System.out.println("Starting Spotify Server with two-port architecture...");

        // Start the streaming server first
        new Thread(() -> streamingServer.start()).start();

        // Then start the command server
        new Thread(() -> commandServer.start()).start();

        System.out.println("Spotify Server started successfully!");
        System.out.println("Command server listening on port " + COMMAND_PORT);
        System.out.println("Streaming server listening on port " + STREAMING_PORT);
    }

    /**
     * Stops both servers and cleans up resources.
     */
    public void stop() {
        System.out.println("Stopping Spotify Server...");

        // Stop both servers
        commandServer.stop();
        streamingServer.stop();

        // Shutdown cache warmer
        cacheWarmer.shutdown();

        // Shutdown thread pool
        threadPool.shutdown();

        System.out.println("Spotify Server stopped");
    }

    /**
     * Main method to start the SpotifySocketServer.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  Spotify Server - Two Port Architecture  ");
        System.out.println("==========================================");

        // Create and start the server
        SpotifySocketServer server = new SpotifySocketServer();
        server.start();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown signal received...");
            server.stop();
        }));

        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted");
        }
    }
}