package org.applab.ledgerlink.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.domain.model.MeetingSaving;
import org.applab.ledgerlink.domain.model.MeetingWelfare;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.schema.MemberSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/28/13.
 */
public class MemberRepo {
    static ArrayList<Member> members;
    private Context context;
    protected int memberId;

    public MemberRepo() {
    }

    public MemberRepo(Context context) {
        this.context = context;
    }

    public MemberRepo(Context context, int memberId){
        this.memberId = memberId;
        this.context = context;
    }

    public void archiveMember(){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = "update Members set HasLeft = ?, MemberNo = ?, DateLeft = ? where _id = ?";
            db.execSQL(sql, new String[]{String.valueOf(1), String.valueOf(0), Utils.formatDateToSqlite(new Date()), String.valueOf(this.memberId)});
        }catch (Exception e){
            Log.e("archiveMember", e.getMessage());
        }
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

            if (member.getDateOfBirth() == null) {
                member.setDateOfBirth(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_OF_BIRTH, Utils.formatDateToSqlite(member.getDateOfBirth()));
            values.put(MemberSchema.COL_M_PHONE_NO, member.getPhoneNumber());
            if (member.getDateOfAdmission() == null) {
                member.setDateOfAdmission(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_JOINED, Utils.formatDateToSqlite(member.getDateOfAdmission()));


            // Inserting Row
            long retVal = db.insert(MemberSchema.getTableName(), null, values);
            if (retVal != -1) {
                member.setMemberId((int) retVal);
                return saveMiddleCycleValues(member); //New: save middle cycle values too
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.addMember", ex.getMessage());
            return false;
        } finally {
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

            if (member.getDateOfBirth() == null) {
                member.setDateOfBirth(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_OF_BIRTH, Utils.formatDateToSqlite(member.getDateOfBirth()));
            values.put(MemberSchema.COL_M_PHONE_NO, member.getPhoneNumber());
            if (member.getDateOfAdmission() == null) {
                member.setDateOfAdmission(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_JOINED, Utils.formatDateToSqlite(member.getDateOfAdmission()));

            // Inserting Row
            long retVal = db.insert(MemberSchema.getTableName(), null, values);
            if (retVal != -1) {
                Log.d(context.getPackageName(), "GSW member added " + member.getSurname() + " - ret val is " + retVal);
                //set the members id now
                member.setMemberId((int) retVal);

                return saveMiddleCycleValues(member);
            } else {
                Log.d(context.getPackageName(), "GSW member not added " + member.getSurname());
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //     Log.e("MemberRepo.addMember", ex.getMessage().toString());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public boolean updateMemberLoanOnSetup(Member member) {
        boolean isGSW = true;
        MeetingRepo meetingRepo = new MeetingRepo(context);


        Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();

        //If loan on setup exists, then update else insert
        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
        MeetingLoanIssued loanIssuedToMemberInMeeting = meetingLoanIssuedRepo.getLoanIssuedToMemberInMeeting(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());

//        final Calendar c = Calendar.getInstance();
//        c.setTime(dummyGettingStartedWizardMeeting.getMeetingDate());
//        c.add(Calendar.MONTH, 1);
//        Date dueDate = c.getTime();

        String comment = "";

        if (null != member.getOutstandingLoanOnSetupCorrectionComment()) {
            if (!member.getOutstandingLoanOnSetupCorrectionComment().isEmpty()) {
                comment = member.getOutstandingLoanOnSetupCorrectionComment();
            }
        }

        if (loanIssuedToMemberInMeeting == null) {
            Log.d(context.getPackageName(), "updateMemberLoanOnSetup : loan issued not found, so create new record");
            if (member.getOutstandingLoanOnSetup() <= 0) {
                Log.d(context.getPackageName(), "Saving of loan on setup skipped because loan amount is " + member.getOutstandingLoanOnSetup());
                return true;
            }
            meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
            int loanId = meetingLoanIssuedRepo.getMemberLoanId(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());

            //First get the Interest Rate for the Current Cycle
            double interest = 0.0;
            double balance = 0.0;

            boolean isUpdate = false;

            // Save the loan
            boolean loanSaveResult = meetingLoanIssuedRepo.saveMemberLoanIssue(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId(), member.getOutstandingLoanNumberOnSetup(), member.getOutstandingLoanOnSetup(), interest, balance, member.getDateOfFirstRepayment(), comment, isUpdate);

            Log.d(context.getPackageName(), "updateMemberLoanOnSetup: Create record for loan on setup, Result:" + loanSaveResult);


            return loanSaveResult;
        } else {
            // Update loan balances
            Log.d(context.getPackageName(), "updateMemberLoanOnSetup : loan issued record found, update it");
            meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
            return meetingLoanIssuedRepo.updateMemberLoanIssued(loanIssuedToMemberInMeeting.getLoanId(), member.getOutstandingLoanNumberOnSetup(), member.getOutstandingLoanOnSetup(), member.getDateOfFirstRepayment());
//            meetingLoanIssuedRepo.updateMemberLoanNumber(loanIssuedToMemberInMeeting.getLoanId(), member.getOutstandingLoanNumberOnSetup());
//            return meetingLoanIssuedRepo.updateMemberLoanBalancesAndComment(loanIssuedToMemberInMeeting.getLoanId(), member.getOutstandingLoanOnSetup(), member.getDateOfFirstRepayment(), comment);
        }
    }


    public boolean updateGettingStartedWizardMember(Member member) {
        SQLiteDatabase db = null;
        try {
            if (member == null) {
                return false;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MemberSchema.COL_M_MEMBER_NO, member.getMemberNo());
            values.put(MemberSchema.COL_M_SURNAME, member.getSurname());
            values.put(MemberSchema.COL_M_OTHER_NAMES, member.getOtherNames());
            values.put(MemberSchema.COL_M_OCCUPATION, member.getOccupation());
            values.put(MemberSchema.COL_M_GENDER, member.getGender());
            if (member.getDateOfBirth() == null) {
                member.setDateOfBirth(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_OF_BIRTH, Utils.formatDateToSqlite(member.getDateOfBirth()));
            values.put(MemberSchema.COL_M_PHONE_NO, member.getPhoneNumber());
            if (member.getDateOfAdmission() == null) {
                member.setDateOfAdmission(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_JOINED, Utils.formatDateToSqlite(member.getDateOfAdmission()));

            // updating row
            int retVal = db.update(MemberSchema.getTableName(), values, MemberSchema.COL_M_MEMBER_ID + " = ?",
                    new String[]{String.valueOf(member.getMemberId())});

            return retVal > 0 && saveMiddleCycleValues(member);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    protected boolean saveMiddleCycleValues(Member member) {

        // Update savings at setup
        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(context);
        MeetingWelfareRepo meetingWelfareRepo = new MeetingWelfareRepo(context);
        MeetingOutstandingWelfareRepo meetingOutstandingWelfareRepo = new MeetingOutstandingWelfareRepo(context);
        MeetingRepo meetingRepo = new MeetingRepo(context);
        String comment = "";
        VslaCycle vslaCycle = new VslaCycleRepo(context).getCurrentCycle();
        int meetingID = meetingRepo.getFirstCycleMeetingID(vslaCycle.getCycleId());
        Meeting dummyGettingStartedWizardMeeting = new MeetingRepo(context).getMeetingById(meetingID);
        if (dummyGettingStartedWizardMeeting == null) {
            // No GSW wizard meeting? probably training mode... either way, be optimistic and return now
            return true;
        }

        if (null != member.getSavingsOnSetupCorrectionComment()) {
            if (!member.getSavingsOnSetupCorrectionComment().isEmpty()) {
                comment = member.getSavingsOnSetupCorrectionComment();
            }
        }
        boolean updateReturnValue = meetingSavingRepo.saveMemberSaving(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId(), member.getSavingsOnSetup(), comment);
        if (updateReturnValue) {
            Log.d(context.getPackageName(), "Savings to date saved for member " + member.getSurname());

            //Save the loan
            boolean loanSaveResult = updateMemberLoanOnSetup(member);
            if (!loanSaveResult) {
                Log.d(context.getPackageName(), "Failed to save GSW loan on setup" + member.getSurname());
            }

            //Save the welfare on setup
            meetingWelfareRepo.saveMemberWelfare(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId(), member.getWelfareOnSetup(), member.getWelfareOnSetupCorrectionComment());

            //Save the outstanding welfare on setup
            MeetingOutstandingWelfare meetingOutstandingWelfare = new MeetingOutstandingWelfare();
            meetingOutstandingWelfare.setMeeting(dummyGettingStartedWizardMeeting);
            meetingOutstandingWelfare.setMember(member);
            meetingOutstandingWelfare.setAmount(member.getOutstandingWelfareOnSetup());
            meetingOutstandingWelfare.setExpectedDate(member.getOutstandingWelfareDueDateOnSetup());
            meetingOutstandingWelfare.setIsCleared(0);
            meetingOutstandingWelfareRepo.saveMemberOutstandingWelfare(meetingOutstandingWelfare);
        }

        return true;
    }

    // Loads the savings at setup and loans at setup for the member
    public boolean loadMemberGettingStartedWizardValues(Member member) {

        MeetingRepo meetingRepo = new MeetingRepo(context);
        Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();

        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
        MeetingLoanIssued loanIssuedToMemberInMeeting = meetingLoanIssuedRepo.getLoanIssuedToMemberInMeeting(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());

        //If loan object is null, set the loan on setup as 0
        if (loanIssuedToMemberInMeeting == null) {
            member.setOutstandingLoanOnSetup(0);
        } else {
            member.setOutstandingLoanNumberOnSetup(loanIssuedToMemberInMeeting.getLoanNo());
            member.setOutstandingLoanOnSetup(loanIssuedToMemberInMeeting.getLoanBalance());
            member.setDateOfFirstRepayment(loanIssuedToMemberInMeeting.getDateDue());
            member.setOutstandingLoanOnSetupCorrectionComment(loanIssuedToMemberInMeeting.getComment());
        }

        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(context);
        MeetingSaving memberSaving = meetingSavingRepo.getMemberSavingAndComment(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());
        member.setSavingsOnSetup(memberSaving.getAmount());
        member.setSavingsOnSetupCorrectionComment(memberSaving.getComment());

        MeetingWelfareRepo meetingWelfareRepo = new MeetingWelfareRepo(context);
        member.setWelfareOnSetup(meetingWelfareRepo.getMemberWelfare(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId()));

        MeetingOutstandingWelfare meetingOutstandingWelfare = new MeetingOutstandingWelfareRepo(context).getMemberOutstandingWelfare(dummyGettingStartedWizardMeeting.getMeetingId(), member.getMemberId());
        if(meetingOutstandingWelfare != null) {
            member.setOutstandingWelfareOnSetup(meetingOutstandingWelfare.getAmount());
        }
        return true;
    }

    public ArrayList<Member> getActiveMembers(Date meetingDate){
        ArrayList<Member> allMembers = this.getAllMembers();
        ArrayList<Member> activeMembers = new ArrayList<Member>();
        if(allMembers != null) {
            for (Member member : allMembers) {
                if(member.getMemberNo() > 0) {
                    if (member.isActive()) {
                        activeMembers.add(member);
                    } else {
                        if (meetingDate.equals(member.getDateLeft()) || meetingDate.before(member.getDateLeft())) {
                            activeMembers.add(member);
                        }
                    }
                }
            }
        }
        return activeMembers;
    }

    public ArrayList<Member> getActiveMembers(){
        ArrayList<Member> allMembers = this.getAllMembers();
        ArrayList<Member> activeMembers = new ArrayList<Member>();
        if(allMembers != null) {
            for (Member member : allMembers) {
                if(member.getMemberNo() > 0) {
                    if (member.isActive()) {
                        activeMembers.add(member);
                    }
                }
            }
        }
        return activeMembers;
    }


    public ArrayList<Member> getAllMembers() {

        ArrayList<Member> members = null;
        ArrayList<Member> inactiveMembers = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            members = new ArrayList<Member>();
            inactiveMembers = new ArrayList<Member>();
            String columnList = MemberSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s, %s", columnList, MemberSchema.getTableName(),
                    MemberSchema.COL_M_MEMBER_NO, MemberSchema.COL_M_SURNAME);

            cursor = db.rawQuery(selectQuery, null);

            if (cursor == null) {
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
                    int status = cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_HAS_LEFT));
                    if(status == 0){
                        member.activate();
                    }else if(status == 1){
                        member.deactivate();
                    }
                    if(cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_LEFT))){
                        member.setDateLeft(null);
                    }else{
                        member.setDateLeft(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_LEFT))));
                    }

                    if (!loadMemberGettingStartedWizardValues(member)) {
                        Log.d(context.getPackageName(), "Failed to load Loan at setup and saving at setup for member " + member.getFullName());
                    }
                    if (cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))) {
                        member.setDateOfBirth(new Date());
                    } else {
                        member.setDateOfBirth(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))));
                    }
                    if (cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))) {
                        member.setDateOfAdmission(new Date());
                    } else {
                        member.setDateOfAdmission(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))));
                    }
                    if(member.getMemberNo() > 0) {
                        members.add(member);
                    }else{
                        inactiveMembers.add(member);
                    }

                } while (cursor.moveToNext());
            }

            // return the list
            members.addAll(inactiveMembers);
            return members;
        } catch (Exception ex) {
            ex.printStackTrace();
            //Log.e("MemberRepo.getAllMembers", ex.getMessage());
            return new ArrayList<Member>();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }


    //Counts members
    public int countMembers() {


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

            if (cursor == null) {
                return 0;
            }

            return cursor.getCount();


        } catch (Exception ex) {
            ex.printStackTrace();
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


    public Member getMemberById(int memberId) {
        Member member = null;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            cursor = db.query(MemberSchema.getTableName(), MemberSchema.getColumnListArray(),
                    MemberSchema.COL_M_MEMBER_ID + "=?",
                    new String[]{String.valueOf(memberId)}, null, null, null, null);

            if (cursor == null) {
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

            if(cursor.getInt(cursor.getColumnIndex(MemberSchema.COL_M_HAS_LEFT)) == 1 ? true : false){
                member.deactivate();
            }else{
                member.activate();
            }

            loadMemberGettingStartedWizardValues(member);

            if (cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))) {
                member.setDateOfBirth(new Date());
            } else {
                member.setDateOfBirth(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))));
            }
            if (cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))) {
                member.setDateOfAdmission(new Date());
            } else {
                member.setDateOfAdmission(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))));
            }

            // return the entity
            return member;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.getMemberById", ex.getMessage());
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

    public Member getMemberByMemberNo(int memberNo) {
        Member member = null;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            cursor = db.query(MemberSchema.getTableName(), MemberSchema.getColumnListArray(),
                    MemberSchema.COL_M_MEMBER_NO + "=?",
                    new String[]{String.valueOf(memberNo)}, null, null, null, null);

            if (cursor == null) {
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

            if (cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))) {
                member.setDateOfBirth(new Date());
            } else {
                member.setDateOfBirth(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_OF_BIRTH))));
            }
            if (cursor.isNull(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))) {
                member.setDateOfAdmission(new Date());
            } else {
                member.setDateOfAdmission(Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex(MemberSchema.COL_M_DATE_JOINED))));
            }
            // return the entity
            return member;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.getMemberByMemberNo", ex.getMessage());
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

    public boolean updateMember(Member member) {
        SQLiteDatabase db = null;

        try {
            if (member == null) {
                return false;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MemberSchema.COL_M_MEMBER_NO, member.getMemberNo());
            values.put(MemberSchema.COL_M_SURNAME, member.getSurname());
            values.put(MemberSchema.COL_M_OTHER_NAMES, member.getOtherNames());
            values.put(MemberSchema.COL_M_OCCUPATION, member.getOccupation());
            values.put(MemberSchema.COL_M_GENDER, member.getGender());
            values.put(MemberSchema.COL_M_HAS_LEFT, 0);
            values.put(MemberSchema.COL_M_DATE_LEFT, "");
            if (member.getDateOfBirth() == null) {
                member.setDateOfBirth(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_OF_BIRTH, Utils.formatDateToSqlite(member.getDateOfBirth()));
            values.put(MemberSchema.COL_M_PHONE_NO, member.getPhoneNumber());
            if (member.getDateOfAdmission() == null) {
                member.setDateOfAdmission(new Date());
            }
            values.put(MemberSchema.COL_M_DATE_JOINED, Utils.formatDateToSqlite(member.getDateOfAdmission()));


            // updating row
            int retVal = db.update(MemberSchema.getTableName(), values, MemberSchema.COL_M_MEMBER_ID + " = ?",
                    new String[]{String.valueOf(member.getMemberId())});

            return retVal > 0 && saveMiddleCycleValues(member);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.updateMember", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Deleting single entity
    public void deleteMember(Member member) {
        SQLiteDatabase db = null;
        try {
            if (member == null) {
                return;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // To remove all rows and get a count pass "1" as the whereClause.
            db.delete(MemberSchema.getTableName(), MemberSchema.COL_M_MEMBER_ID + " = ?",
                    new String[]{String.valueOf(member.getMemberId())});

            // TODO: Delete savings and outstanding loans and un paid fines too? Is it assumed that the person cashed out an paid all money due?

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.deleteMember", ex.getMessage());
        } finally {
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

            if (cursor == null) {
                return true;
            }

            if (!cursor.moveToFirst()) {
                return true;
            }

            // return
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MemberRepo.isMemberNoAvailable", ex.getMessage());
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


    /*Returns a list of available member numbers that can be used */
    public ArrayList<String> getListOfAvailableMemberNumbers() {
        ArrayList<String> memberNumbers = new ArrayList<String>();

        for (int i = 1; memberNumbers.size() < 100; i++) {
            if (isMemberNoAvailable(i, 0)) {
                memberNumbers.add(i + "");
            }
        }
        return memberNumbers;
    }


}
