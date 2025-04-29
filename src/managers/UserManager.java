package managers;

public class UserManager {

    //Singleton instance
    private static UserManager instance;

    //Private constructor
    private UserManager() {}

    //Return single instance
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    //Manage subscription
    SubscriptionManager manager = SubscriptionManager.getInstance();

    //Void for the moment

    //Basic user methods
    void findUserByUsername(){}
    void findUserByEmail(){}

    //Followers methods
    void getFollowers(){}
    void getFollowedUsers(){}

}
