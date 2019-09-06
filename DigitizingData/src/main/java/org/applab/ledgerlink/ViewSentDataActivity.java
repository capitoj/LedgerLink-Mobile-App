package org.applab.ledgerlink;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.SendMeetingDataArrayAdapter;
import org.applab.ledgerlink.repo.VslaCycleRepo;

import java.util.ArrayList;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

/**
 * Created by Moses on 7/23/13.
 */
public class ViewSentDataActivity extends ListActivity {
    private ArrayList<Meeting> meetings;
    TextView txtHeader;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_send_meeting_data);

        //ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
           // actionBar.setIcon(R.drawable.icon_training_mode);
        }

        //actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setTitle(R.string.sent_data);
        //actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(true);

        //Set the appropriate Header Instructional Text
        txtHeader = (TextView) findViewById(R.id.txtSMD_header);
        txtHeader.setText(R.string.select_meeting_data_to_view);

        //Populate the Meetings
        populateMeetingData();
    }

    //Populate Members List
    private void populateMeetingData() {
        //Load the Main Menu
        VslaCycle recentCycle = new VslaCycleRepo(getApplicationContext()).getMostRecentCycle();
        meetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatus(true, recentCycle.getCycleId());

        ArrayList<Meeting> unsentMeetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatus(false, recentCycle.getCycleId());

        if (meetings.isEmpty()) {
            meetings = new ArrayList<Meeting>();
            txtHeader.setText(R.string.all_meeting_data_sent);
            if (!unsentMeetings.isEmpty()) {
                txtHeader.setText(getString(R.string.have_not_sent__any_data_yet) +
                        getString(R.string.after_sending_data_review_each_meeting) +
                        getString(R.string.be_sure_to_send_data_after_each_meeting));
            }else{
                txtHeader.setText(R.string.no_meeting_yet);
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