package spotifyServer.commandProcessor;

import services.songServices.SongService;
import songsAndArtists.Song;

import java.util.List;

// Search command processor
class SearchCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();

    @Override
    public String processCommand(String command) {
        if (command.toLowerCase().startsWith("search ")) {
            String query = command.substring(7).trim();

            if (query.isEmpty()) {
                return "Error: Missing search query. Usage: search <query>";
            }

            // Simple search implementation - search by title
            List<Song> results = songService.getSongsByTitle(query);

            if (results.isEmpty()) {
                return "No songs found matching: " + query;
            }

            StringBuilder response = new StringBuilder("Search results for: " + query + "\n");
            for (Song song : results) {
                response.append("ID: ").append(song.getSongId())
                        .append(" | Title: ").append(song.getTitle())
                        .append(" | Artist ID: ").append(song.getArtistId())
                        .append("\n");
            }

            return response.toString();
        }
        return handleNext(command);
    }
}
