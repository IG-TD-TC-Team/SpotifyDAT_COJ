package persistence.interfaces;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository interface that defines the standard operations
 * to be executed on a repository.
 *
 * @param <T> The domain model type the repository manages
 */
public interface Repository<T> {

    /// -------READ------- ///
    /**
     * Finds all entities in the repository.
     *
     * @return A list of all entities
     */
    List<T> findAll();

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id The unique identifier of the entity
     * @return An Optional containing the entity if found, or empty if not found
     */
    Optional<T> findById(int id);

    /// ---------CREATE------ ///
    /**
     * Saves a new entity to the repository.
     *
     * @param entity The entity to save
     * @return The saved entity (may include generated values)
     */
    T save(T entity);

    /**
     * Saves all entities to the repository.
     *
     * @param entities The list of entities to save
     */
    void saveAll(List<T> entities);

    /// ---------UPDATE------ ///
    /**
     * Updates an existing entity in the repository.
     *
     * @param entity The entity with updated values
     * @return The updated entity, or empty if not found
     */
    Optional<T> update(T entity);

    /// ---------DELETE------ ///
    /**
     * Deletes an entity by its unique identifier.
     *
     * @param id The unique identifier of the entity to delete
     * @return true if the entity was found and deleted, false otherwise
     */
    boolean deleteById(int id);
}