package user;

//Strategy
public interface SubscriptionPlan {

    boolean hasAds();
    boolean skipTracks(); //could be a max skip tracks
    boolean canListenOffline();
    boolean downloadSongs();
    boolean shuffleOnly();
}
