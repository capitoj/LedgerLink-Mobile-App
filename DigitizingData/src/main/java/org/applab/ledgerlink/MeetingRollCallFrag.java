package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.MembersRollCallArrayAdapter;
import org.applab.ledgerlink.repo.MeetingAttendanceRepo;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingRollCallFrag extends Fragment {
    ActionBar actionBar;
    ArrayList<Member> members = null;
    int meetingId;
    Meeting selectedMeeting;
    private MeetingActivity parentActivity;
    ScrollView fragmentView;
    protected Context context;

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
        fragmentView = (ScrollView) inflater.inflate(R.layout.frag_meeting_rollcall, container, false);
        reloadFragmentInfo();
        context = getActivity().getApplicationContext();

        return fragmentView;
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

    }

    private void reloadFragmentInfo() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        actionBar = parentActivity.getSupportActionBar();
        meetingId = parentActivity.getIntent().getIntExtra("_meetingId", 0);
        //get date from meeting repo via id
        String title = "Meeting";
        if (meetingId != 0) {
            selectedMeeting = parentActivity.ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);
            //title = String.format("Meeting    %s", Utils.formatDate(selectedMeeting.getMeetingDate(), getString(R.string.date_format)));
            title = getString(R.string.meeting);

        }
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
        actionBar.setSubtitle(Utils.formatDate(selectedMeeting.getMeetingDate(), getString(R.string.date_format)));
        //TextView lblMeetingDate = (TextView)parentActivity.findViewById(R.id.lblMRCFMeetingDate);
        //meetingDate = parentActivity.getIntent().getStringExtra("_meetingDate");
        //TODO: Get the Meeting Id from meetingRepo.getCurrentMeeting();
        //Wrap this long task in a runnable and run it asynchronously so as to prevent app freezes
        Runnable loaderRunnable = new Runnable() {
            @Override
            public void run() {
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(loaderRunnable, getString(R.string.please_wait), getString(R.string.loading_member_list), parentActivity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    // Populate Members List
    private void populateMembersList() {

        // Load the Main Menu
        members = parentActivity.ledgerLinkApplication.getMemberRepo().getActiveMembers(selectedMeeting.getMeetingDate());

        // Now get the data via the adapter
        final MembersRollCallArrayAdapter adapter = new MembersRollCallArrayAdapter(parentActivity.getBaseContext(), members);

        // Set whether this adapter is view only, to disable changing roll call in view only mode
        adapter.viewOnly = parentActivity.isViewOnly();

        // Pass on the meeting Id to the adapter
        adapter.setMeetingId(meetingId);
        final ListView lvwMembers = (ListView) fragmentView.findViewById(R.id.lvwMRCFMembers);
        final TextView txtEmpty = (TextView) fragmentView.findViewById(R.id.lvwMRCFEmpty);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
                Utils.setListViewHeightBasedOnChildren(lvwMembers);
            }
        };
        parentActivity.runOnUiThread(r);


        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //Do not invoke the event when in Read only Mode
                if (parentActivity.isViewOnly()) {
                    Toast.makeText(parentActivity.getBaseContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return;
                }
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    CheckBox chkAttendance = (CheckBox) view.findViewById(R.id.chkRMRCallAttendance);
                    chkAttendance.toggle();
                }

            }
        });
        lvwMembers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                //Do not invoke the event when in Read only Mode
                if (parentActivity.isViewOnly()) {
                    Toast.makeText(parentActivity.getBaseContext(), R.string.values_for_this_meeting_cannot_modify, Toast.LENGTH_LONG).show();
                    return true;
                }
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = (Member) parent.getItemAtPosition(position);
                    //Member selectedMember = members.get(position);
                    Intent i = new Intent(view.getContext(), MemberAttendanceHistoryActivity.class);
                    // Pass on data
                    if (selectedMeeting != null)
                        i.putExtra("_meetingDate", selectedMeeting.getMeetingDate());
                    i.putExtra("_memberId", selectedMember.getMemberId());
                    i.putExtra("_name", selectedMember.toString());
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                    parentActivity.finish();
                }
                return true;
            }
        });

    }
}