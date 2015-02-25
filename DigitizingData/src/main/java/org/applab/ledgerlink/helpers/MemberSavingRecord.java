package org.applab.ledgerlink.helpers;

import java.util.Date;

/**
 * Created by Moses on 7/27/13.
 */
public class MemberSavingRecord {
    private int savingId;
    private Date meetingDate;
    private double amount;

    public int getSavingId() {
        return savingId;
    }

    public void setSavingId(int savingId) {
        this.savingId = savingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }
}
