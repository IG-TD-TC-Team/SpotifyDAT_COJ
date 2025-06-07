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
 * Extracts music resources from JAR to external filesystem.
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
            // First try the comprehensive JAR scanning method
            List<String> musicFiles = scanMusicFilesInJar();

            // If JAR scanning fails or returns empty, fall back to directory scanning
            if (musicFiles.isEmpty()) {
                System.out.println("JAR scanning returned no files, trying alternative method...");
                musicFiles = getMusicFilesFromResourcesDirectory();
            }

            if (musicFiles.isEmpty()) {
                System.out.println("No music files found in JAR resources");
                return;
            }

            System.out.println("Found " + musicFiles.size() + " music files to extract:");
            musicFiles.forEach(file -> System.out.println("  - " + file));

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
     * Scans the JAR file to find all music files in the /music directory.
     * This is the most comprehensive method for finding all music files.
     */
    private static List<String> scanMusicFilesInJar() {
        List<String> musicFiles = new ArrayList<>();

        try {
            URI uri = MusicResourceExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI();

            if (uri.toString().endsWith(".jar")) {
                System.out.println("Scanning JAR file for music files: " + uri);

                try (FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(uri), Collections.emptyMap())) {
                    Path musicPath = fileSystem.getPath("/music");

                    if (Files.exists(musicPath)) {
                        System.out.println("Found /music directory in JAR, scanning contents...");

                        Files.walk(musicPath)
                                .filter(Files::isRegularFile)
                                .filter(path -> {
                                    String fileName = path.getFileName().toString().toLowerCase();
                                    return fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                                            fileName.endsWith(".flac") || fileName.endsWith(".ogg");
                                })
                                .forEach(path -> {
                                    String relativePath = musicPath.relativize(path).toString();
                                    // Normalize path separators to forward slashes
                                    String normalizedPath = relativePath.replace('\\', '/');
                                    musicFiles.add(normalizedPath);
                                    System.out.println("  Found music file: " + normalizedPath);
                                });
                    } else {
                        System.out.println("No /music directory found in JAR");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not scan JAR for music files: " + e.getMessage());
            e.printStackTrace();
        }

        return musicFiles;
    }

    /**
     * Alternative method to find music files by checking the classpath resources.
     * This method tries a different approach when JAR scanning fails.
     */
    private static List<String> getMusicFilesFromResourcesDirectory() {
        List<String> musicFiles = new ArrayList<>();

        try {
            // Try to get the music directory as a resource
            URI musicUri = MusicResourceExtractor.class.getClassLoader().getResource("music").toURI();

            if ("jar".equals(musicUri.getScheme())) {
                // Handle JAR file system
                String[] parts = musicUri.toString().split("!");
                URI jarUri = URI.create(parts[0]);

                try (FileSystem jarFs = FileSystems.newFileSystem(jarUri, Collections.emptyMap())) {
                    Path musicPath = jarFs.getPath(parts[1]);

                    if (Files.exists(musicPath)) {
                        Files.list(musicPath)
                                .filter(Files::isRegularFile)
                                .filter(path -> {
                                    String fileName = path.getFileName().toString().toLowerCase();
                                    return fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                                            fileName.endsWith(".flac") || fileName.endsWith(".ogg");
                                })
                                .forEach(path -> {
                                    String fileName = path.getFileName().toString();
                                    musicFiles.add(fileName);
                                    System.out.println("  Found music file via resources: " + fileName);
                                });
                    }
                }
            } else {
                // Handle regular file system (development mode)
                Path musicPath = Paths.get(musicUri);
                if (Files.exists(musicPath)) {
                    Files.list(musicPath)
                            .filter(Files::isRegularFile)
                            .filter(path -> {
                                String fileName = path.getFileName().toString().toLowerCase();
                                return fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                                        fileName.endsWith(".flac") || fileName.endsWith(".ogg");
                            })
                            .forEach(path -> {
                                String fileName = path.getFileName().toString();
                                musicFiles.add(fileName);
                                System.out.println("  Found music file: " + fileName);
                            });
                }
            }
        } catch (Exception e) {
            System.err.println("Could not scan resources for music files: " + e.getMessage());
            e.printStackTrace();
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

    /**
     * Debug method to list all resources that can be found in the music directory.
     * Useful for troubleshooting what files are actually available in the JAR.
     */
    public static void debugListMusicResources() {
        System.out.println("=== DEBUG: Music Resources Analysis ===");

        // Method 1: Try JAR scanning
        System.out.println("Method 1 - JAR Scanning:");
        List<String> jarFiles = scanMusicFilesInJar();
        if (jarFiles.isEmpty()) {
            System.out.println("  No files found via JAR scanning");
        } else {
            jarFiles.forEach(file -> System.out.println("  JAR: " + file));
        }

        // Method 2: Try resource directory scanning
        System.out.println("Method 2 - Resource Directory Scanning:");
        List<String> resourceFiles = getMusicFilesFromResourcesDirectory();
        if (resourceFiles.isEmpty()) {
            System.out.println("  No files found via resource scanning");
        } else {
            resourceFiles.forEach(file -> System.out.println("  Resource: " + file));
        }

        // Method 3: Check if music directory resource exists
        System.out.println("Method 3 - Resource Existence Check:");
        try {
            URI musicUri = MusicResourceExtractor.class.getClassLoader().getResource("music").toURI();
            System.out.println("  Music resource URI: " + musicUri);
        } catch (Exception e) {
            System.out.println("  Music resource not found: " + e.getMessage());
        }

        System.out.println("=== End Debug Analysis ===");
    }
}