package services.songServices;
import factory.RepositoryFactory;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for retrieving and managing song data.
 *
 * This class provides a centralized access point for song-related operations,
 * including searching and retrieving songs by various criteria such as ID, title,
 * artist, album, and genre. It implements the Singleton pattern to ensure a single
 * instance manages song data throughout the application.
 *
 * The service abstracts the underlying repository interactions and provides
 * business logic for advanced search operations, handling the relationships between
 * songs and other entities like artists and albums.
 */
public class SongService {
    /**
     * Singleton instance of the SongService.
     * Ensures only one instance exists throughout the application.
     */
    // Singleton instance
    private static SongService instance;

    /**
     * Repository interface for accessing song data.
     * Follows the Repository pattern to abstract data access operations.
     */
    // Repositories
    private final SongRepositoryInterface songRepository;

    /**
     * References to related services for accessing artist and album data.
     * These fields use lazy initialization to prevent circular dependencies.
     */
    // Services for related entities
    private ArtistService artistService;
    private AlbumService albumService;

    /**
     * Private constructor following the Singleton pattern.
     * Initializes the song repository but defers service dependencies to avoid
     * circular references.
     */
    private SongService() {
        // Initialize repositories
        songRepository = RepositoryFactory.getInstance().getSongRepository();

        // Initialize services after this instance is created to avoid circular dependency
        // We don't initialize these in the field declaration to prevent circular dependency
    }

    /**
     * Lazily initializes and returns the ArtistService instance.
     * This method helps prevent circular dependencies by deferring initialization
     * until the service is actually needed.
     *
     * @return The singleton instance of ArtistService
     */
    // Lazy initialization of related services to avoid circular dependency
    private ArtistService getArtistService() {
        if (artistService == null) {
            artistService = ArtistService.getInstance();
        }
        return artistService;
    }

    /**
     * Lazily initializes and returns the AlbumService instance.
     * This method helps prevent circular dependencies by deferring initialization
     * until the service is actually needed.
     *
     * @return The singleton instance of AlbumService
     */
    private AlbumService getAlbumService() {
        if (albumService == null) {
            albumService = AlbumService.getInstance();
        }
        return albumService;
    }

    /**
     * Returns the singleton instance of SongService.
     * Creates the instance if it doesn't exist yet, following the lazy initialization
     * approach to the Singleton pattern.
     *
     * @return The singleton instance of SongService
     */
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
     * Retrieves songs by title with flexible matching.
     * First tries exact match, then partial match if no exact matches found.
     * @param songTitle The title of the song to search for.
     * @return A list of songs matching the title exactly or partially.
     */
    public List<Song> getSongsByTitleFlexible(String songTitle) {
        // First try exact match (case-insensitive)
        List<Song> exactMatches = songRepository.findByTitle(songTitle);

        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }

        // If no exact matches, try partial match
        String normalizedSearch = songTitle.toLowerCase().trim();
        return songRepository.findAll().stream()
                .filter(song -> {
                    String normalizedTitle = song.getTitle().toLowerCase().trim();
                    return normalizedTitle.contains(normalizedSearch) ||
                            normalizedSearch.contains(normalizedTitle);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Enhanced search that also removes common punctuation and extra spaces
     *
     * @param songTitle The title of the song to search for.
     * @return A list of songs matching the normalized title.
     */
    public List<Song> getSongsByTitleAdvanced(String songTitle) {
        String normalizedSearchTerm = normalizeTitle(songTitle);

        return songRepository.findAll().stream()
                .filter(song -> {
                    String normalizedSongTitle = normalizeTitle(song.getTitle());
                    return normalizedSongTitle.equals(normalizedSearchTerm) ||
                            normalizedSongTitle.contains(normalizedSearchTerm) ||
                            normalizedSearchTerm.contains(normalizedSongTitle);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Normalizes title by removing extra spaces, punctuation, and converting to lowercase
     * @param title The title to normalize.
     * @return The normalized title string, or an empty string if the input is null.
     */
    private String normalizeTitle(String title) {
        if (title == null) return "";

        return title.toLowerCase()
                .replaceAll("[\\p{Punct}]", "") // Remove punctuation
                .replaceAll("\\s+", " ")        // Replace multiple spaces with single space
                .trim();
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
     * The method now checks both first name, last name, and full name (case-insensitive).
     * @param artistName The name of the artist whose songs to retrieve.
     * @return A list of songs by the specified artist.
     */
    public List<Song> getSongByArtistName(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Normalize the search name
        String normalizedName = artistName.toLowerCase().trim();

        // Get all artists that match this name
        List<Artist> matchingArtists = getArtistService().getAllArtists().stream()
                .filter(artist -> {
                    String firstName = artist.getFirstName() != null ? artist.getFirstName().toLowerCase() : "";
                    String lastName = artist.getLastName() != null ? artist.getLastName().toLowerCase() : "";
                    String fullName = firstName + " " + lastName;

                    return firstName.equals(normalizedName) ||
                            lastName.equals(normalizedName) ||
                            fullName.equals(normalizedName) ||
                            firstName.contains(normalizedName) ||
                            lastName.contains(normalizedName) ||
                            fullName.contains(normalizedName);
                })
                .collect(Collectors.toList());

        // If no matching artists, return empty list
        if (matchingArtists.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all songs by these artists
        List<Song> result = new ArrayList<>();
        for (Artist artist : matchingArtists) {
            result.addAll(getSongsByArtistId(artist.getArtistID()));
        }

        return result;
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
        if (albumName == null || albumName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Normalize the search name
        String normalizedName = albumName.toLowerCase().trim();

        // Get all albums that match this name
        List<Album> matchingAlbums = getAlbumService().getAllAlbums().stream()
                .filter(album -> {
                    String title = album.getTitle() != null ? album.getTitle().toLowerCase() : "";
                    return title.equals(normalizedName) || title.contains(normalizedName);
                })
                .collect(Collectors.toList());

        // If no matching albums, return empty list
        if (matchingAlbums.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all songs from these albums
        List<Song> result = new ArrayList<>();
        for (Album album : matchingAlbums) {
            result.addAll(getSongsByAlbumId(album.getId()));
        }

        return result;
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