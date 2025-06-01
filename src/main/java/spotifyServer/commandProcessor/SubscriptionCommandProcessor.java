package spotifyServer.commandProcessor;

import services.userServices.SubscriptionService;
import services.userServices.UserService;
import user.User;
import user.subscription.SubscriptionPlan;
import user.subscription.SubscriptionInfo;

/**
 * Processor that handles subscription-related commands: viewsubscription, upgradesubscription, downgradesubscription.
 * This processor manages subscription operations through the SubscriptionService.
 */
public class SubscriptionCommandProcessor extends AbstractProcessor {
    private final SubscriptionService subscriptionService = SubscriptionService.getInstance();
    private final UserService userService = UserService.getInstance();

    @Override
    public String processCommand(String command) {
        String lowerCommand = command.toLowerCase();

        // Check if this is a subscription command
        if (lowerCommand.equals("viewsubscription")) {
            return handleViewSubscription();
        } else if (lowerCommand.startsWith("upgradesubscription ")) {
            return handleUpgradeSubscription(command);
        } else if (lowerCommand.equals("downgradesubscription")) {
            return handleDowngradeSubscription();
        }

        // Pass to next processor if not a subscription command
        return handleNext(command);
    }

    /**
     * Handles viewing the current subscription details.
     */
    private String handleViewSubscription() {
        try {
            int userId = getCurrentUserId();

            User user = userService.getUserById(userId);
            SubscriptionPlan plan = user.getSubscriptionPlan();
            SubscriptionInfo info = user.getSubscriptionInfo();

            StringBuilder response = new StringBuilder("Subscription Details:\n");
            response.append("═══════════════════════════════\n");

            if (plan == null) {
                response.append("No active subscription\n");
            } else {
                response.append("Plan Type: ").append(plan.getType()).append("\n");
                response.append("Has Ads: ").append(plan.hasAds() ? "Yes" : "No").append("\n");
                response.append("Can Skip Tracks: ").append(plan.skipTracks() ? "Yes" : "No").append("\n");
                response.append("Offline Listening: ").append(plan.canListenOffline() ? "Yes" : "No").append("\n");
                response.append("Download Songs: ").append(plan.downloadSongs() ? "Yes" : "No").append("\n");
                response.append("Shuffle Only: ").append(plan.shuffleOnly() ? "Yes" : "No").append("\n");
                response.append("Max Quality: ").append(plan.getMaxQuality()).append(" kbps\n");
                response.append("Device Limit: ").append(plan.getDeviceLimit()).append("\n");
            }

            if (info != null) {
                response.append("\nSubscription Info:\n");
                response.append("Start Date: ").append(info.getStartDate()).append("\n");
                response.append("End Date: ").append(info.getEndDate() != null ? info.getEndDate() : "N/A").append("\n");
                response.append("Auto Renew: ").append(info.isAutoRenew() ? "Yes" : "No").append("\n");
                response.append("Price: $").append(info.getPrice()).append("\n");

                if (subscriptionService.isExpired(userId)) {
                    response.append("\n SUBSCRIPTION EXPIRED \n");
                }
            }

            response.append("═══════════════════════════════");

            return response.toString();

        } catch (Exception e) {
            return "ERROR: Failed to retrieve subscription details: " + e.getMessage();
        }
    }

    /**
     * Handles upgrading the subscription.
     * Format: upgradesubscription <planType>
     * Plan types: premium, family, student
     */
    private String handleUpgradeSubscription(String command) {
        String[] parts = command.split(" ", 2);

        if (parts.length < 2) {
            return "Error: Missing plan type. Usage: upgradesubscription <planType>\n" +
                    "Available plans: premium, family, student";
        }

        String planType = parts[1].toLowerCase();

        try {
            int userId = getCurrentUserId();

            switch (planType) {
                case "premium":
                    subscriptionService.upgradeToPremium(userId);
                    return "SUCCESS: Upgraded to Premium subscription! Enjoy ad-free listening.";

                case "family":
                    subscriptionService.upgradeToFamily(userId);
                    return "SUCCESS: Upgraded to Family subscription! You can now share with up to 6 family members.";

                case "student":
                    subscriptionService.upgradeToStudent(userId);
                    return "SUCCESS: Upgraded to Student subscription! Enjoy premium benefits at a discounted rate.";

                default:
                    return "ERROR: Invalid plan type. Available plans: premium, family, student";
            }

        } catch (Exception e) {
            return "ERROR: Failed to upgrade subscription: " + e.getMessage();
        }
    }

    /**
     * Handles downgrading the subscription to free.
     */
    private String handleDowngradeSubscription() {
        try {
            int userId = getCurrentUserId();

            subscriptionService.downgradeToFree(userId);

            return "SUCCESS: Downgraded to Free subscription. You will now experience ads and limited features.";

        } catch (Exception e) {
            return "ERROR: Failed to downgrade subscription: " + e.getMessage();
        }
    }
}