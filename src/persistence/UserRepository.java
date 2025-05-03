package persistence;

import persistence.interfaces.UserRepositoryInterface;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserRepository implements UserRepositoryInterface for persisting User entities.
 * Follows pure repository pattern by focusing only on data access operations.
 */
public class UserRepository extends JsonRepository<User> implements UserRepositoryInterface {

    private static UserRepository instance;

    private UserRepository() {
        super(User.class, "users.json", User::getUserID);
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean userIdExists(int userId) {
        return findById(userId).isPresent();
    }

    @Override
    public boolean usernameExists(String username) {
        return findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public boolean emailExists(String email) {
        return findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public List<User> findUsersByFirstName(String firstName) {
        return findAll().stream()
                .filter(user -> user.getFirstName() != null &&
                        user.getFirstName().equalsIgnoreCase(firstName))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findUsersByLastName(String lastName) {
        return findAll().stream()
                .filter(user -> user.getLastName() != null &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findUsersByFullName(String firstName, String lastName) {
        return findAll().stream()
                .filter(user -> user.getFirstName() != null &&
                        user.getFirstName().equalsIgnoreCase(firstName) &&
                        user.getLastName() != null &&
                        user.getLastName().equalsIgnoreCase(lastName))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findActiveUsers() {
        return findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findInactiveUsers() {
        return findAll().stream()
                .filter(user -> !user.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getFollowerIds(int userId) {
        Optional<User> userOpt = findById(userId);
        return userOpt.map(User::getFollowersIDs).orElse(new ArrayList<>());
    }

    @Override
    public List<Integer> getFollowedUserIds(int userId) {
        Optional<User> userOpt = findById(userId);
        return userOpt.map(User::getFollowedUsersIDs).orElse(new ArrayList<>());
    }

    @Override
    public boolean addFollower(int userId, int followerId) {
        Optional<User> userOpt = findById(userId);
        Optional<User> followerOpt = findById(followerId);

        if (userOpt.isEmpty() || followerOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        User follower = followerOpt.get();

        List<Integer> followers = user.getFollowersIDs();
        if (followers == null) {
            followers = new ArrayList<>();
            user.setFollowersIDs(followers);
        }

        List<Integer> following = follower.getFollowedUsersIDs();
        if (following == null) {
            following = new ArrayList<>();
            follower.setFollowedUsersIDs(following);
        }

        if (!followers.contains(followerId)) {
            followers.add(followerId);
            following.add(userId);

            update(user);
            update(follower);
            return true;
        }

        return false;
    }

    @Override
    public boolean removeFollower(int userId, int followerId) {
        Optional<User> userOpt = findById(userId);
        Optional<User> followerOpt = findById(followerId);

        if (userOpt.isEmpty() || followerOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        User follower = followerOpt.get();

        List<Integer> followers = user.getFollowersIDs();
        List<Integer> following = follower.getFollowedUsersIDs();

        boolean removed = false;

        if (followers != null && followers.remove(Integer.valueOf(followerId))) {
            removed = true;
        }

        if (following != null && following.remove(Integer.valueOf(userId))) {
            removed = true;
        }

        if (removed) {
            update(user);
            update(follower);
            return true;
        }

        return false;
    }

    @Override
    public User save(User user) {
        // No validation here - moved to service layer
        return super.save(user);
    }

    @Override
    public Optional<User> update(User user) {
        // No validation here - moved to service layer
        return super.update(user);
    }

    @Override
    public boolean deleteById(int userId) {
        return super.deleteById(userId);
    }
}