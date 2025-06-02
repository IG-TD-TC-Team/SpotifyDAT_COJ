package factory;

import songsAndArtists.*;
import persistence.interfaces.*;

import java.util.*;

/**
 * Factory responsible for creating and managing music-related entities (songs, artists, albums).
 *
 * This class follows the Factory pattern to centralize the creation logic for music entities,
 * ensuring consistent ID generation, proper relationships between entities, and data persistence.
 * It delegates storage operations to the appropriate repositories while handling the business
 * logic of entity creation and relationships.
 *
 * As a singleton, MusicFactory ensures that entity IDs are properly managed across the application
 * lifecycle, preventing duplicate IDs and maintaining referential integrity between entities.
 *
 */
public class MusicFactory {

    // Singleton instance
    private static MusicFactory instance;

    // Repositories for persistence operations
    private final SongRepositoryInterface songRepository;
    private final ArtistRepositoryInterface artistRepository;
    private final AlbumRepositoryInterface albumRepository;

    // ID generators
    private int nextSongId = 1000;
    private int nextArtistId = 100;
    private int nextAlbumId = 10000;

    /**
     * Private constructor that initializes the factory with repositories and existing IDs.
     * Following the Singleton pattern, this constructor is only called once.
     */
    private MusicFactory() {
        // Get repositories from RepositoryFactory
        this.songRepository = RepositoryFactory.getInstance().getSongRepository();
        this.artistRepository = RepositoryFactory.getInstance().getArtistRepository();
        this.albumRepository = RepositoryFactory.getInstance().getAlbumRepository();

        // Initialize next IDs based on existing data
        initializeNextIds();
    }

    /**
     * Gets the singleton instance of MusicFactory.
     * This ensures that there is only one factory managing entity creation
     * throughout the application, maintaining consistency in ID generation.
     *
     * @return The singleton instance of MusicFactory
     */
    public static synchronized MusicFactory getInstance() {
        if (instance == null) {
            instance = new MusicFactory();
        }
        return instance;
    }

    /**
     * Initializes the next ID values based on existing data in repositories.
     * This method examines all existing entities to determine the next available
     * ID for each entity type, ensuring no ID conflicts occur when creating new entities.
     */
    private void initializeNextIds() {
        // Get all songs from SongService
        List<Song> songs = songRepository.findAll();
        for (Song song : songs) {
            if (song.getSongId() >= nextSongId) {
                nextSongId = song.getSongId() + 1;
            }
        }

        // Initialize artist IDs from artists repository
        List<Artist> artists = artistRepository.findAll();
        for (Artist artist : artists) {
            if (artist.getArtistID() >= nextArtistId) {
                nextArtistId = artist.getArtistID() + 1;
            }
        }

        // Initialize album IDs from albums repository
        List<Album> albums = albumRepository.findAll();
        for (Album album : albums) {
            if (album.getId() >= nextAlbumId) {
                nextAlbumId = album.getId() + 1;
            }
        }
    }

    /**
     * Creates a new song with the specified details and persists it to the repository.
     * This method also establishes relationships by adding the song to both the artist's
     * and album's song lists.
     *
     * @param title The title of the song
     * @param artistId The ID of the artist who created the song
     * @param albumId The ID of the album containing the song
     * @param genre The genre classification of the song
     * @param durationSeconds The duration of the song in seconds
     * @param filePath The file path to the song's audio file
     * @return The created and persisted Song entity
     * @throws IllegalArgumentException if the specified artist or album doesn't exist
     */
    public Song createSong(String title, int artistId, int albumId, Genre genre, int durationSeconds, String filePath) {
        // Verify that the artist and album exist using repositories
        Optional<Artist> artistOpt = artistRepository.findById(artistId);
        Optional<Album> albumOpt = albumRepository.findById(albumId);

        if (artistOpt.isEmpty()) {
            throw new IllegalArgumentException("Artist with ID " + artistId + " not found");
        }

        if (albumOpt.isEmpty()) {
            throw new IllegalArgumentException("Album with ID " + albumId + " not found");
        }

        int songId = nextSongId++;
        Song song = new Song(songId, title, artistId, albumId, genre, durationSeconds, filePath);

        // Add song to artist's songs list
        artistRepository.addSongToArtist(artistId, songId);

        // Add song to album's songs list
        albumRepository.addSongToAlbum(albumId, songId);

        // Save the song
        songRepository.save(song);

        return song;
    }

    /**
     * Creates a new artist with the specified details and persists it to the repository.
     *
     * @param firstName The first name of the artist
     * @param lastName The last name of the artist
     * @param birthDate The birthdate of the artist
     * @param countryOfBirth The country of birth of the artist
     * @return The created and persisted Artist entity
     */
    public Artist createArtist(String firstName, String lastName, Date birthDate, String countryOfBirth) {
        int artistId = nextArtistId++;
        Artist artist = new Artist(artistId, firstName, lastName, birthDate, countryOfBirth);
        artist.setSongs(new ArrayList<>()); // Initialize empty songs list
        artistRepository.save(artist);

        return artist;
    }

    /**
     * Creates a new album with the specified details and persists it to the repository.
     *
     * @param title The title of the album
     * @param artistId The ID of the artist who created the album
     * @return The created and persisted Album entity
     * @throws IllegalArgumentException if the specified artist doesn't exist
     */
    public Album createAlbum(String title, int artistId) {
        // Verify that the artist exists
        Optional<Artist> artistOpt = artistRepository.findById(artistId);
        if (artistOpt.isEmpty()) {
            throw new IllegalArgumentException("Artist with ID " + artistId + " not found");
        }

        int albumId = nextAlbumId++;
        Album album = new Album(albumId, title, artistId, new ArrayList<>());
        albumRepository.save(album);

        return album;
    }

    /**
     * Finds an existing artist by name or creates a new one if not found.
     * This convenience method reduces code duplication when the application
     * needs to ensure an artist exists before referencing it.
     *
     * @param firstName The first name of the artist
     * @param lastName The last name of the artist
     * @return An existing artist with the matching name, or a newly created one if not found
     */
    public Artist findOrCreateArtist(String firstName, String lastName) {
        // Look for artists with matching name in songs
        List<Artist> matchingArtists = artistRepository.findByName(firstName + " " + lastName);

        // Return first matching artist if found
        if (!matchingArtists.isEmpty()) {
            return matchingArtists.get(0);
        }


        // Artist not found, create a new one
        return createArtist(firstName, lastName, new Date(), "Unknown");

    }

    /**
     * Finds an existing album by title and artist ID or creates a new one if not found.
     * This convenience method reduces code duplication when the application
     * needs to ensure an album exists before referencing it.
     *
     * @param title The title of the album
     * @param artistId The ID of the artist who created the album
     * @return An existing album with the matching title and artist, or a newly created one if not found
     */
    public Album findOrCreateAlbum(String title, int artistId) {
        // Find albums by artist ID
        List<Album> artistAlbums = albumRepository.findByArtistId(artistId);

        for (Album album : artistAlbums) {
            if (album.getTitle().equalsIgnoreCase(title)) {
                return album;
            }
        }

        // Album not found, create a new one
        return createAlbum(title, artistId);
    }

    // In MusicFactory.java, update the createSongWithAutoPath method:

    /**
     * Creates a new song with an automatically generated file path based on the song title.
     * This method sanitizes the title to create a safe file path that works across
     * different systems and protocols.
     *
     * @param title The title of the song
     * @param artistId The ID of the artist
     * @param albumId The ID of the album
     * @param genre The genre of the song
     * @param durationSeconds The duration in seconds
     * @return The created Song with an auto-generated file path
     */
    public Song createSongWithAutoPath(String title, int artistId, int albumId, Genre genre, int durationSeconds) {
        String sanitizedTitle = sanitizeFileName(title);
        String filePath = "/music/" + sanitizedTitle + ".mp3";
        return createSong(title, artistId, albumId, genre, durationSeconds, filePath);
    }

    /**
     * Sanitizes a file name by removing or replacing problematic characters.
     * This utility method ensures file paths are compatible across different operating
     * systems and file systems by removing special characters, spaces, and accents.
     *
     * @param fileName The original file name to sanitize
     * @return A sanitized file name safe for use in file paths
     */
    private String sanitizeFileName(String fileName) {
        // Convert to lowercase
        String sanitized = fileName.toLowerCase();

        // Replace common problematic characters with safe alternatives
        sanitized = sanitized
                .replace(" ", "")           // Remove spaces
                .replace("'", "")           // Remove apostrophes
                .replace("'", "")           // Remove smart apostrophes
                .replace("\"", "")          // Remove quotes
                .replace("?", "")           // Remove question marks
                .replace("!", "")           // Remove exclamation marks
                .replace(":", "")           // Remove colons
                .replace(";", "")           // Remove semicolons
                .replace("/", "_")          // Replace forward slashes
                .replace("\\", "_")         // Replace backslashes
                .replace("*", "")           // Remove asterisks
                .replace("<", "")           // Remove less than
                .replace(">", "")           // Remove greater than
                .replace("|", "")           // Remove pipes
                .replace(",", "")           // Remove commas
                .replace(".", "")           // Remove periods (except for extension)
                .replace("(", "")           // Remove opening parenthesis
                .replace(")", "")           // Remove closing parenthesis
                .replace("[", "")           // Remove opening bracket
                .replace("]", "")           // Remove closing bracket
                .replace("{", "")           // Remove opening brace
                .replace("}", "")           // Remove closing brace
                .replace("&", "and")        // Replace ampersand
                .replace("@", "at")         // Replace at symbol
                .replace("#", "")           // Remove hash
                .replace("$", "")           // Remove dollar sign
                .replace("%", "")           // Remove percent
                .replace("^", "")           // Remove caret
                .replace("=", "")           // Remove equals
                .replace("+", "")           // Remove plus
                .replace("~", "")           // Remove tilde
                .replace("`", "")           // Remove backtick
                .replace("é", "e")          // Replace accented e
                .replace("è", "e")          // Replace accented e
                .replace("ê", "e")          // Replace accented e
                .replace("ë", "e")          // Replace accented e
                .replace("à", "a")          // Replace accented a
                .replace("â", "a")          // Replace accented a
                .replace("ä", "a")          // Replace accented a
                .replace("ç", "c")          // Replace c with cedilla
                .replace("ñ", "n")          // Replace n with tilde
                .replace("ö", "o")          // Replace accented o
                .replace("ô", "o")          // Replace accented o
                .replace("ü", "u")          // Replace accented u
                .replace("ù", "u")          // Replace accented u
                .replace("û", "u")          // Replace accented u
                .replace("ï", "i")          // Replace accented i
                .replace("î", "i");         // Replace accented i

        // Remove any remaining non-alphanumeric characters (except hyphens and underscores)
        sanitized = sanitized.replaceAll("[^a-z0-9_-]", "");

        // Ensure the filename is not empty
        if (sanitized.isEmpty()) {
            sanitized = "untitled";
        }

        return sanitized;
    }

    /**
     * Creates a complete song with artist and album, creating any of these entities
     * if they don't already exist. This high-level method encapsulates the entire
     * song creation process, ensuring all necessary related entities exist.
     *
     * This method exemplifies the Factory pattern by handling the complex process
     * of creating interrelated entities while providing a simple interface.
     *
     *
     * @param title The title of the song
     * @param artistFirstName The first name of the artist
     * @param artistLastName The last name of the artist
     * @param albumTitle The title of the album
     * @param genre The genre of the song
     * @param durationSeconds The duration in seconds
     * @return The created Song entity with all relationships established
     */
    public Song createCompleteSong(String title, String artistFirstName, String artistLastName,
                                   String albumTitle, Genre genre, int durationSeconds) {
        // Find or create artist
        Artist artist = findOrCreateArtist(artistFirstName, artistLastName);

        // Find or create album
        Album album = findOrCreateAlbum(albumTitle, artist.getArtistID());

        // Create song with auto-generated file path
        return createSongWithAutoPath(title, artist.getArtistID(), album.getId(), genre, durationSeconds);
    }


}