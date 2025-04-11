package songsManagement;

import java.util.List;

public class Library {

    private int userID;
    private List<Playlist> playlists;

    public Library(int userID) {
        this.userID = userID;
    }
}