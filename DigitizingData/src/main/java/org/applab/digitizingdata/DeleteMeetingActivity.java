package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

public class DeleteMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    int meetingId = 0;
    MeetingRepo meetingRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingAttendanceRepo attendanceRepo = null;
    MeetingLoanRepaymentRepo repaymentRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    MeetingFineRepo fineRepo = null;
    String meetingDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_delete_meeting);
        inflateCustomActionBar();


        if (getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_meetingDate")) {
            this.meetingDate = getIntent().getStringExtra("_meetingDate");
        }

        //Initialize the Repositories
        meetingRepo = new MeetingRepo(getApplicationContext());
        savingRepo = new MeetingSavingRepo(getApplicationContext());
        attendanceRepo = new MeetingAttendanceRepo(getApplicationContext());
        repaymentRepo = new MeetingLoanRepaymentRepo(getApplicationContext());
        loanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());
        fineRepo = new MeetingFineRepo(getApplicationContext());

        //Set the values for the meeting details
        TextView txtMeetingDate = (TextView) findViewById(R.id.lblDMMeetingDate);
        TextView txtAttendedCount = (TextView) findViewById(R.id.lblDMAttended);
        TextView txtFines = (TextView) findViewById(R.id.lblDMFines);
        TextView txtSavings = (TextView) findViewById(R.id.lblDMSavings);
        TextView txtLoanRepayments = (TextView) findViewById(R.id.lblDMLoansRepaid);
        TextView txtLoanIssues = (TextView) findViewById(R.id.lblDMLoansIssued);


        TextView lblDMInstructions = (TextView) findViewById(R.id.lblDMInstructions);

        StringBuilder sb = new StringBuilder("Are you sure you want to delete this meeting? If so, tap <b>Done</b>. The following information will be deleted:");
        lblDMInstructions.setText(Html.fromHtml(sb.toString()));

        //Retrieve the target Meeting
        Meeting targetMeeting = meetingRepo.getMeetingById(meetingId);

        if (null != targetMeeting) {
            txtMeetingDate.setText(String.format("%s", Utils.formatDate(targetMeeting.getMeetingDate())));

            if (null == attendanceRepo) {
                attendanceRepo = new MeetingAttendanceRepo(getApplicationContext());
            }
            if (null != attendanceRepo) {
                txtAttendedCount.setText(String.format("Members Present: %d", attendanceRepo.getAttendanceCountByMeetingId(meetingId, 1)));
            }

            if (null == savingRepo) {
                savingRepo = new MeetingSavingRepo(getApplicationContext());
            }
            double totalMeetingSavings = 0.0;
            double totalFinesCollected = 0.0;
            double totalLoansIssuedInMeeting = 0.0;
            double totalLoansRepaidInMeeting = 0.0;

            totalMeetingSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            txtSavings.setText(String.format("Savings: %,.0f %s", totalMeetingSavings, getResources().getString(R.string.operating_currency)));

            totalLoansRepaidInMeeting = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            txtLoanRepayments.setText(String.format("Loans repaid: %,.0f %s", totalLoansRepaidInMeeting, getResources().getString(R.string.operating_currency)));

            totalFinesCollected = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);
            txtFines.setText(String.format("Fines: %,.0f %s", totalFinesCollected, getResources().getString(R.string.operating_currency)));

            totalLoansIssuedInMeeting = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            txtLoanIssues.setText(String.format("Loans issued: %,.0f %s", totalLoansIssuedInMeeting, getResources().getString(R.string.operating_currency)));
        } else {
            txtAttendedCount.setText("");
            txtFines.setText("");
            txtSavings.setText("");
            txtLoanRepayments.setText("");
            txtLoanIssues.setText("");
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

                        Meeting targetMeeting = meetingRepo.getMeetingById(meetingId);
                        if (null == targetMeeting) {
                            //The meeting was not retrieved.
                            //TODO: Figure out a better way of handling this.
                            finish();

                            return;
                        }

                        // Check whether there are savings attached to the target meeting
                        if (savingRepo.getTotalSavingsInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //Check whether there are Repayments attached to the target meeting
                        //will require a look into the repayments
                        if (repaymentRepo.getTotalLoansRepaidInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //Check whether there are Loans attached to the target meeting
                        //will require a look into the loans issued
                        if (loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //TODO: Should we allow deletion of a meeting whose data has already been sent?

                        //Only allow deletion if the meeting is the most recent or it is a past meeting without data
                        //Check whether this meeting is the most recent meeting in the current cycle
                        //If not, then don't allow deleting coz it will mess up loans
                        //TODO: Figure out how to cascade Loan info when a meet is deleted
                        if (targetMeeting.getVslaCycle() != null) {
                            Meeting mostRecent = meetingRepo.getMostRecentMeetingInCycle(targetMeeting.getVslaCycle().getCycleId());
                            if (mostRecent.getMeetingId() == meetingId) {
                                cannotBeDeleted = false;
                            } else {
                                Toast.makeText(getApplicationContext(), String.format("Sorry, first delete the most recent meeting in this cycle dated: %s.", Utils.formatDate(mostRecent.getMeetingDate())), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if (cannotBeDeleted) {
                            Toast.makeText(getApplicationContext(), "The meeting cannot be deleted. It contains meeting data.", Toast.LENGTH_LONG).show();
                            finish();
                            return;

                        } else {
                            //Check whether to make the sibling meeting the current one: Data should not have been sent
                            Meeting previousMeeting = meetingRepo.getPreviousMeeting(targetMeeting.getVslaCycle().getCycleId(), targetMeeting.getMeetingId());
                            if (previousMeeting != null) {   // && targetMeeting.isCurrent()
                                if (!previousMeeting.isMeetingDataSent()) {
                                    meetingRepo.activateMeeting(previousMeeting);
                                }
                            }

                            //TODO: Implement the UNDO feature, instead of directly deleting the meeting
                            //TODO: Cascade the deletion to clear meeting items and avoid orphaned records
                            //TODO: What if the meeting's data has been sent, what happens?
                            //repaymentRepo.reverseLoanRepaymentsForMeeting(meetingId);
                            repaymentRepo.reverseLoanRepaymentsForMeeting(meetingId, meetingDate);
                            meetingRepo.deleteMeeting(meetingId);
                            Toast.makeText(getApplicationContext(), "The meeting has been deleted.", Toast.LENGTH_LONG).show();


                            //Now refresh the Begin Meeting screen
                            Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            return;
                        }
                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        return;
                    }
                }
        );


        actionBar = getSupportActionBar();
        actionBar.setTitle("Delete Meeting");

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
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.delete_meeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
