package services.playlistServices;

import factory.RepositoryFactory;
import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.UserRepositoryInterface;
import services.userServices.UserService;
import services.userServices.SocialService;
import songsOrganisation.Playlist;
import user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for social interactions related to playlists.
 *
 * This class manages the social aspects of playlists, including sharing, liking,
 * and access control. It implements the Singleton pattern to ensure consistent
 * management of playlist social features throughout the application.
 */
public class SocialPlaylistService {

    /**
     * Singleton instance of the SocialPlaylistService.
     */
    private static SocialPlaylistService instance;

    /**
     * Repository for accessing and modifying playlist data.
     */
    private final PlaylistRepositoryInterface playlistRepository;

    /**
     * Repository for accessing user data.
     */
    private final UserRepositoryInterface userRepository;

    /**
     * Service for user-related operations.
     */
    private final UserService userService;

    /**
     * Service for social relationship operations.
     */
    private final SocialService socialService;

    /**
     * Private constructor following the Singleton pattern.
     * Initializes all required dependencies from their respective factories and singletons.
     */
    private SocialPlaylistService() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        this.userRepository = RepositoryFactory.getInstance().getUserRepository();
        this.userService = UserService.getInstance();
        this.socialService = SocialService.getInstance();
    }

    /**
     * Returns the singleton instance of SocialPlaylistService.
     * Creates the instance if it doesn't exist yet, following the lazy initialization
     * approach to the Singleton pattern.
     *
     * @return The singleton instance of SocialPlaylistService
     */
    public static synchronized SocialPlaylistService getInstance() {
        if (instance == null) {
            instance = new SocialPlaylistService();
        }
        return instance;
    }

    /**
     * Sets the public shareability status of a playlist.
     *
     * When a playlist is publicly shareable, it becomes visible to all users
     * who follow the playlist's owner. Only the owner of the playlist can change
     * this setting.
     *
     * @param playlistId The ID of the playlist to modify
     * @param ownerId The ID of the user attempting to change the setting
     * @param shareable True to make the playlist publicly shareable, false to make it private
     * @return True if the setting was successfully changed, false if the playlist was not found
     * @throws SecurityException If the user attempting to change the setting is not the owner
     */
    public boolean setPlaylistShareability(int playlistId, int ownerId, boolean shareable) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Only the owner can change shareability
        if (playlist.getOwnerID() != ownerId) {
            throw new SecurityException("Only the playlist owner can change shareability settings");
        }

        playlist.setPubliclyShareable(shareable);
        playlistRepository.update(playlist);

        return true;
    }

    /**
     * Gets all shared playlists from users that the given user follows.
     *
     * This method retrieves all playlists that are either:
     *
     * Owned by users that the specified user follows AND marked as publicly shareable
     * Explicitly shared with the specified user by users they follow
     *
     * This provides a "discovery" mechanism for users to find playlists from people they follow.
     *
     * @param userId The ID of the user discovering playlists
     * @return A list of playlists that are discoverable by the specified user
     */
    public List<Playlist> getDiscoverablePlaylistsForUser(int userId) {
        // Get all users this user follows
        List<User> followedUsers = socialService.getFollowedUsers(userId);

        // Get all shared playlists from followed users
        return followedUsers.stream()
                .map(User::getUserID)
                .flatMap(followedUserId ->
                        playlistRepository.findByOwnerID(followedUserId).stream()
                                .filter(playlist -> playlist.isPubliclyShareable() || // Check the new field
                                        playlist.getSharedWith().contains(userId)))
                .collect(Collectors.toList());
    }

    /**
     * Likes a playlist by a user.
     *
     * A user can like a playlist only if they have access to it, which means
     * they either own it, it's shared with them, or it's a publicly shareable playlist
     * from a user they follow.
     *
     * @param userId The ID of the user liking the playlist
     * @param playlistId The ID of the playlist to like
     * @return True if the playlist was successfully liked, false if the playlist wasn't found,
     *         the user already liked it, or the user doesn't have access to it
     */
    public boolean likePlaylist(int userId, int playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if user can access this playlist (either shared with them or from a followed user)
        if (!canUserAccessPlaylist(userId, playlist)) {
            return false;
        }

        // Add user to likedByUsers if not already there
        List<Integer> likedBy = playlist.getLikedByUsers();
        if (!likedBy.contains(userId)) {
            likedBy.add(userId);
            playlist.setLikedByUsers(likedBy);
            playlistRepository.update(playlist);
            return true;
        }

        return false; // Already liked
    }

    /**
     * Unlikes a playlist by a user.
     *
     * Removes the user from the list of users who have liked the playlist.
     * This action doesn't require the user to still have access to the playlist;
     * they can unlike a playlist they previously liked even if they no longer have access to it.
     *
     * @param userId The ID of the user unliking the playlist
     * @param playlistId The ID of the playlist to unlike
     * @return True if the playlist was successfully unliked, false if the playlist wasn't found
     *         or the user hadn't liked it previously
     */
    public boolean unlikePlaylist(int userId, int playlistId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();
        List<Integer> likedBy = playlist.getLikedByUsers();

        if (likedBy.contains(userId)) {
            likedBy.remove(Integer.valueOf(userId));
            playlist.setLikedByUsers(likedBy);
            playlistRepository.update(playlist);
            return true;
        }

        return false; // Wasn't liked
    }

    /**
     * Gets all playlists that a user has liked.
     *
     * Retrieves all playlists across the system that have been liked by the specified user.
     * This method doesn't filter based on current access permissions, so it may include
     * playlists the user no longer has access to but had liked in the past.
     *
     * @param userId The ID of the user whose liked playlists to retrieve
     * @return A list of playlists liked by the specified user
     */
    public List<Playlist> getLikedPlaylistsByUser(int userId) {
        return playlistRepository.findAll().stream()
                .filter(playlist -> playlist.getLikedByUsers().contains(userId))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user can access a playlist.
     *
     * @param userId The ID of the user attempting to access the playlist
     * @param playlist The playlist being accessed
     * @return True if the user can access the playlist, false otherwise
     */
    public boolean canUserAccessPlaylist(int userId, Playlist playlist) {
        // User owns the playlist
        if (playlist.getOwnerID() == userId) {
            return true;
        }

        // Playlist is explicitly shared with the user
        if (playlist.getSharedWith().contains(userId)) {
            return true;
        }

        // Playlist is publicly shareable and user follows the owner
        if (playlist.isPubliclyShareable() &&
                socialService.isFollowing(userId, playlist.getOwnerID())) {
            return true;
        }

        return false;
    }

    /**
     * Gets all playlists a user can access.
     *
     * @param userId The ID of the user whose accessible playlists to retrieve
     * @return A list of all playlists the user can access
     */
    public List<Playlist> getAllAccessiblePlaylistsForUser(int userId) {
        List<Playlist> allPlaylists = playlistRepository.findAll();

        return allPlaylists.stream()
                .filter(playlist -> canUserAccessPlaylist(userId, playlist))
                .collect(Collectors.toList());
    }

    /**
     * Gets all users who have liked a specific playlist.
     *
     * This information is only available to the playlist owner for privacy reasons.
     * The method retrieves detailed user information for each user who has liked the playlist.
     *
     * @param playlistId The ID of the playlist
     * @param requestingUserId The ID of the user requesting this information
     * @return A list of users who have liked the playlist
     * @throws SecurityException If the requesting user is not the owner of the playlist
     */
    public List<User> getUsersWhoLikedPlaylist(int playlistId, int requestingUserId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return new ArrayList<>();
        }

        Playlist playlist = playlistOpt.get();

        // Only the owner should be able to see who liked their playlist
        if (playlist.getOwnerID() != requestingUserId) {
            throw new SecurityException("Only the playlist owner can see who liked their playlist");
        }

        // Get the list of user IDs who liked this playlist
        List<Integer> likedByUserIds = playlist.getLikedByUsers();

        // Convert user IDs to User objects
        List<User> usersWhoLiked = new ArrayList<>();
        for (Integer userId : likedByUserIds) {
            try {
                User user = userService.getUserById(userId);
                usersWhoLiked.add(user);
            } catch (Exception e) {
                // Skip users who might not exist anymore
                System.err.println("User with ID " + userId + " no longer exists");
            }
        }

        return usersWhoLiked;
    }

    /**
     * Gets the count of users who have liked a specific playlist.
     *
     * This is a lightweight alternative to getUsersWhoLikedPlaylist() that returns
     * only the count without fetching detailed user information. This information
     * is only available to the playlist owner for privacy reasons.
     *
     * @param playlistId The ID of the playlist
     * @param requestingUserId The ID of the user requesting this information
     * @return The number of users who have liked the playlist
     * @throws SecurityException If the requesting user is not the owner of the playlist
     */
    public int getLikeCountForPlaylist(int playlistId, int requestingUserId) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return 0;
        }

        Playlist playlist = playlistOpt.get();

        // Only the owner should be able to see like counts for their playlist
        if (playlist.getOwnerID() != requestingUserId) {
            throw new SecurityException("Only the playlist owner can see like statistics for their playlist");
        }

        return playlist.getLikedByUsers().size();
    }

    /**
     * Shares a playlist with another user.
     *
     * This creates an explicit sharing relationship between the playlist and the
     * specified user, allowing them to access the playlist regardless of whether they
     * follow the playlist owner or whether the playlist is publicly shareable.
     *
     * @param playlistId The ID of the playlist to share
     * @param userIdToShareWith The ID of the user to share the playlist with
     * @return True if the playlist was successfully shared, false if the playlist wasn't found
     */
    public boolean sharePlaylist(int playlistId, int userIdToShareWith) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if already shared with user
        if (playlist.getSharedWith().contains(userIdToShareWith)) {
            return true; // Already shared
        }

        // Add user to shared list
        List<Integer> sharedWith = new ArrayList<>(playlist.getSharedWith());
        sharedWith.add(userIdToShareWith);
        playlist.setSharedWith(sharedWith);

        playlistRepository.update(playlist);
        return true;
    }

    /**
     * Unshares a playlist with a user.
     *
     * This removes the explicit sharing relationship between the playlist and the
     * specified user. After this operation, the user may still be able to access the
     * playlist if it's publicly shareable and they follow the playlist owner.
     *
     * @param playlistId The ID of the playlist to unshare
     * @param userIdToUnshareWith The ID of the user to unshare the playlist with
     * @return True if the playlist was successfully unshared, false if the playlist wasn't found
     *         or wasn't shared with the specified user
     */
    public boolean unsharePlaylist(int playlistId, int userIdToUnshareWith) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        if (playlistOpt.isEmpty()) {
            return false;
        }

        Playlist playlist = playlistOpt.get();

        // Check if shared with user
        if (!playlist.getSharedWith().contains(userIdToUnshareWith)) {
            return false; // Not shared with this user
        }

        // Remove user from shared list
        List<Integer> sharedWith = new ArrayList<>(playlist.getSharedWith());
        sharedWith.remove(Integer.valueOf(userIdToUnshareWith));
        playlist.setSharedWith(sharedWith);

        playlistRepository.update(playlist);
        return true;
    }
}