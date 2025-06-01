package services.playbackServices;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import services.playlistServices.PlaylistService;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing music playback across the application.
 * Implements the Singleton pattern and manages playlist navigation strategies.
 */
public class PlaybackService {
    // Singleton instance
    private static PlaybackService instance;

    // Dependencies
    private final PlaylistService playlistService;

    // Map of active navigators by user ID
    private final ConcurrentHashMap<Integer, PlaylistNavigator> activeNavigators = new ConcurrentHashMap<>();

    /**
     * Private constructor for Singleton pattern.
     */
    private PlaybackService() {
        this.playlistService = PlaylistService.getInstance();
    }

    /**
     * Gets the singleton instance of PlaybackService.
     * @return The singleton instance
     */
    public static synchronized PlaybackService getInstance() {
        if (instance == null) {
            instance = new PlaybackService();
        }
        return instance;
    }

    /**
     * Starts playback of a playlist for a user.
     * @param playlistId The ID of the playlist to play
     * @param userId The ID of the user
     * @return The first song to play, or null if the playlist is empty or not found
     */
    public Song startPlaylist(int playlistId, int userId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        if (playlist == null || playlist.getSongs().isEmpty()) {
            return null;
        }

        // Create a navigator for this user and playlist
        PlaylistNavigator navigator = new PlaylistNavigator();
        navigator.setPlaylist(playlist);

        // Store the navigator
        activeNavigators.put(userId, navigator);

        // Get the first song
        return navigator.getNextSong();
    }

    /**
     * Gets the next song in the playlist for a user.
     * @param userId The ID of the user
     * @return The next song, or null if no more songs or no active playlist
     */
    public Song getNextSong(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return null;
        }

        return navigator.getNextSong();
    }

    /**
     * Gets the previous song in the playlist for a user.
     * @param userId The ID of the user
     * @return The previous song, or null if at the beginning or no active playlist
     */
    public Song getPreviousSong(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return null;
        }

        return navigator.getPreviousSong();
    }

    /**
     * Gets the current song in the playlist for a user.
     * @param userId The ID of the user
     * @return The current song, or null if no current song or no active playlist
     */
    public Song getCurrentSong(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return null;
        }

        return navigator.getCurrentSong();
    }

    /**
     * Sets the playback mode to sequential for a user.
     * @param userId The ID of the user
     */
    public void setSequentialMode(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator != null) {
            navigator.useSequentialStrategy();
        }
    }

    /**
     * Sets the playback mode to shuffle for a user.
     * @param userId The ID of the user
     */
    public void setShuffleMode(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator != null) {
            navigator.useShuffleStrategy();
        }
    }

    /**
     * Sets the playback mode to repeat one for a user.
     * @param userId The ID of the user
     */
    public void setRepeatOneMode(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator != null) {
            navigator.useRepeatOneStrategy();
        }
    }

    /**
     * Sets the playback mode to repeat all for a user.
     * @param userId The ID of the user
     */
    public void setRepeatAllMode(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator != null) {
            navigator.useRepeatAllStrategy();
        }
    }

    /**
     * Gets the current playback mode for a user.
     * @param userId The ID of the user
     * @return The name of the current playback mode, or null if no active playlist
     */
    public String getPlaybackMode(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return null;
        }

        return navigator.getCurrentStrategyName();
    }

    /**
     * Gets the current playlist for a user.
     * @param userId The ID of the user
     * @return The current playlist, or null if no active playlist
     */
    public Playlist getCurrentPlaylist(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return null;
        }

        return navigator.getPlaylist();
    }

    /**
     * Stops playback for a user.
     * @param userId The ID of the user
     */
    public void stopPlayback(int userId) {
        activeNavigators.remove(userId);
    }

    /**
     * Checks if a user has an active playlist.
     * @param userId The ID of the user
     * @return true if the user has an active playlist, false otherwise
     */
    public boolean hasActivePlaylist(int userId) {
        return activeNavigators.containsKey(userId);
    }

    /**
     * Gets the remaining songs in the playlist for a user.
     * @param userId The ID of the user
     * @return The number of remaining songs, or -1 if no active playlist
     */
    public int getRemainingCount(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return -1;
        }

        int count = 0;
        Song current = navigator.getCurrentSong();

        if (current == null) {
            return -1;
        }

        LinkedList<Song> songs = navigator.getPlaylist().getSongs();
        int currentIndex = songs.indexOf(current);

        if (currentIndex >= 0) {
            count = songs.size() - currentIndex - 1;
        }

        return count;
    }

    /**
     * Peeks at the next song without advancing the position.
     * @param userId The ID of the user
     * @return The next song, or null if no more songs or no active playlist
     */
    public Song peekNextSong(int userId) {
        PlaylistNavigator navigator = activeNavigators.get(userId);
        if (navigator == null) {
            return null;
        }

        // Get current song position
        Song currentSong = navigator.getCurrentSong();

        // Get next song
        Song nextSong = navigator.getNextSong();

        // If we advanced, go back to the current song
        if (nextSong != null && currentSong != null) {
            navigator.reset();

            // Advance to current song
            Song tempSong = null;
            do {
                tempSong = navigator.getNextSong();
            } while (tempSong != null && !tempSong.equals(currentSong));
        }

        return nextSong;
    }
}