package spotifyServer.commandProcessor;

import services.playbackServices.PlaybackService;
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
    private final PlaybackService playbackService = PlaybackService.getInstance();

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

                // Start playlist in the PlaybackService and get the first song
                Song firstSong = playbackService.startPlaylist(playlistId, userId);

                if (firstSong == null) {
                    return "Error: Failed to start playlist - unable to get first song";
                }

                // Prepare streaming for the first song
                streamingServer.prepareForStreaming(firstSong);

                // Send instructions to client to connect to streaming server for this song
                // Include both the first song info AND the playlist ID for context
                String response = "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + firstSong.getFilePath() +
                        "|" + firstSong.getTitle() +
                        "|" + firstSong.getArtistId() +
                        "|" + userId +
                        "|" + playlistId;  // Include playlist ID to mark this as part of playlist

                System.out.println("Starting playlist '" + playlist.getName() +
                        "' for user " + username + " (ID: " + userId +
                        ") with song: " + firstSong.getTitle());

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