import factory.RepositoryFactory;
import services.playlistServices.PlaylistService;
import services.playlistServices.SongInPlaylistService;
import services.playlistServices.SocialPlaylistService;
import services.songServices.SongService;
import persistence.interfaces.PlaylistRepositoryInterface;
import persistence.interfaces.SongRepositoryInterface;
import songsAndArtists.Genre;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

import java.util.List;
import java.util.Optional;

/**
 * Test class for demonstrating the PlaylistService functionality with the updated Playlist class structure.
 */
public class PlaylistTester {

    public static void main(String[] args) {
        // Get service instances
        PlaylistService playlistService = PlaylistService.getInstance();
        SongInPlaylistService songInPlaylistService = SongInPlaylistService.getInstance(
                RepositoryFactory.getInstance().getPlaylistRepository());
        SocialPlaylistService socialPlaylistService = SocialPlaylistService.getInstance();
        SongService songService = SongService.getInstance();

        // Get repositories
        PlaylistRepositoryInterface playlistRepository = RepositoryFactory.getInstance().getPlaylistRepository();
        SongRepositoryInterface songRepository = RepositoryFactory.getInstance().getSongRepository();

        // Print all existing playlists
        System.out.println("\n=== Existing Playlists ===");
        List<Playlist> existingPlaylists = playlistService.getAllPlaylists();
        for (Playlist playlist : existingPlaylists) {
            System.out.println(playlist.getName() + " (ID: " + playlist.getPlaylistID() +
                    ", Owner: " + playlist.getOwnerID() +
                    ", Songs: " + playlist.getSongCount() + ")");
        }

        // Create a new playlist
        System.out.println("\n=== Creating New Playlist ===");
        int testUserId = 5; // Using an existing user ID from the data

        // Using createPlaylist from PlaylistService instead of PlaylistFactory
        Playlist newPlaylist = new Playlist("Test Rock Playlist", testUserId);
        newPlaylist.setPlaylistID(generateNextPlaylistId(existingPlaylists));
        playlistRepository.save(newPlaylist);

        System.out.println("Created playlist: " + newPlaylist.getName() + " (ID: " + newPlaylist.getPlaylistID() + ")");

        // Add some songs to the playlist
        System.out.println("\n=== Adding Songs to Playlist ===");
        List<Song> rockSongs = songRepository.findByGenre(Genre.ROCK);

        int count = 0;
        for (Song song : rockSongs) {
            if (count < 5) { // Add first 5 rock songs
                // Using SongInPlaylistService to add songs to playlist
                songInPlaylistService.addSongToPlaylist(newPlaylist.getPlaylistID(), song);
                System.out.println("Added song: " + song.getTitle() + " to playlist");
                count++;
            } else {
                break;
            }
        }

        // Display playlist contents
        System.out.println("\n=== Playlist Contents ===");
        // Get the updated playlist
        Playlist updatedPlaylist = playlistService.getPlaylistById(newPlaylist.getPlaylistID());

        if (updatedPlaylist != null) {
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
                Song firstSong = updatedPlaylist.getSongs().getFirst();
                System.out.println("Moving song forward: " + firstSong.getTitle());
                // Use SongInPlaylistService to move the song
                songInPlaylistService.moveNext(updatedPlaylist.getPlaylistID(), firstSong);

                // Get the updated playlist again
                Playlist playlistAfterMove = playlistService.getPlaylistById(newPlaylist.getPlaylistID());
                if (playlistAfterMove != null) {
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
            // Create a copy manually since PlaylistFactory.copyPlaylist is not available
            Playlist copiedPlaylist = new Playlist("Copy of " + updatedPlaylist.getName(), testUserId);
            copiedPlaylist.setPlaylistID(generateNextPlaylistId(playlistService.getAllPlaylists()));

            // Copy songs from original playlist
            copiedPlaylist.setSongs(new java.util.LinkedList<>(updatedPlaylist.getSongs()));

            // Save the copied playlist
            playlistRepository.save(copiedPlaylist);

            System.out.println("Created copy: " + copiedPlaylist.getName() +
                    " with " + copiedPlaylist.getSongCount() + " songs");

            // Test sharing a playlist
            System.out.println("\n=== Sharing a Playlist ===");
            int userToShareWith = 3; // Another existing user ID
            boolean shared = socialPlaylistService.sharePlaylist(updatedPlaylist.getPlaylistID(), userToShareWith);
            System.out.println("Playlist shared with user ID " + userToShareWith + ": " + shared);

            // Find playlists shared with a user
            System.out.println("\n=== Playlists Shared With User ===");
            List<Playlist> sharedPlaylists = playlistRepository.findSharedWithUserByID(userToShareWith);
            for (Playlist playlist : sharedPlaylists) {
                System.out.println(playlist.getName() + " (Owner: " + playlist.getOwnerID() + ")");
            }

            // Clean up - delete test playlists
            System.out.println("\n=== Cleaning Up ===");
            playlistService.deletePlaylist(newPlaylist.getPlaylistID());
            playlistService.deletePlaylist(copiedPlaylist.getPlaylistID());
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

    /**
     * Generates the next available playlist ID based on existing playlists.
     *
     * @param existingPlaylists the list of existing playlists
     * @return the next available playlist ID
     */
    private static int generateNextPlaylistId(List<Playlist> existingPlaylists) {
        int maxId = 0;
        for (Playlist playlist : existingPlaylists) {
            if (playlist.getPlaylistID() > maxId) {
                maxId = playlist.getPlaylistID();
            }
        }
        return maxId + 1;
    }
}