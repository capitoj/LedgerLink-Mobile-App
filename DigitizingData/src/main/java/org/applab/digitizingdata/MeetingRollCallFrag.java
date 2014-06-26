package org.applab.digitizingdata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.LongTaskRunner;
import org.applab.digitizingdata.helpers.MembersRollCallArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MeetingRollCallFrag extends SherlockFragment {
    ActionBar actionBar;
    ArrayList<Member> members = null;
    int meetingId;
    Meeting selectedMeeting;
    private MeetingActivity parentActivity;
    ScrollView fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        fragmentView = (ScrollView) inflater.inflate(R.layout.frag_meeting_rollcall, container, false);
        reloadFragmentInfo();

        return fragmentView;
    }


    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

    }

    private void reloadFragmentInfo() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        parentActivity = (MeetingActivity) getSherlockActivity();
        actionBar = parentActivity.getSupportActionBar();
        meetingId = parentActivity.getIntent().getIntExtra("_meetingId", 0);
        //get date from meeting repo via id
        String title = "Meeting";
        if (meetingId != 0) {
            MeetingRepo meetingRepo = new MeetingRepo(this.parentActivity.getBaseContext());
            selectedMeeting = meetingRepo.getMeetingById(meetingId);
            //title = String.format("Meeting    %s", Utils.formatDate(selectedMeeting.getMeetingDate(), "dd MMM yyyy"));
            title = "Meeting";

        }
        switch (Utils._meetingDataViewMode) {
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
        actionBar.setSubtitle(Utils.formatDate(selectedMeeting.getMeetingDate(), "dd MMM yyyy"));
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
        LongTaskRunner.runLongTask(loaderRunnable, "Please wait..", "Loading member list", parentActivity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    //Populate Members List
    private void populateMembersList() {

        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(parentActivity.getBaseContext());
        members = memberRepo.getAllMembers();

        //Now get the data via the adapter
        final MembersRollCallArrayAdapter adapter = new MembersRollCallArrayAdapter(parentActivity.getBaseContext(), members, "fonts/roboto-regular.ttf");
        //set whether this adapter is view only, to disable changing roll call in view only mode
        adapter.viewOnly = parentActivity.isViewOnly();
        //Pass on the meeting Id to the adapter
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
                    Toast.makeText(parentActivity.getBaseContext(), "Values for this past meeting cannot be modified at this time", Toast.LENGTH_LONG).show();
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
                    i.putExtra("_names", selectedMember.toString());
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                    parentActivity.finish();
                }
                return true;
            }
        });

    }
}