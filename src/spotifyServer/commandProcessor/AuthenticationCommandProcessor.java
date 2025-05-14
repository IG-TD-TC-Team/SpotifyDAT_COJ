package spotifyServer.commandProcessor;

import services.userServices.AuthenticationService;
import services.userServices.exceptions.*;
import factory.UserFactory;
import user.User;

/**
 * Processor that handles authentication-related commands: login, logout, register.
 * This processor manages user authentication through the AuthenticationService
 * and user creation through the UserFactory.
 */
public class AuthenticationCommandProcessor extends AbstractProcessor {
    private final AuthenticationService authService = AuthenticationService.getInstance();
    private final UserFactory userFactory = UserFactory.getInstance();

    @Override
    public String processCommand(String command) {
        String lowerCommand = command.toLowerCase();

        // Check if this is an authentication command
        if (lowerCommand.startsWith("login ")) {
            return handleLogin(command);
        } else if (lowerCommand.equals("logout")) {
            return handleLogout();
        } else if (lowerCommand.startsWith("register ")) {
            return handleRegister(command);
        }

        // Pass to next processor if not an authentication command
        return handleNext(command);
    }

    /**
     * Handles the login command.
     * Format: login <username> <password>
     */
    private String handleLogin(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid login format. Usage: login <username> <password>";
        }

        String username = parts[1];
        String password = parts[2];

        try {
            // Get client address from socket
            String clientAddress = clientSocket.getRemoteSocketAddress().toString();

            // Attempt to authenticate the user
            String sessionId = authService.login(username, password, clientAddress);

            // TODO: Store session ID in connection context
            return "SUCCESS: Logged in successfully. Welcome, " + username + "!";

        } catch (AuthenticationException e) {
            return "ERROR: Authentication failed. Invalid username or password.";
        } catch (UserNotFoundException e) {
            return "ERROR: User not found.";
        } catch (Exception e) {
            return "ERROR: Login failed: " + e.getMessage();
        }
    }

    /**
     * Handles the logout command.
     * This would need session tracking to be fully implemented.
     */
    private String handleLogout() {
        try {
            // TODO: Get session ID from connection context
            // Just return a success message
            return "SUCCESS: Logged out successfully.";

        } catch (Exception e) {
            return "ERROR: Logout failed: " + e.getMessage();
        }
    }

    /**
     * Handles the register command.
     * Format: register <username> <email> <password> <firstName> <lastName>
     */
    private String handleRegister(String command) {
        String[] parts = command.split(" ", 6);

        if (parts.length < 6) {
            return "Error: Invalid registration format. Usage: register <username> <email> <password> <firstName> <lastName>";
        }

        String username = parts[1];
        String email = parts[2];
        String password = parts[3];
        String firstName = parts[4];
        String lastName = parts[5];

        try {
            // Validate email format
            if (!email.contains("@") || !email.contains(".")) {
                return "ERROR: Invalid email format.";
            }

            // Create the new user through UserFactory
            User newUser = userFactory.createUser(firstName, lastName, username, email, password);

            return "SUCCESS: User registered successfully. Welcome, " + newUser.getUsername() + "!";

        } catch (IllegalArgumentException e) {
            return "ERROR: Registration failed: " + e.getMessage();
        } catch (Exception e) {
            return "ERROR: Registration failed: " + e.getMessage();
        }
    }
}