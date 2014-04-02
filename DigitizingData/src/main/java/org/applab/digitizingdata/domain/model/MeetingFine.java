package org.applab.digitizingdata.domain.model;

import java.util.Date;

/**
 * Created by Moses on 4/1/14.
 */
public class MeetingFine {
    private int fineId;
    private Meeting meeting;
    private Member member;
    private FineType fineType;
    private double amount;
    private Date expectedDate;
    private boolean isCleared;
    private Date dateCleared;

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public FineType getFineType() {
        return fineType;
    }

    public void setFineType(FineType fineType) {
        this.fineType = fineType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(Date expectedDate) {
        this.expectedDate = expectedDate;
    }

    public boolean isCleared() {
        return isCleared;
    }

    public void setCleared(boolean isCleared) {
        this.isCleared = isCleared;
    }

    public Date getDateCleared() {
        return dateCleared;
    }

    public void setDateCleared(Date dateCleared) {
        this.dateCleared = dateCleared;
    }
}
