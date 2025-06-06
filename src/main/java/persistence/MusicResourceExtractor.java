package persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extracts music resources from JAR to external filesystem, following the same pattern
 * as JsonRepository for data files. This allows the existing streaming logic to work
 * unchanged with regular file paths.
 */
public class MusicResourceExtractor {

    private static boolean musicResourcesExtracted = false;
    private static Path musicDirectory = null;

    /**
     * Extracts music resources from JAR if running in production mode.
     * Returns the path to the music directory (either extracted or existing).
     */
    public static synchronized Path ensureMusicDirectory() {
        if (musicDirectory != null) {
            return musicDirectory;
        }

        // Determine the music directory path
        musicDirectory = determineMusicDirectory();

        try {
            // Ensure the music directory exists
            Files.createDirectories(musicDirectory);

            // Extract music resources if running from JAR and not already extracted
            if (!musicResourcesExtracted && isRunningFromJar()) {
                extractMusicResources(musicDirectory);
                musicResourcesExtracted = true;
            }

        } catch (IOException e) {
            System.err.println("Warning: Failed to create music directory: " + e.getMessage());
        }

        return musicDirectory;
    }

    /**
     * Determines the appropriate music directory based on execution context.
     */
    private static Path determineMusicDirectory() {
        if (isRunningFromJar()) {
            // Production: Use music folder next to JAR (same as data)
            Path jarPath = getJarPath();
            Path musicDir = jarPath.getParent().resolve("music");
            System.out.println("Production mode: Using music directory: " + musicDir);
            return musicDir;
        } else {
            // Development: Use music folder in current working directory
            Path musicDir = Paths.get("music");
            System.out.println("Development mode: Using music directory: " + musicDir);
            return musicDir;
        }
    }

    /**
     * Checks if the application is running from a JAR file.
     */
    private static boolean isRunningFromJar() {
        try {
            URI uri = MusicResourceExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            return uri.toString().endsWith(".jar");
        } catch (URISyntaxException e) {
            System.err.println("Warning: Could not determine if running from JAR: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the path to the JAR file.
     */
    private static Path getJarPath() {
        try {
            URI uri = MusicResourceExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            return Paths.get(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not determine JAR path", e);
        }
    }

    /**
     * Extracts music resources from JAR to external filesystem.
     */
    private static void extractMusicResources(Path targetMusicDirectory) {
        System.out.println("Extracting music resources from JAR...");

        try {
            // Get list of music files from resources
            List<String> musicFiles = getMusicFilesFromResources();

            if (musicFiles.isEmpty()) {
                System.out.println("No music files found in JAR resources");
                return;
            }

            int extractedCount = 0;
            for (String musicFile : musicFiles) {
                if (extractMusicFile("music/" + musicFile, targetMusicDirectory.resolve(musicFile))) {
                    extractedCount++;
                }
            }

            System.out.println("Successfully extracted " + extractedCount + " music files to: " + targetMusicDirectory);

        } catch (Exception e) {
            System.err.println("Warning: Could not extract all music resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets a list of music files from the JAR resources.
     * You can either hardcode known files or scan the resources.
     */
    private static List<String> getMusicFilesFromResources() {
        // For now, let's try to extract based on common music file extensions
        // In a real implementation, you might want to scan the JAR or have a predefined list

        List<String> musicFiles = new ArrayList<>();

        // Try some common music files that might be in your resources
        String[] commonMusicFiles = {
                "fuego.mp3",
                "supermamielive.mp3",
                "echameelculpa.mp3",
                "echangelaculpa.mp3",
                "mayonesa.mp3",
                "palmae.mp3",
                "elballelive.mp3",
                // Add more files as needed...
        };

        for (String fileName : commonMusicFiles) {
            // Check if the resource exists
            try (InputStream stream = MusicResourceExtractor.class.getClassLoader()
                    .getResourceAsStream("music/" + fileName)) {
                if (stream != null) {
                    musicFiles.add(fileName);
                }
            } catch (IOException e) {
                // File doesn't exist, skip it
            }
        }

        return musicFiles;
    }

    /**
     * Alternative method to get ALL music files by scanning the JAR.
     * This is more comprehensive but requires additional JAR scanning logic.
     */
    private static List<String> scanMusicFilesInJar() {
        List<String> musicFiles = new ArrayList<>();

        try {
            // This is a more advanced approach to scan the JAR
            URI uri = MusicResourceExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI();

            if (uri.toString().endsWith(".jar")) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(uri), Collections.emptyMap())) {
                    Path musicPath = fileSystem.getPath("/music");

                    if (Files.exists(musicPath)) {
                        Files.walk(musicPath)
                                .filter(Files::isRegularFile)
                                .filter(path -> {
                                    String fileName = path.getFileName().toString().toLowerCase();
                                    return fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                                            fileName.endsWith(".flac") || fileName.endsWith(".ogg");
                                })
                                .forEach(path -> {
                                    String relativePath = musicPath.relativize(path).toString();
                                    musicFiles.add(relativePath.replace('\\', '/'));
                                });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not scan JAR for music files: " + e.getMessage());
            // Fall back to the predefined list method
            return getMusicFilesFromResources();
        }

        return musicFiles;
    }

    /**
     * Extracts a single music file from JAR to filesystem.
     */
    private static boolean extractMusicFile(String resourcePath, Path targetPath) {
        try {
            InputStream inputStream = MusicResourceExtractor.class.getClassLoader()
                    .getResourceAsStream(resourcePath);

            if (inputStream != null) {
                // Create parent directories if they don't exist
                Files.createDirectories(targetPath.getParent());

                // Copy the resource to the target location
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Extracted: " + resourcePath + " -> " + targetPath);
                inputStream.close();
                return true;
            } else {
                System.out.println("Music resource not found in JAR: " + resourcePath);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Failed to extract music resource " + resourcePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the absolute path for a music file, ensuring it's extracted if needed.
     */
    public static String getMusicFilePath(String fileName) {
        Path musicDir = ensureMusicDirectory();
        return musicDir.resolve(fileName).toAbsolutePath().toString();
    }

    /**
     * Checks if a music file exists in the extracted directory.
     */
    public static boolean musicFileExists(String fileName) {
        Path musicDir = ensureMusicDirectory();
        return Files.exists(musicDir.resolve(fileName));
    }

    /**
     * Lists all music files in the extracted directory.
     */
    public static List<String> listExtractedMusicFiles() {
        List<String> files = new ArrayList<>();
        Path musicDir = ensureMusicDirectory();

        try {
            if (Files.exists(musicDir)) {
                Files.walk(musicDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.getFileName().toString().toLowerCase();
                            return fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                                    fileName.endsWith(".flac") || fileName.endsWith(".ogg");
                        })
                        .forEach(path -> {
                            files.add(path.getFileName().toString());
                        });
            }
        } catch (IOException e) {
            System.err.println("Error listing music files: " + e.getMessage());
        }

        return files;
    }
}