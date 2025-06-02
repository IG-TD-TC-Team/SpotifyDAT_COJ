package seeding;

import services.songServices.SongService;
import persistence.interfaces.SongRepositoryInterface;
import factory.RepositoryFactory;
import songsAndArtists.Song;

import java.io.File;
import java.util.List;

/**
 * Utility class to fix and standardize file paths for existing songs in the database.
 * This class scans all songs in the repository, generates sanitized file paths based on
 * song titles, updates the database records, and optionally renames the actual files on disk.
 *
 * This is useful for maintaining consistency in file paths and ensuring proper
 * file naming conventions are followed throughout the system.
 */
public class FixExistingSongPaths {

    /**
     * Main method to execute the song path fixing process.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Starting to fix song file paths...");

        SongRepositoryInterface songRepository = RepositoryFactory.getInstance().getSongRepository();
        List<Song> allSongs = songRepository.findAll();

        int fixedCount = 0;
        int renamedCount = 0;

        for (Song song : allSongs) {
            String oldPath = song.getFilePath();
            String newPath = generateSanitizedPath(song.getTitle());

            if (!oldPath.equals(newPath)) {
                System.out.println("Fixing song: " + song.getTitle());
                System.out.println("  Old path: " + oldPath);
                System.out.println("  New path: " + newPath);

                File oldFile = new File("C:\\Users\\facos\\Desktop\\music\\" + oldPath.replace("/", "\\"));
                File newFile = new File(newPath);

                if (oldFile.exists()) {
                    boolean renamed = oldFile.renameTo(newFile);
                    if (renamed) {
                        System.out.println("  Renamed physical file successfully.");
                        song.setFilePath(newPath);
                        songRepository.update(song);
                        fixedCount++;
                        renamedCount++;
                    } else {
                        System.out.println("  Failed to rename physical file!");
                    }
                } else {
                    System.out.println("  Warning: File does not exist, only path updated.");
                    song.setFilePath(newPath);
                    songRepository.update(song);
                    fixedCount++;
                }
            }
        }

        System.out.println("\nFixed " + fixedCount + " song file paths!");
        System.out.println("Renamed " + renamedCount + " physical files.");
    }

    /**
     * Generates a full sanitized file path for a song based on its title.
     *
     * @param title The song title to use for the file name
     * @return A sanitized file path suitable for storage
     */
    private static String generateSanitizedPath(String title) {
        String sanitized = sanitizeFileName(title);
        return "C:\\Users\\facos\\Desktop\\music\\" + sanitized + ".mp3";
    }

    /**
     * Sanitizes a file name by removing or replacing problematic characters.
     * This ensures the file name will work across different systems and protocols.
     *
     * @param fileName The original file name
     * @return A sanitized file name safe for use in file paths
     */
    private static String sanitizeFileName(String fileName) {
        String sanitized = fileName.toLowerCase();

        sanitized = sanitized
                .replace(" ", "")
                .replace("'", "")
                .replace("\"", "")
                .replace("?", "")
                .replace("!", "")
                .replace(":", "")
                .replace(";", "")
                .replace("/", "_")
                .replace("\\", "_")
                .replace("*", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "")
                .replace(",", "")
                .replace(".", "")
                .replace("(", "")
                .replace(")", "")
                .replace("[", "")
                .replace("]", "")
                .replace("{", "")
                .replace("}", "")
                .replace("&", "and")
                .replace("@", "at")
                .replace("#", "")
                .replace("$", "")
                .replace("%", "")
                .replace("^", "")
                .replace("=", "")
                .replace("+", "")
                .replace("~", "")
                .replace("`", "")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ä", "a")
                .replace("ç", "c")
                .replace("ñ", "n")
                .replace("ö", "o")
                .replace("ô", "o")
                .replace("ü", "u")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ï", "i")
                .replace("î", "i");

        sanitized = sanitized.replaceAll("[^a-z0-9_-]", "");

        if (sanitized.isEmpty()) {
            sanitized = "untitled";
        }

        return sanitized;
    }
}
