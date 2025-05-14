package spotifyServer.commandProcessor;

import services.playlistServices.PlaylistService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import spotifyServer.SpotifySocketServer;
import spotifyServer.StreamingServer;

import java.util.LinkedList;

/**
 * PlaylistCommandProcessor that properly separates command processing
 * from music streaming using the two-port architecture.
 *
 * This processor handles the "playlist" command by:
 * 1. Validating the playlist exists
 * 2. Sending streaming instructions to the client (text protocol on command port)
 * 3. Preparing the streaming server for the playlist
 * 4. Letting the client initiate streaming on the dedicated streaming port
 */
public class PlaylistCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();
    private final StreamingServer streamingServer;

    public PlaylistCommandProcessor() {
        // Get the singleton StreamingServer instance
        this.streamingServer = StreamingServer.getInstance();
    }

    @Override
    public String processCommand(String command) {
        // Check if the command starts with "playlist "
        if (command.toLowerCase().startsWith("playlist ")) {
            try {
                // Extract playlist ID from command
                String[] parts = command.split(" ", 2);
                if (parts.length < 2) {
                    return "Error: Missing playlist ID. Usage: playlist <playlist_id>";
                }

                int playlistId = Integer.parseInt(parts[1]);

                // Check if playlist exists
                Playlist playlist = playlistService.getPlaylistById(playlistId);
                if (playlist == null) {
                    return "Error: Playlist not found with ID: " + playlistId;
                }

                // Get the songs from the playlist
                LinkedList<Song> songs = playlist.getSongs();
                if (songs.isEmpty()) {
                    return "Error: Playlist is empty";
                }

                // Build file paths for all songs in the playlist
                StringBuilder playlistData = new StringBuilder();
                for (int i = 0; i < songs.size(); i++) {
                    if (i > 0) playlistData.append(",");
                    playlistData.append(songs.get(i).getFilePath());
                }

                // IMPORTANT: Send streaming instructions, not the actual stream
                // The client will connect to the streaming port separately
                String response = "PLAYLIST_STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + playlistData.toString() + "|" + playlist.getName();

                System.out.println("Sending playlist streaming instructions: " + playlist.getName());

                // Optionally, prepare the streaming server for this playlist
                streamingServer.prepareForPlaylistStreaming(playlist, songs);

                return response;

            } catch (NumberFormatException e) {
                return "Error: Invalid playlist ID format. Please provide a number.";
            } catch (Exception e) {
                return "Error processing playlist command: " + e.getMessage();
            }
        }

        // If this processor can't handle the command, pass it to the next one
        return handleNext(command);
    }
}