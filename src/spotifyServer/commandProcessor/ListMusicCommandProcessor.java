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
        String[] parts = command.split(" ");

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: listsongs <type> [id/name] <identifier>";
        }

        String type = parts[1].toLowerCase();
        List<Song> songs = null;
        String listTitle = "";

        try {
            switch (type) {
                case "artist":
                    songs = handleArtistSongs(parts);
                    if (songs != null) {
                        Artist artist = artistService.getArtistById(songs.get(0).getArtistId());
                        listTitle = "Songs by " + artist.getFirstName() + " " + artist.getLastName();
                    }
                    break;

                case "album":
                    songs = handleAlbumSongs(parts);
                    if (songs != null && !songs.isEmpty()) {
                        Album album = albumService.getAlbumById(songs.get(0).getAlbumId());
                        listTitle = "Songs in album '" + album.getTitle() + "'";
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
                response.append("ID: ").append(song.getSongId())
                        .append(" | ").append(song.getTitle())
                        .append(" | Artist: ").append(artist.getFirstName()).append(" ").append(artist.getLastName())
                        .append(" | Duration: ").append(formatDuration(song.getDurationSeconds()))
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Total songs: ").append(songs.size());

            return response.toString();

        } catch (IllegalArgumentException e) {
            return "ERROR: Invalid genre. Valid genres are: ROCK, POP, JAZZ, CLASSICAL, HIP_HOP, ELECTRONIC, COUNTRY, BLUES";
        } catch (Exception e) {
            return "ERROR: Failed to list songs: " + e.getMessage();
        }
    }

    /**
     * Handles listing songs by artist.
     */
    private List<Song> handleArtistSongs(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid format for artist songs");
        }

        String identifierType = parts[2].toLowerCase();
        String identifier = parts[3];

        if ("id".equals(identifierType)) {
            int artistId = Integer.parseInt(identifier);
            return songService.getSongsByArtistId(artistId);
        } else if ("name".equals(identifierType)) {
            // Join remaining parts for multi-word names
            if (parts.length > 4) {
                StringBuilder nameBuilder = new StringBuilder(identifier);
                for (int i = 4; i < parts.length; i++) {
                    nameBuilder.append(" ").append(parts[i]);
                }
                identifier = nameBuilder.toString();
            }
            return songService.getSongByArtistName(identifier);
        } else {
            throw new IllegalArgumentException("Invalid identifier type. Use 'id' or 'name'");
        }
    }

    /**
     * Handles listing songs by album.
     */
    private List<Song> handleAlbumSongs(String[] parts) {
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid format for album songs");
        }

        String identifierType = parts[2].toLowerCase();
        String identifier = parts[3];

        if ("id".equals(identifierType)) {
            int albumId = Integer.parseInt(identifier);
            return songService.getSongsByAlbumId(albumId);
        } else if ("name".equals(identifierType)) {
            // Join remaining parts for multi-word names
            if (parts.length > 4) {
                StringBuilder nameBuilder = new StringBuilder(identifier);
                for (int i = 4; i < parts.length; i++) {
                    nameBuilder.append(" ").append(parts[i]);
                }
                identifier = nameBuilder.toString();
            }
            return songService.getSongsByAlbumName(identifier);
        } else {
            throw new IllegalArgumentException("Invalid identifier type. Use 'id' or 'name'");
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