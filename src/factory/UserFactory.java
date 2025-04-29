package factory;

import managers.SubscriptionManager;
import persistence.UserRepository;
import user.*;
import managers.UserManager;
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
        if (userRepository.emailExists(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        //Check username is unique
        if (userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("Username already in use: " + username);
        }

        // Create userID
        int newId = userRepository.findAll().stream()
                .mapToInt(User::getUserID)
                .max()
                .orElse(0) + 1;

        // Create user object
        User user = new User(newId, username, email, password, new Date());
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
        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userID the ID of the user to delete
     * @throws IllegalArgumentException if no user with the given ID exists
     */
    public void deleteUser(int userID) {
        // Verify userID exists
        if (!userRepository.userIdExists(userID)) {
            throw new IllegalArgumentException("No user with ID=" + userID);
        }

        // Delete
        userRepository.deleteById(userID);
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
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        // Load user
        User user = userRepository.findByUsername(username).get();

        // Delete user
        userRepository.delete(user);
        System.out.println("Deleted user → Username=" + user.getUsername());
    }

    /**
     * Disables a user's account by ID.
     *
     * @param userID the ID of the user
     */
    public void disableAccount(int userID){
        // Verify userID exists
        if (!userRepository.userIdExists(userID)) {
            throw new IllegalArgumentException("No user with ID=" + userID);
        }

        User user = userRepository.findById(userID).get();
        user.setActive(false);
        userRepository.update(user);
        System.out.println("Account disabled");
    }

    /**
     * Disables a user's account by username.
     *
     * @param username the username of the user
     */
    public void disableAccount(String username){
        // Verify username exists
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        User user = userRepository.findByUsername(username).get();
        user.setActive(false);
        userRepository.update(user);
        System.out.println("Account disabled");
    }

    /**
     * Enables a user's account by ID.
     *
     * @param userID the ID of the user
     */
    public void enableAccount(int userID){
        // Verify userID exists
        if (!userRepository.userIdExists(userID)) {
            throw new IllegalArgumentException("No user with ID=" + userID);
        }

        User user = userRepository.findById(userID).get();
        user.setActive(true);
        userRepository.update(user);
        System.out.println("Account enabled");
    }

    /**
     * Enables a user's account by username.
     *
     * @param username the username of the user
     */
    public void enableAccount(String username){
        // Find user by username
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        User user = userRepository.findByUsername(username).get();
        user.setActive(true);
        userRepository.update(user);
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
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        // Check new username isn't taken
        if (userRepository.usernameExists(newUsername)) {
            throw new IllegalArgumentException("Username already in use: " + newUsername);
        }

        // Update
        User user = userRepository.findByUsername(username).get();
        user.setUsername(newUsername);
        userRepository.update(user);
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
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        // Update
        User user = userRepository.findByUsername(username).get();
        user.setPassword(newPassword);
        userRepository.update(user);
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
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        // Check new email isn't used
        if (userRepository.emailExists(newEmail)) {
            throw new IllegalArgumentException("Email already in use: " + newEmail);
        }

        // Update
        User user = userRepository.findByUsername(username).get();
        user.setEmail(newEmail);
        userRepository.update(user);
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
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        User user = userRepository.findByUsername(username).get();

        // Check if it's already Premium
        if (user.getSubscriptionPlan() instanceof PremiumPlan) {
            throw new IllegalStateException("User " + username + " already has Premium plan");
        }

        // Update to premium for 1 year
        Date start = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.DAY_OF_MONTH, 365);
        Date end = cal.getTime();

        PremiumPlan premium = new PremiumPlan();
        user.setSubscriptionPlan(premium);
        user.setSubscriptionInfo(new SubscriptionInfo(start, end));
        userRepository.update(user);

        System.out.println("DEBUG: Upgraded to Premium → username=" + username + ", expires=" + end);
    }

    /**
     * Downgrades a user's subscription to a free plan.
     *
     * @param username the username
     */
    public void downgradeToFree(String username){
        // Verify username exists
        if (!userRepository.usernameExists(username)) {
            throw new IllegalArgumentException("No user with username=" + username);
        }

        User user = userRepository.findByUsername(username).get();

        // Check if it's already free
        if (user.getSubscriptionPlan() instanceof FreePlan) {
            throw new IllegalStateException("User " + username + " already has Free plan");
        }

        // Downgrade to free
        Date now = new Date();
        FreePlan freePlan = new FreePlan();
        user.setSubscriptionPlan(freePlan);
        user.setSubscriptionInfo(new SubscriptionInfo(now, null));
        userRepository.update(user);

        System.out.println("Downgraded to Free → username=" + username);
    }

    /**
     * Downgrades users with expired subscriptions to a free plan.
     */
    // Runs when UserFactory is initialized
    public void downgradeToFreeIfExpired(){
        Date now = new Date();
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            SubscriptionInfo info = user.getSubscriptionInfo();
            if (info != null && info.getEndDate() != null && info.getEndDate().before(now)) {
                if (user.getSubscriptionPlan() instanceof PremiumPlan) {
                    FreePlan freePlan = new FreePlan();
                    user.setSubscriptionPlan(freePlan);
                    user.setSubscriptionInfo(new SubscriptionInfo(now, null));
                    userRepository.update(user);
                    System.out.println("Downgraded expired subscription → username=" + user.getUsername());
                }
            }
        }
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
        // Check usernames
        if (!userRepository.usernameExists(followerUsername) || !userRepository.usernameExists(followeeUsername)) {
            throw new IllegalArgumentException("Invalid usernames provided");
        }
        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        // Load users
        User follower = userRepository.findByUsername(followerUsername).get();
        User followee = userRepository.findByUsername(followeeUsername).get();

        // Check if already following
        List<Integer> follows = follower.getFollowedUsersIDs();
        if (follows.contains(followee.getUserID())) {
            throw new IllegalStateException(followerUsername + " already follows " + followeeUsername);
        }

        // Update
        follows.add(followee.getUserID());
        follower.setFollowedUsersIDs(follows);

        List<Integer> followers = followee.getFollowersIDs();
        followers.add(follower.getUserID());
        followee.setFollowersIDs(followers);

        userRepository.update(follower);
        userRepository.update(followee);

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
        // Check usernames
        if (!userRepository.usernameExists(followerUsername) || !userRepository.usernameExists(followeeUsername)) {
            throw new IllegalArgumentException("Invalid usernames provided");
        }
        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        // Load users
        User follower = userRepository.findByUsername(followerUsername).get();
        User followee = userRepository.findByUsername(followeeUsername).get();

        // Check if already unfollowing
        List<Integer> follows = follower.getFollowedUsersIDs();
        if (!follows.remove((Integer) followee.getUserID())) {
            throw new IllegalStateException(followerUsername + " does not follow " + followeeUsername);
        }

        // Update
        follower.setFollowedUsersIDs(follows);

        List<Integer> followers = followee.getFollowersIDs();
        followers.remove((Integer) follower.getUserID());
        followee.setFollowersIDs(followers);

        userRepository.update(follower);
        userRepository.update(followee);

        System.out.println(followerUsername + " unfollowed " + followeeUsername);
    }
}
