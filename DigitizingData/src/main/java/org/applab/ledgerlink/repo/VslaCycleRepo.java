package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.schema.VslaCycleSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/3/13.
 */
public class VslaCycleRepo {
    private static Context context;

    public VslaCycleRepo() {
    }

    public VslaCycleRepo(Context context) {
        this.context = context;
    }

    // Adding new Entity
    public boolean addCycle(VslaCycle cycle) {
        SQLiteDatabase db = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            if(cycle.getStartDate() == null) {
                cycle.setStartDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_START_DATE, Utils.formatDateToSqlite(cycle.getStartDate()));
            if(cycle.getEndDate() == null) {
                cycle.setEndDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_END_DATE, Utils.formatDateToSqlite(cycle.getEndDate()));
            values.put(VslaCycleSchema.COL_VC_INTEREST_RATE, cycle.getInterestRate());
            values.put(VslaCycleSchema.COL_VC_MAX_SHARE_QTY, cycle.getMaxSharesQty());
            values.put(VslaCycleSchema.COL_VC_MAX_START_SHARE, cycle.getMaxStartShare());
            values.put(VslaCycleSchema.COL_VC_SHARE_PRICE, cycle.getSharePrice());
            values.put(VslaCycleSchema.COL_VC_TYPE_OF_INTEREST, cycle.getTypeOfInterest());
            values.put(VslaCycleSchema.COL_VC_IS_ACTIVE, (cycle.isActive()) ? 1 : 0);

            //Adding portion to save GSW data
            values.put(VslaCycleSchema.COL_VC_FINES_AT_SETUP, cycle.getFinesAtSetup());
            values.put(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP, cycle.getInterestAtSetup());

            // Inserting Row
            long retVal = db.insert(VslaCycleSchema.getTableName(), null, values);
            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.addCycle", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<VslaCycle> getAllCycles() {

        ArrayList<VslaCycle> cycles = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cycles = new ArrayList<VslaCycle>();
            String columnList = VslaCycleSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s DESC", columnList, VslaCycleSchema.getTableName(),
                    VslaCycleSchema.COL_VC_CYCLE_ID);

            cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    VslaCycle cycle = new VslaCycle();
                    cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
                    cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
                    cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
                    cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
                    cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
                    cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
                    cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
                    cycle.setInterestAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP)));
                    cycle.setFinesAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP)));
                    if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                        cycle.activate();
                    }
                    else {
                        cycle.deactivate();
                    }

                    if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                        Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                        double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                        cycle.end(dateEnded,sharedAmount);
                    }

                    cycles.add(cycle);

                } while (cursor.moveToNext());
            }

            // return the list
            return cycles;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getAllCycles", ex.getMessage());
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

    //Creates the dummy GSW meeting
    public boolean createGettingStartedDummyMeeting(VslaCycle currentCycle) {
        Meeting meeting = new Meeting();
        meeting.setGettingStarted(true);
        meeting.setIsCurrent(true);
        meeting.setLoanFromBank(currentCycle.getOutstandingBankLoanAtSetup());
        meeting.setVslaCycle(currentCycle);
        //meeting.setMeetingDate(currentCycle.getStartDate());
        //GSW meeting date changed to current date
        meeting.setMeetingDate(new Date());
        MeetingRepo repo = new MeetingRepo(context);
        return repo.addMeeting(meeting);

    }

    //Return cycles that are still active
    public ArrayList<VslaCycle> getActiveCycles() {
        ArrayList<VslaCycle> activeCycles = null;

        try {
            activeCycles = new ArrayList<VslaCycle>();
            for(VslaCycle cycle: getAllCycles()) {
                if(cycle.isActive() && !cycle.isEnded()) {
                    activeCycles.add(cycle);
                }
            }
        }
        catch(Exception ex) {
            Log.e("VslaCycleRepo.getActiveCycles", ex.getMessage());
        }

        return activeCycles;
    }

    //Return cycles that are inactive
    public ArrayList<VslaCycle> getInActiveCycles() {
        ArrayList<VslaCycle> inActiveCycles = null;

        try {
            inActiveCycles = new ArrayList<VslaCycle>();
            for(VslaCycle cycle: getAllCycles()) {
                if(!cycle.isActive() && cycle.isEnded()) {
                    inActiveCycles.add(cycle);
                }
            }
        }
        catch(Exception ex) {
            Log.e("VslaCycleRepo.getInActiveCycles", ex.getMessage());
        }

        return inActiveCycles;
    }

    //Return cycles that are that completed
    public ArrayList<VslaCycle> getCompletedCycles() {
        ArrayList<VslaCycle> completedCycles = null;

        try {
            completedCycles = new ArrayList<VslaCycle>();
            for(VslaCycle cycle: getAllCycles()) {
                if(cycle.isEnded()) {
                    completedCycles.add(cycle);
                }
            }
        }
        catch(Exception ex) {
            Log.e("VslaCycleRepo.getCompletedCycles", ex.getMessage());
        }

        return completedCycles;
    }

    // Getting single Cycle
    public VslaCycle getCycle(int cycleId) {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(VslaCycleSchema.getTableName(), VslaCycleSchema.getColumnListArray(),
                    VslaCycleSchema.COL_VC_CYCLE_ID + "=?",
                    new String[] { String.valueOf(cycleId) }, null, null, null, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            //cycle.setTypeOfInterest((int) cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_TYPE_OF_INTEREST)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            cycle.setInterestAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP)));
            cycle.setFinesAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP)));
            cycle.setInterestAtSetupCorrectionComment(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP_COMMENT)));
            cycle.setFinesAtSetupCorrectionComment(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP_COMMENT)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getCycle", ex.getMessage());
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

    // Getting single Cycle by Date
    public VslaCycle getCycleByDate(Date targetDate) {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
           /** db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(VslaCycleSchema.getTableName(), VslaCycleSchema.getColumnListArray(),
                    "? BETWEEN " + VslaCycleSchema.COL_VC_START_DATE + " AND " + VslaCycleSchema.COL_VC_END_DATE,
                    new String[] { String.valueOf(targetDate) }, null, null, null, null);
           */
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
           // cycles = new ArrayList<VslaCycle>();
            String columnList = VslaCycleSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s<=\'%s\' AND %s>=\'%s\' ORDER BY %s DESC LIMIT 1",
                    columnList,
                    VslaCycleSchema.getTableName(),
                    VslaCycleSchema.COL_VC_START_DATE,
                    Utils.formatDateToSqlite(targetDate),
                    VslaCycleSchema.COL_VC_END_DATE,
                    Utils.formatDateToSqlite(targetDate),
                    VslaCycleSchema.COL_VC_CYCLE_ID);

            cursor = db.rawQuery(selectQuery, null);
            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            cycle.setInterestAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP)));
            cycle.setFinesAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getCycle", ex.getMessage());
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

    // Getting the Current Cycle
    public VslaCycle getCurrentCycle() {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(VslaCycleSchema.getTableName(), VslaCycleSchema.getColumnListArray(),
                    VslaCycleSchema.COL_VC_IS_ACTIVE + "=?",
                    new String[] { String.valueOf(1) }, null, null, null, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            cycle.setInterestAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP)));
            cycle.setFinesAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            MeetingRepo meetingRepo = new MeetingRepo(context);
            Meeting meeting = meetingRepo.getDummyGettingStartedWizardMeeting();
            Log.e("OutstandingLoanFromBank", String.valueOf(meeting.getLoanFromBank()));
            cycle.setOutstandingBankLoanAtSetup(meeting.getLoanFromBank());

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getCycle", ex.getMessage());
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

    // Getting the Current Cycle
    public VslaCycle getMostRecentCycle() {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        int isEnded = 1;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1",
                    VslaCycleSchema.getColumnList(), VslaCycleSchema.getTableName(), VslaCycleSchema.COL_VC_CYCLE_ID);
            cursor = db.rawQuery(selectQuery, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            cycle.setInterestAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP)));
            cycle.setFinesAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getMostRecentCycle", ex.getMessage());
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

    // Getting the Current Cycle
    public VslaCycle getMostRecentUnEndedCycle() {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        int isEnded = 1;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC LIMIT 1",
                    VslaCycleSchema.getColumnList(), VslaCycleSchema.getTableName(), VslaCycleSchema.COL_VC_IS_ENDED, isEnded, VslaCycleSchema.COL_VC_CYCLE_ID);
            cursor = db.rawQuery(selectQuery, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaCycle cycle = new VslaCycle();
            cycle.setCycleId(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_CYCLE_ID)));
            cycle.setStartDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_START_DATE))));
            cycle.setEndDate(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_END_DATE))));
            cycle.setInterestRate(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_RATE)));
            cycle.setSharePrice(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARE_PRICE)));
            cycle.setMaxSharesQty(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_SHARE_QTY)));
            cycle.setMaxStartShare(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_MAX_START_SHARE)));
            cycle.setInterestAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP)));
            cycle.setFinesAtSetup(cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_FINES_AT_SETUP)));
            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ACTIVE)) == 1) {
                cycle.activate();
            }
            else {
                cycle.deactivate();
            }

            if(cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_IS_ENDED)) == 1) {
                Date dateEnded = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaCycleSchema.COL_VC_DATE_ENDED)));
                double sharedAmount = cursor.getDouble(cursor.getColumnIndex(VslaCycleSchema.COL_VC_SHARED_AMOUNT));
                cycle.end(dateEnded,sharedAmount);
            }

            // return data
            return cycle;
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getMostRecentCycle", ex.getMessage());
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


    public boolean updateCycle(VslaCycle cycle) {
        SQLiteDatabase db = null;

        try {
            if(cycle == null) {
                return false;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            if(cycle.getStartDate() == null) {
                cycle.setStartDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_START_DATE, Utils.formatDateToSqlite(cycle.getStartDate()));
            if(cycle.getEndDate() == null) {
                cycle.setEndDate(new Date());
            }
            values.put(VslaCycleSchema.COL_VC_END_DATE, Utils.formatDateToSqlite(cycle.getEndDate()));
            values.put(VslaCycleSchema.COL_VC_INTEREST_RATE, cycle.getInterestRate());
            values.put(VslaCycleSchema.COL_VC_TYPE_OF_INTEREST, cycle.getTypeOfInterest());
            values.put(VslaCycleSchema.COL_VC_MAX_SHARE_QTY, cycle.getMaxSharesQty());
            values.put(VslaCycleSchema.COL_VC_MAX_START_SHARE, cycle.getMaxStartShare());
            values.put(VslaCycleSchema.COL_VC_SHARE_PRICE, cycle.getSharePrice());
            values.put(VslaCycleSchema.COL_VC_IS_ACTIVE, (cycle.isActive()) ? 1 : 0);
            values.put(VslaCycleSchema.COL_VC_IS_ENDED, (cycle.isEnded()) ? 1 : 0);
            values.put(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP, cycle.getInterestAtSetup());
            values.put(VslaCycleSchema.COL_VC_FINES_AT_SETUP, cycle.getFinesAtSetup());
            values.put(VslaCycleSchema.COL_VC_INTEREST_AT_SETUP_COMMENT, cycle.getInterestAtSetupCorrectionComment());
            values.put(VslaCycleSchema.COL_VC_FINES_AT_SETUP_COMMENT, cycle.getFinesAtSetupCorrectionComment());

            //if dateEnded is Null use the current date
            if(cycle.getDateEnded() == null) {
                Calendar c = Calendar.getInstance();
                values.put(VslaCycleSchema.COL_VC_DATE_ENDED, Utils.formatDateToSqlite(c.getTime()));
            }
            else {
                values.put(VslaCycleSchema.COL_VC_DATE_ENDED, Utils.formatDateToSqlite(cycle.getDateEnded()));
            }

            // updating row
            int retVal = db.update(VslaCycleSchema.getTableName(), values, VslaCycleSchema.COL_VC_CYCLE_ID + " = ?",
                    new String[] { String.valueOf(cycle.getCycleId()) });

            return retVal > 0;
        }
        catch (Exception ex) {
            Log.e("VslaCycleSchema.updateCycle", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Deleting single VslaCycle
    public void deleteCycle(VslaCycle cycle) {
        SQLiteDatabase db = null;

        try {
            if(cycle == null) {
                return;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // To remove all rows and get a count pass "1" as the whereClause.
            db.delete(VslaCycleSchema.getTableName(), VslaCycleSchema.COL_VC_CYCLE_ID + " = ?",
                    new String[] { String.valueOf(cycle.getCycleId()) });
        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.deleteCycle", ex.getMessage());
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public int getCyclesCount() {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            String countQuery = "SELECT  * FROM " + VslaCycleSchema.getTableName();
            cursor = db.rawQuery(countQuery, null);

            // return count
            return cursor.getCount();
        }
        catch (Exception ex) {
            Log.e("VslaCycleSchema.CyclesCount", ex.getMessage());
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

    public boolean activateCycle(VslaCycle cycle) {
        if(cycle != null && cycle.getCycleId()>0) {
            //Deactivate all
            //for(VslaCycle vslaCycle: vslaCycles) {
            //    vslaCycle.deactivate();
            //}

            //Activate the target
            cycle.activate();
            return true;
        }
        else {
            return false;
        }
    }

    public static int getInterestTypevalue() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int interestTypevalue = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT TypeOfInterest FROM VslaCycles WHERE IsActive = 1",
                    VslaCycleSchema.COL_VC_TYPE_OF_INTEREST, VslaCycleSchema.getTableName());
            cursor = db.rawQuery(query, null);


            if (cursor != null && cursor.moveToFirst()) {
                interestTypevalue = cursor.getInt(cursor.getColumnIndex(VslaCycleSchema.COL_VC_TYPE_OF_INTEREST));
            }

            return interestTypevalue;

        }
        catch (Exception ex) {
            Log.e("VslaCycleRepo.getInterestTypevalue", ex.getMessage());
            return interestTypevalue;
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