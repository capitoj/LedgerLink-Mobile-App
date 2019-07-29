package org.applab.ledgerlink.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */

public class VslaCycleSchema {
    // Table: VslaCycles
    private static final String TBL_VSLA_CYCLES = "VslaCycles";
    public static final String COL_VC_CYCLE_ID = "_id";
    private static final String COL_VC_CYCLE_CODE = "CycleCode";
    public static final String COL_VC_START_DATE = "StartDate";
    public static final String COL_VC_END_DATE = "EndDate";
    public static final String COL_VC_SHARE_PRICE = "SharePrice";
    public static final String COL_VC_MAX_SHARE_QTY = "MaxShareQuantity";
    public static final String COL_VC_MAX_START_SHARE = "MaxStartShare";
    public static final String COL_VC_INTEREST_RATE = "InterestRate";
    public static final String COL_VC_IS_ACTIVE = "IsActive";
    public static final String COL_VC_IS_ENDED = "IsEnded";
    public static final String COL_VC_DATE_ENDED = "DateEnded";
    public static final String COL_VC_SHARED_AMOUNT = "SharedAmount";
    public static final String COL_VC_INTEREST_AT_SETUP = "InterestAtSetup";    //Interest in case of Mid-CYcle Setup
    public static final String COL_VC_FINES_AT_SETUP = "FinesAtSetup";    //Fines collected so far in case of Mid-CYcle Setup
    public static final String COL_VC_INTEREST_AT_SETUP_COMMENT = "InterestAtSetupComment";    //Interest in case of Mid-CYcle Setup
    public static final String COL_VC_FINES_AT_SETUP_COMMENT = "FinesAtSetupComment";    //Fines collected so far in case of Mid-CYcle Setup
    public static final String COL_VC_OUTSTANDING_BANK_LOAN_AT_SETUP = "OutstandingBankLoanAtSetup";
    public static final String COL_VC_OUTSTANDING_BANK_LOAN_AT_SETUP_COMMENT = "OutstandingBankLoanAtSetupComment";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: VslaInfo
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_VSLA_CYCLES + " (");
        sb.append(COL_VC_CYCLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_VC_CYCLE_CODE + " TEXT ,");
        sb.append(COL_VC_START_DATE + " TEXT ,");
        sb.append(COL_VC_END_DATE + " TEXT ,");
        sb.append(COL_VC_SHARE_PRICE + " NUMERIC ,");
        sb.append(COL_VC_MAX_SHARE_QTY + " NUMERIC ,");
        sb.append(COL_VC_MAX_START_SHARE + " NUMERIC ,");
        sb.append(COL_VC_INTEREST_RATE + " NUMERIC ,");
        sb.append(COL_VC_IS_ACTIVE + " INTEGER ,");
        sb.append(COL_VC_IS_ENDED + " INTEGER ,");
        sb.append(COL_VC_DATE_ENDED + " TEXT ,");
        sb.append(COL_VC_SHARED_AMOUNT + " NUMERIC ,");
        sb.append(COL_VC_INTEREST_AT_SETUP + " NUMERIC ,");
        sb.append(COL_VC_INTEREST_AT_SETUP_COMMENT + " TEXT ,");
        sb.append(COL_VC_FINES_AT_SETUP + " NUMERIC ,");
        sb.append(COL_VC_FINES_AT_SETUP_COMMENT + " TEXT");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_VSLA_CYCLES;
    }

    public static String getAlterTableScript() {
        StringBuffer sbAlter = null;

        // Create Table: VslaInfo
        sbAlter = new StringBuffer();
        sbAlter.append("ALTER TABLE " + TBL_VSLA_CYCLES + " ");
        //sbAlter.append("ADD COLUMN " + COL_VC_FINES_AT_SETUP_COMMENT + " TEXT AFTER " + COL_VC_FINES_AT_SETUP + ", ");
      //  sbAlter.append("ADD IF NOT EXISTS COLUMN " + COL_VC_NARA_AT_SETUP_COMMENT + " TEXT AFTER " + COL_VC_INTEREST_AT_SETUP_COMMENT + ";" );
        return sbAlter.toString();
    }

    public static String getTableName() {
        return TBL_VSLA_CYCLES;
    }

    public static String getColumnList() {

        return (COL_VC_CYCLE_ID + ",") + COL_VC_CYCLE_CODE + "," + COL_VC_START_DATE + "," + COL_VC_END_DATE + "," + COL_VC_SHARE_PRICE + "," + COL_VC_MAX_SHARE_QTY + "," + COL_VC_MAX_START_SHARE + "," + COL_VC_INTEREST_RATE + "," + COL_VC_IS_ACTIVE + "," + COL_VC_IS_ENDED + "," + COL_VC_DATE_ENDED + "," + COL_VC_SHARED_AMOUNT + "," + COL_VC_INTEREST_AT_SETUP + "," + COL_VC_INTEREST_AT_SETUP_COMMENT + "," + COL_VC_FINES_AT_SETUP + "," + COL_VC_FINES_AT_SETUP_COMMENT;
    }

    public static String[] getColumnListArray() {
        return getColumnList().split(",");
    }


}

