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
    void createUser(){}
    void login(){}
    void logout(){}

    void disableAccount(){}
    void enableAccount(){}
    void deleteUser(){}

    void updateUsername(){}
    void updatePassword(){}
    void updateEmail(){}

    void findUserByUsername(){}
    void findUserByEmail(){}

    //Link with SubscriptionManager
    void subscribeToPremium(){}
    void downgradeToFree(){}
    void downgradeToFreeIfExpired(){}

    //Followers methods
    void followUser(){}
    void unfollowUser(){}
    void getFollowers(){}
    void getFollowedUsers(){}

}
