package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("StudentSubscription")
public class StudentSubscription extends AbstractSubscriptionPlan {

    public StudentSubscription() {
        super(SubscriptionType.STUDENT);
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
        return 256; // Medium-high quality (kbps)
    }

    @Override
    public int getDeviceLimit() {
        return 3; // Student gets moderate device limit
    }
}