package songsAndArtists;

import java.rmi.server.UID;
import java.util.*;

public class Album {

    public Album() {}
    public Album (int id, String title, String artistId, List<Integer> songs) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.songs = songs;
    }


    private  int id;
    private String title;
    private String artistId;
    private List<Integer> songs;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getArtistId() {
        return artistId;
    }
    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }
    public List<Integer> getSongs() {
        return songs;
    }
    public void setSongs(List<Integer> songs) {
        this.songs = songs;
    }
}
