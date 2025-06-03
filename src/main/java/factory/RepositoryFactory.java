package factory;

import cache.*;
import persistence.*;
import persistence.interfaces.*;
import songsAndArtists.Album;
import songsAndArtists.Artist;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import user.User;

import java.util.List;

/**
 * Factory for creating and providing access to repository instances.
 *
 * This class follows both the Factory and Singleton patterns:
 * - Factory pattern: Creates and configures appropriate repository instances
 * - Singleton pattern: Ensures consistent repository access throughout the application
 *
 * The RepositoryFactory centralizes repository creation and configuration, especially:
 * - Configuring caching strategies for different entity types
 * - Creating appropriate repository wrappers
 * - Ensuring single instances of repositories are used application-wide
 *
 * This factory acts as a dependency provider in the application's dependency injection
 * approach, allowing other components to obtain repository references without
 * creating direct dependencies on concrete implementations.
 *
 */
public class RepositoryFactory {

    /**
     * The singleton instance of the factory.
     */
    private static RepositoryFactory instance;

    /**
     * Flag indicating whether the factory has been initialized.
     */
    private static boolean isInitialized = false;

    /**
     * Private constructor to enforce the Singleton pattern.
     * Performs one-time initialization when first instantiated.
     */
    // Private constructor to prevent instantiation
    private RepositoryFactory() {
        if (!isInitialized) {
            isInitialized = true;
        }
    }

    /**
     * Gets the singleton instance of RepositoryFactory.
     * Creates the instance if it doesn't exist yet.
     *
     * @return The singleton instance of RepositoryFactory
     */
    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    /**
     * Gets the UserRepository singleton instance.
     *
     * @return The UserRepository instance
     */
    public UserRepositoryInterface getUserRepository() {

        // Create base repository
        UserRepository baseUserRepo = UserRepository.getInstance();

        // Create cache strategy for users (no cache)
        CachingStrategy<User> userCache = new NoCacheStrategy<>();

        // Wrap with caching
        CachedRepository<User> cachedUserRepo = new CachedRepository<>(baseUserRepo, userCache,
                user -> "user_" + user.getUserID());

        // Return as UserRepositoryInterface
        return new CachedUserRepositoryWrapper(cachedUserRepo, baseUserRepo);
    }

    /**
     * Gets a new SongRepository instance.
     *
     * @return A SongRepository instance
     */
    public SongRepositoryInterface getSongRepository() {

        // Create base repository
        Repository<Song> baseSongRepo = SongRepository.getInstance();

        // Create cache strategy for songs (in-memory)
        CachingStrategy<Song> songCache = new InMemoryCacheStrategy<>();

        // Wrap with caching
        CachedRepository<Song> cachedSongRepo = new CachedRepository<>(baseSongRepo, songCache,
                song -> "song_" + song.getSongId());

        // Return as SongRepositoryInterface
        return new CachedSongRepositoryWrapper(cachedSongRepo);
    }

    /**
     * Gets a new ArtistRepository instance with caching applied.
     *
     * @return An ArtistRepositoryInterface instance
     */
    public ArtistRepositoryInterface getArtistRepository() {
        // Create base repository
        ArtistRepository baseArtistRepo = ArtistRepository.getInstance();

        // Create cache strategy for artists (in-memory for better performance)
        CachingStrategy<Artist> artistCache = new InMemoryCacheStrategy<>();

        // Wrap with caching
        CachedRepository<Artist> cachedArtistRepo = new CachedRepository<>(baseArtistRepo, artistCache,
                artist -> "artist_" + artist.getArtistID());

        // Return as ArtistRepositoryInterface with the wrapper
        return new CachedArtistRepositoryWrapper(cachedArtistRepo, baseArtistRepo);
    }

    /**
     * Gets a new AlbumRepository instance with caching applied.
     *
     * @return An AlbumRepositoryInterface instance
     */
    public AlbumRepositoryInterface getAlbumRepository() {
        // Create base repository
        AlbumRepository baseAlbumRepo = AlbumRepository.getInstance();

        // Create cache strategy for albums (in-memory for better performance)
        CachingStrategy<Album> albumCache = new InMemoryCacheStrategy<>();

        // Wrap with caching
        CachedRepository<Album> cachedAlbumRepo = new CachedRepository<>(baseAlbumRepo, albumCache,
                album -> "album_" + album.getId());

        // Return as AlbumRepositoryInterface with the wrapper
        return new CachedAlbumRepositoryWrapper(cachedAlbumRepo, baseAlbumRepo);
    }

    /**
     * Gets a new PlaylistRepository instance with caching applied.
     *
     * @return A PlaylistRepositoryInterface instance
     */
    public PlaylistRepositoryInterface getPlaylistRepository() {
        // Create base repository
        PlaylistRepository basePlaylistRepo = PlaylistRepository.getInstance();

        // Create cache strategy for playlists (in-memory for better performance)
        CachingStrategy<Playlist> playlistCache = new InMemoryCacheStrategy<>();

        // Wrap with caching
        CachedRepository<Playlist> cachedPlaylistRepo = new CachedRepository<>(basePlaylistRepo, playlistCache,
                playlist -> "playlist_" + playlist.getPlaylistID());

        // Return as PlaylistRepositoryInterface with the wrapper
        return new CachedPlaylistRepositoryWrapper(cachedPlaylistRepo, basePlaylistRepo);
    }
}