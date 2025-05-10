package spotifyServer;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class responsible for streaming MP3 files to clients using the JLayer library.
 * This implementation respects MP3 frame boundaries for proper streaming.
 * Simplified to focus only on audio streaming without metadata handling.
 */
public class MusicStreamer {

    // Size of buffer used when looking for MP3 frame headers
    private static final int BUFFER_SIZE = 4096;

    // Flag to allow stopping the stream
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);

    /**
     * Streams an MP3 file to a client with proper frame handling.
     *
     * @param filePath the path to the MP3 file
     * @param clientSocket the client socket to stream to
     * @return true if streaming was successful, false otherwise
     */
    public boolean streamAudioFile(String filePath, Socket clientSocket) {
        File file = new File(filePath);

        // Check if file exists and is readable
        if (!file.exists() || !file.canRead()) {
            System.err.println("Error: File does not exist or is not readable: " + filePath);
            return false;
        }

        try (
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            System.out.println("Starting to stream MP3 file: " + filePath);

            // Skip ID3 tags if present (to start streaming from actual audio data)
            long startPosition = skipID3Tags(bufferedInputStream);

            // Reset file position after scanning for ID3 tags (not needed with this method)
            // bufferedInputStream is already positioned correctly after skipID3Tags

            // Create a Bitstream to allow frame-by-frame processing
            Bitstream bitstream = new Bitstream(bufferedInputStream);

            // Stream file frame by frame
            streamMP3Frames(bitstream, outputStream);

            outputStream.flush();
            System.out.println("Completed streaming file: " + filePath);
            return true;

        } catch (IOException e) {
            if (stopRequested.get()) {
                System.out.println("Streaming was stopped as requested");
                return true;
            } else {
                System.err.println("Error streaming file: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Stops the current streaming operation.
     */
    public void stopStreaming() {
        stopRequested.set(true);
    }

    /**
     * Skip ID3v2 tags that might be present at the beginning of the MP3 file.
     *
     * @param inputStream the input stream for the MP3 file
     * @return the position after any ID3 tags
     * @throws IOException if an I/O error occurs
     */
    private long skipID3Tags(BufferedInputStream inputStream) throws IOException {
        byte[] headerBuffer = new byte[10];


        inputStream.mark(10);

        if (inputStream.read(headerBuffer, 0, 10) == 10) {
            String id3Header = new String(headerBuffer, 0, 3);

            if (id3Header.equals("ID3")) {
                // ID3v2 tag found, calculate size
                int size = ((headerBuffer[6] & 0x7F) << 21) |
                        ((headerBuffer[7] & 0x7F) << 14) |
                        ((headerBuffer[8] & 0x7F) << 7) |
                        (headerBuffer[9] & 0x7F);



                // Skip over the tag
                inputStream.reset();
                inputStream.skip(size + 10);
                return size + 10;
            }
        }

        // No ID3 tag found or error reading, reset to beginning
        inputStream.reset();
        return 0;
    }

    /**
     * Streams the MP3 file frame by frame to ensure proper streaming.
     *
     * @param bitstream the JLayer Bitstream for the MP3 file
     * @param outputStream the output stream to the client
     * @throws IOException if an I/O error occurs
     */
    private void streamMP3Frames(Bitstream bitstream, OutputStream outputStream) throws IOException {
        try {
            ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream(BUFFER_SIZE);
            long totalBytesStreamed = 0;

            // Process each frame
            Header header;
            while ((header = bitstream.readFrame()) != null && !stopRequested.get()) {
                // Get the frame data
                byte[] frameData = getFrameData(bitstream, header);

                // Send the frame
                outputStream.write(frameData);
                totalBytesStreamed += frameData.length;

                // Log progress periodically
                if (totalBytesStreamed % (BUFFER_SIZE * 64) == 0) {
                    System.out.println("Streaming progress: " + totalBytesStreamed + " bytes");
                }

                // Important: close the current frame
                bitstream.closeFrame();
            }

        } catch (BitstreamException e) {
            throw new IOException("Error processing MP3 frames: " + e.getMessage(), e);
        }
    }

    /**
     * Extract the raw frame data from the bitstream.
     *
     * @param bitstream the JLayer Bitstream for the MP3 file
     * @param header the frame header
     * @return the raw frame data as byte array
     * @throws IOException if an I/O error occurs
     */
    private byte[] getFrameData(Bitstream bitstream, Header header) throws IOException {
        try {
            // Calculate frame size
            int frameSize = header.calculate_framesize();

            // Read frame data
            ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream(frameSize);

            // Write header (4 bytes)
            ByteBuffer headerBytes = ByteBuffer.allocate(4);
            headerBytes.putInt(header.getSyncHeader());
            frameBuffer.write(headerBytes.array());

            // Read rest of frame data (minus 4 header bytes)
            byte[] frameData = new byte[frameSize - 4];

            // Get the file input stream from bitstream (using reflection as it's not directly exposed)
            java.lang.reflect.Field bitstreamField = bitstream.getClass().getDeclaredField("source");
            bitstreamField.setAccessible(true);
            InputStream sourceStream = (InputStream) bitstreamField.get(bitstream);

            // Read the frame data
            int read = sourceStream.read(frameData);
            if (read > 0) {
                frameBuffer.write(frameData, 0, read);
            }

            return frameBuffer.toByteArray();

        } catch (Exception e) {
            throw new IOException("Error extracting frame data: " + e.getMessage(), e);
        }
    }
}