package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * Sequential navigation strategy - plays songs in order from first to last.
 */
public class SequentialNavigationStrategy implements PlaylistNavigationStrategy {
    /**
     * The collection of songs to navigate through in sequential order.
     * A defensive copy is made during initialization to prevent external modifications.
     */
    private LinkedList<Song> songs;

    /**
     * The current position in the playlist.
     * Initially set to -1 to indicate playback has not started.
     * Values from 0 to (songs.size()-1) represent positions in the playlist.
     */
    private int currentIndex = -1;

    /**
     * Initializes the strategy with a playlist of songs.
     * Creates a defensive copy of the provided songs collection to prevent external modifications.
     *
     * @param songs The songs from the playlist to navigate through sequentially
     */
    @Override
    public void initialize(LinkedList<Song> songs) {
        this.songs = new LinkedList<>(songs); // Create a copy to avoid modifying the original
        this.currentIndex = -1;
    }

    /**
     * Gets the next song in the playlist.
     * Advances to the next song in sequence until the end of the playlist is reached.
     *
     * This method advances the current position by one and returns the song at that position.
     * If the end of the playlist is reached, it returns null to indicate no more songs are available.
     *
     * @return The next song in sequence, or null if the end of playlist is reached or playlist is empty
     */
    @Override
    public Song getNextSong() {
        if (songs == null || songs.isEmpty()) {
            return null;
        }

        if (currentIndex < songs.size() - 1) {
            currentIndex++;
            return songs.get(currentIndex);
        }

        return null; // End of playlist
    }

    /**
     * Gets the previous song in the playlist.
     * Moves back to the previous song in sequence unless already at the beginning.
     *
     * This method decrements the current position by one and returns the song at that position.
     * If already at the beginning of the playlist, it returns null to indicate
     * there are no previous songs available.
     *
     * @return The previous song in sequence, or null if at the beginning of playlist or playlist is empty
     */
    @Override
    public Song getPreviousSong() {
        if (songs == null || songs.isEmpty() || currentIndex <= 0) {
            return null;
        }

        currentIndex--;
        return songs.get(currentIndex);
    }

    /**
     * Gets the current song in the playlist without changing position.
     *
     * Returns the song at the current position in the playlist without
     * advancing or rewinding the position.
     *
     * @return The current song, or null if no current song (empty playlist, playback not started,
     *         or position out of bounds)
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
     *
     * After calling this method, getCurrentSong() will return null and
     * getNextSong() will return the first song in the playlist, effectively
     * restarting playback from the beginning.
     */
    @Override
    public void reset() {
        currentIndex = -1;
    }

    /**
     * Checks if there are more songs to play in the sequence.
     *
     * This method returns true if the current position is not at the end of the playlist,
     * indicating that more songs are available by calling getNextSong().
     *
     * @return true if there are more songs after the current position, false otherwise
     */
    @Override
    public boolean hasMoreSongs() {
        return songs != null && !songs.isEmpty() && currentIndex < songs.size() - 1;
    }

    /**
     * Gets the name of this navigation strategy.
     *
     * @return The strategy name "Sequential"
     */
    @Override
    public String getStrategyName() {
        return "Sequential";
    }
}