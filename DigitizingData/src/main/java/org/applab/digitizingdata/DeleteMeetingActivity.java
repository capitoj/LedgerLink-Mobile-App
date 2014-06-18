package org.applab.digitizingdata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

public class DeleteMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    int meetingId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_delete_meeting);

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_next, null);
        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MeetingRepo meetingRepo = new MeetingRepo(getApplicationContext());
                        MeetingSavingRepo savingRepo = new MeetingSavingRepo(getApplicationContext());
                        MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(getApplicationContext());
                        MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(getApplicationContext());
                        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());

                        boolean deletedSuccessful = false;
                        boolean cannotBeDeleted = false;

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
                        }

                        //Check whether there are Repayments attached to the target meeting
                        //will require a look into the repayments
                        if(repaymentRepo.getTotalLoansRepaidInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        //Check whether there are Loans attached to the target meeting
                        //will require a look into the loans issued
                        if(meetingLoanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId) > 0.0D) {
                            cannotBeDeleted = true;
                        }

                        if(cannotBeDeleted) {
                            Utils.createAlertDialogOk(getApplicationContext(), "Delete Meeting", "The meeting cannot be deleted.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                            finish();
                            return;

                        }
                        else{
                            meetingRepo.deleteMeeting(meetingId);
                            Toast.makeText(getApplicationContext(),"The meeting has been deleted.",Toast.LENGTH_LONG).show();
                            finish();
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
