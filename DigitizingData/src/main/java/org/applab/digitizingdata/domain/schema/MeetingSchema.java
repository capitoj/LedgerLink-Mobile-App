package org.applab.digitizingdata.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class MeetingSchema {
    // Table: Meetings
    public static final String TBL_MEETINGS = "Meetings";
    public static final String COL_MT_MEETING_ID = "_id";
    public static final String COL_MT_CYCLE_ID = "CycleId";
    public static final String COL_MT_MEETING_DATE = "MeetingDate";
    public static final String COL_MT_IS_START_OF_CYCLE = "IsStartOfCycle";
    public static final String COL_MT_IS_END_OF_CYCLE = "IsEndOfCycle";
    public static final String COL_MT_IS_DATA_SENT = "IsDataSent";
    public static final String COL_MT_DATE_SENT = "DateSent";
    public static final String COL_MT_IS_CURRENT = "IsCurrent";

    public static String getCreateTableScript() {
        StringBuffer sb = new StringBuffer();

        // Create Table: VslaInfo
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_MEETINGS + " (");
        sb.append(COL_MT_MEETING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_MT_CYCLE_ID + " INTEGER ,");
        sb.append(COL_MT_MEETING_DATE + " TEXT ,");
        sb.append(COL_MT_IS_START_OF_CYCLE + " INTEGER ,");
        sb.append(COL_MT_IS_END_OF_CYCLE + " INTEGER ,");
        sb.append(COL_MT_IS_DATA_SENT + " INTEGER ,");
        sb.append(COL_MT_DATE_SENT + " TEXT ,");
        sb.append(COL_MT_IS_CURRENT + " INTEGER");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_MEETINGS;
    }

    public static String getTableName() {
        return TBL_MEETINGS;
    }

    public static String getColumnList() {
        StringBuffer sb = new StringBuffer();
        sb.append(COL_MT_MEETING_ID + ",");
        sb.append(COL_MT_CYCLE_ID + ",");
        sb.append(COL_MT_MEETING_DATE + ",");
        sb.append(COL_MT_IS_START_OF_CYCLE + ",");
        sb.append(COL_MT_IS_END_OF_CYCLE + ",");
        sb.append(COL_MT_IS_DATA_SENT + ",");
        sb.append(COL_MT_DATE_SENT + ",");
        sb.append(COL_MT_IS_CURRENT);

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
