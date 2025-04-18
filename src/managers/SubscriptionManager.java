package managers;

import user.SubscriptionPlan;
import user.User;

public class SubscriptionManager {

    //Singleton instance
    private static SubscriptionManager instance;

    //Private constructor
    private SubscriptionManager() {}

    //Return single instance
    public static synchronized SubscriptionManager getInstance() {
        if (instance == null) {
            instance = new SubscriptionManager();
        }
        return instance;
    }

    private User user;

    public void createSubscription(User user, SubscriptionPlan subscriptionPlan, int days) {

    }

    public boolean isExpired() {
        //Check dates
        return false;
    }

    public void renew(User user, int days){}

    public void downgradeToFree(User user){}

    public void upgradeToPremium(User user){}


    public int getSubscriptionInfos(User user) {
        return 0;
    }

}
