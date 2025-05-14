package spotifyServer.commandProcessor;

import services.userServices.UserService;
import services.userServices.PasswordService;
import user.User;

/**
 * Processor that handles profile-related commands: viewprofile, updateprofile, changepassword.
 * This processor manages user profile operations through UserService and PasswordService.
 */
public class ProfileCommandProcessor extends AbstractProcessor {
    private final UserService userService = UserService.getInstance();
    private final PasswordService passwordService = PasswordService.getInstance();

    @Override
    public String processCommand(String command) {
        String lowerCommand = command.toLowerCase();

        // Check if this is a profile command
        if (lowerCommand.equals("viewprofile")) {
            return handleViewProfile();
        } else if (lowerCommand.startsWith("updateprofile ")) {
            return handleUpdateProfile(command);
        } else if (lowerCommand.startsWith("changepassword ")) {
            return handleChangePassword(command);
        }

        // Pass to next processor if not a profile command
        return handleNext(command);
    }

    /**
     * Handles viewing the current user's profile.
     * TODO: This needs session management to get the current user
     */
    private String handleViewProfile() {
        try {
            // TODO: Get current user ID from session

            int userId = 1; // This should come from session !!!

            User user = userService.getUserById(userId);

            StringBuilder profile = new StringBuilder("User Profile:\n");
            profile.append("════════════════════════════════\n");
            profile.append("User ID: ").append(user.getUserID()).append("\n");
            profile.append("Username: ").append(user.getUsername()).append("\n");
            profile.append("Email: ").append(user.getEmail()).append("\n");
            profile.append("Name: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
            profile.append("Account Status: ").append(user.isActive() ? "Active" : "Inactive").append("\n");
            profile.append("Subscription: ").append(user.getSubscriptionPlan() != null ?
                    user.getSubscriptionPlan().getType() : "None").append("\n");
            profile.append("Followers: ").append(user.getFollowersIDs().size()).append("\n");
            profile.append("Following: ").append(user.getFollowedUsersIDs().size()).append("\n");
            profile.append("════════════════════════════════\n");

            return profile.toString();

        } catch (Exception e) {
            return "ERROR: Failed to retrieve profile: " + e.getMessage();
        }
    }

    /**
     * Handles updating profile information.
     * Format: updateprofile <field> <newValue>
     * Fields: firstname, lastname, email
     */
    private String handleUpdateProfile(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid update format. Usage: updateprofile <field> <newValue>";
        }

        String field = parts[1].toLowerCase();
        String newValue = parts[2];

        try {
            // TODO: Get current user ID from session
            int userId = 1; // This should come from session !!!

            switch (field) {
                case "firstname":
                    userService.updateUserProfile(userId, newValue, null);
                    return "SUCCESS: First name updated to: " + newValue;

                case "lastname":
                    userService.updateUserProfile(userId, null, newValue);
                    return "SUCCESS: Last name updated to: " + newValue;

                case "email":
                    userService.updateEmail(userId, newValue);
                    return "SUCCESS: Email updated to: " + newValue;

                default:
                    return "ERROR: Invalid field. Valid fields are: firstname, lastname, email";
            }

        } catch (Exception e) {
            return "ERROR: Failed to update profile: " + e.getMessage();
        }
    }

    /**
     * Handles changing the user's password.
     * Format: changepassword <currentPassword> <newPassword>
     */
    private String handleChangePassword(String command) {
        String[] parts = command.split(" ", 3);

        if (parts.length < 3) {
            return "Error: Invalid format. Usage: changepassword <currentPassword> <newPassword>";
        }

        String currentPassword = parts[1];
        String newPassword = parts[2];

        try {
            // TODO: Get current user ID from session
            int userId = 1; // This should come from session !!!

            passwordService.changePassword(userId, currentPassword, newPassword);
            return "SUCCESS: Password changed successfully.";

        } catch (Exception e) {
            return "ERROR: Failed to change password: " + e.getMessage();
        }
    }
}