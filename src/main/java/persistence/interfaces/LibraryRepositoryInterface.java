package persistence.interfaces;

import songsOrganisation.Library;
import songsOrganisation.Playlist;

import java.util.List;

/**
 * Repository interface for Library entities.
 */
public interface LibraryRepositoryInterface extends Repository<Library> {

    /**
     * Adds a playlist to a user's library.
     */
    boolean addPlaylistToLibrary(int userId, Playlist playlist);

    /**
     * Removes a playlist from a user's library.
     */
    boolean removePlaylistFromLibrary(int userId, String playlistName);

    /**
     * Gets all playlists in a user's library.
     */
    List<Playlist> getUserPlaylists(int userId);

    /**
     * Creates a new library for a user if it doesn't exist.
     */
    Library createLibrary(int userId);
}