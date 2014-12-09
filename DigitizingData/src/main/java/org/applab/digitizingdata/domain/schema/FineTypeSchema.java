package org.applab.digitizingdata.domain.schema;

/**
 * Created by Moses on 7/13/13.
 */
public class FineTypeSchema {
    // Table: Savings
    public static final String TBL_FINE_TYPES = "FineTypes";
    public static final String COL_FT_FINE_TYPE_ID = "_id";
    public static final String COL_FT_FINE_TYPE_NAME = "FineTypeName";
    public static final String COL_FT_FINE_TYPE_DESC = "FineTypeDesc";
    public static final String COL_FT_DEFAULT_AMOUNT = "DefaultAmount";

    public static String getCreateTableScript() {
        StringBuffer sb = null;

        // Create Table: Savings
        sb = new StringBuffer();
        sb.append("CREATE TABLE " + TBL_FINE_TYPES + " (");
        sb.append(COL_FT_FINE_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,");
        sb.append(COL_FT_FINE_TYPE_NAME + " TEXT ,");
        sb.append(COL_FT_FINE_TYPE_DESC + " TEXT ,");
        sb.append(COL_FT_DEFAULT_AMOUNT + " NUMERIC");
        sb.append(")");

        return sb.toString();
    }

    public static String getDropTableScript() {
        return "DROP TABLE IF EXISTS " + TBL_FINE_TYPES;
    }

    public static String getTableName() {
        return TBL_FINE_TYPES;
    }

    public static String getColumnList() {

        return (COL_FT_FINE_TYPE_ID + ",") + COL_FT_FINE_TYPE_NAME + "," + COL_FT_FINE_TYPE_DESC + "," + COL_FT_DEFAULT_AMOUNT;
    }

    public static String[] getColumnListArray() {
        String[] columns = getColumnList().split(",");
        return columns;
    }
}
