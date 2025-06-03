package cache;

import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.PlaylistRepository;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.List;
import java.util.Optional;

/**
 * A wrapper for the Playlist repository that selectively applies caching to basic CRUD operations.
 * This class implements the PlaylistRepositoryInterface and uses a hybrid approach:
 * - Basic CRUD operations are delegated to a CachedRepository for performance
 * - Specialized query and social operations are delegated to the original repository implementation
 *
 * This wrapper follows both the Decorator and Adapter patterns:
 * - Decorator: It adds caching behavior to certain operations
 * - Adapter: It adapts the CachedRepository to the full PlaylistRepositoryInterface contract
 */
public class CachedPlaylistRepositoryWrapper implements PlaylistRepositoryInterface {
    private final CachedRepository<Playlist> cachedRepo;
    private final PlaylistRepositoryInterface original;

    /**
     * Constructs a new CachedPlaylistRepositoryWrapper with the specified cached repository
     * and original playlist repository.
     *
     * @param cachedRepo The cached repository for handling basic CRUD operations
     * @param original The original repository for handling specialized queries
     */
    public CachedPlaylistRepositoryWrapper(CachedRepository<Playlist> cachedRepo, PlaylistRepositoryInterface original) {
        this.cachedRepo = cachedRepo;
        this.original = original;
    }

    /**
     * Retrieves all playlists from the cached repository.
     *
     * @return A list of all playlists
     */
    @Override
    public List<Playlist> findAll() {
        return cachedRepo.findAll();
    }

    /**
     * Retrieves a playlist by its ID from the cached repository.
     *
     * @param id The unique identifier of the playlist
     * @return An Optional containing the playlist if found, or empty if not found
     */
    @Override
    public Optional<Playlist> findById(int id) {
        return cachedRepo.findById(id);
    }

    /**
     * Saves a new playlist to both the cached repository and the original repository.
     *
     * @param playlist The playlist to save
     * @return The saved playlist (may contain generated values)
     */
    @Override
    public Playlist save(Playlist playlist) {
        return cachedRepo.save(playlist);
    }

    /**
     * Saves multiple playlists to both the cached repository and the original repository.
     *
     * @param playlists The list of playlists to save
     */
    @Override
    public void saveAll(List<Playlist> playlists) {
        cachedRepo.saveAll(playlists);
    }

    /**
     * Updates an existing playlist in both the cached repository and the original repository.
     *
     * @param playlist The playlist with updated values
     * @return An Optional containing the updated playlist if successful, or empty if not found
     */
    @Override
    public Optional<Playlist> update(Playlist playlist) {
        return cachedRepo.update(playlist);
    }

    /**
     * Deletes a playlist by its ID from both the cached repository and the original repository.
     *
     * @param id The unique identifier of the playlist to delete
     * @return true if the playlist was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    /**
     * Finds a playlist by its name and owner ID directly from the original repository.
     *
     * @param name The name of the playlist
     * @param ownerID The ID of the owner
     * @return An Optional containing the playlist if found, or empty if not found
     */
    @Override
    public Optional<Playlist> findByNameAndOwnerID(String name, int ownerID) {
        return original.findByNameAndOwnerID(name, ownerID);
    }

    /**
     * Finds playlists by owner ID directly from the original repository.
     *
     * @param ownerID The ID of the owner
     * @return A list of playlists owned by the specified user
     */
    @Override
    public List<Playlist> findByOwnerID(int ownerID) {
        return original.findByOwnerID(ownerID);
    }

    /**
     * Finds playlists shared with a specific user directly from the original repository.
     *
     * @param userID The ID of the user
     * @return A list of playlists shared with the specified user
     */
    @Override
    public List<Playlist> findSharedWithUserByID(int userID) {
        return original.findSharedWithUserByID(userID);
    }

    /**
     * Checks if a playlist is shared with a specific user directly from the original repository.
     *
     * @param playlistId The ID of the playlist
     * @param userId The ID of the user
     * @return true if the playlist is shared with the user, false otherwise
     */
    @Override
    public boolean isPlaylistSharedWithUser(int playlistId, int userId) {
        return original.isPlaylistSharedWithUser(playlistId, userId);
    }

    /**
     * Deletes a playlist by its name and owner directly from the original repository.
     *
     * @param name The name of the playlist
     * @param ownerID The ID of the owner
     * @return true if the playlist was found and deleted, false otherwise
     */
    @Override
    public boolean deleteByNameAndOwner(String name, int ownerID) {
        boolean result = original.deleteByNameAndOwner(name, ownerID);
        if (result) {
            // If deletion was successful, invalidate cache
            cachedRepo.findAll(); // This will refresh the cache
        }
        return result;
    }

    /**
     * Adds a song to a playlist directly through the original repository.
     *
     * @param name The name of the playlist
     * @param ownerID The ID of the owner
     * @param song The song to add
     * @return true if the song was successfully added, false otherwise
     */
    @Override
    public boolean addSongToPlaylist(String name, int ownerID, Song song) {
        boolean result = original.addSongToPlaylist(name, ownerID, song);
        if (result) {
            // If addition was successful, invalidate cache for the specific playlist
            Optional<Playlist> playlist = original.findByNameAndOwnerID(name, ownerID);
            playlist.ifPresent(p -> cachedRepo.update(p));
        }
        return result;
    }

    /**
     * Shares a playlist with a user directly through the original repository.
     *
     * @param name The name of the playlist
     * @param ownerID The ID of the owner
     * @param userID The ID of the user to share with
     * @return true if the playlist was successfully shared, false otherwise
     */
    @Override
    public boolean sharePlaylistWithUser(String name, int ownerID, int userID) {
        boolean result = original.sharePlaylistWithUser(name, ownerID, userID);
        if (result) {
            // If sharing was successful, invalidate cache for the specific playlist
            Optional<Playlist> playlist = original.findByNameAndOwnerID(name, ownerID);
            playlist.ifPresent(p -> cachedRepo.update(p));
        }
        return result;
    }

    /**
     * Unshares a playlist with a user directly through the original repository.
     *
     * @param name The name of the playlist
     * @param ownerID The ID of the owner
     * @param userID The ID of the user to unshare with
     * @return true if the playlist was successfully unshared, false otherwise
     */
    @Override
    public boolean unsharePlaylistWithUser(String name, int ownerID, int userID) {
        boolean result = original.unsharePlaylistWithUser(name, ownerID, userID);
        if (result) {
            // If unsharing was successful, invalidate cache for the specific playlist
            Optional<Playlist> playlist = original.findByNameAndOwnerID(name, ownerID);
            playlist.ifPresent(p -> cachedRepo.update(p));
        }
        return result;
    }
}