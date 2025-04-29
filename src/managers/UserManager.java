package managers;

import persistence.UserRepository;
import user.User;

import java.util.List;

/**
 * Manages user-related operations such as retrieving user information
 * and handling followers and followed users.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class UserManager {

    /**
     * Singleton instance of UserManager.
     */
    private static UserManager instance;

    /**
     * Repository for accessing user data.
     */
    private final UserRepository userRepository;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the UserRepository instance.
     */
    private UserManager() {
        this.userRepository = UserRepository.getInstance();
    }

    /**
     * Returns the single instance of UserManager, creating it if it doesn't exist.
     *
     * @return the singleton instance of UserManager
     */
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id the ID of the user
     * @return the User object
     * @throws IllegalArgumentException if no user with the specified ID exists
     */
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No user with ID=" + id));
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return the User object
     * @throws IllegalArgumentException if no user with the specified username exists
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user with username='" + username + "'"));
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email of the user
     * @return the User object
     * @throws IllegalArgumentException if no user with the specified email exists
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user with email='" + email + "'"));
    }

    /**
     * Retrieves the list of followers for the specified user.
     *
     * @param username the username of the user
     * @return the list of users who follow the specified user
     */
    public List<User> getFollowers(String username) {
        User user = getUserByUsername(username);
        return userRepository.findFollowers(user);
    }

    /**
     * Retrieves the list of users that the specified user is following.
     *
     * @param username the username of the user
     * @return the list of users followed by the specified user
     */
    public List<User> getFollowedUsers(String username) {
        User user = getUserByUsername(username);
        return userRepository.findFollowedUsers(user);
    }

}
