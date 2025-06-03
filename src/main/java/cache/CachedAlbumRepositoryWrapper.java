package cache;

import persistence.interfaces.AlbumRepositoryInterface;
import songsAndArtists.Album;

import java.util.List;
import java.util.Optional;

/**
 * A wrapper for the Album repository that selectively applies caching to basic CRUD operations.
 * This class implements the AlbumRepositoryInterface and uses a hybrid approach:
 * - Basic CRUD operations are delegated to a CachedRepository for performance
 * - Specialized query and social operations are delegated to the original repository implementation
 */
public class CachedAlbumRepositoryWrapper implements AlbumRepositoryInterface {
    private final CachedRepository<Album> cachedRepo;
    private final AlbumRepositoryInterface original;

    /**
     * Constructs a new CachedAlbumRepositoryWrapper with the specified cached repository
     * and original album repository.
     *
     * @param cachedRepo The cached repository for handling basic CRUD operations
     * @param original The original repository for handling specialized queries
     */
    public CachedAlbumRepositoryWrapper(CachedRepository<Album> cachedRepo, AlbumRepositoryInterface original) {
        this.cachedRepo = cachedRepo;
        this.original = original;
    }

    /**
     * Retrieves all albums from the cached repository.
     *
     * @return A list of all albums
     */
    @Override
    public List<Album> findAll() {
        return cachedRepo.findAll();
    }

    /**
     * Retrieves an album by its ID from the cached repository.
     *
     * @param id The unique identifier of the album
     * @return An Optional containing the album if found, or empty if not found
     */
    @Override
    public Optional<Album> findById(int id) {
        return cachedRepo.findById(id);
    }

    /**
     * Saves a new album to both the cached repository and the original repository.
     *
     * @param album The album to save
     * @return The saved album (may contain generated values)
     */
    @Override
    public Album save(Album album) {
        return cachedRepo.save(album);
    }

    /**
     * Saves multiple albums to both the cached repository and the original repository.
     *
     * @param albums The list of albums to save
     */
    @Override
    public void saveAll(List<Album> albums) {
        cachedRepo.saveAll(albums);
    }

    /**
     * Updates an existing album in both the cached repository and the original repository.
     *
     * @param album The album with updated values
     * @return An Optional containing the updated album if successful, or empty if not found
     */
    @Override
    public Optional<Album> update(Album album) {
        return cachedRepo.update(album);
    }

    /**
     * Deletes an album by its ID from both the cached repository and the original repository.
     *
     * @param id The unique identifier of the album to delete
     * @return true if the album was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    /**
     * Finds albums by artist ID.
     *
     * @param artistId The ID of the artist
     * @return A list of albums created by the specified artist
     */
    @Override
    public List<Album> findByArtistId(int artistId) {
        return original.findByArtistId(artistId);
    }

    /**
     * Finds albums containing a specific song.
     *
     * @param songId The ID of the song
     * @return A list of albums containing the specified song
     */
    @Override
    public List<Album> findBySongId(int songId) {
        return original.findBySongId(songId);
    }

    /**
     * Adds a song to an album.
     *
     * @param albumId The ID of the album
     * @param songId The ID of the song to add
     * @return true if successful, false otherwise
     */
    @Override
    public boolean addSongToAlbum(int albumId, int songId) {
        boolean result = original.addSongToAlbum(albumId, songId);
        if (result) {
            // Refresh the album in the cache if the operation was successful
            original.findById(albumId).ifPresent(cachedRepo::update);
        }
        return result;
    }

    /**
     * Removes a song from an album.
     *
     * @param albumId The ID of the album
     * @param songId The ID of the song to remove
     * @return true if successful, false otherwise
     */
    @Override
    public boolean removeSongFromAlbum(int albumId, int songId) {
        boolean result = original.removeSongFromAlbum(albumId, songId);
        if (result) {
            // Refresh the album in the cache if the operation was successful
            original.findById(albumId).ifPresent(cachedRepo::update);
        }
        return result;
    }
}