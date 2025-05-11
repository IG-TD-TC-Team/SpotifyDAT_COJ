// Create src/cache/CacheWarmer.java
package cache;

import factory.RepositoryFactory;
import persistence.interfaces.SongRepositoryInterface;
import services.songServices.SongService;
import songsAndArtists.Song;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheWarmer {
    private static CacheWarmer instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private CacheWarmer() {}

    public static synchronized CacheWarmer getInstance() {
        if (instance == null) {
            instance = new CacheWarmer();
        }
        return instance;
    }

    /**
     * Warm all caches on application startup
     */
    public static void warmAllCaches() {
        System.out.println("Starting cache warming...");
        long startTime = System.currentTimeMillis();

        // Warm caches that need warming in parallel
        CompletableFuture<Void> songWarmup = CompletableFuture.runAsync(() -> {
            warmSongCache();
        });

        // For later use, here add others

        // Wait for all warmups to complete
        CompletableFuture.allOf(songWarmup).join();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Cache warming completed in " + duration + "ms");
    }

    /**
     * Warm song cache if enabled
     */
    private static void warmSongCache() {
        try {
            SongService songService = SongService.getInstance();

            System.out.println("Warming song cache...");
            long startTime = System.currentTimeMillis();

            // Pre-load all songs
            List<Song> allSongs = songService.getAllSongs();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Loaded " + allSongs.size() + " songs into cache in " + duration + "ms");

        } catch (Exception e) {
            System.err.println("Error warming song cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Schedule periodic cache refresh
     */
    public void schedulePeriodicWarmup(long intervalMinutes) {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Performing scheduled cache refresh...");
            warmAllCaches();
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);

        System.out.println("Scheduled cache refresh every " + intervalMinutes + " minutes");
    }

    /**
     * Warm specific cache by name
     */
    public void warmCache(String cacheName) {
        System.out.println("Warming cache: " + cacheName);

        switch (cacheName.toLowerCase()) {
            case "song":
                warmSongCache();
                break;
            // Add other caches as needed
            default:
                System.out.println("Unknown cache: " + cacheName);
        }
    }

    /**
     * Stop the scheduler when application shuts down
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}