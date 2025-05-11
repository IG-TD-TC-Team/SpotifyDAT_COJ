package spotifyServer.commandProcessor;

import services.songServices.SongService;
import songsAndArtists.Song;
import spotifyServer.SpotifySocketServer;
import spotifyServer.StreamingServer;

import java.net.Socket;
import java.util.Arrays;

/**
 * PlayCommandProcessor class processes the "play" command.
 * It retrieves the song ID from the command, checks if the song exists,
 * and returns streaming instructions to the client.
 */
class PlayCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();
    private final StreamingServer streamingServer;

    PlayCommandProcessor() {
        this.streamingServer = StreamingServer.getInstance();
    }

    @Override
    public String processCommand(String command) {
        /// Check if the command starts with "play "
        if (command.toLowerCase().startsWith("play ")) {

            System.out.println("Debug: Received play command: " + command); /// DEBUG
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

                // Get current socket from context
                Socket clientSocket = CommandContext.getInstance().getCurrentSocket();
                if (clientSocket == null) {
                    return "Error: Client socket not available";
                }



                // Return the streaming instructions to the client
                String response = "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + song.getFilePath() + "|" + song.getTitle() + "|" + song.getArtistId();
                System.out.println("Debug: Sending response: " + response);

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
