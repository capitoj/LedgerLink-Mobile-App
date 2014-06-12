package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.AttendanceArrayAdapter;
import org.applab.digitizingdata.helpers.AttendanceRecord;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/25/13.
 */
public class MemberAttendanceHistoryActivity extends SherlockListActivity {

    private ActionBar actionBar;
    private ArrayList<AttendanceRecord> attendances;

    private int memberId = 0;
    private int cycleId = 0;
    private VslaCycle currentCycle;
    private String meetingDate;
    private String fullNames;
    private int isPresent;
    private int meetingId = 0;

    private VslaCycleRepo cycleRepo;
    private MeetingAttendanceRepo attendanceRepo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(saveAttendanceComment()) {
                            Toast.makeText(MemberAttendanceHistoryActivity.this, "Comment entered successfully", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "rollCall");
                            i.putExtra("_meetingDate",meetingDate);
                            i.putExtra("_meetingId",meetingId);
                            startActivity(i);
                            finish();
                        }

                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", "rollCall");
                        i.putExtra("_meetingDate",meetingDate);
                        i.putExtra("_meetingId",meetingId);
                        startActivity(i);
                        finish();
                    }
                });


        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("Roll Call");
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)

        setContentView(R.layout.activity_member_attendance_history);
        cycleRepo = new VslaCycleRepo(getApplicationContext());
        attendanceRepo = new MeetingAttendanceRepo(getApplicationContext());

        //TODO: I will get the Cycle in which the Meeting belongs to
        if(getIntent().hasExtra("_cycleId")) {
            this.cycleId = getIntent().getIntExtra("_cycleId", 0);
        }
        else {
            currentCycle = cycleRepo.getCurrentCycle();
            if(null!= currentCycle) {
                this.cycleId = currentCycle.getCycleId();
            }
        }
        if(getIntent().hasExtra("_memberId")) {
            this.memberId = getIntent().getIntExtra("_memberId", 0);
        }

        if(getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if(getIntent().hasExtra("_meetingDate")) {
            this.meetingDate = getIntent().getStringExtra("_meetingDate");
        }

        if(getIntent().hasExtra("_names")) {
            this.fullNames = getIntent().getStringExtra("_names");
        }

        if(getIntent().hasExtra("_isPresent")) {
            this.isPresent = getIntent().getIntExtra("_isPresent", 0);
        }

        //Setup the TextViews
        TextView txtFullNames = (TextView)findViewById(R.id.txtMAHFullName);
        TextView txtMeetingDate = (TextView)findViewById(R.id.txtMAHMeetingDate);
        CheckBox chkAttendance = (CheckBox)findViewById(R.id.chkMAHAttendance);
        TextView txtComments = (TextView)findViewById(R.id.txtMAHComment);

        chkAttendance.setChecked(attendanceRepo.getMemberAttendance(meetingId, memberId));
        txtComments.setText(attendanceRepo.getMemberAttendanceComment(meetingId,memberId));

        txtFullNames.setText(fullNames);
        txtMeetingDate.setText(meetingDate);

        //Populate the Attendance History
        populateAttendanceData();

        chkAttendance.requestFocus();
        txtComments.requestFocus();

    }

    private void populateAttendanceData() {
        //Load the Main Menu
        MeetingAttendanceRepo repo = new MeetingAttendanceRepo(getApplicationContext());
        attendances = repo.getMemberAttendanceHistoryInCycle(cycleId, memberId);

        if(attendances == null) {
            attendances = new ArrayList<AttendanceRecord>();
        }

        //Now get the data via the adapter
        AttendanceArrayAdapter adapter = new AttendanceArrayAdapter(MemberAttendanceHistoryActivity.this, attendances, "fonts/roboto-regular.ttf");

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
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "rollCall");
                upIntent.putExtra("_meetingDate",meetingDate);
                upIntent.putExtra("_meetingId",meetingId);

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
                i.putExtra("_meetingDate",meetingDate);
                i.putExtra("_meetingId",meetingId);
                startActivity(i);
                return true;
            case R.id.mnuMAHSave:
                //First Save the Cycle Dates
                //If successful move to next activity
                if(saveAttendanceComment()) {
                    i = new Intent(MemberAttendanceHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect", "rollCall");
                    i.putExtra("_meetingDate",meetingDate);
                    i.putExtra("_meetingId",meetingId);
                    startActivity(i);
                }
        }
        return true;

    }

    public boolean saveAttendanceComment(){
        boolean successFlg = false;
        String comment = null;

        try{
            TextView txtComment = (TextView)findViewById(R.id.txtMAHComment);
            comment = txtComment.getText().toString().trim();
            if(comment.length() > 0) {
                if(attendanceRepo == null) {
                    attendanceRepo = new MeetingAttendanceRepo(MemberAttendanceHistoryActivity.this);
                }
                successFlg = attendanceRepo.saveMemberAttendanceComment(meetingId, memberId,comment);
            }
            else {
                Utils.createAlertDialogOk(MemberAttendanceHistoryActivity.this,"Roll Call", "You have not entered the Comments", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtComment.requestFocus();
                successFlg = false;
            }
            return successFlg;
        }
        catch(Exception ex) {
            Log.e("MemberAttendanceHistory.saveMemberAttendanceComment", ex.getMessage());
            return successFlg;
        }
    }

}