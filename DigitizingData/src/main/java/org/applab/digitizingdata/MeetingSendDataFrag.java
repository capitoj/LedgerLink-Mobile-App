package org.applab.digitizingdata;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.ConcurrentMeetingsArrayAdapter;
import org.applab.digitizingdata.helpers.MeetingsArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.*;

import java.util.ArrayList;


public class MeetingSendDataFrag extends SherlockFragment {

    private static com.actionbarsherlock.view.Menu MENU;
    ActionBar actionBar = null;
    int numberOfPastUnsentMeetings = 0;
    private Meeting selectedMeeting;
    private int numberOfMembers;
    private double totalSavingsInSelectedMeeting;
    private double totalLoansRepaidInSelectedMeeting;
    private double totalFinesInSelectedMeeting;
    private double totalLoansIssuedInSelectedMeeting;
    private int selectedMeetingAttendance;
    int currentMeetingId;
    int selectedMeetingId;
    private boolean viewingCurrentMeeting;
    private ArrayList<Meeting> unsentMeetings;
    private ArrayList<Meeting> activeMeetings;
    private MeetingActivity parentActivity;
    //public FragmentTransaction fragmentTransaction;

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

        actionBar = getSherlockActivity().getSupportActionBar();
        String title = "Meeting";

        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                title = "Meeting";
                break;
        }
        actionBar.setTitle(title);
        parentActivity = (MeetingActivity) getSherlockActivity();
        refreshFragmentView();

    }

    private void refreshFragmentView() {

        loadFragmentInformation(selectedMeetingId);

        //Set title
        actionBar.setTitle("MEETING " + Utils.formatDate(selectedMeeting.getMeetingDate(), "dd MMM yyyy"));
        Log.i("SendDataFrag", "number of unsent meetings is " + numberOfPastUnsentMeetings);

        LinearLayout layoutMSDUnsentPastMeetings = (LinearLayout) getSherlockActivity().findViewById(R.id.layoutMSDUnsentPastMeetings);

        //Hide unrequired views
        layoutMSDUnsentPastMeetings.setVisibility((numberOfPastUnsentMeetings == 0) ? View.GONE : View.VISIBLE);


        //TODO: Load current meeting(s)
        /*/could be more than one
        if(activeMeetings != null) {
            if(activeMeetings.size()>0) {
                TextView txtMSDFragCurrentMeetingDetails = (TextView) getSherlockActivity().findViewById(R.id.txtMSDFragCurrentMeetingDetails);
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
        LinearLayout layoutMSDCurrentMeetingSummary = (LinearLayout) getSherlockActivity().findViewById(R.id.layoutMSDCurrentMeetingSummary);
        //layoutMSDCurrentMeetingSummary.setVisibility(selectedMeeting.isCurrent() ? View.GONE : View.VISIBLE);
        layoutMSDCurrentMeetingSummary.setVisibility(viewingCurrentMeeting ? View.GONE : View.VISIBLE);
        //Populate summary of the "Current" meeting from the parent activity
        //Apply null check
        if (parentActivity.getCurrentMeeting() != null) {
            TextView txtMSDFragCurrentMeetingDetails = (TextView) getSherlockActivity().findViewById(R.id.txtMSDFragCurrentMeetingDetails);
            txtMSDFragCurrentMeetingDetails.setText(Utils.formatDate(parentActivity.getCurrentMeeting().getMeetingDate(), "dd MMM yyyy"));

            txtMSDFragCurrentMeetingDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //To change body of implemented methods use File | Settings | File Templates.
                    selectedMeetingId = parentActivity.getCurrentMeeting().getMeetingId();
                    getSherlockActivity().getIntent().putExtra("_meetingId", selectedMeetingId);
                    //make the view mode modifiable

                    getSherlockActivity().getIntent().putExtra("_viewOnly", false);
                    getSherlockActivity().getIntent().putExtra("_meetingDate", Utils.formatDate(parentActivity.getCurrentMeeting().getMeetingDate(), "dd MMM yyyy"));
                    getSherlockActivity().getIntent().getExtras();
                    viewingCurrentMeeting = true;
                    refreshFragmentView();
                }
            });
        } else {
            //current meeting is null, i.e no current meeting in view hide the current meeting section
            //TODO: to accomodate concurrent cycles, this section may show all current meetings for the different cycles
            layoutMSDCurrentMeetingSummary.setVisibility(View.GONE);
        }


        populateSelectedMeetingSummary();
        // TextView txtStatus = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragStatus);
        TextView txtInstructions = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragInstructions);
        if (Utils.isNetworkConnected(getSherlockActivity().getApplicationContext())) {

            if (numberOfPastUnsentMeetings > 0) {
                populateMeetingsList();

                //txtStatus.setText("The data network is available.");
                txtInstructions.setText((Html.fromHtml("The data network is available. You may review the information below then tap <b>Send</b> to send the current meeting and all past meetings.")));
                //Show button
                MENU.findItem(R.id.mnuMSDFSend).setVisible(true);
            } else {
                //txtStatus.setText("The data network is available.");
                txtInstructions.setText((Html.fromHtml("The data network is available. You may review the information below then tap <b>Send</b>  to send the current meeting.")));
                //Show button
                MENU.findItem(R.id.mnuMSDFSend).setVisible(true);
            }
        } else {
            // txtStatus.setText("The data network is not available.");
            txtInstructions.setText((Html.fromHtml("The data network is not available. Move to a place with data network to send meeting data. You can send meeting data later by selecting <b>Meeting</b> from the main menu.")));
            //Hide button
            MENU.findItem(R.id.mnuMSDFSend).setVisible(false);
        }

        //Set onclick event of send meeting button
        MENU.findItem(R.id.mnuMSDFSend).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MeetingActivity meetingActivity = (MeetingActivity) getSherlockActivity();
                meetingActivity.sendMeetingData(selectedMeetingId);
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }


    /*counts the number of past unset meetings, and computes current meeting saved values */
    public void loadFragmentInformation(int meetingIdToLoad) {

        selectedMeetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        //get the current meeting by id
        MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getBaseContext());
        selectedMeeting = meetingRepo.getMeetingById(selectedMeetingId);


        if (selectedMeeting.isCurrent()) {
            viewingCurrentMeeting = true;
        }

        activeMeetings = meetingRepo.getAllMeetingsByActiveStatus(true);

        unsentMeetings = meetingRepo.getAllMeetingsByDataSentStatus(false);
        numberOfPastUnsentMeetings = unsentMeetings.size();

        Log.i("Unset meeting count ", "" + numberOfPastUnsentMeetings);

        //Get the current meeting
        //selectedMeeting = meetingRepo.getMeetingById(meetingIdToLoad);

        //Get total savings in current meeting
        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(getSherlockActivity().getBaseContext());
        totalSavingsInSelectedMeeting = meetingSavingRepo.getTotalSavingsInMeeting(selectedMeetingId);


        MeetingLoanRepaymentRepo meetingLoanRepaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getBaseContext());
        totalLoansRepaidInSelectedMeeting = meetingLoanRepaymentRepo.getTotalLoansRepaidInMeeting(selectedMeetingId);


        //Get total fines in meeting
        MeetingFineRepo meetingFineRepo = new MeetingFineRepo(getSherlockActivity().getBaseContext());
        totalFinesInSelectedMeeting = meetingFineRepo.getTotalFinesInMeeting(selectedMeetingId);

        //Get total loans in current meeting
        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getBaseContext());
        totalLoansIssuedInSelectedMeeting = meetingLoanIssuedRepo.getTotalLoansIssuedInMeeting(selectedMeetingId);

        //Get attendance in current meeting
        MeetingAttendanceRepo meetingAttendanceRepo = new MeetingAttendanceRepo(getSherlockActivity().getBaseContext());
        selectedMeetingAttendance = meetingAttendanceRepo.getAttendanceCountByMeetingId(selectedMeetingId, 1);

        //Get count of all members
        MemberRepo memberRepo = new MemberRepo(getSherlockActivity().getBaseContext());
        numberOfMembers = memberRepo.countMembers();

        if (numberOfPastUnsentMeetings > 0) {
            populateMeetingsList();
        }


    }


    //Populate Meetings List
    protected void populateMeetingsList() {
        //Now get the data via the adapter
        ConcurrentMeetingsArrayAdapter adapter = new ConcurrentMeetingsArrayAdapter(getSherlockActivity().getBaseContext(), unsentMeetings);


        // listening to single list item on click
        ListView membersListView = (ListView) getSherlockActivity().findViewById(R.id.lstMSDPastMeetingList);

        membersListView.setAdapter(adapter);
        membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Meeting meeting = unsentMeetings.get(position);
                selectedMeetingId = meeting.getMeetingId();
                getSherlockActivity().getIntent().putExtra("_meetingId", selectedMeetingId);
                //make the view read only
                getSherlockActivity().getIntent().putExtra("_viewOnly", true);
                getSherlockActivity().getIntent().putExtra("_meetingDate", Utils.formatDate(meeting.getMeetingDate(), "dd MMM yyyy"));
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
        parentActivity.getIntent().putExtra("_meetingId", selectedMeeting.getMeetingId());
        //parentActivity.getIntent().putExtra("_viewOnly",true);

        TextView lblMSDFragRollcall = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragRollcall);
        lblMSDFragRollcall.setText(String.format("Roll Call %d/%d", selectedMeetingAttendance, numberOfMembers));
        //Onclick load roll call viewing current meeting
        lblMSDFragRollcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(0));
            }
        });

        TextView lblMSDFragSavings = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragSavings);
        lblMSDFragSavings.setText(String.format("Savings %,.0f UGX", totalSavingsInSelectedMeeting));
        lblMSDFragSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(3));
            }
        });

        TextView lblMSDFragLoanPayments = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragLoanPayments);
        lblMSDFragLoanPayments.setText(String.format("Loan Payments %,.0f UGX", totalLoansRepaidInSelectedMeeting));
        lblMSDFragLoanPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(4));
            }
        });

        TextView lblMSDFragFines = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragFines);
        lblMSDFragFines.setText(String.format("Fines %,.0f UGX", totalFinesInSelectedMeeting));
        lblMSDFragFines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(5));
            }
        });

        TextView lblMSDFragNewLoans = (TextView) getSherlockActivity().findViewById(R.id.lblMSDFragNewLoans);
        lblMSDFragNewLoans.setText(String.format("New Loans %,.0f UGX", totalLoansIssuedInSelectedMeeting));
        lblMSDFragNewLoans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.selectTab(actionBar.getTabAt(6));
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        MENU = menu;
        getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_send_data, menu);
        /*if(isNetworkConnected(getSherlockActivity().getApplicationContext())) {
            getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_send_data, menu);
        } */
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
