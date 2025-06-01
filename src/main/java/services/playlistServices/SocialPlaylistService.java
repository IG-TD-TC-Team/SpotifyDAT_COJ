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

public class SocialPlaylistService {

    private static SocialPlaylistService instance;
    private final PlaylistRepositoryInterface playlistRepository;
    private final UserRepositoryInterface userRepository;
    private final UserService userService;
    private final SocialService socialService;

    private SocialPlaylistService() {
        this.playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        this.userRepository = RepositoryFactory.getInstance().getUserRepository();
        this.userService = UserService.getInstance();
        this.socialService = SocialService.getInstance();
    }

    public static synchronized SocialPlaylistService getInstance() {
        if (instance == null) {
            instance = new SocialPlaylistService();
        }
        return instance;
    }

    /**
     * Sets the shareability of a playlist
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
     * Gets all shared playlists from users that the given user follows
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
     * Likes a playlist
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
     * Unlikes a playlist
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
     * Gets all playlists that a user has liked
     */
    public List<Playlist> getLikedPlaylistsByUser(int userId) {
        return playlistRepository.findAll().stream()
                .filter(playlist -> playlist.getLikedByUsers().contains(userId))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user can access a playlist (either owns it, it's shared with them, or from a followed user)
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
     * Gets all playlists a user can access (their own + shared + from followed users)
     */
    public List<Playlist> getAllAccessiblePlaylistsForUser(int userId) {
        List<Playlist> allPlaylists = playlistRepository.findAll();

        return allPlaylists.stream()
                .filter(playlist -> canUserAccessPlaylist(userId, playlist))
                .collect(Collectors.toList());
    }

    /**
     * Gets all users who have liked a specific playlist
     * Only the playlist owner or users with permission can see this information
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
     * Gets the count of users who have liked a specific playlist
     * This is a lightweight alternative that doesn't require fetching all user details
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
     * Shares a playlist with another user
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
     * Unshares a playlist with a user
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