package songsOrganisation;

import songsAndArtists.*;
import user.User;

import java.util.*;

/**
 * Playlist class represents a collection of songs that can be managed by a user.
 * It allows adding, removing, and sharing songs with other users.
 * It also provides methods to move songs within the playlist and find songs by artist or genre.
 */

public class Playlist {
    private String name;
    private LinkedList<Song> songs;
    private int ownerID;
    private List<Integer> iDsOfUsersSharedWith;

    public Playlist(String name, int ownerID) {
        this.name = name;
        this.ownerID = ownerID;
        this.songs = new LinkedList<>();
        this.iDsOfUsersSharedWith = new ArrayList<>();
    }

    /**
     * Adds a song to the playlist.
     * @param song The song to be added.
     */
    public void addSong(Song song) {
        songs.add(song);
    }

    /**
     * Adds a song to the beginning of the playlist.
     * @param song The song to be added at the beginning.
     */
    public void addSongToBeginning(Song song) {
        songs.addFirst(song);
    }

    /**
     * Adds a song to the end of the playlist.
     * @param song The song to be added at the end.
     */
    public void addSongToEnd(Song song) {
        songs.addLast(song);
    }

    /**
     * Removes a song from the playlist.
     * @param song The song to be removed.
     */
    public void removeSong(Song song) {
        songs.remove(song);
    }

    /**
     * Removes the first song from the playlist.
     * @return The removed song or null if the playlist is empty.
     */
    public Song removeFirstSong() {
        if (songs.isEmpty()) {
            return null;
        }
        return songs.removeFirst();
    }

    /**
     * Removes the last song from the playlist.
     * @return The removed song or null if the playlist is empty.
     */
    public Song removeLastSong() {
        if (songs.isEmpty()) {
            return null;
        }
        return songs.removeLast();
    }


    /**
     * Moves a song forward one position in the playlist.
     * @param song The song to be moved forward.
     *
     * This method finds the specified song in the playlist and swaps it with the song
     * immediately after it, effectively moving it one position forward in the list.
     * If the song is already at the end of the playlist, no movement occurs.
     */
    public void moveNext(Song song) {
        int index = songs.indexOf(song);
        if (index < songs.size() - 1) {
            Collections.swap(songs, index, index + 1);
        }
    }

    /**
     * Moves a song backward one position in the playlist.
     * @param song The song to be moved backward.
     *
     * This method finds the specified song in the playlist and swaps it with the song
     * immediately before it, effectively moving it one position backward in the list.
     * If the song is already at the beginning of the playlist, no movement occurs.
     */
    public void movePrevious(Song song) {
        int index = songs.indexOf(song);
        if (index > 0) {
            Collections.swap(songs, index, index - 1);
        }
    }

    /**
     * Gets the first song in the playlist.
     * @return The first song or null if the playlist is empty.
     */
    public Song getFirstSong() {
        if (songs.isEmpty()) {
            return null;
        }
        return songs.getFirst();
    }

    /**
     * Gets the last song in the playlist.
     * @return The last song or null if the playlist is empty.
     */
    public Song getLastSong() {
        if (songs.isEmpty()) {
            return null;
        }
        return songs.getLast();
    }

    /**
     * Checks if the playlist is shared with a specific user.
     * @param userID The user to check.
     * @return true if the playlist is shared with the user, false otherwise.
     */
    public boolean isSharedWith(int userID) {
        return iDsOfUsersSharedWith.contains(userID);
    }

    /**
     * Shares the playlist with another user.
     * @param userID The user to share the playlist with.
     */
    public void addUserToShareWith(int userID) {
        if (!iDsOfUsersSharedWith.contains(userID)) {
            iDsOfUsersSharedWith.add(userID);
        }
    }

    /**
     * Removes a user from the shared list.
     * @param userID The user to remove from the shared list.
     */
    public void removeSharing(int userID) {
        iDsOfUsersSharedWith.remove(userID);
    }


    /**
     * Retrieves the number of songs in the playlist.
     * @return The number of songs in the playlist.
     */
    public int getSongCount() {
        return songs.size();
    }

    /**
     * Retrieves a song at a specific index in the playlist.
     * @param index The index of the song to retrieve.
     * @return The song at the specified index, or null if the index is out of bounds.
     */
    public Song getSongAt(int index) {
        if (index >= 0 && index < songs.size()) {
            return songs.get(index);
        }
        return null;
    }


    /// Helper method to convert to a list for JSON persistence
    /// This method is used when saving the playlist to JSON
    /**
     * Converts the playlist to a list of songs.
     * @return A list of songs in the playlist.
     */
    public List<Song> toList() {
        return new ArrayList<>(songs);
    }

    /// Helper method to rebuild from a list (after loading from JSON)
    /**
     * Rebuilds the playlist from a list of songs.
     * @param songs The list of songs to set in the playlist.
     */
    public void fromList(List<Song> songs) {
        clear(); // Clear current playlist
        for (Song song : songs) {
            addSong(song);
        }
    }

    /**
     * Clears the playlist by removing all songs.
     */
    public void clear() {
        songs.clear();
    }

    // Getters and setters
    /**
     * Retrieves the name of the playlist.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the playlist.
     * @param name The new name of the playlist.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the list of songs in the playlist.
     * @return An unmodifiable list of songs in the playlist.
     */
    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    /**
     * Retrieves the owner of the playlist.
     * @return The owner of the playlist.
     */
    public int getOwnerID() {
        return ownerID;
    }

    /**
     * Sets the owner of the playlist.
     * @param ownerID The new owner of the playlist.
     */
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    /**
     * Retrieves the list of users with whom the playlist is shared.
     * @return An unmodifiable list of users with whom the playlist is shared.
     */
    public List<Integer> getSharedWith() {
        return Collections.unmodifiableList(iDsOfUsersSharedWith);
    }

    /**
     * Calculates the total duration of the playlist in seconds.
     * @return The total duration of the playlist in seconds.
     */
    public int getTotalDuration() {
        int totalDuration = 0;
        for (Song song : songs) {
            totalDuration += song.getDurationSeconds();
        }
        return totalDuration;
    }

    /**
     * Finds songs by a specific artist in the playlist.
     * @param artistID The ID of the artist to search for.
     * @return A list of songs by the specified artist.
     */
    public List<Song> findSongsByArtistID(int artistID) {
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getArtistId() == artistID) {
                result.add(song);
            }
        }
        return result;
    }

    /**
     * Finds songs by a specific genre in the playlist.
     * @param genre The genre to search for.
     * @return A list of songs of the specified genre.
     */
    public List<Song> findSongsByGenre(String genre) {
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getGenre() == Genre.valueOf(genre)) {
                result.add(song);
            }
        }
        return result;
    }


    @Override
    public String toString() {
        return "Playlist{" +
                "name='" + name + '\'' +
                ", songs=" + songs.size() +
                //", owner=" + owner.getUsername() +
                ", sharedWith=" + iDsOfUsersSharedWith.size() +
                '}';
    }
}