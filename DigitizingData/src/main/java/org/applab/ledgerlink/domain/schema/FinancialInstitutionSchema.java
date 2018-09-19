package org.applab.ledgerlink.domain.schema;

/**
 * Created by JCapito on 9/17/2018.
 */

public class FinancialInstitutionSchema {

    private static final String TBL_FINANCIAL_INSTITUTION = "FinancialInstitution";
    public static final String COL_FI_FIID = "FIID";
    public static final String COL_FI_NAME = "Name";
    public static final String COL_FI_CODE = "Code";
    public static final String COL_FI_IP_ADDRESS = "IpAddress";

    public static String getCreateTableScript(){
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_FINANCIAL_INSTITUTION + " (");
        sb.append(COL_FI_FIID+ " INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(COL_FI_NAME+ " TEXT, ");
        sb.append(COL_FI_CODE+ " TEXT, ");
        sb.append(COL_FI_IP_ADDRESS+ " TEXT");
        sb.append(")");
        return sb.toString();
    }

    public static String getTableName(){
        return TBL_FINANCIAL_INSTITUTION;
    }

}
