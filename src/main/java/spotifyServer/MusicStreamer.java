package spotifyServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MODIFICATIONS: Fixed MusicStreamer to handle PURE AUDIO STREAMING ONLY.
 * Removed all text control messages from audio stream to prevent MP3 decoder corruption.
 * Now streams single songs on demand - playlist control handled via command port.
 */
public class MusicStreamer {
    // Track active streamers by user ID
    private static final ConcurrentHashMap<Integer, MusicStreamer> activeStreamersByUserId = new ConcurrentHashMap<>();

    // Playback state flags
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicLong bytesStreamed = new AtomicLong(0);

    // Stream resources
    private Integer userId;
    private String currentFilePath;
    private Socket streamingSocket;
    private RandomAccessFile audioFile;
    private OutputStream clientOut;

    // MODIFICATIONS: Removed PrintWriter textOut - NO MORE TEXT ON AUDIO STREAM!

    public MusicStreamer() {
        // MODIFICATIONS: Removed PlaybackService dependency - not needed for pure audio streaming
    }

    /**
     * Gets the active streamer for a user ID.
     */
    public static MusicStreamer getStreamerForUser(Integer userId) {
        if (userId == null) {
            return null;
        }
        return activeStreamersByUserId.get(userId);
    }

    /**
     * Registers this streamer with a user ID.
     */
    public void registerForUser(Integer userId) {
        this.userId = userId;
        if (userId != null) {
            activeStreamersByUserId.put(userId, this);
            System.out.println("Registered streamer for user ID: " + userId);
        }
    }

    /**
     * MODIFICATIONS: Simplified to stream SINGLE SONG ONLY with PURE AUDIO DATA.
     * No text messages, no playlist logic - just raw MP3 streaming.
     *
     * @param filePath Path to the audio file
     * @param streamingSocket Socket for streaming
     * @param userId User ID for tracking
     * @return true if streaming started successfully
     */
    public boolean streamAudioFile(String filePath, Socket streamingSocket, Integer userId) {
        this.streamingSocket = streamingSocket;
        this.currentFilePath = filePath;

        // Register with user ID system
        registerForUser(userId);

        try {
            clientOut = streamingSocket.getOutputStream();
            // MODIFICATIONS: NO PrintWriter - only binary output stream

            return streamSingleSong(filePath);

        } catch (Exception e) {
            System.err.println("Streaming error: " + e.getMessage());
            return false;
        } finally {
            cleanup();
        }
    }

    /**
     * MODIFICATIONS: Pure audio streaming with no text control messages.
     */
    private boolean streamSingleSong(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            System.err.println("Error: File not accessible: " + filePath);
            // MODIFICATIONS: No error messages sent to client - just fail silently
            return false;
        }

        this.currentFilePath = filePath;
        // MODIFICATIONS: Removed text messages - client will detect stream start/end via HTTP
        System.out.println("Started streaming single song for user " + userId + ": " + file.getName());

        return streamFileContent(file);
    }

    /**
     * MODIFICATIONS: Pure audio content streaming - no control messages mixed in.
     */
    private boolean streamFileContent(File file) throws IOException {
        try (RandomAccessFile audioFile = new RandomAccessFile(file, "r")) {
            this.audioFile = audioFile;

            byte[] buffer = new byte[4096];
            int bytesRead;
            long fileSize = file.length();
            long totalBytesRead = 0;

            while ((bytesRead = audioFile.read(buffer)) != -1) {
                // Check for pause
                waitWhilePaused();

                // Check for stop
                if (stopRequested.get()) {
                    System.out.println("Streaming stopped for user ID: " + userId);
                    return false;
                }

                // MODIFICATIONS: ONLY stream raw audio data - no text messages!
                clientOut.write(buffer, 0, bytesRead);
                clientOut.flush();
                bytesStreamed.addAndGet(bytesRead);
                totalBytesRead += bytesRead;

                // Optional: Add a small delay to prevent overwhelming the client
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            System.out.println("Finished streaming file: " + file.getName() + " (" + totalBytesRead + " bytes)");
            // MODIFICATIONS: No "STREAMING_COMPLETE" message - client detects end via audio events
            return true;

        } catch (IOException e) {
            System.err.println("Error streaming file " + file.getName() + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * MODIFICATIONS: Simplified pause - no text messages sent.
     */
    public synchronized void pause() {
        if (!isPaused.get()) {
            isPaused.set(true);
            // MODIFICATIONS: No text message to client - control via command port only
            System.out.println("PAUSE requested for user: " + userId);
        }
    }

    /**
     * MODIFICATIONS: Simplified resume - no text messages sent.
     */
    public synchronized void resume() {
        if (isPaused.get()) {
            isPaused.set(false);
            // MODIFICATIONS: No text message to client - control via command port only
            System.out.println("RESUME requested for user: " + userId);
        }
    }

    /**
     * MODIFICATIONS: Simplified stop - no text messages sent.
     */
    public synchronized void stopStreaming() {
        stopRequested.set(true);
        isPaused.set(false);

        // MODIFICATIONS: No text message to client - control via command port only
        System.out.println("STOP requested for user: " + userId);
    }

    /**
     * MODIFICATIONS: Removed skip methods - playlist navigation handled via command port.
     */

    /**
     * Waits while streaming is paused.
     */
    private void waitWhilePaused() {
        if (isPaused.get()) {
            System.out.println("User " + userId + " stream paused at " + bytesStreamed.get() + " bytes");
            while (isPaused.get() && !stopRequested.get()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // State query methods
    public boolean isPaused() { return isPaused.get(); }
    public boolean isStopped() { return stopRequested.get(); }
    public long getBytesStreamed() { return bytesStreamed.get(); }
    public String getCurrentFile() { return currentFilePath; }
    public Integer getUserId() { return userId; }

    /**
     * Performs resource cleanup after streaming is complete or terminated.
     */
    private void cleanup() {
        try {
            if (audioFile != null) {
                audioFile.close();
            }
            if (streamingSocket != null && !streamingSocket.isClosed()) {
                streamingSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cleaning up resources: " + e.getMessage());
        } finally {
            if (userId != null) {
                activeStreamersByUserId.remove(userId);
            }
        }
    }
}