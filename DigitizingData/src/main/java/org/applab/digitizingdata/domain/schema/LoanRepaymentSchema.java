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

    public static String getCreateTableScript() {
        StringBuffer sb = new StringBuffer();

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
        sb.append(COL_LR_COMMENTS + " TEXT");
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
        StringBuffer sb = new StringBuffer();
        sb.append(COL_LR_REPAYMENT_ID + ",");
        sb.append(COL_LR_LOAN_ID + ",");
        sb.append(COL_LR_MEMBER_ID + ",");
        sb.append(COL_LR_MEETING_ID + ",");
        sb.append(COL_LR_AMOUNT + ",");
        sb.append(COL_LR_BAL_BEFORE + ",");
        sb.append(COL_LR_BAL_AFTER + ",");
        sb.append(COL_LR_INTEREST_AMOUNT + ",");
        sb.append(COL_LR_ROLLOVER_AMOUNT + ",");
        sb.append(COL_LR_COMMENTS);

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
