package persistence;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import persistence.interfaces.PlaylistRepositoryInterface;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * A repository for persisting and retrieving Playlist objects.
 * Strictly follows CRUD operations for data access.
 */
public class PlaylistRepository extends JsonRepository<Playlist> implements PlaylistRepositoryInterface {

    /**
     * Singleton instance of PlaylistRepository.
     */
    private static PlaylistRepository instance;

    /**
     * Constructor that initializes the Playlist repository.
     */
    private PlaylistRepository() {
        super(Playlist.class, "playlists.json", Playlist::getOwnerID);
    }

    /**
     * Gets the singleton instance of PlaylistRepository.
     */
    public static synchronized PlaylistRepository getInstance() {
        if (instance == null) {
            instance = new PlaylistRepository();
        }
        return instance;
    }

    /**
     * Finds a playlist by its name and owner ID.
     */
    @Override
    public Optional<Playlist> findByNameAndOwnerID(String name, int ownerID) {
        return findAll().stream()
                .filter(playlist -> playlist.getName().equals(name) && playlist.getOwnerID() == ownerID)
                .findFirst();
    }

    /**
     * Finds playlists by owner ID.
     */
    @Override
    public List<Playlist> findByOwnerID(int ownerID) {
        return findAll().stream()
                .filter(playlist -> playlist.getOwnerID() == ownerID)
                .collect(Collectors.toList());
    }

    /**
     * Finds playlists shared with a specific user.
     */
    @Override
    public List<Playlist> findSharedWithUserByID(int userID) {
        return findAll().stream()
                .filter(playlist -> playlist.getSharedWith().contains(userID))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a playlist by its name and owner.
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
     * Since addSong was removed from Playlist class, this method directly updates the songs collection.
     */
    @Override
    public boolean addSongToPlaylist(String name, int ownerID, Song song) {
        Optional<Playlist> playlistOpt = findByNameAndOwnerID(name, ownerID);

        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        LinkedList<Song> songs = new LinkedList<>(playlist.getSongs());
        songs.add(song);

        playlist.setSongs(songs);

        return update(playlist).isPresent();
    }

    /// --------SOCIAL FUNCTIONS-------- ///

    /**
     * Checks if a playlist is shared with a specific user.
     *
     * @param playlistId The ID of the playlist to check
     * @param userId The ID of the user to check
     * @return true if the playlist is shared with the user, false otherwise or if playlist not found
     */
    public boolean isPlaylistSharedWithUser(int playlistId, int userId) {
        Optional<Playlist> playlistOpt = findById(playlistId);

        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        return playlist.getSharedWith().contains(userId);
    }

    /**
     * Shares a playlist with a user.
     * Updated to work with strict CRUD operations.
     */
    @Override
    public boolean sharePlaylistWithUser(String name, int ownerID, int userID) {
        Optional<Playlist> playlistOpt = findByNameAndOwnerID(name, ownerID);

        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        List<Integer> sharedWith = new ArrayList<>(playlist.getSharedWith());

        if (!sharedWith.contains(userID)) {
            sharedWith.add(userID);
            playlist.setSharedWith(sharedWith);
            return update(playlist).isPresent();
        }

        return true; // Already shared
    }

    /**
     * Unshares a playlist with a user.
     *
     */
    @Override
    public boolean unsharePlaylistWithUser(String name, int ownerID, int userID) {
        Optional<Playlist> playlistOpt = findByNameAndOwnerID(name, ownerID);

        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        List<Integer> sharedWith = new ArrayList<>(playlist.getSharedWith());

        if (sharedWith.contains(userID)) {
            sharedWith.remove(Integer.valueOf(userID));
            playlist.setSharedWith(sharedWith);
            return update(playlist).isPresent();
        }

        return false; // Not shared with this user
    }

    /**
     * Finds a playlist by its ID.
     */
    @Override
    public Optional<Playlist> findById(int id) {
        return findAll().stream()
                .filter(playlist -> playlist.getPlaylistID() == id)
                .findFirst();
    }

    /**
     * Deletes a playlist by its ID.
     *
     * @param id The ID of the playlist to delete
     * @return true if successful, false if not found
     */
    @Override
    public boolean deleteById(int id) {
        List<Playlist> playlists = findAll();
        int initialSize = playlists.size();

        // Filter to remove the playlist with the specified ID
        playlists.removeIf(playlist -> playlist.getPlaylistID() == id);

        // If size changed, something was removed
        if (playlists.size() < initialSize) {
            saveAll(playlists);
            return true;
        }

        return false;
    }
}