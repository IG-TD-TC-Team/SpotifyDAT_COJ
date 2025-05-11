package spotifyServer.commandProcessor;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * CommandContext class is a singleton that manages the current client socket and output stream.
 * It provides methods to set the current client, send responses, and clear the current client.
 */
public class CommandContext {
    // Singleton instance
    private static CommandContext instance;
    // Current client socket and output stream
    private Socket currentClientSocket;
    private PrintWriter currentOutputStream;

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
}