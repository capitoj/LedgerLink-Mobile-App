package org.applab.ledgerlink.helpers;

import java.util.Date;

/**
 * Created by JCapito on 9/27/2018.
 */

public class MemberWelfareRecord {
    private int welfareId;
    private Date meetingDate;
    private double amount;

    public void setWelfareId(int welfareId){
        this.welfareId = welfareId;
    }

    public int getWelfareId(){
        return this.welfareId;
    }

    public void setMeetingDate(Date meetingDate){
        this.meetingDate = meetingDate;
    }

    public Date getMeetingDate(){
        return this.meetingDate;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }

    public double getAmount(){
        return this.amount;
    }
}
