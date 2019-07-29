package org.applab.ledgerlink.datatransformation;

import java.util.Date;

/**
 * Created by JCapito on 12/4/2018.
 */

public class OutstandingWelfareDataTransferRecord {
    private int outstandingWelfareId;
    private int meetingId;
    private int memberId;
    private double amount;
    private Date expectedDate;
    private int isCleared;
    private Date dateCleared;
    private int paidInMeetingId;
    private String comment;

    public int getOutstandingWelfareId(){
        return this.outstandingWelfareId;
    }

    public void setOutstandingWelfareId(int outstandingWelfareId){
        this.outstandingWelfareId = outstandingWelfareId;
    }

    public void setMeetingId(int meetingId){
        this.meetingId = meetingId;
    }

    public int getMeetingId(){
        return this.meetingId;
    }

    public void setMemberId(int memberId){
        this.memberId = memberId;
    }

    public int getMemberId(){
        return this.memberId;
    }

    public double getAmount(){
        return this.amount;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }

    public Date getExpectedDate(){
        return this.expectedDate;
    }

    public void setExpectedDate(Date expectedDate){
        this.expectedDate = expectedDate;
    }

    public int getIsCleared(){
        return this.isCleared;
    }

    public void setIsCleared(int isCleared){
        this.isCleared = isCleared;
    }

    public Date getDateCleared(){
        return this.dateCleared;
    }

    public void setDateCleared(Date dateCleared){
        this.dateCleared = dateCleared;
    }

    public void setPaidInMeetingId(int paidInMeetingId){
        this.paidInMeetingId = paidInMeetingId;
    }

    public int getPaidInMeetingId(){
        return this.paidInMeetingId;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return this.comment;
    }
}
