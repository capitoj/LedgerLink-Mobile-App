package org.applab.ledgerlink.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.applab.ledgerlink.domain.model.TrainingModule;
import org.applab.ledgerlink.domain.schema.AttendanceSchema;
import org.applab.ledgerlink.domain.schema.FinancialInstitutionSchema;
import org.applab.ledgerlink.domain.schema.FineSchema;
import org.applab.ledgerlink.domain.schema.FineTypeSchema;
import org.applab.ledgerlink.domain.schema.LoanIssueSchema;
import org.applab.ledgerlink.domain.schema.LoanRepaymentSchema;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.domain.schema.MessageChannelsSchema;
import org.applab.ledgerlink.domain.schema.SavingSchema;
import org.applab.ledgerlink.domain.schema.TrainingModuleResponseSchema;
import org.applab.ledgerlink.domain.schema.TrainingModuleSchema;
import org.applab.ledgerlink.domain.schema.VslaCycleSchema;
import org.applab.ledgerlink.domain.schema.VslaInfoSchema;
import org.applab.ledgerlink.SettingsActivity;
import org.applab.ledgerlink.domain.schema.MemberSchema;
import org.applab.ledgerlink.domain.schema.WelfareSchema;
import org.applab.ledgerlink.repo.TrainingModuleRepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String EXTERNAL_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = "ledgerlinkdb";
    private static final int DATABASE_VERSION = 61;
    private static final String TRAINING_DATABASE_NAME = "ledgerlinktraindb";
    private static final String DATA_FOLDER = "LedgerLink";

    public static Context databaseContext = null;

    private DatabaseHandler(Context context) {
        super(context, createDatabaseFolder(context), null, DATABASE_VERSION);
        databaseContext = context;
    }

    protected static boolean isSDCardMounted(){

        return android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String createDatabaseFolder(Context context) {
        //creates the database folders and returns path as string
        String folderName = DATABASE_NAME;
        if(DatabaseHandler.isSDCardMounted()){

            File[] fileList = new File("/storage/").listFiles();
            int cursor = 0;
            for(File file: fileList){
                String absolutePath = file.getAbsolutePath();
                if(absolutePath.contains("sdcard")){
                    cursor++;
                    if(cursor == 1){
                        break;
                    }
                }
            }
            if(cursor == 1){
                folderName = DatabaseHandler.__createFolderPath(context, EXTERNAL_STORAGE_LOCATION);
            }
        }else{
            folderName = DatabaseHandler.__createFolderPath(context, EXTERNAL_STORAGE_LOCATION);
        }
        return folderName;
    }

    protected static String __createFolderPath(Context context, String absolutePath){
        File databaseStorageDir = new File(absolutePath + File.separator + DATA_FOLDER);
        if(! databaseStorageDir.exists()) {
            boolean isFolderCreated = databaseStorageDir.mkdir();
        }
        return databaseStorageDir.getAbsolutePath() + File.separator + ((Utils.getDefaultSharedPreferences(context).getString(SettingsActivity.PREF_KEY_EXECUTION_MODE,"1").equalsIgnoreCase(SettingsActivity.PREF_VALUE_EXECUTION_MODE_TRAINING)) ? TRAINING_DATABASE_NAME : DATABASE_NAME);
    }

    public void onCreate(SQLiteDatabase db) {

        String sqlQuery = null;

        //Create Table: FinancialInstitution
        sqlQuery = FinancialInstitutionSchema.getCreateTableScript();
        db.execSQL(sqlQuery);
        preLoadFinancialInstitutions(db);

        // Create Table: VslaInfo
        sqlQuery = VslaInfoSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: Members
        sqlQuery = MemberSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: VslaCycles
        sqlQuery = VslaCycleSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: Meetings
        sqlQuery = MeetingSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: Attendance
        sqlQuery = AttendanceSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: Savings
        sqlQuery = SavingSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create table: Welfare
        sqlQuery = WelfareSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: LoanIssues
        sqlQuery = LoanIssueSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: LoanRepayments
        sqlQuery = LoanRepaymentSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: FineTypes
        sqlQuery = FineTypeSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        // Create Table: Fines
        sqlQuery = FineSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        //Create Table: TrainingModule
        sqlQuery = TrainingModuleSchema.getCreateTableScript();
        db.execSQL(sqlQuery);
        preLoadTrainingModule(db);

        //Create Table: TrainingModuleResponse
        sqlQuery = TrainingModuleResponseSchema.getCreateTableScript();
        db.execSQL(sqlQuery);

        sqlQuery = MessageChannelsSchema.getCreateTableScript();
        db.execSQL(sqlQuery);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //If the database already exists

        //Create Table: TrainingModule
        String sqlQuery = null;
        if(!this.hasDbTable(db, TrainingModuleSchema.getTableName())) {
            sqlQuery = TrainingModuleSchema.getCreateTableScript();
            db.execSQL(sqlQuery);
            Log.e("DatabaseHandler", "Table TrainingModule Added");
            preLoadTrainingModule(db);
        }

        //Create Table: TrainingModuleResponse
        if(!this.hasDbTable(db, TrainingModuleResponseSchema.getTableName())) {
            sqlQuery = TrainingModuleResponseSchema.getCreateTableScript();
            db.execSQL(sqlQuery);
            Log.e("DatabaseHandler", "Table TrainingModuleResponse Added");
        }

        //Create table: FinancialInstitution
        if(!this.hasDbTable(db, FinancialInstitutionSchema.getTableName())){
            sqlQuery = FinancialInstitutionSchema.getCreateTableScript();
            db.execSQL(sqlQuery);
            Log.e("DatabaseHandler", "Table FinancialInstitution Added");
            preLoadFinancialInstitutions(db);
        }

        //Create table: welfare
        if(!this.hasDbTable(db, WelfareSchema.getTableName())){
            sqlQuery = WelfareSchema.getCreateTableScript();
            db.execSQL(sqlQuery);
            Log.e("DatabaseHandler", "Table Welfare Added");
        }

        //Add columns to the meeting table
        if(!this.hasTableColumn(db, MeetingSchema.getTableName(), "BankLoanRepayment"))
            db.execSQL(MeetingSchema.addColumnBankLoanRepayment());
        if(!this.hasTableColumn(db, MeetingSchema.getTableName(), "LoanFromBank"))
            db.execSQL(MeetingSchema.addColumnLoanFromBank());

        //Create Table: MessageChannels
        sqlQuery = MessageChannelsSchema.getCreateTableScript();
        db.execSQL(sqlQuery);
        Log.e("DatabaseHandler", "Table MessageChannels Added");
    }

    protected boolean hasDbTable(SQLiteDatabase db, String tableName){
        String sqlQuery = "select distinct tbl_name from sqlite_master where tbl_name = ?";
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{tableName});
        if(cursor.getCount() > 0) {
            cursor.moveToNext();
            String nTableName = cursor.getString(0);
            cursor.close();
            if (nTableName.length() > 0) {
                if (nTableName.equals(tableName)) {
                    return true;
                }
            }
        }
        return  false;
    }

    protected boolean hasTableColumn(SQLiteDatabase db, String tableName, String columnName){
        boolean hasTableColumn = false;
        String sqlQuery = String.format("select * from %s limit 1", tableName);
        Cursor cursor = db.rawQuery(sqlQuery, null);
        String[] columnNames = cursor.getColumnNames();
        for(int i = 0; i < columnNames.length; i++){
            if(columnNames[i].equals(columnName)){
                hasTableColumn = true;
            }
        }
        cursor.close();
        return hasTableColumn;
    }

    protected void preLoadTrainingModule(SQLiteDatabase db){
        db.execSQL("delete from TrainingModule");
        db.execSQL("delete from sqlite_sequence where name = 'TrainingModule'");
        db.execSQL("Insert into TrainingModule (module) values (?)", new String[]{"Ledger Link Training"});
        db.execSQL("Insert into TrainingModule (module) values (?)", new String[]{"eKeys Training"});
        db.execSQL("Insert into TrainingModule (module) values (?)", new String[]{"General Support"});
        db.execSQL("Insert into TrainingModule (module) values (?)", new String[]{"Refresher Training"});
        Log.e("DatabaseHandler", "Preloaded TrainingModule with data");
    }

    protected void preLoadFinancialInstitutions(SQLiteDatabase db){
        db.execSQL("delete from " + FinancialInstitutionSchema.getTableName());
        db.execSQL("delete from sqlite_sequence where name = '" + FinancialInstitutionSchema.getTableName() + "'");
        db.execSQL("insert into " + FinancialInstitutionSchema.getTableName() + " (Name, Code, IpAddress) values (?, ?, ?)", new String[]{"Post Bank Uganda", "POST_BANK_UGANDA", "154.0.139.218:9008"});
        db.execSQL("insert into " + FinancialInstitutionSchema.getTableName() + " (Name, Code, IpAddress) values (?, ?, ?)", new String[]{"Centenary Rural Development Bank", "CENTENARY_RURAL_DEVELOPMENT_BANK", "154.0.139.218:9008"});
        db.execSQL("insert into " + FinancialInstitutionSchema.getTableName() + " (Name, Code, IpAddress) values (?, ?, ?)", new String[]{"Finca Uganda Limited", "FINCA_UGANDA_LIMITED", "154.0.139.218:9008"});
        db.execSQL("insert into " + FinancialInstitutionSchema.getTableName() + " (Name, Code, IpAddress) values (?, ?, ?)", new String[]{"Opportunity Bank", "OPPORTUNITY_BANK", "154.0.139.218:9008"});
        db.execSQL("insert into " + FinancialInstitutionSchema.getTableName() + " (Name, Code, IpAddress) values (?, ?, ?)", new String[]{"Rural Finance Initiative", "RURAL_FINANCE_INITIATIVE", "154.0.139.218:9008"});
        Log.e("DatabaseHandler", "Preloaded Financial Institutions");
    }

    public static DatabaseHandler getInstance(Context context) {
        return new DatabaseHandler(context);
    }
}
