package org.applab.ledgerlink.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.applab.ledgerlink.domain.model.TrainingModule;
import org.applab.ledgerlink.domain.schema.AttendanceSchema;
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
import org.applab.ledgerlink.repo.TrainingModuleRepo;

import java.io.File;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String EXTERNAL_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = "ledgerlinkdb";
    private static final int DATABASE_VERSION = 55;
    private static final String TRAINING_DATABASE_NAME = "ledgerlinktraindb";
    private static final String DATA_FOLDER = "LedgerLink";

    public static Context databaseContext = null;

    private DatabaseHandler(Context context) {
        //super(context, createDatabaseFolder() + ((Utils.getDefaultSharedPreferences(context).getString(SettingsActivity.PREF_KEY_EXECUTION_MODE,"1").equalsIgnoreCase(SettingsActivity.PREF_VALUE_EXECUTION_MODE_TRAINING)) ? TRAINING_DATABASE_NAME : DATABASE_NAME), null, DATABASE_VERSION);
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
            File databaseStorageDir = new File(EXTERNAL_STORAGE_LOCATION + File.separator + DATA_FOLDER);
            if(! databaseStorageDir.exists()) {
                //create it
                boolean mkdir = databaseStorageDir.mkdir();
                if(mkdir) {
                    Log.d("DatabaseHandler.createDatabaseFolder", "Data folder "+databaseStorageDir.getAbsolutePath() +" has been created");
                }
                else {
                    Log.d("DatabaseHandler.createDatabaseFolder", "Data folder "+databaseStorageDir.getAbsolutePath() +" failed to be created");
                }
            }
            folderName = databaseStorageDir.getAbsolutePath() + File.separator + ((Utils.getDefaultSharedPreferences(context).getString(SettingsActivity.PREF_KEY_EXECUTION_MODE,"1").equalsIgnoreCase(SettingsActivity.PREF_VALUE_EXECUTION_MODE_TRAINING)) ? TRAINING_DATABASE_NAME : DATABASE_NAME);
        }
        return folderName;
        //return databaseStorageDir.getAbsolutePath() + File.separator;
    }

    public void onCreate(SQLiteDatabase db) {

        String sqlQuery = null;

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
        cursor.moveToNext();
        String nTableName = cursor.getString(0);
        cursor.close();
        if(nTableName.length() > 0){
            if(nTableName.equals(tableName)){
                return true;
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

    public static DatabaseHandler getInstance(Context context) {
        return new DatabaseHandler(context);
    }
}
