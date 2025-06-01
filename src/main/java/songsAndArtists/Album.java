package songsAndArtists;

import java.rmi.server.UID;
import java.util.*;

/**
 * Album class represents an album in the music library.
 * It contains information about the album's ID, title, artist ID, and a list of song IDs.
 */
public class Album {

    public Album() {}
    private int id;
    private String title;
    private int artistId;
    private List<Integer> songsIds;

    /**
     * Constructor to create an Album object with the specified parameters.
     *
     * @param id       The ID of the album.
     * @param title    The title of the album.
     * @param artistId The ID of the artist associated with the album.
     * @param songsIds A list of song IDs associated with the album.
     */
    public Album (int id, String title, int artistId, List<Integer> songsIds) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.songsIds = songsIds;
    }


    /**
     * Getters and Setters for the Album class.
     */
    /**
     * Get the ID of the album
     * @return the ID of the album
     */
    public int getId() {
        return id;
    }

    /**
     * Set the ID of the album
     * @param id the ID of the album
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the title of the album
     * @return the title of the album
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title of the album
     * @param title the title of the album
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the artist ID associated with the album
     * @return the artist ID associated with the album
     */
    public int getArtistId() {
        return artistId;
    }

    /**
     * Set the artist ID associated with the album
     * @param artistId the artist ID associated with the album
     */
    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    /**
     * Get the list of song IDs associated with the album
     * @return the list of song IDs associated with the album
     */
    public List<Integer> getSongs() {
        return songsIds;
    }

    /**
     * Set the list of song IDs associated with the album
     * @param songsIds the list of song IDs associated with the album
     */
    public void setSongs(List<Integer> songsIds) {
        this.songsIds = songsIds;
    }
}
