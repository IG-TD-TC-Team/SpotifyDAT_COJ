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
    private LinkedList<Song> originalSongs;
    private LinkedList<Song> shuffledSongs;
    private int currentIndex = -1;
    private Random random = new Random();

    @Override
    public void initialize(LinkedList<Song> songs) {
        this.originalSongs = new LinkedList<>(songs);
        this.shuffledSongs = new LinkedList<>(songs);
        shuffle();
        this.currentIndex = -1;
    }

    /**
     * Shuffles the playlist.
     */
    private void shuffle() {
        List<Song> tempList = new ArrayList<>(shuffledSongs);
        Collections.shuffle(tempList, random);
        shuffledSongs = new LinkedList<>(tempList);
    }

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

    @Override
    public Song getPreviousSong() {
        if (shuffledSongs == null || shuffledSongs.isEmpty() || currentIndex <= 0) {
            return null;
        }

        currentIndex--;
        return shuffledSongs.get(currentIndex);
    }

    @Override
    public Song getCurrentSong() {
        if (shuffledSongs == null || shuffledSongs.isEmpty() || currentIndex < 0 || currentIndex >= shuffledSongs.size()) {
            return null;
        }

        return shuffledSongs.get(currentIndex);
    }

    @Override
    public void reset() {
        shuffle(); // Reshuffle when resetting
        currentIndex = -1;
    }

    @Override
    public boolean hasMoreSongs() {
        return shuffledSongs != null && !shuffledSongs.isEmpty() && currentIndex < shuffledSongs.size() - 1;
    }

    @Override
    public String getStrategyName() {
        return "Shuffle";
    }
}