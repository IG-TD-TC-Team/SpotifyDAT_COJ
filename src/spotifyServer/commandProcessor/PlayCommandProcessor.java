package spotifyServer.commandProcessor;

import services.songServices.SongService;
import songsAndArtists.Song;
import spotifyServer.StreamingServer;

import java.net.Socket;

// Play command processor
class PlayCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();
    private final StreamingServer streamingServer;

    PlayCommandProcessor() {
        this.streamingServer = StreamingServer.getInstance();
    }

    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("play ")) {
            try {
                // Extract song ID from command
                String[] parts = command.split(" ", 2);
                if (parts.length < 2) {
                    return "Error: Missing song ID. Usage: play <song_id>";
                }

                int songId = Integer.parseInt(parts[1]);

                // Check if song exists
                Song song = songService.getSongById(songId);
                if (song == null) {
                    return "Error: Song not found with ID: " + songId;
                }

                // Get current socket from context
                Socket clientSocket = CommandContext.getInstance().getCurrentSocket();


                // Return the streaming instructions to the client
                return "STREAMING_STARTED|" + song.getTitle() + song.getArtistId();
            } catch (NumberFormatException e) {
                return "Error: Invalid song ID format. Please provide a number.";
            } catch (Exception e) {
                return "Error processing play command: " + e.getMessage();
            }
        }
        return handleNext(command);
    }
}
