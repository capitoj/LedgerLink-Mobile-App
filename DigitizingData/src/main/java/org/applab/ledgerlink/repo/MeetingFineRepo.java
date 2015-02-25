package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.datatransformation.FinesDataTransferRecord;
import org.applab.ledgerlink.domain.schema.FineSchema;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.MemberFineRecord;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 4/2/14.
 */
public class MeetingFineRepo {

    private final Context context;

    public MeetingFineRepo(Context context) {
        this.context = context;
    }

    public int getMemberFineId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int fineId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
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
            String query = String.format("SELECT %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
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
            String query = String.format("SELECT %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
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

    // Picks sum of all fines issued to member regardless of whether they are paid or not
    public double getMemberTotalFinesInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFines = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFines FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEMBER_ID, memberId,
                    FineSchema.COL_F_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);

            cursor = db.rawQuery(sumQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalFines"));
            }


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

    // Picks sum of all fines outstanding for a member
    public double getMemberTotalFinesOutstandingInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFines = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFines FROM %s WHERE %s=%d AND %s!=%d AND IFNULL(%s,0)!=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d )",
            // String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFines FROM %s WHERE %s=%d AND %s!=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d )",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEMBER_ID, memberId,
                    FineSchema.COL_F_IS_CLEARED, 1,
                    FineSchema.COL_F_IS_DELETED, 1,
                    FineSchema.COL_F_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);

            cursor = db.rawQuery(sumQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalFines"));
            }


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
            String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFines FROM %s WHERE %s=%d",
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

    public double getTotalFinesPaidInThisMeeting(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFinesPaidInMeeting = 0.00;
        int paymentStatus = 1;

        // String meetingDateString = Utils.formatDate(meetingDate, "yyyy-MM-dd");

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            /** String sumQuery = String.format("SELECT SUM(%s) AS TotalFinesPaid FROM %s WHERE %s=%d AND %s LIKE '%s%%' AND %s=%d",
             FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
             FineSchema.COL_F_IS_CLEARED, paymentStatus,
             FineSchema.COL_F_DATE_CLEARED, meetingDateString,
             FineSchema.COL_F_PAID_IN_MEETING_ID, meetingId); */

            String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFinesPaid FROM %s WHERE %s=%d AND %s=%d",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_IS_CLEARED, paymentStatus,
                    FineSchema.COL_F_PAID_IN_MEETING_ID, meetingId);


            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalFinesPaidInMeeting = cursor.getDouble(cursor.getColumnIndex("TotalFinesPaid"));
            }

            return totalFinesPaidInMeeting;
        } catch (Exception ex) {
            Log.e("MeetingFineRepo.getTotalFinesInThisMeeting", ex.getMessage());
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
            String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFines FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
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

    public double getTotalFinesPaidInCycle(int cycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double totalFines = 0.00;
        int paymentStatus = 1;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT IFNULL(SUM(%s),0) AS TotalFinesPaidInCycle FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d) AND %s=%d",
                    FineSchema.COL_F_AMOUNT, FineSchema.getTableName(),
                    FineSchema.COL_F_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    FineSchema.COL_F_IS_CLEARED, paymentStatus);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                totalFines = cursor.getDouble(cursor.getColumnIndex("TotalFinesPaidInCycle"));
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
        String datePaid = "";
        int paidMeetingId;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(FineSchema.COL_F_MEETING_ID, meetingId);
            values.put(FineSchema.COL_F_MEMBER_ID, memberId);
            values.put(FineSchema.COL_F_AMOUNT, fineAmount);
            values.put(FineSchema.COL_F_IS_CLEARED, paymentStatus);
            values.put(FineSchema.COL_F_FINE_TYPE_ID, fineTypeId);


            if (paymentStatus == 1) {
                Date date = new Date();
                datePaid = Utils.formatDateToSqlite(date);
                values.put(FineSchema.COL_F_DATE_CLEARED, datePaid);

                // If paid on same day as meeting paidMeetingId is the current meetingId
                values.put(FineSchema.COL_F_PAID_IN_MEETING_ID, meetingId);
            }

            // Inserting Row
            long retVal = -1;
            retVal = db.insert(FineSchema.getTableName(), null, values);

            return retVal != -1;
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

            String query = String.format("SELECT  %s.%s AS FineId, %s.%s AS FineTypeId, %s.%s AS MeetingDate, %s.%s AS Amount, %s.%s AS Status, %s.%s AS PaidInMeetingId " +
                            " FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE IFNULL(%s.%s,0)!=%d AND %s.%s=%d AND %s.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s.%s DESC",

                    // " FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s.%s DESC",

                    FineSchema.getTableName(), FineSchema.COL_F_FINE_ID,
                    FineSchema.getTableName(), FineSchema.COL_F_FINE_TYPE_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_MEETING_DATE,
                    FineSchema.getTableName(), FineSchema.COL_F_AMOUNT,
                    FineSchema.getTableName(), FineSchema.COL_F_IS_CLEARED,
                    FineSchema.getTableName(), FineSchema.COL_F_PAID_IN_MEETING_ID,
                    FineSchema.getTableName(), MeetingSchema.getTableName(),
                    FineSchema.getTableName(), FineSchema.COL_F_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_MEETING_ID,
                    FineSchema.getTableName(), FineSchema.COL_F_IS_DELETED, 1,
                    FineSchema.getTableName(), FineSchema.COL_F_MEMBER_ID, memberId,
                    FineSchema.getTableName(), FineSchema.COL_F_MEETING_ID,
                    MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    FineSchema.getTableName(), FineSchema.COL_F_FINE_ID
            );
            cursor = db.rawQuery(query, null);


            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberFineRecord fine = new MemberFineRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    fine.setMeetingDate(meetingDate);
                    fine.setFineTypeId(cursor.getInt(cursor.getColumnIndex("FineTypeId")));
                    fine.setFineId(cursor.getInt(cursor.getColumnIndex("FineId")));
                    fine.setAmount(cursor.getDouble(cursor.getColumnIndex("Amount")));
                    fine.setStatus(cursor.getInt(cursor.getColumnIndex("Status")));
                    fine.setPaidInMeetingId(cursor.getInt(cursor.getColumnIndex("PaidInMeetingId")));
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
            String query = String.format("SELECT %s AS FineId, %s AS MemberId, %s AS Amount " +
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


    public boolean updateMemberFineStatus(int paidInMeetingId, int fineId, int paymentStatus, String datePaid) {
        SQLiteDatabase db = null;
        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(FineSchema.COL_F_IS_CLEARED, paymentStatus);
            if (paymentStatus == 1) {

                values.put(FineSchema.COL_F_PAID_IN_MEETING_ID, paidInMeetingId);
            } else {
                values.put(FineSchema.COL_F_PAID_IN_MEETING_ID, 0);
            }

            values.put(FineSchema.COL_F_DATE_CLEARED, datePaid);
            long retVal = -1;

            // Updating row
            retVal = db.update(FineSchema.getTableName(), values, FineSchema.COL_F_FINE_ID + " = ?",
                    new String[]{String.valueOf(fineId)});
            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberFineRepo.updateMemberFine", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateMemberFineDeletedFlag(int fineId) {
        SQLiteDatabase db = null;
        int deleted = 1;
        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(FineSchema.COL_F_IS_DELETED, deleted);

            long retVal = -1;

            // Updating row
            retVal = db.update(FineSchema.getTableName(), values, FineSchema.COL_F_FINE_ID + " = ?",
                    new String[]{String.valueOf(fineId)});
            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberFineRepo.updateMemberFine", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
