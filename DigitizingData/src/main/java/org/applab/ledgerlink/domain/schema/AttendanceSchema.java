package org.applab.ledgerlink.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class AttendanceSchema {
    // Table: Attendance
    public static final String TBL_ATTENDANCE = "Attendance";
    public static final String COL_A_ATTENDANCE_ID = "_id";
    public static final String COL_A_MEETING_ID = "MeetingId";
    public static final String COL_A_MEMBER_ID = "MemberId";
    public static final String COL_A_IS_PRESENT = "IsPresent";
    public static final String COL_A_COMMENTS = "Comments";

    public static String getCreateTableScript() {
        StringBuffer sb;

        // Create Table: Attendance
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_ATTENDANCE + " (");
        sb.append(COL_A_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_A_MEETING_ID + " INTEGER ,");
        sb.append(COL_A_MEMBER_ID + " INTEGER ,");
        sb.append(COL_A_IS_PRESENT + " INTEGER ,");
        sb.append(COL_A_COMMENTS + " TEXT");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_ATTENDANCE;
    }

    public static String getTableName() {
        return TBL_ATTENDANCE;
    }

    public static String getColumnList() {

        return (COL_A_ATTENDANCE_ID + ",") + COL_A_MEETING_ID + "," + COL_A_MEMBER_ID + "," + COL_A_IS_PRESENT + "," + COL_A_COMMENTS;
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
