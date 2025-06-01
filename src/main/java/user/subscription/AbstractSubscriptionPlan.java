package user.subscription;

public abstract class AbstractSubscriptionPlan implements SubscriptionPlan {
    private final SubscriptionType type;

    protected AbstractSubscriptionPlan(SubscriptionType type) {
        this.type = type;
    }

    @Override
    public SubscriptionType getType() {
        return type;
    }

    // Common default implementation
    @Override
    public int getDeviceLimit() {
        return 1;
    }
}
