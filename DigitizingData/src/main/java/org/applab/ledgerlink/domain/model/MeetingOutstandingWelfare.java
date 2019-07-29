package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by JCapito on 11/5/2018.
 */

public class MeetingOutstandingWelfare {

    private int outstandingWelfareId;
    private Meeting meeting;
    private Member member;
    private double amount;
    private Date expectedDate;
    private int isCleared;
    private Date dateCleared;
    private Meeting paidInMeeting;
    private String comment;

    public int getOutstandingWelfareId(){
        return this.outstandingWelfareId;
    }

    public void setOutstandingWelfareId(int outstandingWelfareId){
        this.outstandingWelfareId = outstandingWelfareId;
    }

    public Meeting getMeeting(){
        return this.meeting;
    }

    public void setMeeting(Meeting meeting){
        this.meeting = meeting;
    }

    public Member getMember(){
        return this.member;
    }

    public void setMember(Member member){
        this.member = member;
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

    public void setPaidInMeeting(Meeting paidInMeeting){
        this.paidInMeeting = paidInMeeting;
    }

    public Meeting getPaidInMeeting(){
        return this.paidInMeeting;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return this.comment;
    }
}
