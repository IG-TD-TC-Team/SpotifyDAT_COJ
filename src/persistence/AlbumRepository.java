package persistence;

import songsAndArtists.Album;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A repository for persisting and retrieving Album objects.
 * Extends the generic JsonRepository to provide Album-specific functionalities.
 */
public class AlbumRepository extends JsonRepository<Album> {

    /**
     * Constructor that initializes the Album repository.
     */
    public AlbumRepository() {
        super(Album.class, "albums.json");
    }

    /**
     * Finds an album by its ID.
     *
     * @param albumId The ID of the album to find.
     * @return An Optional containing the album if found, or empty if not found.
     */
    public Optional<Album> findById(int albumId) {
        return findAll().stream()
                .filter(album -> album.getId() == albumId)
                .findFirst();
    }

    /**
     * Finds albums by artist ID.
     *
     * @param artistId The ID of the artist whose albums to find.
     * @return A List of albums by the specified artist.
     */
    public List<Album> findByArtistId(String artistId) {
        return findAll().stream()
                .filter(album -> album.getArtistId().equals(artistId))
                .collect(Collectors.toList());
    }

    /**
     * Finds albums that contain a specific song.
     *
     * @param songId The ID of the song to search for.
     * @return A List of albums containing the specified song.
     */
    public List<Album> findBySongId(int songId) {
        return findAll().stream()
                .filter(album -> album.getSongs() != null && album.getSongs().contains(songId))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing album in the repository.
     *
     * @param updatedAlbum The album with updated fields.
     * @return true if the album was found and updated, false otherwise.
     */
    public boolean update(Album updatedAlbum) {
        List<Album> albums = findAll();
        boolean found = false;

        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId() == updatedAlbum.getId()) {
                albums.set(i, updatedAlbum);
                found = true;
                break;
            }
        }

        if (found) {
            saveAll(albums);
        }

        return found;
    }

    /**
     * Deletes an album by its ID.
     *
     * @param albumId The ID of the album to delete.
     * @return true if the album was found and deleted, false otherwise.
     */
    public boolean deleteById(int albumId) {
        List<Album> albums = findAll();
        boolean removed = albums.removeIf(album -> album.getId() == albumId);

        if (removed) {
            saveAll(albums);
        }

        return removed;
    }

    /**
     * Adds a song to an album.
     *
     * @param albumId The ID of the album.
     * @param songId The ID of the song to add.
     * @return true if the album was found and the song was added, false otherwise.
     */
    public boolean addSongToAlbum(int albumId, int songId) {
        Optional<Album> albumOpt = findById(albumId);

        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            List<Integer> songs = album.getSongs();

            // If the song isn't already in the album, add it
            if (!songs.contains(songId)) {
                songs.add(songId);
                album.setSongs(songs);
                return update(album);
            }
            return true; // Song was already in the album
        }

        return false; // Album not found
    }

    /**
     * Removes a song from an album.
     *
     * @param albumId The ID of the album.
     * @param songId The ID of the song to remove.
     * @return true if the album was found and the song was removed, false otherwise.
     */
    public boolean removeSongFromAlbum(int albumId, int songId) {
        Optional<Album> albumOpt = findById(albumId);

        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            List<Integer> songs = album.getSongs();
            boolean removed = songs.removeIf(id -> id == songId);

            if (removed) {
                album.setSongs(songs);
                return update(album);
            }

            return false; // Song was not in the album
        }

        return false; // Album not found
    }
}