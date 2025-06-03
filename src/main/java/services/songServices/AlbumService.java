package services.songServices;

import factory.RepositoryFactory;
import persistence.interfaces.AlbumRepositoryInterface;
import songsAndArtists.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing albums.
 * Follows the repository-service pattern by using the AlbumRepository
 * to perform CRUD operations on albums.
 * Implements the Singleton pattern to ensure only one instance exists.
 */

public class AlbumService {

    /**
     * Singleton instance of AlbumService.
     */
    private static AlbumService instance;

    /**
     * Repository for accessing album data.
     */
    private final AlbumRepositoryInterface albumRepository;

    /**
     * List of albums.
     */
    private List<Album> albumsCache;

    /**
     * Flag to track if the cache is initialized
     */
    private boolean cacheInitialized = false;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the album repository instance using the RepositoryFactory.
     */
    private AlbumService() {
        albumRepository = RepositoryFactory.getInstance().getAlbumRepository();
        initializeCache();
    }

    /**
     * Initializes the cache by loading all albums from the repository.
     * This is called once during service initialization.
     */
    private void initializeCache() {
        this.albumsCache = albumRepository.findAll();
        this.cacheInitialized = true;
        System.out.println("AlbumService initialized with " + albumsCache.size() + " albums");
    }

    /**
     * Ensures the cache is loaded before accessing it.
     * This method is called by methods that access the cache to ensure
     * it is initialized and up-to-date.
     */
    private void ensureCacheIsLoaded() {
        if (!cacheInitialized) {
            initializeCache();
        }
    }

    /**
     * Returns the singleton instance of AlbumService.
     * @return The singleton instance of AlbumService.
     */
    public static synchronized AlbumService getInstance() {
        if (instance == null) {
            instance = new AlbumService();
        }
        return instance;
    }

    /// --------------------- ALBUM RETRIEVAL ----------------- ///
    /**
     * Retrieves an album by its ID.
     * @param albumId The ID of the album to retrieve.
     * @return The album with the specified ID, or null if not found.
     */
    public Album getAlbumById(int albumId) {
        // Use the repository directly to leverage its caching
        return albumRepository.findById(albumId).orElse(null);
    }

    /**
     * Retrieves albums by title (case-insensitive).
     * @param title The title to search for.
     * @return A list of albums matching the title.
     */
    public List<Album> getAlbumsByTitle(String title) {
        ensureCacheIsLoaded();
        List<Album> result = new ArrayList<>();
        String searchTerm = title.toLowerCase();

        for (Album album : albumsCache) {
            if (album.getTitle() != null && album.getTitle().toLowerCase().contains(searchTerm)) {
                result.add(album);
            }
        }

        return result;
    }

    /**
     * Retrieves albums by artist ID.
     * @param artistId The ID of the artist.
     * @return A list of albums by the specified artist.
     */
    public List<Album> getAlbumsByArtist(int artistId) {
        // Use the repository for specialized queries
        return albumRepository.findByArtistId(artistId);
    }

    /**
     * Retrieves all albums.
     * @return A list of all albums.
     */
    public List<Album> getAllAlbums() {
        ensureCacheIsLoaded();
        return new ArrayList<>(albumsCache); // Return a copy to prevent external modification
    }

    /**
     * Searches for albums by title using flexible matching strategies.
     * This method employs multiple strategies to find matching albums:
     * 1. Exact match (case-insensitive)
     * 2. Contains match (case-insensitive)
     * 3. Fuzzy match (normalized strings)
     *
     * @param title The title to search for
     * @return A list of matching albums ordered by relevance
     */
    public List<Album> searchAlbumsByTitle(String title) {
        ensureCacheIsLoaded();

        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Album> exactMatches = new ArrayList<>();
        List<Album> containsMatches = new ArrayList<>();
        List<Album> fuzzyMatches = new ArrayList<>();

        String normalizedQuery = normalizeString(title);

        for (Album album : albumsCache) {
            if (album.getTitle() == null) continue;

            // Exact match (case-insensitive)
            if (album.getTitle().equalsIgnoreCase(title)) {
                exactMatches.add(album);
                continue;
            }

            // Contains match (case-insensitive)
            if (album.getTitle().toLowerCase().contains(title.toLowerCase())) {
                containsMatches.add(album);
                continue;
            }

            // Fuzzy match
            String normalizedTitle = normalizeString(album.getTitle());
            if (normalizedTitle.contains(normalizedQuery)) {
                fuzzyMatches.add(album);
            }
        }

        // Combine results, prioritizing exact matches, then contains matches, then fuzzy matches
        List<Album> results = new ArrayList<>(exactMatches);
        results.addAll(containsMatches);
        results.addAll(fuzzyMatches);

        return results;
    }

    /**
     * Helper method to normalize a string for fuzzy matching.
     */
    private String normalizeString(String input) {
        if (input == null) return "";

        // Convert to lowercase
        String result = input.toLowerCase();

        // Remove special characters
        result = result.replaceAll("[^a-z0-9\\s]", "");

        // Replace multiple spaces with a single space
        result = result.replaceAll("\\s+", " ");

        // Remove accents (simplified approach)
        result = result.replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");

        return result.trim();
    }

    /// ---------------------- ALBUM UPDATE ----------------- ///
    /**
     * Adds a song to an album.
     *
     * @param songId The ID of the song
     * @param albumId The ID of the album
     * @return true if successful, false otherwise
     */
    public boolean addSongToAlbum(int songId, int albumId) {
        boolean result = albumRepository.addSongToAlbum(albumId, songId);
        if (result) {
            refreshCache(); // Refresh the cache after successful update
        }
        return result;
    }

    /**
     * Removes a song from an album.
     *
     * @param songId The ID of the song
     * @param albumId The ID of the album
     * @return true if successful, false otherwise
     */
    public boolean removeSongFromAlbum(int songId, int albumId) {
        boolean result = albumRepository.removeSongFromAlbum(albumId, songId);
        if (result) {
            refreshCache(); // Refresh the cache after successful update
        }
        return result;
    }

    /// ----------------------CACHE REFRESH----------------- ///
    /**
     * Refreshes the in-memory cache with the latest data from repositories.
     * Call this after entities are created, updated, or deleted.
     */
    public void refreshCache() {
        albumsCache = albumRepository.findAll();
        System.out.println("AlbumService cache refreshed with " + albumsCache.size() + " albums");
    }
}