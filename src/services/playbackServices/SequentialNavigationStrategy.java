package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * Sequential navigation strategy - plays songs in order from first to last.
 */
public class SequentialNavigationStrategy implements PlaylistNavigationStrategy {
    private LinkedList<Song> songs;
    private int currentIndex = -1;

    @Override
    public void initialize(LinkedList<Song> songs) {
        this.songs = new LinkedList<>(songs); // Create a copy to avoid modifying the original
        this.currentIndex = -1;
    }

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

    @Override
    public Song getPreviousSong() {
        if (songs == null || songs.isEmpty() || currentIndex <= 0) {
            return null;
        }

        currentIndex--;
        return songs.get(currentIndex);
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
        return songs != null && !songs.isEmpty() && currentIndex < songs.size() - 1;
    }

    @Override
    public String getStrategyName() {
        return "Sequential";
    }
}