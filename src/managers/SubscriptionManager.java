package managers;

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

    public boolean isExpired() {
        return false;
    }
    //When creating check that enddate is after startdate

    public void renew(int months){}

    public void downgradeToFree(){}

    public void upgradeToPremium(){}

    //public void cancel(){}

    public int daysRemaining() {
        return 0;
    }

}
