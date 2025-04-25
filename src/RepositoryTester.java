import persistence.*;
import persistence.interfaces.*;
import songsAndArtists.*;
import songsOrganisation.*;
import user.User;
// Removed other user.* imports that were causing issues

import java.util.*;

/**
 * Test class for testing CRUD operations on various repositories.
 * This class tests UserRepository, SongRepository, ArtistRepository,
 * AlbumRepository, PlaylistRepository, and LibraryRepository.
 */
public class RepositoryTester {

    public static void main(String[] args) {
        System.out.println("Starting Repository Tests...");

        // Test each repository
        testUserRepository();
        testSongRepository();
        testArtistRepository();
        testAlbumRepository();
        testPlaylistRepository();
        testLibraryRepository();

        System.out.println("All tests completed!");
    }

    /**
     * Tests CRUD operations for UserRepository.
     */
    private static void testUserRepository() {
        System.out.println("\n===== Testing UserRepository =====");
        UserRepository userRepo = UserRepository.getInstance();

        // Test CREATE
        User newUser = createTestUser();
        try {
            User savedUser = userRepo.save(newUser);
            System.out.println("CREATE: User created with ID: " + savedUser.getUserID());

            // Test READ
            Optional<User> retrievedUser = userRepo.findById(savedUser.getUserID());
            if (retrievedUser.isPresent()) {
                System.out.println("READ: Successfully retrieved user: " + retrievedUser.get().getUsername());
            } else {
                System.out.println("READ: Failed to retrieve user!");
            }

            // Test UPDATE
            User userToUpdate = retrievedUser.get();
            userToUpdate.setUsername("updated_" + userToUpdate.getUsername());
            Optional<User> updatedUser = userRepo.update(userToUpdate);
            if (updatedUser.isPresent()) {
                System.out.println("UPDATE: Successfully updated user: " + updatedUser.get().getUsername());
            } else {
                System.out.println("UPDATE: Failed to update user!");
            }

            // Test DELETE
            boolean deleted = userRepo.deleteById(userToUpdate.getUserID());
            System.out.println("DELETE: User deleted: " + deleted);
        } catch (Exception e) {
            System.out.println("Error during UserRepository test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests CRUD operations for SongRepository.
     */
    private static void testSongRepository() {
        System.out.println("\n===== Testing SongRepository =====");
        SongRepository songRepo = SongRepository.getInstance();

        // Test CREATE
        Song newSong = createTestSong();
        try {
            Song savedSong = songRepo.save(newSong);
            System.out.println("CREATE: Song created with ID: " + savedSong.getSongId());

            // Test READ
            Optional<Song> retrievedSong = songRepo.findById(savedSong.getSongId());
            if (retrievedSong.isPresent()) {
                System.out.println("READ: Successfully retrieved song: " + retrievedSong.get().getTitle());
            } else {
                System.out.println("READ: Failed to retrieve song!");
            }

            // Test UPDATE
            Song songToUpdate = retrievedSong.get();
            songToUpdate.setTitle("Updated " + songToUpdate.getTitle());
            Optional<Song> updatedSong = songRepo.update(songToUpdate);
            if (updatedSong.isPresent()) {
                System.out.println("UPDATE: Successfully updated song: " + updatedSong.get().getTitle());
            } else {
                System.out.println("UPDATE: Failed to update song!");
            }

            // Test READ by genre
            List<Song> rockSongs = songRepo.findByGenre(Genre.ROCK);
            System.out.println("READ: Found " + rockSongs.size() + " rock songs");

            // Test DELETE
            boolean deleted = songRepo.deleteById(songToUpdate.getSongId());
            System.out.println("DELETE: Song deleted: " + deleted);
        } catch (Exception e) {
            System.out.println("Error during SongRepository test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests CRUD operations for ArtistRepository.
     */
    private static void testArtistRepository() {
        System.out.println("\n===== Testing ArtistRepository =====");
        ArtistRepository artistRepo = ArtistRepository.getInstance();

        // Test CREATE
        Artist newArtist = createTestArtist();
        try {
            Artist savedArtist = artistRepo.save(newArtist);
            System.out.println("CREATE: Artist created with ID: " + savedArtist.getArtistID());

            // Test READ
            Optional<Artist> retrievedArtist = artistRepo.findById(savedArtist.getArtistID());
            if (retrievedArtist.isPresent()) {
                System.out.println("READ: Successfully retrieved artist: " +
                        retrievedArtist.get().getFirstName() + " " +
                        retrievedArtist.get().getLastName());
            } else {
                System.out.println("READ: Failed to retrieve artist!");
            }

            // Test UPDATE
            Artist artistToUpdate = retrievedArtist.get();
            artistToUpdate.setFirstName("Updated" + artistToUpdate.getFirstName());
            Optional<Artist> updatedArtist = artistRepo.update(artistToUpdate);
            if (updatedArtist.isPresent()) {
                System.out.println("UPDATE: Successfully updated artist: " +
                        updatedArtist.get().getFirstName() + " " +
                        updatedArtist.get().getLastName());
            } else {
                System.out.println("UPDATE: Failed to update artist!");
            }

            // Test song association
            boolean songAdded = artistRepo.addSongToArtist(artistToUpdate.getArtistID(), 101);
            System.out.println("UPDATE: Song added to artist: " + songAdded);

            // Test DELETE
            boolean deleted = artistRepo.deleteById(artistToUpdate.getArtistID());
            System.out.println("DELETE: Artist deleted: " + deleted);
        } catch (Exception e) {
            System.out.println("Error during ArtistRepository test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests CRUD operations for AlbumRepository.
     */
    private static void testAlbumRepository() {
        System.out.println("\n===== Testing AlbumRepository =====");
        AlbumRepository albumRepo = AlbumRepository.getInstance();

        // Test CREATE
        Album newAlbum = createTestAlbum();
        try {
            Album savedAlbum = albumRepo.save(newAlbum);
            System.out.println("CREATE: Album created with ID: " + savedAlbum.getId());

            // Test READ
            Optional<Album> retrievedAlbum = albumRepo.findById(savedAlbum.getId());
            if (retrievedAlbum.isPresent()) {
                System.out.println("READ: Successfully retrieved album: " + retrievedAlbum.get().getTitle());
            } else {
                System.out.println("READ: Failed to retrieve album!");
            }

            // Test UPDATE
            Album albumToUpdate = retrievedAlbum.get();
            albumToUpdate.setTitle("Updated " + albumToUpdate.getTitle());
            Optional<Album> updatedAlbum = albumRepo.update(albumToUpdate);
            if (updatedAlbum.isPresent()) {
                System.out.println("UPDATE: Successfully updated album: " + updatedAlbum.get().getTitle());
            } else {
                System.out.println("UPDATE: Failed to update album!");
            }

            // Test adding song to album
            boolean songAdded = albumRepo.addSongToAlbum(albumToUpdate.getId(), 101);
            System.out.println("UPDATE: Song added to album: " + songAdded);

            // Test DELETE
            boolean deleted = albumRepo.deleteById(albumToUpdate.getId());
            System.out.println("DELETE: Album deleted: " + deleted);
        } catch (Exception e) {
            System.out.println("Error during AlbumRepository test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests CRUD operations for PlaylistRepository.
     */
    private static void testPlaylistRepository() {
        System.out.println("\n===== Testing PlaylistRepository =====");
        PlaylistRepository playlistRepo = PlaylistRepository.getInstance();

        // Test CREATE
        Playlist newPlaylist = createTestPlaylist();
        try {
            Playlist savedPlaylist = playlistRepo.save(newPlaylist);
            System.out.println("CREATE: Playlist created with name: " + savedPlaylist.getName());

            // Test READ
            Optional<Playlist> retrievedPlaylist = playlistRepo.findByNameAndOwnerID(savedPlaylist.getName(), savedPlaylist.getOwnerID());
            if (retrievedPlaylist.isPresent()) {
                System.out.println("READ: Successfully retrieved playlist: " + retrievedPlaylist.get().getName());
            } else {
                System.out.println("READ: Failed to retrieve playlist!");
            }

            // Test UPDATE
            Playlist playlistToUpdate = retrievedPlaylist.get();
            playlistToUpdate.setName("Updated " + playlistToUpdate.getName());
            Optional<Playlist> updatedPlaylist = playlistRepo.update(playlistToUpdate);
            if (updatedPlaylist.isPresent()) {
                System.out.println("UPDATE: Successfully updated playlist: " + updatedPlaylist.get().getName());
            } else {
                System.out.println("UPDATE: Failed to update playlist!");
            }

            // Test sharing playlist
            boolean shared = playlistRepo.sharePlaylistWithUser(playlistToUpdate.getName(), playlistToUpdate.getOwnerID(), 2);
            System.out.println("UPDATE: Shared playlist with user: " + shared);

            // Test DELETE
            boolean deleted = playlistRepo.deleteByNameAndOwner(playlistToUpdate.getName(), playlistToUpdate.getOwnerID());
            System.out.println("DELETE: Playlist deleted: " + deleted);
        } catch (Exception e) {
            System.out.println("Error during PlaylistRepository test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests CRUD operations for LibraryRepository.
     */
    private static void testLibraryRepository() {
        System.out.println("\n===== Testing LibraryRepository =====");
        LibraryRepository libraryRepo = LibraryRepository.getInstance();

        // Test CREATE/READ
        try {
            int testUserId = 999;
            Library library = libraryRepo.createLibrary(testUserId);
            System.out.println("CREATE: Library created for user ID: " + library.getUserID());

            // Test READ
            Optional<Library> retrievedLibrary = libraryRepo.findById(testUserId);
            if (retrievedLibrary.isPresent()) {
                System.out.println("READ: Successfully retrieved library for user ID: " + retrievedLibrary.get().getUserID());
            } else {
                System.out.println("READ: Failed to retrieve library!");
            }

            // Test UPDATE (add playlist to library)
            Playlist playlist = createTestPlaylist();
            playlist.setOwnerID(testUserId);
            boolean playlistAdded = libraryRepo.addPlaylistToLibrary(testUserId, playlist);
            System.out.println("UPDATE: Playlist added to library: " + playlistAdded);

            // Test READ (get user playlists)
            List<Playlist> userPlaylists = libraryRepo.getUserPlaylists(testUserId);
            System.out.println("READ: Found " + userPlaylists.size() + " playlists for user");

            // Test UPDATE (remove playlist from library)
            boolean playlistRemoved = libraryRepo.removePlaylistFromLibrary(testUserId, playlist.getName());
            System.out.println("UPDATE: Playlist removed from library: " + playlistRemoved);

            // Test DELETE
            boolean deleted = libraryRepo.deleteById(testUserId);
            System.out.println("DELETE: Library deleted: " + deleted);
        } catch (Exception e) {
            System.out.println("Error during LibraryRepository test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a test User object for testing purposes.
     */
    private static User createTestUser() {
        // Generate unique ID to avoid conflicts
        int userId = (int) (System.currentTimeMillis() % 10000);
        String username = "testuser" + userId;
        String email = "test" + userId + "@example.com";

        User user = new User(userId, username, email, "password123", new Date());
        user.setFirstName("Test");
        user.setLastName("User");

        // Skip setting the subscription plan for now to avoid FreePlan instantiation issue
        // The User object should still be valid for the repository tests

        return user;
    }

    /**
     * Creates a test Song object for testing purposes.
     */
    private static Song createTestSong() {
        // Generate unique ID to avoid conflicts
        int songId = (int) (System.currentTimeMillis() % 10000);

        return new Song(
                songId,
                "Test Song " + songId,
                1, // artistId
                1001, // albumId
                Genre.ROCK,
                180, // 3 minutes
                "/music/test_song_" + songId + ".mp3"
        );
    }

    /**
     * Creates a test Artist object for testing purposes.
     */
    private static Artist createTestArtist() {
        // Generate unique ID to avoid conflicts
        int artistId = (int) (System.currentTimeMillis() % 10000);

        return new Artist(
                artistId,
                "Test",
                "Artist " + artistId,
                new Date(),
                "Test Country"
        );
    }

    /**
     * Creates a test Album object for testing purposes.
     */
    private static Album createTestAlbum() {
        // Generate unique ID to avoid conflicts
        int albumId = (int) (System.currentTimeMillis() % 10000);

        return new Album(
                albumId,
                "Test Album " + albumId,
                1, // artistId
                new ArrayList<>() // empty song list
        );
    }

    /**
     * Creates a test Playlist object for testing purposes.
     */
    private static Playlist createTestPlaylist() {
        // Create a test playlist with a unique name
        String playlistName = "Test Playlist " + (System.currentTimeMillis() % 10000);
        Playlist playlist = new Playlist(playlistName, 1); // owner ID 1

        return playlist;
    }
}