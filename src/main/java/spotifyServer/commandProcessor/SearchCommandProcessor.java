package spotifyServer.commandProcessor;

import services.songServices.AlbumService;
import services.songServices.ArtistService;
import services.songServices.SongService;
import songsAndArtists.Album;
import songsAndArtists.Artist;
import songsAndArtists.Genre;
import songsAndArtists.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SearchCommandProcessor that integrates with the enhanced
 * Chain of Responsibility pattern with direct socket handling.
 *
 * This processor searches for songs and returns results directly
 * to the client through the established socket connection.
 */
public class SearchCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();
    private final ArtistService artistService = ArtistService.getInstance();
    private final AlbumService albumService = AlbumService.getInstance();

    @Override
    public String processCommand(String command) {
        // Check if the command starts with "search "
        if (command.toLowerCase().startsWith("search ")) {
            return handleSearch(command);
        }

        // Pass to next processor if not a search command
        return handleNext(command);
    }

    /**
     * Handles search operations for different entity types.
     * Format: search <type> <query>
     * Types: song, artist, album, genre
     */
    private String handleSearch(String command) {
        // First check if user is authenticated
        if (!isAuthenticated()) {
            return "ERROR: Authentication required. Please login first.";
        }

        // Parse the command while preserving quoted strings
        String[] parts = parseCommandWithQuotes(command);

        if (parts.length < 3) {
            return "Error: Invalid search format. Usage: search <type> <query>";
        }

        String type = parts[1].toLowerCase();
        String query = parts[2];

        // Remove quotes if present
        if (query.startsWith("\"") && query.endsWith("\"")) {
            query = query.substring(1, query.length() - 1);
        }

        if (query.isEmpty()) {
            return "Error: Search query cannot be empty";
        }

        try {
            switch (type) {
                case "song":
                    return searchSongs(query);
                case "artist":
                    return searchArtists(query);
                case "album":
                    return searchAlbums(query);
                case "genre":
                    return searchGenres(query);
                default:
                    return "Error: Invalid search type. Valid types are: song, artist, album, genre";
            }
        } catch (Exception e) {
            return "ERROR: Search failed: " + e.getMessage();
        }
    }

    /**
     * Helper method to parse command while preserving quoted strings
     */
    private String[] parseCommandWithQuotes(String command) {
        List<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                currentPart.append(c);
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


    /**
     * Searches for songs by title.
     */
    private String searchSongs(String query) {
        // Use flexible search to improve results
        List<Song> songs = songService.getSongsByTitleFlexible(query);

        if (songs.isEmpty()) {
            return "No songs found matching: " + query;
        }

        StringBuilder response = new StringBuilder("Songs matching '" + query + "':\n");
        response.append("═══════════════════════════════\n");

        for (Song song : songs) {
            Artist artist = artistService.getArtistById(song.getArtistId());
            String artistName = (artist != null) ?
                    artist.getFirstName() + " " + artist.getLastName() : "Unknown Artist";

            response.append("ID: ").append(song.getSongId())
                    .append(" | ").append(song.getTitle())
                    .append(" | Artist: ").append(artistName)
                    .append(" | Genre: ").append(song.getGenre())
                    .append("\n");
        }

        response.append("═══════════════════════════════\n");
        response.append("Found ").append(songs.size()).append(" song(s)");
        response.append("\nUse 'play <song_id>' to play a song");

        return response.toString();
    }

    /**
     * Searches for artists by name.
     */
    private String searchArtists(String query) {
        List<Artist> artists = artistService.getAllArtists().stream()
                .filter(artist -> {
                    String fullName = artist.getFirstName() + " " + artist.getLastName();
                    return artist.getFirstName().toLowerCase().contains(query.toLowerCase()) ||
                            artist.getLastName().toLowerCase().contains(query.toLowerCase()) ||
                            fullName.toLowerCase().contains(query.toLowerCase());
                })
                .collect(Collectors.toList());

        if (artists.isEmpty()) {
            return "No artists found matching: " + query;
        }

        StringBuilder response = new StringBuilder("Artists matching '" + query + "':\n");
        response.append("═══════════════════════════════\n");

        for (Artist artist : artists) {
            List<Song> songs = songService.getSongsByArtistId(artist.getArtistID());

            response.append("ID: ").append(artist.getArtistID())
                    .append(" | ").append(artist.getFirstName()).append(" ").append(artist.getLastName())
                    .append(" | Country: ").append(artist.getCountryOfBirth())
                    .append(" | Songs: ").append(songs.size())
                    .append("\n");
        }

        response.append("═══════════════════════════════\n");
        response.append("Found ").append(artists.size()).append(" artist(s)");
        response.append("\nUse 'listsongs artist id <artist_id>' to see an artist's songs");

        return response.toString();
    }

    /**
     * Searches for albums based on a user query string with multiple matching strategies.
     * Results are prioritized in the order of matching strategy, with exact matches appearing first,
     * followed by contains matches, and finally fuzzy matches.
     *
     * @param query The search query string provided by the user
     * @return A formatted string containing the search results, including album IDs, titles,
     *         artist names, and song counts, or a message indicating no albums were found
     */
    private String searchAlbums(String query) {
        // Log the search query for debugging
        System.out.println("DEBUG: Searching for album with query: '" + query + "'");

        List<Album> allAlbums = albumService.getAllAlbums();
        System.out.println("DEBUG: Found " + allAlbums.size() + " total albums in the system");

        // Enhanced search logic with multiple strategies:
        // 1. First try exact match (case-insensitive)
        // 2. Then try contains match (case-insensitive)
        // 3. Finally try fuzzy matching (ignoring special characters, etc.)

        List<Album> exactMatches = new ArrayList<>();
        List<Album> containsMatches = new ArrayList<>();
        List<Album> fuzzyMatches = new ArrayList<>();

        String normalizedQuery = normalizeString(query);

        for (Album album : allAlbums) {
            if (album.getTitle() == null) continue;

            // Try exact match (case-insensitive)
            if (album.getTitle().equalsIgnoreCase(query)) {
                exactMatches.add(album);
                continue;
            }

            // Try contains match (case-insensitive)
            if (album.getTitle().toLowerCase().contains(query.toLowerCase())) {
                containsMatches.add(album);
                continue;
            }

            // Try fuzzy matching
            String normalizedTitle = normalizeString(album.getTitle());
            if (normalizedTitle.contains(normalizedQuery)) {
                fuzzyMatches.add(album);
            }
        }

        // Combine results, prioritizing exact matches, then contains matches, then fuzzy matches
        List<Album> albums = new ArrayList<>(exactMatches);
        albums.addAll(containsMatches);
        albums.addAll(fuzzyMatches);

        System.out.println("DEBUG: Found matches - Exact: " + exactMatches.size()
                + ", Contains: " + containsMatches.size()
                + ", Fuzzy: " + fuzzyMatches.size());

        if (albums.isEmpty()) {
            return "No albums found matching: \"" + query + "\"";
        }

        StringBuilder response = new StringBuilder("Albums matching '" + query + "':\n");
        response.append("═══════════════════════════════\n");

        for (Album album : albums) {
            // Get artist info from the service
            Artist artist = artistService.getArtistById(album.getArtistId());
            String artistName = (artist != null) ?
                    artist.getFirstName() + " " + artist.getLastName() : "Unknown Artist";

            // Get song count safely
            int songCount = 0;
            if (album.getSongs() != null) {
                songCount = album.getSongs().size();
            }

            response.append("ID: ").append(album.getId())
                    .append(" | ").append(album.getTitle())
                    .append(" | Artist: ").append(artistName)
                    .append(" | Songs: ").append(songCount)
                    .append("\n");
        }

        response.append("═══════════════════════════════\n");
        response.append("Found ").append(albums.size()).append(" album(s)");
        response.append("\nUse 'listsongs album id <album_id>' to see an album's songs");

        return response.toString();
    }

    /**
     * Helper method to normalize a string for fuzzy matching:
     * - Convert to lowercase
     * - Remove special characters and extra spaces
     * - Remove accents
     */
    private String normalizeString(String input) {
        if (input == null) return "";

        // Convert to lowercase
        String result = input.toLowerCase();

        // Remove special characters
        result = result.replaceAll("[^a-z0-9\\s]", "");

        // Replace multiple spaces with a single space
        result = result.replaceAll("\\s+", " ");

        // Remove accents (simplified approach)
        result = result.replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n");

        return result.trim();
    }

    /**
     * Searches for songs by genre.
     */
    private String searchGenres(String query) {
        try {
            // Try to parse the genre directly
            Genre genre = Genre.valueOf(query.toUpperCase());
            List<Song> songs = songService.getSongsByGenre(genre);

            if (songs.isEmpty()) {
                return "No songs found with genre: " + genre;
            }

            StringBuilder response = new StringBuilder("Songs with genre '" + genre + "':\n");
            response.append("═══════════════════════════════\n");

            for (Song song : songs) {
                Artist artist = artistService.getArtistById(song.getArtistId());
                String artistName = (artist != null) ?
                        artist.getFirstName() + " " + artist.getLastName() : "Unknown Artist";

                response.append("ID: ").append(song.getSongId())
                        .append(" | ").append(song.getTitle())
                        .append(" | Artist: ").append(artistName)
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Found ").append(songs.size()).append(" song(s)");

            return response.toString();
        } catch (IllegalArgumentException e) {
            // If it's not a valid genre, show available genres
            StringBuilder response = new StringBuilder("Invalid genre: " + query + "\n");
            response.append("Available genres: ");
            response.append(Arrays.stream(Genre.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", ")));
            return response.toString();
        }
    }


}