package org.applab.digitizingdata.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class LoanIssueSchema {
    // Table: LoanIssues
    public static final String TBL_LOAN_ISSUE = "LoanIssues";
    public static final String COL_LI_LOAN_ID = "_id";
    public static final String COL_LI_LOAN_NO = "LoanNo";
    public static final String COL_LI_MEETING_ID = "MeetingId";
    public static final String COL_LI_MEMBER_ID = "MemberId";
    public static final String COL_LI_PRINCIPAL_AMOUNT = "PrincipalAmount";
    public static final String COL_LI_INTEREST_AMOUNT = "InterestAmount";
    public static final String COL_LI_BALANCE = "Balance";
    public static final String COL_LI_DATE_DUE = "DateDue";
    public static final String COL_LI_TOTAL_REPAID = "TotalRepaid";
    public static final String COL_LI_CLEARANCE_DATE = "ClearanceDate";
    public static final String COL_LI_IS_CLEARED = "IsCleared";
    public static final String COL_LI_DATE_CLEARED = "DateCleared";
    public static final String COL_LI_IS_DEFAULTED = "IsDefaulted";
    public static final String COL_LI_COMMENT = "Comments";
    public static final String COL_LI_IS_WRITTEN_OFF = "IsWrittenOff";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: LoanIssues
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_LOAN_ISSUE + " (");
        sb.append(COL_LI_LOAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_LI_LOAN_NO + " INTEGER ,");
        sb.append(COL_LI_MEETING_ID + " INTEGER ,");
        sb.append(COL_LI_MEMBER_ID + " INTEGER ,");
        sb.append(COL_LI_PRINCIPAL_AMOUNT + " NUMERIC ,");
        sb.append(COL_LI_INTEREST_AMOUNT + " NUMERIC ,");
        sb.append(COL_LI_BALANCE + " NUMERIC ,");
        sb.append(COL_LI_DATE_DUE + " TEXT ,");
        sb.append(COL_LI_TOTAL_REPAID + " NUMERIC ,");
        sb.append(COL_LI_CLEARANCE_DATE + " TEXT ,");
        sb.append(COL_LI_IS_DEFAULTED + " INTEGER ,");
        sb.append(COL_LI_IS_CLEARED + " INTEGER ,");
        sb.append(COL_LI_DATE_CLEARED + " TEXT ,");
        sb.append(COL_LI_COMMENT + " TEXT ,");
        sb.append(COL_LI_IS_WRITTEN_OFF + " INTEGER");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_LOAN_ISSUE;
    }

    public static String getTableName() {
        return TBL_LOAN_ISSUE;
    }

    public static String getColumnList() {

        return (COL_LI_LOAN_ID + ",") + COL_LI_LOAN_NO + "," + COL_LI_MEETING_ID + "," + COL_LI_MEMBER_ID + "," + COL_LI_PRINCIPAL_AMOUNT + "," + COL_LI_INTEREST_AMOUNT + "," + COL_LI_BALANCE + "," + COL_LI_DATE_DUE + "," + COL_LI_TOTAL_REPAID + "," + COL_LI_CLEARANCE_DATE + "," + COL_LI_IS_DEFAULTED + "," + COL_LI_IS_CLEARED + "," + COL_LI_DATE_CLEARED + "," + COL_LI_COMMENT + "," + COL_LI_IS_WRITTEN_OFF;
    }

    public static String[] getColumnListArray() {
        return getColumnList().split(",");
    }
}
