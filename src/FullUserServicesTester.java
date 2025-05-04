import factory.UserFactory;
import services.userServices.UserService;
import services.userServices.PasswordService;
import services.userServices.SubscriptionService;
import services.userServices.SocialService;
import user.User;
import user.subscription.*;

import java.util.List;

/**
 * Comprehensive test suite for all user services including SocialService.
 * Tests user management, authentication, subscriptions, and social connections.
 */
public class FullUserServicesTester {

    public static void main(String[] args) {
        // Initialize all services and factory
        UserFactory userFactory = UserFactory.getInstance();
        UserService userService = UserService.getInstance();
        PasswordService passwordService = PasswordService.getInstance();
        SubscriptionService subscriptionService = SubscriptionService.getInstance();
        SocialService socialService = SocialService.getInstance();

        System.out.println("=== Full User Services Test Suite ===\n");

        // Test 1: User Lifecycle (Create, Read, Update, Delete)
        System.out.println("Test 1: User Lifecycle");
        testUserLifecycle(userFactory, userService);

        // Test 2: Password Management
        System.out.println("\nTest 2: Password Management");
        testPasswordManagement(passwordService, userFactory);

        // Test 3: Subscription Management
        System.out.println("\nTest 3: Subscription Management");
        testSubscriptionManagement(subscriptionService, userFactory);

        // Test 4: Social Connections
        System.out.println("\nTest 4: Social Connections");
        testSocialConnections(socialService, userFactory);

        // Test 5: Advanced Social Features
        System.out.println("\nTest 5: Advanced Social Features");
        testAdvancedSocialFeatures(socialService, userFactory);

        // Test 6: Integration Test (All Services Working Together)
        System.out.println("\nTest 6: Integration Test");
        testServicesIntegration(userFactory, userService, passwordService,
                subscriptionService, socialService);

        System.out.println("\n=== Test Suite Completed ===");
    }

    private static void testUserLifecycle(UserFactory userFactory, UserService userService) {
        try {
            // CREATE - Basic user creation
            User user1 = userFactory.createUser(
                    "Alice", "Johnson", "alice_j",
                    "alice@test.com", "password123"
            );
            System.out.println("✓ Created user: " + user1.getUsername());

            // CREATE - User with subscription
            User user2 = userFactory.createUser(
                    "Bob", "Smith", "bob_s",
                    "bob@test.com", "password456",
                    new PremiumSubscription(), 90
            );
            System.out.println("✓ Created premium user: " + user2.getUsername());

            // READ - Test various retrieval methods
            User foundById = userService.getUserById(user1.getUserID());
            User foundByUsername = userService.getUserByUsername("alice_j");
            User foundByEmail = userService.getUserByEmail("alice@test.com");

            System.out.println("✓ Retrieved by ID: " + (foundById.getUserID() == user1.getUserID()));
            System.out.println("✓ Retrieved by username: " + foundByUsername.getUsername());
            System.out.println("✓ Retrieved by email: " + foundByEmail.getEmail());

            // UPDATE - Test profile updates
            user1 = userService.updateUserProfile(user1.getUserID(), "Alicia", "Johnson");
            System.out.println("✓ Updated profile: " + user1.getFirstName());

            user1 = userService.updateUsername(user1.getUserID(), "alicia_j");
            System.out.println("✓ Updated username: " + user1.getUsername());

            user1 = userService.updateEmail(user1.getUserID(), "alicia@test.com");
            System.out.println("✓ Updated email: " + user1.getEmail());

            // DELETE - Test deletion
            boolean deleted = userService.deleteUser(user1.getUserID());
            System.out.println("✓ User deleted: " + deleted);

        } catch (Exception e) {
            System.out.println("✗ User lifecycle test failed: " + e.getMessage());
        }
    }

    private static void testPasswordManagement(PasswordService passwordService, UserFactory userFactory) {
        try {
            User user = userFactory.createUser(
                    "Chris", "Wilson", "chris_w",
                    "chris@test.com", "oldPass123"
            );

            // Verify initial password
            boolean verified = passwordService.verifyPassword(user.getUserID(), "oldPass123");
            System.out.println("✓ Initial password verified: " + verified);

            // Change password
            passwordService.changePassword(user.getUserID(), "oldPass123", "newPass456");
            boolean newVerified = passwordService.verifyPassword(user.getUserID(), "newPass456");
            System.out.println("✓ Password changed and verified: " + newVerified);

            // Reset password (admin action)
            passwordService.resetPassword(user.getUserID(), "resetPass789");
            boolean resetVerified = passwordService.verifyPassword(user.getUserID(), "resetPass789");
            System.out.println("✓ Password reset and verified: " + resetVerified);

        } catch (Exception e) {
            System.out.println("✗ Password management test failed: " + e.getMessage());
        }
    }

    private static void testSubscriptionManagement(SubscriptionService subscriptionService, UserFactory userFactory) {
        try {
            // Create free user
            User freeUser = userFactory.createUser(
                    "Diana", "Prince", "diana_p",
                    "diana@test.com", "password123"
            );

            // Test upgrades
            subscriptionService.upgradeToPremium(freeUser.getUserID());
            boolean hasPremium = subscriptionService.hasActiveSubscription(
                    freeUser.getUserID(), PremiumSubscription.class);
            System.out.println("✓ Upgraded to premium: " + hasPremium);

            subscriptionService.upgradeToFamily(freeUser.getUserID());
            boolean hasFamily = subscriptionService.hasActiveSubscription(
                    freeUser.getUserID(), FamilySubscription.class);
            System.out.println("✓ Upgraded to family: " + hasFamily);

            subscriptionService.upgradeToStudent(freeUser.getUserID());
            boolean hasStudent = subscriptionService.hasActiveSubscription(
                    freeUser.getUserID(), StudentSubscription.class);
            System.out.println("✓ Upgraded to student: " + hasStudent);

            // Test downgrade
            subscriptionService.downgradeToFree(freeUser.getUserID());
            boolean isFree = subscriptionService.hasActiveSubscription(
                    freeUser.getUserID(), FreeSubscription.class);
            System.out.println("✓ Downgraded to free: " + isFree);

            // Test subscription renewal
            subscriptionService.createSubscription(freeUser.getUserID(), new PremiumSubscription(), 30);
            subscriptionService.renewSubscription(freeUser.getUserID(), 60);
            System.out.println("✓ Subscription renewed for 60 days");

        } catch (Exception e) {
            System.out.println("✗ Subscription management test failed: " + e.getMessage());
        }
    }

    private static void testSocialConnections(SocialService socialService, UserFactory userFactory) {
        try {
            // Create test users
            User user1 = userFactory.createUser("Eve", "Adams", "eve_a", "eve@test.com", "pass123");
            User user2 = userFactory.createUser("Frank", "Wright", "frank_w", "frank@test.com", "pass456");
            User user3 = userFactory.createUser("Grace", "Kim", "grace_k", "grace@test.com", "pass789");

            // Test following
            boolean followed = socialService.followUser(user1.getUserID(), user2.getUserID());
            System.out.println("✓ User1 followed User2: " + followed);

            // Test follower count
            int followerCount = socialService.getFollowerCount(user2.getUserID());
            System.out.println("✓ User2 has " + followerCount + " follower(s)");

            int followingCount = socialService.getFollowingCount(user1.getUserID());
            System.out.println("✓ User1 is following " + followingCount + " user(s)");

            // Test is following check
            boolean isFollowing = socialService.isFollowing(user1.getUserID(), user2.getUserID());
            System.out.println("✓ User1 is following User2: " + isFollowing);

            // Test get followers
            List<User> followers = socialService.getFollowers(user2.getUserID());
            System.out.println("✓ Retrieved follower list: " + followers.size());

            // Test unfollow
            boolean unfollowed = socialService.unfollowUser(user1.getUserID(), user2.getUserID());
            System.out.println("✓ User1 unfollowed User2: " + unfollowed);

        } catch (Exception e) {
            System.out.println("✗ Social connections test failed: " + e.getMessage());
        }
    }

    private static void testAdvancedSocialFeatures(SocialService socialService, UserFactory userFactory) {
        try {
            // Create a social network
            User userA = userFactory.createUser("Hannah", "Lee", "hannah_l", "hannah@test.com", "pass123");
            User userB = userFactory.createUser("Isaac", "Newton", "isaac_n", "isaac@test.com", "pass456");
            User userC = userFactory.createUser("Julia", "Moore", "julia_m", "julia@test.com", "pass789");
            User userD = userFactory.createUser("Kevin", "Park", "kevin_p", "kevin@test.com", "pass012");

            // Create connections: A follows B and C, B follows C and D, C follows D
            socialService.followUser(userA.getUserID(), userB.getUserID());
            socialService.followUser(userA.getUserID(), userC.getUserID());
            socialService.followUser(userB.getUserID(), userC.getUserID());
            socialService.followUser(userB.getUserID(), userD.getUserID());
            socialService.followUser(userC.getUserID(), userD.getUserID());

            // Test mutual follows
            List<User> mutualFollows = socialService.getMutualFollows(userA.getUserID(), userB.getUserID());
            System.out.println("✓ Mutual follows between A and B: " + mutualFollows.size());

            // Test edge cases
            boolean selfFollow = false;
            try {
                socialService.followUser(userA.getUserID(), userA.getUserID());
            } catch (IllegalArgumentException e) {
                selfFollow = true;
                System.out.println("✓ Self-follow prevented correctly");
            }

            // Test double follow (should return true but not duplicate)
            boolean doubleFollow = socialService.followUser(userA.getUserID(), userB.getUserID());
            System.out.println("✓ Double follow handled: " + doubleFollow);

        } catch (Exception e) {
            System.out.println("✗ Advanced social features test failed: " + e.getMessage());
        }
    }

    private static void testServicesIntegration(UserFactory userFactory, UserService userService,
                                                PasswordService passwordService,
                                                SubscriptionService subscriptionService,
                                                SocialService socialService) {
        try {
            // Create two users with different subscriptions
            User premiumUser = userFactory.createUser(
                    "Leonard", "Cohen", "leonard_c",
                    "leonard@test.com", "password123",
                    new PremiumSubscription(), 365
            );

            User studentUser = userFactory.createUser(
                    "Maya", "Angelou", "maya_a",
                    "maya@test.com", "password456",
                    new StudentSubscription(), 180
            );

            // Premium user follows student user
            socialService.followUser(premiumUser.getUserID(), studentUser.getUserID());

            // Update student's password
            passwordService.changePassword(studentUser.getUserID(), "password456", "newPassword789");

            // Verify premium user can see student in their followed list
            List<User> premiumFollowing = socialService.getFollowedUsers(premiumUser.getUserID());
            System.out.println("✓ Premium user follows: " + premiumFollowing.size() + " user(s)");

            // Verify student user profile updates
            studentUser = userService.getUserById(studentUser.getUserID());
            System.out.println("✓ Student user updated: " + studentUser.getUsername());

            // Test subscription validation for premium user
            subscriptionService.validatePremiumSubscription(premiumUser.getUserID());
            System.out.println("✓ Premium subscription validated");

            // Test downgrading expired subscriptions
            int downgraded = subscriptionService.downgradeExpiredSubscriptions();
            System.out.println("✓ Expired subscriptions checked: " + downgraded + " downgraded");

            System.out.println("✓ Integration test completed successfully");

        } catch (Exception e) {
            System.out.println("✗ Integration test failed: " + e.getMessage());
        }
    }
}