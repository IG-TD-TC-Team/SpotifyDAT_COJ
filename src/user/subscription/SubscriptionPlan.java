package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Strategy interface for defining different subscription plans.
 * The JsonTypeInfo and JsonSubTypes annotations enable Jackson to properly
 * serialize and deserialize implementations of this interface.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FreeSubscription.class, name = "FreePlan"),
        @JsonSubTypes.Type(value = PremiumSubscription.class, name = "PremiumPlan"),
        @JsonSubTypes.Type(value = FamilySubscription.class, name = "FamilyPlan"),
        @JsonSubTypes.Type(value = StudentSubscription.class, name = "StudentPlan")
})
public interface SubscriptionPlan {
    boolean hasAds();
    boolean skipTracks(); //could be a max skip tracks
    boolean canListenOffline();
    boolean downloadSongs();
    boolean shuffleOnly();
    int getMaxQuality();
    int getDeviceLimit();
    SubscriptionType getType();
}
