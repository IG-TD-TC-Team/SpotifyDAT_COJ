package songsOrganisation;

import java.util.List;

public class Library {

    private int userID;
    private List<Playlist> playlists;

    public Library(int userID, List<Playlist> playlists) {
        this.userID = userID;
        this.playlists = playlists;
    }

    /*
     * Getters and Setters
     */

    /**
     * Get the user ID
     * @return the user ID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Set the user ID
     * @param userID the user ID
     */
    public void setUserID(int userID) {
        this.userID = userID;
    }

    /**
     * Get the playlists
     * @return the playlists
     */
    public List<Playlist> getPlaylists() {
        return playlists;
    }

    /**
     * Set the playlists
     * @param playlists the playlists
     */
    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    /**
     * Add a playlist to the library
     * @param playlist the playlist to add
     */
    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }
    /**
     * Remove a playlist from the library
     * @param playlist the playlist to remove
     */
    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
    }
}