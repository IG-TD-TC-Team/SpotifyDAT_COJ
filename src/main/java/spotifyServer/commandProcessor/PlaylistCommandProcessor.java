package spotifyServer.commandProcessor;

import services.playbackServices.PlaybackService;
import services.playlistServices.PlaylistService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import spotifyServer.SpotifySocketServer;
import spotifyServer.StreamingServer;
import services.userServices.AuthorizationService;

import java.util.LinkedList;

/**
 * Processor that handles playlist streaming commands.
 * This class is part of the Chain of Responsibility pattern and processes
 * commands related to playing playlists through the streaming server.
 *
 * The processor supports various command formats for playlist identification:
 * - By ID: "playlist 123" or "playlist id 123"
 * - By name: "playlist My Playlist" or "playlist name My Playlist"
 *
 * Upon successful processing, it returns streaming instructions to the client.
 */
public class PlaylistCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();
    private final StreamingServer streamingServer;
    private final AuthorizationService authorizationService = AuthorizationService.getInstance();
    private final PlaybackService playbackService = PlaybackService.getInstance();

    /**
     * Constructs a new PlaylistCommandProcessor.
     * Initializes the StreamingServer instance required for playlist streaming.
     */
    public PlaylistCommandProcessor() {
        this.streamingServer = StreamingServer.getInstance();
    }

    /**
     * Processes playlist commands and either handles them or passes them to the next processor.
     * This method parses playlist commands, validates authentication and authorization,
     * and prepares the streaming server for playback if all checks pass.
     *
     * @param command The command string from the client
     * @return A response string containing streaming instructions or an error message
     */
    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("playlist ")) {
            // Check authentication
            if (!isAuthenticated()) {
                return "ERROR: You must be logged in to play music";
            }

            try {
                // Parse command with support for both ID and name
                String[] parts = parseCommandWithQuotes(command);

                if (parts.length < 2) {
                    return "Error: Missing playlist identifier. Usage: playlist <playlist_id> or playlist id <id> or playlist name <name>";
                }

                // Get user context
                Integer userId = getCurrentUserId();
                String username = getCurrentUsername();

                if (userId == null) {
                    return "ERROR: Could not determine user identity";
                }

                Playlist playlist = null;
                int playlistId = -1;

                // Handle different command formats
                if (parts.length == 2) {
                    // Original format: "playlist <identifier>"
                    String identifier = parts[1];

                    if (identifier.matches("\\d+")) {
                        // It's numeric, treat as ID
                        playlistId = Integer.parseInt(identifier);
                        playlist = playlistService.getPlaylistById(playlistId);

                        if (playlist == null) {
                            return "Error: Playlist not found with ID: " + playlistId;
                        }
                    } else {
                        // It's not numeric, treat as name - search in user's playlists
                        playlist = playlistService.getPlaylistByNameAndOwner(identifier, userId);

                        if (playlist == null) {
                            return "Error: Playlist not found with name: " + identifier;
                        }
                        playlistId = playlist.getPlaylistID();
                    }
                } else if (parts.length >= 3) {
                    // New format: "playlist id <id>" or "playlist name <name>"
                    String identifierType = parts[1].toLowerCase();
                    String identifier = parts[2];

                    if ("id".equals(identifierType)) {
                        try {
                            playlistId = Integer.parseInt(identifier);
                            playlist = playlistService.getPlaylistById(playlistId);

                            if (playlist == null) {
                                return "Error: Playlist not found with ID: " + playlistId;
                            }
                        } catch (NumberFormatException e) {
                            return "Error: Invalid playlist ID format. Please provide a numeric ID.";
                        }
                    } else if ("name".equals(identifierType)) {
                        // Remove quotes if present
                        if (identifier.startsWith("\"") && identifier.endsWith("\"")) {
                            identifier = identifier.substring(1, identifier.length() - 1);
                        }

                        playlist = playlistService.getPlaylistByNameAndOwner(identifier, userId);

                        if (playlist == null) {
                            return "Error: Playlist not found with name: " + identifier;
                        }
                        playlistId = playlist.getPlaylistID();
                    } else {
                        return "Error: Invalid identifier type. Use 'id' or 'name'";
                    }
                }

                if (playlist == null || playlistId == -1) {
                    return "Error: Could not identify playlist from the provided parameters";
                }

                LinkedList<Song> songs = playlist.getSongs();
                if (songs.isEmpty()) {
                    return "Error: Playlist '" + playlist.getName() + "' is empty";
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

    /**
     * Helper method to parse command while preserving quoted strings.
     * This allows playlist names with spaces to be properly handled.
     *
     * @param command The command string to parse
     * @return An array of parsed command parts
     */
    private String[] parseCommandWithQuotes(String command) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                currentPart.append(c); // Keep quotes for later processing
            } else if (c == ' ' && !inQuotes) {
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart = new StringBuilder();
                }
            } else {
                currentPart.append(c);
            }
        }

        // Add the last part
        if (currentPart.length() > 0) {
            parts.add(currentPart.toString());
        }

        return parts.toArray(new String[0]);
    }
}