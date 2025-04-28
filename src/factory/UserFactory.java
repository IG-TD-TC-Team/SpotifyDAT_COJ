package factory;

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

    // Private constructor
    private UserFactory() {
        this.userRepository = UserRepository.getInstance();
        this.userManager = UserManager.getInstance();
    }

    //Return single instance
    public static synchronized UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    //Basic user methods
    void createUser(){} //manage unique ID + userrepo checks email exist before adding a new user

    void disableAccount(){}
    void enableAccount(){}
    void deleteUser(){}

    void updateUsername(){}
    void updatePassword(){}
    void updateEmail(){}

    //Link with SubscriptionManager
    void subscribeToPremium(){}
    void downgradeToFree(){}
    void downgradeToFreeIfExpired(){}

    //Followers methods
    void followUser(){}
    void unfollowUser(){}
}
