package user.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementation of the PasswordHasher interface using the SHA-256 hashing algorithm.
 *
 * This class provides both simple and salted password hashing functionality, as well as
 * verification of passwords against previously generated hashes. It uses SHA-256, which
 * is a cryptographic hash function that is reasonably secure for password storage when
 * properly implemented with salting.
 *
 */
public class SHA256Hasher implements PasswordHasher {
    /**
     * The length of the random salt in bytes.
     * A 16-byte salt provides a good balance between security and storage efficiency.
     */
    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a password using SHA-256 without salt.
     *
     * This method produces a 64-character hexadecimal string representing the hash.
     * Since this method does not use salt, identical passwords will produce identical
     * hashes, making it vulnerable to rainbow table attacks.
     *
     * @param password The plaintext password to hash
     * @return A 64-character hexadecimal string representing the hash
     * @throws RuntimeException If the SHA-256 algorithm is not available in the
     *         current Java environment
     */
    @Override
    public String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Hashes a password using SHA-256 with a randomly generated salt.
     *
     * The resulting string contains both the salt and hash, allowing for later
     * verification without storing the salt separately.
     *
     * @param password The plaintext password to hash
     * @return A Base64-encoded string containing both the salt and hash
     * @throws RuntimeException If the SHA-256 algorithm is not available
     */
    @Override
    public String hashWithSalt(String password) {
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Hash password with salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] encodedHash = digest.digest(password.getBytes());

            // Combine salt and hash
            byte[] combined = new byte[salt.length + encodedHash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(encodedHash, 0, combined, salt.length, encodedHash.length);

            // Return as base64 string
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies that a plaintext password matches a previously hashed password.
     *
     * The method automatically detects the format and uses the appropriate verification
     * technique. For salted hashes, it extracts the salt from the stored hash, rehashes
     * the password with that salt, and compares the result.
     *
     * @param rawPassword The plaintext password to verify
     * @param hashedPassword The previously hashed password (may include salt)
     * @return true if the password matches the hash, false otherwise
     */
    @Override
    public boolean verify(String rawPassword, String hashedPassword) {
        // For simple hex hashes (no salt)
        if (hashedPassword.length() == 64 && hashedPassword.matches("[0-9a-f]+")) {
            return hash(rawPassword).equals(hashedPassword);
        }

        try {
            // Try to decode from base64
            byte[] combined;
            try {
                combined = Base64.getDecoder().decode(hashedPassword);
            } catch (IllegalArgumentException e) {
                // If it's not base64, try direct comparison as a fallback
                return hash(rawPassword).equals(hashedPassword);
            }

            // Make sure we have enough bytes for salt + hash
            if (combined.length <= SALT_LENGTH) {
                // Not enough data for salt + hash, try direct comparison
                return hash(rawPassword).equals(hashedPassword);
            }

            // Extract salt
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // Hash password with extracted salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] encodedHash = digest.digest(rawPassword.getBytes());

            // Create expected hash for comparison
            byte[] expectedHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, expectedHash, 0, expectedHash.length);

            // Compare hashes
            return MessageDigest.isEqual(encodedHash, expectedHash);
        } catch (NoSuchAlgorithmException e) {
            return false; // Invalid algorithm
        }
    }
}