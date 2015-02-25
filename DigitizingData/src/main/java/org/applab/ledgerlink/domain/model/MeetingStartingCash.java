package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by Moses on 4/1/14.
 */
public class MeetingStartingCash {
    private int startingCashId;
    private Meeting meeting;
    private double actualStartingCash;
    private double cashSavedInBank;
    private String comment;
    private double amount;
    private Date dateSent;
    private double expectedStartingCash;
    private double loanTopUps;

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

    public double getLoanTopUps() {
        return loanTopUps;
    }

    public void setLoanTopUps(double loanTopUps) {
        this.loanTopUps = loanTopUps;
    }

    public double getExpectedStartingCash() {
        return expectedStartingCash;
    }

    public void setExpectedStartingCash(double expectedStartingCash) {
        this.expectedStartingCash = expectedStartingCash;
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

    public double getCashSavedInBank() {
        return cashSavedInBank;
    }

    public void setCashSavedInBank(double cashSavedInBank){
        this.cashSavedInBank = cashSavedInBank;
    }
}
