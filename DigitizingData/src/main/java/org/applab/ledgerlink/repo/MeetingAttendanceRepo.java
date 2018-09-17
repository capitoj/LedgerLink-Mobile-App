package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.schema.AttendanceSchema;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.datatransformation.AttendanceDataTransferRecord;
import org.applab.ledgerlink.helpers.AttendanceRecord;
import org.applab.ledgerlink.helpers.DatabaseHandler;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 7/5/13.
 */
public class MeetingAttendanceRepo {
    private Context context;
    private int meetingId;

    public MeetingAttendanceRepo() {
    }

    public MeetingAttendanceRepo(Context context){
        this.context = context;
    }

    public MeetingAttendanceRepo(Context context, int meetingId){
        this.context = context;
        this.meetingId = meetingId;
    }

    public boolean hasAttendants(){
        boolean hasAttedants = false;
        try {
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "select count(*) as total from Attendance where _id = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(meetingId)});
            if(cursor.moveToNext()){
                int total = cursor.getInt(0);
                if(total > 0){
                    hasAttedants = true;
                }
            }
        }catch (Exception e){
            Log.e("hasAttendants", e.getMessage());
        }
        return  hasAttedants;
    }

    int getMemberAttendanceId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int attendanceId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    AttendanceSchema.COL_A_ATTENDANCE_ID, AttendanceSchema.getTableName(),
                    AttendanceSchema.COL_A_MEETING_ID, meetingId,
                    AttendanceSchema.COL_A_MEMBER_ID, memberId, AttendanceSchema.COL_A_ATTENDANCE_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                attendanceId = cursor.getInt(cursor.getColumnIndex(AttendanceSchema.COL_A_ATTENDANCE_ID));
            }
            return attendanceId;
        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getMemberAttendanceId", ex.getMessage());
            return attendanceId;
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

    public boolean getMemberAttendance(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    AttendanceSchema.COL_A_IS_PRESENT, AttendanceSchema.getTableName(),
                    AttendanceSchema.COL_A_MEETING_ID, meetingId,
                    AttendanceSchema.COL_A_MEMBER_ID, memberId, AttendanceSchema.COL_A_ATTENDANCE_ID);
            cursor = db.rawQuery(query, null);

            int isPresent = 0;
            if (cursor != null && cursor.moveToFirst()) {
                if(!cursor.isNull(cursor.getColumnIndex(AttendanceSchema.COL_A_IS_PRESENT))) {
                    isPresent = cursor.getInt(cursor.getColumnIndex(AttendanceSchema.COL_A_IS_PRESENT));
                }
            }
            return isPresent > 0;
        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getMemberAttendance", ex.getMessage());
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

    public String getMemberAttendanceComment(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    AttendanceSchema.COL_A_COMMENTS, AttendanceSchema.getTableName(),
                    AttendanceSchema.COL_A_MEETING_ID, meetingId,
                    AttendanceSchema.COL_A_MEMBER_ID, memberId, AttendanceSchema.COL_A_ATTENDANCE_ID);
            cursor = db.rawQuery(query, null);

            String comment = null;
            if (cursor != null && cursor.moveToFirst()) {
                comment = cursor.getString(cursor.getColumnIndex(AttendanceSchema.COL_A_COMMENTS));
            }

            return comment;

        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getMemberAttendance", ex.getMessage());
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

    public int getMemberAttendanceCountInCycle(int cycleId, int memberId, int isPresentFlag) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        //isPresentFlag can be 0 or 1

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            //TODO: Performance will be affected but will come here later and return both absent and present count
            String query = String.format("SELECT  COUNT(*) AS PresentCount FROM %s WHERE %s=%d AND %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    AttendanceSchema.getTableName(),
                    AttendanceSchema.COL_A_IS_PRESENT, isPresentFlag,
                    AttendanceSchema.COL_A_MEMBER_ID, memberId,
                    AttendanceSchema.COL_A_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(query, null);

            int attendanceCount = 0;
            if (cursor != null && cursor.moveToFirst()) {
                attendanceCount = cursor.getInt(cursor.getColumnIndex("PresentCount"));
            }
           return attendanceCount;
        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getMemberAttendanceCountInCycle", ex.getMessage());
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

    public int getAttendanceCountByMeetingId(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        //isPresentFlag can be 0 or 1

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            //TODO: Performance will be affected but will come here later and return both absent and present count
            String query = String.format("SELECT  COUNT(*) AS PresentCount FROM %s WHERE %s=%d AND %s=%d",
                    AttendanceSchema.getTableName(),
                    AttendanceSchema.COL_A_IS_PRESENT, 1,
                    AttendanceSchema.COL_A_MEETING_ID, meetingId);
            cursor = db.rawQuery(query, null);

            int attendanceCount = 0;
            if (cursor != null && cursor.moveToFirst()) {
                attendanceCount = cursor.getInt(cursor.getColumnIndex("PresentCount"));
            }
            return attendanceCount;
        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getAttendanceCountByMeetingId", ex.getMessage());
            return -1;
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

    public ArrayList<AttendanceRecord> getMemberAttendanceHistoryInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<AttendanceRecord> attendances;

        try {
            attendances = new ArrayList<AttendanceRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            //TODO: I don't think I need the Sub-Query: can do Meetings.CycleId = xx
            String query = String.format("SELECT  %s.%s AS AttendanceId, %s.%s AS MeetingDate, %s.%s AS IsPresent, %s.%s AS Comments " +
                    " FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s.%s DESC",
                    AttendanceSchema.getTableName(),AttendanceSchema.COL_A_ATTENDANCE_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_MEETING_DATE,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_IS_PRESENT,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_COMMENTS,
                    AttendanceSchema.getTableName(), MeetingSchema.getTableName(),
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEETING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_MEETING_ID,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEMBER_ID, memberId,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId, AttendanceSchema.getTableName(),AttendanceSchema.COL_A_ATTENDANCE_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AttendanceRecord attendance = new AttendanceRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    attendance.setMeetingDate(meetingDate);
                    attendance.setAttendanceId(cursor.getInt(cursor.getColumnIndex("AttendanceId")));
                    attendance.setPresent(cursor.getInt(cursor.getColumnIndex("IsPresent")));
                    attendance.setComment(cursor.getString(cursor.getColumnIndex("Comments")));

                    attendances.add(attendance);

                } while (cursor.moveToNext());
            }
            return attendances;
        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getMemberAttendanceCountInCycle", ex.getMessage());
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

    public ArrayList<AttendanceRecord> getMemberAbsenceHistoryInCycle(int cycleId, int memberId, int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<AttendanceRecord> attendances;
        int attendanceStatus = 0;

        try {
            attendances = new ArrayList<AttendanceRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            //TODO: I don't think I need the Sub-Query: can do Meetings.CycleId = xx
            String query = String.format("SELECT  %s.%s AS AttendanceId, %s.%s AS MeetingDate, %s.%s AS IsPresent, %s.%s AS Comments " +
                            " FROM %s INNER JOIN %s ON %s.%s=%s.%s WHERE %s.%s=%d AND %s.%s=%d AND %s.%s IN (SELECT %s FROM %s WHERE %s=%d AND %s NOT %d) ORDER BY %s.%s DESC",
                    AttendanceSchema.getTableName(),AttendanceSchema.COL_A_ATTENDANCE_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_MEETING_DATE,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_IS_PRESENT,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_COMMENTS,
                    AttendanceSchema.getTableName(), MeetingSchema.getTableName(),
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEETING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_MEETING_ID,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEMBER_ID, memberId,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_IS_PRESENT, attendanceStatus,
                    AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(),
                    MeetingSchema.COL_MT_CYCLE_ID, cycleId, MeetingSchema.COL_MT_MEETING_ID, meetingId, AttendanceSchema.getTableName(),AttendanceSchema.COL_A_ATTENDANCE_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AttendanceRecord attendance = new AttendanceRecord();
                    Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                    attendance.setMeetingDate(meetingDate);
                    attendance.setAttendanceId(cursor.getInt(cursor.getColumnIndex("AttendanceId")));
                    attendance.setPresent(cursor.getInt(cursor.getColumnIndex("IsPresent")));
                    attendance.setComment(cursor.getString(cursor.getColumnIndex("Comments")));

                    attendances.add(attendance);

                } while (cursor.moveToNext());
            }
            return attendances;
        }
        catch (Exception ex) {
            Log.e("MeetingRollCallRepo.getMemberAttendanceCountInCycle", ex.getMessage());
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

    public ArrayList<AttendanceDataTransferRecord> getMeetingAttendanceForAllMembers(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<AttendanceDataTransferRecord> attendances;

        try {
            attendances = new ArrayList<AttendanceDataTransferRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s AS AttendanceId, %s AS MemberId, %s AS IsPresent, %s AS Comments " +
                    " FROM %s WHERE %s=%d ORDER BY %s",
                    AttendanceSchema.COL_A_ATTENDANCE_ID, AttendanceSchema.COL_A_MEMBER_ID, AttendanceSchema.COL_A_IS_PRESENT,
                    AttendanceSchema.COL_A_COMMENTS, AttendanceSchema.getTableName(), AttendanceSchema.COL_A_MEETING_ID, meetingId,
                    AttendanceSchema.COL_A_ATTENDANCE_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    AttendanceDataTransferRecord attendance = new AttendanceDataTransferRecord();
                    attendance.setAttendanceId(cursor.getInt(cursor.getColumnIndex("AttendanceId")));
                    attendance.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                    attendance.setMeetingId(meetingId);
                    attendance.setPresentFlg(cursor.getInt(cursor.getColumnIndex("IsPresent")));
                    attendance.setComments(cursor.getString(cursor.getColumnIndex("Comments")));

                    attendances.add(attendance);

                } while (cursor.moveToNext());
            }
            return attendances;
        }
        catch (Exception ex) {
            Log.e("MeetingAttendanceRepo.getMeetingAttendanceForAllMembers", ex.getMessage());
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

    public boolean saveMemberAttendance(int meetingId, int memberId, int isPresent) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int attendanceId = 0;
        try {
            //Check if exists and do an Update
            attendanceId = getMemberAttendanceId(meetingId, memberId);
            if(attendanceId > 0) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(AttendanceSchema.COL_A_MEETING_ID, meetingId);
            values.put(AttendanceSchema.COL_A_MEMBER_ID, memberId);
            values.put(AttendanceSchema.COL_A_IS_PRESENT, isPresent);

            // Inserting or UpdatingRow
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(AttendanceSchema.getTableName(), values, AttendanceSchema.COL_A_ATTENDANCE_ID + " = ?",
                        new String[] { String.valueOf(attendanceId) });
            }
            else {
                retVal = db.insert(AttendanceSchema.getTableName(), null, values);
            }

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("MeetingAttendanceRepo.saveMemberAttendance", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean saveMemberAttendanceWithComment(int meetingId, int memberId, String comment, int attendance) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int attendanceId = 0;
        try {
            //Check if exists and do an Update
            attendanceId = getMemberAttendanceId(meetingId, memberId);
            if(attendanceId > 0) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(AttendanceSchema.COL_A_MEETING_ID, meetingId);
            values.put(AttendanceSchema.COL_A_MEMBER_ID, memberId);
            values.put(AttendanceSchema.COL_A_COMMENTS, comment);
            values.put(AttendanceSchema.COL_A_IS_PRESENT, attendance);

            // Inserting or UpdatingRow
            long retVal = -1;
            if(performUpdate) {
                // updating row
                retVal = db.update(AttendanceSchema.getTableName(), values, AttendanceSchema.COL_A_ATTENDANCE_ID + " = ?",
                        new String[] { String.valueOf(attendanceId) });
            }
            else {
                retVal = db.insert(AttendanceSchema.getTableName(), null, values);
            }

            return retVal != -1;
        }
        catch (Exception ex) {
            Log.e("MeetingAttendanceRepo.saveMemberAttendanceWithComment", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

}
