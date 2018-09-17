package org.applab.ledgerlink.domain.schema;

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
    public static final String COL_MT_CASH_FROM_BOX = "CashFromBox";
    public static final String COL_MT_CASH_FROM_BOX_COMMENT = "CashFromBoxComment";
    public static final String COL_MT_CASH_FROM_BANK = "CashFromBank";
    public static final String COL_MT_LOAN_TOP_UPS = "LoanTopUps";
    public static final String COL_MT_CASH_FINES = "CashFines";
    public static final String COL_MT_CASH_WELFARE = "CashWelfare";
    public static final String COL_MT_CASH_EXPENSES = "CashExpenses";
    public static final String COL_MT_CASH_SAVED_BOX = "CashSavedBox";
    public static final String COL_MT_CASH_SAVED_BANK = "CashSavedBank";
    public static final String COL_MT_IS_GETTING_STARTED_WIZARD = "IsGettingStartedWizard";
    public static final String COL_MT_IS_MARKED_FOR_DELETION = "IsMarkedForDeletion";
    public static final String COL_MT_LOAN_FROM_BANK = "LoanFromBank";
    public static final String COL_MT_BANK_LOAN_REPAYMENT = "BankLoanRepayment";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

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
        sb.append(COL_MT_IS_CURRENT + " INTEGER ,");
        sb.append(COL_MT_CASH_FROM_BOX + " NUMERIC ,");
        sb.append(COL_MT_CASH_FROM_BANK + " NUMERIC ,");
        sb.append(COL_MT_CASH_FROM_BOX_COMMENT + " TEXT ,");
        sb.append(COL_MT_LOAN_TOP_UPS + " NUMERIC ,");
        sb.append(COL_MT_CASH_FINES + " NUMERIC ,");
        sb.append(COL_MT_CASH_WELFARE + " NUMERIC ,");
        sb.append(COL_MT_CASH_EXPENSES + " NUMERIC ,");
        sb.append(COL_MT_CASH_SAVED_BOX + " NUMERIC ,");
        sb.append(COL_MT_CASH_SAVED_BANK + " NUMERIC ,");
        sb.append(COL_MT_IS_GETTING_STARTED_WIZARD + " INTEGER ,");
        sb.append(COL_MT_IS_MARKED_FOR_DELETION + " INTEGER ,");
        sb.append(COL_MT_LOAN_FROM_BANK + " NUMERIC ,");
        sb.append(COL_MT_BANK_LOAN_REPAYMENT + " NUMERIC");
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
        //sb.append(COL_MT_IS_MARKED_FOR_DELETION); will enable this later. when implementing UNDO feature
        return (COL_MT_MEETING_ID + ",") + COL_MT_CYCLE_ID + "," + COL_MT_MEETING_DATE + "," + COL_MT_IS_START_OF_CYCLE + "," + COL_MT_IS_END_OF_CYCLE + "," + COL_MT_IS_DATA_SENT + "," + COL_MT_DATE_SENT + "," + COL_MT_IS_CURRENT + "," + COL_MT_CASH_FROM_BOX + "," + COL_MT_CASH_FROM_BOX_COMMENT + "," + COL_MT_CASH_FROM_BANK + "," + COL_MT_LOAN_TOP_UPS + "," + COL_MT_CASH_FINES + "," + COL_MT_CASH_WELFARE + "," + COL_MT_CASH_EXPENSES + "," + COL_MT_CASH_SAVED_BOX + "," + COL_MT_CASH_SAVED_BANK + "," + COL_MT_IS_GETTING_STARTED_WIZARD;
    }

    public static String[] getColumnListArray() {
        return getColumnList().split(",");
    }

    public static String addColumnLoanFromBank(){
        return "Alter table Meetings add column LoanFromBank NUMERIC";
    }

    public static String addColumnBankLoanRepayment(){
        return "Alter table Meetings add column BankLoanRepayment NUMERIC";
    }
}
