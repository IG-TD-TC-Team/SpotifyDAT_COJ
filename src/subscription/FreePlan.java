package subscription;

//Concrete strategy
public class FreePlan implements SubscriptionPlan {
    @Override
    public boolean hasAds() {
        return true;
    }

    @Override
    public boolean skipTracks() {
        return false;
    }

    @Override
    public int maxSkipsPerHour() {
        return 10;
    }

    @Override
    public boolean canListenOffline() {
        return false;
    }

    @Override
    public boolean downloadSongs() {
        return false;
    }

    @Override
    public boolean shuffleOnly() {
        return true;
    }
}
