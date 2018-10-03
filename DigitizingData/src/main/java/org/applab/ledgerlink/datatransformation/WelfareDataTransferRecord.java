package org.applab.ledgerlink.datatransformation;

import java.util.Date;

/**
 * Created by JCapito on 10/3/2018.
 */

public class WelfareDataTransferRecord {
    private int welfareId;
    private int meetingId;
    private int memberId;
    private double amount;
    private String comment;

    public void setWelfareId(int welfareId){
        this.welfareId = welfareId;
    }

    public int getWelfareId(){
        return this.welfareId;
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
