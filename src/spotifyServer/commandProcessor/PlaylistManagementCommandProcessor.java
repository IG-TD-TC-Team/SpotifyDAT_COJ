package spotifyServer.commandProcessor;

import factory.PlaylistFactory;
import factory.RepositoryFactory;
import services.playlistServices.PlaylistService;
import services.playlistServices.SongInPlaylistService;
import services.songServices.SongService;
import persistence.interfaces.PlaylistRepositoryInterface;
import services.userServices.AuthorizationService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import java.util.List;

/**
 * Processor that handles playlist management commands: createplaylist, renameplaylist,
 * deleteplaylist, viewplaylist, listplaylists, addtoplaylist, removefromplaylist.
 * This processor manages playlist operations through PlaylistService and related services.
 */
public class PlaylistManagementCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();
    private final PlaylistFactory playlistFactory = PlaylistFactory.getInstance();
    private final SongService songService = SongService.getInstance();
    private final PlaylistRepositoryInterface playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
    private final SongInPlaylistService songInPlaylistService = SongInPlaylistService.getInstance(playlistRepository);
    private final AuthorizationService authorizationService = AuthorizationService.getInstance();

    @Override
    public String processCommand(String command) {
        // First check if user is authenticated
        if (!isAuthenticated()) {
            return "ERROR: Authentication required. Please login first.";
        }

        String lowerCommand = command.toLowerCase();

        // Check if this is a playlist management command
        if (lowerCommand.startsWith("createplaylist ")) {
            return handleCreatePlaylist(command);
        } else if (lowerCommand.startsWith("renameplaylist ")) {
            return handleRenamePlaylist(command);
        } else if (lowerCommand.startsWith("deleteplaylist ")) {
            return handleDeletePlaylist(command);
        } else if (lowerCommand.startsWith("viewplaylist ")) {
            return handleViewPlaylist(command);
        } else if (lowerCommand.equals("listplaylists")) {
            return handleListPlaylists();
        } else if (lowerCommand.startsWith("addtoplaylist ")) {
            return handleAddToPlaylist(command);
        } else if (lowerCommand.startsWith("removefromplaylist ")) {
            return handleRemoveFromPlaylist(command);
        }

        // Pass to next processor if not a playlist management command
        return handleNext(command);
    }

    /**
     * Handles creating a new playlist.
     * Format: createplaylist <name>
     */
    private String handleCreatePlaylist(String command) {
        String[] parts = command.split(" ", 2);

        if (parts.length < 2) {
            return "Error: Missing playlist name. Usage: createplaylist <name>";
        }

        String playlistName = parts[1];

        try {

            int userId = getCurrentUserId();

            Playlist newPlaylist = playlistFactory.createPlaylist(playlistName, userId);

            return "SUCCESS: Playlist '" + playlistName + "' created successfully! (ID: " + newPlaylist.getPlaylistID() + ")";

        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            return "ERROR: Failed to create playlist: " + e.getMessage();
        }
    }

    /**
     * Handles renaming a playlist.
     * Format: renameplaylist id <id> <newName> or renameplaylist name <oldName> <newName>
     */
    private String handleRenamePlaylist(String command) {
        String[] parts = command.split(" ", 4);

        if (parts.length < 4) {
            return "Error: Invalid format. Usage: renameplaylist id <id> <newName> or renameplaylist name <oldName> <newName>";
        }

        String identifierType = parts[1].toLowerCase();
        String identifier = parts[2];
        String newName = parts[3];

        try {
            int userId = getCurrentUserId();
            Playlist playlist = null;

            if ("id".equals(identifierType)) {
                int playlistId = Integer.parseInt(identifier);
                playlist = playlistService.getPlaylistById(playlistId);

                // Check authorization
                if (!authorizationService.canModifyPlaylist(userId, playlistId)) {
                    return "ERROR: You don't have permission to modify this playlist";
                }
            } else if ("name".equals(identifierType)) {
                playlist = playlistService.getPlaylistByNameAndOwner(identifier, userId);
            } else {
                return "ERROR: Invalid identifier type. Use 'id' or 'name'";
            }

            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Verify ownership
            /*if (playlist.getOwnerID() != userId) {
                return "ERROR: You don't have permission to rename this playlist";
            }*/

            boolean success = playlistService.renamePlaylist(playlist.getPlaylistID(), newName);

            if (success) {
                return "SUCCESS: Playlist renamed to '" + newName + "'";
            } else {
                return "ERROR: Failed to rename playlist";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid playlist ID format";
        } catch (Exception e) {
            return "ERROR: Failed to rename playlist: " + e.getMessage();
        }
    }

    /**
     * Handles deleting a playlist.
     * Format: deleteplaylist id <id> or deleteplaylist name <name>
     */
    private String handleDeletePlaylist(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: deleteplaylist id <id> or deleteplaylist name <name>";
        }

        String identifierType = parts[1].toLowerCase();
        String identifier = parts[2];

        try {
            int userId = getCurrentUserId();
            Playlist playlist = null;

            if ("id".equals(identifierType)) {
                int playlistId = Integer.parseInt(identifier);
                playlist = playlistService.getPlaylistById(playlistId);

                // Check authorization
                if (!authorizationService.canModifyPlaylist(userId, playlistId)) {
                    return "ERROR: You don't have permission to delete this playlist";
                }
            } else if ("name".equals(identifierType)) {
                playlist = playlistService.getPlaylistByNameAndOwner(identifier, userId);
            } else {
                return "ERROR: Invalid identifier type. Use 'id' or 'name'";
            }

            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Verify ownership
            /*if (playlist.getOwnerID() != userId) {
                return "ERROR: You don't have permission to delete this playlist";
            }*/

            boolean success = playlistService.deletePlaylist(playlist.getPlaylistID());

            if (success) {
                return "SUCCESS: Playlist '" + playlist.getName() + "' deleted successfully";
            } else {
                return "ERROR: Failed to delete playlist";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid playlist ID format";
        } catch (Exception e) {
            return "ERROR: Failed to delete playlist: " + e.getMessage();
        }
    }

    /**
     * Handles viewing a playlist's details.
     * Format: viewplaylist id <id> or viewplaylist name <name>
     */
    private String handleViewPlaylist(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: viewplaylist id <id> or viewplaylist name <name>";
        }

        String identifierType = parts[1].toLowerCase();
        String identifier = parts[2];

        try {
            int userId = getCurrentUserId();
            Playlist playlist = null;
            int playlistId = -1;

            if ("id".equals(identifierType)) {
                playlistId = Integer.parseInt(identifier);
                playlist = playlistService.getPlaylistById(playlistId);
            } else if ("name".equals(identifierType)) {
                playlist = playlistService.getPlaylistByNameAndOwner(identifier, userId);
                if (playlist != null) {
                    playlistId = playlist.getPlaylistID();
                }
            } else {
                return "ERROR: Invalid identifier type. Use 'id' or 'name'";
            }

            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Check authorization
            if (!authorizationService.canAccessPlaylist(userId, playlistId)) {
                return "ERROR: You don't have permission to view this playlist";
            }

            // Build playlist details
            StringBuilder response = new StringBuilder();
            response.append("Playlist Details:\n");
            response.append("═══════════════════════════════\n");
            response.append("Name: ").append(playlist.getName()).append("\n");
            response.append("ID: ").append(playlist.getPlaylistID()).append("\n");
            response.append("Owner ID: ").append(playlist.getOwnerID()).append("\n");
            response.append("Total Songs: ").append(playlist.getSongCount()).append("\n");
            response.append("Duration: ").append(formatDuration(playlist.getTotalDuration())).append("\n");
            response.append("Shared With: ").append(playlist.getSharedWith().size()).append(" users\n");
            response.append("Liked By: ").append(playlist.getLikedByUsers().size()).append(" users\n");
            response.append("Public: ").append(playlist.isPubliclyShareable() ? "Yes" : "No").append("\n");
            response.append("\nSongs:\n");
            response.append("─────────────────────────────\n");

            int index = 1;
            for (Song song : playlist.getSongs()) {
                response.append(index++).append(". ").append(song.getTitle())
                        .append(" (ID: ").append(song.getSongId()).append(")")
                        .append(" - ").append(formatDuration(song.getDurationSeconds()))
                        .append("\n");
            }

            response.append("═══════════════════════════════");

            return response.toString();

        } catch (NumberFormatException e) {
            return "ERROR: Invalid playlist ID format";
        } catch (Exception e) {
            return "ERROR: Failed to view playlist: " + e.getMessage();
        }
    }

    /**
     * Handles listing all playlists for the current user.
     */
    private String handleListPlaylists() {
        try {
            int userId = getCurrentUserId();

            List<Playlist> playlists = playlistService.getPlaylistsByOwner(userId);

            if (playlists.isEmpty()) {
                return "You don't have any playlists yet. Create one with 'createplaylist <name>'";
            }

            StringBuilder response = new StringBuilder("Your Playlists:\n");
            response.append("═══════════════════════════════\n");

            for (Playlist playlist : playlists) {
                response.append("• ").append(playlist.getName())
                        .append(" (ID: ").append(playlist.getPlaylistID()).append(")")
                        .append(" - ").append(playlist.getSongCount()).append(" songs")
                        .append(" - ").append(formatDuration(playlist.getTotalDuration()))
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Total playlists: ").append(playlists.size());

            return response.toString();

        } catch (Exception e) {
            return "ERROR: Failed to list playlists: " + e.getMessage();
        }
    }

    /**
     * Handles adding a song to a playlist.
     * Format: addtoplaylist playlistid <playlistId> songid <songId>
     */
    private String handleAddToPlaylist(String command) {
        String[] parts = command.split(" ");

        if (parts.length < 5) {
            return "Error: Invalid format. Usage: addtoplaylist playlistid <id> songid <id>";
        }

        try {
            int userId = getCurrentUserId();

            int playlistId = -1;
            int songId = -1;

            // Parse the command
            for (int i = 1; i < parts.length - 1; i++) {
                if ("playlistid".equals(parts[i].toLowerCase())) {
                    playlistId = Integer.parseInt(parts[i + 1]);
                } else if ("songid".equals(parts[i].toLowerCase())) {
                    songId = Integer.parseInt(parts[i + 1]);
                }
            }

            if (playlistId == -1 || songId == -1) {
                return "ERROR: Invalid format. Please specify both playlistid and songid";
            }


            // Get the playlist and verify ownership
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Check authorization
            if (!authorizationService.canAccessPlaylist(userId, playlistId)) {
                return "ERROR: You don't have permission to modify this playlist";
            }

            // Get the song
            Song song = songService.getSongById(songId);
            if (song == null) {
                return "ERROR: Song not found";
            }

            // Add song to playlist
            songInPlaylistService.addSongToPlaylist(playlistId, song);

            return "SUCCESS: Added '" + song.getTitle() + "' to playlist '" + playlist.getName() + "'";

        } catch (NumberFormatException e) {
            return "ERROR: Invalid ID format";
        } catch (Exception e) {
            return "ERROR: Failed to add song to playlist: " + e.getMessage();
        }
    }

    /**
     * Handles removing a song from a playlist.
     * Format: removefromplaylist playlistid <playlistId> songid <songId>
     */
    private String handleRemoveFromPlaylist(String command) {
        String[] parts = command.split(" ");

        if (parts.length < 5) {
            return "Error: Invalid format. Usage: removefromplaylist playlistid <id> songid <id>";
        }

        try {
            int userId = getCurrentUserId();

            int playlistId = -1;
            int songId = -1;

            // Parse the command
            for (int i = 1; i < parts.length - 1; i++) {
                if ("playlistid".equals(parts[i].toLowerCase())) {
                    playlistId = Integer.parseInt(parts[i + 1]);
                } else if ("songid".equals(parts[i].toLowerCase())) {
                    songId = Integer.parseInt(parts[i + 1]);
                }
            }

            if (playlistId == -1 || songId == -1) {
                return "ERROR: Invalid format. Please specify both playlistid and songid";
            }

            // Get the playlist and verify ownership
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Check authorization
            if (!authorizationService.canAccessPlaylist(userId, playlistId)) {
                return "ERROR: You don't have permission to delete a song from this playlist";
            }

            // Get the song
            Song song = songService.getSongById(songId);
            if (song == null) {
                return "ERROR: Song not found";
            }

            // Remove song from playlist
            Playlist updatedPlaylist = songInPlaylistService.removeSongFromPlaylist(playlistId, song);

            if (updatedPlaylist != null) {
                return "SUCCESS: Removed '" + song.getTitle() + "' from playlist '" + playlist.getName() + "'";
            } else {
                return "ERROR: Failed to remove song. Song might not be in the playlist.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid ID format";
        } catch (Exception e) {
            return "ERROR: Failed to remove song from playlist: " + e.getMessage();
        }
    }

    /**
     * Helper method to format duration in seconds to a readable format.
     */
    private String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%d:%02d", minutes, secs);
        }
    }
}