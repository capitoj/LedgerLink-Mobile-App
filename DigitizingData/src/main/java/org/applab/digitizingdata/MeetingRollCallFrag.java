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
import com.actionbarsherlock.app.SherlockListFragment;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.MembersCustomArrayAdapter;
import org.applab.digitizingdata.helpers.MembersRollCallArrayAdapter;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingRollCallFrag extends SherlockFragment {
    ActionBar actionBar;
    ArrayList<Member> members = null;
    int meetingId;
    String meetingDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        return (RelativeLayout)inflater.inflate(R.layout.frag_meeting_rollcall, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle("Meeting");
        TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMRCFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        //TODO: I need to find a way of getting the Meeting Id from meetingRepo.getCurrentMeeting();
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        lblMeetingDate.setText(meetingDate);

        //Populate the Members
        populateMembersList();
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getSherlockActivity().getApplicationContext());
        members = memberRepo.getAllMembers();

        //Now get the data via the adapter
        MembersRollCallArrayAdapter adapter = new MembersRollCallArrayAdapter(getSherlockActivity().getBaseContext(), members);
        //Pass on the meeting Id to the adapter
        adapter.setMeetingId(meetingId);
        ListView lvwMembers = (ListView)getSherlockActivity().findViewById(R.id.lvwMRCFMembers);
        TextView txtEmpty = (TextView)getSherlockActivity().findViewById(R.id.lvwMRCFEmpty);

        lvwMembers.setEmptyView(txtEmpty);
        lvwMembers.setAdapter(adapter);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                CheckBox chkAttendance = (CheckBox)view.findViewById(R.id.chkRMRCallAttendance);
                chkAttendance.toggle();

    }
});
        lvwMembers.setOnItemLongClickListener (new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                Member selectedMember = (Member)parent.getItemAtPosition(position);
                //Member selectedMember = members.get(position);
                Intent i = new Intent(view.getContext(), MemberAttendanceHistoryActivity.class);

                // Pass on data
                i.putExtra("_meetingDate",meetingDate);
                i.putExtra("_memberId", selectedMember.getMemberId());
                i.putExtra("_names", selectedMember.toString());
                i.putExtra("_meetingId",meetingId);
                startActivity(i);
                return true;
            }
        });
    }
}