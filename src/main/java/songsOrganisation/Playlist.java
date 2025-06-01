package songsOrganisation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import songsAndArtists.*;

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
    private int playlistID;
    private List<Integer> IDsOfUsersSharedWith;
    private List<Integer> likedByUsers;
    private boolean isPubliclyShareable;


    /**
     * Default constructor for Jackson deserialization.
     */
    public Playlist() {
        this.songs = new LinkedList<>();
        this.IDsOfUsersSharedWith = new ArrayList<>();
        this.likedByUsers = new ArrayList<>();
        this.isPubliclyShareable = false;
    }

    /**
     * Constructor to create a Playlist object with the specified name and owner ID.
     * @param name The name of the playlist.
     * @param ownerID The ID of the user who owns the playlist.
     */

    public Playlist(String name, int ownerID) {
        this.name = name;
        this.ownerID = ownerID;
        this.songs = new LinkedList<>();
        this.IDsOfUsersSharedWith = new ArrayList<>();
        this.likedByUsers = new ArrayList<>();
        this.isPubliclyShareable = false;
    }

/// --------------------- PLAYLIST SOCIAL GET/SET ----------------- ///

    /**
     * Retrieves the list of users with whom the playlist is shared.
     * @return The list of users with whom the playlist is shared.
     */
    public List<Integer> getSharedWith() {
        return IDsOfUsersSharedWith;  // Return the actual list, not an unmodifiable view
    }

    /**
     * Sets the shared users list.
     * @param sharedWith The new list of users with whom to share the playlist.
     */
    public void setSharedWith(List<Integer> sharedWith) {
        this.IDsOfUsersSharedWith = sharedWith;
    }


/// ---------------------- PLAYLIST SONGS GET/SET ----------------- ///
    /**
     * Retrieves the number of songs in the playlist.
     * @return The number of songs in the playlist.
     */
    @JsonIgnore // Prevent serialization of this method as a property
    public int getSongCount() {
        return songs.size();
    }

    /**
     * Retrieves a song at a specific index in the playlist.
     * @param index The index of the song to retrieve.
     * @return The song at the specified index, or null if the index is out of bounds.
     */
    @JsonIgnore // Prevent serialization of this method as a property
    public Song getSongAt(int index) {
        if (index >= 0 && index < songs.size()) {
            return songs.get(index);
        }
        return null;
    }
    /**
     * Retrieves the list of songs in the playlist.
     * @return An unmodifiable list of songs in the playlist.
     */
    public LinkedList<Song> getSongs() {

        return songs;
    }

    /**
     * Adds the songs to the playlist.
     * @param songs The songs to set.
     */
    public void setSongs(LinkedList<Song> songs) {
        this.songs = songs;
    }



    /// --------------------- PLAYLIST GET/SET ----------------- ///
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
     * Retrieves the owner of the playlist.
     * @return The owner of the playlist.
     */
    public int getOwnerID() {
        return ownerID;
    }
    /**
     * Retrieves the ID of the playlist.
     * @return The ID of the playlist.
     */
    public int getPlaylistID() {
        return playlistID;
    }
    /**
     * Sets the ID of the playlist.
     * @param playlistID The new ID of the playlist.
     */
    public void setPlaylistID(int playlistID) {
        this.playlistID = playlistID;
    }

    /**
     * Sets the owner of the playlist.
     * @param ownerID The new owner of the playlist.
     */
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }


    /**
     * Calculates the total duration of the playlist in seconds.
     * @return The total duration of the playlist in seconds.
     */
    @JsonIgnore // Prevent serialization of this method as a property
    public int getTotalDuration() {
        int totalDuration = 0;
        for (Song song : songs) {
            totalDuration += song.getDurationSeconds();
        }
        return totalDuration;
    }

    public List<Integer> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<Integer> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    public boolean isPubliclyShareable() {
        return isPubliclyShareable;
    }

    public void setPubliclyShareable(boolean publiclyShareable) {
        this.isPubliclyShareable = publiclyShareable;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "name='" + name + '\'' +
                ", songs=" + songs.size() +
                //", owner=" + owner.getUsername() +
                ", sharedWith=" + IDsOfUsersSharedWith.size() +
                '}';
    }

/// ---------------------- PLAYLIST HELPER METHODS ----------------- ///
    /// Helper method to rebuild from a list (after loading from JSON)
    /**
     * Rebuilds the playlist from a list of songs.
     * @param songsList The list of songs to set in the playlist.
     */
    public void fromList(List<Song> songsList) {
        clear(); // Clear current playlist

        this.songs.addAll(songsList);
    }

    /// Helper method to convert to a list for JSON persistence
    /// This method is used when saving the playlist to JSON
    /**
     * Converts the playlist to a list of songs.
     * @return A list of songs in the playlist.
     */
    @JsonIgnore // Prevent serialization of this method as a property
    public List<Song> toList() {
        return new ArrayList<>(songs);
    }

    /**
     * Clears the playlist by removing all songs.
     */
    public void clear() {
        songs.clear();
    }



}