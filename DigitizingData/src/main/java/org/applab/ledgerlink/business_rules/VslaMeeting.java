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

    protected double __getActualStartingCash(){
        return this.meetingRepo.getMeeting().getOpeningBalanceBox();
    }

    protected double __getCashFromBank(){
        return this.meetingRepo.getMeeting().getOpeningBalanceBank();
    }

    protected double __getLoanFromBank(){
        return this.meetingRepo.getMeeting().getLoanFromBank();
    }

    protected double __getTotalSavings(){
        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(this.context);
        return meetingSavingRepo.getTotalSavingsInMeeting(this.meetingId);
    }

    protected double __getTotalLoansRepaid(){
        MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(this.context);
        return repaymentRepo.getTotalLoansRepaidInMeeting(this.meetingId);
    }

    protected double __getTotalLoansIssued(){
        MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(this.context);
        return loanIssuedRepo.getTotalLoansIssuedInMeeting(this.meetingId);
    }

    protected double __getTotalFinesPaid(){
        MeetingFineRepo meetingFineRepo = new MeetingFineRepo(this.context);
        return meetingFineRepo.getTotalFinesPaidInThisMeeting(this.meetingId);
    }

    protected double __getTotalWelfare(){
        MeetingWelfareRepo meetingWelfareRepo = new MeetingWelfareRepo(this.context);
        return meetingWelfareRepo.getTotalWelfareInMeeting(this.meetingId);
    }

    protected double __getBankLoanRepayment(){
        return this.meetingRepo.getMeeting().getBankLoanRepayment();
    }

    public static double getTotalCashInBox(Context context, int meetingId){
        VslaMeeting vslaMeeting = new VslaMeeting(context, meetingId);
        double totalCashInBox = (vslaMeeting.__getActualStartingCash() + vslaMeeting.__getTotalSavings() + vslaMeeting.__getTotalLoansRepaid() + vslaMeeting.__getTotalFinesPaid() + vslaMeeting.__getLoanFromBank() + vslaMeeting.__getCashFromBank() + vslaMeeting.__getTotalWelfare()) - (vslaMeeting.__getTotalLoansIssued() + vslaMeeting.__getBankLoanRepayment());
        return totalCashInBox;

    }
}
