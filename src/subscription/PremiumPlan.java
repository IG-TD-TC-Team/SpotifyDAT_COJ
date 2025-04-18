package subscription;

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
    public int maxSkipsPerHour() {
        return Integer.MAX_VALUE;
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
