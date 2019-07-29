package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.SendMeetingDataArrayAdapter;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;

/**
 * Created by Moses on 7/23/13.
 */
public class SendMeetingDataActivity extends SherlockListActivity {
    private ArrayList<Meeting> meetings;
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
        actionBar.setTitle("Send Data");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Populate the Meetings
        populateMeetingData();
    }

    //Populate Members List
    private void populateMeetingData() {
        //Load the Main Menu
        //TODO: Testing purposes only. Should be removed
        //If the process has finished, then mark the meeting as sent

        //Retrieve the meetings whose data has not been sent
        MeetingRepo meetingRepo = new MeetingRepo(this);
        VslaCycle recentCycle = new VslaCycleRepo(getApplicationContext()).getMostRecentCycle();
        meetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatus(false, recentCycle.getCycleId());

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
                i.putExtra("_meetingId", selectedMeeting.getMeetingId());
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

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}