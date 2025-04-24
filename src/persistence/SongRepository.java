package persistence;


import persistence.interfaces.*;
import songsAndArtists.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A repository for persisting and retrieving Song objects.
 * Extends the generic JsonRepository to provide Song-specific functionalities.
 *
 */
public class SongRepository extends JsonRepository<Song> implements SongRepositoryInterface {

    /**
     * Constructor that initializes the Song repository.
     * It sets the entity type to Song, the storage file to "songs.json",
     */
    public SongRepository() {
        super(Song.class, "songs.json", Song::getSongId);
    }

    /**
     * Finds all songs in the repository.
     *
     * @return A list of all songs.
     */
    @Override
    public List<Song> findByArtistId(int artistId) {
        return findAll().stream()
                .filter(song -> song.getArtistId() == artistId)
                .collect(Collectors.toList());
    }

    /**
     * Finds all songs in the repository.
     *
     * @return A list of all songs.
     */
    @Override
    public List<Song> findByGenre(Genre genre) {
        return findAll().stream()
                .filter(song -> song.getGenre().equals(genre))
                .collect(Collectors.toList());
    }

    /**
     * Finds all songs in the repository.
     *
     * @return A list of all songs.
     */
    @Override
    public List<Song> findByTitle(String title) {
        return findAll().stream()
                .filter(song -> song.getTitle().equalsIgnoreCase(title))
                .collect(Collectors.toList());
    }
}