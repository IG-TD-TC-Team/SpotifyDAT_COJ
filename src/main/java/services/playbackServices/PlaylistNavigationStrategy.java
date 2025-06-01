package services.playbackServices;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;

/**
 * Interface defining a strategy for navigating through a playlist.
 * Different implementations can provide different navigation patterns (sequential, shuffle, etc.)
 */
public interface PlaylistNavigationStrategy {
    /**
     * Initializes the strategy with a playlist of songs.
     * @param songs The songs from the playlist
     */
    void initialize(LinkedList<Song> songs);

    /**
     * Gets the next song in the playlist according to the strategy.
     * @return The next song, or null if no more songs
     */
    Song getNextSong();

    /**
     * Gets the previous song in the playlist according to the strategy.
     * @return The previous song, or null if at the beginning
     */
    Song getPreviousSong();

    /**
     * Gets the current song in the playlist.
     * @return The current song, or null if no current song
     */
    Song getCurrentSong();

    /**
     * Resets the navigation to the beginning of the playlist.
     */
    void reset();

    /**
     * Checks if there are more songs to play.
     * @return true if there are more songs, false otherwise
     */
    boolean hasMoreSongs();

    /**
     * Gets the name of the strategy.
     * @return The strategy name (e.g., "Sequential", "Shuffle", "Repeat One")
     */
    String getStrategyName();
}