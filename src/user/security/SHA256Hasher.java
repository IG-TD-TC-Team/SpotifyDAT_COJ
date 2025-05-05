package user.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SHA256Hasher implements PasswordHasher {
    private static final int SALT_LENGTH = 16;

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