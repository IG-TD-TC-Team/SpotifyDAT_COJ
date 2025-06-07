package songsAndArtists;

/**
 * Represents a song with its details.
 */
public class Song {
    /**
     * @param genre used as genre.ROCK to add a genre.
     * */
    private int songId;
    private String title;
    private int artistId;
    private int albumId;
    private Genre genre;
    private int durationSeconds;
    private String filePath;

    public Song(int songId, String title, int artistId, int albumId, Genre genre, int durationSeconds, String filePath) {
        this.songId = songId;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
    }
    public Song() {}
    /**
     * Getters and Setters
     */
    public int getSongId() {
        return songId;
    }
    public void setSongId(int songId) {
        this.songId = songId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getArtistId() {
        return artistId;
    }
    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }
    public int getAlbumId() {
        return albumId;
    }
    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }
    public Genre getGenre() {
        return genre;
    }
    public void setGenre(Genre genre) {
        this.genre = genre;
    }
    public int getDurationSeconds() {
        return durationSeconds;
    }
    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


}
