package factory;

import cache.*;
import persistence.*;
import persistence.interfaces.*;
import songsAndArtists.Song;
import user.User;

import java.util.List;

/**
 * Factory for creating repository instances.
 * This factory centralizes access to all repositories in the application.
 * Since repositories implement the singleton pattern, this factory returns
 * the singleton instances.
 */
public class RepositoryFactory {

    private static RepositoryFactory instance;
    private static boolean isInitialized = false;

    // Private constructor to prevent instantiation
    private RepositoryFactory() {
        if (!isInitialized) {
            isInitialized = true;
        }
    }

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
     * Gets a new ArtistRepository instance.
     *
     * @return An ArtistRepository instance
     */
    public ArtistRepositoryInterface getArtistRepository() {
        return ArtistRepository.getInstance();
    }

    /**
     * Gets a new AlbumRepository instance.
     *
     * @return An AlbumRepository instance
     */
    public AlbumRepositoryInterface getAlbumRepository() {
        return AlbumRepository.getInstance();
    }

    /**
     * Gets a new PlaylistRepository instance.
     *
     * @return A PlaylistRepository instance
     */
    public PlaylistRepositoryInterface getPlaylistRepository() {
        return PlaylistRepository.getInstance();
    }

}