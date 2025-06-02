package persistence;

import persistence.interfaces.UserRepositoryInterface;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserRepository implements UserRepositoryInterface for persisting User entities.
 * Follows pure repository pattern by focusing only on data access operations.
 */
public class UserRepository extends JsonRepository<User> implements UserRepositoryInterface {

    /**
     * Singleton instance of UserRepository.
     * Using a singleton ensures consistent access to user data throughout the application.
     */
    private static UserRepository instance;

    /**
     * Private constructor that initializes the User repository.
     *
     * The constructor configures the base JsonRepository with:
     * - The User entity class type
     * - The "users.json" storage file path
     * - A method reference to extract the user ID (User::getUserID)
     *
     */
    private UserRepository() {
        super(User.class, "users.json", User::getUserID);
    }

    /**
     * Gets the singleton instance of UserRepository.
     * Creates the instance if it doesn't exist yet, following the lazy initialization
     * approach to the Singleton pattern.
     *
     * @return The singleton instance of UserRepository
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /**
     * Finds a user by their username.
     *
     * This method performs a case-sensitive search for a user with the exact matching username.
     * Username uniqueness is enforced in the application, so this should return at most one user.
     *
     *
     * @param username The username to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Finds a user by their email address.
     *
     * This method performs a case-sensitive search for a user with the exact matching email.
     * Email uniqueness is enforced in the application, so this should return at most one user.
     *
     *
     * @param email The email address to search for
     * @return An Optional containing the user if found, or empty if not found
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Checks if a user with the specified ID exists.
     *
     * This is a convenience method that leverages the findById method from the parent class.
     *
     *
     * @param userId The user ID to check
     * @return true if a user with this ID exists, false otherwise
     */
    @Override
    public boolean userIdExists(int userId) {
        return findById(userId).isPresent();
    }

    /**
     * Checks if a username is already taken.
     *
     * This method performs a case-sensitive check to determine if any existing user
     * has the specified username. This is useful for enforcing username uniqueness
     * during user registration.
     *
     *
     * @param username The username to check
     * @return true if the username is already taken, false otherwise
     */
    @Override
    public boolean usernameExists(String username) {
        return findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    /**
     * Checks if an email address is already registered.
     *
     * This method performs a case-insensitive check to determine if any existing user
     * has the specified email address. This is useful for enforcing email uniqueness
     * during user registration or profile updates.
     *
     *
     * @param email The email address to check
     * @return true if the email is already registered, false otherwise
     */
    @Override
    public boolean emailExists(String email) {
        return findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    /**
     * Finds users by their first name.
     *
     * This method performs a case-insensitive search for users with a matching first name.
     * It handles null first names by excluding those users from the results.
     *
     *
     * @param firstName The first name to search for
     * @return A list of users with the matching first name, may be empty
     */
    @Override
    public List<User> findUsersByFirstName(String firstName) {
        return findAll().stream()
                .filter(user -> user.getFirstName() != null &&
                        user.getFirstName().equalsIgnoreCase(firstName))
                .collect(Collectors.toList());
    }

    /**
     * Finds users by their last name.
     *
     * This method performs a case-insensitive search for users with a matching last name.
     * It handles null last names by excluding those users from the results.
     *
     *
     * @param lastName The last name to search for
     * @return A list of users with the matching last name, may be empty
     */
    @Override
    public List<User> findUsersByLastName(String lastName) {
        return findAll().stream()
                .filter(user -> user.getLastName() != null &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    /**
     * Finds users by their full name (first name and last name).
     *
     * This method performs a case-insensitive search for users with both matching first and last names.
     * It handles null first or last names by excluding those users from the results.
     *
     *
     * @param firstName The first name to search for
     * @param lastName The last name to search for
     * @return A list of users with the matching full name, may be empty
     */
    @Override
    public List<User> findUsersByFullName(String firstName, String lastName) {
        return findAll().stream()
                .filter(user -> user.getFirstName() != null &&
                        user.getFirstName().equalsIgnoreCase(firstName) &&
                        user.getLastName() != null &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    /**
     * Finds all active users.
     *
     * Active users are those with the 'active' flag set to true.
     * This is useful for filtering out deactivated or suspended accounts.
     *
     *
     * @return A list of all active users, may be empty
     */
    @Override
    public List<User> findActiveUsers() {
        return findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Finds all inactive users.
     *
     * Inactive users are those with the 'active' flag set to false.
     * This is useful for administrative tasks like managing deactivated accounts.
     *
     *
     * @return A list of all inactive users, may be empty
     */
    @Override
    public List<User> findInactiveUsers() {
        return findAll().stream()
                .filter(user -> !user.isActive())
                .collect(Collectors.toList());
    }

    /**
     * Gets the IDs of all followers for a specific user.
     *
     * Followers are users who follow the specified user.
     * If the user is not found or has no followers, an empty list is returned.
     *
     *
     * @param userId The ID of the user whose followers to retrieve
     * @return A list of user IDs representing the followers, may be empty
     */
    @Override
    public List<Integer> getFollowerIds(int userId) {
        Optional<User> userOpt = findById(userId);
        return userOpt.map(User::getFollowersIDs).orElse(new ArrayList<>());
    }

    /**
     * Gets the IDs of all users that a specific user follows.
     *
     * These are the users that the specified user follows.
     * If the user is not found or doesn't follow anyone, an empty list is returned.
     *
     *
     * @param userId The ID of the user whose followed users to retrieve
     * @return A list of user IDs representing the followed users, may be empty
     */
    @Override
    public List<Integer> getFollowedUserIds(int userId) {
        Optional<User> userOpt = findById(userId);
        return userOpt.map(User::getFollowedUsersIDs).orElse(new ArrayList<>());
    }

    /**
     * Adds a follower relationship between two users.
     *
     * This method establishes a bidirectional following relationship:
     * - The follower is added to the target user's followers list
     * - The target user is added to the follower's following list
     *
     * If either user doesn't exist, or if the relationship already exists,
     * the operation fails.
     *
     *
     * @param userId The ID of the user being followed
     * @param followerId The ID of the follower
     * @return true if the relationship was successfully established, false otherwise
     */
    @Override
    public boolean addFollower(int userId, int followerId) {
        Optional<User> userOpt = findById(userId);
        Optional<User> followerOpt = findById(followerId);

        if (userOpt.isEmpty() || followerOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        User follower = followerOpt.get();

        List<Integer> followers = user.getFollowersIDs();
        if (followers == null) {
            followers = new ArrayList<>();
            user.setFollowersIDs(followers);
        }

        List<Integer> following = follower.getFollowedUsersIDs();
        if (following == null) {
            following = new ArrayList<>();
            follower.setFollowedUsersIDs(following);
        }

        if (!followers.contains(followerId)) {
            followers.add(followerId);
            following.add(userId);

            update(user);
            update(follower);
            return true;
        }

        return false;
    }

    /**
     * Removes a follower relationship between two users.
     *
     * This method removes a bidirectional following relationship:
     * - The follower is removed from the target user's followers list
     * - The target user is removed from the follower's following list
     *
     * If either user doesn't exist, or if the relationship doesn't exist,
     * the operation fails.
     *
     *
     * @param userId The ID of the user being unfollowed
     * @param followerId The ID of the follower to remove
     * @return true if the relationship was successfully removed, false otherwise
     */
    @Override
    public boolean removeFollower(int userId, int followerId) {
        Optional<User> userOpt = findById(userId);
        Optional<User> followerOpt = findById(followerId);

        if (userOpt.isEmpty() || followerOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        User follower = followerOpt.get();

        List<Integer> followers = user.getFollowersIDs();
        List<Integer> following = follower.getFollowedUsersIDs();

        boolean removed = false;

        if (followers != null && followers.remove(Integer.valueOf(followerId))) {
            removed = true;
        }

        if (following != null && following.remove(Integer.valueOf(userId))) {
            removed = true;
        }

        if (removed) {
            update(user);
            update(follower);
            return true;
        }

        return false;
    }

    /**
     * Saves a new user to the repository.
     *
     * This method delegates to the parent class implementation without additional validation.
     * Validation is handled in the service layer, following the separation of concerns principle.
     *
     *
     * @param user The user to save
     * @return The saved user (may include generated values)
     */
    @Override
    public User save(User user) {
        // No validation here - moved to service layer
        return super.save(user);
    }

    /**
     * Updates an existing user in the repository.
     *
     * This method delegates to the parent class implementation without additional validation.
     * Validation is handled in the service layer, following the separation of concerns principle.
     *
     *
     * @param user The user with updated values
     * @return An Optional containing the updated user if successful, or empty if not found
     */
    @Override
    public Optional<User> update(User user) {
        // No validation here - moved to service layer
        return super.update(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * This method delegates to the parent class implementation.
     *
     *
     * @param userId The ID of the user to delete
     * @return true if the user was found and deleted, false otherwise
     */
    @Override
    public boolean deleteById(int userId) {
        return super.deleteById(userId);
    }
}