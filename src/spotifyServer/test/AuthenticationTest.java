package spotifyServer.test;

import services.songServices.SongService;
import services.userServices.AuthenticationService;
import services.userServices.exceptions.AuthenticationException;
import songsAndArtists.Song;
import user.User;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthenticationTest {
    public static final int PORT = 45000;

    public static void main(String[] args) {
        // Create a thread pool to handle multiple client connections
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Spotify Server is listening on port " + PORT);
            System.out.println("You can test with: telnet localhost " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from " + clientSocket.getRemoteSocketAddress());

                    // Handle each client in a separate thread
                    executor.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    // Inner class to handle client connections
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final String clientAddress;
        private final AuthenticationService authService;
        private String sessionId = null;
        private User currentUser = null;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.clientAddress = socket.getRemoteSocketAddress().toString();
            this.authService = AuthenticationService.getInstance();
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                // Send welcome message
                out.println("Welcome to Spotify Server. Send commands or type 'exit' to quit.");
                out.println("Type 'login <username> <password>' to authenticate.");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Log the received command (hide password in login command)
                    String logSafeCommand = inputLine;
                    if (logSafeCommand.startsWith("login ")) {
                        String[] parts = logSafeCommand.split("\\s+", 3);
                        if (parts.length >= 3) {
                            logSafeCommand = "login " + parts[1] + " ********";
                        }
                    }

                    System.out.println("Client " + clientAddress + " sent: " + logSafeCommand);

                    //Send to be processed
                    String response = processCommand(inputLine);

                    // Send response back to client
                    out.println(response);

                    // If client wants to exit, break the loop
                    if ("exit".equalsIgnoreCase(inputLine.trim())) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    // Clean up authentication session if exists
                    if (sessionId != null) {
                        authService.logout(sessionId);
                    }

                    clientSocket.close();
                    System.out.println("Closed connection from " + clientAddress);
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        /// --------- BUSINESS LOGIC --------- ///

        SongService songService = SongService.getInstance();

        private String processCommand(String command) {
            command = command.trim();

            // Login command handling
            if (command.startsWith("login ")) {
                return handleLogin(command);
            }

            // Commands that require authentication
            if (command.startsWith("GetSongs") ||
                    command.startsWith("GetPlaylist") ||
                    command.startsWith("AddToPlaylist") ||
                    command.startsWith("CreatePlaylist")) {

                if (sessionId == null || !authService.isAuthenticated(sessionId)) {
                    return "You must be logged in to perform this action. Use 'login <username> <password>'";
                }
            }

            if ("help".equalsIgnoreCase(command)) {
                return "Available commands: help, time, login <username> <password>, logout, whoami, exit";
            } else if ("time".equalsIgnoreCase(command)) {
                return "Server time: " + LocalDateTime.now();
            } else if ("exit".equalsIgnoreCase(command)) {
                if (sessionId != null) {
                    authService.logout(sessionId);
                    sessionId = null;
                    currentUser = null;
                }
                return "Goodbye! Closing connection.";
            } else if ("logout".equalsIgnoreCase(command)) {
                if (sessionId != null && authService.isAuthenticated(sessionId)) {
                    authService.logout(sessionId);
                    sessionId = null;
                    currentUser = null;
                    return "You have been successfully logged out.";
                } else {
                    return "You are not logged in.";
                }
            } else if ("whoami".equalsIgnoreCase(command)) {
                if (sessionId != null && authService.isAuthenticated(sessionId)) {
                    currentUser = authService.getUserFromSession(sessionId);
                    if (currentUser != null) {
                        return "Logged in as: " + currentUser.getUsername() +
                                " (" + currentUser.getFirstName() + " " + currentUser.getLastName() + ")";
                    }
                }
                return "You are not logged in.";
            } else if (command.startsWith("GetSongs")) {
                return formatSongList(songService.getAllSongs());
            } else {
                // This is where you'll route commands to your business layer
                return "Received command: " + command + " (Not implemented yet)";
            }
        }

        private String handleLogin(String command) {
            String[] parts = command.split("\\s+", 3);

            if (parts.length < 3) {
                return "Usage: login <username> <password>";
            }

            String username = parts[1];
            String password = parts[2];

            try {
                // Log out previous session if exists
                if (sessionId != null) {
                    authService.logout(sessionId);
                }

                // Create new session
                sessionId = authService.login(username, password, clientAddress);
                currentUser = authService.getUserFromSession(sessionId);

                return "Login successful. Welcome, " + currentUser.getFirstName() + "!";
            } catch (AuthenticationException e) {
                return "Login failed: " + e.getMessage();
            }
        }

        private String formatSongList(List<Song> songs) {
            if (songs.isEmpty()) {
                return "No songs found.";
            }

            StringBuilder result = new StringBuilder("Songs found:\n");
            for (Song song : songs) {
                result.append(song.getSongId())
                        .append(": ")
                        .append(song.getTitle())
                        .append(" - Artist ID: ")
                        .append(song.getArtistId())
                        .append(" - Genre: ")
                        .append(song.getGenre())
                        .append("\n");
            }
            return result.toString();
        }
    }
}
