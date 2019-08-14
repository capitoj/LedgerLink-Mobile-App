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

import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.domain.model.FinancialInstitution;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingStartingCash;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.DataFactory;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.tasks.SubmitDataAsync;
import org.applab.ledgerlink.repo.FinancialInstitutionRepo;
import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;
import org.applab.ledgerlink.repo.SendDataRepo;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Moses on 6/27/13.
 */
public class MeetingActivity extends ActionBarActivity implements ActionBar.TabListener {
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
            title = getString(R.string.send_data);
        }
        else {
            title = getString(R.string.send_data);
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

        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.rollcall)).setText(getString(R.string.register_main)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.summary)).setText(getString(R.string.summary_main)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.startingcash)).setText(getString(R.string.starting_cash)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.savings)).setText(getString(R.string.savings)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.welfare)).setText(getString(R.string.welfare_main)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.outstandingwelfare)).setText(getString(R.string.outstanding_welfare_mai)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.loansrepaid)).setText(getString(R.string.loans_repaid)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.fines_all_smallcaps)).setText(getString(R.string.fines)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.loansissued)).setText(getString(R.string.new_loans)).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag(getString(R.string.cashbook)).setText(getString(R.string.cash_book)).setTabListener(this));

        //Do not show the Send Data tab when in READ_ONLY Mode
        if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            actionBar.addTab(actionBar.newTab().setTag(getString(R.string.senddata)).setText(getString(R.string.send_data)).setTabListener(this));
        }

        if (getIntent().hasExtra(getString(R.string._tabtoselect))) {
            String tabTag = getIntent().getStringExtra(getString(R.string._tabtoselect));
            if (tabTag.equalsIgnoreCase(getString(R.string.savings))) {
                actionBar.selectTab(actionBar.getTabAt(3));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.startingcash))) {
                actionBar.selectTab(actionBar.getTabAt(2));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.loansrepaid))) {
                actionBar.selectTab(actionBar.getTabAt(6));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.loanissued))) {
                actionBar.selectTab(actionBar.getTabAt(8));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.cashbook))) {
                actionBar.selectTab(actionBar.getTabAt(9));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.fines))) {
                actionBar.selectTab(actionBar.getTabAt(7));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.rollcall))) {
                actionBar.selectTab(actionBar.getTabAt(0));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.welfare_smallcaps))){
                actionBar.selectTab(actionBar.getTabAt(4));
            } else if (tabTag.equalsIgnoreCase(getString(R.string.outstandingwelfare))){
                actionBar.selectTab(actionBar.getTabAt(5));
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
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
            case R.id.mnuMeetingBorrowWelfare:
                if(this.isViewOnly()){
                    Toast.makeText(this.getBaseContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return false;
                }
                Intent borrowWelfareIntent = new Intent(getApplicationContext(), BorrowWelfareMeetingActivity.class);
                borrowWelfareIntent.putExtra("_meetingId", getIntent().getIntExtra("_meetingId", 0));
                startActivity(borrowWelfareIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        String selectedTag = (String) tab.getTag();
        Fragment fragment;

        mActionMode = null;

        if (selectedTag.equalsIgnoreCase(getString(R.string.summary))) {
            fragment = new MeetingSummaryFrag();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.rollcall))) {
            fragment = new MeetingRollCallFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.startingcash))) {
            fragment = new MeetingStartingCashFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.savings))) {
            fragment = new MeetingSavingsFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.loansrepaid))) {
            fragment = new MeetingLoansRepaidFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.fines))) {
            fragment = new MeetingFinesFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.loansissued))) {
            fragment = new MeetingLoansIssuedFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.cashbook))) {
            fragment = new MeetingCashBookFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);

            //Create an Context Action Bar Menu
            //mActionMode = MeetingActivity.this.startActionMode(cashBookActionModeCallback);
        } else if (selectedTag.equalsIgnoreCase(getString(R.string.senddata))) {
            try {
                fragment = new MeetingSendDataFrag();
                fragmentTransaction.replace(android.R.id.content, fragment);
            }catch(Exception e){
                Toast.makeText(MeetingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if(selectedTag.equalsIgnoreCase(getString(R.string.welfare))) {
            try {
                fragment = new MeetingWelfareFrag();
                fragmentTransaction.replace(android.R.id.content, fragment);
            }catch(Exception e){
                Toast.makeText(MeetingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else if(selectedTag.equalsIgnoreCase(getString(R.string.outstandingwelfare))){
            try {
                fragment = new MeetingOutstandingWelfareFrag();
                fragmentTransaction.replace(android.R.id.content, fragment);
            }catch(Exception e){
                Toast.makeText(MeetingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
                    Toast.makeText(MeetingActivity.this, R.string.selected_menu,
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
        String meetingDataToBeSent = DataFactory.getJSONOutput(this, meetingId);
        try {
            JSONObject jsonItem = new JSONObject(meetingDataToBeSent);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonItem);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("FileSubmission", jsonArray);

            VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(getApplicationContext());
            VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
            FinancialInstitutionRepo financialInstitutionRepo = new FinancialInstitutionRepo(getApplicationContext(), vslaInfo.getFiID());
            FinancialInstitution financialInstitution = financialInstitutionRepo.getFinancialInstitution();
//            String baseUrl = "http://127.0.0.1:82";
            String baseUrl = "http://" + financialInstitution.getIpAddress();

            serverUri = String.format("%s/%s/%s", baseUrl, getString(R.string.digitizingdata), getString(R.string.submitdata));
            new SubmitDataAsync(this).execute(serverUri, String.valueOf(jsonObject));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    // Update starting cash
    private void updateStartingCash(int targetMeetingId) {

//        // Initialize contributing metrics
//        double totalSavings = 0.0;
//        double totalLoansRepaid = 0.0;
//        double totalLoansIssued = 0.0;
//        double totalFines = 0.0;
//        double actualStartingCash = 0.0;
//        double expectedStartingCash = 0.0;
//        MeetingStartingCash previousMeetingClosure = null;
//        double cashTakenToBank = 0.0;
//        String comment = "Closing";
//        boolean successFlg = false;
//        Meeting previousMeeting = null;
//        int meetingId = -1;
//
//        MeetingSavingRepo savingRepo = new MeetingSavingRepo(getApplicationContext());
//        MeetingFineRepo fineRepo = new MeetingFineRepo(getApplicationContext());
//        MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());
//        MeetingLoanRepaymentRepo loanRepaymentRepo = new MeetingLoanRepaymentRepo(getApplicationContext());
//
//        previousMeeting = ledgerLinkApplication.getMeetingRepo().getPreviousMeeting(ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getVslaCycle().getCycleId(), targetMeetingId);
//
//        if (previousMeeting != null) {
//            //meetingId = previousMeeting.getMeetingId();
//            meetingId = targetMeetingId;
//
//            previousMeetingClosure = ledgerLinkApplication.getMeetingRepo().getMeetingStartingCash(previousMeeting.getMeetingId());
//
//            totalSavings = savingRepo.getTotalSavingsInMeeting(previousMeeting.getMeetingId());
//            totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(previousMeeting.getMeetingId());
//            totalLoansRepaid = loanRepaymentRepo.getTotalLoansRepaidInMeeting(previousMeeting.getMeetingId());
//            totalFines = fineRepo.getTotalFinesPaidInThisMeeting(previousMeeting.getMeetingId());
//            actualStartingCash = previousMeetingClosure.getActualStartingCash();
//            cashTakenToBank = previousMeetingClosure.getCashSavedInBank();
//            expectedStartingCash = actualStartingCash + totalSavings + totalLoansRepaid + totalFines - totalLoansIssued - cashTakenToBank;
//
//            if (previousMeeting.isGettingStarted()) {
//                expectedStartingCash = previousMeeting.getVslaCycle().getFinesAtSetup() + previousMeeting.getVslaCycle().getInterestAtSetup() + totalSavings;
//                Log.d("MA exCah", String.valueOf(expectedStartingCash));
//            }
//        } else {
//
//            /** If no previous meeting; i.e. fresh Start expected starting Cash = 0;
//             *If GSW has recorded cash then the recorded cash should be shown here as a net
//             */
//            meetingId = targetMeetingId;
//            if (ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).isGettingStarted()) {
//                totalSavings = savingRepo.getTotalSavingsInMeeting(ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getMeetingId());
//                expectedStartingCash = ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getVslaCycle().getFinesAtSetup() + ledgerLinkApplication.getMeetingRepo().getMeetingById(targetMeetingId).getVslaCycle().getInterestAtSetup() + totalSavings;
//                Log.d("MA exCahgsw", String.valueOf(expectedStartingCash));
//            }
//        }
//
//        // Save Starting cash values as closing balance for previous meeting
//        successFlg = ledgerLinkApplication.getMeetingRepo().updateExpectedStartingCash(meetingId, expectedStartingCash);
    }
}
