package user.subscription;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("FamilySubscription")
public class FamilySubscription extends AbstractSubscriptionPlan {

    public FamilySubscription() {
        super(SubscriptionType.FAMILY);
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
        return 6; // Family plan gets more devices
    }
}
