package seeding;

import persistence.interfaces.SongRepositoryInterface;
import persistence.MusicResourceExtractor;
import factory.RepositoryFactory;
import songsAndArtists.Song;

import java.nio.file.Path;
import java.util.List;

/**
 * Updated utility class to fix and standardize file paths for existing songs in the database
 * using the MusicResourceExtractor system. This class updates all song paths in the JSON database
 * to point to the extracted music directory, ensuring compatibility with both development
 * and production (JAR) environments.
 */
public class FixExistingSongPaths {

    /**
     * Main method to execute the song path fixing process.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("FIXING SONG PATHS WITH MUSIC RESOURCE EXTRACTOR");
        System.out.println("=".repeat(70));

        // Initialize the music directory (extracts from JAR if needed)
        System.out.println("Initializing music resource extractor...");
        Path musicDirectory = MusicResourceExtractor.ensureMusicDirectory();
        System.out.println("✓ Music directory: " + musicDirectory.toAbsolutePath());

        // Get all songs from the database
        SongRepositoryInterface songRepository = RepositoryFactory.getInstance().getSongRepository();
        List<Song> allSongs = songRepository.findAll();
        System.out.println("✓ Loaded " + allSongs.size() + " songs from database");

        // Get available music files
        List<String> availableFiles = MusicResourceExtractor.listExtractedMusicFiles();
        System.out.println("✓ Found " + availableFiles.size() + " music files in directory");
        System.out.println();

        int updatedCount = 0;
        int matchedCount = 0;
        int notFoundCount = 0;
        int unchangedCount = 0;

        for (Song song : allSongs) {
            String oldPath = song.getFilePath();
            String sanitizedFileName = sanitizeFileName(song.getTitle()) + ".mp3";
            String newPath = MusicResourceExtractor.getMusicFilePath(sanitizedFileName);

            // Check if the path needs updating
            if (!oldPath.equals(newPath)) {
                // Check if the file exists in the extracted directory
                boolean fileExists = MusicResourceExtractor.musicFileExists(sanitizedFileName);

                if (fileExists) {
                    System.out.println("✓ Updating: " + song.getTitle());
                    System.out.println("  From: " + oldPath);
                    System.out.println("  To:   " + newPath);

                    song.setFilePath(newPath);
                    songRepository.update(song);
                    updatedCount++;
                    matchedCount++;
                } else {
                    System.out.println("⚠ File not found for: " + song.getTitle());
                    System.out.println("  Expected: " + sanitizedFileName);
                    System.out.println("  Updating path anyway...");

                    song.setFilePath(newPath);
                    songRepository.update(song);
                    updatedCount++;
                    notFoundCount++;
                }
                System.out.println();
            } else {
                unchangedCount++;
            }
        }

        // Display summary
        System.out.println("=".repeat(70));
        System.out.println("SONG PATH UPDATE SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println("Total songs in database: " + allSongs.size());
        System.out.println("Songs with updated paths: " + updatedCount);
        System.out.println("  - Files found and matched: " + matchedCount);
        System.out.println("  - Files not found: " + notFoundCount);
        System.out.println("Songs with unchanged paths: " + unchangedCount);
        System.out.println("Available music files: " + availableFiles.size());
        System.out.println();

        // Show file matching analysis
        if (notFoundCount > 0) {
            System.out.println("MISSING FILES ANALYSIS:");
            System.out.println("-".repeat(50));

            for (Song song : allSongs) {
                String sanitizedFileName = sanitizeFileName(song.getTitle()) + ".mp3";
                if (!MusicResourceExtractor.musicFileExists(sanitizedFileName)) {
                    System.out.println("Missing: " + song.getTitle() + " -> " + sanitizedFileName);
                }
            }
            System.out.println();
        }

        // Show available files that might not be in database
        System.out.println("AVAILABLE MUSIC FILES:");
        System.out.println("-".repeat(50));
        if (availableFiles.isEmpty()) {
            System.out.println("No music files found in the extracted directory.");
            System.out.println("Make sure your music files are in the JAR under /music/ folder.");
        } else {
            System.out.println("Sample of available files:");
            availableFiles.stream().limit(10).forEach(file -> System.out.println("  - " + file));
            if (availableFiles.size() > 10) {
                System.out.println("  ... and " + (availableFiles.size() - 10) + " more files");
            }
        }

        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("Update completed! All song paths now use MusicResourceExtractor system.");
        System.out.println("=".repeat(70));
    }

    /**
     * Sanitizes a file name by removing or replacing problematic characters.
     * This method should match the sanitization used in MusicFactory to ensure consistency.
     *
     * @param fileName The original file name
     * @return A sanitized file name safe for use in file paths
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "untitled";
        }

        String sanitized = fileName.toLowerCase().trim();

        sanitized = sanitized
                .replace(" ", "")
                .replace("'", "")
                .replace("'", "")  // Smart apostrophe
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
                .replace("–", "-")  // En dash
                .replace("—", "-")  // Em dash
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("ë", "e")
                .replace("à", "a")
                .replace("á", "a")
                .replace("â", "a")
                .replace("ä", "a")
                .replace("å", "a")
                .replace("ç", "c")
                .replace("ñ", "n")
                .replace("ö", "o")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("ø", "o")
                .replace("ü", "u")
                .replace("ú", "u")
                .replace("ù", "u")
                .replace("û", "u")
                .replace("ï", "i")
                .replace("í", "i")
                .replace("î", "i")
                .replace("ì", "i");

        // Remove any remaining non-alphanumeric characters except hyphens and underscores
        sanitized = sanitized.replaceAll("[^a-z0-9_-]", "");

        // Ensure the filename is not empty
        if (sanitized.isEmpty()) {
            sanitized = "untitled";
        }

        return sanitized;
    }

    /**
     * Helper method to check if a song title would match an available file.
     * This can help with debugging file matching issues.
     *
     * @param songTitle The title of the song
     * @param availableFiles List of available music files
     * @return true if a potential match is found
     */
    private static boolean hasFileMatch(String songTitle, List<String> availableFiles) {
        String sanitized = sanitizeFileName(songTitle);
        String expectedFileName = sanitized + ".mp3";

        // Check exact match
        if (availableFiles.contains(expectedFileName)) {
            return true;
        }

        // Check for partial matches (without extension)
        for (String file : availableFiles) {
            String fileWithoutExt = file.toLowerCase().replaceFirst("\\.[^.]+$", "");
            if (fileWithoutExt.equals(sanitized)) {
                return true;
            }
        }

        return false;
    }
}