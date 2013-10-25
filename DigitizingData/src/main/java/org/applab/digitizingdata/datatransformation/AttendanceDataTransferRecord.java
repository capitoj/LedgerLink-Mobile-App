package org.applab.digitizingdata.datatransformation;

/**
 * Created by Moses on 10/25/13.
 */
public class AttendanceDataTransferRecord {
    private int attendanceId;
    private int meetingId;
    private int memberId;
    private int isPresentFlg;
    private String comments;

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
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

    public int getPresentFlg() {
        return isPresentFlg;
    }

    public void setPresentFlg(int presentFlg) {
        isPresentFlg = presentFlg;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
