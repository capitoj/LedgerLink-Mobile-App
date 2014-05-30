package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.datatransformation.FinesDataTransferRecord;
import org.applab.digitizingdata.domain.schema.FineSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.domain.schema.SavingSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.MemberFineRecord;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 4/2/14.
 */
public class MeetingFineRepo {

    private Context context;

    public MeetingFineRepo(Context context) {
        this.context = context;
    }

    public int getMemberFineId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int fineId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    FineSchema.COL_F_FINE_ID, FineSchema.getTableName(),
                    FineSchema.COL_F_MEETING_ID, meetingId,
                    FineSchema.COL_F_MEMBER_ID, memberId, FineSchema.COL_F_FINE_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                fineId = cursor.getInt(cursor.getColumnIndex(FineSchema.COL_F_FINE_ID));
            }
            return fineId;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getMemberFineId", ex.getMessage());
            return fineId;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getMemberFine(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEETING_ID, meetingId,
                    FineSchema.COL_F_MEMBER_ID, memberId, FineSchema.COL_F_FINE_ID);
            cursor = db.rawQuery(query, null);

            double fine = 0.0;
            if (cursor != null && cursor.moveToFirst()) {
                fine = cursor.getDouble(cursor.getColumnIndex(FineSchema.COL_F_AMOUNT));
            }
            return fine;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getMemberFine", ex.getMessage());
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

    public double getMemberFineStatus(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    FineSchema.COL_F_IS_CLEARED, FineSchema.getTableName(),
                    FineSchema.COL_F_MEETING_ID, meetingId,
                    FineSchema.COL_F_MEMBER_ID, memberId, FineSchema.COL_F_FINE_ID);
            cursor = db.rawQuery(query, null);

            int fineStatus = 0;
            if (cursor != null && cursor.moveToFirst()) {
                fineStatus = cursor.getInt(cursor.getColumnIndex(FineSchema.COL_F_IS_CLEARED));
            }
            return fineStatus;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getMemberFine", ex.getMessage());
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

    public double getMemberTotalFinesInCycle(int cycleId, int memberId) {
        Log.d("Some Total fines in Here", "KWANZA");
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFines = 0.00;

        try {
            Log.d("Some Total fines in Here", "UNHA");
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalFines FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEMBER_ID, memberId,
                    FineSchema.COL_F_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            //,
            // MeetingSchema.getTableName(),MeetingSchema.COL_MT_CYCLE_ID,cycleId);

            cursor = db.rawQuery(sumQuery, null);
            Log.d("Some Total fines in Here", sumQuery);
            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalFines"));
                Log.d("Some Total fines in Here", "YES" + String.valueOf(totalFines));
            }
            Log.d("Some Total fines in Here", "YES" + String.valueOf(totalFines));

            return totalFines;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getMemberTotalFinesInCycle", ex.getMessage());
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

    public double getTotalFinesInMeeting(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFines = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalFines FROM %s WHERE %s=%d",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEETING_ID, meetingId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalFines"));
            }


            return totalFines;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getTotalFinesInMeeting", ex.getMessage());
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

    public double getTotalFinesInCycle(int cycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFines = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalFines FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalFines"));
            }

            return totalFines;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getTotalFineInCycle", ex.getMessage());
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

    public boolean saveMemberFine(int meetingId, int memberId, double fineAmount, int fineTypeId, int paymentStatus) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int fineId = 0;
        try {
            //Check if exists and do an Update
            //     fineId = getMemberFineId(meetingId, memberId);
            //   if (fineId > 0) {
            //     performUpdate = true;
            // }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(FineSchema.COL_F_MEETING_ID, meetingId);
            values.put(FineSchema.COL_F_MEMBER_ID, memberId);
            values.put(FineSchema.COL_F_AMOUNT, fineAmount);
            values.put(FineSchema.COL_F_IS_CLEARED, paymentStatus);
            values.put(FineSchema.COL_F_FINE_TYPE_ID, fineTypeId);

//            Log.d("SaveFine", sumQuery);

            // Inserting or UpdatingRow
            long retVal = -1;
            /**  if (performUpdate) {
             // updating row
             retVal = db.update(FineSchema.getTableName(), values, FineSchema.COL_F_FINE_ID + " = ?",
             new String[]{String.valueOf(fineId)});
             Log.d("MemberFineRepo.saveMemberFine", "DONE!");
             } else { */
            retVal = db.insert(FineSchema.getTableName(), null, values);
            Log.d("MemberFineRepo.saveMemberFine", "INSERT DONE!");
            // }

            if (retVal != -1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            Log.e("MemberFineRepo.saveMemberFine", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<MemberFineRecord> getMemberFineHistoryInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<MemberFineRecord> fines;

        try {
            fines = new ArrayList<MemberFineRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            //TODO: I don't think I need the Sub-Query: can do Meetings.CycleId = xx
            String query = String.format("SELECT  %s.%s AS FineId, %s.%s AS MeetingDate, %s.%s AS Amount, %s.%s AS Status " +
                            " FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s.%s DESC",
                    FineSchema.getTableName(), FineSchema.COL_F_FINE_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_MEETING_DATE,
                    FineSchema.getTableName(), FineSchema.COL_F_AMOUNT,
                    FineSchema.getTableName(), FineSchema.COL_F_IS_CLEARED,
                    FineSchema.getTableName(), MeetingSchema.getTableName(),
                    FineSchema.getTableName(), FineSchema.COL_F_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_MEETING_ID,
                    FineSchema.getTableName(), FineSchema.COL_F_MEMBER_ID, memberId,
                    FineSchema.getTableName(), FineSchema.COL_F_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    FineSchema.getTableName(), FineSchema.COL_F_FINE_ID
            );
            cursor = db.rawQuery(query, null);
            Log.d("MeetingFineRepo", query);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberFineRecord fine = new MemberFineRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    fine.setMeetingDate(meetingDate);
                    fine.setFineId(cursor.getInt(cursor.getColumnIndex("FineId")));
                    fine.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    fine.setStatus(cursor.getInt(cursor.getColumnIndex("Status")));

                    fines.add(fine);
                } while (cursor.moveToNext());
            }
            return fines;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getMemberFineHistoryInCycle", ex.getMessage());
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

    public ArrayList<FinesDataTransferRecord> getMeetingFinesForAllMembers(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<FinesDataTransferRecord> fines;

        try {
            fines = new ArrayList<FinesDataTransferRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s AS FineId, %s AS MemberId, %s AS Amount " +
                            " FROM %s WHERE %s=%d ORDER BY %s",
                    FineSchema.COL_F_FINE_ID, FineSchema.COL_F_MEMBER_ID, FineSchema.COL_F_AMOUNT,
                    FineSchema.getTableName(), FineSchema.COL_F_MEETING_ID, meetingId, FineSchema.COL_F_FINE_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    FinesDataTransferRecord fine = new FinesDataTransferRecord();
                    fine.setFinesId(cursor.getInt(cursor.getColumnIndex("FineId")));
                    fine.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                    fine.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    fine.setMeetingId(meetingId);

                    fines.add(fine);

                } while (cursor.moveToNext());
            }
            return fines;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getMeetingFinesForAllMembers", ex.getMessage());
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


    public boolean updateMemberFineStatus(int fineId, int paymentStatus) {

        SQLiteDatabase db = null;
        boolean performUpdate = false;
        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(FineSchema.COL_F_IS_CLEARED, paymentStatus);

        long retVal = -1;
         // updating row
         retVal = db.update(FineSchema.getTableName(), values, FineSchema.COL_F_FINE_ID + " = ?",
         new String[]{String.valueOf(fineId)});
         Log.d("MemberFineRepo.updateMemberFineStatus", "DONE!");
            if (retVal != -1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            Log.e("MemberFineRepo.saveMemberFine", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
