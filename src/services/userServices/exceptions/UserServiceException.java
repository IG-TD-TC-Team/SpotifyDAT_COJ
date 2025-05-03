package services.userServices.exceptions;


/**
 * Base exception class for all user service related exceptions.
 * Provides a common type for all user service exceptions.
 */
public abstract class UserServiceException extends RuntimeException {

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
