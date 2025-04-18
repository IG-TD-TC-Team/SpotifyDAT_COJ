package persistence;

import user.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserRepository extends JsonRepository<User>{

    //Singleton instance
    private static UserRepository instance;

    //Private constructor
    private UserRepository() {
        super(User.class, "users.json");
    }

    //Return single instance
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // Retrieve
    public Optional<User> findById(int userId) {
        return findAll().stream()
                .filter(user -> user.getUserID() == userId)
                .findFirst();
    }

    public Optional<User> findByUsername(String username){
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    public Optional<User> findByEmail(String email){
        return findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    public List<User> findUsersByFirstName(String firstName) {
        return findAll().stream()
                .filter(user -> user.getFirstName().equalsIgnoreCase(firstName))
                .collect(Collectors.toList());
    }

    public List<User> findUsersByLastName(String lastName) {
        return findAll().stream()
                .filter(user -> user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    public List<User> findUsersByFullName(String firstName, String lastName) {
        return findAll().stream()
                .filter(user -> user.getFirstName().equalsIgnoreCase(firstName) &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }


    // findAll already inherited

    // Add
    public boolean emailExists(String email){
        return findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public boolean usernameExists(String username){
        return findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    public void add(User user){
        if (!emailExists(user.getEmail())) {
            super.add(user); // save to users.json
        } else {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
    }

    // Modify globally (specifications in UserManager
    public void update(User user) {
        //Load all users
        List<User> users = findAll();
        //Check for the user to update by id
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserID() == user.getUserID()) {
                //Replace it
                users.set(i, user);
                //Save all users back to the json
                saveAll(users);
                return;
            }
        }
        throw new IllegalArgumentException("User with ID " + user.getUserID() + " not found.");
    }

    // Delete
    public void delete(User user){
        List<User> users = findAll();
        boolean removed = users.removeIf(u -> u.getUserID() == user.getUserID());
        if (removed) {
            saveAll(users); // Save the updated list back to JSON
        } else {
            throw new IllegalArgumentException("User with ID " + user.getUserID() + " not found.");
        }
    }

    // Authenticate
    public boolean checkCredentialsByUsername(String username, String password){
        return findByUsername(username)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
    public boolean checkCredentialsByEmail(String email, String password){
        return findByEmail(email)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    // Retrieve follower
    public List<User> findFollowers(User user) {
        return user.getFollowers();
    }
    public List<User> findUsersFollowed(User user) {
        return user.getFollowedUsers();
    }

    //Check for one user
    public Optional<User> findFollowerByUsername(User user, String username) {
        return user.getFollowers().stream()
                .filter(f -> f.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<User> findFollowersByFirstName(User user, String firstName) {
        return user.getFollowers().stream()
                .filter(f -> f.getFirstName().equalsIgnoreCase(firstName))
                .collect(Collectors.toList());
    }

    public List<User> findFollowersByLastName(User user, String lastName) {
        return user.getFollowers().stream()
                .filter(f -> f.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    public List<User> findFollowersByFullName(User user, String firstName, String lastName) {
        return user.getFollowers().stream()
                .filter(f -> f.getFirstName().equalsIgnoreCase(firstName) &&
                        f.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    //Check for one followed user
    public Optional<User> findFollowedUserByUsername(User user, String username) {
        return user.getFollowedUsers().stream()
                .filter(f -> f.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<User> findFollowedUsersByFirstName(User user, String firstName) {
        return user.getFollowedUsers().stream()
                .filter(f -> f.getFirstName().equalsIgnoreCase(firstName))
                .collect(Collectors.toList());
    }

    public List<User> findFollowedUsersByLastName(User user, String lastName) {
        return user.getFollowedUsers().stream()
                .filter(f -> f.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    public List<User> findFollowedUsersByFullName(User user, String firstName, String lastName) {
        return user.getFollowedUsers().stream()
                .filter(f -> f.getFirstName().equalsIgnoreCase(firstName) &&
                        f.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }


}
