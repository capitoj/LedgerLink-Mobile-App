package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.FineHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberFineRecord;
import org.applab.digitizingdata.helpers.MembersFinesArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

public class FineMemberMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;
    String tabToSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_meeting_fine_member);

        meetingDate = getIntent().getStringExtra("_meetingDate");

        meetingId = getIntent().getIntExtra("_meetingId", 0);

        tabToSelect = getIntent().getStringExtra("_tabToSelect");
        inflateCustomActionBar();


        /** TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMSavFMeetingDate);
         meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */

       /** TextView lblReviewFinesMessage = (TextView) findViewById(R.id.lblReviewFinesMessage);
        lblReviewFinesMessage.setText("Select the member below to add a fine.");

        TextView lblFineMember = (TextView) findViewById(R.id.lblFineMember);
        lblFineMember.isShown(); */

        //Populate the Members
        populateMembersList();
    }

    private void inflateCustomActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        //final View
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel, null);
        /** customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         finish();
                         return;
                     }
                 }); */

        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });


        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("FINE MEMBER");

        // Set to false to remove caret and disable its function; if designer decides otherwise set both to true
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    // Populate Members List
    private void populateMembersList() {
        // Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getApplicationContext());
        members = memberRepo.getAllMembers();

        // Now get the data via the adapter
        MembersFinesArrayAdapter adapter = new MembersFinesArrayAdapter(getBaseContext(), members, "fonts/roboto-regular.ttf");
        adapter.setMeetingId(meetingId);


        // Assign Adapter to ListView
        ListView lvwMembers = (ListView)findViewById(R.id.lvwMFineMembers);
        TextView txtEmpty = (TextView)findViewById(R.id.txtMFineEmpty);

        lvwMembers.setEmptyView(txtEmpty);
        lvwMembers.setAdapter(adapter);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Do not invoke the event when in Read only Mode
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = (Member) members.get(position);
                    Intent i = new Intent(view.getContext(), AddFineActivity.class);

                    // Pass on data
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_memberId", selectedMember.getMemberId());
                    i.putExtra("_name", selectedMember.getFullName());
                    i.putExtra("_meetingId", meetingId);

                    startActivity(i);
                }
            }
        });
    }

}
