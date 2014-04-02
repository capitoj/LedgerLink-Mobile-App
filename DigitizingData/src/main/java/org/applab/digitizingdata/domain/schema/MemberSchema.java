package org.applab.digitizingdata.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class MemberSchema {
    // Table: Members
    public static final String TBL_MEMBERS = "Members";
    public static final String COL_M_MEMBER_ID = "_id";
    public static final String COL_M_MEMBER_NO = "MemberNo";
    public static final String COL_M_GLOBAL_ID = "GlobalId";
    public static final String COL_M_SURNAME = "Surname";
    public static final String COL_M_OTHER_NAMES = "OtherNames";
    public static final String COL_M_PHONE_NO = "PhoneNo";
    public static final String COL_M_GENDER = "Gender";
    public static final String COL_M_DATE_OF_BIRTH = "DateOfBirth";
    public static final String COL_M_OCCUPATION = "Occupation";
    public static final String COL_M_DATE_JOINED = "DateJoined";
    public static final String COL_M_DATE_LEFT = "DateLeft";
    public static final String COL_M_HAS_LEFT = "HasLeft";
    public static final String COL_M_IS_RECESSED = "IsRecessed";
    public static final String COL_M_SAVINGS_AT_REGISTRATION = "SavingsAtRegistration";
    public static final String COL_M_LOAN_BALANCE_AT_REGISTRATION = "LoanBalanceAtRegistration";

    public static String getCreateTableScript() {
        StringBuffer sb = new StringBuffer();

        // Create Table: Members
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_MEMBERS + " (");
        sb.append(COL_M_MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(COL_M_MEMBER_NO + " NUMERIC ,");
        sb.append(COL_M_GLOBAL_ID + " TEXT ,");
        sb.append(COL_M_SURNAME + " TEXT ,");
        sb.append(COL_M_OTHER_NAMES + " TEXT ,");
        sb.append(COL_M_PHONE_NO + " TEXT ,");
        sb.append(COL_M_GENDER + " TEXT ,");
        sb.append(COL_M_DATE_OF_BIRTH + " TEXT ,");
        sb.append(COL_M_OCCUPATION + " TEXT ,");
        sb.append(COL_M_DATE_JOINED + " TEXT ,");
        sb.append(COL_M_DATE_LEFT + " TEXT ,");
        sb.append(COL_M_HAS_LEFT + " INTEGER ,");
        sb.append(COL_M_IS_RECESSED + " INTEGER ,");
        sb.append(COL_M_SAVINGS_AT_REGISTRATION + " NUMERIC ,");
        sb.append(COL_M_LOAN_BALANCE_AT_REGISTRATION + " NUMERIC");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_MEMBERS;
    }

    public static String getTableName() {
        return TBL_MEMBERS;
    }

    public static String getColumnList() {
        StringBuffer sb = new StringBuffer();
        sb.append(COL_M_MEMBER_ID + ",");
        sb.append(COL_M_MEMBER_NO + ",");
        sb.append(COL_M_GLOBAL_ID + ",");
        sb.append(COL_M_SURNAME + ",");
        sb.append(COL_M_OTHER_NAMES + ",");
        sb.append(COL_M_PHONE_NO + ",");
        sb.append(COL_M_GENDER + ",");
        sb.append(COL_M_DATE_OF_BIRTH + ",");
        sb.append(COL_M_OCCUPATION + ",");
        sb.append(COL_M_DATE_JOINED + ",");
        sb.append(COL_M_DATE_LEFT + ",");
        sb.append(COL_M_HAS_LEFT + ",");
        sb.append(COL_M_IS_RECESSED + ",");
        sb.append(COL_M_SAVINGS_AT_REGISTRATION + ",");
        sb.append(COL_M_LOAN_BALANCE_AT_REGISTRATION );

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
