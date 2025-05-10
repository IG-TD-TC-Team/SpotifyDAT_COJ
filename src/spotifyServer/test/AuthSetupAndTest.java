package spotifyServer.test;

import factory.UserFactory;
import persistence.UserRepository;
import user.User;
import user.security.SHA256Hasher;

public class AuthSetupAndTest {

    public static void main(String[] args) {
        // Create a test user with a simple password
        createTestUser("testuser", "testpass", "test@example.com");

        // Start the authentication test server
        AuthenticationTest.main(args);
    }

    /**
     * Creates a test user with a known plain password for testing authentication
     */
    private static void createTestUser(String username, String password, String email) {
        try {
            // Make sure user doesn't already exist
            UserRepository userRepo = UserRepository.getInstance();
            if (userRepo.usernameExists(username)) {
                System.out.println("Test user '" + username + "' already exists. You can use it to test authentication.");
                return;
            }

            // Create user with the UserFactory
            UserFactory userFactory = UserFactory.getInstance();

            // Create a simple hashed password
            String hashedPassword = new SHA256Hasher().hash(password);

            // Use reflection to access protected createUser method with our custom hash
            User user = userFactory.createUser(
                    username, // firstName
                    "TestUser", // lastName
                    username, // username
                    email,
                    password  // Password will be hashed internally by the factory
            );

            System.out.println("Created test user:");
            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            System.out.println("Email: " + email);
            System.out.println("\nYou can now use these credentials to test authentication with telnet:");
            System.out.println("telnet localhost 45000");
            System.out.println("login " + username + " " + password);

        } catch (Exception e) {
            System.err.println("Error creating test user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}