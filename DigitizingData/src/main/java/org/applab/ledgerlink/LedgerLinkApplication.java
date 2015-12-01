package org.applab.ledgerlink;

import android.app.Application;

import org.applab.ledgerlink.repo.MeetingAttendanceRepo;
import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;
import org.applab.ledgerlink.repo.MemberRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.repo.VslaInfoRepo;


/**
 * Created by John Mark on 7/11/2014.
 */
public class LedgerLinkApplication extends Application
{

    //Define all repos used by all fragments here for memory management
    //These will be instantiated lazilly as required

    private MeetingRepo meetingRepo;
    private MeetingFineRepo meetingFineRepo;
    private MeetingAttendanceRepo meetingAttendanceRepo;
    private MeetingLoanIssuedRepo meetingLoanIssuedRepo;
    private MeetingLoanRepaymentRepo meetingLoanRepaymentRepo;
    private MeetingSavingRepo meetingSavingRepo;
    private MemberRepo memberRepo;
    private VslaCycleRepo vslaCycleRepo;
    private VslaInfoRepo vslaInfoRepo;



    public LedgerLinkApplication() {
      super();

    }


    public MeetingRepo getMeetingRepo() {
        if(meetingRepo == null) {
            meetingRepo = new MeetingRepo(getApplicationContext());
        }
        return meetingRepo;
    }

    public void setMeetingRepo(MeetingRepo meetingRepo) {
        this.meetingRepo = meetingRepo;
    }

    public MeetingFineRepo getMeetingFineRepo() {
        if(meetingFineRepo == null) {
            meetingFineRepo = new MeetingFineRepo(getApplicationContext());
        }
        return meetingFineRepo;
    }

    public void setMeetingFineRepo(MeetingFineRepo meetingFineRepo) {
        this.meetingFineRepo = meetingFineRepo;
    }

    public MeetingAttendanceRepo getMeetingAttendanceRepo() {
        if(meetingAttendanceRepo == null) {
            meetingAttendanceRepo = new MeetingAttendanceRepo(getApplicationContext());
        }
        return meetingAttendanceRepo;
    }

    public void setMeetingAttendanceRepo(MeetingAttendanceRepo meetingAttendanceRepo) {
        this.meetingAttendanceRepo = meetingAttendanceRepo;
    }

    public MeetingLoanIssuedRepo getMeetingLoanIssuedRepo() {
        if(meetingLoanIssuedRepo == null) {
            meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());
        }
        return meetingLoanIssuedRepo;
    }

    public void setMeetingLoanIssuedRepo(MeetingLoanIssuedRepo meetingLoanIssuedRepo) {
        this.meetingLoanIssuedRepo = meetingLoanIssuedRepo;
    }

    public MeetingLoanRepaymentRepo getMeetingLoanRepaymentRepo() {
        if(meetingLoanRepaymentRepo == null) {
            meetingLoanRepaymentRepo = new MeetingLoanRepaymentRepo(getApplicationContext());
        }
        return meetingLoanRepaymentRepo;
    }

    public void setMeetingLoanRepaymentRepo(MeetingLoanRepaymentRepo meetingLoanRepaymentRepo) {
        this.meetingLoanRepaymentRepo = meetingLoanRepaymentRepo;
    }

    public MeetingSavingRepo getMeetingSavingRepo() {
        if(meetingSavingRepo == null) {
            meetingSavingRepo = new MeetingSavingRepo(getApplicationContext());
        }
        return meetingSavingRepo;
    }

    public void setMeetingSavingRepo(MeetingSavingRepo meetingSavingRepo) {
        this.meetingSavingRepo = meetingSavingRepo;
    }

    public MemberRepo getMemberRepo() {
        if(memberRepo == null) {
            memberRepo = new MemberRepo(getApplicationContext());
        }
        return memberRepo;
    }

    public void setMemberRepo(MemberRepo memberRepo) {

        this.memberRepo = memberRepo;
    }

    public VslaCycleRepo getVslaCycleRepo() {
        if(vslaCycleRepo == null) {
            vslaCycleRepo = new VslaCycleRepo(getApplicationContext());
        }
        return vslaCycleRepo;
    }

    public void setVslaCycleRepo(VslaCycleRepo vslaCycleRepo) {
        this.vslaCycleRepo = vslaCycleRepo;
    }

    public VslaInfoRepo getVslaInfoRepo() {
        if(vslaInfoRepo == null) {
            vslaInfoRepo = new VslaInfoRepo(getApplicationContext());
        }
        return vslaInfoRepo;
    }

    public void setVslaInfoRepo(VslaInfoRepo vslaInfoRepo) {
        this.vslaInfoRepo = vslaInfoRepo;
    }
}
