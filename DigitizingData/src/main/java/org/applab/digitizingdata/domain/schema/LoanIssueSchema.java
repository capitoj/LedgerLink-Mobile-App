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
    public static final String COL_LI_COMMENTS = "Comments";
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
        sb.append(COL_LI_COMMENTS + " TEXT ,");
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
        StringBuffer sb = new StringBuffer();
        sb.append(COL_LI_LOAN_ID + ",");
        sb.append(COL_LI_LOAN_NO + ",");
        sb.append(COL_LI_MEETING_ID + ",");
        sb.append(COL_LI_MEMBER_ID + ",");
        sb.append(COL_LI_PRINCIPAL_AMOUNT + ",");
        sb.append(COL_LI_INTEREST_AMOUNT + ",");
        sb.append(COL_LI_BALANCE + ",");
        sb.append(COL_LI_DATE_DUE + ",");
        sb.append(COL_LI_TOTAL_REPAID + ",");
        sb.append(COL_LI_CLEARANCE_DATE + ",");
        sb.append(COL_LI_IS_DEFAULTED + ",");
        sb.append(COL_LI_IS_CLEARED + ",");
        sb.append(COL_LI_DATE_CLEARED + ",");
        sb.append(COL_LI_COMMENTS + ",");
        sb.append(COL_LI_IS_WRITTEN_OFF);

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
