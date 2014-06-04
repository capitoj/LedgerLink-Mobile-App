package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.domain.model.*;
import org.applab.digitizingdata.domain.schema.MemberSchema;
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
            ex.printStackTrace();
            Log.e("MemberRepo.addMember", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }






    // Inserts the new member, and updates the members loans and savings in the dummy meeting
    public boolean addGettingStartedWizardMember(Member member) {
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
                Log.d(context.getPackageName(), "GSW member added "+member.getSurname()+" - ret val is "+retVal);
                //set the members id now
                member.setMemberId((int) retVal);
                MeetingSavingRepo repo = new MeetingSavingRepo(context);

                MeetingRepo meetingRepo = new MeetingRepo(context);
                Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();
                boolean savingResult = repo.saveMemberSaving(dummyGettingStartedWizardMeeting.getMeetingId(), (int) retVal, member.getSavingsOnSetup());

                if( ! savingResult) {
                    Log.d(context.getPackageName(), "Savings to date could not be added for member "+member.getSurname());
                    System.out.println("Savings to date could not be added for member "+member.getSurname());
                    return false;
                }
                else {
                    Log.d(context.getPackageName(), "Savings to date added for member "+member.getSurname());
                    //Save loan IFF loan amount on setup is greater than zero
                    if(member.getOutstandingLoanOnSetup() <= 0) {
                        Log.d(context.getPackageName(), "Saving of loan on setup skipped because loan amount is "+member.getOutstandingLoanOnSetup());
                       return savingResult;
                    }


                    //Save the loan
                    boolean loanSaveResult = updateMemberLoanOnSetup(member);
                    if( ! loanSaveResult ) {
                        Log.d(context.getPackageName(), "Failed to save GSW loan on setup"+member.getSurname());
                    }
                }



                return true;
            }
            else {
                Log.d(context.getPackageName(), "GSW member not added "+member.getSurname());
                return false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
       //     Log.e("MemberRepo.addMember", ex.getMessage().toString());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public boolean updateMemberLoanOnSetup(Member member) {
        Log.d(context.getPackageName(), "Metrod entry updateMemberLoanOnSetup");
        boolean savingResult = false;
        MeetingRepo meetingRepo = new MeetingRepo(context);


        Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();

        //If loan on setup exists, then update else insert
        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
        MeetingLoanIssued loanIssuedToMemberInMeeting = meetingLoanIssuedRepo.getLoanIssuedToMemberInMeeting(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());

        final Calendar c = Calendar.getInstance();
        c.setTime(dummyGettingStartedWizardMeeting.getMeetingDate());
        c.add(Calendar.MONTH, 1);

        if(loanIssuedToMemberInMeeting == null) {
            Log.d(context.getPackageName(), "updateMemberLoanOnSetup : loan issued not found, so create new record");
        if(member.getOutstandingLoanOnSetup() <= 0) {
            Log.d(context.getPackageName(), "Saving of loan on setup skipped because loan amount is "+member.getOutstandingLoanOnSetup());
            return true;
        }
        meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
        int loanId = meetingLoanIssuedRepo.getMemberLoanId(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());

        //First get the Interest Rate for the Current Cycle
        double interest = 0.0;

        //Save the loan
        boolean loanSaveResult = meetingLoanIssuedRepo.saveMemberLoanIssue(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId(), loanId, member.getOutstandingLoanOnSetup(),interest, c.getTime());

        Log.d(context.getPackageName(), "updateMemberLoanOnSetup: Create record for loan on setup, Result:"+loanSaveResult);


        return loanSaveResult;
        }
        else {
            //Update loan balances
            Log.d(context.getPackageName(), "updateMemberLoanOnSetup : loan issued record found, update it");
            meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
            return meetingLoanIssuedRepo.updateMemberLoanBalances(loanIssuedToMemberInMeeting.getLoanId(),0,member.getOutstandingLoanOnSetup(),loanIssuedToMemberInMeeting.getDateDue()) ;
        }
    }


    public boolean updateGettingStartedWizardMember(Member member){
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
                //Update savings at setup
                MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(context);
                MeetingRepo meetingRepo = new MeetingRepo(context);
                boolean updateReturnValue = meetingSavingRepo.saveMemberSaving(meetingRepo.getDummyGettingStartedWizardMeeting().getMeetingId(), member.getMemberId(), member.getSavingsOnSetup());

                if(updateReturnValue) {
                    Log.d(context.getPackageName(), "Savings to date saved for member "+member.getSurname());



                    //Save the loan
                    boolean loanSaveResult = updateMemberLoanOnSetup(member);
                    if( ! loanSaveResult ) {
                        Log.d(context.getPackageName(), "Failed to save GSW loan on setup"+member.getSurname());
                    }
                }

                //TODO: Update loans

                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.updateGettingStartedMember", ex.getMessage());
            return false;
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Loads the savings at setup and loans at setup for the member
     public boolean loadMemberGettingStartedWizardValues(Member member) {
         MeetingRepo meetingRepo = new MeetingRepo(context);
         Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();

         MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
         Log.d(context.getPackageName(), "Member id is "+member.getMemberId());
         MeetingLoanIssued loanIssuedToMemberInMeeting = meetingLoanIssuedRepo.getLoanIssuedToMemberInMeeting(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());

         //If loan object is null, set the loan on setup as 0
         Log.d(context.getPackageName(), "Loan Issued To Member object is "+loanIssuedToMemberInMeeting);
         member.setOutstandingLoanOnSetup(loanIssuedToMemberInMeeting == null ? 0 : loanIssuedToMemberInMeeting.getLoanBalance());

         MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(context);
         double memberSaving = meetingSavingRepo.getMemberSaving(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());
         member.setSavingsOnSetup(memberSaving);
         return true;
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

                    if(! loadMemberGettingStartedWizardValues(member)) {
                        Log.d(context.getPackageName(), "Failed to load Loan at setup and saving at setup for member "+member.getFullName());
                    }
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
            ex.printStackTrace();
            //Log.e("MemberRepo.getAllMembers", ex.getMessage());
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
            loadMemberGettingStartedWizardValues(member);

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
            ex.printStackTrace();
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
                    MemberSchema.COL_M_MEMBER_NO + "=?",
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
                    new String[]{String.valueOf(memberId), String.valueOf(memberNo)}, null, null, null, null);

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
            ex.printStackTrace();
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


    /*Returns a list of available member numbers that can be used */
    public ArrayList<String> getListOfAvailableMemberNumbers(int count) {
        ArrayList<String> memberNumbers = new ArrayList<String>();

        for(int i=1; memberNumbers.size()<count; i++) {
            if(isMemberNoAvailable(i, 0)) {
                memberNumbers.add(i+"");
            }
        }
        return memberNumbers;
    }


}
