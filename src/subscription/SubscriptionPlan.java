package subscription;

//Strategy
public interface SubscriptionPlan {

    boolean hasAds();
    boolean skipTracks();
    int maxSkipsPerHour();
    boolean canListenOffline();
    boolean downloadSongs();
    boolean shuffleOnly();
}
