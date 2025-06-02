package persistence;

import songsAndArtists.Artist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import persistence.interfaces.ArtistRepositoryInterface;

/**
 * A specific repository for Artist objects.
 * It extends the generic JsonRepository and can contain additional methods specific to Artist.
 */
public class ArtistRepository extends JsonRepository<Artist> implements ArtistRepositoryInterface {

    /**
     * Singleton instance of ArtistRepository.
     */
    private static ArtistRepository instance;

    /**
     * Private constructor that uses "artists.json" as the storage file.
     */
    private ArtistRepository() {
        super(Artist.class, "artists.json", Artist::getArtistID);
    }

    /**
     * Gets the singleton instance of Artist Repository.
     *
     * @return The singleton instance
     */
    public static synchronized ArtistRepository getInstance() {
        if (instance == null) {
            instance = new ArtistRepository();
        }
        return instance;
    }

    /**
     * Finds artists by name (first name, last name, or full name).
     * Performs a case-insensitive search across first name, last name, and
     * combined full name to find matching artists.
     *
     * @param name The name to search for in artist names
     * @return A list of artists whose first name, last name, or full name matches the search term
     */
    @Override
    public List<Artist> findByName(String name) {
        return findAll().stream()
                .filter(artist ->
                        artist.getFirstName().equalsIgnoreCase(name) ||
                                artist.getLastName().equalsIgnoreCase(name) ||
                                (artist.getFirstName() + " " + artist.getLastName()).equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    /**
     * Finds artists by their country of birth.
     * Performs a case-insensitive search to find artists from the specified country.
     *
     * @param country The country of birth to search for
     * @return A list of artists from the specified country
     */
    @Override
    public List<Artist> findByCountry(String country) {
        return findAll().stream()
                .filter(artist -> artist.getCountryOfBirth().equalsIgnoreCase(country))
                .collect(Collectors.toList());
    }

    /**
     * Adds a song to an artist's song list.
     * Retrieves the artist by ID, adds the song to their songs list if not already present,
     * and persists the updated artist.
     *
     * @param artistId The ID of the artist to update
     * @param songId The ID of the song to add
     * @return true if the artist was found and updated or if the song was already in the artist's list,
     *         false if the artist wasn't found
     */
    @Override
    public boolean addSongToArtist(int artistId, int songId) {
        Optional<Artist> artistOpt = findById(artistId);

        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            List<Integer> songs = artist.getSongs();

            if (songs == null) {
                songs = new ArrayList<>();
                artist.setSongs(songs);
            }

            if (!songs.contains(songId)) {
                songs.add(songId);
                return update(artist).isPresent();
            }
            return true; // Song already in artist's list
        }

        return false; // Artist not found
    }

    /**
     * Removes a song from an artist's song list.
     * Retrieves the artist by ID, removes the song from their songs list if present,
     * and persists the updated artist.
     *
     * @param artistId The ID of the artist to update
     * @param songId The ID of the song to remove
     * @return true if the artist was found and the song was removed and the update was successful,
     *         false if the artist wasn't found, had no songs, or the song wasn't in the list
     */
    @Override
    public boolean removeSongFromArtist(int artistId, int songId) {
        Optional<Artist> artistOpt = findById(artistId);

        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            List<Integer> songs = artist.getSongs();

            if (songs == null) {
                return false; // No songs to remove
            }

            boolean removed = songs.removeIf(id -> id == songId);

            if (removed) {
                return update(artist).isPresent();
            }
        }

        return false; // Artist not found or song not in list

    }
}
