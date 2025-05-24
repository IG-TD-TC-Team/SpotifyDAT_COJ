package seeding;

import persistence.ArtistRepository;
import persistence.SongRepository;
import songsAndArtists.Artist;
import songsAndArtists.Song;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks all Artist entries in the JSON database and removes any
 * artist who has no songs in the Song repository.
 */
public class ArtistIntegritySeeder {

    private final ArtistRepository artistRepo;
    private final SongRepository   songRepo;

    public ArtistIntegritySeeder() {
        this.artistRepo = ArtistRepository.getInstance();
        this.songRepo   = SongRepository.getInstance();
    }

    /**
     * Runs the artist integrity check.
     * Builds a set of all artistIds referenced by at least one Song,
     * then deletes every Artist not in that set.
     */
    public void run() {
        // 1) Gather all artist IDs that actually have songs
        List<Song> allSongs = songRepo.findAll();
        Set<Integer> activeArtistIds = allSongs.stream()
                .map(Song::getArtistId)
                .collect(Collectors.toSet());

        // 2) Iterate through every artist and delete those with no songs
        List<Artist> allArtists = artistRepo.findAll();
        for (Artist artist : allArtists) {
            int artistId = artist.getArtistID();
            if (!activeArtistIds.contains(artistId)) {
                boolean deleted = artistRepo.deleteById(artistId);
                if (deleted) {
                    System.out.printf("WARNING: Deleted Artist[id=%d, name=\"%s %s\"] – no songs found.%n",
                            artistId, artist.getFirstName(), artist.getLastName());
                } else {
                    System.err.printf("ERROR: Failed to delete Artist[id=%d] despite no songs.%n", artistId);
                }
            }
        }
    }

    public static void main(String[] args) {
        new ArtistIntegritySeeder().run();
    }
}
