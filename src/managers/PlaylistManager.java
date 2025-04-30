package managers;

import persistence.PlaylistRepository;
import songsAndArtists.Genre;
import songsOrganisation.Playlist;
import user.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages playlist-related operations such as retrieving playlists
 * and providing filtering functionality.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class PlaylistManager {

    /**
     * Singleton instance of PlaylistManager.
     */
    private static PlaylistManager instance;

    /**
     * Repository for accessing playlist data.
     */
    private final PlaylistRepository playlistRepository;

    /**
     * Cache of all playlists for faster retrieval.
     */
    private List<Playlist> playlists;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the PlaylistRepository instance and loads all playlists.
     */
    private PlaylistManager() {
        this.playlistRepository = PlaylistRepository.getInstance();
        refreshCache();
    }

    /**
     * Returns the single instance of PlaylistManager, creating it if it doesn't exist.
     *
     * @return the singleton instance of PlaylistManager
     */
    public static synchronized PlaylistManager getInstance() {
        if (instance == null) {
            instance = new PlaylistManager();
        }
        return instance;
    }

    /**
     * Refreshes the in-memory cache with the latest data from repositories.
     * Call this after playlists are created, updated, or deleted.
     */
    public void refreshCache() {
        playlists = playlistRepository.findAll();
    }

    /**
     * Retrieves all playlists.
     *
     * @return a list of all playlists
     */
    public List<Playlist> getAllPlaylists() {
        refreshCache();
        return playlists;
    }

    /**
     * Retrieves a playlist by its ID.
     *
     * @param playlistId the ID of the playlist
     * @return the playlist with the specified ID, or null if not found
     */
    public Playlist getPlaylistById(int playlistId) {
        refreshCache();
        return playlists.stream()
                .filter(playlist -> playlist.getPlaylistID() == playlistId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a playlist by its name and owner ID.
     *
     * @param name the name of the playlist
     * @param ownerID the ID of the owner
     * @return the playlist with the specified name and owner ID, or null if not found
     */
    public Playlist getPlaylistByNameAndOwner(String name, int ownerID) {
        refreshCache();
        return playlists.stream()
                .filter(playlist -> playlist.getName().equals(name) && playlist.getOwnerID() == ownerID)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves all playlists owned by a specific user.
     *
     * @param ownerID the ID of the owner
     * @return a list of playlists owned by the specified user
     */
    public List<Playlist> getPlaylistsByOwner(int ownerID) {
        refreshCache();
        return playlists.stream()
                .filter(playlist -> playlist.getOwnerID() == ownerID)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all playlists owned by a specific user by username.
     *
     * @param username the username of the owner
     * @param userManager the UserManager instance to resolve username to ID
     * @return a list of playlists owned by the specified user
     */
    public List<Playlist> getPlaylistsByOwnerUsername(String username, UserManager userManager) {

        User user = userManager.getUserByUsername(username);
        return getPlaylistsByOwner(user.getUserID());
    }

    /**
     * Retrieves all playlists shared with a specific user.
     *
     * @param userID the ID of the user
     * @return a list of playlists shared with the specified user
     */
    public List<Playlist> getPlaylistsSharedWithUser(int userID) {
        refreshCache();
        return playlists.stream()
                .filter(playlist -> playlist.getSharedWith().contains(userID))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all playlists that contain a specific song.
     *
     * @param songId the ID of the song
     * @return a list of playlists containing the specified song
     */
    public List<Playlist> getPlaylistsContainingSong(int songId) {
        refreshCache();
        return playlists.stream()
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
        refreshCache();
        return playlists.stream()
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
        refreshCache();
        return playlists.stream()
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
        refreshCache();
        return playlists.stream()
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
     * Checks if a specific song is in a playlist.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song
     * @return true if the song is in the playlist, false otherwise
     */
    public boolean isInPlaylist(int playlistId, int songId) {
        Playlist playlist = getPlaylistById(playlistId);
        if (playlist == null) {
            return false;
        }
        return playlist.getSongs().stream()
                .anyMatch(song -> song.getSongId() == songId);
    }
}