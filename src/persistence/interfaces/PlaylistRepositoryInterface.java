package persistence.interfaces;

import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Playlist entities.
 */
public interface PlaylistRepositoryInterface extends Repository<Playlist> {

    /**
     * Finds a playlist by its name and owner ID.
     */
    Optional<Playlist> findByNameAndOwnerID(String name, int ownerID);

    /**
     * Finds playlists by owner ID.
     */
    List<Playlist> findByOwnerID(int ownerID);

    /**
     * Finds playlists shared with a specific user.
     */
    List<Playlist> findSharedWithUserByID(int userID);

    /**
     * Deletes a playlist by its name and owner.
     */
    boolean deleteByNameAndOwner(String name, int ownerID);

    /**
     * Adds a song to a playlist.
     */
    boolean addSongToPlaylist(String name, int ownerID, Song song);

    /**
     * Shares a playlist with a user.
     */
    boolean sharePlaylistWithUser(String name, int ownerID, int userID);

    /**
     * Unshares a playlist with a user.
     */
    boolean unsharePlaylistWithUser(String name, int ownerID, int userID);
}