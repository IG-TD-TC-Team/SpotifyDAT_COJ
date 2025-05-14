package spotifyServer.commandProcessor;

import services.userServices.SocialService;
import services.userServices.UserService;
import user.User;
import java.util.List;

/**
 * Processor that handles social commands: follow, unfollow, followers, following, searchuser.
 * This processor manages social relationships between users.
 */
public class SocialCommandProcessor extends AbstractProcessor {
    private final SocialService socialService = SocialService.getInstance();
    private final UserService userService = UserService.getInstance();

    @Override
    public String processCommand(String command) {
        String lowerCommand = command.toLowerCase();

        // Check if this is a social command
        if (lowerCommand.startsWith("follow ")) {
            return handleFollow(command);
        } else if (lowerCommand.startsWith("unfollow ")) {
            return handleUnfollow(command);
        } else if (lowerCommand.equals("followers")) {
            return handleGetFollowers();
        } else if (lowerCommand.equals("following")) {
            return handleGetFollowing();
        } else if (lowerCommand.startsWith("searchuser ")) {
            return handleSearchUser(command);
        }

        // Pass to next processor if not a social command
        return handleNext(command);
    }

    /**
     * Handles the follow command.
     * Format: follow id <userId> or follow username <username>
     */
    private String handleFollow(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: follow id <userId> or follow username <username>";
        }

        String type = parts[1].toLowerCase();
        String identifier = parts[2];

        try {
            // TODO: Get current user ID from session
            int currentUserId = 1; // This should come from session

            int targetUserId;

            if ("id".equals(type)) {
                targetUserId = Integer.parseInt(identifier);
            } else if ("username".equals(type)) {
                User targetUser = userService.getUserByUsername(identifier);
                targetUserId = targetUser.getUserID();
            } else {
                return "ERROR: Invalid type. Use 'id' or 'username'";
            }

            boolean success = socialService.followUser(currentUserId, targetUserId);

            if (success) {
                return "SUCCESS: Now following user ID " + targetUserId;
            } else {
                return "ERROR: Failed to follow user. You may already be following them.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid user ID format";
        } catch (Exception e) {
            return "ERROR: Failed to follow user: " + e.getMessage();
        }
    }

    /**
     * Handles the unfollow command.
     * Format: unfollow id <userId> or unfollow username <username>
     */
    private String handleUnfollow(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: unfollow id <userId> or unfollow username <username>";
        }

        String type = parts[1].toLowerCase();
        String identifier = parts[2];

        try {
            // TODO: Get current user ID from session
            int currentUserId = 1; // This should come from session

            int targetUserId;

            if ("id".equals(type)) {
                targetUserId = Integer.parseInt(identifier);
            } else if ("username".equals(type)) {
                User targetUser = userService.getUserByUsername(identifier);
                targetUserId = targetUser.getUserID();
            } else {
                return "ERROR: Invalid type. Use 'id' or 'username'";
            }

            boolean success = socialService.unfollowUser(currentUserId, targetUserId);

            if (success) {
                return "SUCCESS: Unfollowed user ID " + targetUserId;
            } else {
                return "ERROR: Failed to unfollow user. You may not be following them.";
            }

        } catch (NumberFormatException e) {
            return "ERROR: Invalid user ID format";
        } catch (Exception e) {
            return "ERROR: Failed to unfollow user: " + e.getMessage();
        }
    }

    /**
     * Handles getting the list of followers for the current user.
     */
    private String handleGetFollowers() {
        try {
            // TODO: Get current user ID from session
            int currentUserId = 1; // This should come from session

            List<User> followers = socialService.getFollowers(currentUserId);

            if (followers.isEmpty()) {
                return "You have no followers.";
            }

            StringBuilder response = new StringBuilder("Your Followers:\n");
            response.append("═══════════════════════════════\n");

            for (User follower : followers) {
                response.append("ID: ").append(follower.getUserID())
                        .append(" | @").append(follower.getUsername())
                        .append(" | ").append(follower.getFirstName())
                        .append(" ").append(follower.getLastName())
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Total followers: ").append(followers.size());

            return response.toString();

        } catch (Exception e) {
            return "ERROR: Failed to get followers: " + e.getMessage();
        }
    }

    /**
     * Handles getting the list of users that the current user is following.
     */
    private String handleGetFollowing() {
        try {
            // TODO: Get current user ID from session
            int currentUserId = 1; // This should come from session

            List<User> following = socialService.getFollowedUsers(currentUserId);

            if (following.isEmpty()) {
                return "You are not following anyone.";
            }

            StringBuilder response = new StringBuilder("Users You Follow:\n");
            response.append("═══════════════════════════════\n");

            for (User user : following) {
                response.append("ID: ").append(user.getUserID())
                        .append(" | @").append(user.getUsername())
                        .append(" | ").append(user.getFirstName())
                        .append(" ").append(user.getLastName())
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Total following: ").append(following.size());

            return response.toString();

        } catch (Exception e) {
            return "ERROR: Failed to get following list: " + e.getMessage();
        }
    }

    /**
     * Handles searching for users.
     * Format: searchuser name <name> or searchuser username <username>
     */
    private String handleSearchUser(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: searchuser name <name> or searchuser username <username>";
        }

        String type = parts[1].toLowerCase();
        String searchTerm = parts[2];

        try {
            List<User> results = null;

            if ("name".equals(type)) {
                // Search by first name or last name
                results = userService.findUsersByFirstName(searchTerm);
                results.addAll(userService.findUsersByLastName(searchTerm));
            } else if ("username".equals(type)) {
                try {
                    User user = userService.getUserByUsername(searchTerm);
                    results = List.of(user);
                } catch (Exception e) {
                    results = List.of();
                }
            } else {
                return "ERROR: Invalid search type. Use 'name' or 'username'";
            }

            if (results.isEmpty()) {
                return "No users found matching: " + searchTerm;
            }

            StringBuilder response = new StringBuilder("Search Results:\n");
            response.append("═══════════════════════════════\n");

            for (User user : results) {
                response.append("ID: ").append(user.getUserID())
                        .append(" | @").append(user.getUsername())
                        .append(" | ").append(user.getFirstName())
                        .append(" ").append(user.getLastName())
                        .append(" | Email: ").append(user.getEmail())
                        .append("\n");
            }

            response.append("═══════════════════════════════\n");
            response.append("Found ").append(results.size()).append(" user(s)");

            return response.toString();

        } catch (Exception e) {
            return "ERROR: Failed to search users: " + e.getMessage();
        }
    }
}