package persistence;

import songsAndArtists.Artist;
import java.util.List;

/**
 * A specific repository for Artist objects.
 * It extends the generic JsonRepository and can contain additional methods specific to Artist.
 */
public class ArtistRepository extends JsonRepository<Artist> {

    /**
     * Default constructor that uses "artists.json" as the storage file.
     */
    public ArtistRepository() {
        super(Artist.class, "artists.json");
    }

    /**
     * Finds an Artist in the JSON file by the given artistID.
     *
     * @param artistID The ID of the artist to find.
     * @return The Artist object if found, otherwise null.
     */
    public Artist findById(int artistID) {
        // Retrieve all artists.
        List<Artist> artists = findAll();
        // Use Java streams to filter by artistID.
        return artists.stream()
                .filter(artist -> artist.getArtistID() == artistID)
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates an existing artist in the repository.
     *
     * @param artist The artist with updated fields.
     * @return true if the artist was found and updated, false otherwise.
     */
    public boolean update(Artist artist) {
        List<Artist> artists = findAll();
        boolean found = false;

        for (int i = 0; i < artists.size(); i++) {
            if (artists.get(i).getArtistID() == artist.getArtistID()) {
                artists.set(i, artist);
                found = true;
                break;
            }
        }

        if (found) {
            saveAll(artists);
        }

        return found;
    }
}
