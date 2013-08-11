package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.MembersRollCallArrayAdapter;
import org.applab.digitizingdata.helpers.MembersSavingsArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingSavingsFrag extends SherlockFragment {
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
        return (RelativeLayout)inflater.inflate(R.layout.frag_meeting_savings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = getSherlockActivity().getSupportActionBar();
        String title = "Meeting";
        switch(Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                title="Meeting";
                break;
        }
        actionBar.setTitle(title);

        TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMSavFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        //Populate the Members
        populateMembersList();
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getSherlockActivity().getApplicationContext());
        members = memberRepo.getAllMembers();

        //Now get the data via the adapter
        MembersSavingsArrayAdapter adapter = new MembersSavingsArrayAdapter(getSherlockActivity().getBaseContext(), members);
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a SherlockListFragment to work
        //setListAdapter(adapter);
        ListView lvwMembers = (ListView)getSherlockActivity().findViewById(R.id.lvwMSavFMembers);
        TextView txtEmpty = (TextView)getSherlockActivity().findViewById(R.id.txtMSavFEmpty);

        lvwMembers.setEmptyView(txtEmpty);
        lvwMembers.setAdapter(adapter);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Member selectedMember = (Member) members.get(position);
                Intent i = new Intent(view.getContext(), MemberSavingHistoryActivity.class);

                // Pass on data
                i.putExtra("_meetingDate",meetingDate);
                i.putExtra("_memberId", selectedMember.getMemberId());
                i.putExtra("_names", selectedMember.toString());
                i.putExtra("_meetingId",meetingId);

                startActivity(i);

            }
        });
    }
}