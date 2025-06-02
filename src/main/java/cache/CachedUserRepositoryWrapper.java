package cache;

import persistence.interfaces.UserRepositoryInterface;
import user.User;
import java.util.List;
import java.util.Optional;

/**
 * A wrapper for the User repository that selectively applies caching to basic CRUD operations.
 * This class implements the UserRepositoryInterface and uses a hybrid approach:
 * - Basic CRUD operations are delegated to a CachedRepository for performance
 * - Specialized query and social operations are delegated to the original repository implementation
 *
 * This wrapper follows both the Decorator and Adapter patterns:
 * - Decorator: It adds caching behavior to certain operations
 * - Adapter: It adapts the CachedRepository to the full UserRepositoryInterface contract
 *
 */
public class CachedUserRepositoryWrapper implements UserRepositoryInterface {
    private final CachedRepository<User> cachedRepo;
    private final UserRepositoryInterface original;

    /**
     * Constructs a new CachedUserRepositoryWrapper with the specified cached repository
     * and original user repository.
     *
     * @param cachedRepo The cached repository for handling basic CRUD operations
     * @param original The original repository for handling specialized queries
     */
    public CachedUserRepositoryWrapper(CachedRepository<User> cachedRepo, UserRepositoryInterface original) {
        this.cachedRepo = cachedRepo;
        this.original = original;
    }

    /**
     * Retrieves all users from the cached repository.
     *
     * @return A list of all users
     */
    // Basic CRUD operations delegate to CachedRepository
    @Override
    public List<User> findAll() {
        return cachedRepo.findAll();
    }

    /**
     * Retrieves a user by their ID from the cached repository.
     *
     * @param id The unique identifier of the user
     * @return An Optional containing the user if found, or empty if not found
     */
    @Override
    public Optional<User> findById(int id) {
        return cachedRepo.findById(id);
    }

    /**
     * Saves a new user to both the cached repository and the original repository.
     *
     * @param user The user to save
     * @return The saved user (may contain generated values)
     */
    @Override
    public User save(User user) {
        return cachedRepo.save(user);
    }

    /**
     * Saves multiple users to both the cached repository and the original repository.
     *
     * @param users The list of users to save
     */
    @Override
    public void saveAll(List<User> users) {
        cachedRepo.saveAll(users);
    }

    /**
     * Updates an existing user in both the cached repository and the original repository.
     *
     * @param user The user with updated values
     * @return An Optional containing the updated user if successful, or empty if not found
     */
    @Override
    public Optional<User> update(User user) {
        return cachedRepo.update(user);
    }

    /**
     * Deletes a user by their ID from both the cached repository and the original repository.
     *
     * @param id The unique identifier of the user to delete
     * @return true if the user was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    /**
     * Finds a user by their username directly from the original repository (not cached).
     * This specialized query bypasses the cache for freshness and simplicity.
     *
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    // Special methods with filtering (no cache)
    @Override
    public Optional<User> findByUsername(String username) {
        return original.findByUsername(username);
    }

    /**
     * Finds a user by their email directly from the original repository (not cached).
     *
     * @param email The email to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return original.findByEmail(email);
    }

    /**
     * Checks if a user ID exists directly from the original repository.
     *
     * @param userId The user ID to check
     * @return true if the user ID exists, false otherwise
     */
    @Override
    public boolean userIdExists(int userId) {
        return original.userIdExists(userId);
    }

    /**
     * Checks if a username exists directly from the original repository.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    @Override
    public boolean usernameExists(String username) {
        return original.usernameExists(username);
    }

    /**
     * Checks if an email exists directly from the original repository.
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    @Override
    public boolean emailExists(String email) {
        return original.emailExists(email);
    }

    /**
     * Finds users by their first name directly from the original repository.
     *
     * @param firstName The first name to search for
     * @return A list of users with the specified first name
     */
    @Override
    public List<User> findUsersByFirstName(String firstName) {
        return original.findUsersByFirstName(firstName);
    }

    /**
     * Finds users by their last name directly from the original repository.
     *
     * @param lastName The last name to search for
     * @return A list of users with the specified last name
     */
    @Override
    public List<User> findUsersByLastName(String lastName) {
        return original.findUsersByLastName(lastName);
    }

    /**
     * Finds users by their full name directly from the original repository.
     *
     * @param firstName The first name to search for
     * @param lastName The last name to search for
     * @return A list of users with the specified full name
     */
    @Override
    public List<User> findUsersByFullName(String firstName, String lastName) {
        return original.findUsersByFullName(firstName, lastName);
    }

    /**
     * Finds active users directly from the original repository.
     *
     * @return A list of active users
     */
    @Override
    public List<User> findActiveUsers() {
        return original.findActiveUsers();
    }

    /**
     * Finds inactive users directly from the original repository.
     *
     * @return A list of inactive users
     */
    @Override
    public List<User> findInactiveUsers() {
        return original.findInactiveUsers();
    }

    /**
     * Gets the follower IDs for a user directly from the original repository.
     *
     * @param userId The ID of the user whose followers to retrieve
     * @return A list of user IDs who follow the specified user
     */
    @Override
    public List<Integer> getFollowerIds(int userId) {
        return original.getFollowerIds(userId);
    }

    /**
     * Gets the IDs of users that a user follows directly from the original repository.
     *
     * @param userId The ID of the user whose followed users to retrieve
     * @return A list of user IDs the specified user follows
     */
    @Override
    public List<Integer> getFollowedUserIds(int userId) {
        return original.getFollowedUserIds(userId);
    }

    /**
     * Adds a follower relationship directly through the original repository.
     * This social operation modifies relationship data and bypasses the cache.
     *
     * @param userId The ID of the user being followed
     * @param followerId The ID of the follower
     * @return true if the relationship was successfully added, false otherwise
     */
    @Override
    public boolean addFollower(int userId, int followerId) {
        return original.addFollower(userId, followerId);
    }

    /**
     * Removes a follower relationship directly through the original repository.
     *
     * @param userId The ID of the user being unfollowed
     * @param followerId The ID of the follower
     * @return true if the relationship was successfully removed, false otherwise
     */
    @Override
    public boolean removeFollower(int userId, int followerId) {
        return original.removeFollower(userId, followerId);
    }
}