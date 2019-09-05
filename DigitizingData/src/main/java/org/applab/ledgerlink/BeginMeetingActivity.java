package org.applab.ledgerlink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.ConcurrentMeetingsArrayAdapter;
import org.applab.ledgerlink.helpers.DataFactory;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.tasks.SubmitDataAsync;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BeginMeetingActivity extends AppCompatActivity {
    private ArrayList<Meeting> pastMeetings = null;
    private static ProgressDialog progressDialog = null;
    private static int targetMeetingId = 0;
    private static String serverUri = "";
    private static Meeting currentMeeting = null;
    private static int numberOfSentMeetings = 0;
    private ArrayList<Meeting> currentMeetings;
    private boolean noPriorMeetings = false;

    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_begin_meeting);
        refreshActivityView();
    }


    private void refreshActivityView() {


        //Check the past meetings that have not been sent
        VslaCycle recentCycle = ledgerLinkApplication.getVslaCycleRepo().getMostRecentCycle();
        //past meetings should be not active and not sent
        pastMeetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatusAndActiveStatus();

        // Check fore fresh start; no meetings other than GSW
        // if (meetingRepo.getAllNonGSWMeetings().isEmpty()) {
        if (ledgerLinkApplication.getMeetingRepo().getAllMeetings(recentCycle.getCycleId()).isEmpty()) {
            noPriorMeetings = true;
        }

        inflateCustomBar();

        //populate the list
        populateMeetingsList();
        //add LayoutParams
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.setMargins(left, top, right, bottom);
        //Get the Sections for Current Meeting and Past Meetings
        LinearLayout grpCurrentMeeting = (LinearLayout) findViewById(R.id.grpBMCurrentMeeting);
        LinearLayout grpPastMeetings = (LinearLayout) findViewById(R.id.grpBMPastMeetings);
        //Get the Instruction label to dynamically change the instructions based on unsent meetings
        TextView tvInstructionsHeader = (TextView) findViewById(R.id.lblBMHeader);
        StringBuilder sb = null;
        // if(pastMeetings.size() > 0) {
        if (null != pastMeetings) {

            if (pastMeetings.size() > 0) {

                //Setup the Instruction
                sb = new StringBuilder(getString(R.string.press_send_to_send_all_data));
                sb.append(getString(R.string.you_will_not_be_able_to_edit_the_current_meeting));
                tvInstructionsHeader.setText(Html.fromHtml(sb.toString()));

                //Quick-fix for Determining the Most recent Meeting whose data is not sent
                //Show list with current meetings Since we are now considering the flag
                Meeting mostRecentUnsentMeeting = pastMeetings.get(0);
                for (Meeting meeting : pastMeetings) {
                    if (mostRecentUnsentMeeting.getMeetingDate().compareTo(meeting.getMeetingDate()) <= 0) {
                        mostRecentUnsentMeeting = meeting;
                    }
                }

                //Display it
                //TextView tvMostRecentUnsentMeeting = (TextView) findViewById(R.id.lblBMCurrentMeetingDate);
                //tvMostRecentUnsentMeeting.setText(Utils.formatDate(mostRecentUnsentMeeting.getMeetingDate(), getString(R.string.date_format)));

                // Set onclick event for the current meeting
                final Meeting finalMostRecentUnsentMeeting = mostRecentUnsentMeeting;
//                tvMostRecentUnsentMeeting.setOnClickListener(new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View view)
//                    {
//                        //load details for this meeting in meeting activity
//                        //it modifiable mode
//                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
//                        i.putExtra("_meetingId", finalMostRecentUnsentMeeting.getMeetingId());
//                        i.putExtra("_currentMeetingId", finalMostRecentUnsentMeeting.getMeetingId());
//                        //make the view mode modifiable
//                        i.putExtra("_viewOnly", false);
//                        i.putExtra("_meetingDate", Utils.formatDate(finalMostRecentUnsentMeeting.getMeetingDate(), getString(R.string.date_format)));
//                        startActivity(i);
//                    }
//                });

                // If the Past Meetings are less than 4 i.e. 3 displayed in unsent past meetings  then hide the warning message
                if (pastMeetings.size() < 4) {
                    TextView tvWarning = (TextView) findViewById(R.id.lblBMWarning);
                    grpPastMeetings.removeView(tvWarning);
                }

                // If only the GSW meeting exists then hide the current meeting section
                if ((pastMeetings.size() == 1) && (pastMeetings.get(0).isGettingStarted()) && currentMeetings.isEmpty()) {

                    // Hide the Section for Current Meeting
                    grpCurrentMeeting.setVisibility(View.GONE);
                } else if (pastMeetings.size() > 1) {
                    for (Meeting meeting : pastMeetings) {

                        // Skip the Current meeting i.e. mostRecentUnsentMeeting coz it is already displayed
                        if (meeting.getMeetingId() == mostRecentUnsentMeeting.getMeetingId()) {
                        }
                    }
                }

            } else {

                // Hide the Section for Past Meetings
                LinearLayout parent = (LinearLayout) grpPastMeetings.getParent();
                grpPastMeetings.setVisibility(View.GONE);


                // Display the default Instructions
                sb = new StringBuilder(getString(R.string.press_begin_new_meeting));

            }
        }

        if(currentMeetings.size() == 0) {
            // Hide the Section for Current Meeting
            grpCurrentMeeting.setVisibility(View.GONE);

        }

        // Take care of Fresh start No meetings at all
        if (noPriorMeetings) {

            //Hide the Section for Past Meetings
            LinearLayout parent = (LinearLayout) grpPastMeetings.getParent();
            grpPastMeetings.setVisibility(View.GONE);

            //Hide the Section for Current Meeting
            grpCurrentMeeting.setVisibility(View.GONE);

            //Display the default Instructions
            sb = new StringBuilder(getString(R.string.press_begin_new_meeting));

        }

        //Show the Instructions
        tvInstructionsHeader.setText(Html.fromHtml(sb != null ? sb.toString() : null));
    }


    //Populate Meetings List
    protected void populateMeetingsList() {

        //populate the current meetings
        populateCurrentMeetingsList();

        //This is dirty but we have to do what we gotta do TODO: will clean up later
        //Remove meetings set as current from the past list..
        //These should be displayed in current section
        for (int i = 0; i < pastMeetings.size(); i++) {
            //If meetings is current
            //And requested later on, if GSW, then dont show it
            if (pastMeetings.get(i).isCurrent() || pastMeetings.get(i).isGettingStarted()) {
                pastMeetings.remove(i);
            }
        }

        // Now get the data via the adapter
        ConcurrentMeetingsArrayAdapter adapter = new ConcurrentMeetingsArrayAdapter(getBaseContext(), pastMeetings);

        // listening to single list item on click
        ListView membersListView = (ListView) findViewById(R.id.lstBMPastMeetingList);

        membersListView.setAdapter(adapter);
        membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Meeting meeting = pastMeetings.get(position);
                //Do as you wish with this meeting
                Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                i.putExtra("_meetingDate", Utils.formatDate(meeting.getMeetingDate(), getString(R.string.date_format)));
                i.putExtra("_meetingId", meeting.getMeetingId());
                i.putExtra("_viewOnly", true);  //viewing past meeetings should be read only
                startActivity(i);

            }
        });
        Utils.setListViewHeightBasedOnChildren(membersListView);
    }


    //Populate Meetings List
    protected void populateCurrentMeetingsList() {
        //to populate the current meetings
        //Now get the data via the adapter

        currentMeetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsByActiveStatus();
        ConcurrentMeetingsArrayAdapter adapter = new ConcurrentMeetingsArrayAdapter(getBaseContext(), currentMeetings);

        // listening to single list item on click
        ListView currentMeetingsList = (ListView) findViewById(R.id.lstBMCurrentMeetings);

        currentMeetingsList.setAdapter(adapter);
        currentMeetingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Meeting meeting = currentMeetings.get(position);
                //Do as you wish with this meeting
                Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                i.putExtra("_meetingDate", Utils.formatDate(meeting.getMeetingDate(), getString(R.string.date_format)));
                i.putExtra("_meetingId", meeting.getMeetingId());
                i.putExtra("_currentMeetingId", meeting.getMeetingId());
                i.putExtra("_viewOnly", false);  //viewing current meetings should not be read only
                startActivity(i);

            }
        });
        Utils.setListViewHeightBasedOnChildren(currentMeetingsList);
        if (currentMeetings.size() > 1) {
            //Set the title to plural
            TextView currentMeetingsSectionHeading = (TextView) findViewById(R.id.lblBMSection1);
            currentMeetingsSectionHeading.setText(R.string.current_meetings);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //final MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.begin_meeting, menu);
        return true;
    }


    private void inflateCustomBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_begin_send, null);
        customActionBarView.findViewById(R.id.actionbar_send).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MeetingRepo meetingRepo = new MeetingRepo(BeginMeetingActivity.this);
                        serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", getString(R.string.submitdata));
                        VslaCycle recentCycle = ledgerLinkApplication.getVslaCycleRepo().getMostRecentCycle();
                        List<Meeting> pastMeetings = meetingRepo.getPastMeetings(recentCycle.getCycleId());
                        JSONArray jsonArray = new JSONArray();
                        for(Meeting meeting : pastMeetings){
                            String meetingDataToBeSent = DataFactory.getJSONOutput(BeginMeetingActivity.this, meeting.getMeetingId());
                            try {
                                JSONObject jsonItem = new JSONObject(meetingDataToBeSent);
                                jsonArray.put(jsonItem);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("FileSubmission", jsonArray);
                            serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", getString(R.string.submitdata));
                            new SubmitDataAsync(BeginMeetingActivity.this).execute(serverUri, String.valueOf(jsonObject));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        /*
                        //send all meeting data
                        numberOfSentMeetings = 0;
                        for (Meeting thisMeeting : pastMeetings) {
                            sendMeetingData(thisMeeting.getMeetingId());

                            //If sending of previous meeting failed, stop this loop
                            if (!actionSucceeded) {
                                break;
                            }
                        }
                        if (numberOfSentMeetings > 0) {
                            //If atleast a meeting was sent succesfully, refresh the view to reflect sent meetings
                            refreshActivityView();
                        }*/
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_begin).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingDefinitionActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
        );

        if (noPriorMeetings) {
            customActionBarView.findViewById(R.id.actionbar_send).setVisibility(View.GONE);
        }


        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setTitle(R.string.meeting_main);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //NOT necessary since we are not using custom view
        /*
        int id = item.getItemId();
        if (id == R.id.mnuBMBegin) {
            Intent i = new Intent(getApplicationContext(), MeetingDefinitionActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
        */
        return true;

    }


    //Change this to send specified meeting by id
    public void sendMeetingData(int meetingId) {
        //If no network, hide send meeting button
        if (!Utils.isNetworkConnected(getApplicationContext())) {
            Toast.makeText(DatabaseHandler.databaseContext, R.string.can_not_send_info_bse_mobile_data_or_internet_disabled, Toast.LENGTH_LONG).show();
            return;
        }
        //TODO: Confirm this later. Does not support multiple cycles
        //Meeting meeting = repo.getMostRecentMeeting();
        currentMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        HashMap<String, String> meetingData = ledgerLinkApplication.getMeetingRepo().generateMeetingDataMapToSendToServer(meetingId);

        sendDataUsingPostAsync(meetingId, meetingData);
    }


    //Brought this method back from SendDataRepo
    private void sendDataUsingPostAsync(int meetingId, HashMap<String, String> dataFromPhone) {
        //Store the MeetingId as it will be used later after the Async process
        targetMeetingId = meetingId;

        //First identify the initial data to be sent
        /*
        SendDataRepo.dataToBeSent = dataFromPhone;
        currentDataItemPosition = 1;
        String request = SendDataRepo.dataToBeSent.get(SendDataRepo.meetingDataItems.get(currentDataItemPosition));
        serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "submitdata");

        new SendDataPostAsyncTask(this).execute(serverUri, request);
        */
    }

}
