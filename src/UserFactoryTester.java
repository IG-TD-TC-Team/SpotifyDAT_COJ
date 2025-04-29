import factory.UserFactory;
import user.User;

public class UserFactoryTester {
    public static void main(String[] args) {
        UserFactory uf = UserFactory.getInstance();

        // 1. Create users
        System.out.println("=== Creating Users ===");
        User alice = uf.createUser("Alice", "Smith", "alice", "alice@example2025.com", "pass123");
        User bob   = uf.createUser("Bob",   "Johnson","bob",   "bob@example2025.com",   "pass456");

        // 2. Update operations
        System.out.println("\n=== Update Operations ===");
        uf.updateUsername("alice", "alice_new");
        uf.updatePassword("alice_new", "newPass!");
        uf.updateEmail("alice_new", "alice_new@example.com");

        // 3. Disable/Enable account
        System.out.println("\n=== Disable/Enable Account ===");
        uf.disableAccount(alice.getUserID());
        uf.enableAccount(alice.getUserID());
        uf.disableAccount("bob");
        uf.enableAccount("bob");

        // 4. Subscription changes
        System.out.println("\n=== Subscription Changes ===");
        uf.subscribeToPremium("alice_new");
        uf.downgradeToFree("alice_new");
        // Check expiration (no real expiration here)
        uf.downgradeToFreeIfExpired();

        // 5. Follow/unfollow
        System.out.println("\n=== Follow/Unfollow ===");
        uf.followUser("alice_new", "bob");
        uf.unfollowUser("alice_new", "bob");

        // 6. Delete operations
        System.out.println("\n=== Deleting Users ===");
        uf.deleteUser("bob");           // delete by username
        uf.deleteUser(alice.getUserID()); // delete by ID

        System.out.println("\n=== All tests completed ===");
    }
}
