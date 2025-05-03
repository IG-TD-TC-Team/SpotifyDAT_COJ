package services.userServices.exceptions;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends UserServiceException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException() {
        super("Authentication failed: Invalid username or password");
    }
}
