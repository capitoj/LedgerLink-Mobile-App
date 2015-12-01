package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.MembersFinesArrayAdapter;

import java.util.ArrayList;

public class FineMemberMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;
    String tabToSelect;
    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
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

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });


        actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
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
        members = ledgerLinkApplication.getMemberRepo().getAllMembers();

        // Now get the data via the adapter
        MembersFinesArrayAdapter adapter = new MembersFinesArrayAdapter(getBaseContext(), members);
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
                    //finish this list so that it doesnt show up after fining
                    finish();
                }
            }
        });
    }

}
