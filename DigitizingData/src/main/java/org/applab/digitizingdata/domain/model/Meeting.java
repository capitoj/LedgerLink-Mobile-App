package org.applab.digitizingdata.domain.model;

import java.util.Date;

/**
 * Created by Moses on 7/3/13.
 */
public class Meeting {
    private int meetingId;
    private VslaCycle vslaCycle;
    private Date meetingDate;
    private boolean isStartOfCycle;
    private boolean isEndOfCycle;
    private boolean meetingDataSent;
    private Date dateSent;
    private boolean isCurrent;


    public boolean isCurrent() {
        return this.isCurrent;
    }

    public void setIsCurrent(boolean value) {
        isCurrent = value;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public VslaCycle getVslaCycle() {
        return vslaCycle;
    }

    public void setVslaCycle(VslaCycle vslaCycle) {
        this.vslaCycle = vslaCycle;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public boolean isStartOfCycle() {
        return isStartOfCycle;
    }

    public void setStartOfCycle(boolean startOfCycle) {
        isStartOfCycle = startOfCycle;
    }

    public boolean isEndOfCycle() {
        return isEndOfCycle;
    }

    public void setEndOfCycle(boolean endOfCycle) {
        isEndOfCycle = endOfCycle;
    }

    public boolean isMeetingDataSent() {
        return meetingDataSent;
    }

    public void setMeetingDataSent(boolean meetingDataSent) {
        this.meetingDataSent = meetingDataSent;
    }

    public Date DateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Meeting(int meetingId, VslaCycle vslaCycle, Date meetingDate, boolean isStartOfCycle, boolean isEndOfCycle) {
        this.meetingId = meetingId;
        this.vslaCycle = vslaCycle;
        this.meetingDate = meetingDate;
        this.isStartOfCycle = isStartOfCycle;
        this.isEndOfCycle = isEndOfCycle;
    }

    public Meeting() {

    }

    public boolean sendMeetingData() {

        //Connect to HTTP and push the data
        this.setMeetingDataSent(true);
        this.setDateSent(new Date());
        return true;
    }

    public boolean getMeetingData() {
        return true;
    }
}
