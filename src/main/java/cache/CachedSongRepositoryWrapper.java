package cache;

import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Song;
import songsAndArtists.Genre;
import java.util.List;
import java.util.Optional;

/**
 * A wrapper for the Song repository that leverages caching capabilities.
 * This class implements the SongRepositoryInterface and delegates basic CRUD operations
 * to a CachedRepository instance while handling song-specific query methods by filtering
 * cached data.
 *
 * The class follows the Adapter pattern, adapting the generic CachedRepository
 * to the SongRepositoryInterface contract.
 *
 */
public class CachedSongRepositoryWrapper implements SongRepositoryInterface {
    private final CachedRepository<Song> cachedRepo;

    /**
     * Constructs a new CachedSongRepositoryWrapper with the specified cached repository.
     *
     * @param cachedRepo The cached repository to delegate operations to
     */
    public CachedSongRepositoryWrapper(CachedRepository<Song> cachedRepo) {
        this.cachedRepo = cachedRepo;
    }

    /**
     * Retrieves all songs from the repository.
     *
     * @return A list of all songs
     */
    // Basic CRUD operations delegate to CachedRepository
    @Override
    public List<Song> findAll() {
        return cachedRepo.findAll();
    }

    /**
     * Retrieves a song by its ID.
     *
     * @param id The unique identifier of the song
     * @return An Optional containing the song if found, or empty if not found
     */
    @Override
    public Optional<Song> findById(int id) {
        return cachedRepo.findById(id);
    }

    /**
     * Saves a new song to the repository.
     *
     * @param song The song to save
     * @return The saved song (may contain generated values)
     */
    @Override
    public Song save(Song song) {
        return cachedRepo.save(song);
    }

    /**
     * Saves multiple songs to the repository.
     *
     * @param songs The list of songs to save
     */
    @Override
    public void saveAll(List<Song> songs) {
        cachedRepo.saveAll(songs);
    }

    /**
     * Updates an existing song in the repository.
     *
     * @param song The song with updated values
     * @return An Optional containing the updated song if successful, or empty if not found
     */
    @Override
    public Optional<Song> update(Song song) {
        return cachedRepo.update(song);
    }

    /**
     * Deletes a song by its ID.
     *
     * @param id The unique identifier of the song to delete
     * @return true if the song was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    /**
     * Finds songs by artist ID by filtering the cached data.
     * This implementation retrieves all songs from the cache and filters them in memory.
     * Future optimizations might use more efficient strategies for frequently used queries.
     *
     * @param artistId The ID of the artist whose songs to find
     * @return A list of songs by the specified artist
     */
    @Override
    public List<Song> findByArtistId(int artistId) {
        return findAll().stream()
                .filter(song -> song.getArtistId() == artistId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Finds songs by genre by filtering the cached data.
     * This implementation retrieves all songs from the cache and filters them in memory.
     *
     * @param genre The genre of songs to find
     * @return A list of songs with the specified genre
     */
    @Override
    public List<Song> findByGenre(Genre genre) {
        return findAll().stream()
                .filter(song -> song.getGenre() == genre)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Finds songs by title by filtering the cached data.
     * This implementation retrieves all songs from the cache and filters them in memory,
     * performing a case-insensitive comparison.
     *
     * @param title The title of songs to find
     * @return A list of songs with the specified title
     */
    @Override
    public List<Song> findByTitle(String title) {
        return findAll().stream()
                .filter(song -> song.getTitle().equalsIgnoreCase(title))
                .collect(java.util.stream.Collectors.toList());
    }
}