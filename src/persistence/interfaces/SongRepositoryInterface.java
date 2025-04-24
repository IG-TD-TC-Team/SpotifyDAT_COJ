package persistence.interfaces;

import songsAndArtists.Genre;
import songsAndArtists.Song;
import java.util.List;

/**
 * Repository interface for Song entities.
 */
public interface SongRepositoryInterface extends Repository<Song> {

    /**
     * Finds songs by artist ID.
     */
    List<Song> findByArtistId(int artistId);

    /**
     * Finds songs by genre.
     */
    List<Song> findByGenre(Genre genre);

    /**
     * Finds songs by title.
     */
    List<Song> findByTitle(String title);
}