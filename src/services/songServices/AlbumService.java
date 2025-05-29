package services.songServices;

import factory.RepositoryFactory;
import persistence.interfaces.AlbumRepositoryInterface;
import songsAndArtists.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing albums.
 * Follows the repository-service pattern by using the AlbumRepository
 * to perform CRUD operations on albums.
 * Implements the Singleton pattern to ensure only one instance exists.
 */

public class AlbumService {

    /**
     * Singleton instance of AlbumService.
     */
    private static AlbumService instance;

    /**
     * Repository for accessing album data.
     */
    private final AlbumRepositoryInterface albumRepository;

    /**
     * List of albums.
     */
    private List<Album> albums;


    /**
     * Private constructor to prevent external instantiation.
     * Initializes the album repository instance using the RepositoryFactory.
     */
    private AlbumService() {
        albumRepository = RepositoryFactory.getInstance().getAlbumRepository();
        albums = new ArrayList<>();
        albums = albumRepository.findAll();
    }
    /**
     * Returns the singleton instance of AlbumService.
     * @return The singleton instance of AlbumService.
     */
    public static synchronized AlbumService getInstance() {
        if (instance == null) {
            instance = new AlbumService();
        }
        return instance;
    }

    /// --------------------- ALBUM RETRIEVAL ----------------- ///
    /**
     * Retrieves an album by its ID.
     * @param albumId The ID of the album to retrieve.
     * @return The album with the specified ID, or null if not found.
     */
    public Album getAlbumById(int albumId) {
        refreshCache();
        for (Album album : albums) {
            if (album.getId() == albumId) {
                return album;
            }
        }
        return null;// Album not found
    }

    /**
     * Retrieves albums by title (case-insensitive).
     * @param title The title to search for.
     * @return A list of albums matching the title.
     */
    public List<Album> getAlbumsByTitle(String title) {
        refreshCache();
        List<Album> result = new ArrayList<>();
        String searchTerm = title.toLowerCase();

        for (Album album : albums) {
            if (album.getTitle() != null && album.getTitle().toLowerCase().contains(searchTerm)) {
                result.add(album);
            }
        }

        return result;
    }

    /**
     * Retrieves albums by artist ID.
     * @param artistId The ID of the artist.
     * @return A list of albums by the specified artist.
     */
    public List<Album> getAlbumsByArtist(int artistId) {
        refreshCache();
        return albumRepository.findByArtistId(artistId);
    }

    /**
     * Retrieves all albums.
     * @return A list of all albums.
     */
    public List<Album> getAllAlbums() {
        refreshCache();
        return albums;
    }

    /// ---------------------- ALBUM UPDATE ----------------- ///
    /**
     * Adds a song to an album.
     *
     * @param songId The ID of the song
     * @param albumId The ID of the album
     * @return true if successful, false otherwise
     */
    public boolean addSongToAlbum(int songId, int albumId) {
        return albumRepository.addSongToAlbum(albumId, songId);
    }

    /**
     * Removes a song from an album.
     *
     * @param songId The ID of the song
     * @param albumId The ID of the album
     * @return true if successful, false otherwise
     */
    public boolean removeSongFromAlbum(int songId, int albumId) {
        return albumRepository.removeSongFromAlbum(albumId, songId);
    }


    /// ----------------------CAHE REFRESH----------------- ///
    /**
     * Refreshes the in-memory cache with the latest data from repositories.
     * Call this after entities are created, updated, or deleted.
     */
    public void refreshCache() {
        albums = albumRepository.findAll();
    }

}
