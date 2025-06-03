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
    /**
     * Determines whether the subscription includes advertisements.
     *
     * @return {@code true} if the subscription includes advertisements, {@code false} otherwise
     */
    boolean hasAds();

    /**
     * Determines whether the subscription allows unlimited track skipping.
     *
     * @return {@code true} if the subscription allows unlimited track skipping, {@code false} otherwise
     */
    boolean skipTracks(); //could be a max skip tracks

    /**
     * Determines whether the subscription allows listening without an internet connection.
     *
     * @return {@code true} if the subscription supports offline listening, {@code false} otherwise
     */
    boolean canListenOffline();

    /**
     * Determines whether the subscription allows downloading songs for offline use.
     *
     *
     * @return {@code true} if the subscription allows downloading songs, {@code false} otherwise
     */
    boolean downloadSongs();

    /**
     * Determines whether the subscription restricts playback to shuffle mode only.
     *
     *
     * @return {@code true} if the subscription restricts to shuffle-only mode, {@code false} if it allows any playback order
     */
    boolean shuffleOnly();

    /**
     * Returns the maximum audio quality available for this subscription in kbps.
     *
     *
     * @return The maximum audio quality in kbps (kilobits per second)
     */
    int getMaxQuality();

    /**
     * Returns the maximum number of devices that can simultaneously use this subscription.
     *
     *
     * @return The maximum number of devices allowed for this subscription
     */
    int getDeviceLimit();

    /**
     * Returns the subscription type.
     *
     *
     * @return The subscription type (FREE, PREMIUM, FAMILY, or STUDENT)
     * @see SubscriptionType
     */
    SubscriptionType getType();
}
