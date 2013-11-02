package org.applab.digitizingdata.repo;

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
import org.applab.digitizingdata.datatransformation.LoanDataTransferRecord;
import org.applab.digitizingdata.datatransformation.RepaymentDataTransferRecord;
import org.applab.digitizingdata.datatransformation.SavingsDataTransferRecord;
import org.applab.digitizingdata.domain.model.Meeting;
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
    public static final String LOANS_ITEM_KEY = "loans";
    public static final String REPAYMENTS_ITEM_KEY = "repayments";


    //Sending variables
    private static HttpClient client;
    private static int httpStatusCode = 0; //To know whether the Request was successful
    private static boolean actionSucceeded = false;
    private static int targetMeetingId = 0;
    //String targetVslaCode = null; //fake-fix

    //A Map to hold the order of data sending
    private static Map<Integer, String> meetingDataItems;

    //The Actual Data e.g. <"members","{...}">
    private static HashMap<String, String> dataToBeSent;
    private static int currentDataItemPosition = 0;
    private static String serverUri = "";

    static {
        meetingDataItems = new HashMap<Integer, String>();
        meetingDataItems.put(1, "cycleInfo");
        meetingDataItems.put(2, "members");
        meetingDataItems.put(3, "meetingDetails");
        meetingDataItems.put(4, "attendance");
        meetingDataItems.put(5, "savings");
        meetingDataItems.put(6, "loans");
        meetingDataItems.put(7, "repayments");
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
                    .key("HeaderInfo").object()
                    .key("VslaCode").value(getVslaCode())
                    .key("PhoneImei").value(getPhoneImei())
                    .key("NetworkOperator").value(getNetworkOperator())
                    .key("NetworkType").value(networkType)
                    .endObject()
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

    public static void sendDataUsingPostAsync(String request) {
        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,"vslas","submitdata");
        new SendDataPostAsyncTask().execute(uri,request);

        //Do the other stuff in the Async Task
    }

    public static void sendDataUsingPostAsync(int meetingId, HashMap<String, String> dataFromPhone) {
        //Store the MeetingId as it will be used later after the Async process
        targetMeetingId = meetingId;

        //First identify the initial data to be sent
        dataToBeSent = dataFromPhone;
        currentDataItemPosition = 1;
        String request = dataToBeSent.get(meetingDataItems.get(currentDataItemPosition));
        serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,"vslas","submitdata");
        new SendDataPostAsyncTask().execute(serverUri, request);
    }

    // The definition of our task class
    private static class SendDataPostAsyncTask extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //displayProgressBar("Downloading...");
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject result = null;
            String uri = params[0];
            try {
                //instantiates httpclient to make request
                DefaultHttpClient httpClient = new DefaultHttpClient();

                //url with the post data
                HttpPost httpPost = new HttpPost(uri);

                //passes the results to a string builder/entity
                StringEntity se = new StringEntity(params[1]);

                //sets the post request as the resulting string
                httpPost.setEntity(se);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Response handler
                ResponseHandler<String> rh = new ResponseHandler<String>() {
                    // invoked when client receives response
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

                        // get response entity
                        HttpEntity entity = response.getEntity();
                        httpStatusCode = response.getStatusLine().getStatusCode();

                        // read the response as byte array
                        StringBuffer out = new StringBuffer();
                        byte[] b = EntityUtils.toByteArray(entity);

                        // write the response byte array to a string buffer
                        out.append(new String(b, 0, b.length));
                        return out.toString();
                    }
                };

                String responseString = httpClient.execute(httpPost, rh);

                // close the connection
                httpClient.getConnectionManager().shutdown();

                if(httpStatusCode == 200) //sucess
                {
                    result = new JSONObject(responseString);
                }

                return result;
            }
            catch(ClientProtocolException exClient) {
                return null;
            }
            catch(IOException exIo) {
                return null;
            }
            catch(JSONException exJson) {
                return null;
            }
            catch(Exception ex) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //updateProgressBar(values[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            try {
                if(result != null) {
                    actionSucceeded = ((result.getInt("StatusCode") == 0) ? true : false);
                }
                if(actionSucceeded) {
                    //Record that the piece of info has been submitted
                    //Pick and Post the next piece of item if there is any RECURSION
                    currentDataItemPosition++;
                    String nextRequest = dataToBeSent.get(meetingDataItems.get(currentDataItemPosition));
                    if(nextRequest != null) {
                        new SendDataPostAsyncTask().execute(serverUri, nextRequest);
                    }
                    else {
                        //Finished
                        //Have some code to run when process is finished
                        Toast.makeText(DatabaseHandler.databaseContext, "Meeting Data was Sent Successfully",Toast.LENGTH_SHORT).show();

                        //If the process has finished, then mark the meeting as sent
                        Calendar cal = Calendar.getInstance();
                        MeetingRepo meetingRepo = new MeetingRepo(DatabaseHandler.databaseContext);
                        meetingRepo.updateDataSentFlag(targetMeetingId, true, cal.getTime());
                    }
                }
                else {
                    //Process failed
                    Toast.makeText(DatabaseHandler.databaseContext, "Sending of Meeting Data failed during communication error. Try again later.",Toast.LENGTH_LONG).show();
                }
            }
            catch(JSONException exJson) {
                //Process failed
                Toast.makeText(DatabaseHandler.databaseContext, "Sending of Meeting Data failed due to a data format error. Try again later.",Toast.LENGTH_LONG).show();
            }
            catch(Exception ex) {
                //Process failed
                Toast.makeText(DatabaseHandler.databaseContext, "Sending of Meeting Data failed. Try again later.",Toast.LENGTH_LONG).show();
            }
        }
    }
}
