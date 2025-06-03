package user.security;

/**
 * Interface defining the contract for secure password hashing operations.
 *
 * This interface abstracts password hashing algorithms, allowing the application
 * to use different implementations without changing client code. It follows the
 * Strategy pattern by encapsulating interchangeable hashing algorithms.
 *
 * Implementations of this interface should use strong cryptographic hashing algorithms
 * with appropriate security practices such as salting and work factors where applicable.
 */
public interface PasswordHasher {
    /**
     * Hashes a password using a simple algorithm without salt.
     *
     * This method is primarily for backward compatibility or simpler use cases
     * where salt management is handled externally. For new password storage,
     * {@link #hashWithSalt(String)} is strongly recommended.
     *
     * @param password The plaintext password to hash
     * @return A string containing the hashed password
     * @throws RuntimeException If the underlying hashing algorithm is unavailable
     */
    String hash(String password);

    /**
     * Hashes a password with a randomly generated salt for improved security.
     *
     * This method generates a random salt, combines it with the password,
     * and produces a hash. The salt is typically stored with the hash to allow
     * for verification later. This approach prevents identical passwords from
     * producing the same hash, mitigating dictionary and rainbow table attacks.
     *
     * @param password The plaintext password to hash
     * @return A string containing both the salt and hashed password in an implementation-defined format
     * @throws RuntimeException If the underlying hashing algorithm is unavailable
     */
    String hashWithSalt(String password);

    /**
     * Verifies that a plaintext password matches a previously hashed password.
     *
     * This method extracts the salt (if present) from the stored hash,
     * applies the same hashing algorithm to the provided plaintext password,
     * and compares the result with the stored hash.
     *
     * Implementations must ensure this operation is performed in constant time
     * to prevent timing attacks that could leak information about the password.
     *
     * @param rawPassword The plaintext password to verify
     * @param hashedPassword The previously hashed password (may include salt)
     * @return true if the password matches the hash, false otherwise
     */
    boolean verify(String rawPassword, String hashedPassword);
}
