package org.applab.digitizingdata.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class LoanRepaymentSchema {
    // Table: LoanRepayments
    public static final String TBL_LOAN_REPAYMENT = "LoanRepayments";
    public static final String COL_LR_REPAYMENT_ID = "_id";
    public static final String COL_LR_LOAN_ID = "LoanId";
    public static final String COL_LR_MEETING_ID = "MeetingId";
    public static final String COL_LR_MEMBER_ID = "MemberId";
    public static final String COL_LR_AMOUNT = "Amount";
    public static final String COL_LR_BAL_BEFORE = "BalanceBefore";
    public static final String COL_LR_BAL_AFTER = "BalanceAfter";
    public static final String COL_LR_INTEREST_AMOUNT = "InterestAmount";
    public static final String COL_LR_ROLLOVER_AMOUNT = "RollOverAmount";
    public static final String COL_LR_COMMENTS = "Comments";
    public static final String COL_LR_LAST_DATE_DUE = "LastDateDue";
    public static final String COL_LR_NEXT_DATE_DUE = "NextDateDue";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: LoanRepayments
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_LOAN_REPAYMENT + " (");
        sb.append(COL_LR_REPAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_LR_LOAN_ID + " INTEGER ,");
        sb.append(COL_LR_MEMBER_ID + " INTEGER ,");
        sb.append(COL_LR_MEETING_ID + " INTEGER ,");
        sb.append(COL_LR_AMOUNT + " NUMERIC ,");
        sb.append(COL_LR_BAL_BEFORE + " NUMERIC ,");
        sb.append(COL_LR_BAL_AFTER + " NUMERIC ,");
        sb.append(COL_LR_INTEREST_AMOUNT + " NUMERIC ,");
        sb.append(COL_LR_ROLLOVER_AMOUNT + " NUMERIC ,");
        sb.append(COL_LR_COMMENTS + " TEXT ,");
        sb.append(COL_LR_LAST_DATE_DUE + " TEXT ,");
        sb.append(COL_LR_NEXT_DATE_DUE + " TEXT");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_LOAN_REPAYMENT;
    }

    public static String getTableName() {
        return TBL_LOAN_REPAYMENT;
    }

    public static String getColumnList() {

        return (COL_LR_REPAYMENT_ID + ",") + COL_LR_LOAN_ID + "," + COL_LR_MEMBER_ID + "," + COL_LR_MEETING_ID + "," + COL_LR_AMOUNT + "," + COL_LR_BAL_BEFORE + "," + COL_LR_BAL_AFTER + "," + COL_LR_INTEREST_AMOUNT + "," + COL_LR_ROLLOVER_AMOUNT + "," + COL_LR_COMMENTS + "," + COL_LR_LAST_DATE_DUE + "," + COL_LR_NEXT_DATE_DUE;
    }

    public static String[] getColumnListArray() {
        return getColumnList().split(",");
    }
}
