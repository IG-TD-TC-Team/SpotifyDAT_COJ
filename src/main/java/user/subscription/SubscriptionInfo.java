package user.subscription;

import java.util.Date;

/**
 * Stores metadata and billing information for a user's subscription.
 *
 * This class encapsulates all relevant details about a user's subscription status,
 * including temporal information (start date, end date, billing date), payment details,
 * and subscription preferences. It serves as a companion to the {@link SubscriptionPlan}
 * classes, which define the features of a subscription, while this class tracks the
 * lifecycle and billing aspects.
 *
 * This class is designed to work with the Jackson JSON serialization framework,
 * providing both a default no-arg constructor for deserialization and a parameterized
 * constructor for programmatic creation.
 *
 * @see SubscriptionPlan
 * @see SubscriptionType
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

    /**
     * Getters and Setters
     *
     */
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
