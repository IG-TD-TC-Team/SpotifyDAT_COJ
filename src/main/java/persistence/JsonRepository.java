package persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import persistence.interfaces.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * Abstract implementation of the Repository interface for persisting entities as JSON.
 * Automatically detects development vs production environment and handles data folder accordingly.
 *
 * @param <T> The domain model type the repository manages
 */
public abstract class JsonRepository<T> implements Repository<T> {
    protected final Class<T> entityType;
    protected final Path storagePath;
    protected final ObjectMapper mapper;
    protected final ToIntFunction<T> idExtractor;

    // Static flag to ensure resource extraction happens only once
    private static boolean resourcesExtracted = false;

    protected JsonRepository(Class<T> entityType, String filename, ToIntFunction<T> idExtractor) {
        this.entityType = entityType;
        this.mapper = JacksonConfig.getConfiguredMapper();
        this.idExtractor = idExtractor;

        // Determine the appropriate data directory
        Path dataDirectory = determineDataDirectory();
        this.storagePath = dataDirectory.resolve(filename);

        try {
            // Ensure the data directory exists
            Files.createDirectories(dataDirectory);

            // Extract resources if running from JAR and not already extracted
            if (!resourcesExtracted && isRunningFromJar()) {
                extractDataResources(dataDirectory);
                resourcesExtracted = true;
            }

            // If the file doesn't exist, create it and initialize with an empty JSON array
            if (!Files.exists(storagePath)) {
                Files.writeString(storagePath, "[]");
                System.out.println("Created new JSON file: " + storagePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize repository for " + filename, e);
        }
    }

    /**
     * Determines the appropriate data directory based on the execution context.
     */
    private Path determineDataDirectory() {
        if (isRunningFromJar()) {
            // Production: Use data folder next to JAR
            Path jarPath = getJarPath();
            Path dataDir = jarPath.getParent().resolve("data");
            System.out.println("Production mode: Using data directory: " + dataDir);
            return dataDir;
        } else {
            // Development: Use data folder in current working directory
            Path dataDir = Paths.get("data");
            System.out.println("Development mode: Using data directory: " + dataDir);
            return dataDir;
        }
    }

    /**
     * Checks if the application is running from a JAR file.
     */
    private boolean isRunningFromJar() {
        try {
            URI uri = JsonRepository.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            return uri.toString().endsWith(".jar");
        } catch (URISyntaxException e) {
            System.err.println("Warning: Could not determine if running from JAR: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the path to the JAR file.
     */
    private Path getJarPath() {
        try {
            URI uri = JsonRepository.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            return Paths.get(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not determine JAR path", e);
        }
    }

    /**
     * Extracts data resources from JAR to external filesystem.
     */
    private void extractDataResources(Path targetDataDirectory) {
        System.out.println("Extracting data resources from JAR...");

        try {
            // List of JSON files to extract
            String[] dataFiles = {
                    "songs.json",
                    "artists.json",
                    "albums.json",
                    "playlists.json",
                    "users.json",
                    "Library.json"
            };

            for (String filename : dataFiles) {
                extractResourceFile("data/" + filename, targetDataDirectory.resolve(filename));
            }

            System.out.println("Successfully extracted data resources to: " + targetDataDirectory);
        } catch (Exception e) {
            System.err.println("Warning: Could not extract all data resources: " + e.getMessage());
            // Don't throw exception - let the application create empty files if needed
        }
    }

    /**
     * Extracts a single resource file from JAR to filesystem.
     */
    private void extractResourceFile(String resourcePath, Path targetPath) {
        try {
            // Try to read the resource from the classpath
            var inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (inputStream != null) {
                // Create parent directories if they don't exist
                Files.createDirectories(targetPath.getParent());

                // Copy the resource to the target location
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Extracted: " + resourcePath + " -> " + targetPath);
                inputStream.close();
            } else {
                System.out.println("Resource not found in JAR: " + resourcePath + " (will create empty file if needed)");
            }
        } catch (IOException e) {
            System.err.println("Failed to extract resource " + resourcePath + ": " + e.getMessage());
        }
    }

    // ... rest of the methods remain the same ...

    @Override
    public List<T> findAll() {
        try {
            String content = Files.readString(storagePath);
            return mapper.readValue(content,
                    mapper.getTypeFactory().constructCollectionType(List.class, entityType));
        } catch (IOException e) {
            System.err.println("Error reading from " + storagePath + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<T> findById(int id) {
        return findAll().stream()
                .filter(entity -> idExtractor.applyAsInt(entity) == id)
                .findFirst();
    }

    @Override
    public T save(T entity) {
        List<T> entities = findAll();
        int entityId = idExtractor.applyAsInt(entity);

        boolean exists = entities.stream()
                .anyMatch(e -> idExtractor.applyAsInt(e) == entityId);

        if (exists) {
            System.out.println("NOTIFICATION: Entity of type " + entityType.getSimpleName() +
                    " with ID " + entityId + " already exists. Updating instead of creating new.");
            return update(entity).orElse(entity);
        }

        entities.add(entity);
        saveAll(entities);
        return entity;
    }

    @Override
    public void saveAll(List<T> entities) {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entities);
            Files.writeString(storagePath, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save entities to " + storagePath, e);
        }
    }

    @Override
    public Optional<T> update(T updatedEntity) {
        List<T> entities = findAll();
        int entityId = idExtractor.applyAsInt(updatedEntity);

        boolean found = false;
        for (int i = 0; i < entities.size(); i++) {
            if (idExtractor.applyAsInt(entities.get(i)) == entityId) {
                entities.set(i, updatedEntity);
                found = true;
                break;
            }
        }

        if (found) {
            saveAll(entities);
            return Optional.of(updatedEntity);
        }

        return Optional.empty();
    }

    @Override
    public boolean deleteById(int id) {
        List<T> entities = findAll();
        boolean removed = entities.removeIf(entity -> idExtractor.applyAsInt(entity) == id);

        if (removed) {
            saveAll(entities);
        }

        return removed;
    }
}