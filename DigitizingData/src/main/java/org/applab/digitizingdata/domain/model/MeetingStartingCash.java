package org.applab.digitizingdata.domain.model;

import java.util.Date;

/**
 * Created by Moses on 4/1/14.
 */
public class MeetingStartingCash {
    private int startingCashId;
    private Meeting meeting;
    private double actualStartingCash;
    private String comment;
    private double amount;
    private Date dateSent;

    public int getStartingCashId() {
        return startingCashId;
    }

    public void setStartingCashId(int startingCashId) {
        this.startingCashId = startingCashId;
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

    public double getActualStartingCash() {
        return actualStartingCash;
    }

    public void setActualStartingCash(double actualStartingCash) {
        this.actualStartingCash = actualStartingCash;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
