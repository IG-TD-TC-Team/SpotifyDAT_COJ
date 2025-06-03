package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;
/**
 * Concrete implementation of SubscriptionPlan for premium users.
 * The JsonTypeName annotation works with the JsonTypeInfo in the interface
 * to enable proper polymorphic serialization/deserialization.
 */
@JsonTypeName("PremiumSubscription")
public class PremiumSubscription extends AbstractSubscriptionPlan {

    /**
     * Constructs a new premium subscription plan.
     *
     * Initializes the subscription with the PREMIUM type and all features
     * associated with the standard paid individual subscription tier.
     */
    public PremiumSubscription() {
        super(SubscriptionType.PREMIUM);
    }

    /**
     * Determines whether this subscription plan includes advertisements.
     *
     * Premium subscribers enjoy an ad-free listening experience, which is
     * one of the key benefits compared to the free tier.
     *
     * @return {@code false} as the premium plan provides an ad-free experience
     */
    @Override
    public boolean hasAds() {
        return false;
    }

    /**
     * Determines whether this subscription plan allows unlimited track skipping.
     *
     * Premium subscribers can skip tracks without limitations, providing
     * greater control over their listening experience.
     *
     * @return {@code true} as the premium plan allows unlimited track skipping
     */
    @Override
    public boolean skipTracks() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows offline listening.
     *
     * Premium subscribers can listen to music without an internet connection,
     * allowing them to enjoy content in areas with poor connectivity or
     * to save on mobile data usage.
     *
     * @return {@code true} as the premium plan supports offline listening
     */
    @Override
    public boolean canListenOffline() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows downloading songs for offline use.
     *
     * Premium subscribers can download songs to their devices for offline playback,
     * which complements the offline listening capability.
     *
     * @return {@code true} as the premium plan supports downloading songs
     */
    @Override
    public boolean downloadSongs() {
        return true;
    }

    /**
     * Determines whether this subscription plan restricts playback to shuffle mode only.
     *
     * Premium subscribers have full control over playback order, including
     * the ability to play albums, playlists, and songs in their intended sequence.
     *
     * @return {@code false} as the premium plan does not restrict to shuffle-only mode
     */
    @Override
    public boolean shuffleOnly() {
        return false;
    }

    /**
     * Returns the maximum audio quality available for this subscription plan.
     *
     * Premium subscribers have access to high-quality audio streaming at 320 kbps,
     * providing a superior listening experience for users with good headphones or
     * audio equipment.
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
     * Premium subscribers can use their account on up to 5 devices, allowing for
     * flexibility across personal devices such as smartphones, tablets, computers,
     * and dedicated music players.
     *
     * This limit is higher than the free tier (1 device) but lower than the family
     * plan (6 devices), positioning it as suitable for individual users with multiple
     * devices.
     *
     * @return {@code 5} devices, appropriate for individual users with multiple devices
     */
    @Override
    public int getDeviceLimit() {
        return 5; // Premium gets more devices
    }
}