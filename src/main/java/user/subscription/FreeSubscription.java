package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Concrete implementation of SubscriptionPlan for free users.
 * The JsonTypeName annotation works with the JsonTypeInfo in the interface
 * to enable proper polymorphic serialization/deserialization.
 */
@JsonTypeName("FreeSubscription")
public class FreeSubscription extends AbstractSubscriptionPlan {

    /**
     * Constructs a new free subscription plan.
     *
     * Initializes the plan with the FREE subscription type and all the
     * limitations associated with the free tier.
     */
    public FreeSubscription() {
        super(SubscriptionType.FREE);
    }

    /**
     * Determines whether this subscription plan includes advertisements.
     *
     * Free tier users will experience periodic audio advertisements between songs
     * as part of the listening experience.
     *
     * @return {@code true} as the free plan includes advertisements
     */
    @Override
    public boolean hasAds() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows track skipping.
     *
     * Free tier users have limited or no ability to skip tracks, encouraging
     * users to upgrade to premium tiers for more control over their listening experience.
     *
     * @return {@code false} as the free plan restricts track skipping
     */
    @Override
    public boolean skipTracks() {
        return false;
    }

    /**
     * Determines whether this subscription plan allows offline listening.
     *
     * Free tier users must be connected to the internet to stream music,
     * as offline listening is a premium feature.
     *
     * @return {@code false} as the free plan does not support offline listening
     */
    @Override
    public boolean canListenOffline() {
        return false;
    }

    /**
     * Determines whether this subscription plan allows downloading songs for offline use.
     *
     * Free tier users cannot download songs for offline playback,
     * requiring an internet connection for all listening.
     *
     * @return {@code false} as the free plan does not support downloading songs
     */
    @Override
    public boolean downloadSongs() {
        return false;
    }

    /**
     * Determines whether this subscription plan restricts playback to shuffle mode only.
     *
     * Free tier users can only play music in shuffle mode, without the ability
     * to select specific songs or create custom play order sequences.
     *
     * @return {@code true} as the free plan restricts playback to shuffle mode only
     */
    @Override
    public boolean shuffleOnly() {
        return true;
    }

    /**
     * Returns the maximum audio quality available for this subscription plan.
     *
     * Free tier users receive standard audio quality, which balances data usage
     * and sound quality. Premium tiers offer higher quality options.
     *
     * @return {@code 128} kbps, representing standard audio quality
     */
    @Override
    public int getMaxQuality() {
        return 128;
    }
}
