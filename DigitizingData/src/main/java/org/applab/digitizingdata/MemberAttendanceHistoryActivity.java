package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.AttendanceArrayAdapter;
import org.applab.digitizingdata.helpers.AttendanceRecord;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Moses on 7/25/13.
 */
public class MemberAttendanceHistoryActivity extends SherlockListActivity {

    private int memberId = 0;
    private int cycleId = 0;
    private String meetingDate;
    private String fullName;
    private int isPresent;
    private int meetingId = 0;

    private CheckBox chkAttendance;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomActionBar();

        setContentView(R.layout.activity_member_attendance_history);

        if (getIntent().hasExtra("_cycleId")) {
            this.cycleId = getIntent().getIntExtra("_cycleId", 0);
        } else {
            VslaCycle currentCycle = ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle();
            if (null != currentCycle) {
                this.cycleId = currentCycle.getCycleId();
            }
        }
        if (getIntent().hasExtra("_memberId")) {
            this.memberId = getIntent().getIntExtra("_memberId", 0);
        }

        if (getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_meetingDate")) {
            this.meetingDate = getIntent().getStringExtra("_meetingDate");
        }

        if (getIntent().hasExtra("_name")) {
            this.fullName = getIntent().getStringExtra("_name");
        }

        if (getIntent().hasExtra("_isPresent")) {
            this.isPresent = getIntent().getIntExtra("_isPresent", 0);
        }

        // Setup the TextViews
        TextView txtFullName = (TextView) findViewById(R.id.txtMAHFullName);

        // TextView txtMeetingDate = (TextView)findViewById(R.id.txtMAHMeetingDate);
        chkAttendance = (CheckBox) findViewById(R.id.chkMAHAttendance);


        chkAttendance.setChecked(ledgerLinkApplication.getMeetingAttendanceRepo().getMemberAttendance(meetingId, memberId));

        RelativeLayout r = (RelativeLayout) chkAttendance.getParent();

        //chkAttendance.setOnClickListener(new View.OnClickListener() {
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  chkAttendance.toggle();
            }
        });

        chkAttendance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    isPresent = 1;
                } else {
                    isPresent = 0;
                }
            }
        });

        TextView txtComments = (TextView) findViewById(R.id.txtMAHComment);
        txtComments.setText(ledgerLinkApplication.getMeetingAttendanceRepo().getMemberAttendanceComment(meetingId, memberId));

        fullName = fullName.substring(fullName.lastIndexOf(".") + 1).trim();
        txtFullName.setText(fullName);

        // txtMeetingDate.setText(meetingDate);

        // Populate the Attendance History
        populateAttendanceData();

        chkAttendance.requestFocus();
        txtComments.requestFocus();

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
                        if (saveAttendanceComment()) {
                            Toast.makeText(MemberAttendanceHistoryActivity.this, "Comment entered successfully", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "rollCall");
                            i.putExtra("_meetingDate", meetingDate);
                            i.putExtra("_meetingId", meetingId);
                            startActivity(i);
                            finish();
                        }

                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", "rollCall");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        startActivity(i);
                        finish();
                    }
                }
        );


        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("Roll Call");
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        /** actionBar.setDisplayOptions(
         ActionBar.DISPLAY_SHOW_CUSTOM,
         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
         | ActionBar.DISPLAY_SHOW_TITLE
         );
         actionBar.setCustomView(customActionBarView,
         new ActionBar.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT,
         ViewGroup.LayoutParams.MATCH_PARENT)
         ); */
        // END_INCLUDE (inflate_set_custom_view)
    }

    private void populateAttendanceData() {
        ArrayList<AttendanceRecord> attendances = ledgerLinkApplication.getMeetingAttendanceRepo().getMemberAbsenceHistoryInCycle(cycleId, memberId, meetingId);

        if (attendances == null) {
            attendances = new ArrayList<AttendanceRecord>();
        }

        //Now get the data via the adapter
        AttendanceArrayAdapter adapter = new AttendanceArrayAdapter(MemberAttendanceHistoryActivity.this, attendances);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.member_attendance_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "rollCall");
                upIntent.putExtra("_meetingDate", meetingDate);
                upIntent.putExtra("_meetingId", meetingId);

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MeetingActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuMAHCancel:
                i = new Intent(MemberAttendanceHistoryActivity.this, MeetingActivity.class);
                i.putExtra("_tabToSelect", "rollCall");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);
                return true;
            case R.id.mnuMAHSave:
                //First Save the Cycle Dates
                //If successful move to next activity
                if (saveAttendanceComment()) {
                    i = new Intent(MemberAttendanceHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect", "rollCall");
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
        }
        return true;

    }

    public boolean saveAttendanceComment() {
        boolean successFlg = false;
        String comment = null;

        try {
            TextView txtComment = (TextView) findViewById(R.id.txtMAHComment);
            comment = txtComment.getText().toString().trim();
            if (comment.length() > 0) {
                successFlg = ledgerLinkApplication.getMeetingAttendanceRepo().saveMemberAttendanceWithComment(meetingId, memberId, comment, isPresent);
            } else {
                Utils.createAlertDialogOk(MemberAttendanceHistoryActivity.this, "Roll Call", "You have not entered the Comments", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtComment.requestFocus();
                successFlg = false;
            }
            return successFlg;
        } catch (Exception ex) {
            Log.e("MemberAttendanceHistory.saveMemberAttendanceWithComment", ex.getMessage());
            return false;
        }
    }

}