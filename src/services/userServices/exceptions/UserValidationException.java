package services.userServices.exceptions;

/**
 * Exception thrown when there are validation issues with user data.
 */
public class UserValidationException extends UserServiceException {

    public UserValidationException(String message) {
        super(message);
    }

    public UserValidationException(String field, String issue) {
        super("Validation error for field '" + field + "': " + issue);
    }
}
