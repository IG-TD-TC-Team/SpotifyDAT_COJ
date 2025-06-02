package persistence;

import persistence.JsonRepository;
import persistence.interfaces.*;
import songsAndArtists.Album;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of AlbumRepository interface for persisting Album entities as JSON.
 */
public class AlbumRepository extends JsonRepository<Album> implements AlbumRepositoryInterface {

    /**
     * Singleton instance of AlbumRepository.
     */
    private static AlbumRepository instance;

    /**
     * Private constructor that initializes the Album repository.
     */
    private AlbumRepository() {
        super(Album.class, "albums.json", Album::getId);
    }
    /**
     * Gets the singleton instance of UserRepository.
     *
     * @return The singleton instance
     */
    public static synchronized AlbumRepository getInstance() {
        if (instance == null) {
            instance = new AlbumRepository();
        }
        return instance;
    }

    /**
     * Finds albums created by a specific artist.
     * Filters all albums to find those with the specified artist ID.
     *
     * @param artistId The ID of the artist whose albums to find
     * @return A list of albums created by the specified artist
     */
    @Override
    public List<Album> findByArtistId(int artistId) {
        return findAll().stream()
                .filter(album -> album.getArtistId() == artistId)
                .collect(Collectors.toList());
    }

    /**
     * Finds albums that contain a specific song.
     * Filters all albums to find those that include the specified song ID
     * in their songs list.
     *
     * @param songId The ID of the song to search for
     * @return A list of albums containing the specified song
     */
    @Override
    public List<Album> findBySongId(int songId) {
        return findAll().stream()
                .filter(album -> album.getSongs() != null && album.getSongs().contains(songId))
                .collect(Collectors.toList());
    }

    /**
     * Adds a song to an album.
     * Retrieves the album by ID, adds the song to its songs list if not already present,
     * and persists the updated album.
     *
     * @param albumId The ID of the album to update
     * @param songId The ID of the song to add
     * @return true if the album was found and updated or if the song was already in the album,
     *         false if the album wasn't found
     */
    @Override
    public boolean addSongToAlbum(int albumId, int songId) {
        Optional<Album> albumOpt = findById(albumId);

        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            List<Integer> songs = album.getSongs();

            // If the song isn't already in the album, add it
            if (!songs.contains(songId)) {
                songs.add(songId);
                album.setSongs(songs);
                return update(album).isPresent();
            }
            return true; // Song was already in the album
        }

        return false; // Album not found
    }

    /**
     * Removes a song from an album.
     * Retrieves the album by ID, removes the song from its songs list if present,
     * and persists the updated album.
     *
     * @param albumId The ID of the album to update
     * @param songId The ID of the song to remove
     * @return true if the album was found and the song was removed and the update was successful,
     *         false if the album wasn't found or the song wasn't in the album
     */
    @Override
    public boolean removeSongFromAlbum(int albumId, int songId) {
        Optional<Album> albumOpt = findById(albumId);

        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            List<Integer> songs = album.getSongs();
            boolean removed = songs.removeIf(id -> id == songId);

            if (removed) {
                album.setSongs(songs);
                return update(album).isPresent();
            }

            return false; // Song was not in the album
        }

        return false; // Album not found
    }
}