package org.applab.ledgerlink.business_rules;

import android.content.Context;

import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;
import org.applab.ledgerlink.repo.MeetingWelfareRepo;

/**
 * Created by JCapito on 10/3/2018.
 */

public class VslaMeeting {

    private int meetingId;
    private MeetingRepo meetingRepo;
    private Context context;

    public VslaMeeting(Context context, int meetingId){
        this.meetingRepo = new MeetingRepo(context, meetingId);
        this.meetingId = meetingId;
        this.context = context;
    }

    public double getActualStartingCash(){
        return this.meetingRepo.getMeeting().getOpeningBalanceBox();
    }

    public double getCashFromBank(){
        return this.meetingRepo.getMeeting().getOpeningBalanceBank();
    }

    public double getLoanFromBank(){
        return this.meetingRepo.getMeeting().getLoanFromBank();
    }

    public double getTotalSavings(){
        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(this.context);
        return meetingSavingRepo.getTotalSavingsInMeeting(this.meetingId);
    }

    public double getTotalLoansRepaid(){
        MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(this.context);
        return repaymentRepo.getTotalLoansRepaidInMeeting(this.meetingId);
    }

    public double getTotalLoansIssued(){
        MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(this.context);
        return loanIssuedRepo.getTotalLoansIssuedInMeeting(this.meetingId);
    }

    public double getTotalFinesPaid(){
        MeetingFineRepo meetingFineRepo = new MeetingFineRepo(this.context);
        return meetingFineRepo.getTotalFinesPaidInThisMeeting(this.meetingId);
    }

    public double getTotalWelfare(){
        MeetingWelfareRepo meetingWelfareRepo = new MeetingWelfareRepo(this.context);
        return meetingWelfareRepo.getTotalWelfareInMeeting(this.meetingId);
    }

    public double getBankLoanRepayment(){
        return this.meetingRepo.getMeeting().getBankLoanRepayment();
    }

    public double getCashSavedToBank(){
        return this.meetingRepo.getMeeting().getClosingBalanceBank();
    }

    public static double getTotalCashInBox(Context context, int meetingId){
        VslaMeeting vslaMeeting = new VslaMeeting(context, meetingId);
        double totalCashInBox = (vslaMeeting.getActualStartingCash() + vslaMeeting.getTotalSavings() + vslaMeeting.getTotalLoansRepaid() + vslaMeeting.getTotalFinesPaid() + vslaMeeting.getLoanFromBank() + vslaMeeting.getCashFromBank() + vslaMeeting.getTotalWelfare()) - (vslaMeeting.getTotalLoansIssued() + vslaMeeting.getBankLoanRepayment());
        return totalCashInBox;

    }
}
