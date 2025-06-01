package persistence.interfaces;

import user.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 */
public interface UserRepositoryInterface extends Repository<User> {

    /**
     * Finds a user by username.
     *
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email.
     *
     * @param email The email to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns true if a user with this ID exists.
     */
    boolean userIdExists(int userId);

    /**
     * Checks if a username already exists.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean usernameExists(String username);

    /**
     * Checks if an email already exists.
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    boolean emailExists(String email);

    /**
     * Finds users by first name.
     *
     * @param firstName The first name to search for
     * @return A list of users with the matching first name
     */
    List<User> findUsersByFirstName(String firstName);

    /**
     * Finds users by last name.
     *
     * @param lastName The last name to search for
     * @return A list of users with the matching last name
     */
    List<User> findUsersByLastName(String lastName);


    /**
     * Finds users by full name (first and last name).
     *
     * @param firstName The first name to search for
     * @param lastName The last name to search for
     * @return A list of users with the matching full name
     */
    List<User> findUsersByFullName(String firstName, String lastName);

    /**
     * Finds all active users.
     */
    List<User> findActiveUsers();

    /**
     * Finds all inactive users.
     */
    List<User> findInactiveUsers();

    /**
     * Gets all followers of a user by user ID.
     */
    List<Integer> getFollowerIds(int userId);

    /**
     * Gets all users that a user follows by user ID.
     */
    List<Integer> getFollowedUserIds(int userId);

    /**
     * Adds a follower relationship.
     */
    boolean addFollower(int userId, int followerId);

    /**
     * Removes a follower relationship.
     */
    boolean removeFollower(int userId, int followerId);
}