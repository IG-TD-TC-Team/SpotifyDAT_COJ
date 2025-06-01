import services.songServices.SongService;
import songsAndArtists.Song;
import cache.*;
import factory.RepositoryFactory;
import persistence.interfaces.SongRepositoryInterface;

import java.lang.reflect.Field;
import java.util.Map;

public class CacheTest {
    public static void main(String[] args) {
        SongService songService = SongService.getInstance();

        System.out.println("=== Cache Test ===");

        // Initial cache state
        inspectCacheViaReflection("Initial state");

        // First call - should populate cache
        long start1 = System.currentTimeMillis();
        Song song1 = songService.getSongById(101);
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("First call (cache miss): " + time1 + "ms");

        // Check cache after first call
        inspectCacheViaReflection("After getting song 101");

        // Second call - should hit cache
        long start2 = System.currentTimeMillis();
        Song song2 = songService.getSongById(101);
        long time2 = System.currentTimeMillis() - start2;
        System.out.println("Second call (cache hit): " + time2 + "ms");

        // Verify they're the same instance (from cache)
        System.out.println("Same instance? " + (song1 == song2));

        // Get another song
        songService.getSongById(102);
        inspectCacheViaReflection("After getting two songs");

        // Test getAllSongs
        System.out.println("\nTesting getAllSongs()");
        long start3 = System.currentTimeMillis();
        songService.getAllSongs();
        long time3 = System.currentTimeMillis() - start3;
        System.out.println("First getAllSongs(): " + time3 + "ms");

        // Check cache after bulk load
        inspectCacheViaReflection("After getAllSongs()");

        long start4 = System.currentTimeMillis();
        songService.getAllSongs();
        long time4 = System.currentTimeMillis() - start4;
        System.out.println("Second getAllSongs(): " + time4 + "ms");
    }

    // Move inspectCacheViaReflection inside the class
    private static void inspectCacheViaReflection(String label) {
        System.out.println("\n--- Cache State: " + label + " ---");

        try {
            SongService songService = SongService.getInstance();

            // Get the repository field from service
            Field repoField = SongService.class.getDeclaredField("songRepository");
            repoField.setAccessible(true);
            SongRepositoryInterface repo = (SongRepositoryInterface) repoField.get(songService);

            // Navigate to the cache
            if (repo instanceof CachedSongRepositoryWrapper) {
                Field cachedRepoField = CachedSongRepositoryWrapper.class.getDeclaredField("cachedRepo");
                cachedRepoField.setAccessible(true);
                CachedRepository<Song> cachedRepo = (CachedRepository<Song>) cachedRepoField.get(repo);

                Field cacheField = CachedRepository.class.getDeclaredField("cache");
                cacheField.setAccessible(true);
                CachingStrategy<Song> cache = (CachingStrategy<Song>) cacheField.get(cachedRepo);

                if (cache instanceof InMemoryCacheStrategy) {
                    InMemoryCacheStrategy<Song> memCache = (InMemoryCacheStrategy<Song>) cache;
                    Field mapField = InMemoryCacheStrategy.class.getDeclaredField("cache");
                    mapField.setAccessible(true);
                    Map<String, Song> cacheMap = (Map<String, Song>) mapField.get(memCache);

                    System.out.println("Cache size: " + cacheMap.size());
                    System.out.println("Cache keys: " + cacheMap.keySet());

                    System.out.println("Cache contents:");
                    cacheMap.forEach((key, song) -> {
                        System.out.println("  " + key + " -> " + song.getTitle() + " (ID: " + song.getSongId() + ")");
                    });
                }
            } else {
                System.out.println("Repository is not a CachedSongRepositoryWrapper");
            }
        } catch (Exception e) {
            System.out.println("Error inspecting cache: " + e.getMessage());
            e.printStackTrace();
        }
    }
}