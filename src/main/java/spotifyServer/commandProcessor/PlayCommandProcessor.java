package spotifyServer.commandProcessor;

import services.songServices.SongService;
import songsAndArtists.Song;
import spotifyServer.SpotifySocketServer;

import java.util.List;

/**
 * MODIFICATIONS: Updated PlayCommandProcessor for simplified streaming architecture.
 * Removed prepareForStreaming call since StreamingServer now handles pure audio only.
 *
 * This processor handles the "play" command by:
 * 1. Validating the song exists
 * 2. Sending streaming instructions to the client (on command port)
 * 3. Letting the client initiate pure audio streaming on the streaming port
 */
public class PlayCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();
    // MODIFICATIONS: Removed StreamingServer dependency since no preparation needed

    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("play ")) {
            // Check authentication using existing system
            if (!isAuthenticated()) {
                return "ERROR: You must be logged in to play music";
            }

            try {
                // Parse the command to determine if it's by ID or name
                String[] parts = command.split(" ", 3);
                if (parts.length < 2) {
                    return "Error: Missing song identifier. Usage: play <song_id> or play id <song_id> or play name <song_name>";
                }

                Song song = null;

                // Handle different command formats
                if (parts.length == 2) {
                    // Original format: "play <identifier>"
                    // Try to determine if it's an ID or name
                    String identifier = parts[1];
                    if (identifier.matches("\\d+")) {
                        // It's numeric, treat as ID
                        int songId = Integer.parseInt(identifier);
                        song = songService.getSongById(songId);
                        if (song == null) {
                            return "Error: Song not found with ID: " + songId;
                        }
                    } else {
                        // It's not numeric, treat as name
                        song = findSongByName(identifier);
                        if (song == null) {
                            return "Error: Song not found with name: " + identifier;
                        }
                    }
                } else if (parts.length == 3) {
                    // New format: "play id <song_id>" or "play name <song_name>"
                    String identifierType = parts[1].toLowerCase();
                    String identifier = parts[2];

                    if ("id".equals(identifierType)) {
                        try {
                            int songId = Integer.parseInt(identifier);
                            song = songService.getSongById(songId);
                            if (song == null) {
                                return "Error: Song not found with ID: " + songId;
                            }
                        } catch (NumberFormatException e) {
                            return "Error: Invalid song ID format. Please provide a numeric ID.";
                        }
                    } else if ("name".equals(identifierType)) {
                        song = findSongByName(identifier);
                        if (song == null) {
                            return "Error: Song not found with name: " + identifier;
                        }
                    } else {
                        return "Error: Invalid identifier type. Use 'id' or 'name'";
                    }
                }

                if (song == null) {
                    return "Error: Could not identify song from the provided parameters";
                }

                // Get user context from existing CommandContext system
                Integer userId = getCurrentUserId();
                String username = getCurrentUsername();

                if (userId == null) {
                    return "ERROR: Could not determine user identity";
                }

                // MODIFICATIONS: Build streaming request for single song playback
                String response = "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT +
                        "|" + song.getFilePath() + "|" + song.getTitle() +
                        "|" + song.getArtistId() + "|" + userId;

                System.out.println("Sending stream request for user " + username + " (ID: " + userId +
                        "): " + song.getTitle());

                // MODIFICATIONS: No preparation needed - StreamingServer handles pure audio on demand
                // Client will connect to streaming port and request this specific song

                return response;

            } catch (Exception e) {
                return "Error processing play command: " + e.getMessage();
            }
        }

        return handleNext(command);
    }

    /**
     * Finds a song by name using flexible search logic.
     * Returns the first exact match, or the first partial match if no exact match exists.
     * Handles multiple matches by providing user-friendly feedback.
     *
     * @param songName The name of the song to search for
     * @return The found song, or null if not found or ambiguous
     */
    private Song findSongByName(String songName) {
        // Use the enhanced search from SongService
        List<Song> matchingSongs = songService.getSongsByTitleFlexible(songName);

        if (matchingSongs.isEmpty()) {
            return null; // No songs found
        } else if (matchingSongs.size() == 1) {
            return matchingSongs.get(0); // Single match found
        } else {
            // Multiple matches - return the first one but this could be enhanced
            // to show all options to the user in a future iteration
            return matchingSongs.get(0);
        }
    }

    /**
     * MODIFICATIONS: Helper method to handle multiple song matches.
     * Creates a user-friendly error message when multiple songs match the search.
     * Note: This method is not currently used but kept for future enhancements.
     *
     * @param songName The searched song name
     * @param matchingSongs List of matching songs
     * @return Formatted error message with song options
     */
    private String createMultipleMatchesError(String songName, List<Song> matchingSongs) {
        StringBuilder response = new StringBuilder("Multiple songs found matching '");
        response.append(songName).append("':\n");
        response.append("Please be more specific or use song ID:\n");

        for (Song song : matchingSongs) {
            response.append("ID: ").append(song.getSongId())
                    .append(" - ").append(song.getTitle())
                    .append(" (").append(song.getGenre()).append(")\n");
        }

        response.append("Use: play id <song_id> to play a specific song");
        return response.toString();
    }
}