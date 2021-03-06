package org.applab.ledgerlink.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class SavingSchema {
    // Table: Savings
    public static final String TBL_SAVINGS = "Savings";
    public static final String COL_S_SAVING_ID = "_id";
    public static final String COL_S_MEETING_ID = "MeetingId";
    public static final String COL_S_MEMBER_ID = "MemberId";
    public static final String COL_S_AMOUNT = "Amount";
    public static final String COL_S_SAVINGS_AT_SETUP_CORRECTION_COMMENT = "SavingsAtSetupComment";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: Savings
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_SAVINGS + " (");
        sb.append(COL_S_SAVING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_S_MEETING_ID + " INTEGER ,");
        sb.append(COL_S_MEMBER_ID + " INTEGER ,");
        sb.append(COL_S_AMOUNT + " NUMERIC ,");
        sb.append(COL_S_SAVINGS_AT_SETUP_CORRECTION_COMMENT + " TEXT");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_SAVINGS;
    }

    public static String getTableName() {
        return TBL_SAVINGS;
    }

    public static String getColumnList() {

        return (COL_S_SAVING_ID + ",") + COL_S_MEETING_ID + "," + COL_S_MEMBER_ID + "," + COL_S_AMOUNT + "," + COL_S_SAVINGS_AT_SETUP_CORRECTION_COMMENT;
    }

    public static String[] getColumnListArray() {
        return getColumnList().split(",");
    }
}
