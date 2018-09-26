package org.applab.ledgerlink.domain.model;

/**
 * Created by JCapito on 9/25/2018.
 */

public class MeetingWelfare {
    private int welfareId;
    private Meeting meeting;
    private Member member;
    private double amount;
    private String comment;

    public void setWelfareId(int welfareId){
        this.welfareId = welfareId;
    }

    public int getWelfareId(){
        return this.welfareId;
    }

    public void setMeeting(Meeting meeting){
        this.meeting = meeting;
    }

    public Meeting getMeeting(){
        return this.meeting;
    }

    public void setMember(Member member){
        this.member = member;
    }

    public Member getMember(){
        return this.member;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }

    public double getAmount(){
        return this.amount;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return this.comment;
    }
}
