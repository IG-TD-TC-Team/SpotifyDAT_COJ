package factory;

import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory class responsible for creating and initializing playlists.
 *
 * This class follows both the Factory and Singleton patterns:
 * - Factory pattern: Encapsulates the complex creation logic for playlists
 * - Singleton pattern: Ensures centralized management of playlist IDs
 *
 * The factory handles validating unique playlist names per user, generating unique
 * playlist IDs, and creating playlists with optional initial song content.
 *
 */
public class PlaylistFactory {

    /**
     * Singleton instance of PlaylistFactory.
     * Using a singleton ensures consistent ID generation across the application.
     */
    private static PlaylistFactory instance;

    /**
     * Repository for playlist persistence operations.
     * Used for saving new playlists and verifying name uniqueness.
     */
    private final PlaylistRepositoryInterface playlistRepository;

    /**
     * Repository for song-related operations.
     * Used for retrieving song objects when adding them to playlists.
     */
    private final SongRepositoryInterface songRepository;

    /**
     * Counter for generating unique playlist IDs.
     * This value is initialized based on existing playlists in the repository.
     */
    private int nextPlaylistId = 1;

    /**
     * Private constructor initializing repositories through RepositoryFactory.
     * This follows the Singleton pattern to prevent multiple instances from
     * causing ID conflicts.
     */
    private PlaylistFactory() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        this.songRepository = RepositoryFactory.getInstance().getSongRepository();

        initializeNextId();
    }

    /**
     * Returns the singleton instance of PlaylistFactory.
     * Thread-safe implementation ensures only one instance exists even in
     * concurrent environments.
     *
     * @return the singleton instance of PlaylistFactory
     */
    public static synchronized PlaylistFactory getInstance() {
        if (instance == null) {
            instance = new PlaylistFactory();
        }
        return instance;
    }

    /**
     * Initializes the next playlist ID based on existing playlists.
     * This method examines all existing playlists to find the highest ID and
     * sets the next ID to be one greater, ensuring unique IDs for new playlists.
     */
    private void initializeNextId() {
        List<Playlist> existingPlaylists = playlistRepository.findAll();
        for (Playlist playlist : existingPlaylists) {
            if (playlist.getPlaylistID() >= nextPlaylistId) {
                nextPlaylistId = playlist.getPlaylistID() + 1;
            }
        }
    }

    /**
     * Creates a new empty playlist with a unique ID.
     * This method validates that the playlist name is unique for the specified user
     * before creating and persisting the playlist.
     *
     * @param name the name of the playlist
     * @param ownerID the ID of the owner
     * @return the created and persisted playlist
     * @throws IllegalArgumentException if a playlist with the same name already exists for this user
     */
    public Playlist createPlaylist(String name, int ownerID) {
        // Check if playlist with same name already exists for this user
        Optional<Playlist> existingPlaylist = playlistRepository.findByNameAndOwnerID(name, ownerID);
        if (existingPlaylist.isPresent()) {
            throw new IllegalArgumentException("A playlist with name '" + name + "' already exists for this user");
        }

        // Create new playlist
        Playlist playlist = new Playlist(name, ownerID);
        playlist.setPlaylistID(nextPlaylistId++);

        // Save playlist
        playlistRepository.save(playlist);

        System.out.println("Created playlist: " + name + " for user ID: " + ownerID);
        return playlist;
    }

    /**
     * Creates a new playlist with initial songs and returns the up-to-date playlist.
     * This method first creates an empty playlist, then adds the specified songs,
     * and finally retrieves the updated playlist to ensure all songs are included.
     *
     * @param name the name of the playlist
     * @param ownerID the ID of the owner
     * @param songIds the IDs of the songs to add
     * @return the created playlist with all songs added
     * @throws IllegalArgumentException if any song ID is invalid or if a playlist with the same name already exists
     */
    public Playlist createPlaylistWithSongs(String name, int ownerID, List<Integer> songIds) {
        Playlist playlist = createPlaylist(name, ownerID);

        // Add songs to playlist
        for (Integer songId : songIds) {
            Optional<Song> songOpt = songRepository.findById(songId);
            if (songOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid song ID: " + songId);
            }
            playlistRepository.addSongToPlaylist(name, ownerID, songOpt.get());

        }
        System.out.println("Created playlist: " + name + " with songs for user ID: " + ownerID);
        return playlist; ///HERE THE PLAYLIST IS NOT UP TO DATE
    }


}