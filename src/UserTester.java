import factory.UserFactory;
import services.UserService;
import user.User;
import user.security.PasswordHasher;
import user.security.SHA256Hasher;

import java.util.List;

public class UserTester {
    public static void main(String[] args) {
        UserFactory uf = UserFactory.getInstance();
        UserService um = UserService.getInstance();
        PasswordHasher hasher = new SHA256Hasher();

        // 1. Create users
        System.out.println("=== Creating Users ===");
        User liam = uf.createUser("Liam", "Johnson", "liam_j", "liam@mail.com", "liamPass123");
        User emma = uf.createUser("Emma", "Smith", "emma_smith", "emma@mail.com", "emmaPass456");

        // 2. Test hash
        System.out.println("\n=== Testing Password Hash ===");
        String rawPassword = "liamPass123";
        System.out.println("Raw: " + rawPassword);
        System.out.println("Stored Hash: " + liam.getPassword());
        System.out.println("Match: " + hasher.verify(rawPassword, liam.getPassword()));

        // 3. Update operations
        System.out.println("\n=== Update Operations ===");
        uf.updateUsername("liam_j", "liam2025");
        uf.updatePassword("liam2025", "newLiamPass!");
        uf.updateEmail("liam2025", "liam2025@mail.com");

        // 4. Disable/Enable account
        System.out.println("\n=== Disable/Enable Account ===");
        uf.disableAccount(liam.getUserID());
        uf.enableAccount(liam.getUserID());
        uf.disableAccount("emma_smith");
        uf.enableAccount("emma_smith");

        // 5. Subscription changes
        System.out.println("\n=== Subscription Changes ===");
        uf.subscribeToPremium("liam2025");
        uf.downgradeToFree("liam2025");
        uf.downgradeToFreeIfExpired();

        // 6. Follow/unfollow via UserFactory
        System.out.println("\n=== Follow/Unfollow (Factory) ===");
        uf.followUser("liam2025", "emma_smith");
        uf.unfollowUser("liam2025", "emma_smith");

        // 7. Lookup via UserService
        System.out.println("\n=== Lookup via UserService ===");
        User byId       = um.getUserById(liam.getUserID());
        User byUsername = um.getUserByUsername("emma_smith");
        User byEmail    = um.getUserByEmail("liam2025@mail.com");
        System.out.println("Found by ID:       " + byId.getUsername());
        System.out.println("Found by username: " + byUsername.getUsername());
        System.out.println("Found by email:    " + byEmail.getUsername());

        // 8. Followers lookup via UserService
        System.out.println("\n=== Followers via UserService ===");
        uf.followUser("liam2025", "emma_smith");
        List<User> followers = um.getFollowers("emma_smith");
        List<User> followees = um.getFollowedUsers("liam2025");
        System.out.println("emma_smith's followers: " + followers.stream().map(User::getUsername).toList());
        System.out.println("liam2025 follows:       " + followees.stream().map(User::getUsername).toList());

        // 9. Delete operations
        System.out.println("\n=== Deleting Users ===");
        uf.deleteUser("emma_smith");            // by username
        uf.deleteUser(liam.getUserID());        // by ID

        System.out.println("\n=== All tests completed ===");
    }
}
