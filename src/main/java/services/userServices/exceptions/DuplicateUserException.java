package services.userServices.exceptions;

/**
 * Exception thrown when a duplicate user is detected.
 */
public class DuplicateUserException extends UserServiceException {

    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String field, String value) {
        super("A user with " + field + " '" + value + "' already exists");
    }
}
