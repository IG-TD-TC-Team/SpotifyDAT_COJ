package spotifyServer;

import java.io.*;
import java.net.Socket;

import spotifyServer.commandProcessor.AbstractProcessor;
import spotifyServer.commandProcessor.*;

public class ClientCommandHandler implements Runnable {
    private final Socket clientSocket;
    private final AbstractProcessor commandProcessor;
    private BufferedReader in;
    private PrintWriter out;

    public ClientCommandHandler(Socket socket, AbstractProcessor commandProcessor) {
        this.clientSocket = socket;
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void run() {
        try {
            // Set up input and output streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Initialize CommandContext with this client socket
            CommandContext.getInstance().setCurrentClient(clientSocket);
            System.out.println("Debug: Set client socket in CommandContext: " + clientSocket);

            // Send welcome message
            out.println("Welcome to Spotify Server. Send commands or type 'exit' to quit.");
            out.println("Type 'help' for a list of available commands.");

            // Process client commands
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Log the received command
                System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " sent: " + inputLine);

                // Check for exit command
                if ("exit".equalsIgnoreCase(inputLine.trim())) {
                    out.println("Goodbye! Closing connection.");
                    break;
                }

                // Process command through the command processor
                String response = commandProcessor.processCommand(inputLine);

                // Check if it's a streaming response
                if (!response.startsWith("STREAMING_STARTED") &&
                        !response.startsWith("PLAYLIST_STREAMING_STARTED")) {

                    out.println(response);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Closed connection from " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}