package seeding;

import persistence.SongRepository;
import songsAndArtists.Song;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Checks all Song entries in the JSON database and removes any whose
 * filePath no longer points to an existing file.
 */
public class DataIntegritySeeder {

    private final SongRepository songRepo;

    public DataIntegritySeeder() {
        this.songRepo = SongRepository.getInstance();
    }

    /**
     * Runs the integrity check.
     * Iterates through every song in the JSON store, verifies that
     * its filePath exists on disk, and deletes the entry if it doesn't.
     */
    public void run() {
        List<Song> allSongs = songRepo.findAll();
        for (Song song : allSongs) {
            Path path = Path.of(song.getFilePath());
            if (Files.notExists(path)) {
                boolean deleted = songRepo.deleteById(song.getSongId());
                if (deleted) {
                    System.out.printf("WARNING: Deleted Song[id=%d, title=\"%s\"] – file not found at %s%n",
                            song.getSongId(), song.getTitle(), song.getFilePath());
                } else {
                    System.err.printf("ERROR: Failed to delete Song[id=%d] despite missing file.%n",
                            song.getSongId());
                }
            }
        }
    }

    public static void main(String[] args) {
        new DataIntegritySeeder().run();
    }
}
