package org.applab.ledgerlink;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;

import org.applab.ledgerlink.business_rules.VslaMeeting;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.w3c.dom.Text;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingSummaryFrag extends Fragment {
    private ActionBar actionBar;
    private ScrollView fragmentView;
    MeetingActivity parentActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getActivity();
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

        actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

    }

    public void onResume() {
        super.onResume();
        populateMeetingSummary();

    }

    private void populateMeetingSummary() {
        String meetingDate = getActivity().getIntent().getStringExtra("_meetingDate");
        String title = getString(R.string.meeting);

        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = getString(R.string.send_data);
                break;
            case VIEW_MODE_READ_ONLY:
                title = getString(R.string.send_data);
                break;
            default:
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        //TextView lblMeetingDate = (TextView) getActivity()().findViewById(R.id.lblMSFMeetingDate);
        // lblMeetingDate.setText(meetingDate);
        int meetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);
        int previousMeetingId = getActivity().getIntent().getIntExtra("_previousMeetingId", 0);

        //Get the Cycle that contains this meeting
        Meeting currentMeeting = parentActivity.ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);
        //Meeting previousMeeting = new MeetingRepo(getActivity()().getApplicationContext()).getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), meetingId);


        TextView lblMSFCollections = (TextView) getActivity().findViewById(R.id.lblMSFCollections);
        TextView lblTotalSavings = (TextView) getActivity().findViewById(R.id.lblMSFTotalSavings);
        TextView lblOutstandingLoans = (TextView) getActivity().findViewById(R.id.lblMSFOutstandingLoans);
        TextView lblSectionLastMeeting = (TextView) getActivity().findViewById(R.id.lblMSFSection2);
        TextView lblMSFLastWelfare = (TextView) getActivity().findViewById(R.id.lblMSFLastWelfare);
        TextView lblMSFLastFines = (TextView) getActivity().findViewById(R.id.lblMSFLastFines);
        TextView lblMSFLastCashFromBank = (TextView) getActivity().findViewById(R.id.lblMSFLastCashFromBank);
        TextView lblMSFLastCashToBank = (TextView) getActivity().findViewById(R.id.lblMSFLastCashToBank);
        TextView lblMSFLastLoanFromBank = (TextView) getActivity().findViewById(R.id.lblMSFLastLoanFromBank);
        TextView lblMSFLastBankLoanRepayment = (TextView) getActivity().findViewById(R.id.lblMSFLastBankLoanRepayment);

        double outstandingLoans = 0.0;
        double totalSavings = 0.0;
        double totalWelfare = 0.0;
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



        lblOutstandingLoans.setText(String.format(getString(R.string.outstanding_loans)+" %,.0f UGX", outstandingLoans));
        lblTotalSavings.setText(String.format(getString(R.string.total_saving_x)+" %,.0f UGX", totalSavings));
        //lblCycleStartDate.setText(String.format("From: %s", startDate));
        //lblCycleEndDate.setText(String.format("To: %s", endDate));
        //lblIssuedLoans.setText(String.format("Loans Issued: %,.0f UGX", issuedLoans));
        //lblCashInBank.setText(String.format("Total Cash In Bank: %s", "Not Available"));


        // TODO: Get Info about the Last Meeting: Previous Meeting in Current Cycle?
        TextView txtAttendedCount = (TextView) getActivity().findViewById(R.id.lblMSFAttended);
        TextView txtDataSent = (TextView) getActivity().findViewById(R.id.lblMSFDataSentStatus);
        TextView txtTotalCollections = (TextView) getActivity().findViewById(R.id.lblMSFCollections);
        TextView txtTotalSavings = (TextView) getActivity().findViewById(R.id.lblMSFLastSavings);
        TextView txtTotalRepayments = (TextView) getActivity().findViewById(R.id.lblMSFLastLoansRepaid);
        TextView txtTotalLoanIssues = (TextView) getActivity().findViewById(R.id.lblMSFLastLoansIssued);


        //TODO: May be I should retrieve the previous meeting from the previousMeetingId that was sent here by the MeetingDefinitionActivity
        //Challenge is how to get the same in case this activity was called from a different activity
        //Possible Solution: Store the value of Previous Meeting in database->Meetings table
        Meeting previousMeeting = null;

        previousMeeting = parentActivity.ledgerLinkApplication.getMeetingRepo().getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), currentMeeting.getMeetingId());


        //Force view to be of current meeting if in Data Review mode
//        if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_REVIEW) {
//            previousMeeting = currentMeeting;
//
//            //Also Rename the Section Marker
//            lblSectionLastMeeting.setText(R.string.current_meeting_summary);
//        }


        if (null != previousMeeting) {

            lblSectionLastMeeting.setText(String.format(getString(R.string.past_meeting_allcaps) + " %s", previousMeeting.getMeetingDate() == null ? "0.0 UGX" : Utils.formatDate(previousMeeting.getMeetingDate())));

            txtAttendedCount.setText(String.format(getString(R.string.attended)+" %d", parentActivity.ledgerLinkApplication.getMeetingAttendanceRepo().getAttendanceCountByMeetingId(previousMeeting.getMeetingId())));

            txtDataSent.setText(String.format(getString(R.string.data)+" %s", (previousMeeting.isMeetingDataSent()) ? getString(R.string.sent) : getString(R.string.not_sent)));

            //TODO: Get Values for the Financials
            double totalMeetingSavings = 0.0;
            double totalMeetingCollections = 0.0;
            double totalLoansIssuedInMeeting = 0.0;
            double totalLoansRepaidInMeeting = 0.0;

            VslaMeeting prevVslaMeeting = new VslaMeeting(getActivity().getApplicationContext(), previousMeeting.getMeetingId());
            Log.e("PreMeetingId", String.valueOf(previousMeeting.getLoanFromBank()));

            totalMeetingSavings = parentActivity.ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInMeeting(previousMeeting.getMeetingId());
            txtTotalSavings.setText(String.format(getString(R.string.total_saving_x)+": %,.0f UGX", prevVslaMeeting.getTotalSavings()));



            txtTotalLoanIssues.setText(String.format(getString(R.string.total_loan_issued)+" %,.0f UGX", prevVslaMeeting.getTotalLoansIssued()));

            lblMSFLastWelfare.setText(String.format(getString(R.string.total_welfare_x)+": %,.0f UGX", prevVslaMeeting.getTotalWelfare()));
            VslaCycle recentCycle = parentActivity.ledgerLinkApplication.getVslaCycleRepo().getMostRecentCycle();
            if(parentActivity.ledgerLinkApplication.getMeetingRepo().getAllMeetings(recentCycle.getCycleId()).size() < 3){
                txtTotalRepayments.setText(String.format(getString(R.string.total_interest_earned)+" %,.0f UGX", currentMeeting.getVslaCycle().getInterestAtSetup()));
                lblMSFLastFines.setText(String.format(getString(R.string.total_fines)+" %,.0f UGX", currentMeeting.getVslaCycle().getFinesAtSetup()));
            }else {

                totalLoansRepaidInMeeting = parentActivity.ledgerLinkApplication.getMeetingLoanRepaymentRepo().getTotalLoansRepaidInMeeting(previousMeeting.getMeetingId());
                txtTotalRepayments.setText(String.format(getString(R.string.total_loans_repaid)+" %,.0f UGX", prevVslaMeeting.getTotalLoansRepaid()));
                lblMSFLastFines.setText(String.format(getString(R.string.total_fines)+" %,.0f UGX", prevVslaMeeting.getTotalFinesPaid()));

            }

            lblMSFLastCashFromBank.setText(String.format(getString(R.string.cash_from_bank)+" %,.0f UGX", prevVslaMeeting.getCashFromBank()));

            lblMSFLastCashToBank.setText(String.format(getString(R.string.cash_taken_to_bank_x)+" %,.0f UGX", prevVslaMeeting.getCashSavedToBank()));

            lblMSFLastLoanFromBank.setText(String.format(getString(R.string.loan_from_bank)+" %,.0f UGX", prevVslaMeeting.getLoanFromBank()));

            lblMSFLastBankLoanRepayment.setText(String.format(getString(R.string.bank_loan_repayment)+": %,.0f UGX", prevVslaMeeting.getBankLoanRepayment()));

       /**     double finesCollected = fineRepo.getTotalFinesInCycle(previousMeeting.getVslaCycle().getCycleId());
            txtFines.setText(String.format("Fines: %,.0f UGX", finesCollected)); */


            totalMeetingCollections = totalMeetingSavings + totalLoansRepaidInMeeting;
//            Log.e("PreviousMeetingIdX", String.valueOf(prevVslaMeeting.getTotalCashInBox(getActivity().getApplicationContext(), previousMeeting.getMeetingId())));
            lblMSFCollections.setText(String.format(getString(R.string.total_collections)+" %,.0f UGX", prevVslaMeeting.getTotalCashInBox(getActivity().getApplicationContext(), previousMeeting.getMeetingId())));


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
                    TextView lblSectionLastMeeting = (TextView)getActivity().findViewById(R.id.lblMSFSection2);
                    parent.removeView(lblSectionLastMeeting);
                }
            }*/
        }
    }
}