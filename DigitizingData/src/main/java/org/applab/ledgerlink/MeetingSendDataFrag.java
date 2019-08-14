package org.applab.ledgerlink;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.ConcurrentMeetingsArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;


public class MeetingSendDataFrag extends Fragment {

    private Menu MENU;
    private ActionBar actionBar = null;
    private int numberOfPastUnsentMeetings = 0;
    private Meeting selectedMeeting;
    private int numberOfMembers;
    private double totalSavingsInSelectedMeeting;
    private double totalLoansRepaidInSelectedMeeting;
    private double totalFinesInSelectedMeeting;
    private double totalLoansIssuedInSelectedMeeting;
    private double totalWelfareInSelectedMeeting;
    private int selectedMeetingAttendance;
    private int currentMeetingId;
    private int selectedMeetingId;
    private boolean viewingCurrentMeeting;
    private ArrayList<Meeting> unsentInactiveMeetings;
    private MeetingActivity parentActivity;
    //public FragmentTransaction fragmentTransaction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.frag_meeting_send_data, container, false);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        String title = getString(R.string.meeting);

        switch(Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = getString(R.string.send_data);
                break;
            case VIEW_MODE_READ_ONLY:
                title = getString(R.string.send_data);
                break;
            default:
                //title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(title);
        if(parentActivity.getCurrentMeeting() != null)
        {
            currentMeetingId = parentActivity.getCurrentMeeting().getMeetingId();
        }
        else {
            currentMeetingId = 0;
        }
        refreshFragmentView();

    }

    private void refreshFragmentView() {

        loadFragmentInformation(selectedMeetingId);

        //Set title
       // actionBar.setTitle("MEETING "+Utils.formatDate(selectedMeeting.getMeetingDate(), "dd MMM yyyy"));
        actionBar.setTitle(R.string.meeting_cap);
        actionBar.setSubtitle(Utils.formatDate(selectedMeeting.getMeetingDate(), "dd MMM yyyy"));
        Log.i(getString(R.string.senddatafrag), getString(R.string.no_of_unsent_meeting) + numberOfPastUnsentMeetings);

        LinearLayout layoutMSDUnsentPastMeetings = (LinearLayout) getActivity().findViewById(R.id.layoutMSDUnsentPastMeetings);

        //Hide unrequired views
        layoutMSDUnsentPastMeetings.setVisibility( (numberOfPastUnsentMeetings==0) ? View.GONE : View.VISIBLE);

        //TODO: Load current meeting(s)
        /*/could be more than one
        if(activeMeetings != null) {
            if(activeMeetings.size()>0) {
                TextView txtMSDFragCurrentMeetingDetails = (TextView) getActivity()().findViewById(R.id.txtMSDFragCurrentMeetingDetails);
                txtMSDFragCurrentMeetingDetails.setText(Utils.formatDate(activeMeetings.get(0).getMeetingDate(), "dd MMM yyyy"));

                txtMSDFragCurrentMeetingDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //To change body of implemented methods use File | Settings | File Templates.
                        selectedMeetingId = currentMeetingId;
                        viewingCurrentMeeting = true;
                        refreshFragmentView();
                    }
                });
            }
        } */



        //If viewing current meeting, hide the current meeting summary
        LinearLayout layoutMSDCurrentMeetingSummary = (LinearLayout) getActivity().findViewById(R.id.layoutMSDCurrentMeetingSummary);
        //layoutMSDCurrentMeetingSummary.setVisibility(selectedMeeting.isCurrent() ? View.GONE : View.VISIBLE);
        layoutMSDCurrentMeetingSummary.setVisibility(viewingCurrentMeeting ? View.GONE : View.VISIBLE);
        //Populate summary of the "Current" meeting from the parent activity
        //Apply null check

        if(parentActivity.getCurrentMeeting() != null) {
            TextView txtMSDFragCurrentMeetingDetails = (TextView) getActivity().findViewById(R.id.txtMSDFragCurrentMeetingDetails);
            txtMSDFragCurrentMeetingDetails.setText(Utils.formatDate(parentActivity.getCurrentMeeting().getMeetingDate(), "dd MMM yyyy"));

            txtMSDFragCurrentMeetingDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //To change body of implemented methods use File | Settings | File Templates.
                    selectedMeetingId = parentActivity.getCurrentMeeting().getMeetingId();
                    getActivity().getIntent().putExtra("_meetingId", selectedMeetingId);
                    //make the view mode modifiable

                    getActivity().getIntent().putExtra("_viewOnly", false);
                    getActivity().getIntent().putExtra("_meetingDate", Utils.formatDate(parentActivity.getCurrentMeeting().getMeetingDate(), "dd MMM yyyy"));
                    getActivity().getIntent().getExtras();
                    viewingCurrentMeeting = true;
                    refreshFragmentView();
                }
            });
        }
        else {
            //current meeting is null, i.e no current meeting in view hide the current meeting section
            //TODO: to accomodate concurrent cycles, this section may show all current meetings for the differenct cycles
            layoutMSDCurrentMeetingSummary.setVisibility(View.GONE);
        }
        populateSelectedMeetingSummary();
        // TextView txtStatus = (TextView) getActivity()().findViewById(R.id.lblMSDFragStatus);
        TextView txtInstructions = (TextView) getActivity().findViewById(R.id.lblMSDFragInstructions);
        if (Utils.isNetworkConnected(getActivity().getApplicationContext())) {
            if (numberOfPastUnsentMeetings > 0) {
                populateMeetingsList();
                //txtStatus.setText("The data network is available.");
                txtInstructions.setText((Html.fromHtml(getString(R.string.data_netork_is_available_1))));
            } else {
                //txtStatus.setText("The data network is available.");
                txtInstructions.setText((Html.fromHtml(getString(R.string.data_netork_is_available_2))));
            }
        } else {
            // txtStatus.setText("The data network is not available.");
            txtInstructions.setText((Html.fromHtml(getString(R.string.data_netork_is_available_3))));
        }
    }

    private void loadSherlockMenu(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.mnuMSDFSend);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MeetingActivity meetingActivity = (MeetingActivity) getActivity();
                meetingActivity.sendMeetingData(selectedMeetingId);
                return true;
            }
        });
        if(Utils.isNetworkConnected(getActivity().getApplicationContext())) {
            int meetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);
            MeetingRepo meetingRepo = new MeetingRepo(getActivity().getApplication(), meetingId);
            boolean isDataSent = meetingRepo.isMeetingSent();
            if(isDataSent){
                menuItem.setVisible(false);
            }else{
                menuItem.setVisible(true);
            }
        }else{
            menuItem.setVisible(false);
        }
    }



    /*counts the number of past unset meetings, and computes current meeting saved values */
    void loadFragmentInformation(int meetingIdToLoad) {

        selectedMeetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);

        //get the current meeting by id
       selectedMeeting = parentActivity.ledgerLinkApplication.getMeetingRepo().getMeetingById(selectedMeetingId);


        viewingCurrentMeeting = selectedMeeting.getMeetingId() == currentMeetingId;

        ArrayList<Meeting> activeMeetings = parentActivity.ledgerLinkApplication.getMeetingRepo().getAllMeetingsByActiveStatus();

        //unsentInactiveMeetings = meetingRepo.getAllMeetingsByDataSentStatus(false);
        //Past unsent and inactive
        unsentInactiveMeetings = parentActivity.ledgerLinkApplication.getMeetingRepo().getAllMeetingsByDataSentStatusAndActiveStatus();
       numberOfPastUnsentMeetings = unsentInactiveMeetings.size();

        Log.i(getString(R.string.unset_meeting_count), "" + numberOfPastUnsentMeetings);

        //Get the current meeting
        //selectedMeeting = meetingRepo.getMeetingById(meetingIdToLoad);

        //Get total savings in current meeting
        totalSavingsInSelectedMeeting = parentActivity.ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInMeeting(selectedMeetingId);

        totalLoansRepaidInSelectedMeeting = parentActivity.ledgerLinkApplication.getMeetingLoanRepaymentRepo().getTotalLoansRepaidInMeeting(selectedMeetingId);


        //Get total fines in meeting
        totalFinesInSelectedMeeting = parentActivity.ledgerLinkApplication.getMeetingFineRepo().getTotalFinesPaidInThisMeeting(selectedMeetingId);
        
        //Get total loans in current meeting
        totalLoansIssuedInSelectedMeeting = parentActivity.ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInMeeting(selectedMeetingId);

        totalWelfareInSelectedMeeting = parentActivity.ledgerLinkApplication.getMeetingWelfareRepo().getTotalWelfareInMeeting(selectedMeetingId);
        
        //Get attendance in current meeting
        selectedMeetingAttendance = parentActivity.ledgerLinkApplication.getMeetingAttendanceRepo().getAttendanceCountByMeetingId(selectedMeetingId);

        //Get count of all members
        numberOfMembers = parentActivity.ledgerLinkApplication.getMemberRepo().countMembers();

        if(numberOfPastUnsentMeetings > 0) {
            populateMeetingsList();
        }



    }


    //Populate Meetings List
    protected void populateMeetingsList() {
        //Now get the data via the adapter
        ConcurrentMeetingsArrayAdapter adapter = new ConcurrentMeetingsArrayAdapter(getActivity().getBaseContext(), unsentInactiveMeetings);


        // listening to single list item on click
        ListView membersListView = (ListView) getActivity().findViewById(R.id.lstMSDPastMeetingList);

        membersListView.setAdapter(adapter);
        membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Meeting meeting = unsentInactiveMeetings.get(position);
                selectedMeetingId = meeting.getMeetingId();
                getActivity().getIntent().putExtra("_meetingId", selectedMeetingId);
                //make the view read only
                getActivity().getIntent().putExtra("_viewOnly", true);
                getActivity().getIntent().putExtra("_meetingDate", Utils.formatDate(meeting.getMeetingDate(), "dd MMM yyyy"));
                //viewingCurrentMeeting = false;

                //Load this meeting details
                refreshFragmentView();

            }
        });
        Utils.setListViewHeightBasedOnChildren(membersListView);
    }

    //Populates the summary for the current meeting
    public void populateSelectedMeetingSummary() {

        //TODO: We are relying on tab indexes to select the tabs, this may break when the orders are changed we may consider finding a way of selecting by tab text
        parentActivity.getIntent().putExtra("_meetingDate", Utils.formatDate(selectedMeeting.getMeetingDate(), "dd MMM yyyy"));
        parentActivity.getIntent().putExtra("_meetingId",selectedMeeting.getMeetingId());
        //parentActivity.getIntent().putExtra("_viewOnly",true);

        TextView lblMSDFragRollcall = (TextView) getActivity().findViewById(R.id.lblMSDFragRollcall);
        lblMSDFragRollcall.setText(String.format("Roll Call %d/%d", selectedMeetingAttendance, numberOfMembers));
        //Onclick load roll call viewing current meeting
        lblMSDFragRollcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(0));
            }
        });

        TextView lblMSDFragSavings = (TextView) getActivity().findViewById(R.id.lblMSDFragSavings);
        lblMSDFragSavings.setText(String.format("Savings %,.0f UGX", totalSavingsInSelectedMeeting));
        lblMSDFragSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(3));
            }
        });

        TextView lblMSDFragLoanPayments = (TextView) getActivity().findViewById(R.id.lblMSDFragLoanPayments);
        lblMSDFragLoanPayments.setText(String.format("Loan Payments %,.0f UGX", totalLoansRepaidInSelectedMeeting));
        lblMSDFragLoanPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(5));
            }
        });

        TextView lblMSDFragFines = (TextView) getActivity().findViewById(R.id.lblMSDFragFines);
        lblMSDFragFines.setText(String.format("Fines %,.0f UGX", totalFinesInSelectedMeeting));
        lblMSDFragFines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(6));
            }
        });

        TextView lblMSDFragNewLoans = (TextView) getActivity().findViewById(R.id.lblMSDFragNewLoans);
        lblMSDFragNewLoans.setText(String.format("New Loans %,.0f UGX", totalLoansIssuedInSelectedMeeting));
        lblMSDFragNewLoans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(7));
            }
        });

        TextView lblMSDFragWelfare = (TextView) getActivity().findViewById(R.id.lblMSDFragWelfare);
        lblMSDFragWelfare.setText(String.format("Welfare %,.0f UGX", totalWelfareInSelectedMeeting));
        lblMSDFragWelfare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(4));
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        getActivity().getMenuInflater().inflate(R.menu.meeting_send_data, menu);
        this.loadSherlockMenu(menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    //@Override
    //public void onCreateOptionsMenu((Menu menu), com.actionbarsherlock.view.MenuInflater inflater) {

       // menu.clear();
        //getActivity()().getMenuInflater().inflate(R.menu.meeting_send_data, menu);
        /*if(isNetworkConnected(getActivity()().getApplicationContext())) {
            getActivity()().getMenuInflater().inflate(R.menu.meeting_send_data, menu);
        } */
        //super.onCreateOptionsMenu(menu, inflater);
        //MENU = menu;
    //}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                return false;
        /**    case R.id.mnuSMDSend:
                return false;
            case R.id.mnuSMDCancel:
                return false; */
            case R.id.mnuMCBFSave:
                return false;
            case R.id.mnuMSDFSend:
                return false;
            default:
                return false;
        }
    }
}
