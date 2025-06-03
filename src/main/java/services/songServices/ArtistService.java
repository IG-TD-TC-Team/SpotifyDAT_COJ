package services.songServices;

import factory.RepositoryFactory;
import persistence.interfaces.ArtistRepositoryInterface;
import songsAndArtists.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistService {

    /**
     * Singleton instance of ArtistService.
     */
    private static ArtistService instance;

    /**
     * Repository for accessing artist data.
     */
    private final ArtistRepositoryInterface artistRepository;

    /**
     * List of artists.
     */
    private List<Artist> artistsCache;

    /**
     * Flag to track if the cache is initialized
     */
    private boolean cacheInitialized = false;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the artist repository instance using the RepositoryFactory.
     */
    private ArtistService() {
        artistRepository = RepositoryFactory.getInstance().getArtistRepository();
        initializeCache();
    }

    /**
     * Initializes the cache by loading all artists from the repository.
     * This is called once during service initialization.
     */
    private void initializeCache() {
        this.artistsCache = artistRepository.findAll();
        this.cacheInitialized = true;
        System.out.println("ArtistService initialized with " + artistsCache.size() + " artists");
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
     * Returns the singleton instance of ArtistService.
     * @return The singleton instance of ArtistService.
     */
    public static synchronized ArtistService getInstance() {
        if (instance == null) {
            instance = new ArtistService();
        }
        return instance;
    }

    /// ------------------------- ARTIST RETRIEVAL ----------------- ///
    /**
     * Retrieves an artist by their ID.
     * @param artistId The ID of the artist to retrieve.
     * @return The artist with the specified ID, or null if not found.
     */
    public Artist getArtistById(int artistId) {
        // Use the repository directly to leverage its caching
        return artistRepository.findById(artistId).orElse(null);
    }

    /**
     * Retrieves all artists.
     * @return A list of all artists.
     */
    public List<Artist> getAllArtists() {
        ensureCacheIsLoaded();
        return new ArrayList<>(artistsCache); // Return a copy to prevent external modification
    }

    /**
     * Refreshes the repository data with the latest changes.
     * Used after operations that modify user data in other services.
     */
    public void refreshCache() {
        artistsCache = artistRepository.findAll();
        System.out.println("ArtistService cache refreshed with " + artistsCache.size() + " artists");
    }

    /**
     * Adds a song to an artist's song list.
     *
     * @param artistId The ID of the artist
     * @param songId The ID of the song to add
     * @return true if successful, false otherwise
     */
    public boolean addSongToArtist(int artistId, int songId) {
        boolean result = artistRepository.addSongToArtist(artistId, songId);
        if (result) {
            refreshCache(); // Refresh the cache after successful update
        }
        return result;
    }

    /**
     * Removes a song from an artist's song list.
     *
     * @param artistId The ID of the artist
     * @param songId The ID of the song to remove
     * @return true if successful, false otherwise
     */
    public boolean removeSongFromArtist(int artistId, int songId) {
        boolean result = artistRepository.removeSongFromArtist(artistId, songId);
        if (result) {
            refreshCache(); // Refresh the cache after successful update
        }
        return result;
    }

    /**
     * Finds artists by name.
     *
     * @param name The name to search for
     * @return A list of artists with matching names
     */
    public List<Artist> findArtistsByName(String name) {
        return artistRepository.findByName(name);
    }

    /**
     * Finds artists by country.
     *
     * @param country The country to search for
     * @return A list of artists from the specified country
     */
    public List<Artist> findArtistsByCountry(String country) {
        return artistRepository.findByCountry(country);
    }
}