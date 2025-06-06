package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Implementation of a discounted subscription plan specifically for students.
 *
 * The student subscription offers a balance between premium features and affordability,
 * providing most premium benefits at a reduced price point for eligible students.
 *
 * The {@code @JsonTypeName} annotation supports polymorphic serialization and deserialization
 * with Jackson, ensuring that when subscription objects are persisted to or loaded from
 * storage, the correct implementation type is maintained.
 *
 * @see AbstractSubscriptionPlan
 * @see SubscriptionPlan
 * @see SubscriptionType
 * @see PremiumSubscription
 * @see FreeSubscription
 */
@JsonTypeName("StudentSubscription")
public class StudentSubscription extends AbstractSubscriptionPlan {
    /**
     * Constructs a new student subscription plan.
     *
     * Initializes the subscription with the STUDENT type and a feature set
     * that provides premium benefits tailored for student users.
     */
    public StudentSubscription() {
        super(SubscriptionType.STUDENT);
    }

    /**
     * Determines whether this subscription plan includes advertisements.
     *
     * Like premium tiers, student subscriptions offer an ad-free listening
     * experience, enhancing focus during study sessions and providing
     * uninterrupted enjoyment.
     *
     * @return {@code false} as the student plan provides an ad-free experience
     */
    @Override
    public boolean hasAds() {
        return false;
    }

    /**
     * Determines whether this subscription plan allows unlimited track skipping.
     *
     * Student subscribers can skip tracks without limitations, giving them
     * the same flexibility as premium users to customize their listening
     * experience on-the-fly.
     *
     * @return {@code true} as the student plan allows unlimited track skipping
     */
    @Override
    public boolean skipTracks() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows offline listening.
     *
     * Student subscribers can listen to music without an internet connection,
     * which is particularly valuable for studying in areas with poor connectivity
     * or for conserving data usage on campus.
     *
     * @return {@code true} as the student plan supports offline listening
     */
    @Override
    public boolean canListenOffline() {
        return true;
    }

    /**
     * Determines whether this subscription plan allows downloading songs for offline use.
     *
     * Student subscribers can download songs to their devices for offline playback,
     * allowing them to build study playlists or entertainment libraries that don't
     * require constant internet connectivity.
     *
     * @return {@code true} as the student plan supports downloading songs
     */
    @Override
    public boolean downloadSongs() {
        return true;
    }

    /**
     * Determines whether this subscription plan restricts playback to shuffle mode only.
     *
     * Student subscribers have full control over playback order, including
     * the ability to create and play sequential study playlists or listen to
     * albums in their intended order.
     *
     * @return {@code false} as the student plan does not restrict to shuffle-only mode
     */
    @Override
    public boolean shuffleOnly() {
        return false;
    }

    /**
     * Returns the maximum audio quality available for this subscription plan.
     *
     * Student subscribers have access to high-quality audio streaming at 256 kbps,
     * which represents a middle ground between the free tier (128 kbps) and the
     * premium tier (320 kbps). This quality level provides excellent sound for
     * most listening scenarios while helping to balance bandwidth usage for
     * students who may have limited data plans.
     *
     * @return {@code 256} kbps, representing medium-high quality audio streaming
     */
    @Override
    public int getMaxQuality() {
        return 256; // Medium-high quality (kbps)
    }

    /**
     * Returns the maximum number of devices that can simultaneously use this subscription.
     *
     * Student subscribers can use their account on up to 3 devices simultaneously,
     * which accommodates typical student usage patterns across a smartphone, laptop,
     * and perhaps a tablet or desktop computer.
     *
     * This limit positions the student plan between the free tier (1 device) and
     * the premium tier (5 devices), reflecting its intermediate pricing and
     * target audience.
     *
     * @return {@code 3} devices, appropriate for typical student usage patterns
     */
    @Override
    public int getDeviceLimit() {
        return 3; // Student gets moderate device limit
    }
}