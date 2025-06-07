package services.userServices;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user sessions in a thread-safe manner.
 *
 * <p>This class encapsulates all session-related operations including creation,
 * validation, and cleanup of user sessions. It maintains two primary data structures:</p>
 * <ul>
 *   <li>Session ID to User ID mapping for quick authentication checks</li>
 *   <li>User ID to Session Details mapping for managing multiple sessions per user</li>
 * </ul>
 *
 * <p>All operations are thread-safe and support concurrent access from multiple
 * client connections.</p>
 */
public class SessionManager {

    // Thread-safe map to store active user sessions by sessionId
    private final Map<String, Integer> sessionToUserMap = new ConcurrentHashMap<>();

    // Thread-safe map to store sessionIds by userId (one user can have multiple sessions)
    private final Map<Integer, Map<String, SessionInfo>> userToSessionsMap = new ConcurrentHashMap<>();

    /**
     * Stores session information including client details and timing.
     */
    public static class SessionInfo {
        private final String clientAddress;
        private final long loginTime;

        /**
         * Creates session information for a client.
         *
         * @param clientAddress the client's address or identifier
         */
        public SessionInfo(String clientAddress) {
            this.clientAddress = clientAddress;
            this.loginTime = System.currentTimeMillis();
        }

        /**
         * Gets the client address associated with this session.
         *
         * @return the client address
         */
        public String getClientAddress() {
            return clientAddress;
        }

        /**
         * Gets the login time for this session.
         *
         * @return the login time in milliseconds since epoch
         */
        public long getLoginTime() {
            return loginTime;
        }
    }

    /**
     * Creates a new session for a user.
     *
     * @param sessionId the unique session identifier
     * @param userId the user ID to associate with the session
     * @param clientAddress the client's address
     */
    public void createSession(String sessionId, Integer userId, String clientAddress) {
        // Store session in sessionToUserMap
        sessionToUserMap.put(sessionId, userId);

        // Store session in userToSessionsMap
        Map<String, SessionInfo> userSessions = userToSessionsMap.computeIfAbsent(
                userId, k -> new ConcurrentHashMap<>()
        );
        userSessions.put(sessionId, new SessionInfo(clientAddress));
    }

    /**
     * Removes a session by session ID.
     *
     * @param sessionId the session ID to remove
     * @return the user ID that was associated with the session, or null if not found
     */
    public Integer removeSession(String sessionId) {
        Integer userId = sessionToUserMap.remove(sessionId);

        if (userId != null) {
            // Remove from user's sessions
            Map<String, SessionInfo> userSessions = userToSessionsMap.get(userId);
            if (userSessions != null) {
                userSessions.remove(sessionId);

                // If user has no more sessions, remove from map
                if (userSessions.isEmpty()) {
                    userToSessionsMap.remove(userId);
                }
            }
        }

        return userId;
    }

    /**
     * Checks if a session exists.
     *
     * @param sessionId the session ID to check
     * @return true if the session exists, false otherwise
     */
    public boolean sessionExists(String sessionId) {
        return sessionToUserMap.containsKey(sessionId);
    }

    /**
     * Gets the user ID associated with a session.
     *
     * @param sessionId the session ID
     * @return the user ID, or null if session doesn't exist
     */
    public Integer getUserIdFromSession(String sessionId) {
        return sessionToUserMap.get(sessionId);
    }

    /**
     * Gets all sessions for a specific user.
     *
     * @param userId the user ID
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
     * Terminates all sessions for a specific user.
     *
     * @param userId the user ID whose sessions to terminate
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
     * Terminates sessions by client address.
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
            sessions.entrySet().removeIf(sessionEntry -> {
                if (sessionEntry.getValue().getClientAddress().equals(clientAddress)) {
                    sessionToUserMap.remove(sessionEntry.getKey());
                    return true;
                }
                return false;
            });

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
     * Gets session information for a specific session.
     *
     * @param sessionId the session ID
     * @return the SessionInfo object, or null if session doesn't exist
     */
    public SessionInfo getSessionInfo(String sessionId) {
        Integer userId = sessionToUserMap.get(sessionId);
        if (userId != null) {
            Map<String, SessionInfo> userSessions = userToSessionsMap.get(userId);
            if (userSessions != null) {
                return userSessions.get(sessionId);
            }
        }
        return null;
    }
}