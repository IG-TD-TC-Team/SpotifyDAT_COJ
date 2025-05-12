package services.songServices;
import factory.RepositoryFactory;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.*;

import java.util.ArrayList;
import java.util.List;


public class SongService {
    // Singleton instance
    private static SongService instance;

    // Repositories
    private final SongRepositoryInterface songRepository;


    // Services for related entities
    private ArtistService artistService = ArtistService.getInstance();
    private AlbumService albumService = AlbumService.getInstance();

    private SongService() {
        // Initialize repositories
        songRepository = RepositoryFactory.getInstance().getSongRepository();
    }

    /// Public method to get the singleton instance
    public static synchronized SongService getInstance() {
        if (instance == null) {
            instance = new SongService();
        }
        return instance;
    }

    /// --------------------- SONG RETRIEVAL ----------------- ///
    /**
     * Retrieves a song by its ID.
     * @param songId The ID of the song to retrieve.
     * @return The song with the specified ID, or null if not found.
     */
    public Song getSongById(int songId) {
        return songRepository.findById(songId).orElse(null); // Song not found
    }

    /**
     * Retrieves all songs by a Song Name.
     * @param songTitle The name of the song to search for.
     * @return A list of songs with the specified name.
     */
    public List<Song> getSongsByTitle(String songTitle) {
        return songRepository.findByTitle(songTitle);
    }

    /**
     * Retrieves all songs by an artist ID.
     * @param artistId The ID of the artist whose songs to retrieve.
     * @return A list of songs by the specified artist.
     */
    public List<Song> getSongsByArtistId(int artistId) {
        return songRepository.findByArtistId(artistId);
    }

    /**
     * Retrieves all songs by an artist name.
     * @param artistName The name of the artist whose songs to retrieve.
     * @return A list of songs by the specified artist.
     */
    public List<Song> getSongByArtistName(String artistName) {
        return songRepository.findAll().stream()
                .filter(song -> {
                    Artist artist = artistService.getArtistById(song.getArtistId());
                    return artist != null && artist.getLastName().equalsIgnoreCase(artistName);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves all songs by an album ID.
     * @param albumId The ID of the album whose songs to retrieve.
     * @return A list of songs by the specified album.
     */
    public List<Song> getSongsByAlbumId(int albumId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getAlbumId() == albumId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves all songs by an album name.
     * @param albumName The name of the album whose songs to retrieve.
     * @return A list of songs by the specified album.
     */
    public List<Song> getSongsByAlbumName(String albumName) {
        return songRepository.findAll().stream()
                .filter(song -> {
                    Album album = albumService.getAlbumById(song.getAlbumId());
                    return album != null && album.getTitle().equalsIgnoreCase(albumName);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves all songs by a genre.
     * @param genre The genre of the songs to retrieve.
     * @return A list of songs with the specified genre.
     */
    public List<Song> getSongsByGenre(Genre genre) {
        return songRepository.findByGenre(genre);
    }

    /**
     * Retrieves all songs.
     * @return A list of all songs.
     */
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

}
