package user;

//Concrete strategy
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
