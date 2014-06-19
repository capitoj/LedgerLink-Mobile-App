package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.LongTaskRunner;
import org.applab.digitizingdata.helpers.MembersLoansRepaidArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingLoansRepaidFrag extends SherlockFragment {
    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;
    private MeetingActivity parentActivity;

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
        return (RelativeLayout)inflater.inflate(R.layout.frag_meeting_loans_repaid, container, false);
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
                //title="Meeting";
                break;
        }
        actionBar.setTitle(title);

       /** TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMLRepayFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate); */

        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        parentActivity = (MeetingActivity) getSherlockActivity();

        //Wrap and run long task
        Runnable populatorRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(populatorRunnable, "Please wait...", "Loading list of loans repaid...", parentActivity);



    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getSherlockActivity().getApplicationContext());
        members = memberRepo.getAllMembers();

        //Now get the data via the adapter
        final MembersLoansRepaidArrayAdapter adapter = new MembersLoansRepaidArrayAdapter(getSherlockActivity().getBaseContext(), members, "fonts/roboto-regular.ttf");
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a SherlockListFragment to work
        //setListAdapter(adapter);
        final ListView lvwMembers = (ListView)getSherlockActivity().findViewById(R.id.lvwMLRepayFMembers);
        final TextView txtEmpty = (TextView)getSherlockActivity().findViewById(R.id.txtMLRepayFEmpty);

        Runnable runOnUiRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        };
        parentActivity.runOnUiThread(runOnUiRunnable);


        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //Do not invoke the event when in Read only Mode
                if(parentActivity.isViewOnly()) {
                    Toast.makeText(getSherlockActivity().getApplicationContext(), "Values for this past meeting cannot be modified at this time", Toast.LENGTH_LONG).show();
                    return;
                }
                if(Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Intent viewHistory = new Intent(view.getContext(), MemberLoansRepaidHistoryActivity.class);

                    // Pass on data
                    viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                    viewHistory.putExtra("_names", selectedMember.getFullName());
                    viewHistory.putExtra("_meetingDate",meetingDate);
                    viewHistory.putExtra("_meetingId", meetingId);

                    startActivity(viewHistory);
                }

            }
        });
    }
}