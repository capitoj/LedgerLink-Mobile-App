package org.applab.digitizingdata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingSummaryFrag extends SherlockFragment {
    ActionBar actionBar;
    private int meetingId = 0;
    private int previousMeetingId = 0;
    MeetingRepo meetingRepo;
    private MeetingActivity parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        return inflater.inflate(R.layout.frag_meeting_summary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = getSherlockActivity().getSupportActionBar();
        String meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = "Meeting";

        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        parentActivity = (MeetingActivity) getSherlockActivity();
        //TextView lblMeetingDate = (TextView) getSherlockActivity().findViewById(R.id.lblMSFMeetingDate);
        // lblMeetingDate.setText(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        previousMeetingId = getSherlockActivity().getIntent().getIntExtra("_previousMeetingId", 0);

        //Get the Cycle that contains this meeting
        meetingRepo = new MeetingRepo(getSherlockActivity().getBaseContext());
        Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);

        TextView lblTotalSavings = (TextView) getSherlockActivity().findViewById(R.id.lblMSFTotalSavings);
        TextView lblOutstandingLoans = (TextView) getSherlockActivity().findViewById(R.id.lblMSFOutstandingLoans);
        TextView lblSectionLastMeeting = (TextView) getSherlockActivity().findViewById(R.id.lblMSFSection2);

        MeetingSavingRepo savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
        MeetingLoanIssuedRepo loansIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
        MeetingLoanRepaymentRepo loansRepaidRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
        double outstandingLoans = 0.0;
        double totalSavings = 0.0;
        double issuedLoans = 0.0;
        String startDate = "";
        String endDate = "";

        if (null != currentMeeting && null != currentMeeting.getVslaCycle()) {

            startDate = Utils.formatDate(currentMeeting.getVslaCycle().getStartDate());
            endDate = Utils.formatDate(currentMeeting.getVslaCycle().getEndDate());

            //Setup the Total Savings
            totalSavings = savingRepo.getTotalSavingsInCycle(currentMeeting.getVslaCycle().getCycleId());

            //Setup the Loans Issued
            issuedLoans = loansIssuedRepo.getTotalLoansIssuedInCycle(currentMeeting.getVslaCycle().getCycleId());

            //Setup the Loans Outstanding
            outstandingLoans = loansIssuedRepo.getTotalOutstandingLoansInCycle(currentMeeting.getVslaCycle().getCycleId());


            //Get Cash In Bank
            //TODO: Requires the API or Hello Money

            //TODO: May Add Attendance
        }

        lblOutstandingLoans.setText(String.format("Loans Outstanding: %,.0f UGX", outstandingLoans));
        lblTotalSavings.setText(String.format("Total Savings: %,.0f UGX", totalSavings));
        // lblCycleStartDate.setText(String.format("From: %s", startDate));
        // lblCycleEndDate.setText(String.format("To: %s", endDate));
        // lblIssuedLoans.setText(String.format("Loans Issued: %,.0f UGX", issuedLoans));
        //lblCashInBank.setText(String.format("Total Cash In Bank: %s", "Not Available"));


        // TODO: Get Info about the Last Meeting: Previous Meeting in Current Cycle?
        TextView txtAttendedCount = (TextView) getSherlockActivity().findViewById(R.id.lblMSFAttended);
        TextView txtDataSent = (TextView) getSherlockActivity().findViewById(R.id.lblMSFDataSentStatus);
        TextView txtTotalCollections = (TextView) getSherlockActivity().findViewById(R.id.lblMSFCollections);
        TextView txtTotalSavings = (TextView) getSherlockActivity().findViewById(R.id.lblMSFLastSavings);
        TextView txtTotalRepayments = (TextView) getSherlockActivity().findViewById(R.id.lblMSFLastLoansRepaid);
        TextView txtTotalLoanIssues = (TextView) getSherlockActivity().findViewById(R.id.lblMSFLastLoansIssued);

        if (null == meetingRepo) {
            meetingRepo = new MeetingRepo(getSherlockActivity().getBaseContext());
        }

        //TODO: May be I should retrieve the previous meeting from the previousMeetingId that was sent here by the MeetingDefinitionActivity
        //Challenge is how to get the same is case this activity was called from a different activity
        //Possible Solution: Store the value of Previous Meeting in database->Meetings table
        Meeting previousMeeting = null;
        if (null != meetingRepo) {
            previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId());
        }

        //Force view to be of current meeting if in Data Review mode
        if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_REVIEW) {
            previousMeeting = currentMeeting;

            //Also Rename the Section Marker
            lblSectionLastMeeting.setText("Current Meeting Summary");
        }


        if (null != previousMeeting) {
            lblSectionLastMeeting.setText(String.format("PAST MEETING: %s", Utils.formatDate(previousMeeting.getMeetingDate())));

            MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(getSherlockActivity().getBaseContext());
            if (null != attendanceRepo) {
                txtAttendedCount.setText(String.format("Attended: %d", attendanceRepo.getAttendanceCountByMeetingId(previousMeeting.getMeetingId(), 1)));
            }

            txtDataSent.setText(String.format("Data: %s", (previousMeeting.isMeetingDataSent()) ? "Sent" : "Not Sent"));

            //TODO: Get Values for the Financials
            if (null == savingRepo) {
                savingRepo = new MeetingSavingRepo(getSherlockActivity().getBaseContext());
            }
            double totalMeetingSavings = 0.0;
            double totalMeetingCollections = 0.0;
            double totalLoansIssuedInMeeting = 0.0;
            double totalLoansRepaidInMeeting = 0.0;

            totalMeetingSavings = savingRepo.getTotalSavingsInMeeting(previousMeeting.getMeetingId());
            txtTotalSavings.setText(String.format("Savings: %,.0f UGX", totalMeetingSavings));

            totalLoansRepaidInMeeting = loansRepaidRepo.getTotalLoansRepaidInMeeting(previousMeeting.getMeetingId());
            txtTotalRepayments.setText(String.format("Loans repaid: %,.0f UGX", totalLoansRepaidInMeeting));

            totalLoansIssuedInMeeting = loansIssuedRepo.getTotalLoansIssuedInMeeting(previousMeeting.getMeetingId());
            txtTotalLoanIssues.setText(String.format("Loans issued: %,.0f UGX", totalLoansIssuedInMeeting));

       /**     double finesCollected = fineRepo.getTotalFinesInCycle(previousMeeting.getVslaCycle().getCycleId());
            txtFines.setText(String.format("Fines: %,.0f UGX", finesCollected)); */


            totalMeetingCollections = totalMeetingSavings + totalLoansRepaidInMeeting;
            txtTotalCollections.setText(String.format("Total Collections: %,.0f UGX", totalMeetingCollections));


        } else {
            txtAttendedCount.setText("");
            txtDataSent.setText("");
            txtTotalCollections.setText("");
            txtTotalSavings.setText("");
            txtTotalRepayments.setText("");
            txtTotalLoanIssues.setText("");

            //Optional: You May remove this code. Remove the controls
            /**  LinearLayout parent = (LinearLayout) lblMeetingDate.getParent();
             if (null != parent) {
             parent.removeView(txtAttendedCount);
             parent.removeView(txtDataSent);
             //parent.removeView(txtPreviousMeetingDate);
             parent.removeView(txtTotalCollections);
             parent.removeView(txtTotalSavings);
             parent.removeView(txtTotalRepayments);
             parent.removeView(txtTotalLoanIssues);
             } */
        }

        //If this is a Review then do not display the data
        //Remove the controls
        if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_REVIEW) {

            /*if(null != lblMeetingDate) {
                LinearLayout parent = (LinearLayout)lblMeetingDate.getParent();
                if(null != parent) {
                    parent.removeView(txtAttendedCount);
                    parent.removeView(txtDataSent);
                    parent.removeView(txtPreviousMeetingDate);
                    parent.removeView(txtTotalCollections);
                    parent.removeView(txtTotalSavings);
                    parent.removeView(txtTotalRepayments);
                    parent.removeView(txtTotalLoanIssues);

                    //Also Remove the Section Marker
                    TextView lblSectionLastMeeting = (TextView)getSherlockActivity().findViewById(R.id.lblMSFSection2);
                    parent.removeView(lblSectionLastMeeting);
                }
            }*/
        }
    }
}