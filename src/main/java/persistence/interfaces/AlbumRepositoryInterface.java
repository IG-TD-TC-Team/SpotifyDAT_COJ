package persistence.interfaces;

import songsAndArtists.Album;

import java.util.List;

/**
 * Repository interface for Album entities.
 */
public interface AlbumRepositoryInterface extends Repository<Album> {

    /**
     * Finds albums by artist ID.
     *
     * @param artistId The ID of the artist whose albums to find
     * @return A list of albums by the specified artist
     */
    List<Album> findByArtistId(int artistId);

    /**
     * Finds albums that contain a specific song.
     *
     * @param songId The ID of the song to search for
     * @return A list of albums containing the specified song
     */
    List<Album> findBySongId(int songId);

    /**
     * Adds a song to an album.
     *
     * @param albumId The ID of the album
     * @param songId The ID of the song to add
     * @return true if the album was found and the song was added, false otherwise
     */
    boolean addSongToAlbum(int albumId, int songId);

    /**
     * Removes a song from an album.
     *
     * @param albumId The ID of the album
     * @param songId The ID of the song to remove
     * @return true if the album was found and the song was removed, false otherwise
     */
    boolean removeSongFromAlbum(int albumId, int songId);
}