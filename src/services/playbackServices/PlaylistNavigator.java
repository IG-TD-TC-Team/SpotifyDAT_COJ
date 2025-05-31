package services.playbackServices;

import songsAndArtists.Song;
import songsOrganisation.Playlist;
import java.util.LinkedList;

/**
 * Manages playlist playback using various navigation strategies.
 * Implements the Strategy pattern to support different playback modes.
 */
public class PlaylistNavigator {
    // Available strategies
    private PlaylistNavigationStrategy sequentialStrategy;
    private PlaylistNavigationStrategy shuffleStrategy;
    private PlaylistNavigationStrategy repeatOneStrategy;
    private PlaylistNavigationStrategy repeatAllStrategy;

    // Current strategy
    private PlaylistNavigationStrategy currentStrategy;

    // Playlist data
    private Playlist playlist;
    private LinkedList<Song> songs;

    /**
     * Creates a new PlaylistNavigator.
     */
    public PlaylistNavigator() {
        // Initialize strategies
        sequentialStrategy = new SequentialNavigationStrategy();
        shuffleStrategy = new ShuffleNavigationStrategy();
        repeatOneStrategy = new RepeatOneNavigationStrategy();
        repeatAllStrategy = new RepeatAllNavigationStrategy();

        // Default to sequential
        currentStrategy = sequentialStrategy;
    }

    /**
     * Sets the playlist to navigate.
     * @param playlist The playlist to navigate
     */
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        this.songs = playlist.getSongs();

        // Initialize all strategies with the songs
        sequentialStrategy.initialize(songs);
        shuffleStrategy.initialize(songs);
        repeatOneStrategy.initialize(songs);
        repeatAllStrategy.initialize(songs);
    }

    /**
     * Gets the next song according to the current strategy.
     * @return The next song, or null if no more songs
     */
    public Song getNextSong() {
        return currentStrategy.getNextSong();
    }

    /**
     * Gets the previous song according to the current strategy.
     * @return The previous song, or null if at the beginning
     */
    public Song getPreviousSong() {
        return currentStrategy.getPreviousSong();
    }

    /**
     * Gets the current song in the playlist.
     * @return The current song, or null if no current song
     */
    public Song getCurrentSong() {
        return currentStrategy.getCurrentSong();
    }

    /**
     * Sets the navigation strategy to sequential.
     */
    public void useSequentialStrategy() {
        Song currentSong = currentStrategy.getCurrentSong();
        currentStrategy = sequentialStrategy;
        if (currentSong != null) {
            // Try to maintain position
            int index = songs.indexOf(currentSong);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    sequentialStrategy.getNextSong();
                }
            }
        }
    }

    /**
     * Sets the navigation strategy to shuffle.
     */
    public void useShuffleStrategy() {
        currentStrategy = shuffleStrategy;
    }

    /**
     * Sets the navigation strategy to repeat one song.
     */
    public void useRepeatOneStrategy() {
        Song currentSong = currentStrategy.getCurrentSong();
        currentStrategy = repeatOneStrategy;
        if (currentSong != null) {
            // Try to maintain position
            int index = songs.indexOf(currentSong);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    repeatOneStrategy.getNextSong();
                }
            }
        }
    }

    /**
     * Sets the navigation strategy to repeat all songs.
     */
    public void useRepeatAllStrategy() {
        Song currentSong = currentStrategy.getCurrentSong();
        currentStrategy = repeatAllStrategy;
        if (currentSong != null) {
            // Try to maintain position
            int index = songs.indexOf(currentSong);
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    repeatAllStrategy.getNextSong();
                }
            }
        }
    }

    /**
     * Gets the current strategy name.
     * @return The name of the current strategy
     */
    public String getCurrentStrategyName() {
        return currentStrategy.getStrategyName();
    }

    /**
     * Resets the playlist navigation to the beginning.
     */
    public void reset() {
        currentStrategy.reset();
    }

    /**
     * Checks if there are more songs to play.
     * @return true if there are more songs, false otherwise
     */
    public boolean hasMoreSongs() {
        return currentStrategy.hasMoreSongs();
    }

    /**
     * Gets the current playlist.
     * @return The current playlist
     */
    public Playlist getPlaylist() {
        return playlist;
    }
}