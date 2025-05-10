package services.userServices;

import factory.RepositoryFactory;
import persistence.interfaces.UserRepositoryInterface;
import services.userServices.exceptions.AuthenticationException;
import services.userServices.exceptions.UserNotFoundException;
import user.User;
import user.security.PasswordHasher;
import user.security.SHA256Hasher;

import java.util.Optional;

//Implement cache

/**
 * Service class for password-related operations.
 * Follows the repository-service pattern by using the UserRepository
 * and delegating the password hashing to the PasswordHasher.
 */
public class PasswordService {

    // Singleton instance
    private static PasswordService instance;

    // Dependencies
    private final PasswordHasher passwordHasher;
    private final UserRepositoryInterface userRepository;

    /**
     * Private constructor with dependency injection.
     *
     * @param passwordHasher The password hasher implementation to use
     * @param userRepository The user repository to use
     */
    private PasswordService(PasswordHasher passwordHasher, UserRepositoryInterface userRepository) {
        this.passwordHasher = passwordHasher;
        this.userRepository = RepositoryFactory.getInstance().getUserRepository();
    }

    /**
     * Returns the singleton instance of PasswordService.
     *
     * @return the singleton instance
     */
    public static synchronized PasswordService getInstance() {
        if (instance == null) {
            // Using RepositoryFactory to get the repository instance
            instance = new PasswordService(
                    new SHA256Hasher(),
                    RepositoryFactory.getInstance().getUserRepository()
            );
        }
        return instance;
    }

    /**
     * Changes the password for a user by userId.
     *
     * @param userId the ID of the user
     * @param currentPassword the current password for verification
     * @param newPassword the new password to set
     * @throws UserNotFoundException if user doesn't exist
     * @throws AuthenticationException if current password verification fails
     */
    public void changePassword(int userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordHasher.verify(currentPassword, user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Hash and set new password
        String hashedPassword = passwordHasher.hashWithSalt(newPassword);
        user.setPassword(hashedPassword);

        // Update user in repository
        userRepository.update(user);
    }

    /**
     * Changes the password for a user by username.
     *
     * @param username the username of the user
     * @param currentPassword the current password for verification
     * @param newPassword the new password to set
     * @throws UserNotFoundException if user doesn't exist
     * @throws AuthenticationException if current password verification fails
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordHasher.verify(currentPassword, user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Hash and set new password
        String hashedPassword = passwordHasher.hashWithSalt(newPassword);
        user.setPassword(hashedPassword);

        // Update user in repository
        userRepository.update(user);
    }

    /**
     * Resets a user's password by userId (admin function, no verification required).
     *
     * @param userId the ID of the user
     * @param newPassword the new password to set
     * @throws UserNotFoundException if user doesn't exist
     */
    public void resetPassword(int userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();

        // Hash and set new password
        String hashedPassword = passwordHasher.hashWithSalt(newPassword);
        user.setPassword(hashedPassword);

        // Update user in repository
        userRepository.update(user);
    }

    /**
     * Resets a user's password by username (admin function, no verification required).
     *
     * @param username the username of the user
     * @param newPassword the new password to set
     * @throws UserNotFoundException if user doesn't exist
     */
    public void resetPassword(String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();

        // Hash and set new password
        String hashedPassword = passwordHasher.hashWithSalt(newPassword);
        user.setPassword(hashedPassword);

        // Update user in repository
        userRepository.update(user);
    }

    /**
     * Verifies a password for a user by userId.
     *
     * @param userId the ID of the user
     * @param password the password to verify
     * @return true if password is correct, false otherwise
     * @throws UserNotFoundException if user doesn't exist
     */
    public boolean verifyPassword(int userId, String password) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        User user = userOpt.get();
        return passwordHasher.verify(password, user.getPassword());
    }

    /**
     * Verifies a password for a user by username.
     *
     * @param username the username of the user
     * @param password the password to verify
     * @return true if password is correct, false otherwise
     * @throws UserNotFoundException if user doesn't exist
     */
    public boolean verifyPassword(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("username", username);
        }

        User user = userOpt.get();
        return passwordHasher.verify(password, user.getPassword());
    }

    /**
     * Generates a password hash using the current hashing algorithm.
     *
     * @param password the password to hash
     * @return the hashed password
     */
    public String hashPassword(String password) {
        return passwordHasher.hashWithSalt(password);
    }
}