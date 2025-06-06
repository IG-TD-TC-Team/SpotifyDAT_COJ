package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Implementation of a family subscription plan offering premium features for multiple users.
 *
 * The family subscription is a premium-tier plan designed for household sharing,
 * allowing up to 6 devices to use premium features simultaneously.
 *
 * This class is annotated with {@code @JsonTypeName} to support polymorphic serialization
 * and deserialization with Jackson, allowing it to be properly identified during JSON
 * processing within the application's persistence layer.
 *
 * @see AbstractSubscriptionPlan
 * @see SubscriptionPlan
 * @see SubscriptionType
 */
@JsonTypeName("FamilySubscription")
public class FamilySubscription extends AbstractSubscriptionPlan {

    /**
     * Constructs a new family subscription plan.
     *
     * Initializes the plan with the FAMILY subscription type and all the
     * premium features associated with a family subscription.
     */
    public FamilySubscription() {
        super(SubscriptionType.FAMILY);
    }

    /**
     * Determines whether this subscription plan includes advertisements.
     *
     * @return {@code false} as the family plan provides an ad-free experience
     */
    @Override
    public boolean hasAds() {
        return false;
    }

    /**
     * Determines whether this subscription plan allows unlimited track skipping.
     *
     * @return {@code true} as the family plan allows unlimited track skipping
     */
    @Override
    public boolean skipTracks() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows offline listening.
     *
     * @return {@code true} as the family plan supports offline listening
     */
    @Override
    public boolean canListenOffline() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows downloading songs for offline use.
     *
     * @return {@code true} as the family plan supports downloading songs
     */
    @Override
    public boolean downloadSongs() {
        return true;
    }

    /**
     * Determines whether this subscription plan restricts playback to shuffle mode only.
     *
     * @return {@code false} as the family plan allows full playback control, not just shuffle
     */
    @Override
    public boolean shuffleOnly() {
        return false;
    }

    /**
     * Returns the maximum audio quality available for this subscription plan.
     *
     * @return {@code 320} kbps, representing high-quality audio streaming
     */
    @Override
    public int getMaxQuality() {
        return 320; // Higher quality (kbps)
    }

    /**
     * Returns the maximum number of devices that can simultaneously use this subscription.
     *
     * The family plan supports more devices than other plans to accommodate
     * multiple household members.
     *
     * @return {@code 6} devices, allowing for shared family usage
     */
    @Override
    public int getDeviceLimit() {
        return 6; // Family plan gets more devices
    }
}
