package factory;

import songsAndArtists.*;
import persistence.interfaces.*;
import managers.SongManager;
import java.util.*;

/**
 * Factory specifically for creating music-related entities (songs, artists, albums).
 * This factory delegates all data retrieval to the SongManager and focuses solely
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
        this.songRepository = RepositoryFactory.getSongRepository();
        this.artistRepository = RepositoryFactory.getArtistRepository();
        this.albumRepository = RepositoryFactory.getAlbumRepository();

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
        // Get all songs from SongManager
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
     * Uses SongManager for lookup.
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
     * Uses SongManager for lookup.
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

    /**
     * Creates a new song with an automatically generated file path.
     *
     * @param title The title of the song
     * @param artistId The ID of the artist
     * @param albumId The ID of the album
     * @param genre The genre of the song
     * @param durationSeconds The duration in seconds
     * @return The created Song
     */
    public Song createSongWithAutoPath(String title, int artistId, int albumId, Genre genre, int durationSeconds) {
        String filePath = "/music/" + title.toLowerCase().replace(" ", "") + ".mp3";
        return createSong(title, artistId, albumId, genre, durationSeconds, filePath);
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
}