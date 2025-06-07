package persistence.interfaces;

import songsAndArtists.Artist;

import java.util.List;

/**
 * Repository interface for managing Artist entities in the persistence layer.
 *
 * <p>This interface extends the base {@link Repository} interface to provide
 * specialized operations for Artist entities. It includes methods for querying
 * artists by various criteria such as name and country, as well as managing
 * the relationship between artists and their songs.</p>
 *
 * <p>Implementations of this interface should handle:</p>
 * <ul>
 *   <li>CRUD operations for Artist entities</li>
 *   <li>Complex queries based on artist attributes</li>
 *   <li>Maintaining bidirectional relationships between artists and songs</li>
 *   <li>Data consistency when modifying artist-song associations</li>
 * </ul>
 *
 * <p>This interface follows the Repository pattern to abstract data access
 * operations and provide a clean separation between business logic and
 * data persistence concerns.</p>
 *
 * @see Artist
 * @see Repository
 */
public interface ArtistRepositoryInterface extends Repository<Artist> {
    /**
     * Finds artists by name (first name, last name, or full name).
     */
    List<Artist> findByName(String name);

    /**
     * Finds artists by country.
     */
    List<Artist> findByCountry(String country);

    /**
     * Adds a song to an artist's song list.
     */
    boolean addSongToArtist(int artistId, int songId);

    /**
     * Removes a song from an artist's song list.
     */
    boolean removeSongFromArtist(int artistId, int songId);
}