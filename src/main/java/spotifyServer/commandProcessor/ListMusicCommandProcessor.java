package spotifyServer.commandProcessor;

import services.songServices.SongService;
import services.songServices.ArtistService;
import services.songServices.AlbumService;
import songsAndArtists.Song;
import songsAndArtists.Genre;
import songsAndArtists.Artist;
import songsAndArtists.Album;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Processor that handles music listing commands: listsongs.
 * This processor manages song listing operations through SongService and related services.
 */
public class ListMusicCommandProcessor extends AbstractProcessor {
    private final SongService songService = SongService.getInstance();
    private final ArtistService artistService = ArtistService.getInstance();
    private final AlbumService albumService = AlbumService.getInstance();

    @Override
    public String processCommand(String command) {
        String lowerCommand = command.toLowerCase();

        // Check if this is a list music command
        if (lowerCommand.startsWith("listsongs ")) {
            return handleListSongs(command);
        }

        // Pass to next processor if not a list music command
        return handleNext(command);
    }

    /**
     * Handles listing songs by different criteria.
     * Format: listsongs artist id <id> or listsongs artist name <name>
     *         listsongs album id <id> or listsongs album name <name>
     *         listsongs genre <genre>
     */
    private String handleListSongs(String command) {
        String[] parts = command.split(" ", 4); // Split with limit to preserve spaces in names

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: listsongs <type> [id/name] <identifier>";
        }

        String type = parts[1].toLowerCase();
        List<Song> songs = null;
        String listTitle = "";

        try {
            switch (type) {
                case "artist":
                    // Handle artist songs with proper parsing of multi-word names
                    if (parts.length < 4) {
                        return "Error: Missing artist identifier. Usage: listsongs artist [id/name] <identifier>";
                    }

                    String artistIdentifierType = parts[2].toLowerCase();
                    String artistIdentifier = parts[3];

                    // Process artist by ID or name
                    if ("id".equals(artistIdentifierType)) {
                        int artistId = Integer.parseInt(artistIdentifier);
                        songs = songService.getSongsByArtistId(artistId);
                        Artist artist = artistService.getArtistById(artistId);
                        if (artist != null) {
                            listTitle = "Songs by " + artist.getFirstName() + " " + artist.getLastName();
                        } else {
                            listTitle = "Songs by Artist ID " + artistId;
                        }
                    } else if ("name".equals(artistIdentifierType)) {
                        // For artist name, we need to handle multi-word names
                        songs = songService.getSongByArtistName(artistIdentifier);
                        listTitle = "Songs by " + artistIdentifier;
                    } else {
                        return "Error: Invalid identifier type for artist. Use 'id' or 'name'";
                    }
                    break;

                case "album":
                    // Handle album songs with proper parsing
                    if (parts.length < 4) {
                        return "Error: Missing album identifier. Usage: listsongs album [id/name] <identifier>";
                    }

                    String albumIdentifierType = parts[2].toLowerCase();
                    String albumIdentifier = parts[3];

                    // Process album by ID or name
                    if ("id".equals(albumIdentifierType)) {
                        int albumId = Integer.parseInt(albumIdentifier);
                        songs = songService.getSongsByAlbumId(albumId);
                        Album album = albumService.getAlbumById(albumId);
                        if (album != null) {
                            listTitle = "Songs in album '" + album.getTitle() + "'";
                        } else {
                            listTitle = "Songs in Album ID " + albumId;
                        }
                    } else if ("name".equals(albumIdentifierType)) {
                        // For album name, we need to handle multi-word names
                        songs = songService.getSongsByAlbumName(albumIdentifier);
                        listTitle = "Songs in album '" + albumIdentifier + "'";
                    } else {
                        return "Error: Invalid identifier type for album. Use 'id' or 'name'";
                    }
                    break;

                case "genre":
                    if (parts.length < 3) {
                        return "Error: Missing genre. Usage: listsongs genre <genre>";
                    }
                    Genre genre = Genre.valueOf(parts[2].toUpperCase());
                    songs = songService.getSongsByGenre(genre);
                    listTitle = "Songs in genre " + genre;
                    break;

                default:
                    return "ERROR: Invalid type. Use 'artist', 'album', or 'genre'";
            }

            if (songs == null || songs.isEmpty()) {
                return "No songs found matching the criteria.";
            }

            // Build the response
            StringBuilder response = new StringBuilder(listTitle + ":\n");
            response.append("═══════════════════════════════\n");

            for (Song song : songs) {
                Artist artist = artistService.getArtistById(song.getArtistId());
                String artistName = (artist != null) ? artist.getFirstName() + " " + artist.getLastName() : "Unknown Artist";

                response.append("ID: ").append(song.getSongId())
                        .append(" | ").append(song.getTitle())
                        .append(" | Artist: ").append(artistName)
                        .append(" | Duration: ").append(formatDuration(song.getDurationSeconds()))
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Total songs: ").append(songs.size());

            return response.toString();

        } catch (IllegalArgumentException e) {
            if (type.equals("genre")) {
                return "ERROR: Invalid genre. Valid genres are: ROCK, POP, JAZZ, CLASSICAL, HIP_HOP, ELECTRONIC, COUNTRY, BLUES";
            }
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            return "ERROR: Failed to list songs: " + e.getMessage();
        }
    }

    /**
     * Helper method to format duration in seconds to a readable format.
     */
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}