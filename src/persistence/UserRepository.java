package persistence;

import persistence.interfaces.UserRepositoryInterface;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserRepository class is a singleton that manages user data.
 * It provides methods to add, update, delete, and retrieve users from a JSON file.
 * It also includes methods for user authentication and finding followers and followed users.
 */
public class UserRepository extends JsonRepository<User> implements UserRepositoryInterface {

    //Singleton instance
    private static UserRepository instance;

    /**
     * Private constructor for Singleton pattern.
     */
    private UserRepository() {
        super(User.class, "users.json", User::getUserID);
    }

    /**
     * Gets the singleton instance of UserRepository.
     *
     * @return The singleton instance
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    ///------Retrieve------///
    /**
     * Find a user by their ID.
     *
     * @param userId The ID of the user to find.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    public Optional<User> findById(int userId) {
        return findAll().stream()
                .filter(user -> user.getUserID() == userId)
                .findFirst();
    }

    /**
     * Find a user by their username.
     *
     * @param username The username of the user to find.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * Find a user by their email.
     *
     * @param email The email of the user to find.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Find a user by their first name.
     *
     * @param firstName The first name of the user to find.
     * @return A list of Users with the specified first name.
     */
    @Override
    public List<User> findUsersByFirstName(String firstName) {
        return findAll().stream()
                .filter(user -> user.getFirstName().equalsIgnoreCase(firstName))
                .collect(Collectors.toList());
    }

    /**
     * Find a user by their last name.
     *
     * @param lastName The last name of the user to find.
     * @return A list of Users with the specified last name.
     */
    @Override
    public List<User> findUsersByLastName(String lastName) {
        return findAll().stream()
                .filter(user -> user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    /**
     * Find a user by their full name (first and last name).
     *
     * @param firstName The first name of the user to find.
     * @param lastName  The last name of the user to find.
     * @return A list of Users with the specified full name.
     */
    @Override
    public List<User> findUsersByFullName(String firstName, String lastName) {
        return findAll().stream()
                .filter(user -> user.getFirstName().equalsIgnoreCase(firstName) &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }


    // findAll already inherited

    ///-----ADD-----///
    /**
     * Check if an email already exists in the repository.
     *
     * @param email The email to check.
     * @return true if the email exists, false otherwise.
     */
    @Override
    public boolean emailExists(String email) {
        return findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    /**
     * Check if a username already exists in the repository.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    @Override
    public boolean usernameExists(String username) {
        return findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    /**
     * Add a new user to the repository.
     *
     * @param user The user to add.
     * @throws IllegalArgumentException if the email already exists.
     */
    @Override
    public User save(User user) {
        if (emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        return super.save(user);
    }

    // Modify globally (specifications in UserManager)
    /**
     * Update an existing user in the repository.
     *
     * @param user The user to update.
     * @throws IllegalArgumentException if the user with the specified ID is not found.
     */
    @Override
    public Optional<User> update(User user) {
        // Check if user exists before updating
        if (findById(user.getUserID()).isPresent()) {
            return super.update(user);
        }
        throw new IllegalArgumentException("User with ID " + user.getUserID() + " not found.");
    }

    // Delete
    /**
     * Delete a user from the repository.
     *
     * @param user The user to delete.
     * @throws IllegalArgumentException if the user with the specified ID is not found.
     */
    public void delete(User user){
        List<User> users = findAll();
        boolean removed = users.removeIf(u -> u.getUserID() == user.getUserID());
        if (removed) {
            saveAll(users); // Save the updated list back to JSON
        } else {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " not found.");
        }
    }

    @Override
    public boolean deleteById(int id) {
        boolean removed = super.deleteById(id);
        if (!removed) {
            throw new IllegalArgumentException("User with ID " + id + " not found.");
        }
        return true;
    }

    // Authenticate
    /**
     * Check if the provided username and password match a user in the repository.
     *
     * @param username The username to check.
     * @param password The password to check.
     * @return true if the credentials are valid, false otherwise.
     */
    @Override
    public boolean checkCredentialsByUsername(String username, String password) {
        return findByUsername(username)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    /**
     * Check if the provided email and password match a user in the repository.
     *
     * @param email    The email to check.
     * @param password The password to check.
     * @return true if the credentials are valid, false otherwise.
     */
    @Override
    public boolean checkCredentialsByEmail(String email, String password) {
        return findByEmail(email)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    // Retrieve all followers as User objects
    /**
     * Retrieve all followers of a user as User objects.
     *
     * @param user The user whose followers to retrieve.
     * @return A list of User objects representing the followers.
     */
    @Override
    public List<User> findFollowers(User user) {
        List<Integer> followerIds = user.getFollowersIDs();
        List<User> followers = new ArrayList<>();

        if (followerIds != null) {
            for (Integer followerId : followerIds) {
                findById(followerId).ifPresent(followers::add);
            }
        }

        return followers;
    }

    // Find follower by username
    /**
     * Find a follower of a user by their username.
     *
     * @param user     The user whose followers to search.
     * @param username The username of the follower to find.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    @Override
    public Optional<User> findFollowerByUsername(User user, String username) {
        List<Integer> followerIds = user.getFollowersIDs();

        if (followerIds != null) {
            for (Integer followerId : followerIds) {
                Optional<User> follower = findById(followerId);
                if (follower.isPresent() && follower.get().getUsername().equalsIgnoreCase(username)) {
                    return follower;
                }
            }
        }

        return Optional.empty();
    }

    // Find followers by first name
    /**
     * Find followers of a user by their first name.
     *
     * @param user      The user whose followers to search.
     * @param firstName The first name of the followers to find.
     * @return A list of User objects representing the matching followers.
     */
    @Override
    public List<User> findFollowersByFirstName(User user, String firstName) {
        List<Integer> followerIds = user.getFollowersIDs();
        List<User> matchingFollowers = new ArrayList<>();

        if (followerIds != null) {
            for (Integer followerId : followerIds) {
                Optional<User> follower = findById(followerId);
                if (follower.isPresent() && follower.get().getFirstName().equalsIgnoreCase(firstName)) {
                    matchingFollowers.add(follower.get());
                }
            }
        }

        return matchingFollowers;
    }

    // Find followers by last name
    /**
     * Find followers of a user by their last name.
     *
     * @param user      The user whose followers to search.
     * @param lastName  The last name of the followers to find.
     * @return A list of User objects representing the matching followers.
     */
    @Override
    public List<User> findFollowersByLastName(User user, String lastName) {
        List<Integer> followerIds = user.getFollowersIDs();
        List<User> matchingFollowers = new ArrayList<>();

        if (followerIds != null) {
            for (Integer followerId : followerIds) {
                Optional<User> follower = findById(followerId);
                if (follower.isPresent() && follower.get().getLastName().equalsIgnoreCase(lastName)) {
                    matchingFollowers.add(follower.get());
                }
            }
        }

        return matchingFollowers;
    }

    // Find followers by full name
    /**
     * Find followers of a user by their full name (first and last name).
     *
     * @param user      The user whose followers to search.
     * @param firstName The first name of the followers to find.
     * @param lastName  The last name of the followers to find.
     * @return A list of User objects representing the matching followers.
     */
    @Override
    public List<User> findFollowersByFullName(User user, String firstName, String lastName) {
        List<Integer> followerIds = user.getFollowersIDs();
        List<User> matchingFollowers = new ArrayList<>();

        if (followerIds != null) {
            for (Integer followerId : followerIds) {
                Optional<User> follower = findById(followerId);
                if (follower.isPresent() &&
                        follower.get().getFirstName().equalsIgnoreCase(firstName) &&
                        follower.get().getLastName().equalsIgnoreCase(lastName)) {
                    matchingFollowers.add(follower.get());
                }
            }
        }

        return matchingFollowers;
    }

    // Retrieve all followed users as User objects
    /**
     * Retrieve all followed users of a user as User objects.
     *
     * @param user The user whose followed users to retrieve.
     * @return A list of User objects representing the followed users.
     */
    @Override
    public List<User> findFollowedUsers(User user) {
        List<Integer> followedUserIds = user.getFollowedUsersIDs();
        List<User> followedUsers = new ArrayList<>();

        if (followedUserIds != null) {
            for (Integer followedId : followedUserIds) {
                findById(followedId).ifPresent(followedUsers::add);
            }
        }

        return followedUsers;
    }

    // Find followed user by username
    /**
     * Find a followed user of a user by their username.
     *
     * @param user     The user whose followed users to search.
     * @param username The username of the followed user to find.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    @Override
    public Optional<User> findFollowedUserByUsername(User user, String username) {
        List<Integer> followedUserIds = user.getFollowedUsersIDs();

        if (followedUserIds != null) {
            for (Integer followedId : followedUserIds) {
                Optional<User> followedUser = findById(followedId);
                if (followedUser.isPresent() && followedUser.get().getUsername().equalsIgnoreCase(username)) {
                    return followedUser;
                }
            }
        }

        return Optional.empty();
    }

    // Find followed users by first name
    /**
     * Find followed users of a user by their first name.
     *
     * @param user      The user whose followed users to search.
     * @param firstName The first name of the followed users to find.
     * @return A list of User objects representing the matching followed users.
     */
    @Override
    public List<User> findFollowedUsersByFirstName(User user, String firstName) {
        List<Integer> followedUserIds = user.getFollowedUsersIDs();
        List<User> matchingFollowedUsers = new ArrayList<>();

        if (followedUserIds != null) {
            for (Integer followedId : followedUserIds) {
                Optional<User> followedUser = findById(followedId);
                if (followedUser.isPresent() && followedUser.get().getFirstName().equalsIgnoreCase(firstName)) {
                    matchingFollowedUsers.add(followedUser.get());
                }
            }
        }

        return matchingFollowedUsers;
    }

    // Find followed users by last name
    /**
     * Find followed users of a user by their last name.
     *
     * @param user      The user whose followed users to search.
     * @param lastName  The last name of the followed users to find.
     * @return A list of User objects representing the matching followed users.
     */
    @Override
    public List<User> findFollowedUsersByLastName(User user, String lastName) {
        List<Integer> followedUserIds = user.getFollowedUsersIDs();
        List<User> matchingFollowedUsers = new ArrayList<>();

        if (followedUserIds != null) {
            for (Integer followedId : followedUserIds) {
                Optional<User> followedUser = findById(followedId);
                if (followedUser.isPresent() && followedUser.get().getLastName().equalsIgnoreCase(lastName)) {
                    matchingFollowedUsers.add(followedUser.get());
                }
            }
        }

        return matchingFollowedUsers;
    }

    // Find followed users by full name
    /**
     * Find followed users of a user by their full name (first and last name).
     *
     * @param user      The user whose followed users to search.
     * @param firstName The first name of the followed users to find.
     * @param lastName  The last name of the followed users to find.
     * @return A list of User objects representing the matching followed users.
     */
    @Override
    public List<User> findFollowedUsersByFullName(User user, String firstName, String lastName) {
        List<Integer> followedUserIds = user.getFollowedUsersIDs();
        List<User> matchingFollowedUsers = new ArrayList<>();

        if (followedUserIds != null) {
            for (Integer followedId : followedUserIds) {
                Optional<User> followedUser = findById(followedId);
                if (followedUser.isPresent() &&
                        followedUser.get().getFirstName().equalsIgnoreCase(firstName) &&
                        followedUser.get().getLastName().equalsIgnoreCase(lastName)) {
                    matchingFollowedUsers.add(followedUser.get());
                }
            }
        }

        return matchingFollowedUsers;
    }


}
