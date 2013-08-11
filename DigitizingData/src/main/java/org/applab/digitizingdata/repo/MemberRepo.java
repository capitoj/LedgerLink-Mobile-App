package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.schema.MemberSchema;
import org.applab.digitizingdata.domain.schema.VslaCycleSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/28/13.
 */
public class MemberRepo {
    static ArrayList<Member> members;
    private Context context;

    public MemberRepo() {
    }

    public MemberRepo(Context context) {
        this.context = context;
    }

    // Adding new Entity
    public boolean addMember(Member member) {
        SQLiteDatabase db = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MemberSchema.COL_M_MEMBER_NO, member.getMemberNo());
            values.put(MemberSchema.COL_M_SURNAME, member.getSurname());
            values.put(MemberSchema.COL_M_OTHER_NAMES, member.getOtherNames());
            values.put(MemberSchema.COL_M_OCCUPATION, member.getOccupation());
            values.put(MemberSchema.COL_M_GENDER, member.getGender());
            if(member.getDateOfBirth() == null) {
                member.setDateOfBirth(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_OF_BIRTH, Utils.formatDateToSqlite(member.getDateOfBirth()));
            values.put(MemberSchema.COL_M_PHONE_NO, member.getPhoneNumber());
            if(member.getDateOfAdmission() == null) {
                member.setDateOfAdmission(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_JOINED, Utils.formatDateToSqlite(member.getDateOfAdmission()));

            // Inserting Row
            long retVal = db.insert(MemberSchema.getTableName(), null, values);
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

    public ArrayList<Member> getAllMembers() {

        ArrayList<Member> members = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            members = new ArrayList<Member>();
            String columnList = MemberSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s", columnList, MemberSchema.getTableName(),
                    MemberSchema.COL_M_MEMBER_NO);

            cursor = db.rawQuery(selectQuery, null);

            if(cursor == null){
                return new ArrayList<Member>();
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Member member = new Member();
                    member.setMemberId(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_MEMBER_ID)));
                    member.setMemberNo(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_MEMBER_NO)));
                    member.setSurname(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_SURNAME)));
                    member.setOtherNames(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_OTHER_NAMES)));
                    member.setGender(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_GENDER)));
                    member.setOccupation(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_OCCUPATION)));
                    member.setPhoneNumber(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_PHONE_NO)));

                    if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))) {
                        member.setDateOfBirth(new Date());
                    }
                    else {
                        member.setDateOfBirth(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))));
                    }
                    if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))) {
                        member.setDateOfAdmission(new Date());
                    }
                    else {
                        member.setDateOfAdmission(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))));
                    }

                    members.add(member);

                } while (cursor.moveToNext());
            }

            // return the list
            return members;
        }
        catch (Exception ex) {
            Log.e("MemberRepo.getAllMembers", ex.getMessage());
            return new ArrayList<Member>();
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

    public Member getMemberById(int memberId) {
        Member member = null;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            cursor = db.query(MemberSchema.getTableName(), MemberSchema.getColumnListArray(),
                    MemberSchema.COL_M_MEMBER_ID + "=?",
                    new String[] { String.valueOf(memberId) }, null, null, null, null);

            if(cursor == null){
                return null;
            }

            if (!cursor.moveToFirst()) {
               return null;
            }
            member = new Member();
            member.setMemberId(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_MEMBER_ID)));
            member.setMemberNo(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_MEMBER_NO)));
            member.setSurname(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_SURNAME)));
            member.setOtherNames(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_OTHER_NAMES)));
            member.setGender(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_GENDER)));
            member.setOccupation(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_OCCUPATION)));
            member.setPhoneNumber(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_PHONE_NO)));

            if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))) {
                member.setDateOfBirth(new Date());
            }
            else {
                member.setDateOfBirth(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))));
            }
            if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))) {
                member.setDateOfAdmission(new Date());
            }
            else {
                member.setDateOfAdmission(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))));
            }
            // return the entity
            return member;
        }
        catch (Exception ex) {
            Log.e("MemberRepo.getMemberById", ex.getMessage());
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

    public Member getMemberByMemberNo(int memberNo) {
        Member member = null;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(MemberSchema.getTableName(), MemberSchema.getColumnListArray(),
                    MemberSchema.COL_M_MEMBER_ID + "=?",
                    new String[] { String.valueOf(memberNo) }, null, null, null, null);

            if(cursor == null){
                return null;
            }

            if (!cursor.moveToFirst()) {
                return null;
            }
            member = new Member();
            member.setMemberId(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_MEMBER_ID)));
            member.setMemberNo(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_MEMBER_NO)));
            member.setSurname(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_SURNAME)));
            member.setOtherNames(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_OTHER_NAMES)));
            member.setGender(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_GENDER)));
            member.setOccupation(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_OCCUPATION)));
            member.setPhoneNumber(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_PHONE_NO)));

            if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))) {
                member.setDateOfBirth(new Date());
            }
            else {
                member.setDateOfBirth(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))));
            }
            if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))) {
                member.setDateOfAdmission(new Date());
            }
            else {
                member.setDateOfAdmission(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))));
            }
            // return the entity
            return member;
        }
        catch (Exception ex) {
            Log.e("MemberRepo.getMemberByMemberNo", ex.getMessage());
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

    public boolean updateMember(Member member){
        SQLiteDatabase db = null;

        try {
            if(member == null){
                return false;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MemberSchema.COL_M_MEMBER_NO, member.getMemberNo());
            values.put(MemberSchema.COL_M_SURNAME, member.getSurname());
            values.put(MemberSchema.COL_M_OTHER_NAMES, member.getOtherNames());
            values.put(MemberSchema.COL_M_OCCUPATION, member.getOccupation());
            values.put(MemberSchema.COL_M_GENDER, member.getGender());
            if(member.getDateOfBirth() == null) {
                member.setDateOfBirth(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_OF_BIRTH, Utils.formatDateToSqlite(member.getDateOfBirth()));
            values.put(MemberSchema.COL_M_PHONE_NO, member.getPhoneNumber());
            if(member.getDateOfAdmission() == null) {
                member.setDateOfAdmission(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_JOINED, Utils.formatDateToSqlite(member.getDateOfAdmission()));


            // updating row
            int retVal = db.update(MemberSchema.getTableName(), values, MemberSchema.COL_M_MEMBER_ID + " = ?",
                    new String[] { String.valueOf(member.getMemberId()) });

            if (retVal > 0) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            Log.e("MemberRepo.updateMember", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Deleting single entity
    public void deleteMember(Member member) {
        SQLiteDatabase db = null;
        try {
            if(member == null){
                return;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // To remove all rows and get a count pass "1" as the whereClause.
            db.delete(MemberSchema.getTableName(), MemberSchema.COL_M_MEMBER_ID + " = ?",
                    new String[] { String.valueOf(member.getMemberId()) });
        }
        catch (Exception ex) {
            Log.e("MemberRepo.deleteMember", ex.getMessage());
            return;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean isMemberNoAvailable(int memberNo, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(MemberSchema.getTableName(), MemberSchema.getColumnListArray(),
                    MemberSchema.COL_M_MEMBER_ID + "<>? and " + MemberSchema.COL_M_MEMBER_NO + "=?",
                    new String[] { String.valueOf(memberId), String.valueOf(memberNo) }, null, null, null, null);

            if(cursor == null){
                return true;
            }

            if (!cursor.moveToFirst()) {
                return true;
            }

            // return
            return false;
        }
        catch (Exception ex) {
            Log.e("MemberRepo.isMemberNoAvailable", ex.getMessage());
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
}
