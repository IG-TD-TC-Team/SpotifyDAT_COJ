package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;

/**
 * Repeat One navigation strategy - plays the same song repeatedly.
 */
public class RepeatOneNavigationStrategy implements PlaylistNavigationStrategy {
    private LinkedList<Song> songs;
    private int currentIndex = -1;

    @Override
    public void initialize(LinkedList<Song> songs) {
        this.songs = new LinkedList<>(songs);
        this.currentIndex = -1;
    }

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

    @Override
    public Song getPreviousSong() {
        // In repeat one mode, previous behaves the same as current
        return getCurrentSong();
    }

    @Override
    public Song getCurrentSong() {
        if (songs == null || songs.isEmpty() || currentIndex < 0 || currentIndex >= songs.size()) {
            return null;
        }

        return songs.get(currentIndex);
    }

    @Override
    public void reset() {
        currentIndex = -1;
    }

    @Override
    public boolean hasMoreSongs() {
        // Always has more songs in repeat one mode
        return songs != null && !songs.isEmpty() && currentIndex >= 0;
    }

    @Override
    public String getStrategyName() {
        return "Repeat One";
    }
}