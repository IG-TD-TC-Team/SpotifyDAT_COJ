package spotifyServer.commandProcessor;

import services.playlistServices.PlaylistService;
import songsOrganisation.Playlist;
import spotifyServer.SpotifySocketServer;

// Playlist command processor
class PlaylistCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();

    @Override
    public String processCommand(String command) {
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

                // Create a serialized list of song file paths from the playlist
                StringBuilder playlistData = new StringBuilder();
                for (int i = 0; i < playlist.getSongs().size(); i++) {
                    if (i > 0) playlistData.append(",");
                    playlistData.append(playlist.getSongs().get(i).getFilePath());
                }

                // Return the playlist streaming instructions to the client
                return "PLAYLIST_STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + playlistData.toString() + "|" + playlist.getName();

            } catch (NumberFormatException e) {
                return "Error: Invalid playlist ID format. Please provide a number.";
            } catch (Exception e) {
                return "Error processing playlist command: " + e.getMessage();
            }
        }
        return handleNext(command);
    }
}
