package cache;

import persistence.interfaces.UserRepositoryInterface;
import user.User;
import java.util.List;
import java.util.Optional;

public class CachedUserRepositoryWrapper implements UserRepositoryInterface {
    private final CachedRepository<User> cachedRepo;
    private final UserRepositoryInterface original;

    public CachedUserRepositoryWrapper(CachedRepository<User> cachedRepo, UserRepositoryInterface original) {
        this.cachedRepo = cachedRepo;
        this.original = original;
    }

    // Basic CRUD operations delegate to CachedRepository
    @Override
    public List<User> findAll() {
        return cachedRepo.findAll();
    }

    @Override
    public Optional<User> findById(int id) {
        return cachedRepo.findById(id);
    }

    @Override
    public User save(User user) {
        return cachedRepo.save(user);
    }

    @Override
    public void saveAll(List<User> users) {
        cachedRepo.saveAll(users);
    }

    @Override
    public Optional<User> update(User user) {
        return cachedRepo.update(user);
    }

    @Override
    public boolean deleteById(int id) {
        return cachedRepo.deleteById(id);
    }

    // Special methods with filtering (no cache)
    @Override
    public Optional<User> findByUsername(String username) {
        return original.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return original.findByEmail(email);
    }

    @Override
    public boolean userIdExists(int userId) {
        return original.userIdExists(userId);
    }

    @Override
    public boolean usernameExists(String username) {
        return original.usernameExists(username);
    }

    @Override
    public boolean emailExists(String email) {
        return original.emailExists(email);
    }

    @Override
    public List<User> findUsersByFirstName(String firstName) {
        return original.findUsersByFirstName(firstName);
    }

    @Override
    public List<User> findUsersByLastName(String lastName) {
        return original.findUsersByLastName(lastName);
    }

    @Override
    public List<User> findUsersByFullName(String firstName, String lastName) {
        return original.findUsersByFullName(firstName, lastName);
    }

    @Override
    public List<User> findActiveUsers() {
        return original.findActiveUsers();
    }

    @Override
    public List<User> findInactiveUsers() {
        return original.findInactiveUsers();
    }

    @Override
    public List<Integer> getFollowerIds(int userId) {
        return original.getFollowerIds(userId);
    }

    @Override
    public List<Integer> getFollowedUserIds(int userId) {
        return original.getFollowedUserIds(userId);
    }

    @Override
    public boolean addFollower(int userId, int followerId) {
        return original.addFollower(userId, followerId);
    }

    @Override
    public boolean removeFollower(int userId, int followerId) {
        return original.removeFollower(userId, followerId);
    }
}