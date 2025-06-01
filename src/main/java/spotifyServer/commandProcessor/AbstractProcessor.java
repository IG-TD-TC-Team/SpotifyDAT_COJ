package spotifyServer.commandProcessor;

import java.io.*;
import java.net.Socket;

/**
 * AbstractProcessor that handles text-based command processing only.
 * Streaming is delegated to the dedicated streaming server on a separate port.
 *
 * This implementation:
 * 1. Processes text commands only
 * 2. Does not handle binary streaming directly
 * 3. Uses a chain of responsibility pattern for command processing
 */
public abstract class AbstractProcessor {
    protected AbstractProcessor nextProcessor;
    protected Socket clientSocket;
    protected BufferedReader in;
    protected PrintWriter out;
    // The connection context for the command
    protected CommandContext.ConnectionInfo connectionContext;

    /**
     * Sets the next processor in the chain.
     *
     * @param nextProcessor The next processor to handle commands
     */
    public void setNextProcessor(AbstractProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    /**
     * Sets the client socket and initializes text-based I/O streams.
     * Note: We only set up text streams, not binary streams.
     *
     * @param clientSocket The socket for this client connection
     * @throws IOException If there's an error setting up the streams
     */
    public void setClientSocket(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;

        // Try to get existing context or create new one
        this.connectionContext = CommandContext.getContext(clientSocket);
        if (this.connectionContext == null) {
            this.connectionContext = CommandContext.createContext(clientSocket);
        }

        // Initialize text-based I/O streams
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = connectionContext.getOut();
    }

    /**
     * Gets the current authenticated user ID from the context
     */
    protected Integer getCurrentUserId() {
        return connectionContext != null ? connectionContext.getUserId() : null;
    }

    /**
     * Checks if the current connection is authenticated
     */
    protected boolean isAuthenticated() {
        return connectionContext != null && connectionContext.isAuthenticated();
    }

    /**
     * Gets the current username from the context
     */
    protected String getCurrentUsername() {
        return connectionContext != null ? connectionContext.getUsername() : null;
    }

    /**
     * Gets the current client socket.
     *
     * @return The client socket
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Main processing method that handles client command processing.
     * This method only handles text-based commands and responses.
     */
    public void processClient() {
        try {
            // Send welcome message
            out.println("Welcome to Spotify Server. Send commands or type 'exit' to quit.");
            out.println("Type 'help' for a list of available commands.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " sent: " + inputLine);

                // Check for exit command
                if ("exit".equalsIgnoreCase(inputLine.trim())) {
                    out.println("Goodbye! Closing connection.");
                    break;
                }

                // Process the command through the chain
                String response = processCommand(inputLine);

                // Always send response for command processing
                // (Streaming instructions are text-based too)
                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Abstract method that each concrete processor must implement.
     * Processes text commands and returns text responses only.
     *
     * @param command The text command to process
     * @return The text response to send to the client
     */
    public abstract String processCommand(String command);

    /**
     * Handles passing the command to the next processor in the chain.
     *
     * @param command The command to pass along
     * @return The response from the chain
     */
    protected String handleNext(String command) {
        if (nextProcessor != null) {
            // Pass along the socket context to the next processor
            try {
                nextProcessor.setClientSocket(clientSocket);
                return nextProcessor.processCommand(command);
            } catch (IOException e) {
                return "Error: Failed to set client context - " + e.getMessage();
            }
        }
        return "Unknown command. Type 'help' for available commands.";
    }

    /**
     * Sends a text response to the client.
     *
     * @param message The text message to send
     */
    protected void sendResponse(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Closes the client connection and cleans up resources.
     */
    protected void closeConnection() {
        try {
            // Remove the context when closing
            CommandContext.removeContext(clientSocket);

            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Closed connection from " + clientSocket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}