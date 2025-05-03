package services.userServices.exceptions;

/**
 * Exception thrown when a user cannot be found.
 */
public class UserNotFoundException extends UserServiceException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(int userId) {
        super("User with ID " + userId + " not found");
    }

    public UserNotFoundException(String identifierType, String identifierValue) {
        super("User with " + identifierType + " '" + identifierValue + "' not found");
    }
}
