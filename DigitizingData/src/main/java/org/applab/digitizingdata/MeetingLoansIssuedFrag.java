package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.LongTaskRunner;
import org.applab.digitizingdata.helpers.MembersLoansIssuedArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingLoansIssuedFrag extends SherlockFragment {
    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;
    private MeetingActivity parentActivity;
    private View fragmentView;
    MeetingRepo meetingRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingLoanRepaymentRepo repaymentRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    MeetingFineRepo fineRepo = null;
    double cashToBank = 0.0;
    Meeting currentMeeting = null;
    double totalCashInBox = 0.0;

    TextView lblTotalCash;

    MeetingStartingCash startingCashDetails = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        fragmentView = inflater.inflate(R.layout.frag_meeting_loans_issued, container, false);

        initializeFragmentView();
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lblTotalCash = (TextView) getSherlockActivity().findViewById(R.id.lblMLIssuedFTotalCash);

        populateTotalCash();
        if (null != lblTotalCash) {
            lblTotalCash.setText(String.format("Total Cash In Box %,.0f UGX", totalCashInBox));
        }

    }

    private void initializeFragmentView() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        actionBar = getSherlockActivity().getSupportActionBar();
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = "Meeting";
        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                //  title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);

        meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
        savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
        loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
        repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
        fineRepo = new MeetingFineRepo(getSherlockActivity().getApplicationContext());

        /**TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFMeetingDate);
         meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        //Get the Cycle that contains this meeting
        if (null == currentMeeting) {
            currentMeeting = meetingRepo.getMeetingById(meetingId);
        }

        //Populate the Cash Available
        parentActivity = (MeetingActivity) getSherlockActivity();

        // Populate members list
        //Wrap and run long task
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(runnable, "Please wait", "Loading list of loans issued...", parentActivity);
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getSherlockActivity().getApplicationContext());
        members = memberRepo.getAllMembers();

        //Now get the data via the adapter
        final MembersLoansIssuedArrayAdapter adapter = new MembersLoansIssuedArrayAdapter(getSherlockActivity().getBaseContext(), members, "fonts/roboto-regular.ttf");
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a SherlockListFragment to work
        //setListAdapter(adapter);
        final ListView lvwMembers = (ListView) fragmentView.findViewById(R.id.lvwMLIssuedFMembers);
        final TextView txtEmpty = (TextView) fragmentView.findViewById(R.id.txtMLIssuedFEmpty);

        Runnable runOnUi = new Runnable() {
            @Override
            public void run() {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        };
        parentActivity.runOnUiThread(runOnUi);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //Do not invoke the event when in Read only Mode
                if (parentActivity.isViewOnly()) {
                    Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return;
                }
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Intent viewHistory = new Intent(view.getContext(), MemberLoansIssuedHistoryActivity.class);

                    // Pass on data
                    viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                    viewHistory.putExtra("_names", selectedMember.toString());
                    viewHistory.putExtra("_meetingDate", meetingDate);
                    viewHistory.putExtra("_meetingId", meetingId);
                    viewHistory.putExtra("_totalCashInBox", totalCashInBox);

                    startActivity(viewHistory);
                }

            }
        });

        //Hack to ensure all Items in the List View are visible
        //Utils.setListViewHeightBasedOnChildren(lvwMembers);
        // TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFMeetingDate);
        //lblMeetingDate.setFocusable(true);
        //lblMeetingDate.requestFocus();

    }

    private void populateTotalCash() {
        double totalSavings = 0.0;
        double totalLoansRepaid = 0.0;
        double totalLoansIssued = 0.0;
        double totalFines = 0.0;

        double loanTopUps = 0.0;
        double actualStartingCash = 0.0;

        try {
            meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
            repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());

            startingCashDetails = meetingRepo.getMeetingStartingCash(meetingId);

            totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            totalFines = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);

            loanTopUps = startingCashDetails.getLoanTopUps();
            actualStartingCash = startingCashDetails.getActualStartingCash();
            cashToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(currentMeeting.getMeetingId());

            totalCashInBox = actualStartingCash + totalSavings + totalLoansRepaid - totalLoansIssued + totalFines + loanTopUps - cashToBank;
            if(meetingId == meetingRepo.getDummyGettingStartedWizardMeeting().getMeetingId()){
                totalCashInBox = startingCashDetails.getExpectedStartingCash();

            }
        } catch (Exception ex) {

        } finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }

}