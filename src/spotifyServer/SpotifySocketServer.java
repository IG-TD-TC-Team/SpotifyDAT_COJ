package spotifyServer;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cache.CacheWarmer;

import spotifyServer.commandProcessor.*;

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
     * Initializes the command server and streaming server with a thread pool.
     */
    public SpotifySocketServer() {
        // Warm caches before creating servers
        System.out.println("Pre-warming caches...");
        CacheWarmer.warmAllCaches();

        // Create a thread pool to manage client connections
        this.threadPool = Executors.newCachedThreadPool();

        // Initialize StreamingServer
        this.streamingServer = StreamingServer.getInstance(STREAMING_PORT, threadPool);
        // Initialize command processor
        AbstractProcessor commandProcessor = initializeCommandProcessor();
        //Initialize CommandServer
        this.commandServer = CommandServer.getInstance(COMMAND_PORT, threadPool, commandProcessor);

    }

    /**
     * Initializes the command processor with a chain of handlers.
     * @return CommandProcessor instance with the chain of handlers
     */
    private AbstractProcessor initializeCommandProcessor() {

        return CommandProcessorFactory.createProcessorChain();
    }

    /**
     * Starts the command and streaming servers.
     * This method starts both servers in separate threads to handle incoming connections.
     */
    public void start() {
        // Start both servers in separate threads
        new Thread(() -> commandServer.start()).start();
        new Thread(() -> streamingServer.start()).start();

        System.out.println("Spotify Server started:");
        System.out.println("Command server listening on port " + COMMAND_PORT);
        System.out.println("Streaming server listening on port " + STREAMING_PORT);
    }

    /**
     * Stops the command and streaming servers.
     * This method stops both servers and shuts down the thread pool.
     */
    public void stop() {
        commandServer.stop();
        streamingServer.stop();
        cacheWarmer.shutdown();
        threadPool.shutdown();
        System.out.println("Spotify Server stopped");
    }

    /**
     * Main method to start the SpotifySocketServer.
     * This method creates an instance of SpotifySocketServer and starts it.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpotifySocketServer server = new SpotifySocketServer();
        server.start();
    }
}