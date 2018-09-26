package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingWelfare;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.schema.MeetingSchema;
import org.applab.ledgerlink.domain.schema.WelfareSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;

import java.util.ArrayList;

/**
 * Created by JCapito on 9/25/2018.
 */

public class MeetingWelfareRepo {
    private Context context;
    private int welfareId;
    private MeetingWelfare meetingWelfare;

    public MeetingWelfareRepo(Context context){
        this.context = context;
    }

    public MeetingWelfareRepo(Context context, int welfareId){
        this.context = context;
        this.welfareId = welfareId;
        this.__load();
    }

    protected void __load(){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT * FROM %s WHERE %s=%d",
                    WelfareSchema.getTableName(),
                    WelfareSchema.COL_W_WELFARE_ID, this.welfareId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    this.meetingWelfare.setWelfareId(cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_WELFARE_ID)));
                    Meeting meeting = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_MEETING_ID))).getMeeting();
                    this.meetingWelfare.setMeeting(meeting);
                    Member member = new MemberRepo(this.context).getMemberById(cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_MEMBER_ID)));
                    this.meetingWelfare.setMember(member);
                    this.meetingWelfare.setAmount(cursor.getDouble(cursor.getColumnIndex(WelfareSchema.COL_W_AMOUNT)));
                    this.meetingWelfare.setComment(cursor.getString(cursor.getColumnIndex(WelfareSchema.COL_W_WELFARE_AT_SETUP_CORRECTION_COMMMENT)));
                }
            }

        }catch (Exception e){
            Log.e("MeetingWelfareRepo", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }
    }

    public MeetingWelfare getMeetingWelfare(){
        return this.meetingWelfare;
    }

    public double getMemberWelfare(int meetingId, int memberId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double welfareAmount = 0.0;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT %s FROM %s WHERE %s=%d and %s=%d",
                    WelfareSchema.COL_W_AMOUNT,
                    WelfareSchema.getTableName(),
                    WelfareSchema.COL_W_MEETING_ID, meetingId,
                    WelfareSchema.COL_W_MEMBER_ID, memberId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    welfareAmount = cursor.getDouble(cursor.getColumnIndex(WelfareSchema.COL_W_AMOUNT));
                }
            }

        }catch (Exception e){
            Log.e("MeetingWelfareRepo", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }

        return welfareAmount;
    }

    public double getMemberTotalWelfareInCycle(int cycleId, int memberId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double welfareAmount = 0.0;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT SUM(%s) TotalWelfare FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    WelfareSchema.COL_W_AMOUNT, WelfareSchema.getTableName(),
                    WelfareSchema.COL_W_MEMBER_ID, memberId,
                    WelfareSchema.COL_W_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(),MeetingSchema.COL_MT_CYCLE_ID,cycleId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    welfareAmount = cursor.getDouble(cursor.getColumnIndex("TotalWelfare"));
                }
            }
        }catch (Exception e){
            Log.e("TotalWelfaraInCycle", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }

        return welfareAmount;
    }

    public double getTotalWelfareInMeeting(int meetingId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double welfareAmount = 0.0;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT SUM(%s) TotalWelfare FROM %s WHERE %s=%d)",
                    WelfareSchema.COL_W_AMOUNT, WelfareSchema.getTableName(),
                    WelfareSchema.COL_W_MEETING_ID, meetingId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    welfareAmount = cursor.getDouble(cursor.getColumnIndex("TotalWelfare"));
                }
            }
        }catch (Exception e){
            Log.e("TotalWelfaraInMeeting", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }

        return welfareAmount;
    }

    public double getTotalWelfareInCycle(int cycleId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double welfareAmount = 0.0;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT SUM(%s) TotalWelfare FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d))",
                    WelfareSchema.COL_W_AMOUNT, WelfareSchema.getTableName(),
                    WelfareSchema.COL_W_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    welfareAmount = cursor.getDouble(cursor.getColumnIndex("TotalWelfare"));
                }
            }
        }catch (Exception e){
            Log.e("TotalWelfaraInMeeting", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }

        return welfareAmount;
    }

    protected int getMemberWelfareId(int meetingId, int memberId){
        int welfareId = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("SELECT %s FROM %s WHERE %s=%d AND %s=%d)",
                    WelfareSchema.COL_W_WELFARE_ID, WelfareSchema.getTableName(),
                    WelfareSchema.COL_W_MEETING_ID, meetingId,
                    WelfareSchema.COL_W_MEMBER_ID, memberId);
            cursor = db.rawQuery(sql, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    welfareId = cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_WELFARE_ID));
                }
            }
        }catch (Exception e){
            Log.e("MemberWelfareId", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }
        return welfareId;
    }

    protected void saveMemberWelfare(int meetingId, int memberId, double amount, String comment){
        boolean performUpdate = false;
        SQLiteDatabase db = null;
        int welfareId = 0;
        boolean isSaved = false;
        try{
            welfareId = getMemberWelfareId(meetingId, memberId);
            if(welfareId > 0){
                performUpdate = true;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(WelfareSchema.COL_W_WELFARE_ID, welfareId);
            values.put(WelfareSchema.COL_W_MEETING_ID, meetingId);
            values.put(WelfareSchema.COL_W_MEMBER_ID, memberId);
            values.put(WelfareSchema.COL_W_AMOUNT, amount);
            values.put(WelfareSchema.COL_W_WELFARE_AT_SETUP_CORRECTION_COMMMENT, comment);
            if(performUpdate){
                int result = db.update(WelfareSchema.getTableName(), values, WelfareSchema.COL_W_WELFARE_ID + " = ?", new String[]{String.valueOf(welfareId)});
                Log.e("SaveMemberWelfare", String.valueOf(result));
            }else{
                long result = db.insert(WelfareSchema.getTableName(), null, values);
                Log.e("SaveMemberWelfare", String.valueOf(result));
            }
        }catch (Exception e){
            Log.e("SaveMemberWelfare", e.getMessage());
        }finally {
            db.close();
        }
    }

    public ArrayList<MeetingWelfare> getMemberWelfareHistoryInCyle(int cycleId, int memberId){
        ArrayList<MeetingWelfare> meetingWelfareHistory = new ArrayList<MeetingWelfare>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "SELECT * FROM " + WelfareSchema.getTableName() + " WHERE " + WelfareSchema.COL_W_MEMBER_ID + " = ? AND " + WelfareSchema.COL_W_MEETING_ID + " IN (SELECT " + MeetingSchema.COL_MT_MEETING_ID + " FROM " + MeetingSchema.getTableName() + " WHERE " + MeetingSchema.COL_MT_CYCLE_ID + " = ?)";
            cursor = db.rawQuery(sql, new String[]{String.valueOf(memberId), String.valueOf(cycleId)});
            if(cursor.getCount() > 0){
                while(cursor.moveToNext()){
                    MeetingWelfare meetingWelfare = new MeetingWelfare();
                    meetingWelfare.setWelfareId(cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_WELFARE_ID)));
                    Meeting meeting = new MeetingRepo(this.context, cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_MEETING_ID))).getMeeting();
                    meetingWelfare.setMeeting(meeting);
                    Member member = new MemberRepo(this.context).getMemberById(cursor.getInt(cursor.getColumnIndex(WelfareSchema.COL_W_MEMBER_ID)));
                    meetingWelfare.setMember(member);
                    meetingWelfare.setAmount(cursor.getDouble(cursor.getColumnIndex(WelfareSchema.COL_W_AMOUNT)));
                    meetingWelfare.setComment(cursor.getString(cursor.getColumnIndex(WelfareSchema.COL_W_WELFARE_AT_SETUP_CORRECTION_COMMMENT)));
                    meetingWelfareHistory.add(meetingWelfare);
                }
            }
        }catch (Exception e){
            Log.e("MemberWelfareHistory", e.getMessage());
        }finally {
            cursor.close();
            db.close();
        }
        return meetingWelfareHistory;
    }
}
