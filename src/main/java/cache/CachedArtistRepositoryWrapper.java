package cache;

import persistence.interfaces.ArtistRepositoryInterface;
import songsAndArtists.Artist;

import java.util.List;
import java.util.Optional;

/**
 * A wrapper for the Artist repository that selectively applies caching to basic CRUD operations.
 * This class implements the ArtistRepositoryInterface and uses a hybrid approach:
 * - Basic CRUD operations are delegated to a CachedRepository for performance
 * - Specialized query and social operations are delegated to the original repository implementation
 */
public class CachedArtistRepositoryWrapper implements ArtistRepositoryInterface {
    private final CachedRepository<Artist> cachedRepo;
    private final ArtistRepositoryInterface original;

    /**
     * Constructs a new CachedArtistRepositoryWrapper with the specified cached repository
     * and original artist repository.
     *
     * @param cachedRepo The cached repository for handling basic CRUD operations
     * @param original The original repository for handling specialized queries
     */
    public CachedArtistRepositoryWrapper(CachedRepository<Artist> cachedRepo, ArtistRepositoryInterface original) {
        this.cachedRepo = cachedRepo;
        this.original = original;
    }

    /**
     * Retrieves all artists from the cached repository.
     *
     * @return A list of all artists
     */
    @Override
    public List<Artist> findAll() {
        return cachedRepo.findAll();
    }

    /**
     * Retrieves an artist by their ID from the cached repository.
     *
     * @param id The unique identifier of the artist
     * @return An Optional containing the artist if found, or empty if not found
     */
    @Override
    public Optional<Artist> findById(int id) {
        return cachedRepo.findById(id);
    }

    /**
     * Saves a new artist to both the cached repository and the original repository.
     *
     * @param artist The artist to save
     * @return The saved artist (may contain generated values)
     */
    @Override
    public Artist save(Artist artist) {
        return cachedRepo.save(artist);
    }

    /**
     * Saves multiple artists to both the cached repository and the original repository.
     *
     * @param artists The list of artists to save
     */
    @Override
    public void saveAll(List<Artist> artists) {
        cachedRepo.saveAll(artists);
    }

    /**
     * Updates an existing artist in both the cached repository and the original repository.
     *
     * @param artist The artist with updated values
     * @return An Optional containing the updated artist if successful, or empty if not found
     */
    @Override
    public Optional<Artist> update(Artist artist) {
        return cachedRepo.update(artist);
    }

    /**
     * Deletes an artist by their ID from both the cached repository and the original repository.
     *
     * @param id The unique identifier of the artist to delete
     * @return true if the artist was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    /**
     * Finds artists by name.
     *
     * @param name The name to search for
     * @return A list of artists with matching names
     */
    @Override
    public List<Artist> findByName(String name) {
        return original.findByName(name);
    }

    /**
     * Finds artists by country.
     *
     * @param country The country to search for
     * @return A list of artists from the specified country
     */
    @Override
    public List<Artist> findByCountry(String country) {
        return original.findByCountry(country);
    }

    /**
     * Adds a song to an artist's song list.
     *
     * @param artistId The ID of the artist
     * @param songId The ID of the song to add
     * @return true if successful, false otherwise
     */
    @Override
    public boolean addSongToArtist(int artistId, int songId) {
        boolean result = original.addSongToArtist(artistId, songId);
        if (result) {
            // Refresh the artist in the cache if the operation was successful
            original.findById(artistId).ifPresent(cachedRepo::update);
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
    @Override
    public boolean removeSongFromArtist(int artistId, int songId) {
        boolean result = original.removeSongFromArtist(artistId, songId);
        if (result) {
            // Refresh the artist in the cache if the operation was successful
            original.findById(artistId).ifPresent(cachedRepo::update);
        }
        return result;
    }
}