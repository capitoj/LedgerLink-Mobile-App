package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.domain.schema.LoanIssueSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.domain.schema.VslaCycleSchema;
import org.applab.digitizingdata.domain.schema.VslaInfoSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;

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
            vslaInfo.setDateRegistered(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_REGISTERED))));
            vslaInfo.setDateLinked(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_LINKED))));
            vslaInfo.setBankBranch(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_BANK_BRANCH)));
            vslaInfo.setAccountNo(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_ACCOUNT_NO)));
            vslaInfo.setOffline((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_OFFLINE)) == 1)? true : false);
            vslaInfo.setActivated((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_ACTIVATED)) == 1)? true : false);
            vslaInfo.setDateActivated(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(VslaInfoSchema.COL_VI_DATE_ACTIVATED))));
            vslaInfo.setAllowDataMigration((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_ALLOW_DATA_MIGRATION)) == 1)? true : false);
            vslaInfo.setDataMigrated((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_DATA_MIGRATED)) == 1)? true : false);

            vslaInfo.setGettingStartedWizardComplete((cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_IS_GETTING_STARTED_WIZARD_COMPLETE)) == 1)? true : false);
            vslaInfo.setGettingStartedWizardStage(cursor.getInt(cursor.getColumnIndex(VslaInfoSchema.COL_VI_GETTING_STARTED_WIZARD_STAGE)));
            // return data
            return vslaInfo;
        }
        catch (Exception ex) {
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

    public boolean vslaInfoExists() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s", VslaInfoSchema.getColumnList(), VslaInfoSchema.getTableName());
            cursor = db.rawQuery(selectQuery, null);

            // Determine whether there was data
            if (cursor != null && cursor.moveToFirst()){
                return true;
            }

            return false;
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

    public boolean saveVslaInfo(String vslaCode, String vslaName, String passKey) {
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

    public boolean saveOfflineVslaInfo(String vslaCode, String passKey) {
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

    public boolean updateDataMigrationStatusFlag(boolean isDataMigrated) {
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

            if(isDataMigrated) {
                values.put(VslaInfoSchema.COL_VI_IS_DATA_MIGRATED, 1);
            }
            else{
                values.put(VslaInfoSchema.COL_VI_IS_DATA_MIGRATED, 0);
            }

            // Update
            // updating row
            long retVal = db.update(VslaInfoSchema.getTableName(), values, null,null);

            if (retVal != -1) {
                return true;
            }
            else {
                return false;
            }
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
