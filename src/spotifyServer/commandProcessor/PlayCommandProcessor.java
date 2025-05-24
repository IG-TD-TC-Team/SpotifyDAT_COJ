package spotifyServer.commandProcessor;

import services.songServices.SongService;
import songsAndArtists.Song;
import spotifyServer.SpotifySocketServer;
import spotifyServer.StreamingServer;

import java.net.Socket;
import java.util.Arrays;

/**
 * Enhanced PlayCommandProcessor that properly separates command processing
 * from music streaming by using the two-port architecture.
 *
 * This processor handles the "play" command by:
 * 1. Validating the song exists
 * 2. Sending streaming instructions to the client (on command port)
 * 3. Letting the client initiate streaming on the streaming port
 */


public class PlayCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();
    private final StreamingServer streamingServer = StreamingServer.getInstance();

    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("play ")) {
            // Check authentication using your existing system
            if (!isAuthenticated()) {
                return "ERROR: You must be logged in to play music";
            }

            try {
                String[] parts = command.split(" ", 2);
                if (parts.length < 2) {
                    return "Error: Missing song ID. Usage: play <song_id>";
                }

                int songId = Integer.parseInt(parts[1]);
                Song song = songService.getSongById(songId);

                if (song == null) {
                    return "Error: Song not found with ID: " + songId;
                }

                // Get user context from your existing CommandContext system
                Integer userId = getCurrentUserId();
                String username = getCurrentUsername();

                if (userId == null) {
                    return "ERROR: Could not determine user identity";
                }

                // Include user ID in streaming instructions - this is the key handoff!
                String response = "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + song.getFilePath() + "|" + song.getTitle() +
                        "|" + song.getArtistId() + "|" + userId; // <-- User ID added here

                System.out.println("Preparing stream for user " + username + " (ID: " + userId +
                        "): " + song.getTitle());

                // Prepare streaming server
                streamingServer.prepareForStreaming(song);

                return response;

            } catch (NumberFormatException e) {
                return "Error: Invalid song ID format. Please provide a number.";
            } catch (Exception e) {
                return "Error processing play command: " + e.getMessage();
            }
        }

        return handleNext(command);
    }
}