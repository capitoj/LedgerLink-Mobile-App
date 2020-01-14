package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.ledgerlink.business_rules.VslaMeeting;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.MembersLoansIssuedArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingLoansIssuedFrag extends Fragment {
    private ArrayList<Member> members;
    private String meetingDate;
    private int meetingId;
    private MeetingActivity parentActivity;
    private RelativeLayout fragmentView;
    private MeetingRepo meetingRepo;
    private MeetingSavingRepo savingRepo = null;
    private MeetingLoanRepaymentRepo repaymentRepo = null;
    private MeetingLoanIssuedRepo loanIssuedRepo = null;
    private MeetingFineRepo fineRepo = null;
    private Meeting currentMeeting = null;
    private double totalCashInBox = 0.0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getActivity();
        //meetingRepo = parentActivity.ledgerLinkApplication.getMeetingRepo();
        savingRepo = parentActivity.ledgerLinkApplication.getMeetingSavingRepo();
        loanIssuedRepo = parentActivity.ledgerLinkApplication.getMeetingLoanIssuedRepo();
        repaymentRepo = parentActivity.ledgerLinkApplication.getMeetingLoanRepaymentRepo();
        fineRepo = parentActivity.ledgerLinkApplication.getMeetingFineRepo();

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
        fragmentView = (RelativeLayout) inflater.inflate(R.layout.frag_meeting_loans_issued, container, false);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    public void onResume() {
        super.onResume();
        initializeFragmentView();

    }

    private void initializeFragmentView() {

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
                //  title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);


        /**TextView lblMeetingDate = (TextView)getActivity()().findViewById(R.id.lblMLIssuedFMeetingDate);
         meetingDate = getActivity()().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */
        meetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);
        meetingRepo = new MeetingRepo(getActivity().getApplicationContext(), meetingId);

        //Get the Cycle that contains this meeting
        if (null == currentMeeting) {
            currentMeeting = meetingRepo.getMeeting();
        }

        TextView lblTotalCash = (TextView) getActivity().findViewById(R.id.lblMLIssuedFTotalCash);

        populateTotalCash();
        if (null != lblTotalCash) {
            lblTotalCash.setText(String.format(getString(R.string.total_cash_inbox_x)+" %,.0f UGX", totalCashInBox));
        }


        //Populate the Cash Available
        // Populate members list
        //Wrap and run long task
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(runnable, getString(R.string.please_wait), getString(R.string.loading_list_of_loans_issued), parentActivity);
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        members = parentActivity.ledgerLinkApplication.getMemberRepo().getActiveMembers();

        //Now get the data via the adapter
        final MembersLoansIssuedArrayAdapter adapter = new MembersLoansIssuedArrayAdapter(getActivity().getBaseContext(), members);
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a ListFragment to work
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
                    Toast.makeText(getActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return;
                }
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);

//                    Intent viewHistory = new Intent(view.getContext(), MeetingMemberLoansIssueActivity.class);
//                    viewHistory.putExtra("_memberId", selectedMember.getMemberId());
//                    viewHistory.putExtra("_names", selectedMember.toString());
//                    viewHistory.putExtra("_meetingDate", meetingDate);
//                    viewHistory.putExtra("_meetingId", meetingId);
//                    viewHistory.putExtra("_totalCashInBox", totalCashInBox);
//                    viewHistory.putExtra("_action", getString(R.string.loanissue));
//                    startActivity(viewHistory);

                    MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(getContext());
                    MeetingLoanIssued meetingLoanIssued = meetingLoanIssuedRepo.getTotalOutstandingLoansByMemberInCycle(currentMeeting.getVslaCycle().getCycleId(), selectedMember.getMemberId());
                    if(meetingLoanIssued.getLoanBalance() > 0) {
                        Toast.makeText(getActivity().getApplicationContext(), selectedMember.getFullName() + " has an outstanding loan balance of " + meetingLoanIssued.getLoanBalance(), Toast.LENGTH_LONG).show();
                    }else{

                        if(selectedMember.getMemberNo() > 0) {
                            Intent viewHistory = new Intent(view.getContext(), MemberLoansIssuedHistoryActivity.class);
                            // Pass on data
                            viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                            viewHistory.putExtra("_names", selectedMember.toString());
                            viewHistory.putExtra("_meetingDate", meetingDate);
                            viewHistory.putExtra("_meetingId", meetingId);
                            viewHistory.putExtra("_totalCashInBox", totalCashInBox);
                            startActivity(viewHistory);
                        }else{
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.cannot_issue_new_loan_to) + selectedMember.getFullName() + getString(R.string.does_not_have_member_no), Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
        });

        //Hack to ensure all Items in the List View are visible
        //Utils.setListViewHeightBasedOnChildren(lvwMembers);
        // TextView lblMeetingDate = (TextView)getActivity()().findViewById(R.id.lblMLIssuedFMeetingDate);
        //lblMeetingDate.setFocusable(true);
        //lblMeetingDate.requestFocus();

    }

    private void populateTotalCash() {

        try {
            totalCashInBox = VslaMeeting.getTotalCashInBox(getActivity().getApplicationContext(), this.meetingId);

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
        }
    }

}