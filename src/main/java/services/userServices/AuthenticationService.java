package services.userServices;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import factory.RepositoryFactory;
import persistence.interfaces.UserRepositoryInterface;
import services.userServices.exceptions.AuthenticationException;
import services.userServices.exceptions.UserNotFoundException;
import user.User;

/**
 * Service responsible for user authentication and session management.
 * Implements the Singleton pattern and Thread-safe session storage for
 * handling multiple concurrent client connections.
 */
public class AuthenticationService {
    // Singleton instance
    private static AuthenticationService instance;

    // SessionManager
    private final SessionManager sessionManager = new SessionManager();

    // Dependencies (keep these unchanged)
    private final UserService userService;
    private final PasswordService passwordService;
    private final UserRepositoryInterface userRepository;



    /**
     * Private constructor with dependency injection.
     */
    private AuthenticationService() {
        this.userService = UserService.getInstance();
        this.passwordService = PasswordService.getInstance();
        this.userRepository = RepositoryFactory.getInstance().getUserRepository();
    }


    /**
     * Returns the singleton instance of AuthenticationService.
     *
     * @return the singleton instance
     */
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Attempts to authenticate a user with the provided credentials.
     * Creates a new session for the user even if they have other active sessions.
     *
     * @param username the username
     * @param password the password
     * @param clientAddress the client's socket address or other identifier
     * @return a new session ID for the authenticated user
     * @throws AuthenticationException if authentication fails
     * @throws UserNotFoundException if the user doesn't exist
     */
    public String login(String username, String password, String clientAddress) {
        try {
            // Get user by username
            User user = userService.getUserByUsername(username);

            // Verify password
            if (!passwordService.verifyPassword(username, password)) {
                throw new AuthenticationException("Invalid credentials");
            }

            // Check if user account is active
            if (!user.isActive()) {
                throw new AuthenticationException("Account is deactivated");
            }

            // Generate a unique session ID
            String sessionId = generateSessionId();

            // Create session using SessionManager
            sessionManager.createSession(sessionId, user.getUserID(), clientAddress);

            System.out.println("User logged in: " + username +
                    " (Session: " + sessionId + ", Client: " + clientAddress + ")");

            return sessionId;

        } catch (UserNotFoundException e) {

            throw new AuthenticationException();
        }
    }

    /**
     * Logs out a user from their current session.
     *
     * @param sessionId the session ID to invalidate
     * @return true if a session was found and invalidated, false otherwise
     */
    public boolean logout(String sessionId) {
        if (sessionId == null) {
            return false;
        }

        Integer userId = sessionManager.removeSession(sessionId);

        if (userId != null) {
            SessionManager.SessionInfo sessionInfo = sessionManager.getSessionInfo(sessionId);
            if (sessionInfo != null) {
                System.out.println("User logged out: ID=" + userId +
                        " (Session: " + sessionId + ", Client: " + sessionInfo.getClientAddress() + ")");
            }
            return true;
        }

        return false;
    }

    /**
     * Checks if a session is authenticated.
     *
     * @param sessionId the session ID to check
     * @return true if the session is authenticated, false otherwise
     */
    public boolean isAuthenticated(String sessionId) {
        return sessionManager.sessionExists(sessionId);
    }

    /**
     * Gets the user ID associated with a session.
     *
     * @param sessionId the session ID
     * @return the user ID, or null if the session is not authenticated
     */
    public Integer getUserIdFromSession(String sessionId) {
        return sessionManager.getUserIdFromSession(sessionId);
    }

    /**
     * Gets the User object associated with a session.
     *
     * @param sessionId the session ID
     * @return the User object, or null if the session is not authenticated
     */
    public User getUserFromSession(String sessionId) {
        Integer userId = sessionManager.getUserIdFromSession(sessionId);

        if (userId != null) {
            try {
                return userService.getUserById(userId);
            } catch (UserNotFoundException e) {
                // If user no longer exists, invalidate the session
                sessionManager.terminateUserSessions(userId);
                return null;
            }
        }

        return null;
    }

    /**
     * Gets all active sessions for a specific user.
     *
     * @param userId the ID of the user
     * @return a map of session IDs to client addresses
     */
    public Map<String, String> getUserSessions(int userId) {
        return sessionManager.getUserSessions(userId);
    }

    /**
     * Terminates all active sessions for a specific user.
     *
     * @param userId the ID of the user whose sessions to terminate
     * @return the number of sessions terminated
     */
    public int terminateUserSessions(int userId) {
        return sessionManager.terminateUserSessions(userId);
    }

    /**
     * Terminates a session by client address.
     * Useful when a client disconnects without proper logout.
     *
     * @param clientAddress the client address
     * @return the number of sessions terminated
     */
    public int terminateClientSessions(String clientAddress) {
        return sessionManager.terminateClientSessions(clientAddress);
    }

    /**
     * Gets the total number of active sessions.
     *
     * @return the number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }

    /**
     * Gets the number of users with active sessions.
     *
     * @return the number of users with active sessions
     */
    public int getActiveUserCount() {
        return sessionManager.getActiveUserCount();
    }

    /**
     * Generates a unique session ID.
     *
     * @return a unique session ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}