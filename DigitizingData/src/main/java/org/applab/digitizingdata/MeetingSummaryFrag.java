package org.applab.digitizingdata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingSummaryFrag extends SherlockFragment {
    private ActionBar actionBar;
    private ScrollView fragmentView;
    MeetingActivity parentActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getSherlockActivity();
        setHasOptionsMenu(true);
    }

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
        fragmentView = (ScrollView)  inflater.inflate(R.layout.frag_meeting_summary, container, false);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = getSherlockActivity().getSupportActionBar();

    }

    public void onResume() {
        super.onResume();
        populateMeetingSummary();

    }

    private void populateMeetingSummary() {
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
        //TextView lblMeetingDate = (TextView) getSherlockActivity().findViewById(R.id.lblMSFMeetingDate);
        // lblMeetingDate.setText(meetingDate);
        int meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        int previousMeetingId = getSherlockActivity().getIntent().getIntExtra("_previousMeetingId", 0);

        //Get the Cycle that contains this meeting
        Meeting currentMeeting = parentActivity.ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        TextView lblTotalSavings = (TextView) getSherlockActivity().findViewById(R.id.lblMSFTotalSavings);
        TextView lblOutstandingLoans = (TextView) getSherlockActivity().findViewById(R.id.lblMSFOutstandingLoans);
        TextView lblSectionLastMeeting = (TextView) getSherlockActivity().findViewById(R.id.lblMSFSection2);

        double outstandingLoans = 0.0;
        double totalSavings = 0.0;
        double issuedLoans = 0.0;
        String startDate = "";
        String endDate = "";

        if (null != currentMeeting && null != currentMeeting.getVslaCycle()) {

            startDate = Utils.formatDate(currentMeeting.getVslaCycle().getStartDate());
            endDate = Utils.formatDate(currentMeeting.getVslaCycle().getEndDate());

            //Setup the Total Savings
            totalSavings = parentActivity.ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInCycle(currentMeeting.getVslaCycle().getCycleId());

            //Setup the Loans Issued
            issuedLoans = parentActivity.ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInCycle(currentMeeting.getVslaCycle().getCycleId());

            //Setup the Loans Outstanding
            outstandingLoans = parentActivity.ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalOutstandingLoansInCycle(currentMeeting.getVslaCycle().getCycleId());


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


        //TODO: May be I should retrieve the previous meeting from the previousMeetingId that was sent here by the MeetingDefinitionActivity
        //Challenge is how to get the same in case this activity was called from a different activity
        //Possible Solution: Store the value of Previous Meeting in database->Meetings table
        Meeting previousMeeting = null;

        previousMeeting = parentActivity.ledgerLinkApplication.getMeetingRepo().getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), currentMeeting.getMeetingId());


        //Force view to be of current meeting if in Data Review mode
        if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_REVIEW) {
            previousMeeting = currentMeeting;

            //Also Rename the Section Marker
            lblSectionLastMeeting.setText("Current Meeting Summary");
        }


        if (null != previousMeeting) {
            lblSectionLastMeeting.setText(String.format("PAST MEETING: %s", Utils.formatDate(previousMeeting.getMeetingDate())));

            txtAttendedCount.setText(String.format("Attended: %d", parentActivity.ledgerLinkApplication.getMeetingAttendanceRepo().getAttendanceCountByMeetingId(previousMeeting.getMeetingId())));

            txtDataSent.setText(String.format("Data: %s", (previousMeeting.isMeetingDataSent()) ? "Sent" : "Not Sent"));

            //TODO: Get Values for the Financials
            double totalMeetingSavings = 0.0;
            double totalMeetingCollections = 0.0;
            double totalLoansIssuedInMeeting = 0.0;
            double totalLoansRepaidInMeeting = 0.0;

            totalMeetingSavings = parentActivity.ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInMeeting(previousMeeting.getMeetingId());
            txtTotalSavings.setText(String.format("Savings: %,.0f UGX", totalMeetingSavings));

            totalLoansRepaidInMeeting = parentActivity.ledgerLinkApplication.getMeetingLoanRepaymentRepo().getTotalLoansRepaidInMeeting(previousMeeting.getMeetingId());
            txtTotalRepayments.setText(String.format("Loans repaid: %,.0f UGX", totalLoansRepaidInMeeting));

            totalLoansIssuedInMeeting = parentActivity.ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInMeeting(previousMeeting.getMeetingId());
            txtTotalLoanIssues.setText(String.format("Loans issued: %,.0f UGX", totalLoansIssuedInMeeting));

       /**     double finesCollected = fineRepo.getTotalFinesInCycle(previousMeeting.getVslaCycle().getCycleId());
            txtFines.setText(String.format("Fines: %,.0f UGX", finesCollected)); */


            totalMeetingCollections = totalMeetingSavings + totalLoansRepaidInMeeting;
            txtTotalCollections.setText(String.format("Total Collections: %,.0f UGX", totalMeetingCollections));


        } else {
            //Hide the view altogether

            LinearLayout lblMSFPastmeetingSection = (LinearLayout) fragmentView.findViewById(R.id.lblMSFPastmeetingSection);
            lblMSFPastmeetingSection.setVisibility(View.GONE);
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