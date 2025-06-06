package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Shuffle navigation strategy - plays songs in random order.
 */
public class ShuffleNavigationStrategy implements PlaylistNavigationStrategy {
    /**
     * The original, unshuffled collection of songs.
     * Maintained for reference and to support reshuffling.
     */
    private LinkedList<Song> originalSongs;

    /**
     * The shuffled collection of songs for playback.
     * This is a randomized version of originalSongs.
     */
    private LinkedList<Song> shuffledSongs;

    /**
     * The current position in the shuffled playlist.
     * Initially set to -1 to indicate playback has not started.
     */
    private int currentIndex = -1;

    /**
     * Random number generator used for shuffling the playlist.
     * Using a dedicated instance allows for consistent shuffling behavior.
     */
    private Random random = new Random();

    /**
     * Initializes the strategy with a playlist of songs.
     * Creates defensive copies of the provided songs collection, shuffles them,
     * and resets the current position.
     *
     * @param songs The songs from the playlist to shuffle and navigate through
     */
    @Override
    public void initialize(LinkedList<Song> songs) {
        this.originalSongs = new LinkedList<>(songs);
        this.shuffledSongs = new LinkedList<>(songs);
        shuffle();
        this.currentIndex = -1;
    }

    /**
     * Shuffles the playlist to create a random playback order.
     *
     * This method creates a temporary ArrayList (which supports efficient random access)
     * from the LinkedList, shuffles it using Collections.shuffle(), and then
     * converts it back to a LinkedList for navigation.
     */
    private void shuffle() {
        List<Song> tempList = new ArrayList<>(shuffledSongs);
        Collections.shuffle(tempList, random);
        shuffledSongs = new LinkedList<>(tempList);
    }

    /**
     * Gets the next song in the shuffled playlist.
     * Advances to the next song in the shuffled sequence until the end is reached.
     *
     * This method behaves similarly to SequentialNavigationStrategy.getNextSong(),
     * but operates on the shuffled song order rather than the original order.
     *
     * @return The next song in the shuffled sequence, or null if the end is reached or playlist is empty
     */
    @Override
    public Song getNextSong() {
        if (shuffledSongs == null || shuffledSongs.isEmpty()) {
            return null;
        }

        if (currentIndex < shuffledSongs.size() - 1) {
            currentIndex++;
            return shuffledSongs.get(currentIndex);
        }

        return null; // End of playlist
    }

    /**
     * Gets the previous song in the shuffled playlist.
     * Moves back to the previous song in the shuffled sequence unless already at the beginning.
     *
     * This allows the user to navigate backward through the shuffled sequence,
     * maintaining the same random order but in reverse.
     *
     * @return The previous song in the shuffled sequence, or null if at the beginning or playlist is empty
     */
    @Override
    public Song getPreviousSong() {
        if (shuffledSongs == null || shuffledSongs.isEmpty() || currentIndex <= 0) {
            return null;
        }

        currentIndex--;
        return shuffledSongs.get(currentIndex);
    }

    /**
     * Gets the current song in the shuffled playlist without changing position.
     *
     * @return The current song in the shuffled sequence, or null if no current song
     *         (empty playlist, playback not started, or position out of bounds)
     */
    @Override
    public Song getCurrentSong() {
        if (shuffledSongs == null || shuffledSongs.isEmpty() || currentIndex < 0 || currentIndex >= shuffledSongs.size()) {
            return null;
        }

        return shuffledSongs.get(currentIndex);
    }

    /**
     * Resets the navigation and reshuffles the playlist.
     *
     * This method not only resets the current position to before the first song,
     * but also generates a new random order for the songs. This ensures that each time
     * the playlist is played from the beginning, it has a different random sequence.
     */
    @Override
    public void reset() {
        shuffle(); // Reshuffle when resetting
        currentIndex = -1;
    }

    /**
     * Checks if there are more songs to play in the shuffled sequence.
     *
     * Returns true if the current position is not at the end of the shuffled playlist,
     * indicating that more songs are available by calling getNextSong().
     *
     * @return true if there are more songs after the current position in the shuffled sequence,
     *         false otherwise
     */
    @Override
    public boolean hasMoreSongs() {
        return shuffledSongs != null && !shuffledSongs.isEmpty() && currentIndex < shuffledSongs.size() - 1;
    }

    /**
     * Gets the name of this navigation strategy.
     *
     * @return The strategy name "Shuffle"
     */
    @Override
    public String getStrategyName() {
        return "Shuffle";
    }
}