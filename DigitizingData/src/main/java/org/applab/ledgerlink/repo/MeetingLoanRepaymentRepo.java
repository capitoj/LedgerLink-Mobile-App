package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.datatransformation.RepaymentDataTransferRecord;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.schema.LoanIssueSchema;
import org.applab.ledgerlink.domain.schema.LoanRepaymentSchema;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.MemberLoanRepaymentRecord;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;
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
            String sumQuery = String.format("SELECT SUM(%s) AS TotalRepayments FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
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

    public String getMemberRepaymentCommentByLoanId(int loanId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String repaymentComment = "";

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_COMMENTS, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_MEETING_ID, loanId,
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                repaymentComment = cursor.getString(cursor.getColumnIndex(LoanRepaymentSchema.COL_LR_COMMENTS));
            }
            return repaymentComment;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getMemberRepaymentId", ex.getMessage());
            return repaymentComment;
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

    int getMemberRepaymentId(int meetingId, int memberId) {
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

    public boolean saveMemberLoanRepayment(int meetingId, int memberId, int loanId, double amount,
                                           double balanceBefore, String comments, double balanceAfter,
                                           double interestAmount, double rolloverAmount, Date lastDateDue, Date nextDateDue) {
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
            values.put(LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, interestAmount);
            values.put(LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, rolloverAmount);

            //Transaction entry of Balance Before and Balance After: May be best done at database layer with locking and syncing
            values.put(LoanRepaymentSchema.COL_LR_BAL_BEFORE, balanceBefore);
            //Balance after will be the balance upon which the rollover was calculated
            values.put(LoanRepaymentSchema.COL_LR_BAL_AFTER, balanceAfter);

            //The Last Date Due
            Date dtLastDateDue = lastDateDue;
            if(dtLastDateDue == null) {
                Calendar cal = Calendar.getInstance();
                dtLastDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, Utils.formatDateToSqlite(dtLastDateDue));

            //The Next Date Due
            Date dtNextDateDue = nextDateDue;
            if(dtNextDateDue == null) {
                Calendar cal = Calendar.getInstance();
                //cal.add(Calendar.MONTH,1);
                cal.add(Calendar.WEEK_OF_YEAR,4);
                dtNextDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE, Utils.formatDateToSqlite(dtNextDateDue));

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

            //Now update the lastDateDue and nextDateDue
//boolean retValDates = updateMemberLoanRepaymentDates(meetingId, memberId, lastDateDue, nextDateDue);
            return retVal != -1;
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

    public boolean updateMemberLoanRepaymentDates(int meetingId, int memberId, Date lastDateDue, Date nextDateDue) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int repaymentId = 0;
        try {
            // Check if exists and do an Update:
            repaymentId = getMemberRepaymentId(meetingId, memberId);
            if(repaymentId > 0) {
                performUpdate = true;
            }
            else {
                return false;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            // The Last Date Due
            Date dtLastDateDue = lastDateDue;
            if(dtLastDateDue == null) {
                Calendar cal = Calendar.getInstance();
                dtLastDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, Utils.formatDateToSqlite(dtLastDateDue));

            //The Next Date Due
            Date dtNextDateDue = nextDateDue;
            if(dtNextDateDue == null) {
                Calendar cal = Calendar.getInstance();
                // cal.add(Calendar.MONTH,1);
                cal.add(Calendar.WEEK_OF_YEAR,4);
                dtNextDateDue = cal.getTime();
            }
            values.put(LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE, Utils.formatDateToSqlite(dtNextDateDue));

            // Inserting or Updating Row
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(LoanRepaymentSchema.getTableName(), values, LoanRepaymentSchema.COL_LR_REPAYMENT_ID + " = ?",
                        new String[] { String.valueOf(repaymentId) });
            }

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.updateMemberLoanRepaymentDates", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    //TODO: Update this query to display the added fields
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

    public ArrayList<MemberLoanRepaymentRecord> getLoansRepaymentsByMeetingId(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<MemberLoanRepaymentRecord> repayments;

        try {
            repayments = new ArrayList<MemberLoanRepaymentRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT L.%s AS RepaymentId, M.%s AS MeetingDate, L.%s AS Amount, " +
                    "L.%s AS LoanId, L.%s AS RolloverAmount, L.%s AS Comments, LI.%s AS LoanNo" +
                    " FROM %s AS L INNER JOIN %s AS M ON L.%s=M.%s INNER JOIN %s AS LI ON L.%s=LI.%s "+
                    " WHERE L.%s=%d ORDER BY L.%s DESC",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_MEETING_ID,meetingId,
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
            Log.e("MeetingLoanRepaymentRepo.getLoansRepaymentsByMeetingId", ex.getMessage());
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
        MemberLoanRepaymentRecord repaymentRecord = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  LR.%s AS RepaymentId, M.%s AS MeetingDate, LR.%s AS Amount, " +
                    "LR.%s AS LoanId, LR.%s AS RolloverAmount, LR.%s AS Comments, LI.%s AS LoanNo, LR.%s AS BalanceBefore, " +
                    "LR.%s AS BalanceAfter, LR.%s AS InterestAmount, LR.%s AS LastDateDue , LR.%s AS NextDateDue " +
                    " FROM %s AS LR INNER JOIN %s AS M ON LR.%s=M.%s INNER JOIN %s AS LI ON LR.%s=LI.%s " +
                    " WHERE LR.%s=%d AND LR.%s=%d ORDER BY LR.%s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.COL_LR_BAL_BEFORE, LoanRepaymentSchema.COL_LR_BAL_AFTER,
                    LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE,
                    LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
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
                if(!cursor.isNull(cursor.getColumnIndex("LastDateDue"))){
                    Date lastDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("LastDateDue")));
                    repaymentRecord.setLastDateDue(lastDateDue);
                }
                if(!cursor.isNull(cursor.getColumnIndex("NextDateDue"))){
                    Date nextDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDateDue")));
                    repaymentRecord.setNextDateDue(nextDateDue);
                }
                repaymentRecord.setBalanceBefore(cursor.getDouble(cursor.getColumnIndex("BalanceBefore")));
                repaymentRecord.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("BalanceAfter")));
                repaymentRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

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

    /**
     * Returns a Loan Repayment Record. Useful when dealing with deleting, reversing or editing an individual repayment
     * @param repaymentId
     * @return
     */
    public MemberLoanRepaymentRecord getLoansRepaymentByRepaymentId(int repaymentId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MemberLoanRepaymentRecord repaymentRecord = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  LR.%s AS RepaymentId, M.%s AS MeetingDate, LR.%s AS Amount, " +
                    "LR.%s AS LoanId, LR.%s AS RolloverAmount, LR.%s AS Comments, LI.%s AS LoanNo, LR.%s AS BalanceBefore, " +
                    "LR.%s AS BalanceAfter, LR.%s AS InterestAmount, LR.%s AS LastDateDue , LR.%s AS NextDateDue " +
                    " FROM %s AS LR INNER JOIN %s AS M ON LR.%s=M.%s INNER JOIN %s AS LI ON LR.%s=LI.%s " +
                    " WHERE LR.%s=%d LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.COL_LR_BAL_BEFORE, LoanRepaymentSchema.COL_LR_BAL_AFTER,
                    LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE,
                    LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_REPAYMENT_ID,repaymentId
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
                if(!cursor.isNull(cursor.getColumnIndex("LastDateDue"))){
                    Date lastDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("LastDateDue")));
                    repaymentRecord.setLastDateDue(lastDateDue);
                }
                if(!cursor.isNull(cursor.getColumnIndex("NextDateDue"))){
                    Date nextDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDateDue")));
                    repaymentRecord.setNextDateDue(nextDateDue);
                }
                repaymentRecord.setBalanceBefore(cursor.getDouble(cursor.getColumnIndex("BalanceBefore")));
                repaymentRecord.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("BalanceAfter")));
                repaymentRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

            }
            return repaymentRecord;
        }
        catch (Exception ex) {
            Log.e("MeetingLoanRepaymentRepo.getLoansRepaymentsByRepaymentId", ex.getMessage());
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

    /**
     * Retrieve Repayments made by all members in a meeting. This is for Data Transfer to the bank.
     * @param meetingId
     * @return
     */
    public ArrayList<RepaymentDataTransferRecord> getMeetingRepaymentsForAllMembers(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<RepaymentDataTransferRecord> repayments;

        try {
            repayments = new ArrayList<RepaymentDataTransferRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s AS RepaymentId, %s AS MemberId, %s AS LoanId, %s AS Amount, " +
                    " %s AS BalanceBefore, %s AS BalanceAfter, %s AS InterestAmount, %s AS RollOverAmount, " +
                    " %s AS LastDateDue, %s AS NextDateDue, %s AS Comments " +
                    " FROM %s WHERE %s=%d ORDER BY %s",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID, LoanRepaymentSchema.COL_LR_LOAN_ID,
                    LoanRepaymentSchema.COL_LR_AMOUNT, LoanRepaymentSchema.COL_LR_BAL_BEFORE, LoanRepaymentSchema.COL_LR_BAL_AFTER,
                    LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE,LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanRepaymentSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID, meetingId, LoanRepaymentSchema.COL_LR_REPAYMENT_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RepaymentDataTransferRecord repaymentRecord = new RepaymentDataTransferRecord();
                    repaymentRecord.setMeetingId(meetingId);
                    repaymentRecord.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                    repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    repaymentRecord.setRollOverAmount(cursor.getDouble(cursor.getColumnIndex("RollOverAmount")));
                    repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                    repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));
                    if(!cursor.isNull(cursor.getColumnIndex("LastDateDue"))){
                        Date lastDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("LastDateDue")));
                        repaymentRecord.setLastDateDue(lastDateDue);
                    }
                    if(!cursor.isNull(cursor.getColumnIndex("NextDateDue"))){
                        Date nextDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDateDue")));
                        repaymentRecord.setNextDateDue(nextDateDue);
                    }
                    repaymentRecord.setBalanceBefore(cursor.getDouble(cursor.getColumnIndex("BalanceBefore")));
                    repaymentRecord.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("BalanceAfter")));
                    repaymentRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                    repayments.add(repaymentRecord);

                } while (cursor.moveToNext());
            }
            return repayments;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMeetingRepaymentsForAllMembers", ex.getMessage());
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

    /**
     * Reverses a Loan Repayment. Requires that the target be the most recent repayment
     * @param repaymentId
     * @return
     */
    public boolean reverseLoanRepayment(int repaymentId, String meetingDate) {

        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            int affectedRows = 0;

            //Retrieve the repayment record
            MemberLoanRepaymentRecord repaymentRecord = getLoansRepaymentByRepaymentId(repaymentId);

            if(null == repaymentRecord) {
                return false;
            }

            //Reverse the Repayment: First Retrieve the Loan then
            //1. Add the repayment amount to the loan balance
            int targetLoanId = repaymentRecord.getLoanId();
            MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(DatabaseHandler.databaseContext);
            MeetingLoanIssued targetLoan = loanIssuedRepo.getLoanIssuedByLoanId(targetLoanId);

            if(null == targetLoan) {
                return false;
            }
            double revertedLoanBalance = targetLoan.getLoanBalance() + repaymentRecord.getAmount();
            double revertedTotalPaid = targetLoan.getTotalRepaid() - repaymentRecord.getAmount();
            Date revertedDateDue = repaymentRecord.getLastDateDue();
            //boolean revertedBalancesSuccessfully = loanIssuedRepo.updateMemberLoanBalances(targetLoanId, revertedTotalPaid, revertedLoanBalance, revertedDateDue, meetingDate);
            boolean revertedBalancesSuccessfully = loanIssuedRepo.updateMemberLoanBalancesWithMeetingDate(targetLoanId, revertedTotalPaid, revertedLoanBalance, revertedDateDue, meetingDate);

            if(!revertedBalancesSuccessfully) {
                return false;
            }

            // Delete the Loan Repayment
            affectedRows = db.delete(LoanRepaymentSchema.getTableName(), LoanRepaymentSchema.COL_LR_REPAYMENT_ID + " = ?",
                    new String[] {String.valueOf(repaymentId)});

            return affectedRows > 0;

        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Reverses Loan Repayments made in a meeting.
     * @param meetingId
     * @return
     */
    public boolean reverseLoanRepaymentsForMeeting(int meetingId, String meetingDate) {

        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            int affectedRows = 0;

            ArrayList<MemberLoanRepaymentRecord> repayments = getLoansRepaymentsByMeetingId(meetingId);

            if(null == repayments) {
                return false;
            }

            //Loop through all the repayments and reverse each individual repayment.
            //TODO: This can be done better by overloading the method to use the repaymentrecord instead of repaymentid
            for(MemberLoanRepaymentRecord repaymentRecord : repayments){
                reverseLoanRepayment(repaymentRecord.getRepaymentId(), meetingDate);
            }

            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public MemberLoanRepaymentRecord getMemberLoanRepaymentRecord(int memberId) {
        MemberLoanRepaymentRecord repaymentRecord = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  LR.%s AS RepaymentId, M.%s AS MeetingDate, LR.%s AS Amount, " +
                            "LR.%s AS LoanId, LR.%s AS RolloverAmount, LR.%s AS Comments, LI.%s AS LoanNo, LR.%s AS BalanceBefore, " +
                            "LR.%s AS BalanceAfter, LR.%s AS InterestAmount, LR.%s AS LastDateDue , LR.%s AS NextDateDue " +
                            " FROM %s AS LR INNER JOIN %s AS M ON LR.%s=M.%s INNER JOIN %s AS LI ON LR.%s=LI.%s " +
                            " WHERE LR.%s=%d ORDER BY LR.%s DESC LIMIT 1",
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID,MeetingSchema.COL_MT_MEETING_DATE, LoanRepaymentSchema.COL_LR_AMOUNT,
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanRepaymentSchema.COL_LR_ROLLOVER_AMOUNT, LoanRepaymentSchema.COL_LR_COMMENTS,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanRepaymentSchema.COL_LR_BAL_BEFORE, LoanRepaymentSchema.COL_LR_BAL_AFTER,
                    LoanRepaymentSchema.COL_LR_INTEREST_AMOUNT, LoanRepaymentSchema.COL_LR_LAST_DATE_DUE, LoanRepaymentSchema.COL_LR_NEXT_DATE_DUE,
                    LoanRepaymentSchema.getTableName(), MeetingSchema.getTableName(), LoanRepaymentSchema.COL_LR_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.getTableName(), LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_MEMBER_ID,memberId,
                    LoanRepaymentSchema.COL_LR_REPAYMENT_ID
            );
            cursor = db.rawQuery(query, null);
            Log.d("MLRAA", "HERE18");
            if (cursor != null && cursor.moveToFirst()) {
                Log.d("MLRAA", "HERE19");
                repaymentRecord = new MemberLoanRepaymentRecord();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                repaymentRecord.setMeetingDate(meetingDate);
                repaymentRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                repaymentRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                repaymentRecord.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                repaymentRecord.setRolloverAmount(cursor.getDouble(cursor.getColumnIndex("RolloverAmount")));
                repaymentRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                repaymentRecord.setRepaymentId(cursor.getInt(cursor.getColumnIndex("RepaymentId")));
                if(!cursor.isNull(cursor.getColumnIndex("LastDateDue"))){
                    Date lastDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("LastDateDue")));
                    repaymentRecord.setLastDateDue(lastDateDue);
                }
                if(!cursor.isNull(cursor.getColumnIndex("NextDateDue"))){
                    Log.d("MLRAA", "HERE20");
                    Date nextDateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDateDue")));
                    repaymentRecord.setNextDateDue(nextDateDue);
                }
                repaymentRecord.setBalanceBefore(cursor.getDouble(cursor.getColumnIndex("BalanceBefore")));
                repaymentRecord.setBalanceAfter(cursor.getDouble(cursor.getColumnIndex("BalanceAfter")));
                repaymentRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

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
