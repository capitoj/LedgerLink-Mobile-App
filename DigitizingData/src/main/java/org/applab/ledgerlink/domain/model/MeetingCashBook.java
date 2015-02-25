package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by Moses on 4/1/14.
 */
class MeetingCashBook {
    private int cashBookId;
    private Meeting meeting;
    private double amount;
    private Date dateSent;

    public int getCashBookId() {
        return cashBookId;
    }

    public void setCashBookId(int cashBookId) {
        this.cashBookId = cashBookId;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }
}
