package org.applab.digitizingdata.repo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.digitizingdata.LoginActivity;
import org.applab.digitizingdata.MainActivity;
import org.applab.digitizingdata.datatransformation.FinesDataTransferRecord;
import org.applab.digitizingdata.datatransformation.LoanDataTransferRecord;
import org.applab.digitizingdata.datatransformation.RepaymentDataTransferRecord;
import org.applab.digitizingdata.datatransformation.SavingsDataTransferRecord;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingFine;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.datatransformation.AttendanceDataTransferRecord;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;
import org.json.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Moses on 10/24/13.
 */
public class SendDataRepo {

    private static String vslaCode = null;
    private static String phoneImei = null;
    private static String networkOperator = null;
    private static String networkType = null;

    //Sending Data Map Constants
    public static final String CYCLE_INFO_ITEM_KEY = "cycleInfo";
    public static final String MEMBERS_ITEM_KEY = "members";
    public static final String MEETING_DETAILS_ITEM_KEY = "meetingDetails";
    public static final String ATTENDANCE_ITEM_KEY = "attendance";
    public static final String SAVINGS_ITEM_KEY = "savings";
    public static final String FINES_ITEM_KEY = "fines";
    public static final String CASHBOOK_ITEM_KEY = "cashBook";
    public static final String OPENING_CASH_ITEM_KEY = "openingCash";
    public static final String LOANS_ITEM_KEY = "loans";
    public static final String REPAYMENTS_ITEM_KEY = "repayments";

    //A Map to hold the order of data sending
    public static Map<Integer, String> meetingDataItems;
    public static Map<Integer, String> progressDialogMessages;

    //The Actual Data e.g. <"members","{...}">
    public static HashMap<String, String> dataToBeSent;

    static {
        meetingDataItems = new HashMap<Integer, String>();
        meetingDataItems.put(1, CYCLE_INFO_ITEM_KEY);
        meetingDataItems.put(2, MEMBERS_ITEM_KEY);
        meetingDataItems.put(3, MEETING_DETAILS_ITEM_KEY);
        meetingDataItems.put(4, ATTENDANCE_ITEM_KEY);
        meetingDataItems.put(5, SAVINGS_ITEM_KEY);
        meetingDataItems.put(6, LOANS_ITEM_KEY);
        meetingDataItems.put(7, REPAYMENTS_ITEM_KEY);
        meetingDataItems.put(8, CASHBOOK_ITEM_KEY);
        meetingDataItems.put(9, OPENING_CASH_ITEM_KEY);
        meetingDataItems.put(10, FINES_ITEM_KEY);

        progressDialogMessages = new HashMap<Integer, String>();
        progressDialogMessages.put(1, "Sending Cycle Information...");
        progressDialogMessages.put(2, "Sending Members Information...");
        progressDialogMessages.put(3, "Sending Meeting Details...");
        progressDialogMessages.put(4, "Sending Attendance Register...");
        progressDialogMessages.put(5, "Sending Savings Register...");
        progressDialogMessages.put(6, "Sending Loan Repayments Register...");
        progressDialogMessages.put(7, "Sending New Loans Issued...");
        progressDialogMessages.put(8, "Sending CashBook Amount...");
        progressDialogMessages.put(9, "Sending Opening Cash Record...");
        progressDialogMessages.put(10, "Sending Fines Issued...");
    }

    private static String getVslaCode() {
        try {
            if(vslaCode == null || vslaCode.length() < 1) {
                VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(DatabaseHandler.databaseContext);
                VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
                if(null != vslaInfo) {
                    vslaCode = vslaInfo.getVslaCode();
                }
            }
            return vslaCode;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private static String getPhoneImei() {
        try {
            if(phoneImei == null || phoneImei.length()<1){
                TelephonyManager tm = (TelephonyManager)DatabaseHandler.databaseContext.getSystemService(Context.TELEPHONY_SERVICE);
                phoneImei = tm.getDeviceId();
            }
            return phoneImei;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private static String getNetworkOperator() {
        try {
            if(networkOperator == null || networkOperator.length()<1){
                TelephonyManager tm = (TelephonyManager)DatabaseHandler.databaseContext.getSystemService(Context.TELEPHONY_SERVICE);
                if(tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                    networkOperator = tm.getNetworkOperatorName();
                    if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE){
                        networkType = "EDGE";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS){
                        networkType = "GPRS";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA){
                        networkType = "HSDPA";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA){
                        networkType = "HSPA";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP){
                        networkType = "HSPAP";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA){
                        networkType = "HSUPA";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                        networkType = "UMTS";
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                        networkType = "LTE";
                    }
                    else {
                        networkType = "UNKNOWN";
                    }
                }
            }
            return networkOperator;
        }
        catch(Exception ex) {
            return null;
        }
    }

    //Use the CycleId to retrieve information about the VSLACycle
    public static String getVslaCycleJson(int cycleId) {
        VslaCycleRepo cycleRepo = null;
        try{
            cycleRepo = new VslaCycleRepo(DatabaseHandler.databaseContext);
            return getVslaCycleJson(cycleRepo.getCycle(cycleId));
        }
        catch(Exception ex) {
            return null;
        }
    }

    //Accept the VslaCycle object and build the JSON String based on it
    public static String getVslaCycleJson(VslaCycle cycle) {

        if(cycle == null) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            jsonRequest = js
                    .object()
                        .key("HeaderInfo")
                        .object()
                            .key("VslaCode").value(getVslaCode())
                            .key("PhoneImei").value(getPhoneImei())
                            .key("NetworkOperator").value(getNetworkOperator())
                            .key("NetworkType").value(networkType)
                            .key("DataItem").value(CYCLE_INFO_ITEM_KEY)
                        .endObject()
                        .key("VslaCycle")
                        .object()
                            .key("CycleId").value(cycle.getCycleId())
                            .key("StartDate").value(Utils.formatDate(cycle.getStartDate(),"yyyy-MM-dd"))
                            .key("EndDate").value(Utils.formatDate(cycle.getEndDate(),"yyyy-MM-dd"))
                            .key("SharePrice").value(cycle.getSharePrice())
                            .key("MaxShareQty").value(cycle.getMaxSharesQty())
                            .key("MaxStartShare").value(cycle.getMaxStartShare())
                            .key("InterestRate").value(cycle.getInterestRate())
                        .endObject()
                    .endObject()
                    .toString();

        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }

        return jsonRequest;
    }

    //Build a JSON String for all members
    public static String getMembersJson() {
        MemberRepo memberRepo = null;
        try{
            memberRepo = new MemberRepo(DatabaseHandler.databaseContext);
            ArrayList<Member> members = memberRepo.getAllMembers();
            return getMembersJson(members);
        }
        catch(Exception ex) {
            return null;
        }
    }

    //Build a JSON String for supplied list of members
    public static String getMembersJson(ArrayList<Member> members) {

        if(members == null) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            js.object()
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(MEMBERS_ITEM_KEY)
                    .endObject()
                .key("MemberCount").value(members.size())
                .key("Members").array();
            for(Member member : members) {
                js.object()
                        .key("MemberId").value(member.getMemberId())
                        .key("MemberNo").value(member.getMemberNo())
                        .key("Surname").value(member.getSurname())
                        .key("OtherNames").value(member.getOtherNames())
                        .key("Gender").value(member.getGender())
                        .key("DateOfBirth").value(Utils.formatDate(member.getDateOfBirth(), "yyyy-MM-dd"))
                        .key("Occupation").value(member.getOccupation())
                        .key("PhoneNumber").value(member.getPhoneNumber())
                        .key("CyclesCompleted").value(member.getCyclesCompleted())
                        .key("IsActive").value(member.isActive())
                        .key("IsArchived").value(false)
                    .endObject();
            }

            js.endArray()
            .endObject();

            jsonRequest = js.toString();

        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }

        return jsonRequest;
    }

    public static String getMeetingJson(Meeting meeting) {

        if(meeting == null) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingSavingRepo savingRepo = new MeetingSavingRepo(DatabaseHandler.databaseContext);
            MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(DatabaseHandler.databaseContext);
            MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(DatabaseHandler.databaseContext);
            MeetingLoanRepaymentRepo loanRepaymentRepo = new MeetingLoanRepaymentRepo(DatabaseHandler.databaseContext);

            double membersPresent = attendanceRepo.getAttendanceCountByMeetingId(meeting.getMeetingId(), 1);
            double savings = savingRepo.getTotalSavingsInMeeting(meeting.getMeetingId());
            double loansRepaid = loanRepaymentRepo.getTotalLoansRepaidInMeeting(meeting.getMeetingId());
            double loansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meeting.getMeetingId());

            jsonRequest = js
                .object()
                    .key("HeaderInfo")
                .object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(MEETING_DETAILS_ITEM_KEY)
                .endObject()
                    .key("Meeting")
                .object()
                    .key("CycleId").value((meeting.getVslaCycle() != null) ? meeting.getVslaCycle().getCycleId(): 0)
                    .key("MeetingId").value(meeting.getMeetingId())
                    .key("MeetingDate").value(Utils.formatDate(meeting.getMeetingDate(), "yyyy-MM-dd"))
                    .key("OpeningBalanceBox").value(meeting.getOpeningBalanceBox())
                    .key("OpeningBalanceBank").value(meeting.getOpeningBalanceBank())
                    .key("Fines").value(meeting.getFines())
                    .key("MembersPresent").value(membersPresent)
                    .key("Savings").value(savings)
                    .key("LoansRepaid").value(loansRepaid)
                    .key("LoansIssued").value(loansIssued)
                    .key("ClosingBalanceBox").value(meeting.getClosingBalanceBox())
                    .key("ClosingBalanceBank").value(meeting.getClosingBalanceBank())
                    .key("IsCashBookBalanced").value(meeting.isCashBookBalanced())
                    .key("IsDataSent").value(meeting.isMeetingDataSent())
                .endObject()
                .endObject()
                .toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }

    public static String getMeetingAttendanceJson(int meetingId) {

        if(meetingId == 0) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(DatabaseHandler.databaseContext);
            ArrayList<AttendanceDataTransferRecord> attendances = attendanceRepo.getMeetingAttendanceForAllMembers(meetingId);
            js.object()
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(ATTENDANCE_ITEM_KEY)
                    .endObject()
                .key("MeetingId").value(meetingId)
                .key("MembersCount").value(attendances.size())
                .key("Attendances").array();
                for(AttendanceDataTransferRecord record : attendances) {
                    js.object()
                        .key("AttendanceId").value(record.getAttendanceId())
                        .key("MemberId").value(record.getMemberId())
                        .key("IsPresentFlg").value(record.getPresentFlg())
                        .key("Comments").value(record.getComments())
                        .endObject();
                }
                js.endArray()
                .endObject();
                jsonRequest = js.toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }

    public static String getMeetingSavingsJson(int meetingId) {

        if(meetingId == 0) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingSavingRepo savingRepo = new MeetingSavingRepo(DatabaseHandler.databaseContext);
            ArrayList<SavingsDataTransferRecord> savings= savingRepo.getMeetingSavingsForAllMembers(meetingId);
            js.object()
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(SAVINGS_ITEM_KEY)
                    .endObject()
                    .key("MeetingId").value(meetingId)
                    .key("MembersCount").value(savings.size())
                    .key("Savings").array();
            for(SavingsDataTransferRecord record : savings) {
                js.object()
                        .key("SavingId").value(record.getSavingsId())
                        .key("MemberId").value(record.getMemberId())
                        .key("Amount").value(record.getAmount())
                        .endObject();
            }
            js.endArray()
                    .endObject();
            jsonRequest = js.toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }






    public static String getMeetingRepaymentsJson(int meetingId) {

        if(meetingId == 0) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingLoanRepaymentRepo repayRepo = new MeetingLoanRepaymentRepo(DatabaseHandler.databaseContext);
            ArrayList<RepaymentDataTransferRecord> repayments= repayRepo.getMeetingRepaymentsForAllMembers(meetingId);
            js.object()
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(REPAYMENTS_ITEM_KEY)
                    .endObject()
                    .key("MeetingId").value(meetingId)
                    .key("MembersCount").value(repayments.size())
                    .key("Repayments").array();
            for(RepaymentDataTransferRecord record : repayments) {
                js.object()
                        .key("RepaymentId").value(record.getRepaymentId())
                        .key("MemberId").value(record.getMemberId())
                        .key("LoanId").value(record.getLoanId())
                        .key("Amount").value(record.getAmount())
                        .key("BalanceBefore").value(record.getBalanceBefore())
                        .key("BalanceAfter").value(record.getBalanceAfter())
                        .key("InterestAmount").value(record.getInterestAmount())
                        .key("RolloverAmount").value(record.getRollOverAmount())
                        .key("Comments").value(record.getComments())
                        .key("LastDateDue").value(Utils.formatDate(record.getLastDateDue(), "yyyy-MM-dd"))
                        .key("NextDateDue").value(Utils.formatDate(record.getNextDateDue(), "yyyy-MM-dd"))
                        .endObject();
            }
            js.endArray()
                    .endObject();
            jsonRequest = js.toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }

    public static String getMeetingLoanIssuesJson(int meetingId) {

        if(meetingId == 0) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingLoanIssuedRepo loanRepo = new MeetingLoanIssuedRepo(DatabaseHandler.databaseContext);
            ArrayList<LoanDataTransferRecord> loans = loanRepo.getMeetingLoansForAllMembers(meetingId);
            js.object()
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(LOANS_ITEM_KEY)
                    .endObject()
                    .key("MeetingId").value(meetingId)
                    .key("MembersCount").value(loans.size())
                    .key("Loans").array();
            for(LoanDataTransferRecord record : loans) {
                js.object()
                    .key("MemberId").value(record.getMemberId())
                    .key("LoanId").value(record.getLoanId())
                    .key("LoanNo").value(record.getLoanNo())
                    .key("PrincipalAmount").value(record.getPrincipalAmount())
                    .key("InterestAmount").value(record.getInterestAmount())
                    .key("TotalRepaid").value(record.getTotalRepaid())
                    .key("LoanBalance").value(record.getLoanBalance())
                    .key("DateDue").value(Utils.formatDate(record.getDateDue(),"yyyy-MM-dd"))
                    .key("Comments").value(record.getComments())
                    .key("DateCleared").value(Utils.formatDate(record.getDateCleared(), "yyyy-MM-dd"))
                    .key("IsCleared").value(record.isCleared())
                    .key("IsDefaulted").value(record.isDefaulted())
                    .key("IsWrittenOff").value(record.isWrittenOff())
                    .endObject();
            }
            js.endArray()
                    .endObject();
            jsonRequest = js.toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }

    public static String getMeetingFinesJson(int meetingId) {

        if(meetingId == 0) {
            return null;
        }

        //Build JSON input string
        JSONStringer js = new JSONStringer();
        String jsonRequest = null;

        try {
            MeetingFineRepo fineRepo = new MeetingFineRepo(DatabaseHandler.databaseContext);
            ArrayList<FinesDataTransferRecord> fines= fineRepo.getMeetingFinesForAllMembers(meetingId);
            js.object()
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .key("DataItem").value(FINES_ITEM_KEY)
                    .endObject()
                    .key("MeetingId").value(meetingId)
                    .key("MembersCount").value(fines.size())
                    .key("Fines").array();
            for(FinesDataTransferRecord record : fines) {
                js.object()
                        .key("FineId").value(record.getFinesId())
                        .key("MemberId").value(record.getMemberId())
                        .key("Amount").value(record.getAmount())
                        .endObject();
            }
            js.endArray()
                    .endObject();
            jsonRequest = js.toString();
        }
        catch(JSONException ex) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
        return jsonRequest;
    }

}
