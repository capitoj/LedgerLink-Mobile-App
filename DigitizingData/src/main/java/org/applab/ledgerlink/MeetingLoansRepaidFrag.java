package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.MembersLoansRepaidArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingLoansRepaidFrag extends Fragment {
    private ArrayList<Member> members;
    private String meetingDate;
    private int meetingId;
    private MeetingActivity parentActivity;
    private RelativeLayout fragmentView;
    protected List<MeetingLoanIssued> loansIssued;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getActivity();
        setHasOptionsMenu(true);
    }

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
        Log.d("MLRF", "HERE");
        fragmentView = (RelativeLayout) inflater.inflate(R.layout.frag_meeting_loans_repaid, container, false);
        Log.d("MLRF", "HERE1");
        initializeFragment();
        Log.d("MLRF", "HERE2");
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        this.initializeFragment();
    }

    private void initializeFragment() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        meetingDate = getActivity().getIntent().getStringExtra("_meetingDate");
        String title = getString(R.string.meeting);
        switch (Utils._meetingDataViewMode) {
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
        actionBar.setSubtitle(meetingDate);

        meetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);
        //Wrap and run long task
        Runnable populatorRunnable = new Runnable() {
            @Override
            public void run() {

                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(populatorRunnable, getString(R.string.please_wait), getString(R.string.loading_list_of_loans_repaid), parentActivity);
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        members = parentActivity.ledgerLinkApplication.getMemberRepo().getActiveMembers();

        //Now get the data via the adapter
        final MembersLoansRepaidArrayAdapter adapter = new MembersLoansRepaidArrayAdapter(parentActivity.getBaseContext(), members);
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a ListFragment to work
        //setListAdapter(adapter);
        final ListView lvwMembers = (ListView) fragmentView.findViewById(R.id.lvwMLRepayFMembers);
        final TextView txtEmpty = (TextView) fragmentView.findViewById(R.id.txtMLRepayFEmpty);
        Runnable runOnUiRunnable = new Runnable() {
            @Override
            public void run() {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        };

        parentActivity.runOnUiThread(runOnUiRunnable);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //Do not invoke the event when in Read only Mode but not in sent data mode
                if (parentActivity.isViewOnly() && !parentActivity.isViewingSentData()) {
                    Toast.makeText(parentActivity.getBaseContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return;
                }
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Meeting selectedMeeting = new MeetingRepo(parentActivity.getApplicationContext(), meetingId).getMeeting();
                    new MeetingLoanIssuedRepo(parentActivity.getApplicationContext()).getMemberLoanId(selectedMember.getMemberId());
                    MeetingLoanIssued recentLoan = new MeetingLoanIssuedRepo(parentActivity.getApplicationContext()).getOutstandingLoanByMemberInCycle(selectedMeeting.getVslaCycle().getCycleId(), selectedMember.getMemberId());
                    if(recentLoan != null){
                        if(recentLoan.getLoanBalance() > 0) {
                            Intent viewHistory = new Intent(view.getContext(), MemberLoansRepaidHistoryActivity.class);
                            viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                            viewHistory.putExtra("_names", selectedMember.getFullName());
                            viewHistory.putExtra("_meetingDate", meetingDate);
                            viewHistory.putExtra("_meetingId", meetingId);
                            viewHistory.putExtra("_viewingSentData", parentActivity.isViewingSentData());
                            viewHistory.putExtra("_loanId", recentLoan.getLoanId());
                            startActivity(viewHistory);
                        }else{
                            Toast.makeText(parentActivity.getBaseContext(), selectedMember.getFullName() + " does not have an outstanding loan", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(parentActivity.getBaseContext(), "Ledger Link encountered an internal error. Please contact the systems admin", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }
}