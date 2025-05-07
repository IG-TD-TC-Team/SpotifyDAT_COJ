package factory;

import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory class responsible for creating playlists.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class PlaylistFactory {

    /**
     * Singleton instance of PlaylistFactory.
     */
    private static PlaylistFactory instance;

    /**
     * Repository for playlist persistence operations.
     */
    private final PlaylistRepositoryInterface playlistRepository;

    /**
     * Repository for song-related operations.
     */
    private final SongRepositoryInterface songRepository;

    /**
     * Counter for generating unique playlist IDs.
     */
    private int nextPlaylistId = 1;

    /**
     * Private constructor initializing repositories through RepositoryFactory.
     */
    private PlaylistFactory() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        this.songRepository = RepositoryFactory.getInstance().getSongRepository();

        initializeNextId();
    }

    /**
     * Returns the singleton instance of PlaylistFactory.
     *
     * @return the singleton instance
     */
    public static synchronized PlaylistFactory getInstance() {
        if (instance == null) {
            instance = new PlaylistFactory();
        }
        return instance;
    }

    /**
     * Initializes the next playlist ID based on existing playlists.
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
     * Creates a new empty playlist.
     *
     * @param name the name of the playlist
     * @param ownerID the ID of the owner
     * @return the created playlist
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
     * Creates a new playlist with initial songs.
     *
     * @param name the name of the playlist
     * @param ownerID the ID of the owner
     * @param songIds the IDs of the songs to add
     * @throws IllegalArgumentException if any song ID is invalid
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