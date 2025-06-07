package songsAndArtists;

/**
 * Enumeration representing different musical genres available in the system.
 *
 * <p>This enum is used to categorize songs by their musical style and is utilized
 * throughout the application for filtering, searching, and organizing music content.
 * Each genre represents a distinct musical category that helps users discover
 * and organize their music preferences.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * Song song = new Song(1, "Song Title", artistId, albumId, Genre.ROCK, 180, "/path/to/song.mp3");
 * List<Song> rockSongs = songRepository.findByGenre(Genre.ROCK);
 * }</pre>
 *
 * @see Song#getGenre()
 * @see Song#setGenre(Genre)
 */
public enum Genre {
    /**
     * Rock music genre, characterized by strong rhythm and electric instruments.
     */
    ROCK,

    /**
     * Pop music genre, featuring catchy melodies and mainstream appeal.
     */
    POP,

    /**
     * Jazz music genre, known for improvisation and complex harmonies.
     */
    JAZZ,

    /**
     * Classical music genre, featuring orchestral and traditional compositions.
     */
    CLASSICAL,

    /**
     * Hip-hop music genre, characterized by rhythmic speech and urban culture.
     */
    HIP_HOP,

    /**
     * Electronic music genre, created primarily using electronic instruments and technology.
     */
    ELECTRONIC,

    /**
     * Country music genre, featuring rural themes and traditional instruments.
     */
    COUNTRY,

    /**
     * Blues music genre, characterized by specific chord progressions and emotional expression.
     */
    BLUES
}