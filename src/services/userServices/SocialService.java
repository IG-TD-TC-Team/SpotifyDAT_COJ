package services.userServices;


import persistence.interfaces.UserRepositoryInterface;
import factory.RepositoryFactory;
import services.userServices.exceptions.UserNotFoundException;
import user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing social relationships between users.
 * Handles followers, following relationships, and social interactions.
 * Implements the Singleton pattern.
 */
public class SocialService {

    // Singleton instance
    private static SocialService instance;

    // Dependencies
    private final UserRepositoryInterface userRepository;

    /**
     * Private constructor to prevent external instantiation.
     */
    private SocialService() {
        this.userRepository = RepositoryFactory.getUserRepository();
    }

    /**
     * Returns the singleton instance of SocialService.
     *
     * @return the singleton instance
     */
    public static synchronized SocialService getInstance() {
        if (instance == null) {
            instance = new SocialService();
        }
        return instance;
    }

    /**
     * Makes one user follow another.
     *
     * @param followerId the ID of the follower
     * @param followedId the ID of the user to follow
     * @return true if the follow operation was successful, false otherwise
     */
    public boolean followUser(int followerId, int followedId) {
        // Check if user is trying to follow themselves
        if (followerId == followedId) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        User follower = getUserById(followerId);
        User followed = getUserById(followedId);

        // Check if already following
        if (isFollowing(followerId, followedId)) {
            return true;
        }

        return userRepository.addFollower(followedId, followerId);
    }

    /**
     * Makes one user unfollow another.
     */
    public boolean unfollowUser(int followerId, int followedId) {
        User follower = getUserById(followerId);
        User followed = getUserById(followedId);

        if (!isFollowing(followerId, followedId)) {
            return false;
        }

        return userRepository.removeFollower(followedId, followerId);
    }

    /**
     * Checks if one user is following another.
     */
    public boolean isFollowing(int followerId, int followedId) {
        User follower = getUserById(followerId);
        List<Integer> followedUsers = follower.getFollowedUsersIDs();
        return followedUsers != null && followedUsers.contains(followedId);
    }

    /**
     * Gets a list of follower objects for a user.
     */
    public List<User> getFollowers(int userId) {
        getUserById(userId); // Check user exists

        List<Integer> followerIds = userRepository.getFollowerIds(userId);
        return convertIdsToUsers(followerIds);
    }

    /**
     * Gets a list of users that the specified user is following.
     */
    public List<User> getFollowedUsers(int userId) {
        getUserById(userId); // Check user exists

        List<Integer> followedIds = userRepository.getFollowedUserIds(userId);
        return convertIdsToUsers(followedIds);
    }

    /**
     * Gets follower count for a user.
     */
    public int getFollowerCount(int userId) {
        return userRepository.getFollowerIds(userId).size();
    }

    /**
     * Gets count of users that the specified user is following.
     */
    public int getFollowingCount(int userId) {
        return userRepository.getFollowedUserIds(userId).size();
    }

    /**
     * Finds users followed by both users (mutual follows).
     */
    public List<User> getMutualFollows(int userId1, int userId2) {
        List<Integer> user1Follows = userRepository.getFollowedUserIds(userId1);
        List<Integer> user2Follows = userRepository.getFollowedUserIds(userId2);

        List<Integer> mutualIds = new ArrayList<>();
        for (Integer id : user1Follows) {
            if (user2Follows.contains(id)) {
                mutualIds.add(id);
            }
        }

        return convertIdsToUsers(mutualIds);
    }

    // Private helper methods

    private User getUserById(int userId) {
        User user = UserService.getInstance().getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        return user;
    }

    private List<User> convertIdsToUsers(List<Integer> userIds) {
        List<User> users = new ArrayList<>();
        for (Integer id : userIds) {
            try {
                User user = getUserById(id);
                users.add(user);
            } catch (UserNotFoundException e) {
                // Skip invalid user IDs
            }
        }
        return users;
    }
}
