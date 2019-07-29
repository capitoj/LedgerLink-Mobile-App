package org.applab.ledgerlink.domain.schema;

/**
 * Created by JCapito on 11/5/2018.
 */

public class OutstandingWelfareSchema {
    //Table: OutstandingWelfare
    public static final String TBL_OUTSTANDING_WELFARE = "OutstandingWelfare";
    public static final String COL_OW_ID = "_id";
    public static final String COL_OW_MEETING_ID = "MeetingId";
    public static final String COL_OW_MEMBER_ID = "MemberId";
    public static final String COL_OW_AMOUNT = "Amount";
    public static final String COL_OW_EXPECTED_DATE = "ExpectedDate";
    public static final String COL_OW_IS_CLEARED = "IsCleared";
    public static final String COL_OW_DATE_CLEARED = "DateCleared";
    public static final String COL_OW_PAID_IN_MEETING_ID = "PaidInMeetingId";
    public static final String COL_OW_COMMENT = "Comment";

    public static String getCreateTableScript(){
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_OUTSTANDING_WELFARE + " (");
        sb.append(COL_OW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(COL_OW_MEETING_ID + " INTEGER, ");
        sb.append(COL_OW_MEMBER_ID + " INTEGER, ");
        sb.append(COL_OW_AMOUNT + " INTEGER, ");
        sb.append(COL_OW_EXPECTED_DATE + " TEXT, ");
        sb.append(COL_OW_IS_CLEARED + " INTEGER, ");
        sb.append(COL_OW_DATE_CLEARED + " TEXT, ");
        sb.append(COL_OW_PAID_IN_MEETING_ID + " INTEGER, ");
        sb.append(COL_OW_COMMENT + " COMMENT");
        sb.append(")");
        return sb.toString();
    }

    public static String getDropTableScript(){
        return "DROP TABLE IF EXISTS " + TBL_OUTSTANDING_WELFARE;
    }

    public static String getColumnList(){
        return COL_OW_ID + "," + COL_OW_MEETING_ID + "," + COL_OW_MEMBER_ID + "," + COL_OW_AMOUNT + "," + COL_OW_EXPECTED_DATE + "," + COL_OW_IS_CLEARED + "," + COL_OW_DATE_CLEARED + "," + COL_OW_PAID_IN_MEETING_ID + "," + COL_OW_COMMENT;
    }
}
