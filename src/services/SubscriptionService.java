package services;

import persistence.UserRepository;
import user.*;
import user.subscription.FreeSubscription;
import user.subscription.PremiumSubscription;
import user.subscription.SubscriptionInfo;
import user.subscription.SubscriptionPlan;

import java.util.Calendar;
import java.util.Date;

public class SubscriptionService {

    //Singleton instance
    private static SubscriptionService instance;

    //Private constructor
    private SubscriptionService() {}

    //Return single instance
    public static synchronized SubscriptionService getInstance() {
        if (instance == null) {
            instance = new SubscriptionService();
        }
        return instance;
    }


    /**
     * Create or replace a subscription for the given user.
     * @param user the user to update
     * @param plan subscription plan (FreeSubscription or PremiumSubscription)
     * @param days duration in days; zero or negative means no end date
     */
    public void createSubscription(User user, SubscriptionPlan plan, int days) {
        Date start = new Date();
        Date end = calculateEndDate(start, days);

        user.setSubscriptionPlan(plan);
        user.setSubscriptionInfo(new SubscriptionInfo(start, end));
        persistAndRefresh(user);
    }

    /**
     * Upgrade the user to a 1-year premium subscription.
     */
    public void upgradeToPremium(User user) {
        createSubscription(user, new PremiumSubscription(), 365);
    }

    /**
     * Downgrade the user to a free plan (no expiration).
     */
    public void downgradeToFree(User user) {
        createSubscription(user, new FreeSubscription(), 0);
    }

    /**
     * Renew the existing subscription by extending its end date.
     * If the subscription is expired or missing, behaves like createSubscription.
     * @param user the user to renew
     * @param days number of days to extend
     */
    public void renew(User user, int days) {
        SubscriptionInfo info = user.getSubscriptionInfo();
        Date now = new Date();
        Date base = (info != null && info.getEndDate() != null && info.getEndDate().after(now))
                ? info.getEndDate() : now;
        Date newEnd = calculateEndDate(base, days);

        if (info == null) {
            user.setSubscriptionInfo(new SubscriptionInfo(now, newEnd));
        } else {
            info.setEndDate(newEnd);
        }
        persistAndRefresh(user);
    }

    /**
     * Check if the user's subscription is expired.
     */
    public boolean isExpired(User user) {
        SubscriptionInfo info = user.getSubscriptionInfo();
        return info != null
                && info.getEndDate() != null
                && info.getEndDate().before(new Date());
    }

    // Helper to calculate end date or return null if no expiration
    private Date calculateEndDate(Date from, int days) {
        if (days <= 0) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(from);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    // Persist user changes and refresh cache
    private void persistAndRefresh(User user) {
        UserRepository.getInstance().update(user);
        UserService.getInstance().refreshCache();
    }

    public boolean hasActiveSubscription(User user, Class<? extends SubscriptionPlan> planClass) {
        if (user == null || planClass == null) {
            return false;
        }

        SubscriptionPlan plan = user.getSubscriptionPlan();
        return plan != null &&
                planClass.isInstance(plan) &&
                !isExpired(user);
    }

    public void validatePremiumSubscription(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        SubscriptionPlan plan = user.getSubscriptionPlan();
        if (plan == null || !(plan instanceof PremiumSubscription)) {
            throw new IllegalStateException("Premium subscription required for this operation");
        }

        if (isExpired(user)) {
            throw new IllegalStateException("Premium subscription is expired");
        }
    }
}