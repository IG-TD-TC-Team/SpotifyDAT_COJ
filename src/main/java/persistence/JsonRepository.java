package persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import persistence.interfaces.Repository;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Abstract implementation of the Repository interface for persisting entities as JSON.
 *
 * @param <T> The domain model type the repository manages
 */
public abstract class JsonRepository<T> implements Repository<T> {
    // The Class type of T, used by Jackson for serialization/deserialization
    protected final Class<T> entityType;

    // The path where the JSON file is stored
    protected final Path storagePath;

    // Jackson's ObjectMapper for converting objects to/from JSON
    protected final ObjectMapper mapper;

    // Function to extract the ID from an entity
    protected final ToIntFunction<T> idExtractor;

    /**
     * Constructor that initializes the repository.
     *
     * @param entityType   The class type of the entities this repository handles
     * @param filename     The name of the JSON file to store entities
     * @param idExtractor  A function to extract the ID from an entity
     */
    protected JsonRepository(Class<T> entityType, String filename, ToIntFunction<T> idExtractor) {
        this.entityType = entityType;
        this.storagePath = Paths.get("data", filename);
        this.mapper = JacksonConfig.getConfiguredMapper();
        this.idExtractor = idExtractor;

        try {
            // Ensure the parent directory exists
            Files.createDirectories(storagePath.getParent());

            // If the file doesn't exist, create it and initialize with an empty JSON array
            if (!Files.exists(storagePath)) {
                Files.writeString(storagePath, "[]");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize repository", e);
        }
    }

    /// ------------READ------- ///
    @Override
    public List<T> findAll() {
        try {
            String content = Files.readString(storagePath);
            return mapper.readValue(content,
                    mapper.getTypeFactory().constructCollectionType(List.class, entityType));
        } catch (IOException e) {
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

    /// ---------CREATE------ ///
    @Override
    public T save(T entity) {
        List<T> entities = findAll();
        int entityId = idExtractor.applyAsInt(entity);

        // Check if entity with this ID already exists
        boolean exists = entities.stream()
                .anyMatch(e -> idExtractor.applyAsInt(e) == entityId);

        if (exists) {
            // Notify about duplicate entity
            System.out.println("NOTIFICATION: Entity of type " + entityType.getSimpleName() +
                    " with ID " + entityId + " already exists. Updating instead of creating new.");

            // Update the existing entity
            return update(entity).orElse(entity);
        }

        // If it doesn't exist, add it
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
            throw new RuntimeException("Failed to save entities", e);
        }
    }

    /// ---------UPDATE------ ///
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

    /// ---------DELETE------ ///
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