
import songsAndArtists.*;
import songsOrganisation.*;
import factory.*;
import managers.*;
import user.*;

import java.util.*;

/**
 * Test class for validating the factory and manager components.
 * This class demonstrates how the factories handle creation while
 * managers handle retrieval and business logic.
 */
public class FactoryManagerTest {

    public static void main(String[] args) {
        System.out.println("Starting Factory and Manager Test...");

        // Get factory and manager instances
        MusicFactory musicFactory = MusicFactory.getInstance();
        SongManager songManager = SongManager.getInstance();
        //UserFactory userFactory = UserFactory.getInstance();
        UserManager userManager = UserManager.getInstance();

        // Test case 1: Create and retrieve an artist
        testCreateAndRetrieveArtist(musicFactory, songManager);

        // Test case 2: Create and retrieve an album
        testCreateAndRetrieveAlbum(musicFactory, songManager);

        // Test case 3: Create and retrieve a song
        testCreateAndRetrieveSong(musicFactory, songManager);

        // Test case 4: Create a complete song (with auto-created artist and album)
        testCreateCompleteSong(musicFactory, songManager);

        // Test case 5: Create and retrieve a user
        //testCreateAndRetrieveUser(userFactory, userManager);

        // Test case 6: Test user following relationship
        //testUserFollowing(userFactory, userManager);

        // Test case 7: Test song search by various criteria
        testSongSearchCriteria(musicFactory, songManager);

        System.out.println("All tests completed.");
    }

    /**
     * Tests creating an artist with the factory and retrieving it with the manager.
     */
    private static void testCreateAndRetrieveArtist(MusicFactory musicFactory, SongManager songManager) {
        System.out.println("\n=== Testing Artist Creation and Retrieval ===");

        // Create a new artist using the factory
        String firstName = "David";
        String lastName = "Bowie";
        Date birthDate = new Date(1947, 0, 8); // January 8, 1947
        String country = "UK";

        Artist artist = musicFactory.createArtist(firstName, lastName, birthDate, country);
        System.out.println("Created artist: " + artist.getFirstName() + " " + artist.getLastName() +
                " (ID: " + artist.getArtistID() + ")");

        //Refresh manager to ensure we have the latest data
        songManager.refreshCache();

        // Retrieve the artist using the manager
        Artist retrievedArtist = songManager.getArtistById(artist.getArtistID());

        // Validate that we got the same artist
        if (retrievedArtist != null) {
            System.out.println("Retrieved artist: " + retrievedArtist.getFirstName() + " " +
                    retrievedArtist.getLastName());
            System.out.println("Test result: " +
                    (retrievedArtist.getFirstName().equals(firstName) &&
                            retrievedArtist.getLastName().equals(lastName) ? "PASSED" : "FAILED"));
        } else {
            System.out.println("Test result: FAILED - Could not retrieve artist");
        }
    }

    /**
     * Tests creating an album with the factory and retrieving it with the manager.
     */
    private static void testCreateAndRetrieveAlbum(MusicFactory musicFactory, SongManager songManager) {
        System.out.println("\n=== Testing Album Creation and Retrieval ===");

        // First create an artist for the album
        Artist artist = musicFactory.createArtist("Pink", "Floyd", new Date(1965, 7, 15), "UK");

        // Create a new album using the factory
        String albumTitle = "Dark Side of the Moon";
        Album album = musicFactory.createAlbum(albumTitle, artist.getArtistID());
        System.out.println("Created album: " + album.getTitle() + " (ID: " + album.getId() + ")");

        //Refresh manager to ensure we have the latest data


        // Retrieve the album using the manager
        Album retrievedAlbum = songManager.getAlbumById(album.getId());

        // Validate that we got the same album
        if (retrievedAlbum != null) {
            System.out.println("Retrieved album: " + retrievedAlbum.getTitle());
            System.out.println("Test result: " +
                    (retrievedAlbum.getTitle().equals(albumTitle) ? "PASSED" : "FAILED"));
        } else {
            System.out.println("Test result: FAILED - Could not retrieve album");
        }
    }

    /**
     * Tests creating a song with the factory and retrieving it with the manager.
     */
    private static void testCreateAndRetrieveSong(MusicFactory musicFactory, SongManager songManager) {
        System.out.println("\n=== Testing Song Creation and Retrieval ===");

        // First create an artist and album for the song
        Artist artist = musicFactory.createArtist("Queen", "", new Date(1970, 6, 1), "UK");

        Album album = musicFactory.createAlbum("A Night at the Opera", artist.getArtistID());


        // Create a new song using the factory
        String songTitle = "Bohemian Rhapsody";
        int duration = 355; // 5:55
        Song song = musicFactory.createSongWithAutoPath(
                songTitle,
                artist.getArtistID(),
                album.getId(),
                Genre.ROCK,
                duration
        );
        System.out.println("Created song: " + song.getTitle() + " (ID: " + song.getSongId() + ")");

        //Refresh manager to ensure we have the latest data


        // Retrieve the song using the manager
        Song retrievedSong = songManager.getSongById(song.getSongId());

        // Validate that we got the same song
        if (retrievedSong != null) {
            System.out.println("Retrieved song: " + retrievedSong.getTitle());
            System.out.println("Test result: " +
                    (retrievedSong.getTitle().equals(songTitle) ? "PASSED" : "FAILED"));
        } else {
            System.out.println("Test result: FAILED - Could not retrieve song");
        }
    }

    /**
     * Tests creating a complete song (with automatic artist and album creation).
     */
    private static void testCreateCompleteSong(MusicFactory musicFactory, SongManager songManager) {
        System.out.println("\n=== Testing Complete Song Creation ===");

        // Create a complete song (artist and album will be created automatically)
        String songTitle = "Stairway to Heaven";
        String artistFirstName = "Led";
        String artistLastName = "Zeppelin";
        String albumTitle = "Led Zeppelin IV";
        int duration = 482; // 8:02

        Song song = musicFactory.createCompleteSong(
                songTitle,
                artistFirstName,
                artistLastName,
                albumTitle,
                Genre.ROCK,
                duration
        );


        System.out.println("Created complete song: " + song.getTitle() + " by " +
                artistFirstName + " " + artistLastName +
                " in album " + albumTitle);

        //Refresh manager to ensure we have the latest data


        // Test retrieving by ID
        Song retrievedSong = songManager.getSongById(song.getSongId());

        // Verify song was created correctly
        if (retrievedSong != null) {
            Artist artist = songManager.getArtistById(retrievedSong.getArtistId());
            Album album = songManager.getAlbumById(retrievedSong.getAlbumId());

            boolean artistCorrect = artist != null &&
                    artist.getFirstName().equals(artistFirstName) &&
                    artist.getLastName().equals(artistLastName);

            boolean albumCorrect = album != null && album.getTitle().equals(albumTitle);

            System.out.println("Retrieved song: " + retrievedSong.getTitle());
            System.out.println("Artist check: " + (artistCorrect ? "PASSED" : "FAILED"));
            System.out.println("Album check: " + (albumCorrect ? "PASSED" : "FAILED"));
        } else {
            System.out.println("Test result: FAILED - Could not retrieve song");
        }
    }

    /**
     * Tests searching for songs using various criteria.
     */
    private static void testSongSearchCriteria(MusicFactory musicFactory, SongManager songManager) {
        System.out.println("\n=== Testing Song Search Criteria ===");

        // Create test data
        Artist artist = musicFactory.createArtist("The", "Beatles", new Date(1960, 1, 1), "UK");
        Album album = musicFactory.createAlbum("Abbey Road", artist.getArtistID());

        // Create multiple songs with the same artist and album
        Song song1 = musicFactory.createSongWithAutoPath("Come Together", artist.getArtistID(), album.getId(), Genre.ROCK, 260);
        Song song2 = musicFactory.createSongWithAutoPath("Something", artist.getArtistID(), album.getId(), Genre.ROCK, 182);
        Song song3 = musicFactory.createSongWithAutoPath("Maxwell's Silver Hammer", artist.getArtistID(), album.getId(), Genre.ROCK, 207);

        System.out.println("Created test songs for The Beatles");

        // Test search by artist
        List<Song> songsByArtist = songManager.getSongsByArtistId(artist.getArtistID());
        System.out.println("Found " + songsByArtist.size() + " songs by artist ID " + artist.getArtistID());
        System.out.println("Search by artist ID: " + (songsByArtist.size() >= 3 ? "PASSED" : "FAILED"));

        // Test search by album
        List<Song> songsByAlbum = songManager.getSongsByAlbumId(album.getId());
        System.out.println("Found " + songsByAlbum.size() + " songs in album ID " + album.getId());
        System.out.println("Search by album ID: " + (songsByAlbum.size() >= 3 ? "PASSED" : "FAILED"));

        // Test search by title
        List<Song> songsByTitle = songManager.getSongsByTitle("Something");
        System.out.println("Found " + songsByTitle.size() + " songs with title 'Something'");
        System.out.println("Search by title: " + (songsByTitle.size() >= 1 ? "PASSED" : "FAILED"));

        // Test search by genre
        List<Song> songsByGenre = songManager.getSongsByGenre(Genre.ROCK);
        System.out.println("Found " + songsByGenre.size() + " rock songs");
        System.out.println("Search by genre: " + (songsByGenre.size() >= 3 ? "PASSED" : "FAILED"));
    }
}