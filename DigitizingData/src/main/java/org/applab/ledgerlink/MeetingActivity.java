package org.applab.ledgerlink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingStartingCash;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Moses on 6/27/13.
 */
public class MeetingActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
    private Meeting currentMeeting;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    LedgerLinkApplication ledgerLinkApplication;

    private static ProgressDialog progressDialog = null;
    private static int httpStatusCode = 0; //To know whether the Request was successful
    private static boolean actionSucceeded = false;
    private static int targetMeetingId = 0;
    private static int currentDataItemPosition = 0;
    private static String serverUri = "";
    private ActionBar actionBar;
    private String meetingDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_meeting);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        setupActionBarAndTabs();


        if (getIntent().hasExtra("_meetingId")) {
            targetMeetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (targetMeetingId == 0) {
            // If target meeting id is 0, then load it as current meeting id
            targetMeetingId = ledgerLinkApplication.getMeetingRepo().getCurrentMeeting(ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle().getCycleId()).getMeetingId();

            // Define the meeting id to be accessed by all tab fragments
            getIntent().putExtra("_meetingId", targetMeetingId);
        }


        if (getIntent().hasExtra("_enableSendData")) {
            boolean enableSendData = getIntent().getBooleanExtra("_enableSendData", false);
        }


        /*if(viewOnly) {
            Utils._meetingDataViewMode = Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY;
        }*/


        // Update Starting Cash
        updateStartingCash(targetMeetingId);

    }

    private void setupActionBarAndTabs() {
        //ActionBar
        actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);


        // String title = String.format("Meeting    %s", meetingDate);
        String title = "Meeting";
        if(! getCurrentMeeting().isMeetingDataSent())
        {
            title = "Send Data";
        }
        else {
            title = "Sent Data";
        }
//        switch (Utils._meetingDataViewMode) {
//            case VIEW_MODE_REVIEW:
//                title = "Send Data";
//                break;
//            case VIEW_MODE_READ_ONLY:
//                title = "Sent Data";
//                break;
//            default:
//                break;
//        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setTag("rollCall").setText("Register").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("summary").setText("Summary").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("startingCash").setText("Starting Cash").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("savings").setText("Savings").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("loansRepaid").setText("Loans Repaid").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("fines").setText("Fines").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("loansIssued").setText("New Loans").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("cashBook").setText("Cash Book").setTabListener(this));


        //Do not show the Send Data tab when in READ_ONLY Mode
        if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            actionBar.addTab(actionBar.newTab().setTag("sendData").setText("Send Data").setTabListener(this));

        }


        if (getIntent().hasExtra("_tabToSelect")) {
            String tabTag = getIntent().getStringExtra("_tabToSelect");
            if (tabTag.equalsIgnoreCase("savings")) {
                actionBar.selectTab(actionBar.getTabAt(3));
            } else if (tabTag.equalsIgnoreCase("startingCash")) {
                actionBar.selectTab(actionBar.getTabAt(2));
            } else if (tabTag.equalsIgnoreCase("loansRepaid")) {
                actionBar.selectTab(actionBar.getTabAt(4));
            } else if (tabTag.equalsIgnoreCase("loansIssued")) {
                actionBar.selectTab(actionBar.getTabAt(6));
            } else if (tabTag.equalsIgnoreCase("cashBook")) {
                actionBar.selectTab(actionBar.getTabAt(7));
            } else if (tabTag.equalsIgnoreCase("fines")) {
                actionBar.selectTab(actionBar.getTabAt(5));
            } else if (tabTag.equalsIgnoreCase("rollCall")) {
                actionBar.selectTab(actionBar.getTabAt(0));
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.meeting, menu);
        if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            menu.findItem(R.id.mnuMeetingFineMember).setEnabled(false);
            return false;
        }
        return Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_REVIEW;
    }

    public Meeting getCurrentMeeting() {
        if (currentMeeting != null) return currentMeeting;

        if (getIntent().hasExtra("_currentMeetingId")) {

            //try to load it from db
            currentMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(getIntent().getIntExtra("_currentMeetingId", 0));
            return currentMeeting;
        }
        else if (getIntent().hasExtra("_meetingId")) {

            //try to load it from db
            currentMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(getIntent().getIntExtra("_meetingId", 0));
            return currentMeeting;
        }

        //else return null
        return currentMeeting;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            /** This event has been set from the send data fragment
             case R.id.mnuMSDFSend:
             //For the Send Data Fragment in case data is sent during the meeting
             sendMeetingData();
             return true;
             /** case R.id.mnuSMDSend:
             //Send Meeting Data: Build JSON String and send it
             sendMeetingData();
             //                Intent i = new Intent(getApplicationContext(), MainActivity.class);
             //                startActivity(i);
             return true;
             case R.id.mnuSMDCancel:
             //Toast.makeText(getBaseContext(), "You have successfully started a new cycle", Toast.LENGTH_LONG).show();
             Intent i = new Intent(getApplicationContext(), MainActivity.class);
             startActivity(i);
             return true; */
            case R.id.mnuMCBFSave:
                return false;
            case R.id.mnuMeetingDelete:
                Intent intent = new Intent(getApplicationContext(), DeleteMeetingActivity.class);
                intent.putExtra("_meetingId", targetMeetingId);
                startActivity(intent);
                return true;
            case R.id.mnuMeetingChangeDate:
                Intent changeMeetingDate = new Intent(getApplicationContext(), ChangeMeetingDateActivity.class);
                changeMeetingDate.putExtra("_meetingId", targetMeetingId);
                startActivity(changeMeetingDate);
                return true;
            case R.id.mnuMeetingFineMember:
                // Do not invoke the event when in Read only Mode
                if (this.isViewOnly()) {
                    Toast.makeText(this.getBaseContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return false;
                }
                Intent fineMemberIntent = new Intent(getApplicationContext(), FineMemberMeetingActivity.class);
                fineMemberIntent.putExtra("_meetingId", getIntent().getIntExtra("_meetingId", 0));
                startActivity(fineMemberIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        String selectedTag = (String) tab.getTag();
        SherlockFragment fragment;

        mActionMode = null;

        if (selectedTag.equalsIgnoreCase("summary")) {
            fragment = new MeetingSummaryFrag();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        } else if (selectedTag.equalsIgnoreCase("rollCall")) {
            fragment = new MeetingRollCallFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase("startingCash")) {
            fragment = new MeetingStartingCashFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase("savings")) {
            fragment = new MeetingSavingsFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase("loansRepaid")) {
            fragment = new MeetingLoansRepaidFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase("fines")) {
            fragment = new MeetingFinesFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase("loansIssued")) {
            fragment = new MeetingLoansIssuedFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase("cashBook")) {
            fragment = new MeetingCashBookFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);

            //Create an Context Action Bar Menu
            //mActionMode = MeetingActivity.this.startActionMode(cashBookActionModeCallback);
        } else if (selectedTag.equalsIgnoreCase("sendData")) {
            fragment = new MeetingSendDataFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else {
            //Not Sure what to do
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    public boolean isViewOnly() {
        if (getIntent().hasExtra("_viewOnly")) {
            return getIntent().getBooleanExtra("_viewOnly", false);
        }
        return getCurrentMeeting() != null && !getCurrentMeeting().isCurrent();
    }

    //Indicates that we are viewing sent data
    public boolean isViewingSentData() {
        if (getIntent().hasExtra("_viewingSentData")) {
            return getIntent().getBooleanExtra("_viewingSentData", false);
        }
        return false;
    }

    protected Object mActionMode;
    private ActionMode.Callback cashBookActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            // Assumes that you have "cash_book.xml" menu resources
            inflater.inflate(R.menu.meeting_cash_book, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.mnuMCBFSave:
                    Toast.makeText(MeetingActivity.this, "Selected menu",
                            Toast.LENGTH_LONG).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

    //Change this to send specified meeting by id
    public void sendMeetingData(int meetingId) {


        MeetingRepo repo = new MeetingRepo(getApplicationContext());

        //TODO: Confirm this later. Does not support multiple cycles
        //Meeting meeting = repo.getMostRecentMeeting();
        Meeting meeting = repo.getMeetingById(meetingId);

        HashMap<String, String> meetingData = repo.generateMeetingDataMapToSendToServer(meetingId);

        sendDataUsingPostAsync(meeting.getMeetingId(), meetingData);
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
    private static class SendDataPostAsyncTask extends AsyncTask<String, String, JSONObject> {

        //Use a Weak Reference
        private final WeakReference<MeetingActivity> meetingActivityWeakReference;
        private String message = "Please wait...";

        //Initialize the Weak reference in the constructor
        public SendDataPostAsyncTask(MeetingActivity meetingActivity) {
            this.meetingActivityWeakReference = new WeakReference<MeetingActivity>(meetingActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (meetingActivityWeakReference.get() != null && !meetingActivityWeakReference.get().isFinishing()) {
                    if (null == progressDialog) {
                        progressDialog = new ProgressDialog(meetingActivityWeakReference.get());
                        progressDialog.setTitle("Sending Meeting Data...");

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
                assert progressDialog != null;
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
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

                        // get response entity
                        HttpEntity entity = response.getEntity();
                        httpStatusCode = response.getStatusLine().getStatusCode();

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
                    actionSucceeded = ((result.getInt("StatusCode") == 0));
                }
                if (actionSucceeded) {
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
                        meetingRepo.updateDataSentFlag(targetMeetingId, cal.getTime());

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

    // Update starting cash
    private void updateStartingCash(int targetMeetingId) {

        // Initialize contributing metrics
        double totalSavings = 0.0;
        double totalLoansRepaid = 0.0;
        double totalLoansIssued = 0.0;
        double totalFines = 0.0;
        double actualStartingCash = 0.0;
        double expectedStartingCash = 0.0;
        MeetingStartingCash previousMeetingClosure = null;
        double cashTakenToBank = 0.0;
        String comment = "Closing";
        boolean successFlg = false;
        Meeting previousMeeting = null;
        int meetingId = -1;

        MeetingSavingRepo savingRepo = new MeetingSavingRepo(getApplicationContext());
        MeetingFineRepo fineRepo = new MeetingFineRepo(getApplicationContext());
        MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());
        MeetingLoanRepaymentRepo loanRepaymentRepo = new MeetingLoanRepaymentRepo(getApplicationContext());

        previousMeeting = ledgerLinkApplication.getMeetingRepo().getPreviousMeeting(ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getVslaCycle().getCycleId(), targetMeetingId);

        if (previousMeeting != null) {
            //meetingId = previousMeeting.getMeetingId();
            meetingId = targetMeetingId;

            previousMeetingClosure = ledgerLinkApplication.getMeetingRepo().getMeetingStartingCash(previousMeeting.getMeetingId());

            totalSavings = savingRepo.getTotalSavingsInMeeting(previousMeeting.getMeetingId());
            totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(previousMeeting.getMeetingId());
            totalLoansRepaid = loanRepaymentRepo.getTotalLoansRepaidInMeeting(previousMeeting.getMeetingId());
            totalFines = fineRepo.getTotalFinesPaidInThisMeeting(previousMeeting.getMeetingId());
            actualStartingCash = previousMeetingClosure.getActualStartingCash();
            cashTakenToBank = previousMeetingClosure.getCashSavedInBank();
            expectedStartingCash = actualStartingCash + totalSavings + totalLoansRepaid + totalFines - totalLoansIssued - cashTakenToBank;
            Log.d("MA exprevnotnull", "Meeting Id "+ meetingId +" "+ "actSC "+ actualStartingCash +" "+ "cashTskenToBnk "+ cashTakenToBank +" "+String.valueOf(expectedStartingCash));

            if (previousMeeting.isGettingStarted()) {
                expectedStartingCash = previousMeeting.getVslaCycle().getFinesAtSetup() + previousMeeting.getVslaCycle().getInterestAtSetup() + totalSavings;
                Log.d("MA exCah", String.valueOf(expectedStartingCash));
            }
        } else {

            /** If no previous meeting; i.e. fresh Start expected starting Cash = 0;
             *If GSW has recorded cash then the recorded cash should be shown here as a net
             */
            meetingId = targetMeetingId;
            if (ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).isGettingStarted()) {
                totalSavings = savingRepo.getTotalSavingsInMeeting(ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getMeetingId());
                expectedStartingCash = ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getVslaCycle().getFinesAtSetup() + ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getVslaCycle().getInterestAtSetup() + totalSavings;
                Log.d("MA exCahgsw", String.valueOf(expectedStartingCash));
            }
        }

        Log.d("MA exCah3", String.valueOf(meetingId) + String.valueOf(expectedStartingCash));
        // Save Starting cash values as closing balance for previous meeting
        successFlg = ledgerLinkApplication.getMeetingRepo().updateExpectedStartingCash(meetingId, expectedStartingCash);
    }
}
