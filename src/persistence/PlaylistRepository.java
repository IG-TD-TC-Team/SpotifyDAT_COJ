package persistence;

import songsOrganisation.Playlist;
import user.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A repository for persisting and retrieving Playlist objects.
 * Extends the generic JsonRepository to provide Playlist-specific functionalities.
 */
public class PlaylistRepository extends JsonRepository<Playlist> {

    /**
     * Constructor that initializes the Playlist repository.
     * This constructor calls the superclass constructor with the Playlist class type and the JSON file name.
     */
    public PlaylistRepository() {

        super(Playlist.class, "playlists.json");

    }

    /**
     * Finds a playlist by its name and owner.
     *
     * @param name  The name of the playlist.
     * @param ownerID The owner of the playlist.
     * @return An Optional containing the playlist if found, or empty if not found.
     */
    public Optional<Playlist> findByNameAndOwnerID(String name, int ownerID) {
        return findAll().stream()
                .filter(playlist -> playlist.getName().equals(name) && playlist.getOwnerID() == ownerID)
                .findFirst();
    }

    /**
     * Finds playlists by owner.
     *
     * @param ownerID The owner whose playlists to find.
     * @return A List of playlists owned by the specified user.
     */
    public List<Playlist> findByOwnerID(int ownerID) {
        return findAll().stream()
                .filter(playlist -> playlist.getOwnerID() == ownerID)
                .collect(Collectors.toList());
    }

    /**
     * Finds playlists shared with a specific user.
     *
     * @param userID The user to check shared playlists for.
     * @return A List of playlists shared with the specified user.
     */
    public List<Playlist> findSharedWithUserByID(int userID) {
        return findAll().stream()
                .filter(playlist -> playlist.getSharedWith().contains(userID))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing playlist in the repository.
     *
     * @param updatedPlaylist The playlist with updated fields.
     * @return true if the playlist was found and updated, false otherwise.
     */
    public boolean update(Playlist updatedPlaylist) {
        List<Playlist> playlists = findAll();
        boolean found = false;

        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().equals(updatedPlaylist.getName()) &&
                    playlists.get(i).getOwnerID() == updatedPlaylist.getOwnerID()) {
                playlists.set(i, updatedPlaylist);
                found = true;
                break;
            }
        }

        if (found) {
            saveAll(playlists);
        }

        return found;
    }

    /**
     * Deletes a playlist by its name and owner.
     *
     * @param name  The name of the playlist to delete.
     * @param ownerID The owner of the playlist to delete.
     * @return true if the playlist was found and deleted, false otherwise.
     */
    public boolean deleteByNameAndOwner(String name, int ownerID) {
        List<Playlist> playlists = findAll();
        boolean removed = playlists.removeIf(playlist ->
                playlist.getName().equals(name) && playlist.getOwnerID() == ownerID);

        if (removed) {
            saveAll(playlists);
        }

        return removed;
    }
}