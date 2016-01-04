package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.applab.ledgerlink.datatransformation.AttendanceDataTransferRecord;
import org.applab.ledgerlink.datatransformation.FinesDataTransferRecord;
import org.applab.ledgerlink.datatransformation.LoanDataTransferRecord;
import org.applab.ledgerlink.datatransformation.RepaymentDataTransferRecord;
import org.applab.ledgerlink.datatransformation.SavingsDataTransferRecord;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.repo.MemberRepo;
import org.applab.ledgerlink.repo.SendDataRepo;
import org.json.JSONArray;
import org.json.JSONStringer;

import java.util.ArrayList;

/**
 * Created by Joseph Capito on 10/22/2015.
 */
public class DataFactory extends SendDataRepo {

    private String phoneImei;
    private String networkOperator;
    private String networkType;
    private Context context;

    public DataFactory(Context context, int meetingId){
        super(context, meetingId);
        this.context = context;
    }

    private String getPhoneImei() {
        try {
            if(phoneImei == null || phoneImei.length()<1){
                TelephonyManager tm = (TelephonyManager)this.context.getSystemService(Context.TELEPHONY_SERVICE);
                phoneImei = tm.getDeviceId();
            }
            return phoneImei;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private void setNetworkType(String networkType){
        this.networkType = networkType;
    }

    private String getNetworkType(){
        return this.networkType;
    }

    private String getNetworkOperator() {
        try {
            if(networkOperator == null || networkOperator.length()<1){
                TelephonyManager tm = (TelephonyManager)this.context.getSystemService(Context.TELEPHONY_SERVICE);
                if(tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                    networkOperator = tm.getNetworkOperatorName();
                    if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE){
                        this.setNetworkType("EDGE");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS){
                        this.setNetworkType("GPRS");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA){
                        this.setNetworkType("HSDPA");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA){
                        this.setNetworkType("HSPA");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP){
                        this.setNetworkType("HSPAP");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA){
                        this.setNetworkType("HSUPA");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                        this.setNetworkType("UMTS");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                        this.setNetworkType("LTE");
                    }
                    else {
                        this.setNetworkType("UNKNOWN");
                    }
                }
            }
            return networkOperator;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private JSONStringer getHeaderInfo(JSONStringer js){
        try{
            js.key("HeaderInfo")
                    .object()
                    .key("VslaCode").value(this.vslaInfo.getVslaCode())
                    .key("PhoneImei").value(this.getPhoneImei())
                    .key("NetworkOperator").value(this.getNetworkOperator())
                    .key("NetworkType").value(this.getNetworkType())
                    .endObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getCycleInfo(JSONStringer js){
        try{
            js.key("VslaCycleInfo")
                    .object()
                    .key("CycleId").value(String.valueOf(this.vslaCycle.getCycleId()))
                    .key("StartDate").value(Utils.formatDate(this.vslaCycle.getStartDate(), "yyyy-MM-dd"))
                    .key("EndDate").value(Utils.formatDate(this.vslaCycle.getEndDate(), "yyyy-MM-dd"))
                    .key("SharePrice").value(String.valueOf(this.vslaCycle.getSharePrice()))
                    .key("MaxShareQty").value(String.valueOf((int)this.vslaCycle.getMaxSharesQty()))
                    .key("MaxStartShare").value(String.valueOf(this.vslaCycle.getMaxStartShare()))
                    .key("InterestRate").value(String.valueOf(this.vslaCycle.getInterestRate()))
                    .endObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getMembersInfo(JSONStringer js){
        try{
            js.key("MembersInfo").array();
            for(Member member : members){
                js.object()
                        .key("MemberId").value(String.valueOf(member.getMemberId()))
                        .key("MemberNo").value(String.valueOf(member.getMemberNo()))
                        .key("Surname").value(member.getSurname())
                        .key("OtherNames").value(member.getOtherNames())
                        .key("Gender").value(member.getGender())
                        .key("DateOfBirth").value(Utils.formatDate(member.getDateOfBirth(), "yyyy-MM-dd"))
                        .key("Occupation").value(member.getOccupation())
                        .key("PhoneNumber").value(member.getPhoneNumber())
                        .key("CyclesCompleted").value(String.valueOf(member.getCyclesCompleted()))
                        .key("IsActive").value(member.isActive())
                        .key("IsArchived").value(String.valueOf(false))
                        .endObject();
            }
            js.endArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getMeetingInfo(JSONStringer js){
        try{
            js.key("MeetingInfo")
                    .object()
                    .key("CycleId").value(String.valueOf(this.vslaCycle.getCycleId()))
                    .key("MeetingId").value(String.valueOf(this.meeting.getMeetingId()))
                    .key("MeetingDate").value(Utils.formatDate(this.meeting.getMeetingDate(), "yyyy-MM-dd"))
                    .key("OpeningBalanceBox").value(String.valueOf(this.meeting.getOpeningBalanceBox()))
                    .key("OpeningBalanceBank").value(String.valueOf(this.meeting.getOpeningBalanceBank()))
                    .key("Fines").value(String.valueOf(this.getTotalFinesPaid()))
                    .key("MembersPresent").value(String.valueOf((int)this.getMembersPresent()))
                    .key("Savings").value(String.valueOf(this.getTotalSavings()))
                    .key("LoansPaid").value(String.valueOf(this.getTotalLoansPaid()))
                    .key("LoansIssued").value(String.valueOf(this.getTotalLoansIssued()))
                    .key("ClosingBalanceBox").value(String.valueOf(this.meeting.getClosingBalanceBox()))
                    .key("ClosingBalanceBank").value(String.valueOf(this.meeting.getClosingBalanceBank()))
                    .key("IsCashBookBalanced").value(String.valueOf(this.meeting.isCashBookBalanced()))
                    .key("IsDataSent").value(String.valueOf(this.meeting.isMeetingDataSent()))
                    .endObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getAttendance(JSONStringer js){
        try{
            js.key("AttendanceInfo").array();
            for(AttendanceDataTransferRecord record : this.attendances){
                js.object()
                        .key("AttendanceId").value(String.valueOf(record.getAttendanceId()))
                        .key("MemberId").value(String.valueOf(record.getMemberId()))
                        .key("IsPresentFlag").value(String.valueOf(record.getPresentFlg()))
                        .key("Comments").value(record.getComments())
                        .endObject();
            }
            js.endArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getSaving(JSONStringer js){
        try{
            js.key("SavingInfo").array();
            for(SavingsDataTransferRecord record : this.savings){
                js.object()
                        .key("SavingId").value(String.valueOf(record.getSavingsId()))
                        .key("MemberId").value(String.valueOf(record.getMemberId()))
                        .key("Amount").value(String.valueOf(record.getAmount()))
                        .endObject();
            }
            js.endArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getLoanIssues(JSONStringer js){
        try{
            js.key("LoansInfo").array();
            for(LoanDataTransferRecord record: this.loanIssues){
                js.object()
                        .key("MemberId").value(String.valueOf(record.getMemberId()))
                        .key("LoanId").value(String.valueOf(record.getLoanId()))
                        .key("PrincipalAmount").value(String.valueOf(record.getPrincipalAmount()))
                        .key("InterestAmount").value(String.valueOf(record.getInterestAmount()))
                        .key("TotalRepaid").value(String.valueOf(record.getTotalRepaid()))
                        .key("LoanBalance").value(String.valueOf(record.getLoanBalance()))
                        .key("DateDue").value(Utils.formatDate(record.getDateDue(), "yyyy-MM-dd"))
                        .key("Comments").value(record.getComments())
                        .key("DateCleared").value(Utils.formatDate(record.getDateCleared(), "yyyy-MM-dd"))
                        .key("IsCleared").value(String.valueOf(record.isCleared()))
                        .key("IsDefaulted").value(String.valueOf(record.isDefaulted()))
                        .key("IsWrittenOff").value(String.valueOf(record.isWrittenOff()))
                        .endObject();
            }
            js.endArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getLoanRepayments(JSONStringer js){
        try{
            js.key("RepaymentsInfo").array();
            for(RepaymentDataTransferRecord record: this.loanRepayments){
                js.object()
                        .key("RepaymentId").value(String.valueOf(record.getRepaymentId()))
                        .key("MemberId").value(String.valueOf(record.getMemberId()))
                        .key("LoanId").value(String.valueOf(record.getLoanId()))
                        .key("Amount").value(String.valueOf(record.getAmount()))
                        .key("BalanceBefore").value(String.valueOf(record.getBalanceBefore()))
                        .key("BalanceAfter").value(String.valueOf(record.getBalanceAfter()))
                        .key("InterestAmount").value(String.valueOf(record.getInterestAmount()))
                        .key("RolloverAmount").value(String.valueOf(record.getRollOverAmount()))
                        .key("Comments").value(record.getComments())
                        .key("LastDateDue").value(Utils.formatDate(record.getLastDateDue(), "yyyy-MM-dd"))
                        .key("NextDateDue").value(Utils.formatDate(record.getNextDateDue(), "yyyy-MM-dd"))
                        .endObject();
            }
            js.endArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    private JSONStringer getFines(JSONStringer js){
        try{
            js.key("FinesInfo").array();

            for(FinesDataTransferRecord record: this.fines){
                js.object()
                        .key("FineId").value(String.valueOf(record.getFinesId()))
                        .key("MemberId").value(String.valueOf(record.getMemberId()))
                        .key("Amount").value(String.valueOf(record.getAmount()))
                        .key("FineTypeId").value(String.valueOf(record.getFineTypeId()))
                        .key("DateCleared").value(Utils.formatDate(record.getDateCleared(), "yyyy-MM-dd"))
                        .key("IsCleared").value(String.valueOf(record.isCleared()))
                        .key("PaidInMeetingId").value(String.valueOf(record.getPaidInMeetingId()))
                        .key("MeetingId").value(String.valueOf(record.getMeetingId()))
                        .endObject();
            }
            js.endArray();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    public static String getJSONOutput(Context context, int meetingId){
        DataFactory dataFactory = new DataFactory(context, meetingId);

        JSONStringer js = new JSONStringer();
        try {
            //js.object().key("FileSubmission").array();
            js.object();
            js = dataFactory.getHeaderInfo(js);
            js = dataFactory.getCycleInfo(js);
            js = dataFactory.getMembersInfo(js);
            js = dataFactory.getMeetingInfo(js);
            js = dataFactory.getAttendance(js);
            js = dataFactory.getSaving(js);
            js = dataFactory.getFines(js);
            js = dataFactory.getLoanRepayments(js);
            js = dataFactory.getLoanIssues(js);
            js.endObject();
            //js.endArray().endObject();

        }catch (Exception e){
            e.printStackTrace();
        }
        return js.toString();
    }
}
