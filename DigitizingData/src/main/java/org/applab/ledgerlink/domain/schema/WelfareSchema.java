package org.applab.ledgerlink.domain.schema;

/**
 * Created by JCapito on 9/25/2018.
 */

public class WelfareSchema {
    // Table. Welfare
    public static final String TBL_WELFARE = "Welfare";
    public static final String COL_W_WELFARE_ID = "_id";
    public static final String COL_W_MEETING_ID = "MeetingId";
    public static final String COL_W_MEMBER_ID = "MemberId";
    public static final String COL_W_AMOUNT = "Amount";
    public static final String COL_W_WELFARE_AT_SETUP_CORRECTION_COMMMENT = "WelfareAtSetupComment";

    public static String getCreateTableScript(){
        //Create table: Welfare
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_WELFARE + " (");
        sb.append(COL_W_WELFARE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(COL_W_MEETING_ID + " INTEGER, ");
        sb.append(COL_W_MEMBER_ID + " INTEGER, ");
        sb.append(COL_W_AMOUNT + " NUMERIC, ");
        sb.append(COL_W_WELFARE_AT_SETUP_CORRECTION_COMMMENT + " TEXT");
        sb.append(")");
        return sb.toString();
    }

    public static String getDropTableScript(){
        return "DROP TABLE IF EXISTS " + TBL_WELFARE;
    }

    public static String getTableName(){
        return TBL_WELFARE;
    }

    public static String getColumnList(){
        return COL_W_WELFARE_ID + "," + COL_W_MEETING_ID + "," + COL_W_MEMBER_ID + "," + COL_W_AMOUNT + "," + COL_W_WELFARE_AT_SETUP_CORRECTION_COMMMENT;
    }

    public static String[] getColumnListArray(){
        return getColumnList().split(",");
    }
}
