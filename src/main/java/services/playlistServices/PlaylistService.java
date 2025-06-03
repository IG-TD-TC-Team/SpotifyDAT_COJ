package services.playlistServices;

import factory.PlaylistFactory;
import factory.RepositoryFactory;
import persistence.PlaylistRepository;
import persistence.interfaces.PlaylistRepositoryInterface;
import services.userServices.UserService;
import songsAndArtists.Genre;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import user.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages playlist-related operations such as retrieving playlists
 * and providing filtering functionality.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class PlaylistService {

    /**
     * Singleton instance of PlaylistService.
     */
    private static PlaylistService instance;

    /**
     * Repository for accessing playlist data.
     */
    private final PlaylistRepositoryInterface playlistRepository;

    /**
     * Cache of all playlists for faster retrieval.
     */
    private List<Playlist> playlistsCache;

    /**
     * Flag to track if the cache is initialized
     */
    private boolean cacheInitialized = false;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the PlaylistRepository instance and loads all playlists.
     */
    private PlaylistService() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        initializeCache();
    }

    /**
     * Returns the single instance of PlaylistService, creating it if it doesn't exist.
     *
     * @return the singleton instance of PlaylistService
     */
    public static synchronized PlaylistService getInstance() {
        if (instance == null) {
            instance = new PlaylistService();
        }
        return instance;
    }

    /**
     * Initializes the cache by loading all playlists from the repository.
     * This is called once during service initialization.
     */
    private void initializeCache() {
        this.playlistsCache = playlistRepository.findAll();
        this.cacheInitialized = true;
        System.out.println("PlaylistService initialized with " + playlistsCache.size() + " playlists");
    }

    /// ------------------- CACHE REFRESH ----------------- ///
    /**
     * Refreshes the in-memory cache with the latest data from repositories.
     * Call this after playlists are created, updated, or deleted.
     */
    public void refreshCache() {
        this.playlistsCache = playlistRepository.findAll();
        System.out.println("PlaylistService cache refreshed with " + playlistsCache.size() + " playlists");
    }

    /**
     * Ensures the cache is loaded before accessing it.
     * This method is called by all methods that access the cache to ensure
     * it is initialized and up-to-date.
     */
    private void ensureCacheIsLoaded() {
        if (!cacheInitialized) {
            initializeCache();
        }
    }

    /// ------------------ PLAYLIST RETRIEVAL ----------------- ///
    /**
     * Retrieves all playlists.
     *
     * @return a list of all playlists
     */
    public List<Playlist> getAllPlaylists() {
        ensureCacheIsLoaded();
        return new ArrayList<>(playlistsCache); // Return a copy to prevent external modification
    }

    /**
     * Retrieves a playlist by its ID.
     *
     * @param playlistId the ID of the playlist
     * @return the playlist with the specified ID, or null if not found
     */
    public Playlist getPlaylistById(int playlistId) {
        // Use the repository directly for single item lookup to leverage its caching
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        return playlist.orElse(null);
    }

    /**
     * Retrieves a playlist by its name and owner ID.
     *
     * @param name the name of the playlist
     * @param ownerID the ID of the owner
     * @return the playlist with the specified name and owner ID, or null if not found
     */
    public Playlist getPlaylistByNameAndOwner(String name, int ownerID) {
        // Use the repository directly for specialized queries
        Optional<Playlist> playlist = playlistRepository.findByNameAndOwnerID(name, ownerID);
        return playlist.orElse(null);
    }

    /**
     * Retrieves all playlists owned by a specific user.
     *
     * @param ownerID the ID of the owner
     * @return a list of playlists owned by the specified user
     */
    public List<Playlist> getPlaylistsByOwner(int ownerID) {
        // Use the repository directly for specialized queries
        return playlistRepository.findByOwnerID(ownerID);
    }

    /**
     * Retrieves all playlists owned by a specific user by username.
     *
     * @param username the username of the owner
     * @param userService the UserService instance to resolve username to ID
     * @return a list of playlists owned by the specified user
     */
    public List<Playlist> getPlaylistsByOwnerUsername(String username, UserService userService) {
        User user = userService.getUserByUsername(username);
        return getPlaylistsByOwner(user.getUserID());
    }

    /**
     * Retrieves all playlists that contain a specific song.
     *
     * @param songId the ID of the song
     * @return a list of playlists containing the specified song
     */
    public List<Playlist> getPlaylistsContainingSong(int songId) {
        ensureCacheIsLoaded();
        return playlistsCache.stream()
                .filter(playlist -> playlist.getSongs().stream()
                        .anyMatch(song -> song.getSongId() == songId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all playlists that contain songs of a specific genre.
     *
     * @param genre the genre of songs to search for
     * @return a list of playlists containing songs of the specified genre
     */
    public List<Playlist> getPlaylistsContainingGenre(Genre genre) {
        ensureCacheIsLoaded();
        return playlistsCache.stream()
                .filter(playlist -> playlist.getSongs().stream()
                        .anyMatch(song -> song.getGenre() == genre))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all playlists that contain songs by a specific artist.
     *
     * @param artistId the ID of the artist
     * @return a list of playlists containing songs by the specified artist
     */
    public List<Playlist> getPlaylistsContainingArtist(int artistId) {
        ensureCacheIsLoaded();
        return playlistsCache.stream()
                .filter(playlist -> playlist.getSongs().stream()
                        .anyMatch(song -> song.getArtistId() == artistId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all playlists with names containing the specified text.
     *
     * @param nameText the text to search for in playlist names
     * @return a list of playlists with names containing the specified text
     */
    public List<Playlist> searchPlaylistsByName(String nameText) {
        ensureCacheIsLoaded();
        return playlistsCache.stream()
                .filter(playlist -> playlist.getName().toLowerCase().contains(nameText.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total duration of a playlist in seconds.
     *
     * @param playlistId the ID of the playlist
     * @return the total duration of the playlist in seconds, or -1 if playlist not found
     */
    public int getPlaylistDuration(int playlistId) {
        Playlist playlist = getPlaylistById(playlistId);
        if (playlist == null) {
            return -1;
        }
        return playlist.getTotalDuration();
    }

    /**
     * Retrieves all playlists that are shared with a specific user.
     *
     * @param userId the ID of the user to find shared playlists for
     * @return a list of playlists shared with the specified user
     */
    public List<Playlist> getPlaylistsSharedWithUser(int userId) {
        // Use the repository directly for specialized queries
        return playlistRepository.findSharedWithUserByID(userId);
    }

    /// ---------------PLAYLIST UPDATE ----------------- ///
    /**
     * Renames a playlist.
     *
     * @param playlistId the ID of the playlist
     * @param newName the new name for the playlist
     * @return true if the playlist was renamed, false otherwise
     */
    public boolean renamePlaylist(int playlistId, String newName) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if new name already exists for this owner
        Optional<Playlist> existingPlaylist = playlistRepository.findByNameAndOwnerID(newName, playlist.getOwnerID());
        if (existingPlaylist.isPresent() && existingPlaylist.get().getPlaylistID() != playlistId) {
            throw new IllegalArgumentException("A playlist with name '" + newName + "' already exists for this user");
        }

        playlist.setName(newName);
        boolean updated = playlistRepository.update(playlist).isPresent();

        if (updated) {
            refreshCache(); // Refresh the cache after successful update
        }

        return updated;
    }

    /// ------------------ PLAYLIST DELETION ----------------- ///

    /**
     * Deletes a playlist.
     *
     * @param playlistId the ID of the playlist
     * @return true if the playlist was deleted, false otherwise
     */
    public boolean deletePlaylist(int playlistId) {
        // First check if the playlist exists
        Playlist playlist = getPlaylistById(playlistId);
        if (playlist == null) {
            return false;
        }

        // Delete the playlist
        boolean result = playlistRepository.deleteById(playlistId);

        // If deletion was successful, refresh cache
        if (result) {
            refreshCache();
        }

        return result;
    }
}