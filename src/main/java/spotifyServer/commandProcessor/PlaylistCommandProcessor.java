package spotifyServer.commandProcessor;

import services.playbackServices.PlaybackService;
import services.playlistServices.PlaylistService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import spotifyServer.SpotifySocketServer;

import java.util.List;

/**
 * MODIFICATIONS: Updated PlaylistCommandProcessor for simplified streaming architecture.
 * Removed prepareForPlaylistStreaming call since StreamingServer now handles pure audio only.
 * Now uses PlaybackService to start playlist and returns first song stream request.
 *
 * This processor handles the "playlist" command by:
 * 1. Finding the requested playlist
 * 2. Starting playlist in PlaybackService
 * 3. Sending streaming instructions for first song to client
 * 4. Client-driven navigation handles subsequent songs via auto_next
 */
public class PlaylistCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();
    private final PlaybackService playbackService = PlaybackService.getInstance();
    // MODIFICATIONS: Removed StreamingServer dependency since no preparation needed

    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("playlist ")) {
            // Check authentication
            if (!isAuthenticated()) {
                return "ERROR: You must be logged in to play playlists";
            }

            try {
                // Parse the command to get playlist identifier
                String[] parts = command.split(" ", 3);
                if (parts.length < 2) {
                    return "Error: Missing playlist identifier. Usage: playlist <playlist_name> or playlist id <playlist_id>";
                }

                Playlist playlist = null;
                Integer userId = getCurrentUserId();
                String username = getCurrentUsername();

                if (userId == null) {
                    return "ERROR: Could not determine user identity";
                }

                // Handle different command formats
                if (parts.length == 2) {
                    // Format: "playlist <name>"
                    String playlistName = parts[1];
                    playlist = findPlaylistByName(playlistName, userId);
                    if (playlist == null) {
                        return "Error: Playlist not found with name: " + playlistName;
                    }
                } else if (parts.length == 3) {
                    // Format: "playlist id <playlist_id>"
                    String identifierType = parts[1].toLowerCase();
                    String identifier = parts[2];

                    if ("id".equals(identifierType)) {
                        try {
                            int playlistId = Integer.parseInt(identifier);
                            playlist = playlistService.getPlaylistById(playlistId);
                            if (playlist == null) {
                                return "Error: Playlist not found with ID: " + playlistId;
                            }

                            // Check if user can access this playlist
                            if (playlist.getOwnerID() != userId && !playlist.getSharedWith().contains(userId)) {
                                return "Error: You don't have access to playlist ID: " + playlistId;
                            }
                        } catch (NumberFormatException e) {
                            return "Error: Invalid playlist ID format. Please provide a numeric ID.";
                        }
                    } else {
                        return "Error: Invalid identifier type. Use 'id' for playlist ID";
                    }
                }

                if (playlist == null) {
                    return "Error: Could not identify playlist from the provided parameters";
                }

                // Check if playlist has songs
                if (playlist.getSongs().isEmpty()) {
                    return "Error: Playlist '" + playlist.getName() + "' is empty";
                }

                // MODIFICATIONS: Start playlist using PlaybackService (this handles navigation state)
                Song firstSong = playbackService.startPlaylist(playlist.getPlaylistID(), userId);

                if (firstSong == null) {
                    return "Error: Failed to start playlist '" + playlist.getName() + "'";
                }

                System.out.println("Starting playlist '" + playlist.getName() + "' for user " + username +
                        " (ID: " + userId + ") with " + playlist.getSongs().size() + " songs");

                // MODIFICATIONS: Return stream request for first song only
                // Client will handle subsequent songs via auto_next mechanism
                String response = "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + firstSong.getFilePath() + "|" + firstSong.getTitle() +
                        "|" + firstSong.getArtistId() + "|" + userId;

                // MODIFICATIONS: No preparation needed - StreamingServer handles pure audio on demand
                // PlaybackService now manages playlist state for client-driven navigation

                return response;

            } catch (Exception e) {
                return "Error processing playlist command: " + e.getMessage();
            }
        }

        return handleNext(command);
    }

    /**
     * MODIFICATIONS: Finds a playlist by name that the user can access.
     * Checks both owned playlists and playlists shared with the user.
     *
     * @param playlistName The name of the playlist to search for
     * @param userId The ID of the user requesting the playlist
     * @return The found playlist, or null if not found or not accessible
     */
    private Playlist findPlaylistByName(String playlistName, Integer userId) {
        // First, try to find in user's own playlists
        List<Playlist> userPlaylists = playlistService.getPlaylistsByOwner(userId);
        for (Playlist playlist : userPlaylists) {
            if (playlist.getName().equalsIgnoreCase(playlistName)) {
                return playlist;
            }
        }

        // Then, try to find in playlists shared with the user
        List<Playlist> sharedPlaylists = playlistService.getPlaylistsSharedWithUser(userId);
        for (Playlist playlist : sharedPlaylists) {
            if (playlist.getName().equalsIgnoreCase(playlistName)) {
                return playlist;
            }
        }

        // Finally, try partial matching in accessible playlists
        for (Playlist playlist : userPlaylists) {
            if (playlist.getName().toLowerCase().contains(playlistName.toLowerCase())) {
                return playlist;
            }
        }

        for (Playlist playlist : sharedPlaylists) {
            if (playlist.getName().toLowerCase().contains(playlistName.toLowerCase())) {
                return playlist;
            }
        }

        return null; // Playlist not found or not accessible
    }

    /**
     * MODIFICATIONS: Helper method to create user-friendly error for multiple matches.
     * Note: Currently not used but kept for future enhancements.
     *
     * @param playlistName The searched playlist name
     * @param matchingPlaylists List of matching playlists
     * @return Formatted error message with playlist options
     */
    private String createMultiplePlaylistMatchesError(String playlistName, List<Playlist> matchingPlaylists) {
        StringBuilder response = new StringBuilder("Multiple playlists found matching '");
        response.append(playlistName).append("':\n");
        response.append("Please be more specific or use playlist ID:\n");

        for (Playlist playlist : matchingPlaylists) {
            response.append("ID: ").append(playlist.getPlaylistID())
                    .append(" - ").append(playlist.getName())
                    .append(" (").append(playlist.getSongs().size()).append(" songs)\n");
        }

        response.append("Use: playlist id <playlist_id> to play a specific playlist");
        return response.toString();
    }
}