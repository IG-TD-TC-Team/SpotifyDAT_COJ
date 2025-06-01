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

    @Override
    public List<Album> findByArtistId(int artistId) {
        return findAll().stream()
                .filter(album -> album.getArtistId() == artistId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> findBySongId(int songId) {
        return findAll().stream()
                .filter(album -> album.getSongs() != null && album.getSongs().contains(songId))
                .collect(Collectors.toList());
    }

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