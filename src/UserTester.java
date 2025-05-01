import factory.UserFactory;
import managers.UserManager;
import user.User;

import java.util.List;

public class UserTester {
    public static void main(String[] args) {
        UserFactory uf = UserFactory.getInstance();
        UserManager um = UserManager.getInstance();

        // 1. Create users
        System.out.println("=== Creating Users ===");
        User alex = uf.createUser("Alex", "Johnson", "alex_j", "alexj@mail.com", "alexPass123");
        User maya = uf.createUser("Maya", "Smith","maya_smith","maya@mail.com","mayaPass456");

        // 2. Update operations
        System.out.println("\n=== Update Operations ===");
        uf.updateUsername("alex_j", "alex2025");
        uf.updatePassword("alex2025", "newAlexPass!");
        uf.updateEmail("alex2025", "alex2025@mail.com");

        // 3. Disable/Enable account
        System.out.println("\n=== Disable/Enable Account ===");
        uf.disableAccount(alex.getUserID());
        uf.enableAccount(alex.getUserID());
        uf.disableAccount("maya_smith");
        uf.enableAccount("maya_smith");

        // 4. Subscription changes
        System.out.println("\n=== Subscription Changes ===");
        uf.subscribeToPremium("alex2025");
        uf.downgradeToFree("alex2025");
        uf.downgradeToFreeIfExpired();

        // 5. Follow/unfollow via UserFactory
        System.out.println("\n=== Follow/Unfollow (Factory) ===");
        uf.followUser("alex2025", "maya_smith");
        uf.unfollowUser("alex2025", "maya_smith");

        // 6. Lookup via UserManager
        System.out.println("\n=== Lookup via UserManager ===");
        User byId       = um.getUserById(alex.getUserID());
        User byUsername = um.getUserByUsername("maya_smith");
        User byEmail    = um.getUserByEmail("alex2025@mail.com");
        System.out.println("Found by ID:       " + byId.getUsername());
        System.out.println("Found by username: " + byUsername.getUsername());
        System.out.println("Found by email:    " + byEmail.getUsername());

        // 7. Followers lookup via UserManager
        System.out.println("\n=== Followers via UserManager ===");
        uf.followUser("alex2025", "maya_smith");
        List<User> followers = um.getFollowers("maya_smith");
        List<User> followees = um.getFollowedUsers("alex2025");
        System.out.println("maya_smith's followers: " + followers.stream().map(User::getUsername).toList());
        System.out.println("alex2025 follows:       " + followees.stream().map(User::getUsername).toList());

        // 8. Delete operations
        System.out.println("\n=== Deleting Users ===");
        uf.deleteUser("maya_smith");            // by username
        uf.deleteUser(alex.getUserID());        // by ID

        System.out.println("\n=== All tests completed ===");
    }
}