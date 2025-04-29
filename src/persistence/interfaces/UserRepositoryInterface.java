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
     * Validates user credentials by username.
     *
     * @param username The username to check
     * @param password The password to check
     * @return true if the credentials are valid, false otherwise
     */
    boolean checkCredentialsByUsername(String username, String password);

    /**
     * Validates user credentials by email.
     *
     * @param email The email to check
     * @param password The password to check
     * @return true if the credentials are valid, false otherwise
     */
    boolean checkCredentialsByEmail(String email, String password);

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
     * Gets all followers of a user.
     *
     * @param user The user whose followers to retrieve
     * @return A list of users who follow the specified user
     */
    List<User> findFollowers(User user);

    /**
     * Gets all users that a user follows.
     *
     * @param user The user whose followed users to retrieve
     * @return A list of users followed by the specified user
     */
    List<User> findFollowedUsers(User user);

    /**
     * Finds a follower by username.
     *
     * @param user The user whose followers to search
     * @param username The username of the follower to find
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> findFollowerByUsername(User user, String username);

    /**
     * Finds followers by first name.
     *
     * @param user The user whose followers to search
     * @param firstName The first name to search for
     * @return A list of followers with the matching first name
     */
    List<User> findFollowersByFirstName(User user, String firstName);

    /**
     * Finds followers by last name.
     *
     * @param user The user whose followers to search
     * @param lastName The last name to search for
     * @return A list of followers with the matching last name
     */
    List<User> findFollowersByLastName(User user, String lastName);

    /**
     * Finds followers by full name.
     *
     * @param user The user whose followers to search
     * @param firstName The first name to search for
     * @param lastName The last name to search for
     * @return A list of followers with the matching full name
     */
    List<User> findFollowersByFullName(User user, String firstName, String lastName);

    /**
     * Finds followed users by first name.
     *
     * @param user The user whose followed users to search
     * @param firstName The first name to search for
     * @return A list of followed users with the matching first name
     */
    List<User> findFollowedUsersByFirstName(User user, String firstName);

    /**
     * Finds followed users by last name.
     *
     * @param user The user whose followed users to search
     * @param lastName The last name to search for
     * @return A list of followed users with the matching last name
     */
    List<User> findFollowedUsersByLastName(User user, String lastName);

    /**
     * Finds followed users by full name.
     *
     * @param user The user whose followed users to search
     * @param firstName The first name to search for
     * @param lastName The last name to search for
     * @return A list of followed users with the matching full name
     */
    List<User> findFollowedUsersByFullName(User user, String firstName, String lastName);

    /**
     * Finds a followed user by username.
     *
     * @param user The user whose followed users to search
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    Optional<User> findFollowedUserByUsername(User user, String username);
}