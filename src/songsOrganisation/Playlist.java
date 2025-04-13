package songsOrganisation;

import songsAndArtists.*;
import java.util.*;

public class Playlist {
    private String name;
    private SongNode head;
    private SongNode tail;
    private int size;

    public Playlist(String name) {
        this.name = name;
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void addSong(Song song) {
        SongNode newNode = new SongNode(song);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            newNode.setPrevious(tail);
            tail = newNode;
        }
        size++;
    }

    // Add more operations: removeSong, moveNext, movePrevious, etc.

    // Helper method to convert to a list for JSON persistence
    public List<Song> toList() {
        List<Song> songs = new ArrayList<>();
        SongNode current = head;
        while (current != null) {
            songs.add(current.getSong());
            current = current.getNext();
        }
        return songs;
    }

    // Helper method to rebuild from a list (after loading from JSON)
    public void fromList(List<Song> songs) {
        clear(); // Clear current playlist
        for (Song song : songs) {
            addSong(song);
        }
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    // Other getters and setters
}
