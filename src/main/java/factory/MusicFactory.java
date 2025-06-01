package factory;

import songsAndArtists.*;
import persistence.interfaces.*;

import java.util.*;

/**
 * Factory specifically for creating music-related entities (songs, artists, albums).
 * This factory delegates all data retrieval to the SongService and focuses solely
 * on entity creation and persistence.
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
     * Private constructor for singleton pattern.
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
     *
     * @return The singleton instance
     */
    public static synchronized MusicFactory getInstance() {
        if (instance == null) {
            instance = new MusicFactory();
        }
        return instance;
    }

    /**
     * Initializes the next ID values based on existing data.
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
     * Creates a new song.
     *
     * @param title The title of the song
     * @param artistId The ID of the artist
     * @param albumId The ID of the album
     * @param genre The genre of the song
     * @param durationSeconds The duration in seconds
     * @param filePath The file path to the song
     * @return The created Song
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
     * Creates a new artist.
     *
     * @param firstName The first name of the artist
     * @param lastName The last name of the artist
     * @param birthDate The birthdate of the artist
     * @param countryOfBirth The country of birth of the artist
     * @return The created Artist
     */
    public Artist createArtist(String firstName, String lastName, Date birthDate, String countryOfBirth) {
        int artistId = nextArtistId++;
        Artist artist = new Artist(artistId, firstName, lastName, birthDate, countryOfBirth);
        artist.setSongs(new ArrayList<>()); // Initialize empty songs list
        artistRepository.save(artist);

        return artist;
    }

    /**
     * Creates a new album.
     *
     * @param title The title of the album
     * @param artistId The ID of the artist
     * @return The created Album
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
     * Finds an artist by name or creates a new one if not found.
     * Uses SongService for lookup.
     *
     * @param firstName The first name of the artist
     * @param lastName The last name of the artist
     * @return The existing or newly created Artist
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
     * Finds an album by title and artist ID or creates a new one if not found.
     * Uses SongService for lookup.
     *
     * @param title The title of the album
     * @param artistId The ID of the artist
     * @return The existing or newly created Album
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
     * Creates a new song with an automatically generated file path.
     * Now properly sanitizes the file path to handle special characters.
     *
     * @param title The title of the song
     * @param artistId The ID of the artist
     * @param albumId The ID of the album
     * @param genre The genre of the song
     * @param durationSeconds The duration in seconds
     * @return The created Song
     */
    public Song createSongWithAutoPath(String title, int artistId, int albumId, Genre genre, int durationSeconds) {
        String sanitizedTitle = sanitizeFileName(title);
        String filePath = "/music/" + sanitizedTitle + ".mp3";
        return createSong(title, artistId, albumId, genre, durationSeconds, filePath);
    }

    /**
     * Sanitizes a file name by removing or replacing problematic characters.
     * This ensures the file path will work across different systems and protocols.
     *
     * @param fileName The original file name
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
     * Creates a complete song with artist and album, creating them if they don't exist.
     *
     * @param title The title of the song
     * @param artistFirstName The first name of the artist
     * @param artistLastName The last name of the artist
     * @param albumTitle The title of the album
     * @param genre The genre of the song
     * @param durationSeconds The duration in seconds
     * @return The created Song
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