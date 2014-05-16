package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.schema.AttendanceSchema;
import org.applab.digitizingdata.domain.schema.FineSchema;
import org.applab.digitizingdata.domain.schema.LoanIssueSchema;
import org.applab.digitizingdata.domain.schema.LoanRepaymentSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.domain.schema.MemberSchema;
import org.applab.digitizingdata.domain.schema.SavingSchema;
import org.applab.digitizingdata.domain.schema.VslaCycleSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Moses on 7/4/13.
 */
public class MeetingRepo {
    private Context context;

    public MeetingRepo() {

    }

    public MeetingRepo(Context context){
        this.context = context;
    }

    public boolean addMeeting(Meeting meeting){
        SQLiteDatabase db = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            if(meeting.getMeetingDate() == null) {
                meeting.setMeetingDate(new Date());
            }
            values.put(MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDateToSqlite(meeting.getMeetingDate()));
            if(meeting.getVslaCycle() != null){
                values.put(MeetingSchema.COL_MT_CYCLE_ID, meeting.getVslaCycle().getCycleId());
            }
            // Inserting Row
            long retVal = db.insert(MeetingSchema.getTableName(), null, values);

            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MemberRepo.addMember", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateOpeningCash(int meetingId, double cashFromBox, double cashFromBank, double finesPaid) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_CASH_FROM_BOX, cashFromBox);
            values.put(MeetingSchema.COL_MT_CASH_FROM_BANK, cashFromBank);
            values.put(MeetingSchema.COL_MT_CASH_FINES, finesPaid);

            long retVal = -1;
            retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[] { String.valueOf(meetingId) });
            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.updateOpeningCash", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateCashBook(int meetingId, double cashWelfare, double cashExpenses, double cashSavedBox, double cashSavedBank) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_CASH_WELFARE, cashWelfare);
            values.put(MeetingSchema.COL_MT_CASH_EXPENSES, cashExpenses);
            values.put(MeetingSchema.COL_MT_CASH_FROM_BOX, cashSavedBox);
            values.put(MeetingSchema.COL_MT_CASH_FROM_BANK, cashSavedBank);

            long retVal = -1;
            retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[] { String.valueOf(meetingId) });
            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.updateCashBook", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateDataSentFlag(int meetingId, boolean isDataSent, Date dateSent) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_IS_DATA_SENT, (isDataSent)?1:0);
            if(isDataSent) {
                if(dateSent == null) {
                    dateSent = new Date();
                }
                values.put(MeetingSchema.COL_MT_DATE_SENT, Utils.formatDateToSqlite(dateSent));
            }

            long retVal = -1;
            retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[] { String.valueOf(meetingId) });
            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.updateDataSentFlag", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<Meeting> getAllMeetings() {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    }
                    else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getAllMeetings", ex.getMessage());
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

    public ArrayList<Meeting> getAllMeetingsOfCycle(int targetCycleId) {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, targetCycleId, MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    }
                    else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeeting", ex.getMessage());
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

    public ArrayList<Meeting> getAllMeetingsByDataSentStatus(boolean isDataSent) {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int dataSentFlag = 0;
        VslaCycleRepo cycleRepo = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            if(isDataSent) {
                dataSentFlag = 1;
            }

            cycleRepo = new VslaCycleRepo(context);

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE COALESCE(%s,0)=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_IS_DATA_SENT, dataSentFlag, MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    }
                    else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeeting", ex.getMessage());
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

    public HashMap<String, Double> getMeetingOpeningCash(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        HashMap<String, Double> openingCash = new HashMap<String, Double>();

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s FROM %s WHERE %s=%d",
                    MeetingSchema.COL_MT_CASH_FROM_BOX, MeetingSchema.COL_MT_CASH_FROM_BOX,
                    MeetingSchema.COL_MT_CASH_FROM_BANK, MeetingSchema.COL_MT_CASH_FROM_BANK,
                    MeetingSchema.COL_MT_CASH_FINES, MeetingSchema.COL_MT_CASH_FINES,
                    MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                openingCash.put(MeetingSchema.COL_MT_CASH_FROM_BOX, cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX)));
                openingCash.put(MeetingSchema.COL_MT_CASH_FROM_BANK, cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BANK)));
                openingCash.put(MeetingSchema.COL_MT_CASH_FINES, cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FINES)));
                return openingCash;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingOpeningCash", ex.getMessage());
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

    public double getMeetingTotalOpeningCash(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT (IFNULL(%s,0) + IFNULL(%s,0) + IFNULL(%s,0)) AS TotalOpeningCash FROM %s WHERE %s=%d",
                    MeetingSchema.COL_MT_CASH_FROM_BOX, MeetingSchema.COL_MT_CASH_FROM_BANK, MeetingSchema.COL_MT_CASH_FINES,
                    MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getDouble(cursor.getColumnIndex("TotalOpeningCash"));
            }
            else {
                return 0.0;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingTotalOpeningCash", ex.getMessage());
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

    public Meeting getMeetingById(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                }
                else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingById", ex.getMessage());
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

    public Meeting getMeetingByDate(Date theMeetingDate) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE date(%s)='%s' ORDER BY %s DESC LIMIT 1", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDate(theMeetingDate,"yyyy-MM-dd"),
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                }
                else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingByDate", ex.getMessage());
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
     * Retrieves a Meeting by Date from a particular Vsla Cycle
     * @param theMeetingDate
     * @param vslaCycleId
     * @return
     */
    public Meeting getMeetingByDate(Date theMeetingDate, int vslaCycleId ) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE date(%s)='%s' AND %s=%d ORDER BY %s DESC LIMIT 1", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDate(theMeetingDate,"yyyy-MM-dd"),
                    MeetingSchema.COL_MT_CYCLE_ID, vslaCycleId,
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                }
                else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingByDate", ex.getMessage());
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

    public Meeting getPreviousMeeting(int vslaCycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC LIMIT 2", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, vslaCycleId,
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            //Get the second row
            if (cursor != null && cursor.moveToFirst()) {
                if(cursor.getCount() < 2) {
                    return null;
                }
                if(cursor.moveToLast()) {
                    meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    }
                    else {
                        meeting.setMeetingDataSent(false);
                    }
                    return meeting;
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getPreviousMeeting", ex.getMessage());
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

    public Meeting getCurrentMeeting(int vslaCycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC LIMIT 1", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, vslaCycleId,
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                }
                else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getCurrentMeeting", ex.getMessage());
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

    public Meeting getMostRecentMeeting() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                }
                else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMostRecentMeeting", ex.getMessage());
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

    public Meeting getMostRecentMeetingInCycle(int currentCycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC LIMIT 1", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, currentCycleId, MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                }
                else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            }
            else {
                return null;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.getMostRecentMeeting", ex.getMessage());
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
     * This will deactivate all other meetings and activate the one passed to make it the current meeting.
     * @param meeting
     * @return
     */
    public boolean activateMeeting(Meeting meeting) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;

        try {
            if(meeting == null) {
                return false;
            }
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_MEETING_ID,0);

            // updating row:
            int retVal = db.update(MeetingSchema.getTableName(), values, null,null);

            if (retVal > 0) {

                //Update the specific one to Active
                values.clear();
                values.put(MeetingSchema.COL_MT_MEETING_ID,1);
                int retVal2 = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                        new String[] { String.valueOf(meeting.getMeetingId()) });
                if(retVal2 > 0) {
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MeetingRepo.activateMeeting", ex.getMessage());
            return false;
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

    // Deleting single entity
    public boolean deleteMeeting(int meetingId) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            int affectedRows = 0;

            //Do a Transaction
            db.beginTransaction(); //This will create a database Lock
            //db.beginTransactionNonExclusive();  //Non-exclusive lock, but requires API Level 11

            //Delete Attendance for the target meeting
            affectedRows = db.delete(AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEETING_ID + " = ?",
                    new String[] {String.valueOf(meetingId)});

            //Delete Savings for the target meeting
            affectedRows = db.delete(SavingSchema.getTableName(), SavingSchema.COL_S_MEETING_ID + " = ?",
                    new String[] {String.valueOf(meetingId)});

            //Delete Fines for the target meeting
            affectedRows = db.delete(FineSchema.getTableName(), FineSchema.COL_F_MEETING_ID + " = ?",
                    new String[] {String.valueOf(meetingId)});

            //Delete Loan Issues for the target meeting
            affectedRows = db.delete(LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_MEETING_ID + " = ?",
                    new String[] {String.valueOf(meetingId)});

            //TODO: Ending the Transaction pre-maturely before reverting Repayments due to Database Lock issues
            //TODO: Will re-visit this later to ensure reverting of Repayments and Deletion of Meeting are in the same Transaction scope
            db.setTransactionSuccessful();
            db.endTransaction();

            //Delete Loan Repayments for the target meeting
            //Reverse Loan Repayments will do it in a clean manner
            //NB: Calling this within a transaction and avoiding Database Lock requires API Level 11
            MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(DatabaseHandler.databaseContext);
            boolean repaymentsReversedSuccessfully = repaymentRepo.reverseLoanRepaymentsForMeeting(meetingId);

            //Reversal of repayments returns false only when a problem occurs, it will return True in cases where there were no repayments
            if(!repaymentsReversedSuccessfully) {
                return false;
            }

            //Now Delete the Meeting
            // To remove all rows and get a count pass "1" as the whereClause.
            affectedRows = db.delete(MeetingSchema.getTableName(), MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meetingId)});

            if(affectedRows >= 0) {
                return true;
            }
            else {
                return false;
            }

        }
        catch (Exception ex) {
            Log.e("MeetingRepo.deleteMeeting", ex.getMessage());
            db.endTransaction();
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
