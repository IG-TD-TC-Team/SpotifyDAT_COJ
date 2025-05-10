package spotifyServer;

import java.io.*;
import java.net.Socket;

public class MusicStreamHandler implements Runnable {
    private final Socket clientSocket;
    private final MusicStreamer musicStreamer;

    public MusicStreamHandler(Socket socket, MusicStreamer musicStreamer) {
        this.clientSocket = socket;
        this.musicStreamer = musicStreamer;
    }

    // Alternative constructor that creates its own streamer
    public MusicStreamHandler(Socket socket) {
        this.clientSocket = socket;
        this.musicStreamer = new MusicStreamer();
    }

    @Override
    public void run() {
        try {
            // Read the file path from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request = in.readLine();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            if (request == null || request.isEmpty()) {
                out.println("ERROR: No file path provided");
                return;
            }

            System.out.println("Streaming request: " + request);

            // Check if it's a playlist or single song
            if (request.startsWith("PLAYLIST|")) {
                // It's a playlist - format: PLAYLIST|filepath1,filepath2,...|Playlist Name
                String[] parts = request.split("\\|", 3);
                String[] filePaths = parts[1].split(",");
                String playlistName = parts[2];

                out.println("STREAMING_START|" + playlistName + "|" + filePaths.length + " songs");

                // Stream each song in sequence
                for (String filePath : filePaths) {
                    if (!musicStreamer.streamAudioFile(filePath, clientSocket)) {
                        out.println("ERROR: Failed to stream " + filePath);
                        break;
                    }
                    // Small delay between songs if needed
                    Thread.sleep(500);
                }

                try {
                    // Check if socket is still open before trying to write
                    if (!clientSocket.isClosed()) {
                        out.println("PLAYLIST_COMPLETE");
                    }
                } catch (Exception e) {
                    System.out.println("Note: Client closed connection after playlist streaming completed");
                }
            } else if (request.startsWith("STREAM|")) {
                // It's a single song - format: STREAM|filepath|Song Title|Artist ID
                String[] parts = request.split("\\|", 4);
                if (parts.length >= 4) {
                    String filePath = parts[1];
                    String songTitle = parts[2];
                    String artistId = parts[3];

                    out.println("STREAMING_START|" + songTitle);

                    // Stream the requested song
                    boolean success = musicStreamer.streamAudioFile(filePath, clientSocket);

                    try {
                        // Check if socket is still open before trying to write
                        if (!clientSocket.isClosed() && success) {
                            out.println("STREAMING_COMPLETE");
                        }
                    } catch (Exception e) {
                        // Socket was closed by client, log but don't treat as error
                        System.out.println("Note: Client closed connection after streaming completed");
                    }
                } else {
                    out.println("ERROR: Invalid STREAM command format");
                }
            } else {
                out.println("ERROR: Unknown streaming command");
            }

        } catch (IOException e) {
            System.err.println("Error in streaming handler: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Streaming interrupted: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Closed streaming connection from " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.err.println("Error closing streaming socket: " + e.getMessage());
            }
        }
    }
}