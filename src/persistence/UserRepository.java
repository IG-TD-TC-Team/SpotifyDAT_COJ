package persistence;

import user.User;

public class UserRepository extends JsonRepository<User>{

    //Singleton instance
    private static UserRepository instance;

    //Private constructor
    private UserRepository() {
        super(User.class, "users.json");
    }

    //Return single instance
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // Retrieve
    public void findById(int userId){}
    public void findByUsername(String username){}
    public void findByEmail(String email){}
    // findAll already inherited

    // Add
    public boolean emailExists(String email){return false;}
    public void add(User user){}

    // Modify
    public void update(User user){}

    // Delete
    public void delete(User user){}

    // Authenticate
    public boolean checkCredentialsByUsername(String username, String password){return false;}
    public boolean checkCredentialsByEmail(String email, String password){return false;}

    // Retrieve follows
    public void findFollowers(User user){}
    public void findUsersFollowed(User user){}

}
