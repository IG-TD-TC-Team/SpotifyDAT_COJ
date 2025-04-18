package user;

import java.util.Date;

public class SubscriptionInfo {

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
