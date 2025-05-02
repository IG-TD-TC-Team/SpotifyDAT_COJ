package services;

import persistence.UserRepository;
import user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages user-related operations such as retrieving user information
 * and handling followers and followed users.
 * Implements the Singleton pattern to ensure only one instance exists.
 * Uses an in-memory cache to improve performance.
 */
public class UserService {

    /**
     * Singleton instance of UserService.
     */
    private static UserService instance;

    /**
     * Repository for accessing user data.
     */
    private final UserRepository userRepository;

    /**
     * Cache of users to improve performance.
     */
    private List<User> users;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the UserRepository instance and loads the user cache.
     */
    private UserService() {
        this.userRepository = UserRepository.getInstance();
        this.users = new ArrayList<>();
        refreshCache();
    }

    /**
     * Returns the single instance of UserService, creating it if it doesn't exist.
     *
     * @return the singleton instance of UserService
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Refreshes the in-memory cache with the latest data from the repository.
     * Call this after users are created, updated, or deleted.
     */
    public void refreshCache() {
        this.users = userRepository.findAll();
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id the ID of the user
     * @return the User object
     * @throws IllegalArgumentException if no user with the specified ID exists
     */
    public User getUserById(int id) {
        refreshCache();
        for (User user : users) {
            if (user.getUserID() == id) {
                return user;
            }
        }
        throw new IllegalArgumentException("No user with ID=" + id);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username of the user
     * @return the User object
     * @throws IllegalArgumentException if no user with the specified username exists
     */
    public User getUserByUsername(String username) {
        refreshCache();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        throw new IllegalArgumentException("No user with username='" + username + "'");
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email of the user
     * @return the User object
     * @throws IllegalArgumentException if no user with the specified email exists
     */
    public User getUserByEmail(String email) {
        refreshCache();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        throw new IllegalArgumentException("No user with email='" + email + "'");
    }

    /**
     * Retrieves the list of followers for the specified user.
     *
     * @param username the username of the user
     * @return the list of users who follow the specified user
     */
    public List<User> getFollowers(String username) {
        refreshCache();
        User user = getUserByUsername(username);
        List<User> followers = new ArrayList<>();
        List<Integer> followerIds = user.getFollowersIDs();

        if (followerIds != null) {
            for (Integer followerId : followerIds) {
                for (User follower : users) {
                    if (follower.getUserID() == followerId) {
                        followers.add(follower);
                        break;
                    }
                }
            }
        }

        return followers;
    }

    /**
     * Retrieves the list of users that the specified user is following.
     *
     * @param username the username of the user
     * @return the list of users followed by the specified user
     */
    public List<User> getFollowedUsers(String username) {
        refreshCache();
        User user = getUserByUsername(username);
        List<User> followedUsers = new ArrayList<>();
        List<Integer> followedIds = user.getFollowedUsersIDs();

        if (followedIds != null) {
            for (Integer followedId : followedIds) {
                for (User followedUser : users) {
                    if (followedUser.getUserID() == followedId) {
                        followedUsers.add(followedUser);
                        break;
                    }
                }
            }
        }

        return followedUsers;
    }

    /**
     * Returns all users in the system.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        refreshCache();
        return new ArrayList<>(users);
    }

    /**
     * Checks if a username is available (not already in use).
     *
     * @param username the username to check
     * @throws IllegalArgumentException if the username is already in use
     */
    public void validateUsernameAvailable(String username) {
        refreshCache();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                throw new IllegalArgumentException("Username already in use: " + username);
            }
        }
    }

    /**
     * Checks if an email is available (not already in use).
     *
     * @param email the email to check
     * @throws IllegalArgumentException if the email is already in use
     */
    public void validateEmailAvailable(String email) {
        refreshCache();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                throw new IllegalArgumentException("Email already in use: " + email);
            }
        }
    }

    /**
     * Checks if a user with the given username exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        refreshCache();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        refreshCache();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds users by their first name.
     *
     * @param firstName the first name to search for
     * @return a list of users with the given first name
     */
    public List<User> findUsersByFirstName(String firstName) {
        refreshCache();
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getFirstName() != null && user.getFirstName().equalsIgnoreCase(firstName)) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * Finds users by their last name.
     *
     * @param lastName the last name to search for
     * @return a list of users with the given last name
     */
    public List<User> findUsersByLastName(String lastName) {
        refreshCache();
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getLastName() != null && user.getLastName().equalsIgnoreCase(lastName)) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * Finds users by their full name.
     *
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @return a list of users with the given full name
     */
    public List<User> findUsersByFullName(String firstName, String lastName) {
        refreshCache();
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getFirstName() != null && user.getFirstName().equalsIgnoreCase(firstName) &&
                    user.getLastName() != null && user.getLastName().equalsIgnoreCase(lastName)) {
                result.add(user);
            }
        }
        return result;
    }

    /**
     * Validates that a follower-followee relationship can be established.
     *
     * @param followerUsername the username of the follower
     * @param followeeUsername the username of the user to follow
     * @return array containing both User objects [follower, followee]
     * @throws IllegalArgumentException if either username is invalid or if attempting to self-follow
     * @throws IllegalStateException if already following
     */
    public User[] validateFollowRelationship(String followerUsername, String followeeUsername) {
        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        User follower = getUserByUsername(followerUsername);
        User followee = getUserByUsername(followeeUsername);

        // Check if already following
        List<Integer> follows = follower.getFollowedUsersIDs();
        if (follows.contains(followee.getUserID())) {
            throw new IllegalStateException(followerUsername + " already follows " + followeeUsername);
        }

        return new User[] {follower, followee};
    }

    /**
     * Validates that a follower-followee relationship can be removed.
     *
     * @param followerUsername the username of the follower
     * @param followeeUsername the username of the user to unfollow
     * @return array containing both User objects [follower, followee]
     * @throws IllegalArgumentException if either username is invalid or if attempting to self-unfollow
     * @throws IllegalStateException if not currently following
     */
    public User[] validateUnfollowRelationship(String followerUsername, String followeeUsername) {
        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("User cannot unfollow themselves");
        }

        User follower = getUserByUsername(followerUsername);
        User followee = getUserByUsername(followeeUsername);

        // Check if not following
        List<Integer> follows = follower.getFollowedUsersIDs();
        if (!follows.contains(followee.getUserID())) {
            throw new IllegalStateException(followerUsername + " does not follow " + followeeUsername);
        }

        return new User[] {follower, followee};
    }
}