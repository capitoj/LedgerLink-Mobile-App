package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.domain.schema.VslaInfoSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

/**
 * Created by Moses on 8/1/13.
 */
public class VslaInfoRepo {
    private Context context;

    public VslaInfoRepo() {
    }

    public VslaInfoRepo(Context context) {
        this.context = context;
    }

    public VslaInfo getVslaInfo() {

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s LIMIT 1", VslaInfoSchema.getColumnList(), VslaInfoSchema.getTableName());
            cursor = db.rawQuery(selectQuery, null);

            // Determine whether there was data
            if (cursor == null)
            {
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }

            VslaInfo vslaInfo = new VslaInfo();
            vslaInfo.setVslaCode(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_VSLA_CODE)));
            vslaInfo.setVslaName(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_VSLA_NAME)));
            vslaInfo.setPassKey(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_PASS_KEY)));
            vslaInfo.setDateRegistered(null);
            if(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_REGISTERED)) != null)
                vslaInfo.setDateRegistered(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_REGISTERED))));
            vslaInfo.setDateLinked(null);
            if(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_LINKED)) != null)
                vslaInfo.setDateLinked(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_LINKED))));
            vslaInfo.setBankBranch(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_BANK_BRANCH)));
            vslaInfo.setAccountNo(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_ACCOUNT_NO)));
            vslaInfo.setOffline((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_OFFLINE)) == 1));
            vslaInfo.setActivated((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_ACTIVATED)) == 1));
            vslaInfo.setDateActivated(null);
            if(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_ACTIVATED)) != null)
                vslaInfo.setDateActivated(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_ACTIVATED))));
            vslaInfo.setAllowDataMigration((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_ALLOW_DATA_MIGRATION)) == 1));
            vslaInfo.setDataMigrated((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_DATA_MIGRATED)) == 1));

            vslaInfo.setGettingStartedWizardComplete((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE)) == 1));
            vslaInfo.setGettingStartedWizardStage(cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_GETTING_STARTED_WIZARD_STAGE)));
            vslaInfo.setFiID(cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_FINANCIAL_INSTITUTION_ID)));
            // return data
            return vslaInfo;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("VslaInfoRepo.getVslaInfo", ex.getMessage());
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

    protected  boolean vslaInfoExists() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s", VslaInfoSchema.getColumnList(), VslaInfoSchema.getTableName());
            cursor = db.rawQuery(selectQuery, null);

            // Determine whether there was data
            return cursor != null && cursor.moveToFirst();

        }
        catch (Exception ex) {
            Log.e("VslaInfoRepo.VslaInfoExists", ex.getMessage());
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

    public boolean saveVslaInfo(String vslaCode, String vslaName, String passKey, Integer fiID) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int loanId = 0;
        try {
            //If Null return false
            if(null == vslaName || null == vslaCode){
                return false;
            }

            //Check if exists and do an Update
            if(vslaInfoExists()) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(VslaInfoSchema.COL_VI_VSLA_CODE, vslaCode);
            values.put(VslaInfoSchema.COL_VI_VSLA_NAME, vslaName);
            values.put(VslaInfoSchema.COL_VI_PASS_KEY, passKey);
            values.put(VslaInfoSchema.COL_VI_IS_ACTIVATED, 1);
            values.put(VslaInfoSchema.COL_VI_FINANCIAL_INSTITUTION_ID, fiID);

            // Inserting or UpdatingRow
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(VslaInfoSchema.getTableName(), values, null,null);
            }
            else {
                retVal = db.insert(VslaInfoSchema.getTableName(), null, values);
            }

            if (retVal != -1) {

                //If executing in trainng mode, set GSW as completed
                if(Utils.isExecutingInTrainingMode()) {
                    updateGettingStartedWizardCompleteFlag();
                }

                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("VslaInfoRepo.saveVslaInfo", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean saveOfflineVslaInfo(String vslaCode, String passKey, Integer fiID) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int loanId = 0;
        try {
            //Check if exists and do an Update
            if(vslaInfoExists()) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(VslaInfoSchema.COL_VI_VSLA_CODE, vslaCode);
            values.put(VslaInfoSchema.COL_VI_VSLA_NAME, "Offline Mode");
            values.put(VslaInfoSchema.COL_VI_PASS_KEY, passKey);
            values.put(VslaInfoSchema.COL_VI_IS_ACTIVATED, 0);
            values.put(VslaInfoSchema.COL_VI_IS_OFFLINE, 1);
            values.put(VslaInfoSchema.COL_VI_FINANCIAL_INSTITUTION_ID, fiID);

            // Inserting or UpdatingRow
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(VslaInfoSchema.getTableName(), values, null,null);
            }
            else {
                retVal = db.insert(VslaInfoSchema.getTableName(), null, values);
            }

            if (retVal != -1) {
                //If executing in trainng mode, set GSW as completed
                if(Utils.isExecutingInTrainingMode()) {
                    updateGettingStartedWizardCompleteFlag();
                }
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("VslaInfoRepo.saveOfflineVslaInfo", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public boolean updateDataMigrationStatusFlag() {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int loanId = 0;
        try {
            //Check if exists
            if(!vslaInfoExists()) {
                return false;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            if(true) {
                values.put(VslaInfoSchema.COL_VI_IS_DATA_MIGRATED, 1);
            }
            else{
                values.put(VslaInfoSchema.COL_VI_IS_DATA_MIGRATED, 0);
            }

            // Update
            // updating row
            long retVal = db.update(VslaInfoSchema.getTableName(), values, null,null);

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("VslaInfoRepo.updateDataMigrationStatusFlag", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }



    //Updates whether the Getting started wizard has been completed
    public boolean updateGettingStartedWizardCompleteFlag() {
        SQLiteDatabase db = null;

        try {
            //Check if exists
            if(!vslaInfoExists()) {
                return false;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            if(true) {
                values.put(VslaInfoSchema.COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE, 1);

                //Update the GSW meeting to inactive
                MeetingRepo meetingRepo = new MeetingRepo(context);
                meetingRepo.deactivateMeeting(meetingRepo.getDummyGettingStartedWizardMeeting());
            }
            else{
                values.put(VslaInfoSchema.COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE, 0);
            }

            // Update
            // updating row
            long retVal = db.update(VslaInfoSchema.getTableName(), values, null,null);

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("VslaInfoRepo.updateDataMigrationStatusFlag", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }




    //Updates the current stage of the getting started wizard
    public boolean updateGettingStartedWizardStage(int stage) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int loanId = 0;
        try {
            //Check if exists
            if(!vslaInfoExists()) {
                return false;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

                values.put(VslaInfoSchema.COL_VI_GETTING_STARTED_WIZARD_STAGE, stage);


            // Update
            // updating row
            long retVal = db.update(VslaInfoSchema.getTableName(), values, null,null);

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("VslaInfoRepo.updateDataMigrationStatusFlag", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

}
