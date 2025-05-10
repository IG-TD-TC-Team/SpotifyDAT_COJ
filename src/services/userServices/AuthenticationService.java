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

    // Thread-safe map to store active user sessions by sessionId
    private final Map<String, Integer> sessionToUserMap = new ConcurrentHashMap<>();

    // Thread-safe map to store sessionIds by userId (one user can have multiple sessions)
    private final Map<Integer, Map<String, SessionInfo>> userToSessionsMap = new ConcurrentHashMap<>();

    // Dependencies
    private final UserService userService;
    private final PasswordService passwordService;
    private final UserRepositoryInterface userRepository;

    /**
     * Inner class to store session information
     */
    private static class SessionInfo {
        private final String clientAddress;
        private final long loginTime;

        public SessionInfo(String clientAddress) {
            this.clientAddress = clientAddress;
            this.loginTime = System.currentTimeMillis();
        }

        public String getClientAddress() {
            return clientAddress;
        }

        public long getLoginTime() {
            return loginTime;
        }
    }

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

            // Store session in sessionToUserMap
            sessionToUserMap.put(sessionId, user.getUserID());

            // Store session in userToSessionsMap
            Map<String, SessionInfo> userSessions = userToSessionsMap.computeIfAbsent(
                    user.getUserID(), k -> new ConcurrentHashMap<>()
            );
            userSessions.put(sessionId, new SessionInfo(clientAddress));

            System.out.println("User logged in: " + username +
                    " (Session: " + sessionId + ", Client: " + clientAddress + ")");

            return sessionId;

        } catch (UserNotFoundException e) {
            // For security reasons, don't specify whether username or password was incorrect
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
        Integer userId = sessionToUserMap.remove(sessionId);

        if (userId != null) {
            // Remove from user's sessions
            Map<String, SessionInfo> userSessions = userToSessionsMap.get(userId);
            if (userSessions != null) {
                SessionInfo sessionInfo = userSessions.remove(sessionId);
                if (sessionInfo != null) {
                    System.out.println("User logged out: ID=" + userId +
                            " (Session: " + sessionId + ", Client: " + sessionInfo.getClientAddress() + ")");
                }

                // If user has no more sessions, remove from map
                if (userSessions.isEmpty()) {
                    userToSessionsMap.remove(userId);
                }
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
        return sessionToUserMap.containsKey(sessionId);
    }

    /**
     * Gets the user ID associated with a session.
     *
     * @param sessionId the session ID
     * @return the user ID, or null if the session is not authenticated
     */
    public Integer getUserIdFromSession(String sessionId) {
        return sessionToUserMap.get(sessionId);
    }

    /**
     * Gets the User object associated with a session.
     *
     * @param sessionId the session ID
     * @return the User object, or null if the session is not authenticated
     */
    public User getUserFromSession(String sessionId) {
        Integer userId = getUserIdFromSession(sessionId);

        if (userId != null) {
            try {
                return userService.getUserById(userId);
            } catch (UserNotFoundException e) {
                // If user no longer exists, invalidate the session
                terminateUserSessions(userId);
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
        Map<String, String> result = new ConcurrentHashMap<>();
        Map<String, SessionInfo> sessions = userToSessionsMap.get(userId);

        if (sessions != null) {
            for (Map.Entry<String, SessionInfo> entry : sessions.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getClientAddress());
            }
        }

        return result;
    }

    /**
     * Terminates all active sessions for a specific user.
     *
     * @param userId the ID of the user whose sessions to terminate
     * @return the number of sessions terminated
     */
    public int terminateUserSessions(int userId) {
        Map<String, SessionInfo> sessions = userToSessionsMap.remove(userId);

        if (sessions == null) {
            return 0;
        }

        int count = 0;
        for (String sessionId : sessions.keySet()) {
            if (sessionToUserMap.remove(sessionId) != null) {
                count++;
            }
        }

        return count;
    }

    /**
     * Terminates a session by client address.
     * Useful when a client disconnects without proper logout.
     *
     * @param clientAddress the client address
     * @return the number of sessions terminated
     */
    public int terminateClientSessions(String clientAddress) {
        int count = 0;

        // Find all sessions with this client address
        for (Map.Entry<Integer, Map<String, SessionInfo>> userEntry : userToSessionsMap.entrySet()) {
            Map<String, SessionInfo> sessions = userEntry.getValue();

            // Get all sessions for this user that match the client address
            for (Map.Entry<String, SessionInfo> sessionEntry : sessions.entrySet()) {
                if (sessionEntry.getValue().getClientAddress().equals(clientAddress)) {
                    String sessionId = sessionEntry.getKey();
                    sessions.remove(sessionId);
                    sessionToUserMap.remove(sessionId);
                    count++;
                }
            }

            // If user has no more sessions, remove from map
            if (sessions.isEmpty()) {
                userToSessionsMap.remove(userEntry.getKey());
            }
        }

        return count;
    }

    /**
     * Gets the total number of active sessions.
     *
     * @return the number of active sessions
     */
    public int getActiveSessionCount() {
        return sessionToUserMap.size();
    }

    /**
     * Gets the number of users with active sessions.
     *
     * @return the number of users with active sessions
     */
    public int getActiveUserCount() {
        return userToSessionsMap.size();
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