package persistence.interfaces;

import songsAndArtists.Artist;

import java.util.List;

public interface ArtistRepositoryInterface extends Repository<Artist> {
    /**
     * Finds artists by name (first name, last name, or full name).
     */
    List<Artist> findByName(String name);

    /**
     * Finds artists by country.
     */
    List<Artist> findByCountry(String country);

    /**
     * Adds a song to an artist's song list.
     */
    boolean addSongToArtist(int artistId, int songId);

    /**
     * Removes a song from an artist's song list.
     */
    boolean removeSongFromArtist(int artistId, int songId);
}
