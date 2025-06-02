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
    /**
     * The Class type of T, used by Jackson for serialization/deserialization.
     * This allows Jackson to correctly convert between JSON and the specific entity type.
     */
    // The Class type of T, used by Jackson for serialization/deserialization
    protected final Class<T> entityType;

    /**
     * The path where the JSON file is stored.
     * All repository files are stored in a 'data' directory with a specific filename
     * for each entity type.
     */
    // The path where the JSON file is stored
    protected final Path storagePath;

    /**
     * Jackson's ObjectMapper for converting objects to/from JSON.
     * This mapper is configured by the JacksonConfig class to handle the
     * specific serialization needs of the application.
     */
    // Jackson's ObjectMapper for converting objects to/from JSON
    protected final ObjectMapper mapper;

    /**
     * Function to extract the ID from an entity.
     * This allows the repository to identify entities without knowing
     * their specific implementation details.
     */
    // Function to extract the ID from an entity
    protected final ToIntFunction<T> idExtractor;

    /**
     * Constructor that initializes the repository.
     *
     * @param entityType   The class type of the entities this repository handles
     * @param filename     The name of the JSON file to store entities
     * @param idExtractor  A function to extract the ID from an entity
     * @throws RuntimeException if the repository initialization fails
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
    /**
     * Retrieves all entities from the repository.
     *
     * This method reads the entire JSON file and deserializes it into a list of entities.
     * If any errors occur during reading or deserialization, an empty list is returned
     * and the error is logged.
     *
     *
     * @return A list of all entities in the repository
     */
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

    /**
     * Finds an entity by its ID.
     *
     * This method streams through all entities and returns the first one with a matching ID.
     * The ID is extracted using the idExtractor function provided in the constructor.
     *
     *
     * @param id The ID of the entity to find
     * @return An Optional containing the entity if found, or empty if not found
     */
    @Override
    public Optional<T> findById(int id) {
        return findAll().stream()
                .filter(entity -> idExtractor.applyAsInt(entity) == id)
                .findFirst();
    }

    /// ---------CREATE------ ///
    /**
     * Saves a new entity to the repository or updates an existing one.
     *
     * If an entity with the same ID already exists, the existing entity is updated
     * instead of creating a new one. This prevents duplicate entities with the same ID.
     *
     *
     * @param entity The entity to save
     * @return The saved entity (may include generated values)
     */
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

    /**
     * Saves multiple entities to the repository.
     *
     * This method overwrites the entire JSON file with the provided list of entities.
     * It uses Jackson's pretty printer to format the JSON for better readability.
     *
     *
     * @param entities The list of entities to save
     * @throws RuntimeException if the entities cannot be saved
     */
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
    /**
     * Updates an existing entity in the repository.
     *
     * This method finds the entity with the matching ID in the repository and
     * replaces it with the updated entity. If no entity with the matching ID is
     * found, an empty Optional is returned.
     *
     *
     * @param updatedEntity The entity with updated values
     * @return An Optional containing the updated entity if successful, or empty if not found
     */
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
    /**
     * Deletes an entity by its ID.
     *
     * This method removes the entity with the matching ID from the repository.
     * If the entity is found and removed, the changes are saved to the JSON file.
     *
     *
     * @param id The ID of the entity to delete
     * @return true if the entity was found and deleted, false otherwise
     */
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