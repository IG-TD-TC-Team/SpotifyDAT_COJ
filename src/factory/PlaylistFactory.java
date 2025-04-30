package factory;

import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory class responsible for creating and modifying playlists.
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
        this.playlistRepository = RepositoryFactory.getPlaylistRepository();
        this.songRepository = RepositoryFactory.getSongRepository();
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
     * @return the created playlist
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
            playlist.addSong(songOpt.get());
        }

        // Update playlist
        playlistRepository.update(playlist);

        return playlist;
    }

    /**
     * Adds a song to a playlist.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song to add
     * @return true if the song was added, false otherwise
     */
    public boolean addSongToPlaylist(int playlistId, int songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Optional<Song> songOpt = songRepository.findById(songId);
        if (songOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        Song song = songOpt.get();

        // Check if song already exists in playlist
        boolean songExists = playlist.getSongs().stream()
                .anyMatch(s -> s.getSongId() == songId);

        if (songExists) {
            return false; // Song already in playlist
        }

        playlist.addSong(song);
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Adds a song to the beginning of a playlist.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song to add
     * @return true if the song was added, false otherwise
     */
    public boolean addSongToPlaylistBeginning(int playlistId, int songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Optional<Song> songOpt = songRepository.findById(songId);
        if (songOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        Song song = songOpt.get();

        // Check if song already exists in playlist
        boolean songExists = playlist.getSongs().stream()
                .anyMatch(s -> s.getSongId() == songId);

        if (songExists) {
            return false; // Song already in playlist
        }

        playlist.addSongToBeginning(song);
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Removes a song from a playlist.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song to remove
     * @return true if the song was removed, false otherwise
     */
    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Find the song in the playlist
        Optional<Song> songToRemove = playlist.getSongs().stream()
                .filter(song -> song.getSongId() == songId)
                .findFirst();

        if (songToRemove.isPresent()) {
            playlist.removeSong(songToRemove.get());
            playlistRepository.update(playlist);
            return true;
        }

        return false;
    }

    /**
     * Moves a song forward in the playlist order.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song to move
     * @return true if the song was moved, false otherwise
     */
    public boolean moveSongForward(int playlistId, int songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Find the song in the playlist
        Optional<Song> songToMove = playlist.getSongs().stream()
                .filter(song -> song.getSongId() == songId)
                .findFirst();

        if (songToMove.isPresent()) {
            playlist.moveNext(songToMove.get());
            playlistRepository.update(playlist);
            return true;
        }

        return false;
    }

    /**
     * Moves a song backward in the playlist order.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song to move
     * @return true if the song was moved, false otherwise
     */
    public boolean moveSongBackward(int playlistId, int songId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Find the song in the playlist
        Optional<Song> songToMove = playlist.getSongs().stream()
                .filter(song -> song.getSongId() == songId)
                .findFirst();

        if (songToMove.isPresent()) {
            playlist.movePrevious(songToMove.get());
            playlistRepository.update(playlist);
            return true;
        }

        return false;
    }

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
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Shares a playlist with another user.
     *
     * @param playlistId the ID of the playlist
     * @param userIdToShareWith the ID of the user to share with
     * @return true if the playlist was shared, false otherwise
     */
    public boolean sharePlaylist(int playlistId, int userIdToShareWith) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if already shared with user
        if (playlist.isSharedWith(userIdToShareWith)) {
            return true; // Already shared
        }

        playlist.addUserToShareWith(userIdToShareWith);
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Unshares a playlist with a user.
     *
     * @param playlistId the ID of the playlist
     * @param userId the ID of the user to unshare with
     * @return true if the playlist was unshared, false otherwise
     */
    public boolean unsharePlaylist(int playlistId, int userId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if shared with user
        if (!playlist.isSharedWith(userId)) {
            return false; // Not shared with this user
        }

        playlist.removeSharing(userId);
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Deletes a playlist.
     *
     * @param playlistId the ID of the playlist
     * @return true if the playlist was deleted, false otherwise
     */
    public boolean deletePlaylist(int playlistId) {
        return playlistRepository.deleteById(playlistId);
    }

    /**
     * Creates a copy of an existing playlist for a user.
     *
     * @param playlistId the ID of the playlist to copy
     * @param newName the name for the new playlist
     * @param newOwnerId the ID of the new owner
     * @return the newly created playlist copy
     */
    public Playlist copyPlaylist(int playlistId, String newName, int newOwnerId) {
        Optional<Playlist> originalPlaylistOpt = playlistRepository.findById(playlistId);
        if (originalPlaylistOpt.isEmpty()) {
            throw new IllegalArgumentException("Playlist with ID " + playlistId + " not found");
        }

        Playlist originalPlaylist = originalPlaylistOpt.get();

        // Create new playlist
        Playlist newPlaylist = createPlaylist(newName, newOwnerId);

        // Copy all songs
        for (Song song : originalPlaylist.getSongs()) {
            newPlaylist.addSong(song);
        }

        // Update playlist
        playlistRepository.update(newPlaylist);

        return newPlaylist;
    }

    /**
     * Merges multiple playlists into a new one.
     *
     * @param playlistIds the IDs of the playlists to merge
     * @param newName the name for the merged playlist
     * @param ownerId the ID of the owner for the merged playlist
     * @return the newly created merged playlist
     */
    public Playlist mergePlaylists(List<Integer> playlistIds, String newName, int ownerId) {
        // Create new playlist
        Playlist mergedPlaylist = createPlaylist(newName, ownerId);

        // Add songs from all source playlists
        for (Integer playlistId : playlistIds) {
            Optional<Playlist> sourcePlaylistOpt = playlistRepository.findById(playlistId);
            if (sourcePlaylistOpt.isPresent()) {
                Playlist sourcePlaylist = sourcePlaylistOpt.get();
                for (Song song : sourcePlaylist.getSongs()) {
                    // Skip duplicates by checking if already in merged playlist
                    boolean alreadyExists = mergedPlaylist.getSongs().stream()
                            .anyMatch(s -> s.getSongId() == song.getSongId());

                    if (!alreadyExists) {
                        mergedPlaylist.addSong(song);
                    }
                }
            }
        }

        // Update playlist
        playlistRepository.update(mergedPlaylist);

        return mergedPlaylist;
    }
}