package factory;

import persistence.UserRepository;
import user.*;
import user.security.PasswordHasher;
import user.security.SHA256Hasher;
import user.subscription.*;
import user.subscription.FreeSubscription;
import user.subscription.SubscriptionInfo;

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
     * Password hasher for securely storing passwords.
     */
    private final PasswordHasher passwordHasher;

    /**
     * Private constructor initializing repositories.
     */
    private UserFactory() {
        this.userRepository = UserRepository.getInstance();
        this.passwordHasher = new SHA256Hasher();
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
     */
    public User createUser(String firstName, String lastName, String username, String email, String password) {
        // Get next available ID
        int newId = generateNextUserId();

        // Hash password
        String hashedPassword = passwordHasher.hash(password);

        // Create user object
        User user = new User(newId, username, email, hashedPassword, new Date());
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Set default values for a new user
        user.setActive(true);
        user.setFollowedUsersIDs(new ArrayList<>());
        user.setFollowersIDs(new ArrayList<>());

        // Assign a free subscription by default
        FreeSubscription freePlan = new FreeSubscription();
        user.setSubscriptionPlan(freePlan);
        user.setSubscriptionInfo(new SubscriptionInfo(new Date(), null));

        // Save user
        User savedUser = userRepository.save(user);

        System.out.println("Created user → ID=" + savedUser.getUserID()
                + ", username=" + savedUser.getUsername());

        return savedUser;
    }

    /**
     * Creates a new user with a specified subscription plan.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param subscriptionPlan the subscription plan to assign
     * @param subscriptionDays duration in days; zero or negative means no end date
     * @return the created User object
     */
    public User createUser(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            SubscriptionPlan subscriptionPlan,
            int subscriptionDays) {

        // Get next available ID
        int newId = generateNextUserId();

        // Hash password
        String hashedPassword = passwordHasher.hash(password);

        // Create user object
        User user = new User(newId, username, email, hashedPassword, new Date());
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Set default values for a new user
        user.setActive(true);
        user.setFollowedUsersIDs(new ArrayList<>());
        user.setFollowersIDs(new ArrayList<>());

        // Calculate subscription end date
        Date startDate = new Date();
        Date endDate = null;
        if (subscriptionDays > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, subscriptionDays);
            endDate = calendar.getTime();
        }

        // Assign the specified subscription
        user.setSubscriptionPlan(subscriptionPlan);
        user.setSubscriptionInfo(new SubscriptionInfo(startDate, endDate));

        // Save user
        User savedUser = userRepository.save(user);

        System.out.println("Created user → ID=" + savedUser.getUserID()
                + ", username=" + savedUser.getUsername()
                + ", subscription=" + subscriptionPlan.getType());

        return savedUser;
    }

    /**
     * Generates the next available user ID.
     *
     * @return the next available user ID
     */
    private int generateNextUserId() {
        return userRepository.findAll().stream()
                .mapToInt(User::getUserID)
                .max()
                .orElse(0) + 1;
    }


}