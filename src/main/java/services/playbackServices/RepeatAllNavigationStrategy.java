package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;

/**
 * Repeat All navigation strategy - plays songs in order but loops back to the beginning when done.
 *
 * This class implements the Strategy pattern as a concrete strategy for playlist navigation.
 * It provides a repeating playback experience where after the last song completes, playback
 * continues from the first song in the playlist, creating an endless loop.
 *
 * The strategy maintains its position within the playlist using a zero-based index,
 * with -1 indicating that playback has not yet begun.
 */
public class RepeatAllNavigationStrategy implements PlaylistNavigationStrategy {

    /**
     * The collection of songs to navigate through.
     * A defensive copy is made during initialization to prevent external modifications.
     */
    private LinkedList<Song> songs;

    /**
     * The current position in the playlist.
     * Initially set to -1 to indicate playback has not started.
     * Values from 0 to (songs.size()-1) represent the current song position.
     */
    private int currentIndex = -1;

    /**
     * Initializes the strategy with a playlist of songs.
     * Creates a defensive copy of the provided songs collection to prevent external modifications.
     *
     * @param songs The songs from the playlist to navigate through
     */
    @Override
    public void initialize(LinkedList<Song> songs) {
        this.songs = new LinkedList<>(songs);
        this.currentIndex = -1;
    }

    /**
     * Gets the next song in the playlist.
     * In repeat all mode, when reaching the end of the playlist, this method
     * loops back to the beginning, ensuring continuous playback.
     *
     * @return The next song in the playlist, or null if the playlist is empty
     */
    @Override
    public Song getNextSong() {
        if (songs == null || songs.isEmpty()) {
            return null;
        }

        currentIndex = (currentIndex + 1) % songs.size();
        return songs.get(currentIndex);
    }

    /**
     * Gets the previous song in the playlist.
     * In repeat all mode, when at the beginning of the playlist, this method
     * loops around to the end, allowing for bidirectional infinite navigation.
     *
     * @return The previous song in the playlist, or null if the playlist is empty
     */
    @Override
    public Song getPreviousSong() {
        if (songs == null || songs.isEmpty()) {
            return null;
        }

        currentIndex = (currentIndex - 1 + songs.size()) % songs.size();
        return songs.get(currentIndex);
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
     * getNextSong() will return the first song in the playlist.
     */
    @Override
    public void reset() {
        currentIndex = -1;
    }

    /**
     * Checks if there are more songs to play.
     * In repeat all mode, this always returns true for non-empty playlists
     * since playback can continue infinitely by looping through songs.
     *
     * @return true if the playlist contains songs, false otherwise
     */
    @Override
    public boolean hasMoreSongs() {
        // Always has more songs in repeat all mode
        return songs != null && !songs.isEmpty();
    }

    /**
     * Gets the name of this navigation strategy.
     *
     * @return The strategy name "Repeat All"
     */
    @Override
    public String getStrategyName() {
        return "Repeat All";
    }
}