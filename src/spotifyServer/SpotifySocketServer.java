package spotifyServer;

import services.playlistServices.PlaylistService;
import services.songServices.SongService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

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
        PlaylistService playlistService = PlaylistService.getInstance();


        private String processCommand(String command) {
            // For now, simple command processing
            command = command.trim();

            switch (command) {
                case "help":
                    return "Available commands: help, time, exit";
                case "time":
                    return "Server time: " + LocalDateTime.now();
                case "exit":
                    return "Goodbye! Closing connection.";
                case "list songs":
                    List<Song> songs = songService.getAllSongs();
                    return formatSongList(songs);
                case "list playlists":
                    List<Playlist> playlists = playlistService.getAllPlaylists();
                    StringBuilder result = new StringBuilder("Playlists found:\n");
                    for (Playlist playlist : playlists) {
                        result.append(playlist.getPlaylistID())
                                .append(": ")
                                .append(playlist.getName())
                                .append(" - Owner ID: ")
                                .append(playlist.getOwnerID())
                                .append("\n");
                    }
                    default:
                    break;
            }
            //return "Unknown command: " + command;
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