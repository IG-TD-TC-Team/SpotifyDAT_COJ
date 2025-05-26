package spotifyServer.commandProcessor;

import services.playlistServices.PlaylistService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import spotifyServer.SpotifySocketServer;
import spotifyServer.StreamingServer;
import services.userServices.AuthorizationService;

import java.util.LinkedList;

public class PlaylistCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();
    private final StreamingServer streamingServer;
    private final AuthorizationService authorizationService = AuthorizationService.getInstance();

    public PlaylistCommandProcessor() {
        this.streamingServer = StreamingServer.getInstance();
    }

    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("playlist ")) {
            // Check authentication
            if (!isAuthenticated()) {
                return "ERROR: You must be logged in to play music";
            }

            try {
                String[] parts = command.split(" ", 2);
                if (parts.length < 2) {
                    return "Error: Missing playlist ID. Usage: playlist <playlist_id>";
                }

                int playlistId = Integer.parseInt(parts[1]);
                Playlist playlist = playlistService.getPlaylistById(playlistId);

                if (playlist == null) {
                    return "Error: Playlist not found with ID: " + playlistId;
                }

                LinkedList<Song> songs = playlist.getSongs();
                if (songs.isEmpty()) {
                    return "Error: Playlist is empty";
                }

                // Get user ID from CommandContext
                Integer userId = getCurrentUserId();
                String username = getCurrentUsername();

                if (userId == null) {
                    return "ERROR: Could not determine user identity";
                }

                // Check authorization
                if (!authorizationService.canAccessPlaylist(userId, playlistId)) {
                    return "ERROR: You don't have permission to play this playlist";
                }

                // Build file paths including the user ID
                StringBuilder playlistData = new StringBuilder();
                for (int i = 0; i < songs.size(); i++) {
                    if (i > 0) playlistData.append(",");
                    playlistData.append(songs.get(i).getFilePath());
                }

                // Include user ID in streaming instructions
                String response = "PLAYLIST_STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + playlistData.toString() + "|" + playlist.getName() + "|" + userId;

                System.out.println("Sending playlist streaming instructions for user " +
                        username + " (ID: " + userId + "): " + playlist.getName());

                // Prepare the streaming server
                streamingServer.prepareForPlaylistStreaming(playlist, songs);

                return response;

            } catch (NumberFormatException e) {
                return "Error: Invalid playlist ID format. Please provide a number.";
            } catch (Exception e) {
                return "Error processing playlist command: " + e.getMessage();
            }
        }

        return handleNext(command);
    }
}