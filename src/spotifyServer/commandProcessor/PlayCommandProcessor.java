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
    private final StreamingServer streamingServer;

    public PlayCommandProcessor() {
        // Get the singleton StreamingServer instance
        this.streamingServer = StreamingServer.getInstance();
    }

    @Override
    public String processCommand(String command) {
        // Check if the command starts with "play "
        if (command.toLowerCase().startsWith("play ")) {
            System.out.println("Debug: Received play command: " + command);

            try {
                // Extract song ID from command
                String[] parts = command.split(" ", 2);
                System.out.println("Debug: Parsed command parts: " + Arrays.toString(parts));

                if (parts.length < 2) {
                    return "Error: Missing song ID. Usage: play <song_id>";
                }

                int songId = Integer.parseInt(parts[1]);

                // Check if song exists
                Song song = songService.getSongById(songId);
                System.out.println("Debug: Found song: " + (song != null ? song.getTitle() : "null"));

                if (song == null) {
                    return "Error: Song not found with ID: " + songId;
                }

                // IMPORTANT: Instead of streaming directly, we send streaming instructions
                // The client will connect to the streaming port separately
                String response = "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + song.getFilePath() + "|" + song.getTitle() + "|" + song.getArtistId();

                System.out.println("Debug: Sending response: " + response);

                // Optionally, prepare the streaming server for this specific song
                // This could involve caching the song data or preparing the stream
                streamingServer.prepareForStreaming(song);

                return response;

            } catch (NumberFormatException e) {
                return "Error: Invalid song ID format. Please provide a number.";
            } catch (Exception e) {
                return "Error processing play command: " + e.getMessage();
            }
        }

        // If this processor can't handle the command, pass it to the next one
        return handleNext(command);
    }
}