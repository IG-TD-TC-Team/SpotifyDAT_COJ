package services;
import songsAndArtists.*;
import persistence.*;

import java.util.ArrayList;
import java.util.List;


public class SongService {
    // Singleton instance
    private static SongService instance;

    // Repositories
    private final JsonRepository<Artist> artistRepo;
    private final JsonRepository<Song> songRepo;
    private final JsonRepository<Album> albumRepo;

    // Music data
    private List<Song> songs;
    private List<Artist> artists;
    private List<Album> albums;



    private SongService() {


        // Initialize repositories
        artistRepo = ArtistRepository.getInstance();
        songRepo = SongRepository.getInstance();
        albumRepo = AlbumRepository.getInstance();

        //Charge all the data from the JSON files in a list of objects List<T>
        songs = new ArrayList<>();
        songs = songRepo.findAll();

        artists = new ArrayList<>();
        artists = artistRepo.findAll();

        albums = new ArrayList<>();
        albums = albumRepo.findAll();

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
        refreshCache();
        for (Song song : songs) {
            if (song.getSongId() == songId) {
                return song;
            }
        }
        return null; // Song not found
    }

    /**
     * Retrieves all songs by a Song Name.
     * @param songTitle The name of the song to search for.
     * @return A list of songs with the specified name.
     */
    public List<Song> getSongsByTitle(String songTitle) {
        refreshCache();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getTitle().equalsIgnoreCase(songTitle)) {
                result.add(song);
            }
        }
        return result;
    }

    /**
     * Retrieves all songs by an artist ID.
     * @param artistId The ID of the artist whose songs to retrieve.
     * @return A list of songs by the specified artist.
     */
    public List<Song> getSongsByArtistId(int artistId) {
        refreshCache();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getArtistId() == artistId) {
                result.add(song);
            }
        }
        return result;

    }

    /**
     * Retrieves all songs by an artist name.
     * @param artistName The name of the artist whose songs to retrieve.
     * @return A list of songs by the specified artist.
     */
    public List<Song> getSongByArtistName(String artistName) {
        refreshCache();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            Artist artist = getArtistById(song.getArtistId());
            if (artist != null && artist.getLastName().equalsIgnoreCase(artistName)) {
                result.add(song);
            }
        }
        return result;
    }

    /**
     * Retrieves all songs by an album ID.
     * @param albumId The ID of the album whose songs to retrieve.
     * @return A list of songs by the specified album.
     */
    public List<Song> getSongsByAlbumId(int albumId) {
        refreshCache();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getAlbumId() == albumId) {
                result.add(song);
            }
        }
        return result;
    }

    /**
     * Retrieves all songs by an album name.
     * @param albumName The name of the album whose songs to retrieve.
     * @return A list of songs by the specified album.
     */
    public List<Song> getSongsByAlbumName(String albumName) {
        refreshCache();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            Album album = getAlbumById(song.getAlbumId());
            if (album != null && album.getTitle().equalsIgnoreCase(albumName)) {
                result.add(song);
            }
        }
        return result;
    }

    /**
     * Retrieves all songs by a genre.
     * @param genre The genre of the songs to retrieve.
     * @return A list of songs with the specified genre.
     */
    public List<Song> getSongsByGenre(Genre genre) {
        refreshCache();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getGenre() == genre) {
                result.add(song);
            }
        }
        return result;
    }
    /**
     * Retrieves all songs.
     * @return A list of all songs.
     */
    public List<Song> getAllSongs() {
        refreshCache();
        return songs;
    }

    /// ------------------------- ARTIST RETRIEVAL ----------------- ///
    /**
     * Retrieves an artist by their ID.
     * @param artistId The ID of the artist to retrieve.
     * @return The artist with the specified ID, or null if not found.
     */
    public Artist getArtistById(int artistId) {
        refreshCache();
        for (Artist artist : artists) {
            if (artist.getArtistID() == artistId) {
                return artist;
            }
        }
        return null; // Artist not found
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

    /// ----------------------CAHE REFRESH----------------- ///
    /**
     * Refreshes the in-memory cache with the latest data from repositories.
     * Call this after entities are created, updated, or deleted.
     */
    public void refreshCache() {
        songs = songRepo.findAll();
        artists = artistRepo.findAll();
        albums = albumRepo.findAll();
    }

}
