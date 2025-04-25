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

    @Override
    public List<Artist> findByName(String name) {
        return findAll().stream()
                .filter(artist ->
                        artist.getFirstName().equalsIgnoreCase(name) ||
                                artist.getLastName().equalsIgnoreCase(name) ||
                                (artist.getFirstName() + " " + artist.getLastName()).equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Artist> findByCountry(String country) {
        return findAll().stream()
                .filter(artist -> artist.getCountryOfBirth().equalsIgnoreCase(country))
                .collect(Collectors.toList());
    }

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
