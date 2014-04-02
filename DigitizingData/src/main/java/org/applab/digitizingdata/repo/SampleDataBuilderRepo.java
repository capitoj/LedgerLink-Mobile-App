package org.applab.digitizingdata.repo;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.applab.digitizingdata.SettingsActivity;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.schema.AttendanceSchema;
import org.applab.digitizingdata.domain.schema.LoanIssueSchema;
import org.applab.digitizingdata.domain.schema.LoanRepaymentSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.domain.schema.MemberSchema;
import org.applab.digitizingdata.domain.schema.SavingSchema;
import org.applab.digitizingdata.domain.schema.VslaCycleSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Moses on 3/22/14.
 */
public class SampleDataBuilderRepo {
    private static Context appContext;

    public static boolean refreshTrainingData(Context context) {

        //Determine whether the refresh flag is ON
        if(!Utils.isRefreshDataFlgOn()){
            return false;
        }

        //confirm that we are on the training database
        appContext = context;
        if(null == context) {
            return false;
        }

        //VERY VERY CRITICAL STEP
        if(!Utils.isExecutingInTrainingMode()) {
            return false;
        }

        //Proceed
        boolean refreshSucceeded = false;
        if(deleteAllRecords()) {
            refreshSucceeded = insertRecords();
        }

        //If the Refresh succeeded, turn off the Refresh Flag so that data will not be refreshed every time
        if(refreshSucceeded) {
            //Reset the Refresh Flag i.e. set it to False and the Shared Preference to Unchecked
            Utils.setRefreshDataFlag(false);
            SharedPreferences appPrefs = Utils.getDefaultSharedPreferences(appContext);
            SharedPreferences.Editor prefEditor = appPrefs.edit();
            prefEditor.putBoolean(SettingsActivity.PREF_KEY_REFRESH_TRAINING_DATA, false);

            //Save the values
            //Can use apply() but would require API 9
            prefEditor.commit();

        }

        return refreshSucceeded;
    }

    private static boolean deleteAllRecords() {
        SQLiteDatabase db = null;
        try {
            if(null == appContext) {
                return false;
            }
            db = DatabaseHandler.getInstance(appContext).getWritableDatabase();

            //Delete the Attendances
            db.execSQL(String.format("DELETE FROM %s", AttendanceSchema.getTableName()));

            //Delete the Loan Repayments
            db.execSQL(String.format("DELETE FROM %s", LoanRepaymentSchema.getTableName()));

            //Delete the Loan Issues
            db.execSQL(String.format("DELETE FROM %s", LoanIssueSchema.getTableName()));

            //Delete the Savings
            db.execSQL(String.format("DELETE FROM %s", SavingSchema.getTableName()));

            //Delete the Fines
            //db.execSQL(String.format("TRUNCATE TABLE %s", SavingSchema.getTableName()));

            //Delete the Meetings
            db.execSQL(String.format("DELETE FROM %s", MeetingSchema.getTableName()));

            //Delete the Vsla Cycles
            db.execSQL(String.format("DELETE FROM %s", VslaCycleSchema.getTableName()));

            //Delete the Members
            db.execSQL(String.format("DELETE FROM %s", MemberSchema.getTableName()));

            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    private static boolean insertRecords(){
        SQLiteDatabase db = null;
        try {
            if(null == appContext) {
                return false;
            }
            db = DatabaseHandler.getInstance(appContext).getWritableDatabase();

            //Add VSLA Cycle
            VslaCycle cycle = new VslaCycle();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -8);
            cycle.setStartDate(cal.getTime());

            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DATE, -1);
            cycle.setEndDate(cal.getTime());
            cycle.setSharePrice(5000);
            cycle.setMaxSharesQty(5);
            cycle.activate();
            cycle.setInterestRate(10);
            cycle.setMaxStartShare(100000);

            VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(appContext);
            vslaCycleRepo.addCycle(cycle);

            //Retrieve the new Cycle. Need to declare an add method in the repo that returns an object
            cycle = vslaCycleRepo.getMostRecentCycle();

            //Add Members
            addMember(1,"Bwire","Justine","Male",33,"Farmer",1);
            addMember(2,"Waiswa", "Elam","Male",30,"Fish-monger", 2);
            addMember(3,"Mateka", "Emily","Female",36,"Farmer", 2);
            addMember(4,"Nabwire", "Rehema","Female",35,"Teacher", 2);
            addMember(5,"Mutonyi", "Patrick","Male",34,"Lecturer", 2);
            addMember(6,"Nekesa", "Esther","Female",30,"Lecturer", 2);
            addMember(7,"Masaba", "Charles","Male",27,"Businessman", 2);
            addMember(8,"Mungolo", "Ronald","Male",32,"Businessman", 2);
            addMember(9,"Mateka", "Edward","Male",39,"Farmer", 2);
            addMember(10,"Wafula", "Everlyn","Female",25,"Farmer", 2);
            addMember(11,"Wanyama", "Fred","Male",29,"Student", 2);
            addMember(12,"Oundo", "Ronald","Male",27,"Businessman", 2);
            addMember(13,"Onyango", "Stella","Female",25,"Lecturer", 2);
            addMember(14,"Taaka", "Felister","Female",41,"Farmer", 2);
            addMember(15,"Makokha", "Evans","Male",38,"Farmer", 2);
            addMember(16,"Nalyaka", "Juliet","Female",29,"Farmer", 2);
            addMember(17,"Mangeni", "Joseph","Male",27,"Teacher", 2);
            addMember(18,"Kayongo", "Jackson","Male",43,"Teacher", 2);
            addMember(19,"Nampala", "Stephen","Male",45,"Businessman", 2);
            addMember(20,"Nangoma", "Angela","Female",28,"Teacher", 2);
            addMember(21,"Sikuku", "Sylvia","Female",25,"Businesswoman", 2);
            addMember(22,"Nasirumbi", "Joan","Female",23,"Fish-monger", 2);
            addMember(23,"Wanyama", "Moses","Male",26,"Farmer", 2);
            addMember(24,"Wandera", "Agnes","Female",31,"Fish-monger", 2);
            addMember(25,"Wesonga", "Grace","Female",23,"Businesswoman", 2);

            //FIRST MEETING
            //Add First Meeting: on First Day of the Cycle
            Meeting meeting = new Meeting();
            meeting.setMeetingDate(cycle.getStartDate());
            meeting.setVslaCycle(cycle);
            MeetingRepo meetingRepo = new MeetingRepo(appContext);
            meetingRepo.addMeeting(meeting);

            //Retrieve that first meeting
            meeting = meetingRepo.getMostRecentMeeting();

            //If the meeting was not setup just return
            if(meeting.getMeetingId() == 0){
                return false;
            }

            //Otherwise, proceed
            //Setup Attendance: four members out of 5 are present
            //Setup Savings: setup savings
            MemberRepo memberRepo = new MemberRepo(appContext);
            MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(appContext);
            MeetingSavingRepo savingRepo = new MeetingSavingRepo(appContext);
            MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(appContext);
            MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(appContext);

            ArrayList<Member> members = memberRepo.getAllMembers();
            double shareValue = 5000D;
            int loanNumber = 0;

            for(Member memb:members){
                //Wrap this in a try...catch so that in case an error occurs on one member we just jump to the next one
                try{
                    //let every fifth member be absent
                    if(memb.getMemberNo() % 5 == 0) {
                        attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),0);
                    }
                    else{
                        attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),1);

                        //Saving range between 0 star and 5 stars
                        savingRepo.saveMemberSaving(meeting.getMeetingId(), memb.getMemberId(), shareValue * (memb.getMemberNo() % 5));

                        //Issue loans to every 6th member
                        if(memb.getMemberNo() % 6 == 0) {
                            //Get Loan Amount
                            double loanAmount = savingRepo.getMemberTotalSavingsInCycle(cycle.getCycleId(), memb.getMemberId()) * 3;
                            double interestAmount = loanAmount * (cycle.getInterestRate()*0.01);
                            //Get Date Due for loans issued today
                            Calendar calDateDue = Calendar.getInstance();
                            calDateDue.setTime(meeting.getMeetingDate());
                            calDateDue.add(Calendar.MONTH, 1); //since it is on monthly interest.

                            //Issue the Loan
                            loanIssuedRepo.saveMemberLoanIssue(meeting.getMeetingId(),memb.getMemberId(),loanNumber++, loanAmount , interestAmount, calDateDue.getTime());
                        }
                    }
                }
                catch(Exception ex) {
                    continue;
                }
            }


            //SECOND MEETING
            //Add Second Meeting: One Month after Cycle started
            meeting = new Meeting();
            cal.setTime(cycle.getStartDate());
            cal.add(Calendar.MONTH, 1);
            meeting.setMeetingDate(cal.getTime());
            meeting.setVslaCycle(cycle);
            meetingRepo.addMeeting(meeting);

            //Retrieve that meeting to get the generated Meeting ID
            meeting = meetingRepo.getMostRecentMeeting();

            //If the meeting was not setup just return
            if(meeting.getMeetingId() == 0){
                return false;
            }

            //Otherwise, proceed
            //Setup Attendance: four members out of 5 are present
            //Setup Savings: setup savings
            for(Member memb:members){
                //Wrap this in a try...catch so that in case an error occurs on one member we just jump to the next one
                try{
                    //Let all members be present
                    attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),1);
                    savingRepo.saveMemberSaving(meeting.getMeetingId(), memb.getMemberId(), shareValue * ((memb.getMemberNo() % 5) + 1));

                    //Record loan repayments for the members that received loans in the previous meeting [a month ago]
                    if(memb.getMemberNo() % 6 == 0) {
                        //Get Most Recent Loan issued to member
                        MeetingLoanIssued theLoan = loanIssuedRepo.getMostRecentLoanIssuedToMember(memb.getMemberId());

                        boolean loanRepaymentSucceeded = false;
                        if(null != theLoan){
                            loanRepaymentSucceeded = repaymentRepo.saveMemberLoanRepayment(meeting.getMeetingId(),memb.getMemberId(), theLoan.getLoanId(),theLoan.getLoanBalance(),theLoan.getLoanBalance(),"GOOD",0D,0D,0D,meeting.getMeetingDate(),null);
                            if(loanRepaymentSucceeded) {
                                //Also update the balances in the actual Loan entity
                                if (loanIssuedRepo == null) {
                                    loanIssuedRepo = new MeetingLoanIssuedRepo(appContext);
                                }
                                //updateMemberLoanBalances(int loanId, double totalRepaid, double balance, Date newDateDue)
                                loanIssuedRepo.updateMemberLoanBalances(theLoan.getLoanId(),theLoan.getTotalRepaid() + theLoan.getLoanBalance(), 0D, null);
                            }
                        }
                    }
                    //Don't Issue New Loans in this meeting
                }
                catch(Exception ex) {
                    continue;
                }
            }


            //THIRD MEETING
            //Add Third Meeting: 2 months ago
            meeting = new Meeting();
            cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -2);
            meeting.setMeetingDate(cal.getTime());
            meeting.setVslaCycle(cycle);
            meetingRepo.addMeeting(meeting);

            //Retrieve that meeting
            meeting = meetingRepo.getMostRecentMeeting();

            //If the meeting was not setup just return
            if(meeting.getMeetingId() == 0){
                return false;
            }

            //Otherwise, proceed
            for(Member memb:members){
                //Wrap this in a try...catch so that in case an error occurs on one member we just jump to the next one
                try{
                    //let every 4th member be absent
                    if(memb.getMemberNo() % 4 == 0) {
                        attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),0);
                    }
                    else{
                        attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),1);
                        savingRepo.saveMemberSaving(meeting.getMeetingId(), memb.getMemberId(), shareValue * ((memb.getMemberNo() % 5) + 1));

                        //No Loan Repayments

                        //Issue loans to every 5th member that is present
                        if(memb.getMemberNo() % 5 == 0 ) {
                            //Get Loan Amount
                            double loanAmount = savingRepo.getMemberTotalSavingsInCycle(cycle.getCycleId(), memb.getMemberId()) * 3;
                            double interestAmount = loanAmount * (cycle.getInterestRate()*0.01);
                            //Get Date Due for loans issued today
                            Calendar calDateDue = Calendar.getInstance();
                            calDateDue.setTime(meeting.getMeetingDate());
                            calDateDue.add(Calendar.MONTH, 1);

                            //Issue the Loan
                            loanIssuedRepo.saveMemberLoanIssue(meeting.getMeetingId(),memb.getMemberId(),loanNumber++, loanAmount , interestAmount, calDateDue.getTime());
                        }
                    }
                }
                catch(Exception ex) {
                    continue;
                }
            }


            //FOURTH MEETING
            //Add Fourth Meeting: One Month Later
            meeting = new Meeting();
            cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            meeting.setMeetingDate(cal.getTime());
            meeting.setVslaCycle(cycle);
            meetingRepo.addMeeting(meeting);

            //Retrieve that meeting to get the generated Meeting ID
            meeting = meetingRepo.getMostRecentMeeting();

            //If the meeting was not setup just return
            if(meeting.getMeetingId() == 0){
                return false;
            }

            //Otherwise, proceed
            for(Member memb:members){
                //Wrap this in a try...catch so that in case an error occurs on one member we just jump to the next one
                try{
                    //Let all members be present
                    attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),1);
                    savingRepo.saveMemberSaving(meeting.getMeetingId(), memb.getMemberId(), shareValue * ((memb.getMemberNo() % 5) + 1));

                    //Record loan repayments for the members that received loans in the previous meeting [a month ago]
                    if(memb.getMemberNo() % 5 == 0 && memb.getMemberNo() % 4 != 0) {
                        //Get Most Recent Loan issued to member
                        MeetingLoanIssued theLoan = loanIssuedRepo.getMostRecentLoanIssuedToMember(memb.getMemberId());

                        boolean loanRepaymentSucceeded = false;
                        if(null != theLoan){
                            //Don't clear the loan.
                            double repayAmount = savingRepo.getMemberTotalSavingsInCycle(cycle.getCycleId(),memb.getMemberId());
                            double balanceBefore = theLoan.getLoanBalance();
                            double balanceAfter = theLoan.getLoanBalance() - repayAmount;
                            double interestAmount = balanceAfter * (cycle.getInterestRate() * 0.01);
                            double rolloverBalance = balanceAfter + interestAmount;
                            Calendar nextDueDate = Calendar.getInstance();
                            nextDueDate.setTime(meeting.getMeetingDate());
                            nextDueDate.add(Calendar.MONTH, 1);

                            loanRepaymentSucceeded = repaymentRepo.saveMemberLoanRepayment(meeting.getMeetingId(),memb.getMemberId(), theLoan.getLoanId(),repayAmount,balanceBefore,"ROLLOVER",balanceAfter,interestAmount,rolloverBalance,theLoan.getDateDue(),nextDueDate.getTime());
                            if(loanRepaymentSucceeded) {
                                //Also update the balances in the actual Loan entity
                                if (loanIssuedRepo == null) {
                                    loanIssuedRepo = new MeetingLoanIssuedRepo(appContext);
                                }
                                //updateMemberLoanBalances(int loanId, double totalRepaid, double balance, Date newDateDue)
                                loanIssuedRepo.updateMemberLoanBalances(theLoan.getLoanId(),theLoan.getTotalRepaid() + repayAmount, rolloverBalance, nextDueDate.getTime());
                            }
                        }
                    }
                    //Issue new Loan to Member 3 and 9
                    if(memb.getMemberNo() == 3 || memb.getMemberNo() == 9) {
                        //Get Loan Amount
                        double loanAmount = savingRepo.getMemberTotalSavingsInCycle(cycle.getCycleId(), memb.getMemberId()) * 3;
                        double interestAmount = loanAmount * (cycle.getInterestRate()*0.01);
                        //Get Date Due for loans issued today
                        Calendar calDateDue = Calendar.getInstance();
                        calDateDue.setTime(meeting.getMeetingDate());
                        calDateDue.add(Calendar.MONTH, 1);

                        //Issue the Loan
                        loanIssuedRepo.saveMemberLoanIssue(meeting.getMeetingId(),memb.getMemberId(),loanNumber++, loanAmount , interestAmount, calDateDue.getTime());
                    }
                }
                catch(Exception ex) {
                    continue;
                }
            }


            //FIFTH MEETING
            //Add Fifth Meeting: One Week Later-Savings only & loan for member 2 and 7
            meeting = new Meeting();
            cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -21); //3 weeks ago
            meeting.setMeetingDate(cal.getTime());
            meeting.setVslaCycle(cycle);
            meetingRepo.addMeeting(meeting);

            //Retrieve that meeting to get the generated Meeting ID
            meeting = meetingRepo.getMostRecentMeeting();

            //If the meeting was not setup just return
            if(meeting.getMeetingId() == 0){
                return false;
            }

            //Otherwise, proceed
            for(Member memb:members){
                //Wrap this in a try...catch so that in case an error occurs on one member we just jump to the next one
                try{
                    //Let all members be present
                    attendanceRepo.saveMemberAttendance(meeting.getMeetingId(),memb.getMemberId(),1);
                    savingRepo.saveMemberSaving(meeting.getMeetingId(), memb.getMemberId(), shareValue * ((memb.getMemberNo() % 5) + 1));

                    //No Repayments made in this meeting

                    //Issue new Loan to Member 2 and 7
                    if(memb.getMemberNo() == 2 || memb.getMemberNo() == 7) {
                        //Get Loan Amount
                        double loanAmount = savingRepo.getMemberTotalSavingsInCycle(cycle.getCycleId(), memb.getMemberId()) * 3;
                        double interestAmount = loanAmount * (cycle.getInterestRate()*0.01);
                        //Get Date Due for loans issued today
                        Calendar calDateDue = Calendar.getInstance();
                        calDateDue.setTime(meeting.getMeetingDate());
                        calDateDue.add(Calendar.MONTH, 1);

                        //Issue the Loan
                        loanIssuedRepo.saveMemberLoanIssue(meeting.getMeetingId(),memb.getMemberId(),loanNumber++, loanAmount , interestAmount, calDateDue.getTime());
                    }
                }
                catch(Exception ex) {
                    continue;
                }
            }

            //All has gone well
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    private static void addMember(int memberNo, String surname, String otherNames, String gender, int age, String occupation, int cyclesCompleted){
        Member member = null;
        MemberRepo repo = null;
        try {
            member = new Member();
            member.setMemberNo(memberNo);
            member.setSurname(surname);
            member.setOtherNames(otherNames);
            member.setOccupation(occupation);
            member.setGender(gender);
            member.activate();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -age);
            member.setDateOfBirth(cal.getTime());
            repo = new MemberRepo(appContext);
            repo.addMember(member);
        }
        catch(Exception ex) {

        }
    }
}
