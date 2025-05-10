package spotifyServer.commandProcessor;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.IOException;

public class CommandContext {
    private static CommandContext instance;
    private Socket currentClientSocket;
    private PrintWriter currentOutputStream;

    private CommandContext() {}

    public static synchronized CommandContext getInstance() {
        if (instance == null) {
            instance = new CommandContext();
        }
        return instance;
    }

    public void setCurrentClient(Socket clientSocket) {
        this.currentClientSocket = clientSocket;
        try {
            this.currentOutputStream = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating output stream: " + e.getMessage());
            this.currentOutputStream = null;
        }
    }

    public Socket getCurrentSocket() {
        return currentClientSocket;
    }

    public void sendResponse(String message) {
        if (currentOutputStream != null) {
            currentOutputStream.println(message);
        }
    }

    public void clearCurrentClient() {
        this.currentClientSocket = null;
        this.currentOutputStream = null;
    }
}