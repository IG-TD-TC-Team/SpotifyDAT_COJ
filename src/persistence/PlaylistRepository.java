package persistence;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import user.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import persistence.interfaces.PlaylistRepositoryInterface;

/**
 * A repository for persisting and retrieving Playlist objects.
 * Extends the generic JsonRepository to provide Playlist-specific functionalities.
 */
public class PlaylistRepository extends JsonRepository<Playlist> implements PlaylistRepositoryInterface{

    /**
     * Singleton instance of PlaylistRepository.
     */
    private static PlaylistRepository instance;

    /**
     * Constructor that initializes the Playlist repository.
     * This constructor calls the superclass constructor with the Playlist class type and the JSON file name.
     */
    private PlaylistRepository() {

        super(Playlist.class, "playlists.json", Playlist::getOwnerID);

    }
    /**
     * Gets the singleton instance of PlaylistRepository.
     *
     * @return The singleton instance
     */
    public static synchronized PlaylistRepository getInstance() {
        if (instance == null) {
            instance = new PlaylistRepository();
        }
        return instance;
    }


    /**
     * Finds a playlist by its name and owner ID.
     *
     */
    @Override
    public Optional<Playlist> findByNameAndOwnerID(String name, int ownerID) {
        return findAll().stream()
                .filter(playlist -> playlist.getName().equals(name) && playlist.getOwnerID() == ownerID)
                .findFirst();
    }

    /**
     * Finds playlists by owner ID.
     *
     */
    @Override
    public List<Playlist> findByOwnerID(int ownerID) {
        return findAll().stream()
                .filter(playlist -> playlist.getOwnerID() == ownerID)
                .collect(Collectors.toList());
    }

    /**
     * Finds playlists shared with a specific user.
     *
     */
    @Override
    public List<Playlist> findSharedWithUserByID(int userID) {
        return findAll().stream()
                .filter(playlist -> playlist.getSharedWith().contains(userID))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a playlist by its name and owner.
     *
     */
    @Override
    public boolean deleteByNameAndOwner(String name, int ownerID) {
        List<Playlist> playlists = findAll();
        boolean removed = playlists.removeIf(playlist ->
                playlist.getName().equals(name) && playlist.getOwnerID() == ownerID);

        if (removed) {
            saveAll(playlists);
        }

        return removed;
    }

    /**
     * Adds a song to a playlist.
     *
     * @param name
     * @param ownerID
     * @param song
     */
    @Override
    public boolean addSongToPlaylist(String name, int ownerID, Song song) {
        Optional<Playlist> playlistOpt = findByNameAndOwnerID(name, ownerID);

        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            playlist.addSong(song);
            update(playlist);
            return true;
        }

        return false; // Playlist not found
    }

    /**
     * Shares a playlist with a user.
     *
     * @param name
     * @param ownerID
     * @param userID
     */
    @Override
    public boolean sharePlaylistWithUser(String name, int ownerID, int userID) {
        Optional<Playlist> playlistOpt = findByNameAndOwnerID(name, ownerID);

        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();

            if (!playlist.getSharedWith().contains(userID)) {
                playlist.addUserToShareWith(userID);
                update(playlist);
                return true;
            }

            return true; // Already shared with the user
        }

        return false; // Playlist not found
    }

    /**
     * Unshares a playlist with a user.
     *
     * @param name
     * @param ownerID
     * @param userID
     */
    @Override
    public boolean unsharePlaylistWithUser(String name, int ownerID, int userID) {
        Optional<Playlist> playlistOpt = findByNameAndOwnerID(name, ownerID);

        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();

            if (playlist.getSharedWith().contains(userID)) {
                playlist.removeSharing(userID);
                update(playlist);
                return true;
            }

            return false; // Not shared with the user
        }

        return false; // Playlist not found
    }

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id The unique identifier of the entity
     * @return An Optional containing the entity if found, or empty if not found
     */
    @Override
    public Optional<Playlist> findById(int id) {
        return findAll().stream()
                .filter(playlist -> playlist.getPlaylistID() == id)
                .findFirst();
    }


}