package services.userServices;

import factory.RepositoryFactory;
import persistence.interfaces.UserRepositoryInterface;
import services.userServices.exceptions.DuplicateUserException;
import services.userServices.exceptions.UserNotFoundException;
import services.userServices.exceptions.UserValidationException;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages user-related operations such as retrieving user information
 * and handling followers and followed users.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class UserService {

    /**
     * Singleton instance of UserService.
     */
    private static UserService instance;

    /**
     * Repository for accessing user data.
     */
    private final UserRepositoryInterface userRepository;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the UserRepository instance using RepositoryFactory.
     */
    private UserService(UserRepositoryInterface userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the single instance of UserService, creating it if it doesn't exist.
     *
     * @return the singleton instance of UserService
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService(RepositoryFactory.getUserRepository());
        }
        return instance;
    }

    /**
     * Refreshes the repository data with the latest changes.
     * Used after operations that modify user data in other services.
     */
    public void refreshCache() {
        // We'll implement caching in a future iteration
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the ID of the user
     * @return the User object
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public User getUserById(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        return userOpt.get();
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return the User object
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public User getUserByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }
        return userOpt.get();
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email of the user
     * @return the User object
     * @throws UserNotFoundException if no user with the specified email exists
     */
    public User getUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("email", email);
        }
        return userOpt.get();
    }

    /**
     * Updates a user's basic profile information.
     *
     * @param userId the ID of the user to update
     * @param firstName the new first name (or null to leave unchanged)
     * @param lastName the new last name (or null to leave unchanged)
     * @return the updated User object
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public User updateUserProfile(int userId, String firstName, String lastName) {
        User user = getUserById(userId);

        if (firstName != null) {
            user.setFirstName(firstName);
        }

        if (lastName != null) {
            user.setLastName(lastName);
        }

        Optional<User> updatedUserOpt = userRepository.update(user);
        if (updatedUserOpt.isEmpty()) {
            throw new RuntimeException("Failed to update user profile");
        }

        return updatedUserOpt.get();
    }

    /**
     * Updates a user's basic profile information.
     *
     * @param username the username of the user to update
     * @param firstName the new first name (or null to leave unchanged)
     * @param lastName the new last name (or null to leave unchanged)
     * @return the updated User object
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public User updateUserProfile(String username, String firstName, String lastName) {
        User user = getUserByUsername(username);
        return updateUserProfile(user.getUserID(), firstName, lastName);
    }

    /**
     * Updates a user's username.
     *
     * @param userId the ID of the user to update
     * @param newUsername the new username
     * @return the updated User object
     * @throws UserNotFoundException if no user with the specified ID exists
     * @throws DuplicateUserException if the new username is already in use
     */
    public User updateUsername(int userId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new UserValidationException("username", "Username cannot be empty");
        }

        // Check if username is already taken
        if (userRepository.usernameExists(newUsername)) {
            throw new DuplicateUserException("username", newUsername);
        }

        User user = getUserById(userId);
        user.setUsername(newUsername);

        Optional<User> updatedUserOpt = userRepository.update(user);
        if (updatedUserOpt.isEmpty()) {
            throw new RuntimeException("Failed to update username");
        }

        return updatedUserOpt.get();
    }

    /**
     * Updates a user's username.
     *
     * @param currentUsername the current username of the user
     * @param newUsername the new username
     * @return the updated User object
     * @throws UserNotFoundException if no user with the specified username exists
     * @throws DuplicateUserException if the new username is already in use
     */
    public User updateUsername(String currentUsername, String newUsername) {
        User user = getUserByUsername(currentUsername);
        return updateUsername(user.getUserID(), newUsername);
    }

    /**
     * Updates a user's email address.
     *
     * @param userId the ID of the user to update
     * @param newEmail the new email address
     * @return the updated User object
     * @throws UserNotFoundException if no user with the specified ID exists
     * @throws DuplicateUserException if the new email is already in use
     */
    public User updateEmail(int userId, String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new UserValidationException("email", "Email cannot be empty");
        }

        // Check if email is already taken
        if (userRepository.emailExists(newEmail)) {
            throw new DuplicateUserException("email", newEmail);
        }

        User user = getUserById(userId);
        user.setEmail(newEmail);

        Optional<User> updatedUserOpt = userRepository.update(user);
        if (updatedUserOpt.isEmpty()) {
            throw new RuntimeException("Failed to update email");
        }

        return updatedUserOpt.get();
    }

    /**
     * Updates a user's email address.
     *
     * @param username the username of the user to update
     * @param newEmail the new email address
     * @return the updated User object
     * @throws UserNotFoundException if no user with the specified username exists
     * @throws DuplicateUserException if the new email is already in use
     */
    public User updateEmail(String username, String newEmail) {
        User user = getUserByUsername(username);
        return updateEmail(user.getUserID(), newEmail);
    }

    /**
     * Activates a user account.
     *
     * @param userId the ID of the user to activate
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public void activateAccount(int userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.update(user);
    }

    /**
     * Activates a user account.
     *
     * @param username the username of the user to activate
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public void activateAccount(String username) {
        User user = getUserByUsername(username);
        activateAccount(user.getUserID());
    }

    /**
     * Deactivates a user account.
     *
     * @param userId the ID of the user to deactivate
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public void deactivateAccount(int userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.update(user);
    }

    /**
     * Deactivates a user account.
     *
     * @param username the username of the user to deactivate
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public void deactivateAccount(String username) {
        User user = getUserByUsername(username);
        deactivateAccount(user.getUserID());
    }

    /**
     * Permanently deletes a user account.
     *
     * @param userId the ID of the user to delete
     * @return true if the user was deleted, false otherwise
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public boolean deleteUser(int userId) {
        // Check if user exists
        getUserById(userId);

        // Delete user
        return userRepository.deleteById(userId);
    }

    /**
     * Permanently deletes a user account.
     *
     * @param username the username of the user to delete
     * @return true if the user was deleted, false otherwise
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public boolean deleteUser(String username) {
        User user = getUserByUsername(username);
        return deleteUser(user.getUserID());
    }

    /**
     * Retrieves the list of followers for the specified user.
     *
     * @param userId the ID of the user
     * @return the list of users who follow the specified user
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public List<User> getFollowers(int userId) {
        // Ensure user exists
        getUserById(userId);

        // Get follower IDs
        List<Integer> followerIds = userRepository.getFollowerIds(userId);

        // Convert IDs to User objects
        List<User> followers = new ArrayList<>();
        for (Integer followerId : followerIds) {
            try {
                User follower = getUserById(followerId);
                followers.add(follower);
            } catch (UserNotFoundException e) {
                // Skip invalid follower IDs
            }
        }

        return followers;
    }

    /**
     * Retrieves the list of followers for the specified user.
     *
     * @param username the username of the user
     * @return the list of users who follow the specified user
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public List<User> getFollowers(String username) {
        User user = getUserByUsername(username);
        return getFollowers(user.getUserID());
    }

    /**
     * Retrieves the list of users that the specified user is following.
     *
     * @param userId the ID of the user
     * @return the list of users followed by the specified user
     * @throws UserNotFoundException if no user with the specified ID exists
     */
    public List<User> getFollowedUsers(int userId) {
        // Ensure user exists
        getUserById(userId);

        // Get followed user IDs
        List<Integer> followedIds = userRepository.getFollowedUserIds(userId);

        // Convert IDs to User objects
        List<User> followedUsers = new ArrayList<>();
        for (Integer followedId : followedIds) {
            try {
                User followed = getUserById(followedId);
                followedUsers.add(followed);
            } catch (UserNotFoundException e) {
                // Skip invalid followed IDs
            }
        }

        return followedUsers;
    }

    /**
     * Retrieves the list of users that the specified user is following.
     *
     * @param username the username of the user
     * @return the list of users followed by the specified user
     * @throws UserNotFoundException if no user with the specified username exists
     */
    public List<User> getFollowedUsers(String username) {
        User user = getUserByUsername(username);
        return getFollowedUsers(user.getUserID());
    }

    /**
     * Makes one user follow another.
     *
     * @param followerId the ID of the follower
     * @param followedId the ID of the user to follow
     * @return true if the follow operation was successful, false otherwise
     * @throws UserNotFoundException if either user ID is invalid
     * @throws IllegalArgumentException if a user tries to follow themselves
     */
    public boolean followUser(int followerId, int followedId) {
        // Check if either user doesn't exist
        User follower = getUserById(followerId);
        User followed = getUserById(followedId);

        // Check if user is trying to follow themselves
        if (followerId == followedId) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        // Check if already following
        List<Integer> followedUsers = follower.getFollowedUsersIDs();
        if (followedUsers != null && followedUsers.contains(followedId)) {
            return true; // Already following
        }

        // Add follower relationship
        return userRepository.addFollower(followedId, followerId);
    }

    /**
     * Makes one user follow another.
     *
     * @param followerUsername the username of the follower
     * @param followedUsername the username of the user to follow
     * @return true if the follow operation was successful, false otherwise
     * @throws UserNotFoundException if either username is invalid
     * @throws IllegalArgumentException if a user tries to follow themselves
     */
    public boolean followUser(String followerUsername, String followedUsername) {
        User follower = getUserByUsername(followerUsername);
        User followed = getUserByUsername(followedUsername);

        return followUser(follower.getUserID(), followed.getUserID());
    }

    /**
     * Makes one user unfollow another.
     *
     * @param followerId the ID of the follower
     * @param followedId the ID of the user to unfollow
     * @return true if the unfollow operation was successful, false otherwise
     * @throws UserNotFoundException if either user ID is invalid
     */
    public boolean unfollowUser(int followerId, int followedId) {
        // Check if either user doesn't exist
        User follower = getUserById(followerId);
        User followed = getUserById(followedId);

        // Check if not following
        List<Integer> followedUsers = follower.getFollowedUsersIDs();
        if (followedUsers == null || !followedUsers.contains(followedId)) {
            return false; // Not following
        }

        // Remove follower relationship
        return userRepository.removeFollower(followedId, followerId);
    }

    /**
     * Makes one user unfollow another.
     *
     * @param followerUsername the username of the follower
     * @param followedUsername the username of the user to unfollow
     * @return true if the unfollow operation was successful, false otherwise
     * @throws UserNotFoundException if either username is invalid
     */
    public boolean unfollowUser(String followerUsername, String followedUsername) {
        User follower = getUserByUsername(followerUsername);
        User followed = getUserByUsername(followedUsername);

        return unfollowUser(follower.getUserID(), followed.getUserID());
    }

    /**
     * Returns all users in the system.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Returns all active users in the system.
     *
     * @return a list of all active users
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findActiveUsers();
    }

    /**
     * Returns all inactive users in the system.
     *
     * @return a list of all inactive users
     */
    public List<User> getAllInactiveUsers() {
        return userRepository.findInactiveUsers();
    }

    /**
     * Finds users by their first name.
     *
     * @param firstName the first name to search for
     * @return a list of users with the given first name
     */
    public List<User> findUsersByFirstName(String firstName) {
        return userRepository.findUsersByFirstName(firstName);
    }

    /**
     * Finds users by their last name.
     *
     * @param lastName the last name to search for
     * @return a list of users with the given last name
     */
    public List<User> findUsersByLastName(String lastName) {
        return userRepository.findUsersByLastName(lastName);
    }

    /**
     * Finds users by their full name.
     *
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @return a list of users with the given full name
     */
    public List<User> findUsersByFullName(String firstName, String lastName) {
        return userRepository.findUsersByFullName(firstName, lastName);
    }

    /**
     * Checks if a username is available (not already in use).
     *
     * @param username the username to check
     * @return true if the username is available, false otherwise
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.usernameExists(username);
    }

    /**
     * Checks if an email is available (not already in use).
     *
     * @param email the email to check
     * @return true if the email is available, false otherwise
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.emailExists(email);
    }
}