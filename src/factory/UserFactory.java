package factory;

import managers.SubscriptionManager;
import persistence.UserRepository;
import user.*;
import managers.UserManager;
import user.security.PasswordHasher;
import user.security.SHA256Hasher;

import java.util.*;

/**
 * Factory class responsible for creating, updating, deleting, and managing users and their subscriptions.
 * Implements the Singleton pattern.
 */
public class UserFactory {
    /**
     * Singleton instance of UserFactory.
     */
    private static UserFactory instance;

    /**
     * Repository for user persistence operations.
     */
    private final UserRepository userRepository;

    /**
     * Manager for user read operations.
     */
    private final UserManager userManager;

    /**
     * Manager for subscription-related operations.
     */
    private final SubscriptionManager subscriptionManager;

    private final PasswordHasher passwordHasher = new SHA256Hasher();

    /**
     * Private constructor initializing repositories and managers.
     */
    private UserFactory() {
        this.userRepository = UserRepository.getInstance();
        this.userManager = UserManager.getInstance();
        this.subscriptionManager = SubscriptionManager.getInstance();
    }

    /**
     * Returns the singleton instance of UserFactory.
     *
     * @return the singleton instance
     */
    public static synchronized UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    //Basic user methods

    /**
     * Creates a new user with a free subscription.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param username the username
     * @param email the email address
     * @param password the password
     * @return the created User object
     * @throws IllegalArgumentException if username or email already exist
     */
    public User createUser(String firstName, String lastName, String username, String email, String password) {
        //Check email is unique
        userManager.validateEmailAvailable(email);

        //Check username is unique
        userManager.validateUsernameAvailable(username);

        // Create userID
        int newId = userManager.getAllUsers().stream()
                .mapToInt(User::getUserID)
                .max()
                .orElse(0) + 1;

        // Hash password
        String hashed = passwordHasher.hash(password);

        // Create user object
        User user = new User(newId, username, email, hashed, new Date());
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Assign a free subscription by default
        FreePlan freeplan = new FreePlan();
        user.setSubscriptionPlan(freeplan);
        user.setSubscriptionInfo(new SubscriptionInfo(new Date(), null));

        System.out.println("Saved user → "
                + "ID="   + user.getUserID()
                + ", username=" + user.getUsername()
                + ", email="    + user.getEmail()
                + ", created="  + user.getAccountCreationDate()
        );

        // Save user
        User savedUser = userRepository.save(user);

        // Refresh the user cache after creating a new user
        userManager.refreshCache();
        return savedUser;
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userID the ID of the user to delete
     * @throws IllegalArgumentException if no user with the given ID exists
     */
    public void deleteUser(int userID) {
        // Verify userID exists
        userManager.getUserById(userID);

        // Delete
        userRepository.deleteById(userID);

        // Refresh the user cache after deleting a user
        userManager.refreshCache();
        System.out.println("Deleted user → ID=" + userID);
    }

    /**
     * Deletes a user by their username.
     *
     * @param username the username of the user to delete
     * @throws IllegalArgumentException if no user with the given username exists
     */
    public void deleteUser(String username) {
        // Find user by username
        User user = userManager.getUserByUsername(username);

        // Delete user
        userRepository.delete(user);

        // Refresh the user cache after deleting a user
        userManager.refreshCache();
        System.out.println("Deleted user → Username=" + user.getUsername());
    }

    /**
     * Disables a user's account by ID.
     *
     * @param userID the ID of the user
     */
    public void disableAccount(int userID){
        // Verify userID exists
        User user = userManager.getUserById(userID);

        user.setActive(false);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Account disabled");
    }

    /**
     * Disables a user's account by username.
     *
     * @param username the username of the user
     */
    public void disableAccount(String username){
        // Verify username exists
        User user = userManager.getUserByUsername(username);

        user.setActive(false);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Account disabled");
    }

    /**
     * Enables a user's account by ID.
     *
     * @param userID the ID of the user
     */
    public void enableAccount(int userID){
        // Verify userID exists
        User user = userManager.getUserById(userID);

        user.setActive(true);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Account enabled");
    }

    /**
     * Enables a user's account by username.
     *
     * @param username the username of the user
     */
    public void enableAccount(String username){
        // Find user by username
        User user = userManager.getUserByUsername(username);

        user.setActive(true);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Account enabled");
    }

    /**
     * Updates a user's username.
     *
     * @param username the current username
     * @param newUsername the new username
     * @throws IllegalArgumentException if username not found or new username already exists
     */
    public void updateUsername(String username, String newUsername) {
        // Find existing username
        User user = userManager.getUserByUsername(username);

        // Check new username isn't taken
        userManager.validateUsernameAvailable(newUsername);

        // Update
        user.setUsername(newUsername);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Username updated");
    }

    /**
     * Updates a user's password.
     *
     * @param username the username
     * @param newPassword the new password
     */
    public void updatePassword(String username, String newPassword) {
        // Find existing username
        User user = userManager.getUserByUsername(username);

        // Update
        String hashed = passwordHasher.hash(newPassword);
        user.setPassword(hashed);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Password updated");
    }

    /**
     * Updates a user's email.
     *
     * @param username the username
     * @param newEmail the new email
     * @throws IllegalArgumentException if new email already exists
     */
    public void updateEmail(String username, String newEmail) {
        // Find existing username
        User user = userManager.getUserByUsername(username);

        // Check new email isn't used
        userManager.validateEmailAvailable(newEmail);

        // Update
        user.setEmail(newEmail);
        userRepository.update(user);

        // Refresh the cache after updating a user
        userManager.refreshCache();
        System.out.println("Email updated");
    }

    /**
     * Subscribes a user to a premium plan for one year.
     *
     * @param username the username
     * @throws IllegalStateException if already subscribed to premium
     */
    public void subscribeToPremium(String username){
        // Verify username exists
        User user = userManager.getUserByUsername(username);

        // Check if it's already Premium
        if (subscriptionManager.hasActiveSubscription(user, PremiumPlan.class)) {
            throw new IllegalStateException("User " + username + " already has Premium plan");
        }

        // Update to premium for 1 year + refresh + update
        subscriptionManager.upgradeToPremium(user);

        System.out.println("Upgraded to Premium → username=" + username );
    }

    /**
     * Downgrades a user's subscription to a free plan.
     *
     * @param username the username
     * @throws IllegalArgumentException if no such user
     * @throws IllegalStateException    if already on Free
     */
    public void downgradeToFree(String username) {
        // Verify username exists
        User user = userManager.getUserByUsername(username);

        // Check if it's already free
        if (subscriptionManager.hasActiveSubscription(user, FreePlan.class)) {
            throw new IllegalStateException("User " + username + " already has Free plan");
        }

        // Downgrade + refresh + update
        subscriptionManager.downgradeToFree(user);

        System.out.println("Downgraded to Free → username=" + username);
    }


    /**
     * Downgrades users with expired subscriptions to a free plan.
     * Runs when UserFactory is initialized.
     */
    public void downgradeToFreeIfExpired() {
        List<User> allUsers = userManager.getAllUsers();
        boolean anyExpired = false;

        for (User user : allUsers) {
            if (subscriptionManager.isExpired(user)) {
                subscriptionManager.downgradeToFree(user);
                anyExpired = true;
                System.out.println("Downgraded expired subscription → username=" + user.getUsername());
            }
        }

        // Refresh if any changes were made
        if (anyExpired) {
            userManager.refreshCache();
        }
    }

    /**
     * Renews a user's premium subscription for one more year.
     *
     * @param username the username of the user to renew
     * @throws IllegalArgumentException if no user with the given username exists
     * @throws IllegalStateException    if the user does not currently have a Premium plan
     */
    public void renewPremium(String username) {
        // Verify user exists
        User user = userManager.getUserByUsername(username);

        // Ensure user currently on Premium
        try {
            subscriptionManager.validatePremiumSubscription(user);
        } catch (IllegalStateException e) {
            if (!subscriptionManager.isExpired(user)) {
                throw new IllegalStateException("User " + username + " does not have a Premium plan, renewing it.");
            }
        }

        // Delegate to SubscriptionManager
        subscriptionManager.renew(user, 365);

        // Debug
        Date newExpiry = user.getSubscriptionInfo().getEndDate();
        System.out.println("Renewed Premium → username=" + username + ", new expires=" + newExpiry);
    }


    //Followers methods

    /**
     * Makes a user follow another user.
     *
     * @param followerUsername the username of the follower
     * @param followeeUsername the username of the user to follow
     * @throws IllegalStateException if already following
     */
    public void followUser(String followerUsername, String followeeUsername){
        // Check relations
        User[] users = userManager.validateFollowRelationship(followerUsername, followeeUsername);
        User follower = users[0];
        User followee = users[1];

        // Update follower's followed list
        List<Integer> follows = follower.getFollowedUsersIDs();
        follows.add(followee.getUserID());
        follower.setFollowedUsersIDs(follows);

        // Update followee's followers list
        List<Integer> followers = followee.getFollowersIDs();
        followers.add(follower.getUserID());
        followee.setFollowersIDs(followers);

        // Save
        userRepository.update(follower);
        userRepository.update(followee);

        // Refresh the cache after updating users
        userManager.refreshCache();

        System.out.println(followerUsername + " now follows " + followeeUsername);
    }

    /**
     * Makes a user unfollow another user.
     *
     * @param followerUsername the username of the follower
     * @param followeeUsername the username of the user to unfollow
     * @throws IllegalStateException if not currently following
     */
    public void unfollowUser(String followerUsername, String followeeUsername){
        // Check relations
        User[] users = userManager.validateUnfollowRelationship(followerUsername, followeeUsername);
        User follower = users[0];
        User followee = users[1];

        // Update follower's followed list
        List<Integer> follows = follower.getFollowedUsersIDs();
        follows.remove((Integer) followee.getUserID());
        follower.setFollowedUsersIDs(follows);

        // Update followee's followers list
        List<Integer> followers = followee.getFollowersIDs();
        followers.remove((Integer) follower.getUserID());
        followee.setFollowersIDs(followers);

        // Save
        userRepository.update(follower);
        userRepository.update(followee);

        // Refresh the cache after updating users
        userManager.refreshCache();

        System.out.println(followerUsername + " unfollowed " + followeeUsername);
    }
}