package spotifyServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced MusicStreamer with server-side playback control.
 * Integrates with your CommandContext system for authentication.
 */
public class MusicStreamer {
    // Track active streamers by user ID - leverages your existing auth system
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

    /**
     * Gets the active streamer for a user ID.
     * This integrates with your CommandContext.getCurrentUserId()
     */
    public static MusicStreamer getStreamerForUser(Integer userId) {
        if (userId == null) {
            return null;
        }
        return activeStreamersByUserId.get(userId);
    }

    /**
     * Registers this streamer with a user ID from your CommandContext system.
     */
    public void registerForUser(Integer userId) {
        this.userId = userId;
        if (userId != null) {
            activeStreamersByUserId.put(userId, this);
            System.out.println("Registered streamer for user ID: " + userId);
        }
    }

    /**
     * Streams audio file with user context integration.
     * The userId comes from your existing CommandContext system.
     */
    public boolean streamAudioFile(String filePath, Socket streamingSocket, Integer userId) {
        File file = new File(filePath);

        if (!file.exists() || !file.canRead()) {
            System.err.println("Error: File not accessible: " + filePath);
            sendErrorToClient(streamingSocket, "File not found: " + filePath);
            return false;
        }

        this.currentFilePath = filePath;
        this.streamingSocket = streamingSocket;

        // Register with your existing user ID system
        registerForUser(userId);

        try {
            audioFile = new RandomAccessFile(file, "r");
            OutputStream clientOut = streamingSocket.getOutputStream();

            PrintWriter textOut = new PrintWriter(new OutputStreamWriter(clientOut), true);
            textOut.println("STREAMING_START|" + file.getName());
            System.out.println("Started streaming for user " + userId + ": " + file.getName());

            Thread.sleep(200);

            return streamWithControl(clientOut, file.length());

        } catch (Exception e) {
            System.err.println("Streaming error: " + e.getMessage());
            return false;
        } finally {
            cleanup();
        }
    }

    // The streaming control logic with pause/resume support
    private boolean streamWithControl(OutputStream clientOut, long fileSize) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = audioFile.read(buffer)) != -1) {
            // Check for pause
            waitWhilePaused();

            // Check for stop
            if (stopRequested.get()) {
                System.out.println("Streaming stopped for user ID: " + userId);
                return false;
            }

            // Stream to client
            clientOut.write(buffer, 0, bytesRead);
            clientOut.flush();
            bytesStreamed.addAndGet(bytesRead);
        }

        System.out.println("Finished streaming file: " + currentFilePath + " to user ID: " + userId);
        return true;
    }

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

    // Control methods
    public synchronized void pause() {
        if (!isPaused.get()) {
            isPaused.set(true);
            System.out.println("PAUSE requested for user: " + userId);
        }
    }

    public synchronized void resume() {
        if (isPaused.get()) {
            isPaused.set(false);
            System.out.println("RESUME requested for user: " + userId);
        }
    }

    public synchronized void stopStreaming() {
        stopRequested.set(true);
        isPaused.set(false);
        System.out.println("STOP requested for user: " + userId);
    }

    // State query methods
    public boolean isPaused() { return isPaused.get(); }
    public boolean isStopped() { return stopRequested.get(); }
    public long getBytesStreamed() { return bytesStreamed.get(); }
    public String getCurrentFile() { return currentFilePath; }
    public Integer getUserId() { return userId; }

    private void sendErrorToClient(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("ERROR|" + message);
        } catch (IOException e) {
            // Ignore error handling errors
        }
    }

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