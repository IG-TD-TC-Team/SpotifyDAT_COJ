package spotifyServer.commandProcessor;

import services.playbackServices.PlaybackService;
import songsAndArtists.Song;
import songsOrganisation.Playlist;

/**
 * MODIFICATIONS: NEW PROCESSOR for client-driven playlist navigation.
 * Handles playlist commands via command port (45000) while audio streams via port 45001.
 * This separates control protocol from audio streaming to prevent MP3 corruption.
 */
public class PlaylistNavigationProcessor extends AbstractProcessor {

    private final PlaybackService playbackService;

    public PlaylistNavigationProcessor() {
        this.playbackService = PlaybackService.getInstance();
    }

    @Override
    public String processCommand(String command) {
        // Check authentication using built-in method
        if (!isAuthenticated()) {
            return "ERROR: Not authenticated";
        }

        Integer userId = getCurrentUserId();
        if (userId == null) {
            return "ERROR: No user ID available";
        }

        String[] parts = command.trim().split(" ", 2);
        String cmd = parts[0].toLowerCase();

        try {
            switch (cmd) {
                case "next":
                case "playlist_next":
                    return handleNextSong(userId);

                case "previous":
                case "playlist_previous":
                    return handlePreviousSong(userId);

                case "playlist_status":
                    return handlePlaylistStatus(userId);

                case "current_song":
                    return handleCurrentSong(userId);

                case "get_next_song":
                    return handleGetNextSong(userId);

                case "auto_next":
                    // MODIFICATIONS: This is called by client when current song ends
                    return handleAutoNext(userId);

                default:
                    // Pass to next processor using the built-in method
                    return handleNext(command);
            }
        } catch (Exception e) {
            System.err.println("Error in playlist navigation: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * MODIFICATIONS: Handle manual next song request.
     */
    private String handleNextSong(Integer userId) {
        if (!playbackService.hasActivePlaylist(userId)) {
            return "ERROR: No active playlist";
        }

        Song nextSong = playbackService.getNextSong(userId);
        if (nextSong != null) {
            System.out.println("User " + userId + " manually skipped to: " + nextSong.getTitle());

            // MODIFICATIONS: Return multi-line response with stream request
            StringBuilder response = new StringBuilder();
            response.append("SUCCESS: Next song ready\n");
            response.append("STREAM_REQUEST|45001|").append(nextSong.getFilePath())
                    .append("|").append(nextSong.getTitle())
                    .append("|").append(nextSong.getArtistId())
                    .append("|").append(userId);

            return response.toString();
        } else {
            return "INFO: End of playlist reached";
        }
    }

    /**
     * MODIFICATIONS: Handle manual previous song request.
     */
    private String handlePreviousSong(Integer userId) {
        if (!playbackService.hasActivePlaylist(userId)) {
            return "ERROR: No active playlist";
        }

        Song prevSong = playbackService.getPreviousSong(userId);
        if (prevSong != null) {
            System.out.println("User " + userId + " manually skipped to previous: " + prevSong.getTitle());

            // MODIFICATIONS: Return multi-line response with stream request
            StringBuilder response = new StringBuilder();
            response.append("SUCCESS: Previous song ready\n");
            response.append("STREAM_REQUEST|45001|").append(prevSong.getFilePath())
                    .append("|").append(prevSong.getTitle())
                    .append("|").append(prevSong.getArtistId())
                    .append("|").append(userId);

            return response.toString();
        } else {
            return "INFO: At beginning of playlist";
        }
    }

    /**
     * MODIFICATIONS: New method for automatic transitions when client detects song end.
     * This is the key to fixing the playlist flow!
     */
    private String handleAutoNext(Integer userId) {
        if (!playbackService.hasActivePlaylist(userId)) {
            return "INFO: No active playlist for auto-next";
        }

        System.out.println("Client requested auto-next for user " + userId);

        Song nextSong = playbackService.getNextSong(userId);
        if (nextSong != null) {
            System.out.println("Auto-transition for user " + userId + " to: " + nextSong.getTitle());

            // MODIFICATIONS: Return multi-line response with stream request for auto-next
            StringBuilder response = new StringBuilder();
            response.append("AUTO_NEXT_READY\n");
            response.append("STREAM_REQUEST|45001|").append(nextSong.getFilePath())
                    .append("|").append(nextSong.getTitle())
                    .append("|").append(nextSong.getArtistId())
                    .append("|").append(userId);

            return response.toString();
        } else {
            playbackService.stopPlayback(userId);
            System.out.println("Playlist completed for user " + userId);
            return "PLAYLIST_COMPLETE";
        }
    }

    /**
     * MODIFICATIONS: Get current playlist status.
     */
    private String handlePlaylistStatus(Integer userId) {
        if (!playbackService.hasActivePlaylist(userId)) {
            return "STATUS: No active playlist";
        }

        Playlist currentPlaylist = playbackService.getCurrentPlaylist(userId);
        Song currentSong = playbackService.getCurrentSong(userId);
        String playbackMode = playbackService.getPlaybackMode(userId);

        StringBuilder response = new StringBuilder();
        response.append("PLAYLIST_STATUS:\n");

        if (currentPlaylist != null) {
            response.append("  Playlist: ").append(currentPlaylist.getName()).append("\n");
            response.append("  Songs: ").append(currentPlaylist.getSongs().size()).append("\n");
        }
        if (currentSong != null) {
            response.append("  Current: ").append(currentSong.getTitle()).append("\n");
        }
        response.append("  Mode: ").append(playbackMode != null ? playbackMode : "Sequential");

        return response.toString();
    }

    /**
     * MODIFICATIONS: Get current song info.
     */
    private String handleCurrentSong(Integer userId) {
        Song currentSong = playbackService.getCurrentSong(userId);
        if (currentSong != null) {
            StringBuilder response = new StringBuilder();
            response.append("CURRENT_SONG:\n");
            response.append("  Title: ").append(currentSong.getTitle()).append("\n");
            response.append("  Artist ID: ").append(currentSong.getArtistId()).append("\n");
            response.append("  Duration: ").append(currentSong.getDurationSeconds()).append("s");
            return response.toString();
        } else {
            return "INFO: No current song";
        }
    }

    /**
     * MODIFICATIONS: Preview next song without advancing.
     */
    private String handleGetNextSong(Integer userId) {
        Song nextSong = playbackService.peekNextSong(userId);
        if (nextSong != null) {
            StringBuilder response = new StringBuilder();
            response.append("NEXT_SONG_INFO:\n");
            response.append("  Title: ").append(nextSong.getTitle()).append("\n");
            response.append("  Artist ID: ").append(nextSong.getArtistId());
            return response.toString();
        } else {
            return "INFO: No next song available";
        }
    }
}