package org.applab.ledgerlink.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class FineSchema {
    // Table: Fines
    public static final String TBL_FINES = "Fines";
    public static final String COL_F_FINE_ID = "_id";
    public static final String COL_F_MEETING_ID = "MeetingId";
    public static final String COL_F_MEMBER_ID = "MemberId";
    public static final String COL_F_FINE_TYPE_ID = "FineTypeId";
    //public static final String COL_F_FINE_TYPE_NAME = "FineTypeName";
    public static final String COL_F_AMOUNT = "Amount";
    public static final String COL_F_EXPECTED_DATE = "ExpectedDate";
    public static final String COL_F_IS_CLEARED = "IsCleared";
    public static final String COL_F_IS_DELETED = "IsDeleted";
    public static final String COL_F_DATE_CLEARED = "DateCleared";
    public static final String COL_F_PAID_IN_MEETING_ID = "PaidInMeetingId";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: Fines
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_FINES + " (");
        sb.append(COL_F_FINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_F_MEETING_ID + " INTEGER ,");
        sb.append(COL_F_MEMBER_ID + " INTEGER ,");
        //sb.append(COL_F_FINE_TYPE_NAME + " TEXT ,");
        sb.append(COL_F_FINE_TYPE_ID + " INTEGER ,");
        sb.append(COL_F_AMOUNT + " NUMERIC ,");
        sb.append(COL_F_EXPECTED_DATE + " TEXT ,");
        sb.append(COL_F_IS_DELETED + " INTEGER ,");
        sb.append(COL_F_IS_CLEARED + " INTEGER ,");
        sb.append(COL_F_DATE_CLEARED + " TEXT ,");
        sb.append(COL_F_PAID_IN_MEETING_ID + " INTEGER");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_FINES;
    }

    public static String getTableName() {
        return TBL_FINES;
    }

    public static String getColumnList() {
        // sb.append(COL_F_FINE_TYPE_NAME + ",");

        return (COL_F_FINE_ID + ",") + COL_F_MEETING_ID + "," + COL_F_MEMBER_ID + "," + COL_F_FINE_TYPE_ID + "," + COL_F_AMOUNT + "," + COL_F_EXPECTED_DATE + "," + COL_F_IS_DELETED + "," + COL_F_IS_CLEARED + "," + COL_F_DATE_CLEARED + "," + COL_F_PAID_IN_MEETING_ID;
    }

    public static String[] getColumnListArray() {
        return getColumnList().split(",");
    }
}
