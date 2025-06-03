package user.subscription;

/**
 * Abstract base implementation of the SubscriptionPlan interface.
 *
 * All subscription plans must extend this class and implement the remaining
 * abstract methods defined in the SubscriptionPlan interface. This ensures
 * consistent behavior across different subscription types while allowing
 * for plan-specific customizations.
 *
 * @see SubscriptionPlan
 * @see SubscriptionType
 */
public abstract class AbstractSubscriptionPlan implements SubscriptionPlan {
    /**
     * The type of subscription plan.
     * This is stored as an immutable field to ensure type consistency
     * throughout the lifecycle of the subscription plan object.
     */
    private final SubscriptionType type;

    /**
     * Constructs a new subscription plan with the specified type.
     *
     * This constructor is protected to ensure it can only be called
     * by concrete subclasses within the subscription package.
     *
     * @param type The type of subscription plan, which determines its
     *             category and general feature set
     */
    protected AbstractSubscriptionPlan(SubscriptionType type) {
        this.type = type;
    }

    /**
     * Returns the subscription plan type.
     *
     * This implementation is final to ensure that the subscription type
     * remains consistent with how the plan was initialized.
     *
     * @return The subscription type (e.g., FREE, PREMIUM, FAMILY, STUDENT)
     */
    @Override
    public SubscriptionType getType() {
        return type;
    }

    /**
     * Returns the default device limit for this subscription plan.
     *
     * This provides a common default implementation that can be overridden
     * by specific subscription plans that offer different device limits.
     * By default, all subscription plans allow at least 1 device.
     *
     * Subclasses should override this method if they offer a different
     * device limit.
     *
     * @return The number of devices allowed for this subscription plan,
     *         defaults to 1
     */
    // Common default implementation
    @Override
    public int getDeviceLimit() {
        return 1;
    }
}
