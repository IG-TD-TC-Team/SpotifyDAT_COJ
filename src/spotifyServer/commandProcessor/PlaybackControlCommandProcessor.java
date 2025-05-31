package spotifyServer.commandProcessor;

import services.playbackServices.PlaybackService;
import songsAndArtists.Song;
import spotifyServer.MusicStreamer;
import spotifyServer.SpotifySocketServer;
import spotifyServer.StreamingServer;

/**
 * Playback control processor that integrates seamlessly with your existing
 * CommandContext system. No duplication of session management!
 */
public class PlaybackControlCommandProcessor extends AbstractProcessor {

    private final PlaybackService playbackService = PlaybackService.getInstance();
    private final StreamingServer streamingServer;

    public PlaybackControlCommandProcessor() {
        this.streamingServer = StreamingServer.getInstance();
    }

    @Override
    public String processCommand(String command) {
        // Check authentication using your existing system
        if (!isAuthenticated()) {
            return "ERROR: You must be logged in to control playback";
        }

        String lowerCommand = command.toLowerCase().trim();

        switch (lowerCommand) {
            case "pause":
                return handlePause();
            case "resume":
                return handleResume();
            case "stop":
                return handleStop();
            case "status":
                return handleStatus();
            case "next":
                return handleNext();
            case "previous":
                return handlePrevious();
            default:
                if (lowerCommand.startsWith("shuffle ")) {
                    return handleShuffle(command);
                } else if (lowerCommand.startsWith("repeat ")) {
                    return handleRepeat(command);
                }
                return handleNext(command);
        }
    }

    /**
     * Uses your existing CommandContext to get the current user ID.
     * No duplication of session management logic!
     */
    private String handlePause() {
        try {
            // Get user ID from your existing CommandContext system
            Integer userId = getCurrentUserId();

            if (userId == null) {
                return "ERROR: Could not determine user identity";
            }

            // Find the streamer using your existing user ID
            MusicStreamer streamer = MusicStreamer.getStreamerForUser(userId);

            if (streamer == null) {
                return "ERROR: No active stream found for user " + getCurrentUsername();
            }

            if (streamer.isPaused()) {
                return "Stream is already paused";
            }

            streamer.pause();
            return "SUCCESS: Stream paused at " + formatBytes(streamer.getBytesStreamed());

        } catch (Exception e) {
            return "ERROR: Failed to pause: " + e.getMessage();
        }
    }

    private String handleResume() {
        try {
            Integer userId = getCurrentUserId();
            MusicStreamer streamer = MusicStreamer.getStreamerForUser(userId);

            if (streamer == null) {
                return "ERROR: No active stream found for user " + getCurrentUsername();
            }

            if (!streamer.isPaused()) {
                return "Stream is not paused";
            }

            streamer.resume();
            return "SUCCESS: Stream resumed from " + formatBytes(streamer.getBytesStreamed());

        } catch (Exception e) {
            return "ERROR: Failed to resume: " + e.getMessage();
        }
    }

    private String handleStop() {
        try {
            Integer userId = getCurrentUserId();
            MusicStreamer streamer = MusicStreamer.getStreamerForUser(userId);

            if (streamer == null) {
                return "ERROR: No active stream found for user " + getCurrentUsername();
            }

            String currentFile = streamer.getCurrentFile();
            streamer.stopStreaming();

            return "SUCCESS: Stopped streaming " +
                    (currentFile != null ? currentFile : "current track") +
                    " for " + getCurrentUsername();

        } catch (Exception e) {
            return "ERROR: Failed to stop: " + e.getMessage();
        }
    }

    private String handleStatus() {
        try {
            Integer userId = getCurrentUserId();
            MusicStreamer streamer = MusicStreamer.getStreamerForUser(userId);

            if (streamer == null) {
                return "No active stream for " + getCurrentUsername();
            }

            StringBuilder status = new StringBuilder();
            status.append("Playback Status for ").append(getCurrentUsername()).append(":\n");
            status.append("File: ").append(streamer.getCurrentFile()).append("\n");
            status.append("Bytes streamed: ").append(formatBytes(streamer.getBytesStreamed())).append("\n");
            status.append("State: ");

            if (streamer.isStopped()) {
                status.append("STOPPED");
            } else if (streamer.isPaused()) {
                status.append("PAUSED");
            } else {
                status.append("PLAYING");
            }

            return status.toString();

        } catch (Exception e) {
            return "ERROR: Failed to get status: " + e.getMessage();
        }
    }

    private String handleNext() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return "ERROR: Could not determine user identity";
        }

        // Check if user has an active playlist
        if (!playbackService.hasActivePlaylist(userId)) {
            return "ERROR: No active playlist. Start a playlist first with 'playlist <id>'";
        }

        // Get the next song
        Song nextSong = playbackService.getNextSong(userId);
        if (nextSong == null) {
            return "End of playlist reached";
        }

        // Prepare streaming for the next song
        streamingServer.prepareForStreaming(nextSong);

        // Return streaming instructions to client
        return "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT + "|" +
                nextSong.getFilePath() + "|" + nextSong.getTitle() + "|" +
                nextSong.getArtistId() + "|" + userId;
    }

    private String handlePrevious() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return "ERROR: Could not determine user identity";
        }

        // Check if user has an active playlist
        if (!playbackService.hasActivePlaylist(userId)) {
            return "ERROR: No active playlist. Start a playlist first with 'playlist <id>'";
        }

        // Get the previous song
        Song prevSong = playbackService.getPreviousSong(userId);
        if (prevSong == null) {
            return "Beginning of playlist reached";
        }

        // Prepare streaming for the previous song
        streamingServer.prepareForStreaming(prevSong);

        // Return streaming instructions to client
        return "STREAM_REQUEST|" + SpotifySocketServer.STREAMING_PORT + "|" +
                prevSong.getFilePath() + "|" + prevSong.getTitle() + "|" +
                prevSong.getArtistId() + "|" + userId;
    }

    // Add handlers for shuffle and repeat commands
    private String handleShuffle(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            return "Error: Missing mode. Usage: shuffle on/off";
        }

        String mode = parts[1].toLowerCase();
        Integer userId = getCurrentUserId();

        if (userId == null) {
            return "ERROR: Could not determine user identity";
        }

        if (!playbackService.hasActivePlaylist(userId)) {
            return "ERROR: No active playlist. Start a playlist first with 'playlist <id>'";
        }

        if ("on".equals(mode)) {
            playbackService.setShuffleMode(userId);
            return "SUCCESS: Shuffle mode enabled";
        } else if ("off".equals(mode)) {
            playbackService.setSequentialMode(userId);
            return "SUCCESS: Shuffle mode disabled";
        } else {
            return "ERROR: Invalid mode. Use 'on' or 'off'";
        }
    }

    private String handleRepeat(String command) {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            return "Error: Missing mode. Usage: repeat none/one/all";
        }

        String mode = parts[1].toLowerCase();
        Integer userId = getCurrentUserId();

        if (userId == null) {
            return "ERROR: Could not determine user identity";
        }

        if (!playbackService.hasActivePlaylist(userId)) {
            return "ERROR: No active playlist. Start a playlist first with 'playlist <id>'";
        }

        switch (mode) {
            case "none":
                playbackService.setSequentialMode(userId);
                return "SUCCESS: Repeat mode set to none";
            case "one":
                playbackService.setRepeatOneMode(userId);
                return "SUCCESS: Repeat mode set to repeat current song";
            case "all":
                playbackService.setRepeatAllMode(userId);
                return "SUCCESS: Repeat mode set to repeat all songs";
            default:
                return "ERROR: Invalid mode. Use 'none', 'one', or 'all'";
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        } else {
            return (bytes / (1024 * 1024)) + " MB";
        }
    }
}