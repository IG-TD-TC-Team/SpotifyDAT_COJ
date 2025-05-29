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

            // If already logged in, log out first to ensure clean state
            if (isAuthenticated()) {
                System.out.println("User already authenticated on this connection, logging out previous session first");

                // First log out the current user silently
                String sessionId = connectionContext.getSessionId();
                if (sessionId != null) {
                    authService.logout(sessionId);
                    System.out.println("Logged out previous session: " + sessionId);
                }
                // Deauthenticate the connection but keep the socket open
                CommandContext.deauthenticateConnection(clientSocket);

                // Small delay to ensure cleanup is complete
                Thread.sleep(100);

                System.out.println("You can loggin again !");
            }

            // Attempt to authenticate the user
            String sessionId = authService.login(username, password, clientAddress);

            // Get the authenticated user
            User user = authService.getUserFromSession(sessionId);

            // Update the connection context with authentication info
            CommandContext.authenticateConnection(clientSocket, sessionId, user.getUserID(), username);

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
            // Check if user is authenticated
            if (!isAuthenticated()) {
                return "ERROR: You are not logged in.";
            }

            // Get current user info for logging
            String currentUsername = getCurrentUsername();
            Integer currentUserId = getCurrentUserId();

            // Get session ID from context
            String sessionId = connectionContext.getSessionId();

            // Logout through authentication service
            boolean logoutSuccess = authService.logout(sessionId);

            // Clear authentication from connection context
            CommandContext.deauthenticateConnection(clientSocket);

            if (logoutSuccess) {
                System.out.println("User logged out: " + currentUsername + " (ID: " + currentUserId +
                        ") session: " + sessionId);
                return "SUCCESS: Logged out successfully.";
            } else {
                System.out.println("Logout attempt for invalid session: " + sessionId);
                return "SUCCESS: Logged out (session was already invalid).";
            }

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