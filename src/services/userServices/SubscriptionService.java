package services.userServices;

import factory.RepositoryFactory;
import persistence.interfaces.UserRepositoryInterface;
import services.userServices.exceptions.SubscriptionException;
import services.userServices.exceptions.UserNotFoundException;
import user.User;
import user.subscription.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

//Implement cache
//Implement automatic downgrade when expired

/**
 * Service class for subscription-related operations.
 * Follows the repository-service pattern by using the UserRepository
 * and handling subscription plan management.
 */
public class SubscriptionService {

    // Singleton instance
    private static SubscriptionService instance;

    // Dependencies
    private final UserRepositoryInterface userRepository;

    /**
     * Private constructor with dependency injection.
     *
     * @param userRepository The user repository to use
     */
    private SubscriptionService(UserRepositoryInterface userRepository) {
        this.userRepository = RepositoryFactory.getInstance().getUserRepository();
    }

    /**
     * Returns the singleton instance of SubscriptionService.
     *
     * @return the singleton instance
     */
    public static synchronized SubscriptionService getInstance() {
        if (instance == null) {
            // Using RepositoryFactory to get the repository instance
            instance = new SubscriptionService(
                    RepositoryFactory.getInstance().getUserRepository()
            );
        }
        return instance;
    }

    /**
     * Creates or replaces a subscription for the given user by ID.
     *
     * @param userId the ID of the user
     * @param plan subscription plan
     * @param days duration in days; zero or negative means no end date
     * @throws UserNotFoundException if user doesn't exist
     */
    public void createSubscription(int userId, SubscriptionPlan plan, int days) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();
        Date start = new Date();
        Date end = calculateEndDate(start, days);

        user.setSubscriptionPlan(plan);
        user.setSubscriptionInfo(new SubscriptionInfo(start, end));

        userRepository.update(user);
        UserService.getInstance().refreshCache();
    }

    /**
     * Creates or replaces a subscription for the given user by username.
     *
     * @param username the username of the user
     * @param plan subscription plan
     * @param days duration in days; zero or negative means no end date
     * @throws UserNotFoundException if user doesn't exist
     */
    public void createSubscription(String username, SubscriptionPlan plan, int days) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();
        Date start = new Date();
        Date end = calculateEndDate(start, days);

        user.setSubscriptionPlan(plan);
        user.setSubscriptionInfo(new SubscriptionInfo(start, end));

        userRepository.update(user);
        UserService.getInstance().refreshCache();
    }

    /**
     * Upgrades the user to a Premium subscription by user ID.
     *
     * @param userId the ID of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void upgradeToPremium(int userId) {
        createSubscription(userId, new PremiumSubscription(), 365);
    }

    /**
     * Upgrades the user to a Premium subscription by username.
     *
     * @param username the username of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void upgradeToPremium(String username) {
        createSubscription(username, new PremiumSubscription(), 365);
    }

    /**
     * Upgrades the user to a Family subscription by user ID.
     *
     * @param userId the ID of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void upgradeToFamily(int userId) {
        createSubscription(userId, new FamilySubscription(), 365);
    }

    /**
     * Upgrades the user to a Family subscription by username.
     *
     * @param username the username of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void upgradeToFamily(String username) {
        createSubscription(username, new FamilySubscription(), 365);
    }

    /**
     * Upgrades the user to a Student subscription by user ID.
     *
     * @param userId the ID of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void upgradeToStudent(int userId) {
        createSubscription(userId, new StudentSubscription(), 365);
    }

    /**
     * Upgrades the user to a Student subscription by username.
     *
     * @param username the username of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void upgradeToStudent(String username) {
        createSubscription(username, new StudentSubscription(), 365);
    }

    /**
     * Downgrades the user to a Free plan by user ID.
     *
     * @param userId the ID of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void downgradeToFree(int userId) {
        createSubscription(userId, new FreeSubscription(), 0);
    }

    /**
     * Downgrades the user to a Free plan by username.
     *
     * @param username the username of the user
     * @throws UserNotFoundException if user doesn't exist
     */
    public void downgradeToFree(String username) {
        createSubscription(username, new FreeSubscription(), 0);
    }

    /**
     * Renews the existing subscription by extending its end date by user ID.
     *
     * @param userId the ID of the user
     * @param days number of days to extend
     * @throws UserNotFoundException if user doesn't exist
     */
    public void renewSubscription(int userId, int days) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();
        SubscriptionInfo info = user.getSubscriptionInfo();
        Date now = new Date();

        if (info == null) {
            user.setSubscriptionInfo(new SubscriptionInfo(now, calculateEndDate(now, days)));
        } else {
            Date base = (info.getEndDate() != null && info.getEndDate().after(now))
                    ? info.getEndDate() : now;
            info.setEndDate(calculateEndDate(base, days));
            info.setLastBillingDate(now);
        }

        userRepository.update(user);
        UserService.getInstance().refreshCache();
    }

    /**
     * Renews the existing subscription by extending its end date by username.
     *
     * @param username the username of the user
     * @param days number of days to extend
     * @throws UserNotFoundException if user doesn't exist
     */
    public void renewSubscription(String username, int days) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();
        SubscriptionInfo info = user.getSubscriptionInfo();
        Date now = new Date();

        if (info == null) {
            user.setSubscriptionInfo(new SubscriptionInfo(now, calculateEndDate(now, days)));
        } else {
            Date base = (info.getEndDate() != null && info.getEndDate().after(now))
                    ? info.getEndDate() : now;
            info.setEndDate(calculateEndDate(base, days));
            info.setLastBillingDate(now);
        }

        userRepository.update(user);
        UserService.getInstance().refreshCache();
    }

    /**
     * Checks if the user's subscription is expired by user ID.
     *
     * @param userId the ID of the user
     * @return true if the subscription is expired, false otherwise
     * @throws UserNotFoundException if user doesn't exist
     */
    public boolean isExpired(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();
        return isSubscriptionExpired(user);
    }

    /**
     * Checks if the user's subscription is expired by username.
     *
     * @param username the username of the user
     * @return true if the subscription is expired, false otherwise
     * @throws UserNotFoundException if user doesn't exist
     */
    public boolean isExpired(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();
        return isSubscriptionExpired(user);
    }

    /**
     * Checks if a user has an active subscription of a specific type by user ID.
     *
     * @param userId the ID of the user
     * @param planClass the class of the subscription plan to check
     * @return true if the user has an active subscription of the specified type
     * @throws UserNotFoundException if user doesn't exist
     */
    public boolean hasActiveSubscription(int userId, Class<? extends SubscriptionPlan> planClass) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();
        return hasActiveSubscriptionPlan(user, planClass);
    }

    /**
     * Checks if a user has an active subscription of a specific type by username.
     *
     * @param username the username of the user
     * @param planClass the class of the subscription plan to check
     * @return true if the user has an active subscription of the specified type
     * @throws UserNotFoundException if user doesn't exist
     */
    public boolean hasActiveSubscription(String username, Class<? extends SubscriptionPlan> planClass) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();
        return hasActiveSubscriptionPlan(user, planClass);
    }

    /**
     * Validates that a user has an active premium subscription by user ID.
     *
     * @param userId the ID of the user
     * @throws UserNotFoundException if user doesn't exist
     * @throws SubscriptionException if premium subscription is required but not active
     */
    public void validatePremiumSubscription(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();
        validatePremiumSubscriptionPlan(user);
    }

    /**
     * Validates that a user has an active premium subscription by username.
     *
     * @param username the username of the user
     * @throws UserNotFoundException if user doesn't exist
     * @throws SubscriptionException if premium subscription is required but not active
     */
    public void validatePremiumSubscription(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();
        validatePremiumSubscriptionPlan(user);
    }

    /**
     * Downgrades all users with expired subscriptions to the free plan.
     *
     * @return the number of users downgraded
     */
    public int downgradeExpiredSubscriptions() {
        List<User> users = userRepository.findAll();
        int count = 0;

        for (User user : users) {
            if (isSubscriptionExpired(user)) {
                user.setSubscriptionPlan(new FreeSubscription());
                userRepository.update(user);
                count++;
            }
        }

        if (count > 0) {
            UserService.getInstance().refreshCache();
        }

        return count;
    }

    // Helper methods

    /**
     * Checks if a user's subscription is expired.
     */
    private boolean isSubscriptionExpired(User user) {
        SubscriptionInfo info = user.getSubscriptionInfo();
        return info != null
                && info.getEndDate() != null
                && info.getEndDate().before(new Date());
    }

    /**
     * Checks if a user has an active subscription of a specific type.
     */
    private boolean hasActiveSubscriptionPlan(User user, Class<? extends SubscriptionPlan> planClass) {
        if (user == null || planClass == null) {
            return false;
        }

        SubscriptionPlan plan = user.getSubscriptionPlan();
        return plan != null &&
                planClass.isInstance(plan) &&
                !isSubscriptionExpired(user);
    }

    /**
     * Validates that a user has an active premium subscription.
     */
    private void validatePremiumSubscriptionPlan(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        SubscriptionPlan plan = user.getSubscriptionPlan();
        if (plan == null || !(plan instanceof PremiumSubscription)) {
            throw new SubscriptionException("premium-only feature", "Premium");
        }

        if (isSubscriptionExpired(user)) {
            throw new SubscriptionException("Your Premium subscription has expired");
        }
    }

    /**
     * Calculates an end date from a start date and a number of days.
     */
    private Date calculateEndDate(Date from, int days) {
        if (days <= 0) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}