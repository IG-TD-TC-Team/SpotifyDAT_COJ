import songsAndArtists.*;
import songsOrganisation.*;
import persistence.*;
import user.*;

import java.util.*;

/**
 * Comprehensive test class that demonstrates the persistence capabilities
 * of the application by performing CRUD operations on different entities.
 */
public class RepositoryTester {

    public static void main(String[] args) {
        System.out.println("Starting Repository Tests");
        System.out.println("=========================\n");

        // Test repositories one by one
        testArtistRepository();
        testSongRepository();
        testAlbumRepository();
        testUserRepository();
        testPlaylistRepository();
        testRelationships();

        System.out.println("\nAll tests completed!");
    }

    /**
     * Tests the ArtistRepository functionality.
     */
    private static void testArtistRepository() {
        System.out.println("Testing Artist Repository");
        System.out.println("-----------------------");

        // Create repository
        ArtistRepository artistRepo = new ArtistRepository();

        // Create artists
        Artist johnLennon = new Artist(
                3,
                "John",
                "Lennon",
                new Date(1940 - 1900, 9, 9), // Oct 9, 1940
                "UK"
        );

        Artist taylorSwift = new Artist(
                4,
                "Taylor",
                "Swift",
                new Date(1989 - 1900, 11, 13), // Dec 13, 1989
                "USA"
        );

        // Save artists
        artistRepo.add(johnLennon);
        artistRepo.add(taylorSwift);
        System.out.println("Added 2 new artists to the repository");

        // Retrieve all artists
        List<Artist> allArtists = artistRepo.findAll();
        System.out.println("Total artists in repository: " + allArtists.size());

        // Find artist by ID
        Artist foundArtist = artistRepo.findById(3);
        if (foundArtist != null) {
            System.out.println("Found artist by ID: " + foundArtist.getFirstName() + " " + foundArtist.getLastName());
        }

        // Update an artist
        johnLennon.setCountryOfBirth("United Kingdom");
        artistRepo.update(johnLennon);
        System.out.println("Updated John Lennon's country of birth");

        // Verify update
        Artist updatedArtist = artistRepo.findById(3);
        if (updatedArtist != null) {
            System.out.println("Updated country of birth: " + updatedArtist.getCountryOfBirth());
        }

        System.out.println("Artist repository test completed\n");
    }

    /**
     * Tests the SongRepository functionality.
     */
    private static void testSongRepository() {
        System.out.println("Testing Song Repository");
        System.out.println("---------------------");

        // Create repository
        SongRepository songRepo = new SongRepository();

        // Create songs
        Song imagine = new Song(301, "Imagine", 3, 3001, Genre.ROCK, 183, "/music/imagine.mp3");
        Song blankSpace = new Song(401, "Blank Space", 4, 4001, Genre.POP, 231, "/music/blankspace.mp3");
        Song shakeItOff = new Song(402, "Shake It Off", 4, 4001, Genre.POP, 219, "/music/shakeitoff.mp3");

        // Save songs
        songRepo.add(imagine);
        songRepo.add(blankSpace);
        songRepo.add(shakeItOff);
        System.out.println("Added 3 new songs to the repository");

        // Retrieve all songs
        List<Song> allSongs = songRepo.findAll();
        System.out.println("Total songs in repository: " + allSongs.size());

        // Find song by ID
        Optional<Song> foundSong = songRepo.findById(301);
        if (foundSong.isPresent()) {
            System.out.println("Found song by ID: " + foundSong.get().getTitle());
        }

        // Find songs by artist ID
        List<Song> artistSongs = songRepo.findByArtistId(4);
        System.out.println("Found " + artistSongs.size() + " songs by artist ID 4");

        // Find songs by genre
        List<Song> popSongs = songRepo.findByGenre(Genre.POP);
        System.out.println("Found " + popSongs.size() + " POP songs");

        // Update a song
        imagine.setDurationSeconds(180);
        songRepo.update(imagine);
        System.out.println("Updated Imagine's duration");

        // Verify update
        Optional<Song> updatedSong = songRepo.findById(301);
        if (updatedSong.isPresent()) {
            System.out.println("Updated duration: " + updatedSong.get().getDurationSeconds() + " seconds");
        }

        // Delete a song
        boolean deleted = songRepo.deleteById(402);
        System.out.println("Deleted song with ID 402: " + deleted);

        System.out.println("Song repository test completed\n");
    }

    /**
     * Tests the AlbumRepository functionality.
     */
    private static void testAlbumRepository() {
        System.out.println("Testing Album Repository");
        System.out.println("----------------------");

        // Create repository
        AlbumRepository albumRepo = new AlbumRepository();

        // Create albums
        Album imagine = new Album(3001, "Imagine", "3", List.of(301));
        Album nineteen89 = new Album(4001, "1989", "4", List.of(401, 402));

        // Save albums
        albumRepo.add(imagine);
        albumRepo.add(nineteen89);
        System.out.println("Added 2 new albums to the repository");

        // Retrieve all albums
        List<Album> allAlbums = albumRepo.findAll();
        System.out.println("Total albums in repository: " + allAlbums.size());

        // Find album by ID
        Optional<Album> foundAlbum = albumRepo.findById(3001);
        if (foundAlbum.isPresent()) {
            System.out.println("Found album by ID: " + foundAlbum.get().getTitle());
        }

        // Find albums by artist ID
        List<Album> artistAlbums = albumRepo.findByArtistId("4");
        System.out.println("Found " + artistAlbums.size() + " albums by artist ID 4");

        // Add a song to an album
        boolean songAdded = albumRepo.addSongToAlbum(3001, 302);
        System.out.println("Added song 302 to album 3001: " + songAdded);

        // Update an album
        imagine.setTitle("Imagine (Remastered)");
        albumRepo.update(imagine);
        System.out.println("Updated Imagine's title");

        // Verify update
        Optional<Album> updatedAlbum = albumRepo.findById(3001);
        if (updatedAlbum.isPresent()) {
            System.out.println("Updated title: " + updatedAlbum.get().getTitle());
        }

        // Remove a song from an album
        boolean songRemoved = albumRepo.removeSongFromAlbum(4001, 402);
        System.out.println("Removed song 402 from album 4001: " + songRemoved);

        // Delete an album
        boolean deleted = albumRepo.deleteById(3001);
        System.out.println("Deleted album with ID 3001: " + deleted);

        System.out.println("Album repository test completed\n");
    }

    /**
     * Tests the UserRepository functionality.
     */
    private static void testUserRepository() {
        System.out.println("Testing User Repository");
        System.out.println("---------------------");

        // Get the singleton instance
        UserRepository userRepo = UserRepository.getInstance();

        // Create users
        User john = new User(3, "johnlennon", "john@beatles.com", "password123", new Date());
        john.setFirstName("John");
        john.setLastName("Lennon");
        john.setDateOfBirth(new Date(1940 - 1900, 9, 9));
        john.setSubscriptionPlan(new PremiumPlan());
        john.setSubscriptionInfo(new SubscriptionInfo(new Date(), new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)));

        User taylor = new User(4, "taylorswift", "taylor@swift.com", "password456", new Date());
        taylor.setFirstName("Taylor");
        taylor.setLastName("Swift");
        taylor.setDateOfBirth(new Date(1989 - 1900, 11, 13));
        taylor.setSubscriptionPlan(new FreePlan());
        taylor.setSubscriptionInfo(new SubscriptionInfo(new Date(), null));

        // Save users
        try {
            userRepo.add(john);
            userRepo.add(taylor);
            System.out.println("Added 2 new users to the repository");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add users: " + e.getMessage());
        }

        // Retrieve all users
        List<User> allUsers = userRepo.findAll();
        System.out.println("Total users in repository: " + allUsers.size());

        // Find user by ID
        Optional<User> foundUser = userRepo.findById(3);
        if (foundUser.isPresent()) {
            System.out.println("Found user by ID: " + foundUser.get().getUsername());
        }

        // Find user by username
        Optional<User> foundByUsername = userRepo.findByUsername("taylorswift");
        if (foundByUsername.isPresent()) {
            System.out.println("Found user by username: " + foundByUsername.get().getFirstName() + " " + foundByUsername.get().getLastName());
        }

        // Update a user
        john.setEmail("john.lennon@beatles.com");
        try {
            userRepo.update(john);
            System.out.println("Updated John's email");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not update user: " + e.getMessage());
        }

        // Test following
        List<Integer> johnsFollowing = new ArrayList<>();
        johnsFollowing.add(4);
        john.setFollowedUsersIDs(johnsFollowing);

        List<Integer> taylorsFollowers = new ArrayList<>();
        taylorsFollowers.add(3);
        taylor.setFollowersIDs(taylorsFollowers);

        try {
            userRepo.update(john);
            userRepo.update(taylor);
            System.out.println("Updated following/follower relationships");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not update users: " + e.getMessage());
        }

        // Verify updates
        Optional<User> updatedJohn = userRepo.findById(3);
        if (updatedJohn.isPresent()) {
            System.out.println("John is following " + updatedJohn.get().getFollowedUsersIDs().size() + " users");
        }

        Optional<User> updatedTaylor = userRepo.findById(4);
        if (updatedTaylor.isPresent()) {
            System.out.println("Taylor has " + updatedTaylor.get().getFollowersIDs().size() + " followers");
        }

        System.out.println("User repository test completed\n");
    }

    /**
     * Tests the PlaylistRepository functionality.
     */
    private static void testPlaylistRepository() {
        System.out.println("Testing Playlist Repository");
        System.out.println("-------------------------");

        // Create repository
        PlaylistRepository playlistRepo = new PlaylistRepository();

        // We need a SongRepository to get Song objects
        SongRepository songRepo = new SongRepository();
        List<Song> allSongs = songRepo.findAll();

        // Create playlists
        Playlist rockClassics = new Playlist("Rock Classics", 3);
        Playlist popHits = new Playlist("Pop Hits", 4);

        // Add songs to playlists
        for (Song song : allSongs) {
            if (song.getGenre() == Genre.ROCK) {
                rockClassics.addSong(song);
            } else if (song.getGenre() == Genre.POP) {
                popHits.addSong(song);
            }
        }

        // Share playlist with another user
        rockClassics.addUserToShareWith(4);

        // Save playlists
        playlistRepo.add(rockClassics);
        playlistRepo.add(popHits);
        System.out.println("Added 2 new playlists to the repository");

        // Retrieve all playlists
        List<Playlist> allPlaylists = playlistRepo.findAll();
        System.out.println("Total playlists in repository: " + allPlaylists.size());

        // Find playlist by name and owner
        Optional<Playlist> foundPlaylist = playlistRepo.findByNameAndOwnerID("Rock Classics", 3);
        if (foundPlaylist.isPresent()) {
            System.out.println("Found playlist: " + foundPlaylist.get().getName() + " with " +
                    foundPlaylist.get().getSongCount() + " songs");
            System.out.println("Shared with " + foundPlaylist.get().getSharedWith().size() + " users");
        }

        // Find playlists by owner
        List<Playlist> ownerPlaylists = playlistRepo.findByOwnerID(4);
        System.out.println("Found " + ownerPlaylists.size() + " playlists owned by user 4");

        // Find playlists shared with a user
        List<Playlist> sharedPlaylists = playlistRepo.findSharedWithUserByID(4);
        System.out.println("Found " + sharedPlaylists.size() + " playlists shared with user 4");

        // Update a playlist
        if (foundPlaylist.isPresent()) {
            Playlist playlistToUpdate = foundPlaylist.get();
            playlistToUpdate.setName("Rock Classics (Updated)");
            playlistRepo.update(playlistToUpdate);
            System.out.println("Updated playlist name");
        }

        // Delete a playlist
        boolean deleted = playlistRepo.deleteByNameAndOwner("Pop Hits", 4);
        System.out.println("Deleted playlist 'Pop Hits': " + deleted);

        System.out.println("Playlist repository test completed\n");
    }

    /**
     * Tests relationships between different entities.
     */
    private static void testRelationships() {
        System.out.println("Testing Entity Relationships");
        System.out.println("--------------------------");

        // Create repositories
        ArtistRepository artistRepo = new ArtistRepository();
        SongRepository songRepo = new SongRepository();
        AlbumRepository albumRepo = new AlbumRepository();
        UserRepository userRepo = UserRepository.getInstance();
        PlaylistRepository playlistRepo = new PlaylistRepository();

        // Create a new artist
        Artist coldplay = new Artist(
                5,
                "Coldplay",
                "",
                new Date(1996 - 1900, 0, 1), // Jan 1, 1996 (as a band)
                "UK"
        );
        artistRepo.add(coldplay);
        System.out.println("Added new artist: Coldplay");

        // Create songs for the artist
        Song yellowSong = new Song(501, "Yellow", 5, 5001, Genre.ROCK, 265, "/music/yellow.mp3");
        Song paradiseSong = new Song(502, "Paradise", 5, 5001, Genre.ROCK, 278, "/music/paradise.mp3");
        Song fixYouSong = new Song(503, "Fix You", 5, 5002, Genre.ROCK, 296, "/music/fixyou.mp3");

        songRepo.add(yellowSong);
        songRepo.add(paradiseSong);
        songRepo.add(fixYouSong);
        System.out.println("Added 3 songs for Coldplay");

        // Update artist with song IDs
        coldplay.setSongs(Arrays.asList(501, 502, 503));
        artistRepo.update(coldplay);

        // Create albums for the artist
        Album parachutes = new Album(5001, "Parachutes", "5", Arrays.asList(501, 502));
        Album xAndY = new Album(5002, "X&Y", "5", Arrays.asList(503));

        albumRepo.add(parachutes);
        albumRepo.add(xAndY);
        System.out.println("Added 2 albums for Coldplay");

        // Create a new user
        User charlie = new User(5, "charlie", "charlie@example.com", "password789", new Date());
        charlie.setFirstName("Charlie");
        charlie.setLastName("Brown");
        charlie.setSubscriptionPlan(new PremiumPlan());

        try {
            userRepo.add(charlie);
            System.out.println("Added new user: Charlie Brown");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add user: " + e.getMessage());
        }

        // Create a playlist for the user with the artist's songs
        Playlist coldplayFavorites = new Playlist("Coldplay Favorites", 5);
        coldplayFavorites.addSong(yellowSong);
        coldplayFavorites.addSong(fixYouSong);

        playlistRepo.add(coldplayFavorites);
        System.out.println("Added playlist for Charlie with Coldplay songs");

        // Create a library for the user
        Library charliesLibrary = new Library(5, Arrays.asList(coldplayFavorites));
        LibraryRepository libraryRepo = new LibraryRepository();
        libraryRepo.add(charliesLibrary);
        System.out.println("Created library for Charlie");

        // Test retrieving relationships
        System.out.println("\nVerifying relationships:");

        // Get artist's songs
        if (coldplay.getSongs() != null) {
            System.out.println("Coldplay has " + coldplay.getSongs().size() + " songs");
        }

        // Get artist's albums
        List<Album> coldplayAlbums = albumRepo.findByArtistId("5");
        System.out.println("Coldplay has " + coldplayAlbums.size() + " albums");

        // Get songs in an album
        Optional<Album> firstAlbum = albumRepo.findById(5001);
        if (firstAlbum.isPresent()) {
            System.out.println("Album '" + firstAlbum.get().getTitle() + "' has " +
                    firstAlbum.get().getSongs().size() + " songs");
        }

        // Get user's library
        Optional<Library> userLibrary = libraryRepo.findById(5);
        if (userLibrary.isPresent()) {
            System.out.println("Charlie's library has " + userLibrary.get().getPlaylists().size() + " playlists");
        }

        // Get user's playlists
        List<Playlist> userPlaylists = playlistRepo.findByOwnerID(5);
        System.out.println("Charlie has " + userPlaylists.size() + " playlists");

        if (!userPlaylists.isEmpty()) {
            Playlist firstPlaylist = userPlaylists.get(0);
            System.out.println("Playlist '" + firstPlaylist.getName() + "' has " +
                    firstPlaylist.getSongCount() + " songs");
        }

        System.out.println("Entity relationship tests completed");
    }
}