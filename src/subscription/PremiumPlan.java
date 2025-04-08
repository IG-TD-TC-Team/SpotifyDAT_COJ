package subscription;

//Concrete strategy
public class PremiumPlan implements SubscriptionPlan {
    @Override
    public boolean skipTracks() {
        return true;
    }

    @Override
    public boolean showAds() {
        return false;
    }
}
