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
        User docker = uf.createUser("Docker", "Costa", "docker_costa", "docker@example.com", "dockPass123");
        User bianca = uf.createUser("Bianca", "Costa","bianca_costa","bianca@example.com","biancaPass456");

        // 2. Update operations
        System.out.println("\n=== Update Operations ===");
        uf.updateUsername("docker_costa", "docker2025");
        uf.updatePassword("docker2025", "newDockPass!");
        uf.updateEmail("docker2025", "docker2025@example.com");

        // 3. Disable/Enable account
        System.out.println("\n=== Disable/Enable Account ===");
        uf.disableAccount(docker.getUserID());
        uf.enableAccount(docker.getUserID());
        uf.disableAccount("bianca_costa");
        uf.enableAccount("bianca_costa");

        // 4. Subscription changes
        System.out.println("\n=== Subscription Changes ===");
        uf.subscribeToPremium("docker2025");
        uf.downgradeToFree("docker2025");
        uf.downgradeToFreeIfExpired();

        // 5. Follow/unfollow via UserFactory
        System.out.println("\n=== Follow/Unfollow (Factory) ===");
        uf.followUser("docker2025", "bianca_costa");
        uf.unfollowUser("docker2025", "bianca_costa");

        // 6. Lookup via UserManager
        System.out.println("\n=== Lookup via UserManager ===");
        User byId       = um.getUserById(docker.getUserID());
        User byUsername = um.getUserByUsername("bianca_costa");
        User byEmail    = um.getUserByEmail("docker2025@example.com");
        System.out.println("Found by ID:       " + byId.getUsername());
        System.out.println("Found by username: " + byUsername.getUsername());
        System.out.println("Found by email:    " + byEmail.getUsername());

        // 7. Followers lookup via UserManager
        System.out.println("\n=== Followers via UserManager ===");
        uf.followUser("docker2025", "bianca_costa");
        List<User> followers = um.getFollowers("bianca_costa");
        List<User> followees = um.getFollowedUsers("docker2025");
        System.out.println("bianca_costa's followers: " + followers.stream().map(User::getUsername).toList());
        System.out.println("docker2025 follows:       " + followees.stream().map(User::getUsername).toList());

        // 8. Delete operations
        //System.out.println("\n=== Deleting Users ===");
        //uf.deleteUser("bianca_costa");            // by username
        //uf.deleteUser(docker.getUserID());        // by ID

        System.out.println("\n=== All tests completed ===");
    }
}
