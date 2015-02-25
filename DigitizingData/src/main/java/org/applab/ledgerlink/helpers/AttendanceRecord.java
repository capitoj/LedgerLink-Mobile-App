package org.applab.ledgerlink.helpers;

import java.util.Date;

/**
 * Created by Moses on 7/25/13.
 */
public class AttendanceRecord {
    private int attendanceId;
    private Date meetingDate;
    private int isPresent;
    private String comment;

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public int getPresent() {
        return isPresent;
    }

    public void setPresent(int present) {
        isPresent = present;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
