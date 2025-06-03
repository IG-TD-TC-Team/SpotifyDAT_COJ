package services.playlistServices;

import factory.RepositoryFactory;
import persistence.interfaces.PlaylistRepositoryInterface;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing songs within playlists.
 * This class provides operations for adding, removing, and manipulating songs
 * within playlist structures while maintaining cache coherence.
 */
public class SongInPlaylistService {

    /**
     * Singleton instance of SongInPlaylistService.
     */
    private static SongInPlaylistService instance;

    /**
     * Repository for accessing playlist data.
     */
    private final PlaylistRepositoryInterface playlistRepository;

    /**
     * Reference to the PlaylistService for coordinated cache management.
     */
    private final PlaylistService playlistService;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the PlaylistRepository instance.
     */
    private SongInPlaylistService() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        this.playlistService = PlaylistService.getInstance();
    }

    /**
     * Returns the single instance of SongInPlaylistService, creating it if it doesn't exist.
     *
     * @return the singleton instance of SongInPlaylistService
     */
    public static synchronized SongInPlaylistService getInstance() {
        if (instance == null) {
            instance = new SongInPlaylistService();
        }
        return instance;
    }

    /**
     * @deprecated Use getInstance() without parameters instead.
     * This method exists only for backward compatibility and will be removed in future versions.
     *
     * @param playlistRepository The playlist repository (ignored)
     * @return The singleton instance of SongInPlaylistService
     */
    @Deprecated
    public static synchronized SongInPlaylistService getInstance(PlaylistRepositoryInterface playlistRepository) {
        return getInstance();
    }

    /**
     * Refreshes the cache to ensure consistency with the repository data.
     * This delegates to the PlaylistService to refresh its cache.
     */
    public void refreshCache() {
        playlistService.refreshCache();
    }

    /// ------------------------SONG IN PLAYLIST RETRIEVAL ----------------- ///
    /**
     * Checks if a specific song is in a playlist.
     *
     * @param playlistId the ID of the playlist
     * @param songId the ID of the song
     * @return true if the song is in the playlist, false otherwise
     */
    public boolean isInPlaylist(int playlistId, int songId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null) {
            return false;
        }
        return playlist.getSongs().stream()
                .anyMatch(song -> song.getSongId() == songId);
    }

    /**
     * Gets the first song in the playlist.
     * @return The first song or null if the playlist is empty.
     */
    public Song getFirstSongInPlaylist(int playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null || playlist.getSongs().isEmpty()) {
            return null;
        }
        return playlist.getSongs().getFirst();
    }

    /**
     * Gets the last song in the playlist.
     * @return The last song or null if the playlist is empty.
     */
    public Song getLastSongInPlaylist(int playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null || playlist.getSongs().isEmpty()) {
            return null;
        }
        return playlist.getSongs().getLast();
    }

    /// ----------------------- SONG IN PLAYLIST CREATION ----------------- ///
    /**
     * Adds a song to the playlist.
     * @param playlistId The ID of the playlist
     * @param song The song to be added
     * @return The updated playlist, or null if the operation failed
     */
    public Playlist addSongToPlaylist(int playlistId, Song song) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null) {
            return null;
        }

        if (isInPlaylist(playlistId, song.getSongId())) {
            return playlist; // Song already in playlist, no need to add again
        }

        LinkedList<Song> songs = playlist.getSongs();
        /// Add the song to the end of the playlist
        songs.add(song);
        playlist.setSongs(songs);

        Optional<Playlist> updated = playlistRepository.update(playlist);
        if (updated.isPresent()) {
            refreshCache(); // Refresh the cache after successful update
            return updated.get();
        }

        return null;
    }

    /**
     * Adds a song to the beginning of the playlist.
     * @param playlistId The ID of the playlist
     * @param song The song to be added at the beginning
     * @return The updated playlist, or null if the operation failed
     */
    public Playlist addSongToBeginningToPlaylist(int playlistId, Song song) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null) {
            return null;
        }

        if (isInPlaylist(playlistId, song.getSongId())) {
            return playlist; // Song already in playlist, no need to add again
        }

        LinkedList<Song> songs = playlist.getSongs();
        /// Add the song to the beginning of the playlist
        songs.addFirst(song);
        playlist.setSongs(songs);

        Optional<Playlist> updated = playlistRepository.update(playlist);
        if (updated.isPresent()) {
            refreshCache(); // Refresh the cache after successful update
            return updated.get();
        }

        return null;
    }

    /// ----------------------- SONG IN PLAYLIST DELETION ----------------- ///

    /**
     * Removes a song from the playlist.
     * @param playlistId The ID of the playlist
     * @param song The song to be removed
     * @return The updated playlist, or null if the operation failed
     */
    public Playlist removeSongFromPlaylist(int playlistId, Song song) {
        System.out.println("DEBUG: Attempting to remove song '" + song.getTitle() + "' (ID: " + song.getSongId() + ") from playlist " + playlistId);

        Playlist playlist = playlistService.getPlaylistById(playlistId);
        if (playlist == null) {
            System.err.println("DEBUG: Playlist not found with ID: " + playlistId);
            return null; // Playlist not found
        }

        System.out.println("DEBUG: Playlist found: '" + playlist.getName() + "' with " + playlist.getSongs().size() + " songs");

        // Check if song is in playlist first
        boolean songFound = false;
        for (Song s : playlist.getSongs()) {
            if (s.getSongId() == song.getSongId()) {
                songFound = true;
                System.out.println("DEBUG: Found song in playlist: '" + s.getTitle() + "' (ID: " + s.getSongId() + ")");
                break;
            }
        }

        if (!songFound) {
            System.err.println("DEBUG: Song not found in playlist");
            return null;
        }

        // Create a new LinkedList and remove by ID comparison, not object reference
        LinkedList<Song> songs = new LinkedList<>(playlist.getSongs());
        boolean removed = songs.removeIf(s -> s.getSongId() == song.getSongId());

        if (removed) {
            System.out.println("DEBUG: Song removed from list, updating playlist");
            playlist.setSongs(songs);

            Optional<Playlist> updated = playlistRepository.update(playlist);
            if (updated.isPresent()) {
                refreshCache(); // Refresh the cache after successful update
                System.out.println("DEBUG: Successfully removed song from playlist");
                return updated.get();
            } else {
                System.err.println("DEBUG: Failed to update playlist in repository");
                return null;
            }
        } else {
            System.err.println("DEBUG: Failed to remove song from list");
            return null;
        }
    }

    /**
     * Removes the first song from the playlist.
     * @param playlistId The ID of the playlist
     * @return The updated playlist, or null if the operation failed
     */
    public Playlist removeFirstSongFromPlaylist(int playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        if (playlist == null || playlist.getSongs().isEmpty()) {
            return null;
        }

        LinkedList<Song> songs = playlist.getSongs();
        songs.removeFirst();
        playlist.setSongs(songs);

        Optional<Playlist> updated = playlistRepository.update(playlist);
        if (updated.isPresent()) {
            refreshCache(); // Refresh the cache after successful update
            return updated.get();
        }

        return null;
    }

    /**
     * Removes the last song from the playlist.
     * @param playlistId The ID of the playlist
     * @return The updated playlist, or null if the operation failed
     */
    public Playlist removeLastSongFromPlaylist(int playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        if (playlist == null || playlist.getSongs().isEmpty()) {
            return null;
        }

        LinkedList<Song> songs = playlist.getSongs();
        songs.removeLast();
        playlist.setSongs(songs);

        Optional<Playlist> updated = playlistRepository.update(playlist);
        if (updated.isPresent()) {
            refreshCache(); // Refresh the cache after successful update
            return updated.get();
        }

        return null;
    }

    /// ----------------------- SONG IN PLAYLIST UPDATE ----------------- ///

    /**
     * Moves a song to the next position in the playlist.
     * @param playlistId The ID of the playlist
     * @param song The song to be moved
     * @return true if the song was moved, false otherwise
     */
    public boolean moveNext(int playlistId, Song song) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null) {
            return false;
        }

        LinkedList<Song> songs = new LinkedList<>(playlist.getSongs());
        int index = songs.indexOf(song);

        if (index < 0 || index >= songs.size() - 1) {
            return false; // Song not found or already at the end
        }

        Collections.swap(songs, index, index + 1);
        playlist.setSongs(songs);

        boolean updated = playlistRepository.update(playlist).isPresent();
        if (updated) {
            refreshCache(); // Refresh the cache after successful update
        }

        return updated;
    }

    /**
     * Moves a song to the previous position in the playlist.
     * @param playlistId The ID of the playlist
     * @param song The song to be moved
     * @return true if the song was moved, false otherwise
     */
    public boolean movePrevious(int playlistId, Song song) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);

        if (playlist == null) {
            return false;
        }

        LinkedList<Song> songs = new LinkedList<>(playlist.getSongs());
        int index = songs.indexOf(song);

        if (index <= 0) {
            return false; // Song not found or already at the beginning
        }

        Collections.swap(songs, index, index - 1);
        playlist.setSongs(songs);

        boolean updated = playlistRepository.update(playlist).isPresent();
        if (updated) {
            refreshCache(); // Refresh the cache after successful update
        }

        return updated;
    }
}