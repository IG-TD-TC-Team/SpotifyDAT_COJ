package services.userServices;

import services.playlistServices.PlaylistService;
import services.playlistServices.SocialPlaylistService;
import songsOrganisation.Playlist;

/**
 * Service responsible for authorization decisions.
 * Centralizes access control logic for the application.
 */
public class AuthorizationService {
    // Singleton instance
    private static AuthorizationService instance;

    // Dependencies
    private final PlaylistService playlistService;
    private final SocialPlaylistService socialPlaylistService;

    private AuthorizationService() {
        this.playlistService = PlaylistService.getInstance();
        this.socialPlaylistService = SocialPlaylistService.getInstance();
    }

    public static synchronized AuthorizationService getInstance() {
        if (instance == null) {
            instance = new AuthorizationService();
        }
        return instance;
    }

    /**
     * Checks if a user can access a playlist.
     * A user can access a playlist if:
     * 1. They own the playlist
     * 2. The playlist is shared with them
     * 3. The playlist is public and they follow the owner
     *
     * @param userId The ID of the user requesting access
     * @param playlistId The ID of the playlist being accessed
     * @return true if the user can access the playlist, false otherwise
     */
    public boolean canAccessPlaylist(int userId, int playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        if (playlist == null) {
            return false;
        }

        // User owns the playlist OR is sharedWith
        if (playlist.getOwnerID() == userId || playlist.getSharedWith().contains(userId)) {
            return true;
        }

        // Check if the user has access via social features
        return socialPlaylistService.canUserAccessPlaylist(userId, playlist);
    }

    /**
     * Checks if a user can modify a playlist.
     * A user can modify a playlist only if they own it.
     *
     * @param userId The ID of the user requesting modification
     * @param playlistId The ID of the playlist being modified
     * @return true if the user can modify the playlist, false otherwise
     */
    public boolean canModifyPlaylist(int userId, int playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        if (playlist == null) {
            return false;
        }

        // Only the owner can modify a playlist
        return playlist.getOwnerID() == userId;
    }

    /**
     * Checks if a user can view another user's profile details.
     * For now, users can see basic info about all users, but
     * detailed info only about themselves.
     *
     * @param requestingUserId The ID of the user requesting access
     * @param targetUserId The ID of the user whose profile is being accessed
     * @param detailedAccess Whether detailed info is being requested
     * @return true if the user can access the profile, false otherwise
     */
    public boolean canAccessUserProfile(int requestingUserId, int targetUserId, boolean detailedAccess) {
        // Users can always see their own profiles
        if (requestingUserId == targetUserId) {
            return true;
        }

        // For detailed access, only allow users to see their own profile
        if (detailedAccess) {
            return false;
        }

        // For basic info, allow access to all users
        return true;
    }
}