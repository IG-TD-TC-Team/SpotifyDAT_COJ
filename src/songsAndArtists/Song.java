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
    private String artistId;
    private String albumId;
    private Genre genre;
    private int durationSeconds;
    public String filePath;

    public Song(int songId, String title, String artistId, String albumId, Genre genre, int durationSeconds, String filePath) {
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
    public String getArtistId() {
        return artistId;
    }
    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }
    public String getAlbumId() {
        return albumId;
    }
    public void setAlbumId(String albumId) {
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
