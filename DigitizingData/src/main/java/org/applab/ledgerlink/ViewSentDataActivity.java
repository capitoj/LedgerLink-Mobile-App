package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.SendMeetingDataArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Moses on 7/23/13.
 */
public class ViewSentDataActivity extends SherlockListActivity {
    private ArrayList<Meeting> meetings;
    TextView txtHeader;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_send_meeting_data);

        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Sent Data");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Set the appropriate Header Instructional Text
        txtHeader = (TextView) findViewById(R.id.txtSMD_header);
        txtHeader.setText("Select the Meeting whose data you want to view.");

        //Populate the Meetings
        populateMeetingData();
    }

    //Populate Members List
    private void populateMeetingData() {
        //Load the Main Menu
        meetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatus(true);

        ArrayList<Meeting> unsentMeetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatus(false);

        if (meetings.isEmpty()) {
            meetings = new ArrayList<Meeting>();
            txtHeader.setText("All meeting data has been sent");
            if (!unsentMeetings.isEmpty()) {
                txtHeader.setText("You haven't sent any data yet. " +
                        "After sending data, you will be able to review each meeting here. " +
                        "Be sure to send data after each meeting so you do not lose it.");
            }else{
                txtHeader.setText("No meetings yet");
            }
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
                i.putExtra("_meetingId", selectedMeeting.getMeetingId());
                i.putExtra("_currentMeetingId", selectedMeeting.getMeetingId());
                i.putExtra("_enableSendData", false);
                i.putExtra("_viewingSentData", true);

                //Indicate that current data view mode is READ_ONLY
                //Utils._meetingDataViewMode = Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY;
                i.putExtra("_isReadOnly", true);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
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
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return true;

    }

}