package spotifyServer.commandProcessor;

import spotifyServer.MusicStreamer;

/**
 * Playback control processor that integrates seamlessly with your existing
 * CommandContext system. No duplication of session management!
 */
public class PlaybackControlCommandProcessor extends AbstractProcessor {

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
            default:
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