package services.playbackServices;

import songsAndArtists.Song;
import java.util.LinkedList;

/**
 * Repeat All navigation strategy - plays songs in order but loops back to the beginning when done.
 */
public class RepeatAllNavigationStrategy implements PlaylistNavigationStrategy {
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

        currentIndex = (currentIndex + 1) % songs.size();
        return songs.get(currentIndex);
    }

    @Override
    public Song getPreviousSong() {
        if (songs == null || songs.isEmpty()) {
            return null;
        }

        currentIndex = (currentIndex - 1 + songs.size()) % songs.size();
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
        // Always has more songs in repeat all mode
        return songs != null && !songs.isEmpty();
    }

    @Override
    public String getStrategyName() {
        return "Repeat All";
    }
}