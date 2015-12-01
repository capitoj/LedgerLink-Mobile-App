package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.content.Intent;

import org.applab.ledgerlink.LoginActivity;
import org.applab.ledgerlink.datatransformation.FinesDataTransferRecord;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingFine;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.repo.DataRecoveryRepo;
import org.applab.ledgerlink.domain.model.MeetingAttendance;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.MeetingLoanRepayment;
import org.applab.ledgerlink.domain.model.MeetingSaving;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Joseph Capito on 9/23/2015.
 */
public class DataRecoveryFactory extends DataRecoveryRepo {

    protected Context context;
    protected JSONObject jsonObject;
    protected VslaCycle vslaCycle;

    public DataRecoveryFactory(Context context, JSONObject jsonObject){
        super(context);
        this.context = context;
        this.jsonObject = jsonObject;
        this.vslaCycle = new VslaCycle();
    }

    protected Date formatStringToDate(String jsonDate){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formatedDate = jsonDate.replace('T', ' ');
            return simpleDateFormat.parse(formatedDate);
        }catch (Exception e){
            return null;
        }
    }

    protected void saveVslaData(){
        VslaInfo vslaInfo = new VslaInfo();
        try{
            vslaInfo.setVslaCode(jsonObject.getString("VslaCode"));
            vslaInfo.setVslaName(jsonObject.getString("VslaName"));
            vslaInfo.setPassKey(jsonObject.getString("PassKey"));
            if(jsonObject.getString("DateRegistered") != "null") {
                Date dateRegistered = this.formatStringToDate(jsonObject.getString("DateRegistered"));
                vslaInfo.setDateRegistered(dateRegistered);
            }
            if(jsonObject.getString("DateLinked") != "null") {
                Date dateLinked = this.formatStringToDate(jsonObject.getString("DateLinked"));
                vslaInfo.setDateLinked(dateLinked);
            }
            this.addVslaInfo(vslaInfo);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveCycleData(){
        //VslaCycle vslaCycle = new VslaCycle();
        try{
            JSONArray jsonArray = jsonObject.getJSONArray("Cycle");
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            vslaCycle.setCycleId(Integer.valueOf(jsonObject.getString("CycleIdEx")));
            vslaCycle.setEndDate(null);
            if(!jsonObject.getString("EndDate").equalsIgnoreCase("null")){
                Date endDate = formatJSONDate(jsonObject.getString("EndDate"));
                vslaCycle.setEndDate(endDate);
            }
            vslaCycle.setStartDate(null);
            if(!jsonObject.getString("StartDate").equalsIgnoreCase("null")){
                Date startDate = formatJSONDate(jsonObject.getString("StartDate"));
                vslaCycle.setStartDate(startDate);
            }
            vslaCycle.setCycleCode(null);
            if(!jsonObject.getString("CycleCode").equalsIgnoreCase("null")){
                vslaCycle.setCycleCode(jsonObject.getString("CycleCode"));
            }
            vslaCycle.setInterestRate(Double.valueOf(jsonObject.getString("InterestRate")));
            vslaCycle.setInterestAtSetup(0);
            if(!jsonObject.getString("MigratedInterest").equalsIgnoreCase("null")) {
                vslaCycle.setInterestAtSetup(Double.valueOf(jsonObject.getString("MigratedInterest")));
            }
            vslaCycle.setMaxSharesQty(Double.valueOf(jsonObject.getString("MaxShareQuantity")));
            vslaCycle.setMaxStartShare(Double.valueOf(jsonObject.getString("MaxStartShare")));
            vslaCycle.setSharePrice(Double.valueOf(jsonObject.getString("SharePrice")));
            vslaCycle.setFinesAtSetup(0);
            if(!jsonObject.getString("MigratedFines").equalsIgnoreCase("null")) {
                vslaCycle.setFinesAtSetup(Double.valueOf(jsonObject.getString("MigratedFines")));
            }
            vslaCycle.activate();
            this.addCycle(vslaCycle);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveMemberData(){
        try{
            JSONArray jsonArray = jsonObject.getJSONArray("Members");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Member member = new Member();
                member.setMemberId(Integer.valueOf(jsonObject.getString("MemberIdEx")));
                member.setMemberNo(Integer.valueOf(jsonObject.getString("MemberNo")));
                member.setSurname(jsonObject.getString("Surname"));
                member.setOtherNames(jsonObject.getString("OtherNames"));
                member.setGender(jsonObject.getString("Gender"));
                member.setOccupation(jsonObject.getString("Occupation"));
                member.setPhoneNumber(null);
                if(!jsonObject.getString("PhoneNo").equalsIgnoreCase("null"))
                    member.setPhoneNumber(jsonObject.getString("PhoneNo"));
                member.setDateOfBirth(formatJSONDate(jsonObject.getString("DateOfBirth")));
                member.setDateOfAdmission(formatJSONDate(jsonObject.getString("DateArchived")));
                this.addMember(member);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveMeetingData(){
        try{
            JSONArray jsonArray = jsonObject.getJSONArray("Meetings");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Meeting meeting = new Meeting();
                meeting.setMeetingId(Integer.valueOf(jsonObject.getString("MeetingIdEx")));
                meeting.setOpeningBalanceBox(Double.valueOf(jsonObject.getString("CashFromBox")));
                meeting.setOpeningBalanceBank(Double.valueOf(jsonObject.getString("CashFromBank")));
                meeting.setClosingBalanceBank(Double.valueOf(jsonObject.getString("CashSavedBank")));
                meeting.setClosingBalanceBox(Double.valueOf(jsonObject.getString("CashSavedBox")));
                meeting.setIsCurrent(Boolean.valueOf(jsonObject.getString("IsCurrent")));
                meeting.setDateSent(this.formatJSONDate(jsonObject.getString("DateSent")));
                meeting.setMeetingDataSent(Boolean.valueOf(jsonObject.getString("IsDataSent")));
                meeting.setMeetingDate(this.formatJSONDate(jsonObject.getString("MeetingDate")));
                meeting.setVslaCycle(this.vslaCycle);
                if(i == 0)
                    meeting.setGettingStarted(true);
                else
                    meeting.setGettingStarted(false);
                if(!this.hasMeeting(meeting.getMeetingId())) {
                    this.addMeeting(meeting);

                    this.saveMemberAttendance(jsonObject);

                    this.saveMemberSavings(jsonObject);

                    this.saveLoansIssued(jsonObject);

                    this.saveLoanRepayment(jsonObject);

                    this.saveFines(jsonObject);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void saveMemberAttendance(JSONObject jsonObject){
        try{
            JSONArray attendanceJSONArray = jsonObject.getJSONArray("Attendance");
            for(int j = 0; j < attendanceJSONArray.length(); j++){
                MeetingAttendance meetingAttendance = new MeetingAttendance();
                JSONObject attendanceJSONObject = attendanceJSONArray.getJSONObject(j);
                meetingAttendance.setAttendanceId(Integer.valueOf(attendanceJSONObject.getString("AttendanceIdEx")));
                meetingAttendance.setComment(attendanceJSONObject.getString("Comments"));
                boolean isPresent = Boolean.valueOf(attendanceJSONObject.getString("IsPresent"));
                meetingAttendance.setPresent(isPresent);
                Meeting meeting = new Meeting();
                meeting.setMeetingId(Integer.valueOf(attendanceJSONObject.getString("MeetingIdEx")));
                meetingAttendance.setMeeting(meeting);
                Member member = new Member();
                member.setMemberId(Integer.valueOf(attendanceJSONObject.getString("MemberIdEx")));
                meetingAttendance.setMember(member);
                if(!this.hasAttendance(meetingAttendance.getAttendanceId()))
                    this.addAttendance(meetingAttendance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveMemberSavings(JSONObject jsonObject){
        try{
            JSONArray savingJSONArray = jsonObject.getJSONArray("Savings");
            for(int j = 0; j < savingJSONArray.length(); j++){
                MeetingSaving meetingSaving = new MeetingSaving();
                JSONObject savingJSONObject = savingJSONArray.getJSONObject(j);
                meetingSaving.setAmount(Double.valueOf(savingJSONObject.getString("Amount")));
                meetingSaving.setSavingId(Integer.valueOf(savingJSONObject.getString("SavingIdEx")));
                Member member = new Member();
                member.setMemberId(Integer.valueOf(savingJSONObject.getString("MemberIdEx")));
                meetingSaving.setMember(member);
                Meeting meeting = new Meeting();
                meeting.setMeetingId(Integer.valueOf(savingJSONObject.getString("MeetingIdEx")));
                meetingSaving.setMeeting(meeting);
                if(!this.hasSavings(meetingSaving.getSavingId()))
                    this.addSavings(meetingSaving);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveFines(JSONObject jsonObject){
        try{
            JSONArray finesJSONArray = jsonObject.getJSONArray("Fines");
            for(int i = 0; i < finesJSONArray.length(); i++){
                FinesDataTransferRecord meetingFine = new FinesDataTransferRecord();
                JSONObject fineJSONObject = finesJSONArray.getJSONObject(i);
                meetingFine.setFinesId(Integer.valueOf(fineJSONObject.getString("FineIdEx")));
                meetingFine.setAmount(Double.valueOf(fineJSONObject.getString("Amount")));
                meetingFine.setMemberId(fineJSONObject.getInt("MemberIdEx"));
                meetingFine.setMeetingId(fineJSONObject.getInt("MeetingIdEx"));
                int paidInMeeting = fineJSONObject.getInt("PaidInMeetingIdEx");
                meetingFine.setPaidInMeeting(paidInMeeting);
                if(paidInMeeting > 0)
                    meetingFine.setCleared(true);
                else
                    meetingFine.setCleared(false);

                meetingFine.setFineTypeId(Integer.valueOf(fineJSONObject.getString("FineTypeId")));

                meetingFine.setDateCleared(this.formatJSONDate(fineJSONObject.getString("DateCleared")));

                MeetingFineRepo meetingFineRepo = new MeetingFineRepo(this.context);
                meetingFineRepo.saveMemberFine(meetingFine);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveLoansIssued(JSONObject jsonObject){
        try{
            JSONArray loanIssuedJSONArray = jsonObject.getJSONArray("LoanIssues");
            for(int j = 0; j < loanIssuedJSONArray.length(); j++){
                MeetingLoanIssued meetingLoanIssued = new MeetingLoanIssued();
                JSONObject loanIssuedJSONObject = loanIssuedJSONArray.getJSONObject(j);
                Meeting meeting = new Meeting();
                meeting.setMeetingId(Integer.valueOf(loanIssuedJSONObject.getString("MeetingIdEx")));
                meetingLoanIssued.setMeeting(meeting);
                Member member = new Member();
                member.setMemberId(Integer.valueOf(loanIssuedJSONObject.getString("MemberIdEx")));
                meetingLoanIssued.setMember(member);
                meetingLoanIssued.setLoanId(Integer.valueOf(loanIssuedJSONObject.getString("LoanIdEx")));
                meetingLoanIssued.setLoanNo(Integer.valueOf(loanIssuedJSONObject.getString("LoanNo")));
                meetingLoanIssued.setPrincipalAmount(Double.valueOf(loanIssuedJSONObject.getString("PrincipalAmount")));
                meetingLoanIssued.setInterestAmount(Double.valueOf(loanIssuedJSONObject.getString("InterestAmount")));
                meetingLoanIssued.setLoanBalance(Double.valueOf(loanIssuedJSONObject.getString("Balance")));
                meetingLoanIssued.setComment(loanIssuedJSONObject.getString("Comments"));
                meetingLoanIssued.setDateCleared(this.formatJSONDate(loanIssuedJSONObject.getString("DateCleared")));
                meetingLoanIssued.setDateDue(this.formatJSONDate(loanIssuedJSONObject.getString("DateDue")));
                meetingLoanIssued.setTotalRepaid(Double.valueOf(loanIssuedJSONObject.getString("TotalRepaid")));
                meetingLoanIssued.setCleared(Boolean.valueOf(loanIssuedJSONObject.getString("IsCleared")));
                meetingLoanIssued.setDefaulted(Boolean.valueOf(loanIssuedJSONObject.getString("IsDefaulted")));
                meetingLoanIssued.setWrittenOff(Boolean.valueOf(loanIssuedJSONObject.getString("IsWrittenOff")));
                if(!this.hasLoanIssue(meetingLoanIssued.getLoanId()))
                    this.addLoanIssues(meetingLoanIssued);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void saveLoanRepayment(JSONObject jsonObject){
        try{
            JSONArray jsonArray = jsonObject.getJSONArray("LoanRepayments");
            for(int i = 0; i < jsonArray.length(); i++){
                MeetingLoanRepayment meetingLoanRepayment = new MeetingLoanRepayment();
                JSONObject loanRepaymentJSONObject = jsonArray.getJSONObject(i);
                meetingLoanRepayment.setRepaymentId(Integer.valueOf(loanRepaymentJSONObject.getString("RepaymentIdEx")));
                meetingLoanRepayment.setAmount(Double.valueOf(loanRepaymentJSONObject.getString("Amount")));
                meetingLoanRepayment.setBalanceAfter(Double.valueOf(loanRepaymentJSONObject.getString("BalanceAfter")));
                meetingLoanRepayment.setBalanceBefore(Double.valueOf(loanRepaymentJSONObject.getString("BalanceBefore")));
                meetingLoanRepayment.setComment(loanRepaymentJSONObject.getString("Comments"));
                meetingLoanRepayment.setLastDateDue(this.formatJSONDate(loanRepaymentJSONObject.getString("LastDateDue")));
                meetingLoanRepayment.setNextDateDue(this.formatJSONDate(loanRepaymentJSONObject.getString("NextDateDue")));
                meetingLoanRepayment.setInterestAmount(Double.valueOf(loanRepaymentJSONObject.getString("InterestAmount")));
                meetingLoanRepayment.setRollOverAmount(Double.valueOf(loanRepaymentJSONObject.getString("RolloverAmount")));
                Meeting meeting = new Meeting();
                meeting.setMeetingId(Integer.valueOf(loanRepaymentJSONObject.getString("MeetinIdEx")));
                meetingLoanRepayment.setMeeting(meeting);

                Member member = new Member();
                member.setMemberId(Integer.valueOf(loanRepaymentJSONObject.getString("MemberIdEx")));
                meetingLoanRepayment.setMember(member);

                MeetingLoanIssued meetingLoanIssued = new MeetingLoanIssued();
                meetingLoanIssued.setLoanId(Integer.valueOf(loanRepaymentJSONObject.getString("LoanIdEx")));
                meetingLoanRepayment.setMeetingLoanIssued(meetingLoanIssued);
                if(!this.hasLoanRepayment(meetingLoanRepayment.getRepaymentId()))
                    this.addLoanRepayment(meetingLoanRepayment);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected Date formatJSONDate(String jsonDate){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String formattedDate = jsonDate.replace('T', ' ');
            return simpleDateFormat.parse(formattedDate);
        }catch (Exception e){
            return null;
        }
    }

    public static void download(final Context context, JSONObject jsonObject){
        try{
            String jsonData = jsonObject.getString("DataRecoveryResult");
            JSONObject jObject = new JSONObject(jsonData);
            if(jObject.has("Status")){
                String status = jObject.getString("Status");
                if(status.equals("404")){
                    DialogMessageBox.show(context, "Data Recovery Failure", "The Data Recovery was not successful because the recovery information provided is invalid");
                }
            }else{
                DataRecoveryFactory factory = new DataRecoveryFactory(context, jObject);
                factory.saveVslaData();
                factory.saveCycleData();
                factory.saveMemberData();
                factory.saveMeetingData();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                };
                DialogMessageBox.show(context, "Data Recovery", "The Data Recovery was successful", runnable);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
