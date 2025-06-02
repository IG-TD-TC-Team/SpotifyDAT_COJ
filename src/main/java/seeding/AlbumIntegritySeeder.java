package seeding;

import persistence.AlbumRepository;
import persistence.SongRepository;
import songsAndArtists.Album;
import songsAndArtists.Song;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks all Album entries in the JSON database and removes any
 * album that has no songs in the Song repository.
 */
public class AlbumIntegritySeeder {

    /**
     * Repository for accessing and modifying album data.
     * Used to retrieve all albums and delete orphaned ones.
     */
    private final AlbumRepository albumRepo;

    /**
     * Repository for accessing song data.
     * Used to determine which albums are referenced by songs.
     */
    private final SongRepository  songRepo;

    /**
     * Constructs a new AlbumIntegritySeeder with the necessary repositories.
     *
     * Initializes the seeder with instances of the album and song repositories
     * obtained through their respective singleton patterns.
     *
     */
    public AlbumIntegritySeeder() {
        this.albumRepo = AlbumRepository.getInstance();
        this.songRepo  = SongRepository.getInstance();
    }

    /**
     * Runs the album integrity check.
     * Builds a set of all albumIds referenced by at least one Song,
     * then deletes every Album not in that set.
     */
    public void run() {
        // 1) Gather all album IDs that actually have songs
        List<Song> allSongs = songRepo.findAll();
        Set<Integer> activeAlbumIds = allSongs.stream()
                .map(Song::getAlbumId)
                .collect(Collectors.toSet());

        // 2) Iterate through every album and delete those with no songs
        List<Album> allAlbums = albumRepo.findAll();
        for (Album album : allAlbums) {
            int albumId = album.getId();
            if (!activeAlbumIds.contains(albumId)) {
                boolean deleted = albumRepo.deleteById(albumId);
                if (deleted) {
                    System.out.printf(
                            "WARNING: Deleted Album[id=%d, title=\"%s\"] – no songs found.%n",
                            albumId, album.getTitle()
                    );
                } else {
                    System.err.printf(
                            "ERROR: Failed to delete Album[id=%d] despite no songs.%n",
                            albumId
                    );
                }
            }
        }
    }

    public static void main(String[] args) {
        new AlbumIntegritySeeder().run();
    }
}
