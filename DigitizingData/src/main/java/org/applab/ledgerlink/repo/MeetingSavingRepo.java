package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.datatransformation.SavingsDataTransferRecord;
import org.applab.ledgerlink.domain.model.MeetingSaving;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.domain.schema.SavingSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.MemberSavingRecord;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 7/7/13.
 */
public class MeetingSavingRepo {
    private final Context context;

    public MeetingSavingRepo(Context context){
        this.context = context;
    }

    int getMemberSavingId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int savingId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    SavingSchema.COL_S_SAVING_ID, SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEETING_ID, meetingId,
                    SavingSchema.COL_S_MEMBER_ID, memberId, SavingSchema.COL_S_SAVING_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                savingId = cursor.getInt(cursor.getColumnIndex(SavingSchema.COL_S_SAVING_ID));
            }
            return savingId;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMemberSavingId", ex.getMessage());
            return savingId;
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

    public MeetingSaving getMemberSavingAndComment(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingSaving saving = new MeetingSaving();

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT %s, %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    SavingSchema.COL_S_AMOUNT,
                    SavingSchema.COL_S_SAVINGS_AT_SETUP_CORRECTION_COMMENT,
                    SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEETING_ID, meetingId,
                    SavingSchema.COL_S_MEMBER_ID, memberId, SavingSchema.COL_S_SAVING_ID);
            cursor = db.rawQuery(query, null);


            //double saving = 0.0;
            if (cursor != null && cursor.moveToFirst()) {
                saving.setAmount(cursor.getDouble(cursor.getColumnIndex(SavingSchema.COL_S_AMOUNT)));
                saving.setComment(cursor.getString(cursor.getColumnIndex(SavingSchema.COL_S_SAVINGS_AT_SETUP_CORRECTION_COMMENT)));
            }
            return saving;
        }
        catch (Exception ex) {

            Log.e("MeetingSavingRepo.getMemberSavingAndComment", ex.getMessage());
            ex.printStackTrace();
            return saving;
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

    public double getMemberSaving(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    SavingSchema.COL_S_AMOUNT, SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEETING_ID, meetingId,
                    SavingSchema.COL_S_MEMBER_ID, memberId, SavingSchema.COL_S_SAVING_ID);
            cursor = db.rawQuery(query, null);

            double saving = 0.0;
            if (cursor != null && cursor.moveToFirst()) {
                saving = cursor.getDouble(cursor.getColumnIndex(SavingSchema.COL_S_AMOUNT));

            }
            return saving;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMemberSaving", ex.getMessage());
            return 0.0;
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

    public double getMemberTotalSavingsInCycle(int cycleId, int memberId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalSavings = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalSavings FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    SavingSchema.COL_S_AMOUNT, SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEMBER_ID, memberId,
                    SavingSchema.COL_S_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_CYCLE_ID,cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalSavings = cursor.getDouble(cursor.getColumnIndex("TotalSavings"));
            }

            return totalSavings;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMemberTotalSavingsInCycle", ex.getMessage());
            return 0.0;
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

    public double getTotalSavingsInMeeting(int meetingId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalSavings = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalSavings FROM %s WHERE %s=%d",
                    SavingSchema.COL_S_AMOUNT, SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEETING_ID,meetingId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalSavings = cursor.getDouble(cursor.getColumnIndex("TotalSavings"));
            }

            return totalSavings;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MeetingSavingRepo.getTotalSavingsInMeeting", ex.getMessage());
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

    public double getTotalSavingsInCycle(int cycleId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalSavings = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT SUM(%s) AS TotalSavings FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    SavingSchema.COL_S_AMOUNT, SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);

            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalSavings = cursor.getDouble(cursor.getColumnIndex("TotalSavings"));
            }

            return totalSavings;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getTotalSavingsInCycle", "HERE" +ex.getMessage());
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

    public double getTotalSavingsInCycleForPreviousMeeting(int cycleId, int meetingId){
         SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalSavings = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalSavings FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d) AND %s!=%d",
                    SavingSchema.COL_S_AMOUNT, SavingSchema.getTableName(),
                    SavingSchema.COL_S_MEETING_ID,MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_CYCLE_ID,cycleId,
                    SavingSchema.COL_S_MEETING_ID, meetingId);

            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalSavings = cursor.getDouble(cursor.getColumnIndex("TotalSavings"));
            }

            return totalSavings;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getTotalSavingsInCycleForPreviousMeeting", ex.getMessage());
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

    public boolean saveMemberSaving(int meetingId, int memberId, double amount, String comment) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int savingId = 0;
        try {
            //Check if exists and do an Update
            savingId = getMemberSavingId(meetingId, memberId);
            if(savingId > 0) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(SavingSchema.COL_S_MEETING_ID, meetingId);
            values.put(SavingSchema.COL_S_MEMBER_ID, memberId);
            values.put(SavingSchema.COL_S_AMOUNT, amount);
            values.put(SavingSchema.COL_S_SAVINGS_AT_SETUP_CORRECTION_COMMENT, comment);

            // Inserting or UpdatingRow
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(SavingSchema.getTableName(), values, SavingSchema.COL_S_SAVING_ID + " = ?",
                        new String[] { String.valueOf(savingId) });
            }
            else {
                retVal = db.insert(SavingSchema.getTableName(), null, values);
            }

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("MemberSavingRepo.saveMemberSaving", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<MemberSavingRecord> getMemberSavingHistoryInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<MemberSavingRecord> savings;

        try {
            savings = new ArrayList<MemberSavingRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            //TODO: I don't think I need the Sub-Query: can do Meetings.CycleId = xx
            String query = String.format("SELECT  %s.%s AS SavingId, %s.%s AS MeetingDate, %s.%s AS Amount " +
                    " FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s.%s DESC",
                    SavingSchema.getTableName(),SavingSchema.COL_S_SAVING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_MEETING_DATE,
                    SavingSchema.getTableName(), SavingSchema.COL_S_AMOUNT,
                    SavingSchema.getTableName(), MeetingSchema.getTableName(),
                    SavingSchema.getTableName(), SavingSchema.COL_S_MEETING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_MEETING_ID,
                    SavingSchema.getTableName(), SavingSchema.COL_S_MEMBER_ID, memberId,
                    SavingSchema.getTableName(), SavingSchema.COL_S_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    SavingSchema.getTableName(),SavingSchema.COL_S_SAVING_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberSavingRecord saving = new MemberSavingRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    saving.setMeetingDate(meetingDate);
                    saving.setSavingId(cursor.getInt(cursor.getColumnIndex("SavingId")));
                    saving.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));

                    savings.add(saving);

                } while (cursor.moveToNext());
            }
            return savings;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMemberSavingHistoryInCycle", ex.getMessage());
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

    public ArrayList<SavingsDataTransferRecord> getMeetingSavingsForAllMembers(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<SavingsDataTransferRecord> savings;

        try {
            savings = new ArrayList<SavingsDataTransferRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s AS SavingId, %s AS MemberId, %s AS Amount " +
                    " FROM %s WHERE %s=%d ORDER BY %s",
                    SavingSchema.COL_S_SAVING_ID, SavingSchema.COL_S_MEMBER_ID, SavingSchema.COL_S_AMOUNT,
                    SavingSchema.getTableName(), SavingSchema.COL_S_MEETING_ID, meetingId, SavingSchema.COL_S_SAVING_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SavingsDataTransferRecord saving = new SavingsDataTransferRecord();
                    saving.setSavingsId(cursor.getInt(cursor.getColumnIndex("SavingId")));
                    saving.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                    saving.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    saving.setMeetingId(meetingId);

                    savings.add(saving);

                } while (cursor.moveToNext());
            }
            return savings;
        }
        catch (Exception ex) {
            Log.e("MeetingSavingRepo.getMeetingSavingsForAllMembers", ex.getMessage());
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
