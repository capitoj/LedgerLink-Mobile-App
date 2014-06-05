package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.helpers.MembersLoansIssuedArrayAdapter;
import org.applab.digitizingdata.helpers.MembersSavingsArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingLoansIssuedFrag extends SherlockFragment {
    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;

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
        return inflater.inflate(R.layout.frag_meeting_loans_issued, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = getSherlockActivity().getSupportActionBar();
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = String.format("Meeting    %s", meetingDate);

        switch(Utils._meetingDataViewMode) {
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

        /**TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate); */

        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        //Populate the Cash Available
        //populateCashBookFields();

        //Populate the Members
        populateMembersList();


    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getSherlockActivity().getApplicationContext());
        members = memberRepo.getAllMembers();

        //Now get the data via the adapter
        MembersLoansIssuedArrayAdapter adapter = new MembersLoansIssuedArrayAdapter(getSherlockActivity().getBaseContext(), members, "fonts/roboto-regular.ttf");
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a SherlockListFragment to work
        //setListAdapter(adapter);
        ListView lvwMembers = (ListView)getSherlockActivity().findViewById(R.id.lvwMLIssuedFMembers);
        TextView txtEmpty = (TextView)getSherlockActivity().findViewById(R.id.txtMLIssuedFEmpty);

        lvwMembers.setEmptyView(txtEmpty);
        lvwMembers.setAdapter(adapter);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //Do not invoke the event when in Read only Mode
                if(Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Intent viewHistory = new Intent(view.getContext(), MemberLoansIssuedHistoryActivity.class);

                    // Pass on data
                    viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                    viewHistory.putExtra("_names", selectedMember.toString());
                    viewHistory.putExtra("_meetingDate",meetingDate);
                    viewHistory.putExtra("_meetingId", meetingId);

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

    /*
    private void populateCashBookFields() {
        MeetingRepo meetingRepo = null;
        MeetingSavingRepo savingRepo = null;
        MeetingLoanRepaymentRepo repaymentRepo = null;

        try {
            meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
            repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());


            TextView lblTotalCash = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFTotalCash);
            TextView lblOpeningCash = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFOpeningCash);
            TextView lblTotalSavings = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFSavings);
            TextView lblTotalLoansRepaid = (TextView)getSherlockActivity().findViewById(R.id.lblMLIssuedFLoansRepaid);

            double openingCash = meetingRepo.getMeetingTotalExpectedStartingCash(meetingId);
            double totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            double totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);

            double totalCash = openingCash + totalSavings + totalLoansRepaid;

            lblTotalCash.setText(String.format("Total Cash: %,.0fUGX", totalCash));
            lblOpeningCash.setText(String.format("Starting Cash: %,.0fUGX", openingCash));
            lblTotalSavings.setText(String.format("Savings: %,.0fUGX", totalSavings));
            lblTotalLoansRepaid.setText(String.format("Loans Repaid: %,.0fUGX", totalLoansRepaid));

        }
        catch (Exception ex) {

        }
        finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }
    */
}