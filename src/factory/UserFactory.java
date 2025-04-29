package factory;

import managers.SubscriptionManager;
import persistence.UserRepository;
import user.*;
import persistence.interfaces.*;
import managers.UserManager;
import java.util.*;

public class UserFactory {
    // Singleton instance
    private static UserFactory instance;

    // Repositories for persistence operations
    private final UserRepository userRepository;

    // SongManager for all read operations
    private final UserManager userManager;

    // Subscription manager
    private final SubscriptionManager subscriptionManager;

    // Private constructor
    private UserFactory() {
        this.userRepository = UserRepository.getInstance();
        this.userManager = UserManager.getInstance();
        this.subscriptionManager = SubscriptionManager.getInstance();
    }

    //Return single instance
    public static synchronized UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    //Basic user methods
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

    // Delete by ID
    public void deleteUser(int userID) {
        // Verify userID exists
        if (!userRepository.userIdExists(userID)) {
            throw new IllegalArgumentException("No user with ID=" + userID);
        }

        // Delete
        userRepository.deleteById(userID);
        System.out.println("Deleted user → ID=" + userID);
    }

    // Delete by username
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

    // Set isActive to false by userID
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

    // Set isActive to false by username
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

    // Set isActive to true by userID
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

    // Set isActive to true by username
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

    //Link with SubscriptionManager
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
