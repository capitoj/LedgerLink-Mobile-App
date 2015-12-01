package org.applab.ledgerlink.datatransformation;

import org.applab.ledgerlink.domain.model.Meeting;

import java.util.Date;

/**
 * Created by Moses on 10/25/13.
 */
public class FinesDataTransferRecord {
    private int finesId;
    private int meetingId;
    private int memberId;
    private double amount;
    private boolean isCleared;
    private Date dateCleared;
    private int paidInMeetingId;
    private int fineTypeId;

    public void setFineTypeId(int fineTypeId){
        this.fineTypeId = fineTypeId;
    }

    public int getFineTypeId(){
        return this.fineTypeId;
    }

    public void setPaidInMeeting(int paidInMeetingId){
        this.paidInMeetingId = paidInMeetingId;
    }

    public int getPaidInMeetingId(){
        return this.paidInMeetingId;
    }

    public void setDateCleared(Date dateCleared){
        this.dateCleared = dateCleared;
    }

    public Date getDateCleared(){
        return this.dateCleared;
    }

    public void setCleared(boolean isCleared){
        this.isCleared = isCleared;
    }

    public boolean isCleared(){
        return this.isCleared;
    }

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
