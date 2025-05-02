package user.subscription;

import java.util.Date;

/**
 *
 */
public class SubscriptionInfo {
    private Date startDate;
    private Date endDate;
    private Date lastBillingDate;
    private String paymentMethod;
    private boolean autoRenew;
    private double price;

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
        this.startDate = start;
        this.endDate = end;
        this.lastBillingDate = start;
        this.autoRenew = true;
    }

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

    public Date getLastBillingDate() {
        return lastBillingDate;
    }

    public void setLastBillingDate(Date lastBillingDate) {
        this.lastBillingDate = lastBillingDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
