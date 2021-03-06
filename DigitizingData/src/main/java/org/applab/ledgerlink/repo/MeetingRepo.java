package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingStartingCash;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Moses on 7/4/13.
 */
public class MeetingRepo {
    private Context context;
    private int meetingID;
    private boolean isLoaded;
    private Meeting meeting;


    public enum MeetingOrderByEnum {
        ORDER_BY_MEETING_ID,
        ORDER_BY_MEETING_DATE
    }

    public MeetingRepo() {

    }

    public MeetingRepo(Context context) {
        this.context = context;
        this.isLoaded = false;
    }

    public MeetingRepo(Context context, int meetingID){
        this.context = context;
        this.meetingID = meetingID;
        this.isLoaded = false;
        this.meeting = new Meeting();
        this.load();
    }

    protected void load(){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("select * from %s where _id = ?", MeetingSchema.getTableName());
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(meetingID)});
            if(cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    isLoaded = true;
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    VslaCycle vslaCycle = new VslaCycle();
                    vslaCycle.setCycleId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID)));
                    meeting.setVslaCycle(vslaCycle);
                    meeting.setMeetingDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE))));
                    meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(context);
                    meeting.setVslaCycle(vslaCycleRepo.getCycle(cycleId));
                    meeting.setOpeningBalanceBox(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX)));
                    meeting.setOpeningBalanceBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BANK)));
                    meeting.setClosingBalanceBox(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BOX)));
                    meeting.setClosingBalanceBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BANK)));
                    meeting.setLoanFromBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_FROM_BANK)));
                    meeting.setBankLoanRepayment(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_BANK_LOAN_REPAYMENT)));
                    meeting.setComment(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT)));

                    if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    } else {
                        meeting.setMeetingDataSent(false);
                    }
                }
            }

        }catch (Exception e){
            Log.e("MeetingRepo.load", e.getMessage());
        }
    }

    public boolean updateStartingCash(double cashFromBox, double cashFromBank, double loanFromBank, String actualStartingCashComment) {
        boolean isUpdated = false;
        try {
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            if (hasMeeting()) {
                String sql = "update Meetings set CashFromBox = ?, CashFromBank = ?, LoanFromBank = ?, CashFromBoxComment = ? where _id = ?";
                db.execSQL(sql, new String[]{String.valueOf(cashFromBox), String.valueOf(cashFromBank), String.valueOf(loanFromBank), actualStartingCashComment, String.valueOf(meetingID)});
                isUpdated = true;
            } else {
                Log.e("UpdateStartingCash", "Meeting does not exist");
            }
        } catch (Exception e) {
            Log.e("UpdateStartingCash", e.getMessage());
        }
        return isUpdated;
    }

    public Meeting getMeeting(){
        return meeting;
    }

    public boolean hasMeeting(){
        return isLoaded;
    }

    //The expected starting cash comes from the previous meeting
    //ToDo: deprecated
    protected double getExpectedStartingCash(){
        double cashSavedToBox = 0.0;
        try {
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "select CashSavedBox from Meetings where _id < ? order by _id desc limit 1";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(meetingID)});
            if (cursor.moveToNext()) {
                cashSavedToBox = cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BOX));
                cursor.close();
                db.close();
            }
        }catch (Exception e){
            Log.e("getExpectedStartedStartingCash", e.getMessage());
        }
        return cashSavedToBox;
    }

    //Get the previous meeting
    public Meeting getPreviousMeeting(int currentCycleId){
        Meeting previousMeeting = new Meeting();
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "select * from Meetings where CycleId = ? order by _id desc limit 1,1";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(currentCycleId)});
            if(cursor.moveToNext()){
                previousMeeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                VslaCycle vslaCycle = new VslaCycle();
                vslaCycle.setCycleId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID)));
                previousMeeting.setVslaCycle(vslaCycle);
                previousMeeting.setMeetingDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE))));
                previousMeeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(context);
                previousMeeting.setVslaCycle(vslaCycleRepo.getCycle(cycleId));
                previousMeeting.setOpeningBalanceBox(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX)));
                previousMeeting.setOpeningBalanceBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BANK)));
                previousMeeting.setClosingBalanceBox(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BOX)));
                previousMeeting.setClosingBalanceBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BANK)));
                previousMeeting.setLoanFromBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_FROM_BANK)));
                previousMeeting.setBankLoanRepayment(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_BANK_LOAN_REPAYMENT)));
                previousMeeting.setComment(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT)));

                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    previousMeeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    previousMeeting.setDateSent(dateMeetingDataSent);
                } else {
                    previousMeeting.setMeetingDataSent(false);
                }
            }
        }catch (Exception e){
            Log.e("getPreviousMeeting", e.getMessage());
        }
        return previousMeeting;
    }

    public void updateCashBook(double cashSavedToBox, double cashSavedToBank, double bankLoanRepayment){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "update Meetings set CashSavedBox = ?, CashSavedBank = ?, BankLoanRepayment = ? where _id = ?";
            db.execSQL(sql, new String[]{String.valueOf(cashSavedToBox), String.valueOf(cashSavedToBank), String.valueOf(bankLoanRepayment), String.valueOf(meetingID)});
        }catch (Exception e){
            Log.e("updateCashBook", e.getMessage());
        }
    }



    public MeetingStartingCash getStartingCash(){
        MeetingStartingCash meetingStartingCash = new MeetingStartingCash();
        meetingStartingCash.setCashSavedInBank(meeting.getOpeningBalanceBank());
        meetingStartingCash.setActualStartingCash(meeting.getOpeningBalanceBox());
        meetingStartingCash.setLoanFromBank(meeting.getLoanFromBank());
        meetingStartingCash.setComment(meeting.getComment());
        meetingStartingCash.setExpectedStartingCash(this.getExpectedStartingCash());
        return meetingStartingCash;
    }

    //Legacy Code is below. The legacy code needs to be refactored to exploit dependency injection

    //Add more fields to this method to update the meeting data table
    public boolean updateMeeting(Meeting meeting){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MeetingSchema.COL_MT_LOAN_FROM_BANK, meeting.getLoanFromBank());
            long result = db.update(MeetingSchema.TBL_MEETINGS, values, MeetingSchema.COL_MT_MEETING_ID + " = ?", new String[]{String.valueOf(meeting.getMeetingId())});
            if(result != -1){
                return true;
            }
        }catch(Exception e){
            Log.e("UpdateMeeting", e.getMessage());
        }
        return false;
    }

    // Should add this meeting, deactivate all other meetings in cycle, and activate this meeting
    public boolean addMeeting(Meeting meeting) {
        SQLiteDatabase db = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            if (meeting.getMeetingDate() == null) {
                meeting.setMeetingDate(new Date());
            }
            values.put(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD, meeting.isGettingStarted());
            values.put(MeetingSchema.COL_MT_IS_MARKED_FOR_DELETION, 0);
            values.put(MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDateToSqlite(meeting.getMeetingDate()));
            values.put(MeetingSchema.COL_MT_LOAN_FROM_BANK, meeting.getLoanFromBank());
            if (meeting.getVslaCycle() != null) {
                values.put(MeetingSchema.COL_MT_CYCLE_ID, meeting.getVslaCycle().getCycleId());
            }
            String sql = "INSERT INTO " + MeetingSchema.TBL_MEETINGS + " (" + MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD +"," +
                    MeetingSchema.COL_MT_IS_MARKED_FOR_DELETION + "," +
                    MeetingSchema.COL_MT_MEETING_DATE + "," +
                    MeetingSchema.COL_MT_LOAN_FROM_BANK + "," +
                    MeetingSchema.COL_MT_CYCLE_ID + ") VALUES (?, ?, ?, ?, ?)";
            //db.execSQL(sql, new String[]{String.valueOf(meeting.isGettingStarted()), String.valueOf(0), Utils.formatDateToSqlite(meeting.getMeetingDate()), String.valueOf(meeting.getLoanFromBank()), String.valueOf(meeting.getVslaCycle().getCycleId())});
            // Inserting Row
            long retVal = db.insert(MeetingSchema.getTableName(), null, values);
            Meeting targetMeeting = new MeetingRepo(context).getMeetingById(Integer.parseInt(String.valueOf(retVal)));
            Log.e("InsertMeeting", String.valueOf(targetMeeting.getLoanFromBank()));

            if (retVal == -1) {
                return false;
            } else {
                meeting.setMeetingId(Integer.parseInt(retVal + ""));
                //try to activate this meeting
                activateMeeting(meeting);
                return true;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.addMeeting", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateDataSentFlag(int meetingId, Date dateSent) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_IS_DATA_SENT, (true) ? 1 : 0);
            if (dateSent == null) {
                dateSent = new Date();
            }
            values.put(MeetingSchema.COL_MT_DATE_SENT, Utils.formatDateToSqlite(dateSent));

            long retVal = -1;
            retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meetingId)});
            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MeetingRepo.updateDataSentFlag", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateMeetingDate(int meetingId, Date meetingDate) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            if (meetingDate == null) {
                meetingDate = new Date();
            }
            values.put(MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDateToSqlite(meetingDate));

            long retVal = -1;
            retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meetingId)});
            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MeetingRepo.updateMeetingDate", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<Meeting> getAllMeetings(int currentCycleId) {
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
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, currentCycleId,
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // Looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                    boolean isCurrent = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_CURRENT)) == 1 ? true : false;
                    meeting.setIsCurrent(isCurrent);

                    // Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    } else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // Return the list
            return meetings;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MeetingRepo.getAllMeetings", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<Meeting> getAllMeetingsOfCycle(int targetCycleId) {
        return getAllMeetingsOfCycle(targetCycleId, MeetingOrderByEnum.ORDER_BY_MEETING_ID);
    }

    public ArrayList<Meeting> getAllMeetingsOfCycle(int targetCycleId, MeetingOrderByEnum orderByColumnName) {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            //If Ordering by a date, prepare the column properly
            //TODO: Did this in a hurry. Should be refactored
            String orderByClauseColumn = (orderByColumnName == MeetingOrderByEnum.ORDER_BY_MEETING_DATE) ? String.format("datetime(%s)", MeetingSchema.COL_MT_MEETING_DATE) : MeetingSchema.COL_MT_MEETING_ID;
            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, targetCycleId, orderByClauseColumn);
            cursor = db.rawQuery(selectQuery, null);

            // Looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    } else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeeting", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<Meeting> getAllNonGSWMeetings() {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int _isGSW = 0;
        VslaCycleRepo cycleRepo = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            cycleRepo = new VslaCycleRepo(context);

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD, _isGSW, MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                    meeting.setIsCurrent(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_CURRENT)) == 1);

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    } else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeeting", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }


    public ArrayList<Meeting> getAllMeetingsByDataSentStatus(boolean isDataSent, int currentCycleId) {
        ArrayList<Meeting> filteredMeetings = new ArrayList<Meeting>();
        try {
            ArrayList<Meeting> meetingsList = this.getAllMeetings(currentCycleId);
            for(Meeting meeting : meetingsList){
                if(meeting.isMeetingDataSent() == isDataSent){
                    filteredMeetings.add(meeting);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return filteredMeetings;
    }

    public ArrayList<Meeting> getPastMeetings(int currentCycleId){
        ArrayList<Meeting> oldMeetings = new ArrayList<Meeting>();
        try{
            ArrayList<Meeting> meetingsList = this.getAllMeetings(currentCycleId);
            for(Meeting meeting : meetingsList){
                if(!meeting.isMeetingDataSent() && !meeting.isCurrent()){
                    oldMeetings.add(0, meeting);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  oldMeetings;
    }


    public ArrayList<Meeting> getAllMeetingsByDataSentStatusAndActiveStatus() {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int dataSentFlag = 0;
        int activeFlag = 0;
        VslaCycleRepo cycleRepo = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            if (false) {
                dataSentFlag = 1;
            }

            if (false) {
                activeFlag = 1;
            }

            cycleRepo = new VslaCycleRepo(context);

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE COALESCE(%s,0)=%d AND COALESCE(%s,0)=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_IS_DATA_SENT, dataSentFlag, MeetingSchema.COL_MT_IS_CURRENT, activeFlag, MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                    meeting.setIsCurrent(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_CURRENT)) == 1);

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    } else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);
                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeeting", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }


    public ArrayList<Meeting> getAllMeetingsByActiveStatus() {
        ArrayList<Meeting> meetings = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int dataSentFlag = 0;
        VslaCycleRepo cycleRepo = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            meetings = new ArrayList<Meeting>();
            String columnList = MeetingSchema.getColumnList();

            dataSentFlag = 1;

            cycleRepo = new VslaCycleRepo(context);

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_IS_CURRENT, dataSentFlag, MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Meeting meeting = new Meeting();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setMeetingDate(meetingDate);
                    meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);

                    //Check for Nulls while loading the VSLA Cycle
                    int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                    meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                    if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                        meeting.setMeetingDataSent(true);
                        Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                        meeting.setDateSent(dateMeetingDataSent);
                    } else {
                        meeting.setMeetingDataSent(false);
                    }

                    meetings.add(meeting);

                } while (cursor.moveToNext());
            }

            // return the list
            return meetings;
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeeting", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }


    //Todo: To be deleted
    // public HashMap<String, Double> getMeetingStartingCash(int meetingId) {
    public MeetingStartingCash getMeetingStartingCash(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        // HashMap<String, Double> startingCash = new HashMap<String, Double>();
        MeetingStartingCash startingCash = new MeetingStartingCash();
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT IFNULL(%s,0) AS %s, %s AS %s, IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s FROM %s WHERE %s=%d",
                    MeetingSchema.COL_MT_CASH_FROM_BOX, MeetingSchema.COL_MT_CASH_FROM_BOX,
                    MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT, MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT,
                    MeetingSchema.COL_MT_CASH_SAVED_BANK, MeetingSchema.COL_MT_CASH_SAVED_BANK,
                    MeetingSchema.COL_MT_CASH_SAVED_BOX, MeetingSchema.COL_MT_CASH_SAVED_BOX,
                    MeetingSchema.COL_MT_CASH_FINES, MeetingSchema.COL_MT_CASH_FINES,
                    MeetingSchema.COL_MT_LOAN_TOP_UPS, MeetingSchema.COL_MT_LOAN_TOP_UPS,
                    MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                startingCash.setCashSavedInBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BANK)));
                startingCash.setActualStartingCash(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX)));
                startingCash.setComment(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT)));
                startingCash.setExpectedStartingCash(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BOX)));
                startingCash.setLoanTopUps(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_TOP_UPS)));
                return startingCash;
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MeetingRepo.getMeetingStartingCash", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingStartingCash getMeetingActualStartingCashDetails(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingStartingCash startingCash = new MeetingStartingCash();

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s, IFNULL(%s,0) AS %s FROM %s WHERE %s=%d",
                    MeetingSchema.COL_MT_CASH_FROM_BOX, MeetingSchema.COL_MT_CASH_FROM_BOX,
                    MeetingSchema.COL_MT_CASH_SAVED_BOX, MeetingSchema.COL_MT_CASH_SAVED_BOX,
                    MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT, MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT,
                    MeetingSchema.COL_MT_CASH_FROM_BANK, MeetingSchema.COL_MT_CASH_FROM_BANK,
                    MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                startingCash.setActualStartingCash(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX)));
                startingCash.setComment(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT)));
                startingCash.setExpectedStartingCash(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BOX)));
                startingCash.setCashSavedInBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BANK)));
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingActualStartingCashDetails", ex.getMessage());
            ex.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
            return startingCash;
        }
    }

    public double getMeetingTotalExpectedStartingCash(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT (IFNULL(%s,0) + IFNULL(%s,0)) AS TotalExpectedStartingCash FROM %s WHERE %s=%d",
                    MeetingSchema.COL_MT_CASH_FROM_BOX, MeetingSchema.COL_MT_CASH_FROM_BANK,
                    MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getDouble(cursor.getColumnIndex("TotalExpectedStartingCash"));
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingTotalExpectedStartingCash", ex.getMessage());
            return 0.0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateExpectedStartingCash(int meetingId, double cashTakenToBox) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        try {

            // Check if exists and do an Update
            Meeting meeting = getMeetingById(meetingId);

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_CASH_SAVED_BOX, cashTakenToBox);

            // Inserting or UpdatingRow
            long retVal = -1;
            retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meetingId)});

            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MeetingRepo.updateExpectedStartingCash", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateTopUp(int meetingId, double loanTopUp) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Log.d("MeetRep", String.format("UPDATE %s SET %s=%s+%f WHERE %s=%d",
                MeetingSchema.getTableName(), MeetingSchema.COL_MT_LOAN_TOP_UPS,
                MeetingSchema.COL_MT_LOAN_TOP_UPS, loanTopUp,
                MeetingSchema.COL_MT_MEETING_ID, meetingId));

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            // Update Query
            cursor = db.rawQuery(String.format("UPDATE %s SET %s=%s+%f WHERE %s=%d",
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_LOAN_TOP_UPS,
                    MeetingSchema.COL_MT_LOAN_TOP_UPS, loanTopUp,
                    MeetingSchema.COL_MT_MEETING_ID, meetingId), null);

            // looping through all rows and adding to list
            return cursor != null;
        } catch (Exception ex) {
            return false;
        } finally {
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
            String selectQuery = String.format("SELECT * FROM %s WHERE %s=%d", MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                meeting.setLoanFromBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_FROM_BANK)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //Log.e("MeetingRepo.getMeetingById", ex.getMessage());
            return null;
        } finally {
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
                    MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDate(theMeetingDate, "yyyy-MM-dd"),
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                meeting.setLoanFromBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_FROM_BANK)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingByDate", ex.getMessage());
            return null;
        } finally {
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
     *
     * @param theMeetingDate
     * @param vslaCycleId
     * @return
     */
    public Meeting getMeetingByDate(Date theMeetingDate, int vslaCycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query,
            String selectQuery = String.format("SELECT %s FROM %s WHERE date(%s)='%s' AND %s=%d ORDER BY %s DESC LIMIT 1", columnList, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_DATE, Utils.formatDate(theMeetingDate, "yyyy-MM-dd"),
                    MeetingSchema.COL_MT_CYCLE_ID, vslaCycleId,
                    MeetingSchema.COL_MT_MEETING_ID);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMeetingByDate", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public Meeting getPreviousMeeting(int vslaCycleId, int meetingId) {
        Meeting previousMeeting = new Meeting();

        try {
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String selectQuery = "SELECT * FROM Meetings WHERE CycleId = ? AND _id < ? ORDER BY _id DESC LIMIT 1";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(vslaCycleId), String.valueOf(meetingId)});

            if(cursor.moveToNext()){
                previousMeeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                VslaCycle vslaCycle = new VslaCycle();
                vslaCycle.setCycleId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID)));
                previousMeeting.setVslaCycle(vslaCycle);
                previousMeeting.setMeetingDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE))));
                previousMeeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(context);
                previousMeeting.setVslaCycle(vslaCycleRepo.getCycle(cycleId));
                previousMeeting.setOpeningBalanceBox(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX)));
                previousMeeting.setOpeningBalanceBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BANK)));
                previousMeeting.setClosingBalanceBox(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BOX)));
                previousMeeting.setClosingBalanceBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_SAVED_BANK)));
                previousMeeting.setLoanFromBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_FROM_BANK)));
                previousMeeting.setBankLoanRepayment(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_BANK_LOAN_REPAYMENT)));
                previousMeeting.setComment(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_CASH_FROM_BOX_COMMENT)));

                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    previousMeeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    previousMeeting.setDateSent(dateMeetingDataSent);
                } else {
                    previousMeeting.setMeetingDataSent(false);
                }
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getPreviousMeeting", ex.getMessage());
        }
        return previousMeeting;
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
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getCurrentMeeting", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public int getFirstCycleMeetingID(int cycleID){
        SQLiteDatabase db = null;
        int meetingID = 0;
        Cursor cursor = null;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "select _id from Meetings where CycleId = ? order by _id asc limit 1";
            cursor = db.rawQuery(sql, new String[]{String.valueOf(cycleID)});
            if(cursor != null){
                if(cursor.moveToNext()){
                    if(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)) != null){
                        meetingID = Integer.parseInt(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                    }
                }
            }
            cursor.close();
            db.close();

        }catch (Exception e){
            Log.e("GetFirstCycleMeeting", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }

        return meetingID;
    }

    //Returns the Getting started wizard dummy meeting
    public Meeting getDummyGettingStartedWizardMeeting() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;
        Meeting meeting = null;

        try {
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String columnList = MeetingSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT * FROM %s WHERE %s=%d LIMIT 1", MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD, 1);
            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                meeting = new Meeting();
                Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                meeting.setMeetingDate(meetingDate);
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_ID)));
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);
                meeting.setLoanFromBank(cursor.getDouble(cursor.getColumnIndex(MeetingSchema.COL_MT_LOAN_FROM_BANK)));

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getCurrentMeeting", ex.getMessage());
            return null;
        } finally {
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
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMostRecentMeeting", ex.getMessage());
            return null;
        } finally {
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
                meeting.setGettingStarted(cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_GETTING_STARTED_WIZARD)) == 1);

                //Check for Nulls while loading the VSLA Cycle
                int cycleId = cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_CYCLE_ID));
                meeting.setVslaCycle(cycleRepo.getCycle(cycleId));
                if (cursor.getInt(cursor.getColumnIndex(MeetingSchema.COL_MT_IS_DATA_SENT)) == 1) {
                    meeting.setMeetingDataSent(true);
                    Date dateMeetingDataSent = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MeetingSchema.COL_MT_MEETING_DATE)));
                    meeting.setDateSent(dateMeetingDataSent);
                } else {
                    meeting.setMeetingDataSent(false);
                }

                return meeting;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.e("MeetingRepo.getMostRecentMeeting", ex.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }


    /* Deactivates a meeting i.e sets the is current flag to false*/
    public boolean deactivateMeeting(Meeting meeting) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;

        try {
            if (meeting == null) {
                return false;
            }
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_IS_CURRENT, 0);

            // Update the specific one to InActive
            values.put(MeetingSchema.COL_MT_IS_CURRENT, 0);
            int retVal2 = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meeting.getMeetingId())});
            return retVal2 > 0;
        } catch (Exception ex) {
            Log.e("MeetingRepo.activateMeeting", ex.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    /* Marks a meeting for Deletion. If marked meetings are not UNDONE then they will be deleted when the app ends or starts.*/
    public boolean markMeetingForDeletion(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_IS_MARKED_FOR_DELETION, 1);

            //TODO: At this same time, if this meeting was the Current Meeting, then appoint another meeting ->Previous Sibling
            int retVal = db.update(MeetingSchema.getTableName(), values, MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meetingId)});
            return retVal > 0;

        } catch (Exception ex) {
            Log.e("MeetingRepo.markMeetingForDeletion", ex.getMessage());
            return false;
        } finally {
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
     *
     * @param meeting
     * @return
     */
    public boolean activateMeeting(Meeting meeting) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        VslaCycleRepo cycleRepo = null;

        try {
            if (meeting == null) {
                return false;
            }
            cycleRepo = new VslaCycleRepo(context);
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(MeetingSchema.COL_MT_IS_CURRENT, 0);

            String whereClause = MeetingSchema.COL_MT_CYCLE_ID + " = ? ";
            String[] whereArgs = new String[]{String.valueOf(meeting.getVslaCycle().getCycleId())};

            // updating row:
            int retVal = db.update(MeetingSchema.getTableName(), values, whereClause, whereArgs);

            if (retVal > 0) {

                //Update the specific one to Active in this cycle
                values.clear();
                values.put(MeetingSchema.COL_MT_IS_CURRENT, 1);

                whereClause = MeetingSchema.COL_MT_CYCLE_ID + " = ? AND " + MeetingSchema.COL_MT_MEETING_ID + " = ? ";
                whereArgs = new String[]{String.valueOf(meeting.getVslaCycle().getCycleId()), String.valueOf(meeting.getMeetingId())};


                int retVal2 = db.update(MeetingSchema.getTableName(), values, whereClause,
                        whereArgs);
                return retVal2 > 0;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MeetingRepo.activateMeeting", ex.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    //Given a meeting id, generates a map of all meeting data to be sent out
    public HashMap<String, String> generateMeetingDataMapToSendToServer(int meetingId) {

        Meeting meeting = getMeetingById(meetingId);
        HashMap<String, String> meetingData = new HashMap<String, String>();
        /*
        if (null != meeting) {
            //Get the Cycle in which this meeting belongs to
            String vslaCycleJson = SendDataRepo.getVslaCycleJson(meeting.getVslaCycle().getCycleId());
            if (vslaCycleJson != null) {
                //Add to Map
                meetingData.put(SendDataRepo.CYCLE_INFO_ITEM_KEY, vslaCycleJson);
            }
            //Members
            String membersJson = SendDataRepo.getMembersJson();
            if (membersJson != null) {
                //Add to Map
                meetingData.put(SendDataRepo.MEMBERS_ITEM_KEY, membersJson);
            }
            //Meeting Details
            String meetingJson = SendDataRepo.getMeetingJson(meeting);
            if (meetingJson != null) {
                //Add to Map
                meetingData.put(SendDataRepo.MEETING_DETAILS_ITEM_KEY, meetingJson);
            }
            //Attendance
            String meetingAttendanceJson = SendDataRepo.getMeetingAttendanceJson(meeting.getMeetingId());
            if (meetingAttendanceJson != null) {

                //Add to Map
                meetingData.put(SendDataRepo.ATTENDANCE_ITEM_KEY, meetingAttendanceJson);
            }

            //Savings
            String meetingSavingsJson = SendDataRepo.getMeetingSavingsJson(meeting.getMeetingId());
            if (meetingSavingsJson != null) {

                //Add to Map
                meetingData.put(SendDataRepo.SAVINGS_ITEM_KEY, meetingSavingsJson);
            }

            /**   // Opening Cash
             String meetingOpeningCashJson = SendDataRepo.getMeetingOpeningCashJson(meeting.getMeetingId());
             if(meetingOpeningCashJson != null) {
             //Add to Map
             meetingData.put(SendDataRepo.OPENING_CASH_ITEM_KEY, meetingOpeningCashJson);
             }

             // Cashbook
             String meetingCashBookJson = SendDataRepo.getMeetingCashBookJson(meeting.getMeetingId());
             if(meetingCashBookJson != null) {
             //Add to Map
             meetingData.put(SendDataRepo.CASHBOOK_ITEM_KEY, meetingCashBookJson);
             }



             */

            /*
            //Repayments
            String meetingRepaymentsJson = SendDataRepo.getMeetingRepaymentsJson(meeting.getMeetingId());
            if (meetingRepaymentsJson != null) {

                //Add to Map
                meetingData.put(SendDataRepo.REPAYMENTS_ITEM_KEY, meetingRepaymentsJson);
            }

            //Loan Issued
            String meetingLoansJson = SendDataRepo.getMeetingLoanIssuesJson(meeting.getMeetingId());
            if (meetingLoansJson != null) {

                //Add to Map
                meetingData.put(SendDataRepo.LOANS_ITEM_KEY, meetingLoansJson);
            }

            // Fine
            String meetingFineJson = SendDataRepo.getMeetingFinesJson(meeting.getMeetingId());
            if (meetingFineJson != null) {
                //Add to Map
                meetingData.put(SendDataRepo.FINES_ITEM_KEY, meetingFineJson);
            }
        }
        */
        return meetingData;
    }

    // Deleting single entity
    public boolean deleteMeeting(int meetingId) {
        SQLiteDatabase db = null;
        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // To remove all rows and get a count pass "1" as the whereClause.
            int rowCount = db.delete(MeetingSchema.getTableName(), MeetingSchema.COL_MT_MEETING_ID + " = ?",
                    new String[]{String.valueOf(meetingId)});

            return rowCount >= 1;
        } catch (Exception ex) {
            Log.e("MeetingRepo.deleteMeeting", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalCashToBankInCycle(int cycleId) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalCashToBank = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalCashToBank FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    MeetingSchema.COL_MT_CASH_FROM_BANK, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalCashToBank = cursor.getDouble(cursor.getColumnIndex("TotalCashToBank"));
            }

            return totalCashToBank;
        } catch (Exception ex) {
            Log.e("MeetingSavingRepo.getTotalSavingsInCycle", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }

    }

    public double getCashTakenToBankInPreviousMeeting(int meetingId) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalCashToBank = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT %s AS cashToBank FROM %s WHERE %s=%d",
                    MeetingSchema.COL_MT_CASH_SAVED_BANK, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_MEETING_ID, meetingId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalCashToBank = cursor.getDouble(cursor.getColumnIndex("cashToBank"));
            }

            return totalCashToBank;
        } catch (Exception ex) {
            Log.e("MeetingRepo.getCashTakenToBankInPreviousMeeting", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }

    }

    public boolean isMeetingSent(){
        boolean isMeetingSent = false;
        try {
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT IsDataSent FROM Meetings WHERE _id = ?", new String[]{String.valueOf(this.meetingID)});
            cursor.moveToNext();
            isMeetingSent = cursor.getInt(0) == 0 ? false : true;
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return isMeetingSent;
    }


}