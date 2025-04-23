package user;

import java.util.Date;

/**
 *
 */
public class SubscriptionInfo {
    /**
     * Default constructor for Jackson deserialization.
     */
    public SubscriptionInfo() {

    }
    /**
     * Constructor to create a SubscriptionInfo object with start and end dates.
     * @param start The start date of the subscription.
     * @param end The end date of the subscription.
     */
    public SubscriptionInfo(Date start, Date end) {
        startDate = start;
        endDate = end;
    }

    private Date startDate;
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    //Historic possible
}
