package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.os.Environment;
import android.util.Log;
import org.applab.digitizingdata.SettingsActivity;
import org.applab.digitizingdata.domain.schema.*;

import java.io.File;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String EXTERNAL_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = "ledgerlinkdb";
    public static final int DATABASE_VERSION = 22;
    public static final String TRAINING_DATABASE_NAME = "ledgerlinktraindb";
    public static final String DATA_FOLDER = "LedgerLink";

    public static Context databaseContext = null;

    public DatabaseHandler(Context context) {
        super(context, createDatabaseFolder() + ((Utils.getDefaultSharedPreferences(context).getString(SettingsActivity.PREF_KEY_EXECUTION_MODE,"1").equalsIgnoreCase(SettingsActivity.PREF_VALUE_EXECUTION_MODE_TRAINING)) ? TRAINING_DATABASE_NAME : DATABASE_NAME),
                null, DATABASE_VERSION);
        databaseContext = context;
    }

    public static String createDatabaseFolder() {
        //creates the database folders and returns path as string
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
        return databaseStorageDir.getAbsolutePath() + File.separator;
    }

    public void onCreate(SQLiteDatabase db) {

        String sqlQuery = null;
        StringBuffer sb = null;

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

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop and Recreate the Tables - By calling onCreate()

        //Table: VslaInfo
        db.execSQL(VslaInfoSchema.getDropTableScript());

        //Table: Members
        db.execSQL(MemberSchema.getDropTableScript());

        //Table: VslaCycle
        db.execSQL(VslaCycleSchema.getDropTableScript());

        //Table: Meetings
        db.execSQL(MeetingSchema.getDropTableScript());

        //Table: Attendance
        db.execSQL(AttendanceSchema.getDropTableScript());

        //Table: Savings
        db.execSQL(SavingSchema.getDropTableScript());

        //Table: LoanIssues
        db.execSQL(LoanIssueSchema.getDropTableScript());

        //Table: LoanRepayments
        db.execSQL(LoanRepaymentSchema.getDropTableScript());

//        db.execSQL("DROP TRIGGER IF EXISTS dept_id_trigger");
//        db.execSQL("DROP TRIGGER IF EXISTS dept_id_trigger22");
//        db.execSQL("DROP TRIGGER IF EXISTS fk_empdept_deptid");
//        db.execSQL("DROP VIEW IF EXISTS " + viewEmps);
        onCreate(db);
    }

    public static DatabaseHandler getInstance(Context context) {
        return new DatabaseHandler(context);
    }
}
