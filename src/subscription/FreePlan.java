package subscription;

//Concrete strategy
public class FreePlan implements SubscriptionPlan {
    @Override
    public boolean showAds() {
        return true;
    }

    @Override
    public boolean skipTracks() {
        return false;
    }
}
