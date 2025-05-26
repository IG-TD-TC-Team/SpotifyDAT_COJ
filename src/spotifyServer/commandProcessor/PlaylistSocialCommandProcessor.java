package spotifyServer.commandProcessor;

import services.playlistServices.PlaylistService;
import services.playlistServices.SocialPlaylistService;
import services.userServices.UserService;
import services.userServices.AuthorizationService;
import songsOrganisation.Playlist;
import user.User;
import java.util.List;

/**
 * Processor that handles playlist social commands: shareplaylist, unshareplaylist,
 * likeplaylist, unlikeplaylist, viewlikedplaylists, viewplaylistlikes.
 * This processor manages social interactions with playlists.
 */
public class PlaylistSocialCommandProcessor extends AbstractProcessor {
    private final PlaylistService playlistService = PlaylistService.getInstance();
    private final SocialPlaylistService socialPlaylistService = SocialPlaylistService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final AuthorizationService authorizationService = AuthorizationService.getInstance();

    @Override
    public String processCommand(String command) {
        // First check if user is authenticated
        if (!isAuthenticated()) {
            return "ERROR: Authentication required. Please login first.";
        }

        String lowerCommand = command.toLowerCase();

        // Check if this is a playlist social command
        if (lowerCommand.startsWith("shareplaylist ")) {
            return handleSharePlaylist(command);
        } else if (lowerCommand.startsWith("unshareplaylist ")) {
            return handleUnsharePlaylist(command);
        } else if (lowerCommand.startsWith("likeplaylist ")) {
            return handleLikePlaylist(command);
        } else if (lowerCommand.startsWith("unlikeplaylist ")) {
            return handleUnlikePlaylist(command);
        } else if (lowerCommand.equals("viewlikedplaylists")) {
            return handleViewLikedPlaylists();
        } else if (lowerCommand.startsWith("viewplaylistlikes")) {
            return handleViewPlaylistLikes(command);
        }

        // Pass to next processor if not a playlist social command
        return handleNext(command);
    }

    /**
     * Handles sharing a playlist with another user.
     * Format: shareplaylist playlistid <id> userid <userId> or similar variations
     */
    private String handleSharePlaylist(String command) {
        String[] parts = command.split(" ");

        if (parts.length < 5) {
            return "Error: Invalid format. Usage: shareplaylist playlistid <id> userid <userId>";
        }

        try {
            int currentUserId = getCurrentUserId();

            int playlistId = -1;
            int targetUserId = -1;

            // Parse the command for playlist and user identifiers
            for (int i = 1; i < parts.length - 1; i++) {
                if ("playlistid".equals(parts[i].toLowerCase())) {
                    playlistId = Integer.parseInt(parts[i + 1]);
                } else if ("userid".equals(parts[i].toLowerCase())) {
                    targetUserId = Integer.parseInt(parts[i + 1]);
                }
            }

            if (playlistId == -1 || targetUserId == -1) {
                return "ERROR: Invalid format. Please specify both playlistid and userid";
            }

            // Get the playlist and verify ownership
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Check authorization
            if (!authorizationService.canModifyPlaylist(currentUserId, playlistId)) {
                return "ERROR: You don't have permission to share this playlist";
            }

            // Verify target user exists
            User targetUser = userService.getUserById(targetUserId);

            // Share the playlist
            boolean success = socialPlaylistService.sharePlaylist(playlistId, targetUserId);

            if (success) {
                return "SUCCESS: Playlist '" + playlist.getName() + "' shared with user " + targetUser.getUsername();
            } else {
                return "ERROR: Failed to share playlist. It may already be shared with this user.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid ID format";
        } catch (Exception e) {
            return "ERROR: Failed to share playlist: " + e.getMessage();
        }
    }

    /**
     * Handles unsharing a playlist with a user.
     * Format: unshareplaylist playlistid <id> userid <userId>
     */
    private String handleUnsharePlaylist(String command) {
        String[] parts = command.split(" ");

        if (parts.length < 5) {
            return "Error: Invalid format. Usage: unshareplaylist playlistid <id> userid <userId>";
        }

        try {
            int currentUserId = getCurrentUserId();

            int playlistId = -1;
            int targetUserId = -1;

            // Parse the command for playlist and user identifiers
            for (int i = 1; i < parts.length - 1; i++) {
                if ("playlistid".equals(parts[i].toLowerCase())) {
                    playlistId = Integer.parseInt(parts[i + 1]);
                } else if ("userid".equals(parts[i].toLowerCase())) {
                    targetUserId = Integer.parseInt(parts[i + 1]);
                }
            }

            if (playlistId == -1 || targetUserId == -1) {
                return "ERROR: Invalid format. Please specify both playlistid and userid";
            }

            // Get the playlist and verify ownership
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Check authorization
            if (!authorizationService.canModifyPlaylist(currentUserId, playlistId)) {
                return "ERROR: You don't have permission to share this playlist";
            }

            // Unshare the playlist
            boolean success = socialPlaylistService.unsharePlaylist(playlistId, targetUserId);

            if (success) {
                return "SUCCESS: Playlist '" + playlist.getName() + "' is no longer shared with user ID " + targetUserId;
            } else {
                return "ERROR: Failed to unshare playlist. It may not be shared with this user.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid ID format";
        } catch (Exception e) {
            return "ERROR: Failed to unshare playlist: " + e.getMessage();
        }
    }

    /**
     * Handles liking a playlist.
     * Format: likeplaylist id <id> or likeplaylist name <n>
     */
    private String handleLikePlaylist(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: likeplaylist id <id> or likeplaylist name <n>";
        }

        String identifierType = parts[1].toLowerCase();
        String identifier = parts[2];

        try {
            int currentUserId = getCurrentUserId();

            Playlist playlist = null;

            if ("id".equals(identifierType)) {
                int playlistId = Integer.parseInt(identifier);
                playlist = playlistService.getPlaylistById(playlistId);
            } else if ("name".equals(identifierType)) {
                // For now, we'll need to search through all playlists
                // This is a limitation - ideally we'd have a better search mechanism
                List<Playlist> allPlaylists = playlistService.getAllPlaylists();
                for (Playlist p : allPlaylists) {
                    if (p.getName().equals(identifier)) {
                        playlist = p;
                        break;
                    }
                }
            } else {
                return "ERROR: Invalid identifier type. Use 'id' or 'name'";
            }

            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Like the playlist
            boolean success = socialPlaylistService.likePlaylist(currentUserId, playlist.getPlaylistID());

            if (success) {
                return "SUCCESS: You liked the playlist '" + playlist.getName() + "'";
            } else {
                return "ERROR: Failed to like playlist. You may have already liked it.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid playlist ID format";
        } catch (Exception e) {
            return "ERROR: Failed to like playlist: " + e.getMessage();
        }
    }

    /**
     * Handles unliking a playlist.
     * Format: unlikeplaylist id <id> or unlikeplaylist name <n>
     */
    private String handleUnlikePlaylist(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: unlikeplaylist id <id> or unlikeplaylist name <n>";
        }

        String identifierType = parts[1].toLowerCase();
        String identifier = parts[2];

        try {
            int currentUserId = getCurrentUserId();

            Playlist playlist = null;

            if ("id".equals(identifierType)) {
                int playlistId = Integer.parseInt(identifier);
                playlist = playlistService.getPlaylistById(playlistId);
            } else if ("name".equals(identifierType)) {
                // For now, we'll need to search through all playlists
                List<Playlist> allPlaylists = playlistService.getAllPlaylists();
                for (Playlist p : allPlaylists) {
                    if (p.getName().equals(identifier)) {
                        playlist = p;
                        break;
                    }
                }
            } else {
                return "ERROR: Invalid identifier type. Use 'id' or 'name'";
            }

            if (playlist == null) {
                return "ERROR: Playlist not found";
            }

            // Unlike the playlist
            boolean success = socialPlaylistService.unlikePlaylist(currentUserId, playlist.getPlaylistID());

            if (success) {
                return "SUCCESS: You unliked the playlist '" + playlist.getName() + "'";
            } else {
                return "ERROR: Failed to unlike playlist. You may not have liked it.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid playlist ID format";
        } catch (Exception e) {
            return "ERROR: Failed to unlike playlist: " + e.getMessage();
        }
    }

    /**
     * Handles viewing all playlists the current user has liked.
     */
    private String handleViewLikedPlaylists() {
        try {
            int currentUserId = getCurrentUserId();

            List<Playlist> likedPlaylists = socialPlaylistService.getLikedPlaylistsByUser(currentUserId);

            if (likedPlaylists.isEmpty()) {
                return "You haven't liked any playlists yet.";
            }

            StringBuilder response = new StringBuilder("Your Liked Playlists:\n");
            response.append("═══════════════════════════════\n");

            for (Playlist playlist : likedPlaylists) {
                User owner = userService.getUserById(playlist.getOwnerID());
                response.append("• ").append(playlist.getName())
                        .append(" (ID: ").append(playlist.getPlaylistID()).append(")")
                        .append(" by ").append(owner.getUsername())
                        .append(" - ").append(playlist.getSongCount()).append(" songs")
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Total liked playlists: ").append(likedPlaylists.size());

            return response.toString();

        } catch (Exception e) {
            return "ERROR: Failed to view liked playlists: " + e.getMessage();
        }
    }

    /**
     * Handles viewing users who have liked a specific playlist.
     * Format: viewplaylistlikes [id/name] [identifier]
     */
    private String handleViewPlaylistLikes(String command) {
        String[] parts = command.split(" ");

        try {
            int currentUserId = getCurrentUserId();

            if (parts.length == 1) {
                // Show likes for all user's playlists
                List<Playlist> userPlaylists = playlistService.getPlaylistsByOwner(currentUserId);

                if (userPlaylists.isEmpty()) {
                    return "You don't have any playlists.";
                }

                StringBuilder response = new StringBuilder("Likes on Your Playlists:\n");
                response.append("═══════════════════════════════\n");

                for (Playlist playlist : userPlaylists) {
                    int likeCount = socialPlaylistService.getLikeCountForPlaylist(playlist.getPlaylistID(), currentUserId);
                    response.append("• ").append(playlist.getName())
                            .append(" - ").append(likeCount).append(" likes\n");
                }

                response.append("═══════════════════════════════");

                return response.toString();

            } else if (parts.length >= 3) {
                // Show likes for a specific playlist
                String identifierType = parts[1].toLowerCase();
                String identifier = parts[2];
                int playlistId=-1;

                Playlist playlist = null;

                if ("id".equals(identifierType)) {
                    playlistId = Integer.parseInt(identifier);
                    playlist = playlistService.getPlaylistById(playlistId);
                } else if ("name".equals(identifierType)) {
                    playlist = playlistService.getPlaylistByNameAndOwner(identifier, currentUserId);
                    if(playlist!=null) {
                        playlistId = playlist.getPlaylistID();
                    }
                } else {
                    return "ERROR: Invalid identifier type. Use 'id' or 'name'";
                }

                if (playlist == null) {
                    return "ERROR: Playlist not found";
                }

                // Check authorization
                if (!authorizationService.canModifyPlaylist(currentUserId, playlistId)) {
                    return "ERROR: You don't have permission to share this playlist";
                }

                List<User> usersWhoLiked = socialPlaylistService.getUsersWhoLikedPlaylist(playlist.getPlaylistID(), currentUserId);

                StringBuilder response = new StringBuilder("Users who liked '").append(playlist.getName()).append("':\n");
                response.append("═══════════════════════════════\n");

                if (usersWhoLiked.isEmpty()) {
                    response.append("No likes yet.\n");
                } else {
                    for (User user : usersWhoLiked) {
                        response.append("• ").append(user.getUsername())
                                .append(" (").append(user.getFirstName())
                                .append(" ").append(user.getLastName()).append(")\n");
                    }
                }

                response.append("═══════════════════════════════\n");
                response.append("Total likes: ").append(usersWhoLiked.size());

                return response.toString();

            } else {
                return "ERROR: Invalid format. Usage: viewplaylistlikes [id/name <identifier>]";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid ID format";
        } catch (Exception e) {
            return "ERROR: Failed to view playlist likes: " + e.getMessage();
        }
    }
}