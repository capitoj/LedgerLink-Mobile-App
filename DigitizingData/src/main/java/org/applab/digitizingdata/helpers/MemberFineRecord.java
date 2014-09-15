package org.applab.digitizingdata.helpers;

import java.util.Date;

/**
 * Created by Moses on 7/27/13.
 */
public class MemberFineRecord {
    private int fineId;
    private Date meetingDate;
    private double amount;
    private String fineTypeName;
    private int fineTypeId;
    private int status;
    private int paidInMeetingId;

    public int getFineId() {
        return fineId;
    }

    public void setFineId(int fineId) {
        this.fineId = fineId;
    }

    public int getFineTypeId() {
        return fineTypeId;
    }

    public void setFineTypeId(int fineTypeId) {
        this.fineTypeId = fineTypeId;
    }

    public String getFineTypeName() {
        return fineTypeName;
    }

    public void setFineTypeName(String fineTypeName) {
        this.fineTypeName = fineTypeName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPaidInMeetingId() {
        return paidInMeetingId;
    }

    public void setPaidInMeetingId(int paidInMeetingId) {
        this.paidInMeetingId = paidInMeetingId;
    }
}