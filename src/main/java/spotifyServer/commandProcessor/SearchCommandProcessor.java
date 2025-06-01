package spotifyServer.commandProcessor;

import services.songServices.AlbumService;
import services.songServices.ArtistService;
import services.songServices.SongService;
import songsAndArtists.Album;
import songsAndArtists.Artist;
import songsAndArtists.Genre;
import songsAndArtists.Song;

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

        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid search format. Usage: search <type> <query>";
        }

        String type = parts[1].toLowerCase();
        String query = parts[2];

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
     * Searches for albums by title.
     */
    private String searchAlbums(String query) {
        // First, make sure we get all albums from the service
        List<Album> allAlbums = albumService.getAllAlbums();
        System.out.println("Debug: Found " + allAlbums.size() + " total albums in the system");

        // Filter by matching the query against album titles (case-insensitive)
        List<Album> albums = allAlbums.stream()
                .filter(album -> {
                    if (album.getTitle() == null) return false;
                    return album.getTitle().toLowerCase().contains(query.toLowerCase());
                })
                .collect(Collectors.toList());

        System.out.println("Debug: Found " + albums.size() + " albums matching '" + query + "'");

        if (albums.isEmpty()) {
            return "No albums found matching: " + query;
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