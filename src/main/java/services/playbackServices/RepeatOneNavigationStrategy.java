package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;

/**
 * Repeat One navigation strategy - plays the same song repeatedly.
 */
public class RepeatOneNavigationStrategy implements PlaylistNavigationStrategy {
    /**
     * The collection of songs available in the playlist.
     * A defensive copy is made during initialization to prevent external modifications.
     * While the collection contains all songs, this strategy focuses on repeating just one.
     */
    private LinkedList<Song> songs;

    /**
     * The current position in the playlist.
     * Initially set to -1 to indicate playback has not started.
     * Once playback begins, this index remains fixed to repeat the same song.
     */
    private int currentIndex = -1;

    /**
     * Initializes the strategy with a playlist of songs.
     * Creates a defensive copy of the provided songs collection to prevent external modifications.
     * Resets the current index to -1 to indicate playback has not started.
     *
     * @param songs The songs from the playlist to navigate through
     */
    @Override
    public void initialize(LinkedList<Song> songs) {
        this.songs = new LinkedList<>(songs);
        this.currentIndex = -1;
    }

    /**
     * Gets the next song in the playlist, with repeat-one behavior.
     *
     * On the first call after initialization or reset, this advances to the first song.
     * On subsequent calls, it returns the same song repeatedly, implementing the repeat-one behavior.
     *
     * @return The current song to repeat, or the first song if playback just started,
     *         or null if the playlist is empty
     */
    @Override
    public Song getNextSong() {
        if (songs == null || songs.isEmpty()) {
            return null;
        }

        if (currentIndex == -1) {
            // First play - move to first song
            currentIndex = 0;
            return songs.get(currentIndex);
        }

        // Return the same song again (repeat current)
        return songs.get(currentIndex);
    }

    /**
     * Gets the previous song, which in repeat-one mode is the same as the current song.
     *
     * In repeat-one mode, the concept of "previous" doesn't apply since we're repeating
     * a single track. Therefore, this method behaves the same as getCurrentSong().
     *
     * @return The current song, or null if no current song (empty playlist or playback not started)
     */
    @Override
    public Song getPreviousSong() {
        // In repeat one mode, previous behaves the same as current
        return getCurrentSong();
    }

    /**
     * Gets the current song in the playlist without changing position.
     *
     * @return The current song, or null if no current song (empty playlist or playback not started)
     */
    @Override
    public Song getCurrentSong() {
        if (songs == null || songs.isEmpty() || currentIndex < 0 || currentIndex >= songs.size()) {
            return null;
        }

        return songs.get(currentIndex);
    }

    /**
     * Resets the navigation to the beginning state.
     * After calling this method, getCurrentSong() will return null and
     * getNextSong() will select the first song in the playlist.
     */
    @Override
    public void reset() {
        currentIndex = -1;
    }

    /**
     * Checks if there are more songs to play.
     *
     * In repeat-one mode, once a song is selected (currentIndex >= 0),
     * this always returns true for non-empty playlists since the same song
     * can be played infinitely.
     *
     * @return true if a song is currently selected and the playlist is not empty,
     *         false otherwise
     */
    @Override
    public boolean hasMoreSongs() {
        // Always has more songs in repeat one mode
        return songs != null && !songs.isEmpty() && currentIndex >= 0;
    }

    /**
     * Gets the name of this navigation strategy.
     *
     * @return The strategy name "Repeat One"
     */
    @Override
    public String getStrategyName() {
        return "Repeat One";
    }
}