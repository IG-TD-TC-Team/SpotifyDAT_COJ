package persistence;

import songsAndArtists.Song;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A repository for persisting and retrieving Song objects.
 * Extends the generic JsonRepository to provide Song-specific functionalities.
 */
public class SongRepository extends JsonRepository<Song> {

    /**
     * Constructor that initializes the Song repository.
     */
    public SongRepository() {
        super(Song.class, "songs.json");
    }

    /**
     * Finds a song by its ID.
     *
     * @param songId The ID of the song to find.
     * @return An Optional containing the song if found, or empty if not found.
     */
    public Optional<Song> findById(int songId) {
        return findAll().stream()
                .filter(song -> song.getSongId() == songId)
                .findFirst();
    }

    /**
     * Finds songs by artist ID.
     *
     * @param artistId The ID of the artist whose songs to find.
     * @return A List of songs by the specified artist.
     */
    public List<Song> findByArtistId(String artistId) {
        return findAll().stream()
                .filter(song -> song.getArtistId().equals(artistId))
                .collect(Collectors.toList());
    }

    /**
     * Finds songs by genre.
     *
     * @param genre The genre to filter songs by.
     * @return A List of songs of the specified genre.
     */
    public List<Song> findByGenre(String genre) {
        return findAll().stream()
                .filter(song -> song.getGenre().equals(genre))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing song in the repository.
     *
     * @param updatedSong The song with updated fields.
     * @return true if the song was found and updated, false otherwise.
     */
    public boolean update(Song updatedSong) {
        List<Song> songs = findAll();
        boolean found = false;

        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getSongId() == updatedSong.getSongId()) {
                songs.set(i, updatedSong);
                found = true;
                break;
            }
        }

        if (found) {
            saveAll(songs);
        }

        return found;
    }

    /**
     * Deletes a song by its ID.
     *
     * @param songId The ID of the song to delete.
     * @return true if the song was found and deleted, false otherwise.
     */
    public boolean deleteById(int songId) {
        List<Song> songs = findAll();
        boolean removed = songs.removeIf(song -> song.getSongId() == songId);

        if (removed) {
            saveAll(songs);
        }

        return removed;
    }
}