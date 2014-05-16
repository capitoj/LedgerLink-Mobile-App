package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.R;

public class DeleteMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    int meetingId = 0;

    MeetingRepo meetingRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingAttendanceRepo attendanceRepo = null;
    MeetingLoanRepaymentRepo repaymentRepo = null;
    MeetingLoanIssuedRepo meetingLoanIssuedRepo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_meeting);

        meetingRepo = new MeetingRepo(getApplicationContext());
        savingRepo = new MeetingSavingRepo(getApplicationContext());
        attendanceRepo = new MeetingAttendanceRepo(getApplicationContext());
        repaymentRepo = new MeetingLoanRepaymentRepo(getApplicationContext());
        meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean cannotBeDeleted = false;
                        String declineReason = "";

                        Meeting targetMeeting = meetingRepo.getMeetingById(meetingId);
                        if(null == targetMeeting) {
                            //The meeting was not retrieved.
                            //TODO: Figure out a better way of handling this.
                            finish();
                            return;
                        }

                        //Check whether there are savings attached to the target meeting
                        if(savingRepo.getTotalSavingsInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                            declineReason = "There were savings made in this meeting.";
                        }

                        //Check whether there are Repayments attached to the target meeting
                        //will require a look into the repayments
                        if(repaymentRepo.getTotalLoansRepaidInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                            declineReason = "There were loan repayments made in this meeting.";
                        }

                        //Check whether there are Loans attached to the target meeting
                        //will require a look into the loans issued
                        if(meetingLoanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                            declineReason = "There were loans issued in this meeting.";
                        }

                        //If the meeting is the most recent, allow for deleting
                        Meeting mostRecentMeeting = meetingRepo.getMostRecentMeetingInCycle(targetMeeting.getVslaCycle().getCycleId());
                        if(mostRecentMeeting != null && mostRecentMeeting.getMeetingId() == targetMeeting.getMeetingId()) {
                            cannotBeDeleted = false;
                        }

                        //However, if the Meeting Data has already been sent to the bank, then don't allow for deletion
                        if(targetMeeting.isMeetingDataSent()){
                            cannotBeDeleted = true;
                            declineReason = "The data for this meeting is already sent to the bank.";
                        }

                        if(cannotBeDeleted) {
                            Toast.makeText(getApplicationContext(),"The meeting cannot be deleted. \n " + declineReason,Toast.LENGTH_LONG).show();
                            //Utils.createAlertDialogOk(getApplicationContext(), "Delete Meeting", "The meeting cannot be deleted.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                            return;

                        }
                        else{
                            //I need a Transaction that traverses several database tables

                            boolean meetingDeletedSuccessfully = meetingRepo.deleteMeeting(meetingId);
                            if(meetingDeletedSuccessfully) {
                                Toast.makeText(getApplicationContext(),"The meeting has been deleted.",Toast.LENGTH_LONG).show();

                                //Take the user back to the Begin Meeting Screen
                                Intent intent = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"The meeting was not deleted.",Toast.LENGTH_LONG).show();
                                finish();
                            }

                            return;
                        }

                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        return;
                    }
                });


        actionBar = getSupportActionBar();
        actionBar.setTitle("Delete Meeting");
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)

        if(getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        //Populate the fields
        Meeting targetMeeting = meetingRepo.getMeetingById(meetingId);

        //If the meeting doesn't exist, just exit
        if(null == targetMeeting) {
            finish();
            return;
        }

        //Display the Meeting Date

        String meetingDate = Utils.formatDate(targetMeeting.getMeetingDate());
        TextView tvMeetingDate = (TextView)findViewById(R.id.lblDMMeetingDate);
        tvMeetingDate.setText(meetingDate);

        //Display Attendance for the target meeting
        int attendance = attendanceRepo.getAttendanceCountByMeetingId(targetMeeting.getMeetingId(), 1);
        TextView tvAttendance = (TextView)findViewById(R.id.lblDMAttended);
        tvAttendance.setText(String.format("Attended: %d",attendance));

        //Display Savings for the target meeting
        double savings = savingRepo.getTotalSavingsInMeeting(targetMeeting.getMeetingId());
        TextView tvSavings = (TextView)findViewById(R.id.lblDMSavings);
        tvSavings.setText(String.format("Savings: %,.0f UGX",savings));

        //Display Loans Repayments for the target meeting
        double repayments = repaymentRepo.getTotalLoansRepaidInMeeting(targetMeeting.getMeetingId());
        TextView tvRepayments = (TextView)findViewById(R.id.lblDMLoansRepaid);
        tvRepayments.setText(String.format("Loan repaid: %,.0f UGX",repayments));

        //Display Fines for the target meeting
        double fines = 0.0d;
        TextView tvFines = (TextView)findViewById(R.id.lblDMFines);
        tvFines.setText(String.format("Fines: %,.0f UGX",fines));

        //Display Loans Issues for the target meeting
        double loanIssues = meetingLoanIssuedRepo.getTotalLoansIssuedInMeeting(targetMeeting.getMeetingId());
        TextView tvLoanIssues = (TextView)findViewById(R.id.lblDMLoansIssued);
        tvLoanIssues.setText(String.format("Loan issued: %,.0f UGX",loanIssues));

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
