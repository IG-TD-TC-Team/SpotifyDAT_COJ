package spotifyServer.commandProcessor;

import services.songServices.SongService;
import songsAndArtists.Song;

import java.util.List;

/**
 * SearchCommandProcessor that integrates with the enhanced
 * Chain of Responsibility pattern with direct socket handling.
 *
 * This processor searches for songs and returns results directly
 * to the client through the established socket connection.
 */
public class SearchCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();

    @Override
    public String processCommand(String command) {
        // Check if the command starts with "search "
        if (command.toLowerCase().startsWith("search ")) {
            String query = command.substring(7).trim();

            if (query.isEmpty()) {
                return "Error: Missing search query. Usage: search <query>";
            }

            try {
                // Perform search operation
                System.out.println("Searching for: " + query);

                // Simple search implementation - search by title
                List<Song> results = songService.getSongsByTitle(query);

                if (results.isEmpty()) {
                    return "No songs found matching: " + query;
                }

                // Build and return search results
                StringBuilder response = new StringBuilder("Search results for: " + query + "\n");
                response.append("Found ").append(results.size()).append(" song(s):\n");
                response.append("----------------------------------------\n");

                for (Song song : results) {
                    response.append("ID: ").append(song.getSongId())
                            .append(" | Title: ").append(song.getTitle())
                            .append(" | Artist ID: ").append(song.getArtistId())
                            .append(" | Duration: ").append(song.getDurationSeconds()).append("s")
                            .append("\n");
                }

                response.append("----------------------------------------\n");
                response.append("Use 'play <song_id>' to play a song");

                return response.toString();

            } catch (Exception e) {
                System.err.println("Error performing search: " + e.getMessage());
                return "Error: Failed to perform search: " + e.getMessage();
            }
        }

        // Pass to next processor if this one can't handle it
        return handleNext(command);
    }
}