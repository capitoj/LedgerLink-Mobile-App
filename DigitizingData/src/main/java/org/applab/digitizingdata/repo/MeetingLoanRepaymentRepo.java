package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.domain.schema.LoanIssueSchema;
import org.applab.digitizingdata.domain.schema.LoanRepaymentSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.MemberLoanRepaymentRecord;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 7/9/13.
 */
public class MeetingLoanRepaymentRepo {
    private Context context;
    public MeetingLoanRepaymentRepo() {

    }

    public MeetingLoanRepaymentRepo(Context context){
        this.context = context;
    }

    public double getTotalLoansRepaidInCycle(int cycleId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansRepaid = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalRepayments FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID,cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansRepaid = cursor.getDouble(cursor.getColumnIndex("TotalRepayments"));
            }

            return loansRepaid;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getTotalLoansRepaidInCycle", ex.getMessage());
            return 0;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalLoansRepaidInMeeting(int meetingId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansRepaid = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalRepayments FROM %s WHERE %s=%d",
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansRepaid = cursor.getDouble(cursor.getColumnIndex("TotalRepayments"));
            }

            return loansRepaid;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getTotalLoansRepaidInMeeting", ex.getMessage());
            return 0;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalRepaymentByMemberInMeeting(int meetingId, int memberId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansRepaid = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalRepayments FROM %s WHERE %s=%d AND %s=%d",
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId,
                    LoanRepaymentSchema.COL_LR_MEMBER_ID, memberId
            );
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansRepaid = cursor.getDouble(cursor.getColumnIndex("TotalRepayments"));
            }

            return loansRepaid;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getTotalRepaymentByMemberInMeeting", ex.getMessage());
            return 0;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public int getMemberRepaymentId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int repaymentId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId,
                    LoanRepaymentSchema.COL_LR_MEMBER_ID, memberId, LoanRepaymentSchema.COL_LR_REPAYMENT_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                repaymentId = cursor.getInt(cursor.getColumnIndex(LoanRepaymentSchema.COL_LR_REPAYMENT_ID));
            }
            return repaymentId;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getMemberRepaymentId", ex.getMessage());
            return repaymentId;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public boolean saveMemberLoanRepayment(int meetingId, int memberId, int loanId, double amount, double balanceBefore, String comments) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int repaymentId = 0;
        try {
            //Check if exists and do an Update:
            repaymentId = getMemberRepaymentId(meetingId, memberId);
            if(repaymentId > 0) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId);
            values.put(LoanRepaymentSchema.COL_LR_MEMBER_ID, memberId);
            values.put(LoanRepaymentSchema.COL_LR_LOAN_ID, loanId);
            values.put(LoanRepaymentSchema.COL_LR_AMOUNT, amount);
            values.put(LoanRepaymentSchema.COL_LR_COMMENTS, comments);

            //Transaction entry of Balance Before and Balance After: May be best done at database layer with locking and syncing
            values.put(LoanRepaymentSchema.COL_LR_BAL_BEFORE, balanceBefore);
            values.put(LoanRepaymentSchema.COL_LR_BAL_AFTER, balanceBefore - amount);

            // Inserting or Updating Row
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(LoanRepaymentSchema.getTableName(), values, LoanRepaymentSchema.COL_LR_REPAYMENT_ID + " = ?",
                        new String[] { String.valueOf(repaymentId) });
            }
            else {
                retVal = db.insert(LoanRepaymentSchema.getTableName(), null, values);
            }

            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.saveMemberLoanRepayment", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<MemberLoanRepaymentRecord> getLoansRepaymentsByMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<MemberLoanRepaymentRecord> repayments;

        try {
            repayments = new ArrayList<MemberLoanRepaymentRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS RepaymentId, M.%s AS MeetingDate, L.%s AS Amount, " +
                    "L.%s AS LoanId, L.%s AS RolloverAmount, L.%s AS Comments, LI.%s AS LoanNo" +
                    " FROM %s AS L INNER JOIN %s AS M ON L.%s=M.%s INNER JOIN %s AS LI ON L.%s=LI.%s " +
                    " WHERE L.%s=%d AND L.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY L.%s DESC",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID,memberId,
                    LoanRepaymentSchema.COL_LR_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberLoanRepaymentRecord repaymentRecord = new MemberLoanRepaymentRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    repaymentRecord.setMeetingDate(meetingDate);
                    repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    repaymentRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                    repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    repaymentRecord.setRolloverAmount(cursor.getDouble(cursor.getColumnIndex("RolloverAmount")));
                    repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                    repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));

                    repayments.add(repaymentRecord);
                } while (cursor.moveToNext());
            }
            return repayments;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getLoansRepaymentsByMemberInCycle", ex.getMessage());
            return null;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MemberLoanRepaymentRecord getLoansRepaymentByMemberInMeeting(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MemberLoanRepaymentRecord repaymentRecord;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS RepaymentId, M.%s AS MeetingDate, L.%s AS Amount, " +
                    "L.%s AS LoanId, L.%s AS RolloverAmount, L.%s AS Comments, LI.%s AS LoanNo" +
                    " FROM %s AS L INNER JOIN %s AS M ON L.%s=M.%s INNER JOIN %s AS LI ON L.%s=LI.%s " +
                    " WHERE L.%s=%d AND L.%s=%d ORDER BY L.%s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID,memberId,
                    LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId, LoanRepaymentSchema.COL_LR_REPAYMENT_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {

                    repaymentRecord = new MemberLoanRepaymentRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    repaymentRecord.setMeetingDate(meetingDate);
                    repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    repaymentRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                    repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    repaymentRecord.setRolloverAmount(cursor.getDouble(cursor.getColumnIndex("RolloverAmount")));
                    repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                    repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));

            }
            return repaymentRecord;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getLoansRepaymentsByMemberInMeeting", ex.getMessage());
            return null;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

}
