package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;


public class DeleteMeetingActivity extends ActionBarActivity{
    private int meetingId = 0;
    private String meetingDate = "";
    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_delete_meeting);
        inflateCustomActionBar();


        if (getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_meetingDate")) {
            this.meetingDate = getIntent().getStringExtra("_meetingDate");
        }

        //Set the values for the meeting details
        TextView txtMeetingDate = (TextView) findViewById(R.id.lblDMMeetingDate);
        TextView txtAttendedCount = (TextView) findViewById(R.id.lblDMAttended);
        TextView txtFines = (TextView) findViewById(R.id.lblDMFines);
        TextView txtSavings = (TextView) findViewById(R.id.lblDMSavings);
        TextView txtLoanRepayments = (TextView) findViewById(R.id.lblDMLoansRepaid);
        TextView txtLoanIssues = (TextView) findViewById(R.id.lblDMLoansIssued);
        TextView txtWelfeare = (TextView)findViewById(R.id.lblDMWelfare);


        TextView lblDMInstructions = (TextView) findViewById(R.id.lblDMInstructions);

        lblDMInstructions.setText(Html.fromHtml(getString(R.string.are_you_sure_you_want_to_delete_this_metting)));

        //Retrieve the target Meeting
        Meeting targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        if (null != targetMeeting) {
            txtMeetingDate.setText(String.format("%s", Utils.formatDate(targetMeeting.getMeetingDate())));

            double totalMeetingSavings = 0.0;
            double totalFinesCollected = 0.0;
            double totalLoansIssuedInMeeting = 0.0;
            double totalLoansRepaidInMeeting = 0.0;
            double totalWelfare = 0.0;

            totalMeetingSavings = ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInMeeting(meetingId);
            txtSavings.setText(String.format(getString(R.string.saving_asof)+" %,.0f %s", totalMeetingSavings, getResources().getString(R.string.operating_currency)));

            totalLoansRepaidInMeeting = ledgerLinkApplication.getMeetingLoanRepaymentRepo().getTotalLoansRepaidInMeeting(meetingId);
            txtLoanRepayments.setText(String.format(getString(R.string.loans_asof)+" %,.0f %s", totalLoansRepaidInMeeting, getResources().getString(R.string.operating_currency)));

            totalFinesCollected = ledgerLinkApplication.getMeetingFineRepo().getTotalFinesPaidInThisMeeting(meetingId);
            txtFines.setText(String.format(getString(R.string.fines_asof)+" %,.0f %s", totalFinesCollected, getResources().getString(R.string.operating_currency)));

            totalLoansIssuedInMeeting = ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInMeeting(meetingId);
            txtLoanIssues.setText(String.format(getString(R.string.loans_isued_asof)+" %,.0f %s", totalLoansIssuedInMeeting, getResources().getString(R.string.operating_currency)));

            totalWelfare = ledgerLinkApplication.getMeetingWelfareRepo().getTotalWelfareInMeeting(meetingId);
            txtWelfeare.setText(String.format(getString(R.string.welfare_asof)+" %,.0f %s", totalWelfare, getResources().getString(R.string.operating_currency)));
        } else {
            txtAttendedCount.setText("");
            txtFines.setText("");
            txtSavings.setText("");
            txtLoanRepayments.setText("");
            txtLoanIssues.setText("");
            txtWelfeare.setText("");
        }
    }

    private void inflateCustomActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean cannotBeDeleted = false;

                        Meeting targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);
                        if (null == targetMeeting) {
                            //The meeting was not retrieved.
                            //TODO: Figure out a better way of handling this.
                            finish();

                            return;
                        }

                        // Check whether there are savings attached to the target meeting
                        if (ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //Check whether there are Repayments attached to the target meeting
                        //will require a look into the repayments
                        if (ledgerLinkApplication.getMeetingLoanRepaymentRepo().getTotalLoansRepaidInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //Check whether there are Loans attached to the target meeting
                        //will require a look into the loans issued
                        if (ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //TODO: Should we allow deletion of a meeting whose data has already been sent?

                        //Only allow deletion if the meeting is the most recent or it is a past meeting without data
                        //Check whether this meeting is the most recent meeting in the current cycle
                        //If not, then don't allow deleting coz it will mess up loans
                        //TODO: Figure out how to cascade Loan info when a meet is deleted
                        if (targetMeeting.getVslaCycle() != null) {
                            Meeting mostRecent = ledgerLinkApplication.getMeetingRepo().getMostRecentMeetingInCycle(targetMeeting.getVslaCycle().getCycleId());
                            if (mostRecent.getMeetingId() == meetingId) {
                                cannotBeDeleted = false;
                            } else {
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.sorry_first_delete_recent_meeting_in_cycle)+" %s.", Utils.formatDate(mostRecent.getMeetingDate())), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if (cannotBeDeleted) {
                            Toast.makeText(getApplicationContext(), R.string.meeting_cannot_be_deleted, Toast.LENGTH_LONG).show();
                            finish();

                        } else {
                            //Check whether to make the sibling meeting the current one: Data should not have been sent
                            Meeting previousMeeting = ledgerLinkApplication.getMeetingRepo().getPreviousMeeting(targetMeeting.getVslaCycle().getCycleId(), targetMeeting.getMeetingId());
                            if (previousMeeting != null) {   // && targetMeeting.isCurrent()
                                if (!previousMeeting.isMeetingDataSent()) {
                                    ledgerLinkApplication.getMeetingRepo().activateMeeting(previousMeeting);
                                }
                            }

                            //TODO: Implement the UNDO feature, instead of directly deleting the meeting
                            //TODO: Cascade the deletion to clear meeting items and avoid orphaned records
                            //TODO: What if the meeting's data has been sent, what happens?
                            //repaymentRepo.reverseLoanRepaymentsForMeeting(meetingId);
                            ledgerLinkApplication.getMeetingLoanRepaymentRepo().reverseLoanRepaymentsForMeeting(meetingId, meetingDate);
                            ledgerLinkApplication.getMeetingRepo().deleteMeeting(meetingId);
                            Toast.makeText(getApplicationContext(), R.string.meeting_been_deleted, Toast.LENGTH_LONG).show();


                            //Now refresh the Begin Meeting screen
                            Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );


        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setTitle(R.string.delete_meeting_main);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
        // END_INCLUDE (inflate_set_custom_view)
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.delete_meeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

}
