package org.applab.ledgerlink.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class VslaInfoSchema {
    // Table: VslaInfo
    private static final String TBL_VSLA_INFO = "VslaInfo";
    public static final String COL_VI_VSLA_NAME = "VslaName";
    public static final String COL_VI_VSLA_CODE = "VslaCode";
    public static final String COL_VI_PASS_KEY = "PassKey";
    public static final String COL_VI_DATE_REGISTERED = "DateRegistered";
    public static final String COL_VI_DATE_LINKED = "DateLinked";
    public static final String COL_VI_BANK_BRANCH = "BankBranch";
    public static final String COL_VI_ACCOUNT_NO = "AccountNo";
    public static final String COL_VI_IS_ACTIVATED = "IsActivated";
    public static final String COL_VI_DATE_ACTIVATED = "DateActivated";
    public static final String COL_VI_IS_OFFLINE = "IsOffline";
    public static final String COL_VI_ALLOW_DATA_MIGRATION = "AllowDataMigration";
    public static final String COL_VI_IS_DATA_MIGRATED = "IsDataMigrated";

    public static final String COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE = "IsGettingStartedWizardComplete"; //field to show getting started wizard status
    public static final String COL_VI_GETTING_STARTED_WIZARD_STAGE = "GettingStartedWizard"; //field to show getting started wizard status

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: VslaInfo
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_VSLA_INFO + " (");
        sb.append(COL_VI_VSLA_NAME + " TEXT ,");
        sb.append(COL_VI_VSLA_CODE + " TEXT ,");
        sb.append(COL_VI_PASS_KEY + " TEXT ,");
        sb.append(COL_VI_DATE_REGISTERED + " TEXT ,");
        sb.append(COL_VI_DATE_LINKED + " TEXT ,");
        sb.append(COL_VI_BANK_BRANCH + " TEXT ,");
        sb.append(COL_VI_ACCOUNT_NO + " TEXT ,");
        sb.append(COL_VI_IS_ACTIVATED + " INTEGER ,");
        sb.append(COL_VI_DATE_ACTIVATED + " TEXT ,");
        sb.append(COL_VI_IS_OFFLINE + " INTEGER ,");
        sb.append(COL_VI_ALLOW_DATA_MIGRATION + " INTEGER ,");
        sb.append(COL_VI_IS_DATA_MIGRATED + " INTEGER ,");
        sb.append(COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE+ " INTEGER ,");
        sb.append(COL_VI_GETTING_STARTED_WIZARD_STAGE+ " INTEGER");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_VSLA_INFO;
    }

    public static String getTableName() {
        return TBL_VSLA_INFO;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public static String getColumnList() {
        StringBuffer sb = new StringBuffer();
        sb.append(COL_VI_VSLA_NAME + ",");
        sb.append(COL_VI_VSLA_CODE + ",");
        sb.append(COL_VI_PASS_KEY + ",");
        sb.append(COL_VI_DATE_REGISTERED + ",");
        sb.append(COL_VI_DATE_LINKED + ",");
        sb.append(COL_VI_BANK_BRANCH + ",");
        sb.append(COL_VI_ACCOUNT_NO + ",");
        sb.append(COL_VI_IS_ACTIVATED + ",");
        sb.append(COL_VI_DATE_ACTIVATED + ",");
        sb.append(COL_VI_IS_OFFLINE + ",");
        sb.append(COL_VI_ALLOW_DATA_MIGRATION + ",");
        sb.append(COL_VI_IS_DATA_MIGRATED + ",");
        sb.append(COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE + ",");
        sb.append(COL_VI_GETTING_STARTED_WIZARD_STAGE );

        return sb.toString();
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
