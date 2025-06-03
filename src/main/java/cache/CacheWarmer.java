package cache;

import factory.RepositoryFactory;
import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.SongRepositoryInterface;
import services.playlistServices.PlaylistService;
import services.songServices.AlbumService;
import services.songServices.ArtistService;
import services.songServices.SongService;
import songsAndArtists.Album;
import songsAndArtists.Artist;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A utility class that manages warming and refreshing of application caches.
 * This class follows the Singleton pattern and provides methods to pre-load
 * data into caches on startup and periodically refresh them during application runtime.
 *
 * Cache warming improves application performance by loading frequently accessed data
 * into memory proactively, rather than waiting for the first user request.
 *
 */
public class CacheWarmer {
    private static CacheWarmer instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private CacheWarmer() {}

    /**
     * Gets the singleton instance of CacheWarmer.
     *
     * @return The singleton instance
     */
    public static synchronized CacheWarmer getInstance() {
        if (instance == null) {
            instance = new CacheWarmer();
        }
        return instance;
    }

    /**
     * Warms all application caches on startup.
     * This method executes cache warming operations in parallel using CompletableFuture
     * to minimize startup time impact.
     */
    public static void warmAllCaches() {
        System.out.println("Starting cache warming...");
        long startTime = System.currentTimeMillis();

        // Warm caches that need warming in parallel
        CompletableFuture<Void> songWarmup = CompletableFuture.runAsync(() -> {
            warmSongCache();
        });

        CompletableFuture<Void> playlistWarmup = CompletableFuture.runAsync(() -> {
            warmPlaylistCache();
        });

        CompletableFuture<Void> albumWarmup = CompletableFuture.runAsync(() -> {
            warmAlbumCache();
        });

        CompletableFuture<Void> artistWarmup = CompletableFuture.runAsync(() -> {
            warmArtistCache();
        });

        // Wait for all warmups to complete
        CompletableFuture.allOf(songWarmup, playlistWarmup, albumWarmup, artistWarmup).join();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Cache warming completed in " + duration + "ms");
    }

    /**
     * Warms the song cache by loading all songs from the repository.
     * This method uses the SongService to load all songs, which will populate
     * the underlying cache for future queries.
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
     * Warms the playlist cache by loading all playlists from the repository.
     * This method uses the PlaylistService to load all playlists, which will populate
     * the underlying cache for future queries.
     */
    private static void warmPlaylistCache() {
        try {
            PlaylistService playlistService = PlaylistService.getInstance();

            System.out.println("Warming playlist cache...");
            long startTime = System.currentTimeMillis();

            // Pre-load all playlists
            List<Playlist> allPlaylists = playlistService.getAllPlaylists();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Loaded " + allPlaylists.size() + " playlists into cache in " + duration + "ms");

        } catch (Exception e) {
            System.err.println("Error warming playlist cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Warms the album cache by loading all albums from the repository.
     * This method uses the AlbumService to load all albums, which will populate
     * the underlying cache for future queries.
     */
    private static void warmAlbumCache() {
        try {
            AlbumService albumService = AlbumService.getInstance();

            System.out.println("Warming album cache...");
            long startTime = System.currentTimeMillis();

            // Pre-load all albums
            List<Album> allAlbums = albumService.getAllAlbums();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Loaded " + allAlbums.size() + " albums into cache in " + duration + "ms");

        } catch (Exception e) {
            System.err.println("Error warming album cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Warms the artist cache by loading all artists from the repository.
     * This method uses the ArtistService to load all artists, which will populate
     * the underlying cache for future queries.
     */
    private static void warmArtistCache() {
        try {
            ArtistService artistService = ArtistService.getInstance();

            System.out.println("Warming artist cache...");
            long startTime = System.currentTimeMillis();

            // Pre-load all artists
            List<Artist> allArtists = artistService.getAllArtists();

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Loaded " + allArtists.size() + " artists into cache in " + duration + "ms");

        } catch (Exception e) {
            System.err.println("Error warming artist cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Schedules periodic cache refresh at the specified interval.
     * This method uses a ScheduledExecutorService to run cache warming
     * operations at regular intervals, ensuring the cache data remains fresh.
     *
     * @param intervalMinutes The time interval between cache refreshes, in minutes
     */
    public void schedulePeriodicWarmup(long intervalMinutes) {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Performing scheduled cache refresh...");
            warmAllCaches();
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);

        System.out.println("Scheduled cache refresh every " + intervalMinutes + " minutes");
    }

    /**
     * Warms a specific cache identified by name.
     * This method provides a way to selectively warm a specific cache
     * rather than warming all caches.
     *
     * @param cacheName The name of the cache to warm (e.g., "song", "artist", "playlist")
     */
    public void warmCache(String cacheName) {
        System.out.println("Warming cache: " + cacheName);

        switch (cacheName.toLowerCase()) {
            case "song":
                warmSongCache();
                break;
            case "playlist":
                warmPlaylistCache();
                break;
            case "album":
                warmAlbumCache();
                break;
            case "artist":
                warmArtistCache();
                break;
            default:
                System.out.println("Unknown cache: " + cacheName);
        }
    }

    /**
     * Stops the scheduler and releases resources.
     * This method should be called when the application is shutting down
     * to ensure proper cleanup of the scheduler thread pool.
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