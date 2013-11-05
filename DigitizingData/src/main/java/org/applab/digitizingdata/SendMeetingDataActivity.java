package org.applab.digitizingdata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;

import org.applab.digitizingdata.R;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.SendMeetingDataArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Moses on 7/23/13.
 */
public class SendMeetingDataActivity extends SherlockListActivity {
    private ActionBar actionBar;
    private ArrayList<Meeting> meetings;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send_meeting_data);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Send Data");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Populate the Meetings
        populateMeetingData();
    }

    //Populate Members List
    private void populateMeetingData() {
        //Load the Main Menu
        MeetingRepo meetingRepo = new MeetingRepo(getApplicationContext());

        //TODO: Testing purposes only. Should be removed
        //If the process has finished, then mark the meeting as sent
        Calendar cal = Calendar.getInstance();
        //meetingRepo.updateDataSentFlag(1, false, cal.getTime());

        //Retrieve the meetings whose data has not been sent
        meetings = meetingRepo.getAllMeetingsByDataSentStatus(false);

        if(meetings == null) {
            meetings = new ArrayList<Meeting>();
        }

        //Now get the data via the adapter
        SendMeetingDataArrayAdapter adapter = new SendMeetingDataArrayAdapter(getBaseContext(), meetings);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        // listening to single list item on click
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Launching new Activity on selecting single List Item
                Meeting selectedMeeting = meetings.get(position);
                Intent i = new Intent(view.getContext(), MeetingActivity.class);

                i.putExtra("_meetingDate", Utils.formatDate(selectedMeeting.getMeetingDate(), "dd-MMM-yyyy"));
                i.putExtra("_meetingId",selectedMeeting.getMeetingId());
                i.putExtra("_enableSendData", true);

                //Indicate that current data view mode is REVIEW
                Utils._meetingDataViewMode = Utils.MeetingDataViewMode.VIEW_MODE_REVIEW;
                Utils._meetingActiveActionBarMenu = Utils.MeetingActiveActionBarMenu.MENU_REVIEW_SEND;
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                }
                else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }

        return true;

    }
}