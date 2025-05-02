package spotifyServer;

import managers.SongService;
import songsAndArtists.Song;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifySocketServer {
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

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                // Send welcome message
                out.println("Welcome to Spotify Server. Send commands or type 'exit' to quit.");

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Log the received command
                    System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " sent: " + inputLine);

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
                    clientSocket.close();
                    System.out.println("Closed connection from " + clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        /// --------- BUSINESS LOGIC --------- ///

        SongService songService = SongService.getInstance();

        private String processCommand(String command) {
            // For now, simple command processing
            command = command.trim();

            if ("help".equalsIgnoreCase(command)) {
                return "Available commands: help, time, exit";
            } else if ("time".equalsIgnoreCase(command)) {
                return "Server time: " + LocalDateTime.now();
            } else if ("exit".equalsIgnoreCase(command)) {
                return "Goodbye! Closing connection.";
            } else if (command.startsWith("GetSongs")) {
                return formatSongList(songService.getAllSongs());

            } else {
                // This is where you'll route commands to your business layer
                return "Received command: " + command + " (Not implemented yet)";
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