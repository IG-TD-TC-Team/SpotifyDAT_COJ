package spotifyServer.commandProcessor;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CommandContext class is a singleton that manages the current client socket and output stream.
 * It provides methods to set the current client, send responses, and clear the current client.
 * It also manages connection contexts for each client socket, allowing for authentication and de-authentication.
 *
 */
public class CommandContext {
    // Singleton instance
    private static CommandContext instance;
    // Current client socket and output stream
    private Socket currentClientSocket;
    private PrintWriter currentOutputStream;

    // Connection contexts for each client socket
    private static final ConcurrentHashMap<Socket, ConnectionInfo> connectionContexts = new ConcurrentHashMap<>();

    private CommandContext() {}

    public static synchronized CommandContext getInstance() {
        if (instance == null) {
            instance = new CommandContext();
        }
        return instance;
    }

    /**
     * Sets the current client socket and initializes the output stream.
     *
     * @param clientSocket The socket of the current client.
     */
    public void setCurrentClient(Socket clientSocket) {
        this.currentClientSocket = clientSocket;
        try {
            this.currentOutputStream = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating output stream: " + e.getMessage());
            this.currentOutputStream = null;
        }
    }

    /**
     * Gets the current client socket.
     *
     * @return The current client socket.
     */
    public Socket getCurrentSocket() {
        return currentClientSocket;
    }

    /**
     * Sends a response message to the current client.
     *
     * @param message The message to send.
     */
    public void sendResponse(String message) {
        if (currentOutputStream != null) {
            currentOutputStream.println(message);
        }
    }

    /**
     * Clears the current client socket and output stream.
     */
    public void clearCurrentClient() {
        this.currentClientSocket = null;
        this.currentOutputStream = null;
    }



    /**
     * Creates a new connection context for a client
     */
    public static ConnectionInfo createContext(Socket clientSocket) throws IOException {
        ConnectionInfo context = new ConnectionInfo(clientSocket);
        connectionContexts.put(clientSocket, context);
        System.out.println("Created context for client: " + clientSocket.getRemoteSocketAddress());
        return context;
    }

    /**
     * Gets the connection context for a specific socket
     */
    public static ConnectionInfo getContext(Socket socket) {
        return connectionContexts.get(socket);
    }

    /**
     * Updates the context when a user logs in
     */
    public static void authenticateConnection(Socket socket, String sessionId, int userId, String username) {
        ConnectionInfo context = connectionContexts.get(socket);
        if (context != null) {
            context.setSessionId(sessionId);
            context.setUserId(userId);
            context.setUsername(username);
            System.out.println("Authenticated connection for user: " + username + " (ID: " + userId + ")");
        }
    }

    /**
     * Clears authentication info when a user logs out
     */
    public static void deauthenticateConnection(Socket socket) {
        ConnectionInfo context = connectionContexts.get(socket);
        if (context != null) {
            System.out.println("Deauthenticated user: " + context.getUsername());
            context.setSessionId(null);
            context.setUserId(null);
            context.setUsername(null);
        }
    }

    /**
     * Removes the context when a client disconnects
     */
    public static void removeContext(Socket socket) {
        ConnectionInfo removed = connectionContexts.remove(socket);
        if (removed != null) {
            System.out.println("Removed context for client: " + socket.getRemoteSocketAddress());
        }
    }

    /**
     * Gets the current authenticated user ID for a socket
     */
    public static Integer getCurrentUserId(Socket socket) {
        ConnectionInfo context = connectionContexts.get(socket);
        return context != null ? context.getUserId() : null;
    }

    /**
     * Checks if a connection is authenticated
     */
    public static boolean isAuthenticated(Socket socket) {
        ConnectionInfo context = connectionContexts.get(socket);
        return context != null && context.isAuthenticated();
    }

    /**
     * Inner class to hold all connection-specific information
     */
    public static class ConnectionInfo {
        private final Socket socket;
        private final PrintWriter out;
        private String sessionId;
        private Integer userId;
        private String username;
        private String ipAddress;
        private long connectionTime;

        public ConnectionInfo(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.ipAddress = socket.getInetAddress().getHostAddress();
            this.connectionTime = System.currentTimeMillis();
        }

        // Getters and setters
        public Socket getSocket() { return socket; }
        public PrintWriter getOut() { return out; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getIpAddress() { return ipAddress; }
        public long getConnectionTime() { return connectionTime; }

        public boolean isAuthenticated() {
            return sessionId != null && userId != null;
        }
    }
}
