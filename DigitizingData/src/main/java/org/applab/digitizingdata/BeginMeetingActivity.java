package org.applab.digitizingdata;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.ConcurrentMeetingsArrayAdapter;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.MeetingsArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.SendDataRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;



public class BeginMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    MeetingRepo meetingRepo = null;
    ArrayList<Meeting> pastMeetings = null;
    VslaCycle recentCycle = null;
    VslaCycleRepo cycleRepo = null;
    private static ProgressDialog progressDialog = null;
    private static int targetMeetingId = 0;
    private static int currentDataItemPosition = 0;
    private static String serverUri = "";
    private static boolean actionSucceeded = false;
    private static Meeting currentMeeting = null;
    private static int httpStatusCode = 0; //To know whether the Request was successful
    private static int numberOfSentMeetings = 0;
    private ArrayList<Meeting> currentMeetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_begin_meeting);
        refreshActivityView();


    }


    private void refreshActivityView()
    {

        inflateCustomBar();
        //Check the past meetings that have not been sent
        if (null == cycleRepo) cycleRepo = new VslaCycleRepo(getApplicationContext());

        if (null == meetingRepo) meetingRepo = new MeetingRepo(getApplicationContext());

        recentCycle = cycleRepo.getMostRecentCycle();
        pastMeetings = meetingRepo.getAllMeetingsByDataSentStatus(false);
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
            if(pastMeetings.size() > 1) {
                //Set the title to plural
                TextView currentMeetingsSectionHeading = (TextView) findViewById(R.id.lblBMSection1);
                currentMeetingsSectionHeading.setText("CURRENT MEETINGS");
            }
            if (pastMeetings.size() > 0) {

                //Setup the Instruction
                sb = new StringBuilder("Tap <b>Send</b> to send all data for all existing meetings or <b>Begin</b> to begin a new meeting.");
                sb.append("You will not be able to edit the current meeting after beginning a new meeting.");
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
                //tvMostRecentUnsentMeeting.setText(Utils.formatDate(mostRecentUnsentMeeting.getMeetingDate(), "dd MMM yyyy"));

                //Set onclick event for the current meeting
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
//                        i.putExtra("_meetingDate", Utils.formatDate(finalMostRecentUnsentMeeting.getMeetingDate(), "dd MMM yyyy"));
//                        startActivity(i);
//                    }
//                });

                if (pastMeetings.size() > 1) {
                    for (Meeting meeting : pastMeetings) {
                        //Skip the Current meeting i.e. mostRecentUnsentMeeting coz it is already displayed
                        if (meeting.getMeetingId() == mostRecentUnsentMeeting.getMeetingId()) {
                            continue;
                        }
                        //We are using a list now
                        //So this block is obsolete
                        /*
                        TextView tv = new TextView(this);
                        tv.setText(Utils.formatDate(meeting.getMeetingDate(), "dd MMM yyyy"));
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                        //tv.setTextColor(Color.parseColor("#0000FF"));
                        tv.setPadding(10, 2, 2, 2);
                        tv.setId(meeting.getMeetingId());
                        tv.setLayoutParams(params);
                        grpPastMeetings.addView(tv);
                          */
                    }

                    //If the Past Meetings are less than 4 i.e. 3 displayed in unsent past meetings  then hide the warning message
                    if (pastMeetings.size() < 4) {
                        TextView tvWarning = (TextView) findViewById(R.id.lblBMWarning);
                        grpPastMeetings.removeView(tvWarning);
                    }
                } else {
                    //Hide the Section for Past Meetings
                    LinearLayout parent = (LinearLayout) grpPastMeetings.getParent();
                    parent.removeView(grpPastMeetings);

                }
            } else {
                //Hide the Section for Past Meetings
                LinearLayout parent = (LinearLayout) grpPastMeetings.getParent();
                parent.removeView(grpPastMeetings);

                //Hide the Section for Current Meeting
                parent.removeView(grpCurrentMeeting);

                //Display the default Instructions
                sb = new StringBuilder("Tap <b>Begin</b> to begin a new meeting.");

            }
        }
        //Show the Instructions
        tvInstructionsHeader.setText(Html.fromHtml(sb.toString()));
    }


    //Populate Meetings List
    protected void populateMeetingsList() {

        //populate the current meetings
        populateCurrentMeetingsList();

        //This is dirty but we have to do what we gotta do TODO: will clean up later
        //Remove meetings set as current from the past list..
        //These should be displayed in current section
        for(int i=0; i<pastMeetings.size(); i++) {
            if(pastMeetings.get(i).isCurrent()) {
                pastMeetings.remove(i);
            }
        }
        //Now get the data via the adapter
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
                i.putExtra("_meetingDate", Utils.formatDate(meeting.getMeetingDate(), "dd MMM yyyy"));
                i.putExtra("_meetingId",meeting.getMeetingId());
                i.putExtra("_viewOnly",true);  //viewing past meeetings should be read only
                startActivity(i);
                return;

            }
        });
        Utils.setListViewHeightBasedOnChildren(membersListView);
    }



    //Populate Meetings List
    protected void populateCurrentMeetingsList() {
        //to populate the current meetings
        //Now get the data via the adapter
        currentMeetings = meetingRepo.getAllMeetingsByActiveStatus(true);
        ConcurrentMeetingsArrayAdapter adapter = new ConcurrentMeetingsArrayAdapter(getBaseContext(), currentMeetings);


        // listening to single list item on click
        ListView currentMeetingsList = (ListView) findViewById(R.id.lstBMCurrentMeetings);

        currentMeetingsList.setAdapter(adapter);
        currentMeetingsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {

                Meeting meeting = currentMeetings.get(position);
                //Do as you wish with this meeting
                Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                i.putExtra("_meetingDate", Utils.formatDate(meeting.getMeetingDate(), "dd MMM yyyy"));
                i.putExtra("_meetingId", meeting.getMeetingId());
                i.putExtra("_viewOnly", false);  //viewing past meeetings should be read only
                startActivity(i);
                return;

            }
        });
        Utils.setListViewHeightBasedOnChildren(currentMeetingsList);
    }


    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        //final MenuInflater inflater = getSupportMenuInflater();
        //inflater.inflate(R.menu.begin_meeting, menu);
        return true;
    }


    private void inflateCustomBar()
    {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_begin_send, null);
        customActionBarView.findViewById(R.id.actionbar_send).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      //send all meeting data
                        numberOfSentMeetings = 0;
                        for(Meeting thisMeeting : pastMeetings) {
                          sendMeetingData(thisMeeting.getMeetingId());

                          //If sending of previous meeting failed, stop this loop
                            if(!actionSucceeded) {
                                break;
                            }
                        }
                        if(numberOfSentMeetings > 0)
                        {
                            refreshActivityView();
                        }
                    }
                });


        customActionBarView.findViewById(R.id.actionbar_begin).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingDefinitionActivity.class);
                        startActivity(i);
                        finish();
                        return;
                    }
                });
        actionBar = getSupportActionBar();
        actionBar.setTitle("Meeting");
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
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
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
        if(!Utils.isNetworkConnected(getApplicationContext())) {
            Toast.makeText(DatabaseHandler.databaseContext, "Can not send information because mobile data or internet connection is not available.", Toast.LENGTH_LONG).show();
            return;
        }
        //TODO: Confirm this later. Does not support multiple cycles
        //Meeting meeting = repo.getMostRecentMeeting();
        MeetingRepo repo = new MeetingRepo(getApplicationContext());
        currentMeeting = repo.getMeetingById(meetingId);

        HashMap<String, String> meetingData = repo.generateMeetingDataMapToSendToServer(meetingId);

        sendDataUsingPostAsync(meetingId, meetingData);
    }



    //Brought this method back from SendDataRepo
    private void sendDataUsingPostAsync(int meetingId, HashMap<String, String> dataFromPhone) {
        //Store the MeetingId as it will be used later after the Async process
        targetMeetingId = meetingId;

        //First identify the initial data to be sent
        SendDataRepo.dataToBeSent = dataFromPhone;
        currentDataItemPosition = 1;
        String request = SendDataRepo.dataToBeSent.get(SendDataRepo.meetingDataItems.get(currentDataItemPosition));
        serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "submitdata");

        new SendDataPostAsyncTask(this).execute(serverUri, request);
    }



    // The definition of our task class
    private static class SendDataPostAsyncTask extends AsyncTask<String, String, JSONObject>
    {

        //Use a Weak Reference
        private final WeakReference<BeginMeetingActivity> meetingActivityWeakReference;
        private String message = "Please wait...";



        //Initialize the Weak reference in the constructor
        public SendDataPostAsyncTask(BeginMeetingActivity beginMeetingActivity) {
            this.meetingActivityWeakReference = new WeakReference<BeginMeetingActivity>(beginMeetingActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (meetingActivityWeakReference.get() != null && !meetingActivityWeakReference.get().isFinishing()) {
                    if (null == progressDialog) {
                        progressDialog = new ProgressDialog(meetingActivityWeakReference.get());
                        progressDialog.setTitle("Sending Meeting Data..."+currentMeeting.getMeetingId());

                        message = SendDataRepo.progressDialogMessages.get(currentDataItemPosition);
                        if (message == null) {
                            message = "Please wait...";
                        }
                        progressDialog.setMessage(message);
                        progressDialog.setMax(10);
                        progressDialog.setProgress(1);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                    }
                }
            } catch (Exception ex) {
                progressDialog.setMessage(ex.getMessage());
            }
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject result = null;
            String uri = params[0];
            try {
                message = SendDataRepo.progressDialogMessages.get(currentDataItemPosition);
                if (message == null) {
                    message = "Please wait...";
                }
                publishProgress(message);

                //instantiates httpclient to make request
                DefaultHttpClient httpClient = new DefaultHttpClient();

                //url with the post data
                HttpPost httpPost = new HttpPost(uri);

                //passes the results to a string builder/entity
                StringEntity se = new StringEntity(params[1]);

                //sets the post request as the resulting string
                httpPost.setEntity(se);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Response handler
                ResponseHandler<String> rh = new ResponseHandler<String>() {
                    // invoked when client receives response
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException
                    {

                        // get response entity
                        HttpEntity entity = response.getEntity();
                        int httpStatusCode = response.getStatusLine().getStatusCode();

                        // read the response as byte array
                        StringBuffer out = new StringBuffer();
                        byte[] b = EntityUtils.toByteArray(entity);

                        // write the response byte array to a string buffer
                        out.append(new String(b, 0, b.length));
                        return out.toString();
                    }
                };

                String responseString = httpClient.execute(httpPost, rh);

                // close the connection
                httpClient.getConnectionManager().shutdown();

                if (httpStatusCode == 200) //sucess
                {
                    result = new JSONObject(responseString);
                }

                return result;
            } catch (ClientProtocolException exClient) {
                return null;
            } catch (IOException exIo) {
                return null;
            } catch (JSONException exJson) {
                return null;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (null != progressDialog) {
                progressDialog.setMessage(values[0]);
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            try {
                if (result != null) {
                    actionSucceeded = ((result.getInt("StatusCode") == 0) ? true : false);
                }
                if (actionSucceeded) {
                    numberOfSentMeetings++;
                    //Record that the piece of info has been submitted
                    //Pick and Post the next piece of item if there is any RECURSION
                    currentDataItemPosition++;
                    String nextRequest = SendDataRepo.dataToBeSent.get(SendDataRepo.meetingDataItems.get(currentDataItemPosition));
                    if (nextRequest != null) {
                        new SendDataPostAsyncTask(meetingActivityWeakReference.get()).execute(serverUri, nextRequest);
                    } else {
                        //Finished
                        //Have some code to run when process is finished
                        Toast.makeText(DatabaseHandler.databaseContext, "Meeting Data was Sent Successfully", Toast.LENGTH_SHORT).show();

                        //If the process has finished, then mark the meeting as sent
                        Calendar cal = Calendar.getInstance();
                        MeetingRepo meetingRepo = new MeetingRepo(DatabaseHandler.databaseContext);
                        meetingRepo.updateDataSentFlag(targetMeetingId, true, cal.getTime());

                        //Dismiss the progressDialog
                        dismissProgressDialog();

                        //Display the Main Menu or Check & Send data
                        Intent i = new Intent(meetingActivityWeakReference.get(), SendMeetingDataActivity.class);
                        meetingActivityWeakReference.get().startActivity(i);
                    }
                } else {
                    //Process failed
                    Toast.makeText(DatabaseHandler.databaseContext, "Sending of Meeting Data failed due to internet connection error. Try again later.", Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                }
            } catch (JSONException exJson) {
                //Process failed
                Toast.makeText(DatabaseHandler.databaseContext, "Sending of Meeting Data failed due to a data format error. Try again later.", Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            } catch (Exception ex) {
                //Process failed
                Toast.makeText(DatabaseHandler.databaseContext, "Sending of Meeting Data failed. Try again later.", Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }
        }

        //Dismisses the currently showing progress dialog
        private void dismissProgressDialog() {
            if (progressDialog != null) {
                progressDialog.dismiss();
                //set it to null
                progressDialog = null;
            }
        }
    }

}
