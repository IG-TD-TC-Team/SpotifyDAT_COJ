package factory;

import persistence.*;
import persistence.interfaces.*;

/**
 * Factory for creating repository instances.
 * This factory centralizes access to all repositories in the application.
 * Since repositories implement the singleton pattern, this factory returns
 * the singleton instances.
 */
public class RepositoryFactory {

    // Private constructor to prevent instantiation
    private RepositoryFactory() {}

    /**
     * Gets the UserRepository singleton instance.
     *
     * @return The UserRepository instance
     */
    public static UserRepositoryInterface getUserRepository() {
        return UserRepository.getInstance();
    }

    /**
     * Gets a new SongRepository instance.
     *
     * @return A SongRepository instance
     */
    public static SongRepositoryInterface getSongRepository() {
        return SongRepository.getInstance();
    }

    /**
     * Gets a new ArtistRepository instance.
     *
     * @return An ArtistRepository instance
     */
    public static ArtistRepositoryInterface getArtistRepository() {
        return ArtistRepository.getInstance();
    }

    /**
     * Gets a new AlbumRepository instance.
     *
     * @return An AlbumRepository instance
     */
    public static AlbumRepositoryInterface getAlbumRepository() {
        return AlbumRepository.getInstance();
    }

    /**
     * Gets a new PlaylistRepository instance.
     *
     * @return A PlaylistRepository instance
     */
    public static PlaylistRepositoryInterface getPlaylistRepository() {
        return PlaylistRepository.getInstance();
    }

    /**
     * Gets a new LibraryRepository instance.
     *
     * @return A LibraryRepository instance
     */
    public static LibraryRepositoryInterface getLibraryRepository() {
        return LibraryRepository.getInstance();
    }
}