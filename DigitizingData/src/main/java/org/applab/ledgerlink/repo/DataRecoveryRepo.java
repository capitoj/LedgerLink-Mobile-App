package org.applab.ledgerlink.repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.MeetingLoanRepayment;
import org.applab.ledgerlink.domain.model.MeetingSaving;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.domain.model.MeetingAttendance;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

import java.io.File;

/**
 * Created by JCapito on 9/25/2015.
 */
public class DataRecoveryRepo{

    protected Context context;
    private static final String EXTERNAL_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DATABASE_NAME = "ledgerlinkdb";
    private static final String DATA_FOLDER = "LedgerLink";
    protected boolean isDbFolderDeleted = false;

    protected DataRecoveryRepo(Context context){
        this.context = context;
        this.deleteDatabaseFolder();
    }

    protected void deleteDatabaseFolder(){
        try {
            File databaseStorageDir = new File(EXTERNAL_STORAGE_LOCATION + File.separator + DATA_FOLDER);
            if (databaseStorageDir.exists()) {
                String[] files = databaseStorageDir.list();
                for(int i = 0; i < files.length; i++){
                    new File(databaseStorageDir, files[i]).delete();
                }
                isDbFolderDeleted = databaseStorageDir.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void addVslaInfo(VslaInfo vslaInfo){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into VslaInfo (VslaName, VslaCode, PassKey, IsActivated, IsOffline, IsGettingStartedWizardComplete, GettingStartedWizard) values (?, ?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new String[]{vslaInfo.getVslaName(), vslaInfo.getVslaCode(), vslaInfo.getPassKey(), String.valueOf(1), String.valueOf(1), String.valueOf(1), String.valueOf(7)});
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void addCycle(VslaCycle vslaCycle){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into VslaCycles (_id, StartDate, EndDate, SharePrice, MaxShareQuantity, MaxStartShare, InterestRate, IsActive, IsEnded, DateEnded, SharedAmount, InterestAtSetup, FinesAtSetup) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new String[]{String.valueOf(vslaCycle.getCycleId()), Utils.formatDateToSqlite(vslaCycle.getStartDate()), Utils.formatDateToSqlite(vslaCycle.getEndDate()), String.valueOf(vslaCycle.getSharePrice()), String.valueOf(vslaCycle.getMaxSharesQty()), String.valueOf(vslaCycle.getMaxStartShare()), String.valueOf(vslaCycle.getInterestRate()), String.valueOf(1), String.valueOf(0), String.valueOf(vslaCycle.getDateEnded()), String.valueOf(vslaCycle.getSharedAmount()), String.valueOf(vslaCycle.getInterestAtSetup()), String.valueOf(vslaCycle.getFinesAtSetup())});
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void addMember(Member member){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into Members (_id, MemberNo, Surname, OtherNames, PhoneNo, Gender, DateOfBirth, Occupation, DateJoined) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new String[]{String.valueOf(member.getMemberId()), String.valueOf(member.getMemberNo()), member.getSurname(), member.getOtherNames(), member.getPhoneNumber(), member.getGender(), Utils.formatDateToSqlite(member.getDateOfBirth()), member.getOccupation(), Utils.formatDateToSqlite(member.getDateOfAdmission())});
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void addMeeting(Meeting meeting){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into Meetings (_id, CycleId, MeetingDate, IsDataSent, DateSent, IsCurrent, CashFromBox, CashFromBank, CashSavedBox, CashSavedBank, IsGettingStartedWizard) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int isGettingStarted = meeting.isGettingStarted() ? 1 : 0;
            int isCurrent = meeting.isCurrent() ? 1 : 0;
            db.execSQL(sql, new String[]{String.valueOf(meeting.getMeetingId()), String.valueOf(meeting.getVslaCycle().getCycleId()), Utils.formatDateToSqlite(meeting.getMeetingDate()), String.valueOf(1), Utils.formatDateToSqlite(meeting.getDateSent()), String.valueOf(isCurrent), String.valueOf(meeting.getOpeningBalanceBox()), String.valueOf(meeting.getOpeningBalanceBank()), String.valueOf(meeting.getClosingBalanceBox()), String.valueOf(meeting.getClosingBalanceBank()), String.valueOf(isGettingStarted)});
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected boolean hasMeeting(int meetingId){
        boolean hasMeeting = false;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "select COUNT(_id) as count from Meetings where _id = ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(meetingId)});
            cursor.moveToNext();
            int count = cursor.getInt(0);
            hasMeeting = (count == 0) ? false : true;
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return hasMeeting;
    }
    
    protected void addAttendance(MeetingAttendance meetingAttendance){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into Attendance (_id, MeetingId, MemberId, IsPresent, Comments) values (?, ?, ?, ?, ?)";
            int isPresent = meetingAttendance.isPresent() ? 1 : 0;
            db.execSQL(sql, new String[]{String.valueOf(meetingAttendance.getAttendanceId()), String.valueOf(meetingAttendance.getMeeting().getMeetingId()), String.valueOf(meetingAttendance.getMember().getMemberId()), String.valueOf(isPresent), meetingAttendance.getComment()});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected boolean hasAttendance(int attendanceId){
        boolean hasAttendance = false;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            Cursor cursor = db.rawQuery("select COUNT(_id) as count from Attendance where _id = ?", new String[]{String.valueOf(attendanceId)});
            cursor.moveToNext();
            int count = cursor.getInt(0);
            hasAttendance = count == 0 ? false : true;
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return hasAttendance;
    }

    protected void addSavings(MeetingSaving meetingSaving){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into Savings (_id, MeetingId, MemberId, Amount) values (?, ?, ?, ?)";
            db.execSQL(sql, new String[]{String.valueOf(meetingSaving.getSavingId()), String.valueOf(meetingSaving.getMeeting().getMeetingId()), String.valueOf(meetingSaving.getMember().getMemberId()), String.valueOf(meetingSaving.getAmount())});
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected boolean hasSavings(int savingsId){
        boolean hasSavings = false;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            Cursor cursor = db.rawQuery("select COUNT(_id) as count from Savings where _id = ?", new String[]{String.valueOf(savingsId)});
            cursor.moveToNext();
            int count = cursor.getInt(0);
            hasSavings = count == 0 ? false : true;
            cursor.close();
            db.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return hasSavings;
    }

    protected void addLoanIssues(MeetingLoanIssued meetingLoanIssued){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into LoanIssues (_id, LoanNo, MeetingId, MemberId, PrincipalAmount, InterestAmount, Balance, DateDue, TotalRepaid, IsDefaulted, IsCleared, DateCleared, Comments, IsWrittenOff) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int isDefaulted = meetingLoanIssued.isDefaulted() ? 1 : 0;
            int isCleared = meetingLoanIssued.isCleared() ? 1 : 0;
            int isWrittenOff = meetingLoanIssued.isWrittenOff() ? 1 : 0;
            db.execSQL(sql, new String[]{String.valueOf(meetingLoanIssued.getLoanId()), String.valueOf(meetingLoanIssued.getLoanNo()), String.valueOf(meetingLoanIssued.getMeeting().getMeetingId()), String.valueOf(meetingLoanIssued.getMember().getMemberId()), String.valueOf(meetingLoanIssued.getPrincipalAmount()), String.valueOf(meetingLoanIssued.getInterestAmount()), String.valueOf(meetingLoanIssued.getLoanBalance()), Utils.formatDateToSqlite(meetingLoanIssued.getDateDue()), String.valueOf(meetingLoanIssued.getTotalRepaid()), String.valueOf(isDefaulted), String.valueOf(isCleared), Utils.formatDateToSqlite(meetingLoanIssued.getDateCleared()), meetingLoanIssued.getComment(), String.valueOf(isWrittenOff)});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected boolean hasLoanIssue(int loanIssueId){
        boolean hasLoanIssue = false;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            Cursor cursor = db.rawQuery("select count(_id) as count from LoanIssues where _id = ?", new String[]{String.valueOf(loanIssueId)});
            cursor.moveToNext();
            int count = cursor.getInt(0);
            hasLoanIssue = count == 0 ? false : true;
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return hasLoanIssue;
    }

    protected void addLoanRepayment(MeetingLoanRepayment meetingLoanRepayment){
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            String sql = "insert into LoanRepayments (_id, LoanId, MemberId, MeetingId, Amount, BalanceBefore, BalanceAfter, InterestAmount, RollOverAmount, Comments, LastDateDue, NextDateDue) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new String[]{String.valueOf(meetingLoanRepayment.getRepaymentId()), String.valueOf(meetingLoanRepayment.getMeetingLoanIssued().getLoanId()), String.valueOf(meetingLoanRepayment.getMember().getMemberId()), String.valueOf(meetingLoanRepayment.getAmount()), String.valueOf(meetingLoanRepayment.getBalanceBefore()), String.valueOf(meetingLoanRepayment.getBalanceAfter()), String.valueOf(meetingLoanRepayment.getInterestAmount()), String.valueOf(meetingLoanRepayment.getRollOverAmount()), meetingLoanRepayment.getComment(), Utils.formatDateToSqlite(meetingLoanRepayment.getLastDateDue()), Utils.formatDateToSqlite(meetingLoanRepayment.getNextDateDue())});
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected boolean hasLoanRepayment(int loanRepaymentId){
        boolean hasLoanRepayment = false;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(this.context).getWritableDatabase();
            Cursor cursor = db.rawQuery("select count(_id) as count from LoanRepayments where _id = ?", new String[]{String.valueOf(loanRepaymentId)});
            cursor.moveToNext();
            int count = cursor.getInt(0);
            hasLoanRepayment = count == 0 ? false : true;
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return hasLoanRepayment;
    }
}