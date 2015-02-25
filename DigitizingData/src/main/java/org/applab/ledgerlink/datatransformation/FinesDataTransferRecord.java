package org.applab.ledgerlink.datatransformation;

/**
 * Created by Moses on 10/25/13.
 */
public class FinesDataTransferRecord {
    private int finesId;
    private int meetingId;
    private int memberId;
    private double amount;

    public int getFinesId() {
        return finesId;
    }

    public void setFinesId(int finesId) {
        this.finesId = finesId;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
