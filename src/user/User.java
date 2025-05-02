package user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import songsOrganisation.Library;
import songsOrganisation.Playlist;
import user.subscription.SubscriptionInfo;
import user.subscription.SubscriptionPlan;

public class User {
    // Identity fields
    private int userID;
    private String username;
    private String email;
    private String password;

    // Personal information
    private String firstName;
    private String lastName;
    private Date dateOfBirth;

    // Account information
    private Date accountCreationDate;
    private boolean isActive = true;

    // Subscription
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionInfo subscriptionInfo;

    // Social relationships
    private List<Integer> followedUsersIDs = new ArrayList<>();
    private List<Integer> followersIDs = new ArrayList<>();

    // Music organization references (but not operations)
    private final Library library;
    private Playlist defaultPlaylist;

    /**
     * Default constructor for Jackson deserialization.
     */
    public User() {
        this.followedUsersIDs = new ArrayList<>();
        this.followersIDs = new ArrayList<>();

        // Initialize library with empty playlist list
        this.defaultPlaylist = new Playlist("Favorites", 0);
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(defaultPlaylist);
        this.library = new Library(0, playlists);
    }

    /**
     * Constructor with required fields.
     */
    public User(int userID, String username, String email, String password, Date accountCreationDate) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.accountCreationDate = accountCreationDate;
        this.isActive = true;

        this.followedUsersIDs = new ArrayList<>();
        this.followersIDs = new ArrayList<>();

        // Initialize library with default playlist
        this.defaultPlaylist = new Playlist("Favorites", userID);
        List<Playlist> playlists = new ArrayList<>();
        playlists.add(defaultPlaylist);
        this.library = new Library(userID, playlists);
    }

    // Standard getters and setters
    public int getUserID() { return userID; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(Date accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }

    public SubscriptionInfo getSubscriptionInfo() {
        return subscriptionInfo;
    }

    public void setSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
        this.subscriptionInfo = subscriptionInfo;
    }

    public List<Integer> getFollowedUsersIDs() {
        return followedUsersIDs;
    }

    public void setFollowedUsersIDs(List<Integer> followedUsersIDs) {
        this.followedUsersIDs = followedUsersIDs;
    }

    public List<Integer> getFollowersIDs() {
        return followersIDs;
    }

    public void setFollowersIDs(List<Integer> followersIDs) {
        this.followersIDs = followersIDs;
    }

    // Library reference getters only - no management operations
    public Library getLibrary() { return library; }

    public Playlist getDefaultPlaylist() { return defaultPlaylist; }
    public void setDefaultPlaylist(Playlist playlist) { this.defaultPlaylist = playlist; }
}