package org.applab.digitizingdata.domain.schema;

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
        StringBuffer sb = new StringBuffer();

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
        StringBuffer sb = new StringBuffer();
        sb.append(COL_A_ATTENDANCE_ID + ",");
        sb.append(COL_A_MEETING_ID + ",");
        sb.append(COL_A_MEMBER_ID + ",");
        sb.append(COL_A_IS_PRESENT + ",");
        sb.append(COL_A_COMMENTS);

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
