package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Concrete implementation of SubscriptionPlan for free users.
 * The JsonTypeName annotation works with the JsonTypeInfo in the interface
 * to enable proper polymorphic serialization/deserialization.
 */
@JsonTypeName("FreeSubscription")
public class FreeSubscription extends AbstractSubscriptionPlan {

    public FreeSubscription() {
        super(SubscriptionType.FREE);
    }

    @Override
    public boolean hasAds() {
        return true;
    }

    @Override
    public boolean skipTracks() {
        return false;
    }

    @Override
    public boolean canListenOffline() {
        return false;
    }

    @Override
    public boolean downloadSongs() {
        return false;
    }

    @Override
    public boolean shuffleOnly() {
        return true;
    }

    @Override
    public int getMaxQuality() {
        return 128;
    }
}
