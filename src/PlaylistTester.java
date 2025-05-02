import factory.PlaylistFactory;
import factory.RepositoryFactory;
import managers.PlaylistService;
import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Genre;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.List;
import java.util.Optional;

/**
 * Test class for demonstrating the PlaylistService and PlaylistFactory functionality.
 */
public class PlaylistTester {

    public static void main(String[] args) {
        // Get manager, factory, and repositories
        PlaylistService playlistManager = PlaylistService.getInstance();
        PlaylistFactory playlistFactory = PlaylistFactory.getInstance();
        PlaylistRepositoryInterface playlistRepository = RepositoryFactory.getPlaylistRepository();
        SongRepositoryInterface songRepository = RepositoryFactory.getSongRepository();

        // Print all existing playlists
        System.out.println("\n=== Existing Playlists ===");
        List<Playlist> existingPlaylists = playlistManager.getAllPlaylists();
        for (Playlist playlist : existingPlaylists) {
            System.out.println(playlist.getName() + " (ID: " + playlist.getPlaylistID() +
                    ", Owner: " + playlist.getOwnerID() +
                    ", Songs: " + playlist.getSongCount() + ")");
        }

        // Create a new playlist
        System.out.println("\n=== Creating New Playlist ===");
        int testUserId = 5; // Using an existing user ID from the data
        Playlist newPlaylist = playlistFactory.createPlaylist("Test Rock Playlist", testUserId);
        System.out.println("Created playlist: " + newPlaylist.getName() + " (ID: " + newPlaylist.getPlaylistID() + ")");

        // Add some songs to the playlist
        System.out.println("\n=== Adding Songs to Playlist ===");
        List<Song> rockSongs = songRepository.findByGenre(Genre.ROCK);

        int count = 0;
        for (Song song : rockSongs) {
            if (count < 5) { // Add first 5 rock songs
                playlistFactory.addSongToPlaylist(newPlaylist.getPlaylistID(), song.getSongId());
                System.out.println("Added song: " + song.getTitle() + " to playlist");
                count++;
            } else {
                break;
            }
        }

        // Display playlist contents
        System.out.println("\n=== Playlist Contents ===");
        // Get the updated playlist
        Optional<Playlist> updatedPlaylistOpt = playlistRepository.findById(newPlaylist.getPlaylistID());
        if (updatedPlaylistOpt.isPresent()) {
            Playlist updatedPlaylist = updatedPlaylistOpt.get();
            System.out.println("Playlist: " + updatedPlaylist.getName());
            System.out.println("Songs: " + updatedPlaylist.getSongCount());
            System.out.println("Total duration: " + formatDuration(updatedPlaylist.getTotalDuration()));

            List<Song> songs = updatedPlaylist.getSongs();
            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                System.out.println((i+1) + ". " + song.getTitle() + " - " +
                        formatDuration(song.getDurationSeconds()));
            }

            // Test moving songs in the playlist
            System.out.println("\n=== Moving Songs in Playlist ===");
            if (updatedPlaylist.getSongCount() >= 2) {
                Song firstSong = updatedPlaylist.getSongs().get(0);
                System.out.println("Moving song forward: " + firstSong.getTitle());
                playlistFactory.moveSongForward(updatedPlaylist.getPlaylistID(), firstSong.getSongId());

                // Get the updated playlist again
                Optional<Playlist> playlistAfterMoveOpt = playlistRepository.findById(newPlaylist.getPlaylistID());
                if (playlistAfterMoveOpt.isPresent()) {
                    Playlist playlistAfterMove = playlistAfterMoveOpt.get();
                    System.out.println("New playlist order:");
                    List<Song> songsAfterMove = playlistAfterMove.getSongs();
                    for (int i = 0; i < songsAfterMove.size(); i++) {
                        Song song = songsAfterMove.get(i);
                        System.out.println((i+1) + ". " + song.getTitle());
                    }
                }
            }

            // Test copying a playlist
            System.out.println("\n=== Copying a Playlist ===");
            Playlist copiedPlaylist = playlistFactory.copyPlaylist(
                    updatedPlaylist.getPlaylistID(),
                    "Copy of " + updatedPlaylist.getName(),
                    testUserId);
            System.out.println("Created copy: " + copiedPlaylist.getName() +
                    " with " + copiedPlaylist.getSongCount() + " songs");

            // Test sharing a playlist
            System.out.println("\n=== Sharing a Playlist ===");
            int userToShareWith = 3; // Another existing user ID
            boolean shared = playlistFactory.sharePlaylist(updatedPlaylist.getPlaylistID(), userToShareWith);
            System.out.println("Playlist shared with user ID " + userToShareWith + ": " + shared);

            // Find playlists shared with a user
            System.out.println("\n=== Playlists Shared With User ===");
            List<Playlist> sharedPlaylists = playlistRepository.findSharedWithUserByID(userToShareWith);
            for (Playlist playlist : sharedPlaylists) {
                System.out.println(playlist.getName() + " (Owner: " + playlist.getOwnerID() + ")");
            }

            // Clean up - delete test playlists
            System.out.println("\n=== Cleaning Up ===");
            playlistFactory.deletePlaylist(newPlaylist.getPlaylistID());
            playlistFactory.deletePlaylist(copiedPlaylist.getPlaylistID());
            System.out.println("Test playlists deleted");
        }
    }

    /**
     * Formats duration in seconds to a human-readable string (MM:SS).
     *
     * @param seconds the duration in seconds
     * @return formatted duration string
     */
    private static String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}