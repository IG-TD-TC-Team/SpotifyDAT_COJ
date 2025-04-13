package songsAndArtists;

public class SongNode {
    private Song song;
    private SongNode previous;
    private SongNode next;

    public SongNode(Song song) {
        this.song = song;
        this.previous = null;
        this.next = null;
    }

    // Getters and setters
    public Song getSong() { return song; }
    public SongNode getPrevious() { return previous; }
    public SongNode getNext() { return next; }
    public void setSong(Song song) { this.song = song; }
    public void setPrevious(SongNode previous) { this.previous = previous; }
    public void setNext(SongNode next) { this.next = next; }
}