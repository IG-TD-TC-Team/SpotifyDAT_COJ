package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;
/**
 * Concrete implementation of SubscriptionPlan for premium users.
 * The JsonTypeName annotation works with the JsonTypeInfo in the interface
 * to enable proper polymorphic serialization/deserialization.
 */
@JsonTypeName("PremiumSubscription")
public class PremiumSubscription extends AbstractSubscriptionPlan {

    public PremiumSubscription() {
        super(SubscriptionType.PREMIUM);
    }

    @Override
    public boolean hasAds() {
        return false;
    }

    @Override
    public boolean skipTracks() {
        return true;
    }

    @Override
    public boolean canListenOffline() {
        return true;
    }

    @Override
    public boolean downloadSongs() {
        return true;
    }

    @Override
    public boolean shuffleOnly() {
        return false;
    }

    @Override
    public int getMaxQuality() {
        return 320; // Higher quality (kbps)
    }

    @Override
    public int getDeviceLimit() {
        return 5; // Premium gets more devices
    }
}