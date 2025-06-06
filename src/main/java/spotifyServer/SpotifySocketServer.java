package spotifyServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import cache.CacheWarmer;
import spotifyServer.commandProcessor.*;

/**
 * SpotifySocketServer with proper two-port architecture and production-ready setup.
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
        System.out.println("==========================================");
        System.out.println("  Initializing Spotify Server...         ");
        System.out.println("==========================================");

        // Initialize data storage
        initializeDataStorage();

        // Initialize music storage
        initializeMusicStorage();

        // Fix song paths to use the music extractor system
        fixSongPaths();

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
     * Initializes data storage and ensures data directory exists with proper files.
     * This method will automatically extract resources from JAR if running in production mode.
     */
    private void initializeDataStorage() {
        System.out.println("Initializing data storage...");

        try {
            // Force initialization of at least one repository to trigger resource extraction
            // This will create the data directory and extract files if needed
            factory.RepositoryFactory.getInstance().getSongRepository().findAll();

            System.out.println("✓ Data storage initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing data storage: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize data storage", e);
        }
    }

    /**
     * Initializes music storage by ensuring the music directory exists and extracting
     * music files from JAR if running in production mode.
     */
    private void initializeMusicStorage() {
        System.out.println("Initializing music storage...");

        try {
            persistence.MusicResourceExtractor.ensureMusicDirectory();
            System.out.println("✓ Music storage initialization completed");
        } catch (Exception e) {
            System.err.println("Error initializing music storage: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize music storage", e);
        }
    }

    /**
     * Fixes existing song paths in the database to use the MusicResourceExtractor system.
     * This ensures all song paths point to the correct music directory location.
     */
    private void fixSongPaths() {
        System.out.println("Updating song paths to use music extractor system...");

        try {
            // Run the path fixer to update all song paths in the database
            seeding.FixExistingSongPaths.main(new String[]{});
            System.out.println("✓ Song paths updated successfully");
        } catch (Exception e) {
            System.err.println("Warning: Error updating song paths: " + e.getMessage());
            // Don't throw exception here as this is not critical for server startup
            // The server can still function with old paths, just might have some file access issues
            e.printStackTrace();
        }
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
        return CommandProcessorFactory.getInstance().createProcessorChainInstance();
    }

    /**
     * Starts both the command and streaming servers.
     *
     * - Command server handles text-based protocol on port 45000
     * - Streaming server handles binary MP3 data on port 45001
     */
    public void start() {
        System.out.println("🚀 Starting Spotify Server with two-port architecture...");

        // Start the streaming server first
        new Thread(() -> streamingServer.start()).start();

        // Then start the command server
        new Thread(() -> commandServer.start()).start();

        System.out.println("✓ Spotify Server started successfully!");
        System.out.println("📡 Command server listening on port " + COMMAND_PORT);
        System.out.println("🎵 Streaming server listening on port " + STREAMING_PORT);
        System.out.println();
        System.out.println("Server is ready to accept connections!");
        System.out.println("Press Ctrl+C to stop the server");
    }

    /**
     * Stops both servers and cleans up resources.
     */
    public void stop() {
        System.out.println("🛑 Stopping Spotify Server...");

        // Stop both servers
        commandServer.stop();
        streamingServer.stop();

        // Shutdown cache warmer
        cacheWarmer.shutdown();

        // Shutdown thread pool
        threadPool.shutdown();

        System.out.println("✓ Spotify Server stopped gracefully");
    }

    /**
     * Displays server information and data statistics.
     */
    private void displayServerInfo() {
        System.out.println("==========================================");
        System.out.println("  SERVER INFORMATION                     ");
        System.out.println("==========================================");

        try {
            var songRepo = factory.RepositoryFactory.getInstance().getSongRepository();
            var artistRepo = factory.RepositoryFactory.getInstance().getArtistRepository();
            var albumRepo = factory.RepositoryFactory.getInstance().getAlbumRepository();
            var playlistRepo = factory.RepositoryFactory.getInstance().getPlaylistRepository();
            var userRepo = factory.RepositoryFactory.getInstance().getUserRepository();

            System.out.println("📊 Database Statistics:");
            System.out.println("   Songs: " + songRepo.findAll().size());
            System.out.println("   Artists: " + artistRepo.findAll().size());
            System.out.println("   Albums: " + albumRepo.findAll().size());
            System.out.println("   Playlists: " + playlistRepo.findAll().size());
            System.out.println("   Users: " + userRepo.findAll().size());

            // Show music files info
            java.util.List<String> musicFiles = persistence.MusicResourceExtractor.listExtractedMusicFiles();
            System.out.println("   Music Files: " + musicFiles.size());

        } catch (Exception e) {
            System.out.println("📊 Database Statistics: Unable to load (will be available after first access)");
        }

        System.out.println();
        System.out.println("🌐 Network Configuration:");
        System.out.println("   Command Port: " + COMMAND_PORT);
        System.out.println("   Streaming Port: " + STREAMING_PORT);

        // Show music directory info
        try {
            java.nio.file.Path musicDir = persistence.MusicResourceExtractor.ensureMusicDirectory();
            System.out.println("   Music Directory: " + musicDir.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("   Music Directory: Unable to determine");
        }

        System.out.println();
    }

    /**
     * Main method to start the SpotifySocketServer.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  🎵 SPOTIFY SERVER - PRODUCTION MODE   ");
        System.out.println("==========================================");

        // Display environment info
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("User: " + System.getProperty("user.name"));
        System.out.println();

        try {
            // Create and initialize the server
            SpotifySocketServer server = new SpotifySocketServer();

            // Display server information
            server.displayServerInfo();

            // Start the server
            server.start();

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received...");
                server.stop();
                System.out.println("👋 Thank you for using Spotify Server!");
            }));

            // Keep the main thread alive
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Main thread interrupted");
            }

        } catch (Exception e) {
            System.err.println("Failed to start Spotify Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}