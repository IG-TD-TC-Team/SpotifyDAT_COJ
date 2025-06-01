package services.userServices.exceptions;

/**
 * Exception thrown for subscription-related issues.
 */
public class SubscriptionException extends UserServiceException {

    public SubscriptionException(String message) {
        super(message);
    }

    public SubscriptionException(String feature, String requiredPlan) {
        super("Access to '" + feature + "' requires a " + requiredPlan + " subscription");
    }
}
