package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.applab.digitizingdata.domain.schema.*;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "digitizedata";
    public static final int DATABASE_VERSION = 13;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
