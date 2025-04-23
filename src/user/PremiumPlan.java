package user;

import com.fasterxml.jackson.annotation.JsonTypeName;
/**
 * Concrete implementation of SubscriptionPlan for premium users.
 * The JsonTypeName annotation works with the JsonTypeInfo in the interface
 * to enable proper polymorphic serialization/deserialization.
 */
@JsonTypeName("PremiumPlan")
public class PremiumPlan implements SubscriptionPlan {
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
}
