package factory;

import persistence.interfaces.UserRepositoryInterface;
import services.playlistServices.PlaylistService;
import services.playlistServices.SongInPlaylistService;
import services.songServices.SongService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;
import user.*;
import user.security.PasswordHasher;
import user.security.SHA256Hasher;
import user.subscription.*;
import user.subscription.FreeSubscription;
import user.subscription.SubscriptionInfo;

import java.util.*;

/**
 * Factory class responsible for creating, updating, deleting, and managing users and their subscriptions.
 * Implements the Singleton pattern.
 */
public class UserFactory {
    /**
     * Singleton instance of UserFactory.
     */
    private static UserFactory instance;

    /**
     * Repository for user persistence operations.
     */
    private final UserRepositoryInterface userRepository;

    /**
     * Password hasher for securely storing passwords.
     */
    private final PasswordHasher passwordHasher;

    /**
     * Playlist factory for creating playlists.
     */
    private final PlaylistFactory playlistFactory;

    /**
     * Playlist service for playlist operations.
     */
    private final PlaylistService playlistService;

    /**
     * Private constructor initializing repositories through RepositoryFactory.
     */
    private UserFactory() {
        this.userRepository = RepositoryFactory.getInstance().getUserRepository();
        this.passwordHasher = new SHA256Hasher(); //default
        this.playlistFactory = PlaylistFactory.getInstance();
        this.playlistService = PlaylistService.getInstance();
    }

    public UserFactory(UserRepositoryInterface userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.playlistFactory = PlaylistFactory.getInstance();
        this.playlistService = PlaylistService.getInstance();
    }

    /**
     * Returns the singleton instance of UserFactory.
     *
     * @return the singleton instance
     */
    public static synchronized UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    //Basic user methods

    /**
     * Creates a new user with a free subscription.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param username the username
     * @param email the email address
     * @param password the password
     * @return the created User object
     */
    public User createUser(String firstName, String lastName, String username, String email, String password) {
        // Get next available ID
        int newId = generateNextUserId();

        // Hash password
        String hashedPassword = passwordHasher.hash(password);

        // Create user object
        User user = new User(newId, username, email, hashedPassword, new Date());
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Set default values for a new user
        user.setActive(true);
        user.setFollowedUsersIDs(new ArrayList<>());
        user.setFollowersIDs(new ArrayList<>());

        // Assign a free subscription by default
        FreeSubscription freePlan = new FreeSubscription();
        user.setSubscriptionPlan(freePlan);
        user.setSubscriptionInfo(new SubscriptionInfo(new Date(), null));

        // Save user
        User savedUser = userRepository.save(user);

        createDefaultPlaylistForUser(savedUser);

        System.out.println("Created user → ID=" + savedUser.getUserID()
                + ", username=" + savedUser.getUsername());

        return savedUser;
    }

    /**
     * Creates a default playlist for a user with pre-selected songs.
     *
     * @param user The user to create the default playlist for
     */
    private void createDefaultPlaylistForUser(User user) {
        try {
            // Create default "Favorites" playlist using PlaylistFactory
            Playlist favoritesPlaylist = playlistFactory.createPlaylist("Favorites", user.getUserID());

            // Add 4 default songs to the playlist
            addDefaultSongsToPlaylist(favoritesPlaylist);

            System.out.println("Created default playlist 'Favorites' with default songs for user → ID=" + user.getUserID()
                    + ", username=" + user.getUsername());
        } catch (Exception e) {
            System.err.println("Error creating default playlist for user " + user.getUserID() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adds 4 default songs to a newly created user's favorites playlist.
     * Uses predefined song IDs for consistent default content.
     *
     * @param playlist The playlist to add default songs to
     */
    private void addDefaultSongsToPlaylist(Playlist playlist) {
        try {
            SongService songService = SongService.getInstance();
            SongInPlaylistService playlistSongService = SongInPlaylistService.getInstance();

            //Default Songs
            int[] defaultSongIds = {1141, 1146, 1148, 1140};

            for (int songId : defaultSongIds) {
                Song song = songService.getSongById(songId);
                if (song != null) {
                    playlistSongService.addSongToPlaylist(playlist.getPlaylistID(), song);
                    System.out.println("Added default song: " + song.getTitle() + " to favorites");
                } else {
                    System.out.println("Default song not found with ID: " + songId);
                }
            }

        } catch (Exception e) {
            System.err.println("Error adding default songs to playlist: " + e.getMessage());
        }
    }

    /**
     * Creates a new user with a specified subscription plan.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param username the username
     * @param email the email address
     * @param password the password
     * @param subscriptionPlan the subscription plan to assign
     * @param subscriptionDays duration in days; zero or negative means no end date
     * @return the created User object
     */
    public User createUser(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            SubscriptionPlan subscriptionPlan,
            int subscriptionDays) {

        // Get next available ID
        int newId = generateNextUserId();

        // Hash password
        String hashedPassword = passwordHasher.hash(password);

        // Create user object
        User user = new User(newId, username, email, hashedPassword, new Date());
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Set default values for a new user
        user.setActive(true);
        user.setFollowedUsersIDs(new ArrayList<>());
        user.setFollowersIDs(new ArrayList<>());

        // Calculate subscription end date
        Date startDate = new Date();
        Date endDate = null;
        if (subscriptionDays > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, subscriptionDays);
            endDate = calendar.getTime();
        }

        // Assign the specified subscription
        user.setSubscriptionPlan(subscriptionPlan);
        user.setSubscriptionInfo(new SubscriptionInfo(startDate, endDate));

        // Save user
        User savedUser = userRepository.save(user);

        createDefaultPlaylistForUser(savedUser);

        System.out.println("Created user → ID=" + savedUser.getUserID()
                + ", username=" + savedUser.getUsername()
                + ", subscription=" + subscriptionPlan.getType());

        return savedUser;
    }

    /**
     * Generates the next available user ID.
     *
     * @return the next available user ID
     */
    private int generateNextUserId() {
        return userRepository.findAll().stream()
                .mapToInt(User::getUserID)
                .max()
                .orElse(0) + 1;
    }


}