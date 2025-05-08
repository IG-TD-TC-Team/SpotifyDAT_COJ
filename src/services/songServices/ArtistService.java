package services.songServices;

import factory.RepositoryFactory;
import persistence.interfaces.ArtistRepositoryInterface;
import songsAndArtists.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistService {

    /**
     * Singleton instance of ArtistService.
     */
    private static ArtistService instance;

    /**
     * Repository for accessing artist data.
     */
    private final ArtistRepositoryInterface artistRepository;

    /**
     * List of artists.
     */
    private List<Artist> artists;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the artist repository instance using the RepositoryFactory.
     */



    private ArtistService() {
        artistRepository = RepositoryFactory.getInstance().getArtistRepository();
        artists = new ArrayList<>();
        artists = artistRepository.findAll();
    }

    /**
     * Returns the singleton instance of ArtistService.
     * @return The singleton instance of ArtistService.
     */
    public static synchronized ArtistService getInstance() {
        if (instance == null) {
            instance = new ArtistService();
        }
        return instance;
    }



    /// ------------------------- ARTIST RETRIEVAL ----------------- ///
    /**
     * Retrieves an artist by their ID.
     * @param artistId The ID of the artist to retrieve.
     * @return The artist with the specified ID, or null if not found.
     */
    public Artist getArtistById(int artistId) {
        refreshCache();
        for (Artist artist : artists) {
            if (artist.getArtistID() == artistId) {
                return artist;
            }
        }
        return null; // Artist not found
    }
    /**
     * Retrieves all artists.
     * @return A list of all artists.
     */
    public List<Artist> getAllArtists() {
        refreshCache();
        return artists;
    }
    /**
     * Refreshes the repository data with the latest changes.
     * Used after operations that modify user data in other services.
     */
    public void refreshCache() {
        // We'll implement caching in a future iteration
        artists = artistRepository.findAll();
    }

}
