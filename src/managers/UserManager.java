package managers;

import persistence.UserRepository;
import user.User;

import java.util.List;

public class UserManager {

    //Singleton instance
    private static UserManager instance;

    // Repository for user data
    private final UserRepository userRepository;

    //Private constructor
    private UserManager() {
        this.userRepository = UserRepository.getInstance();
    }

    //Return single instance
    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    //Basic get user methods
    public User getUserById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No user with ID=" + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("No user with username='" + username + "'"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user with email='" + email + "'"));
    }


    //Followers methods
    public List<User> getFollowers(String username) {
        User user = getUserByUsername(username);
        return userRepository.findFollowers(user);
    }

    public List<User> getFollowedUsers(String username) {
        User user = getUserByUsername(username);
        return userRepository.findFollowedUsers(user);
    }

}
