package org.applab.ledgerlink;

import android.app.Activity;
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

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.MembersFinesArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.adapters.BorrowFromWelfareArrayAdapter;
import org.applab.ledgerlink.repo.MeetingOutstandingWelfareRepo;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.util.ArrayList;

public class BorrowWelfareMeetingActivity extends ActionBarActivity{

    String meetingDate;
    int meetingId;
    ActionBar actionBar;
    LedgerLinkApplication ledgerLinkApplication;
    ArrayList<Member> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_borrow_welfare_meeting);

        meetingDate = getIntent().getStringExtra("_meetingDate");
        meetingId = getIntent().getIntExtra("_meetingId", 0);

        inflateCustomActionBar();

        Runnable populateRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(populateRunnable, getString(R.string.please_wait), getString(R.string.loading_outstanding_welfare_info), this);
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
        actionBar.setTitle(R.string.borrow_from_welfare_caps);

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
        members = ledgerLinkApplication.getMemberRepo().getActiveMembers();

        // Now get the data via the adapter
        final BorrowFromWelfareArrayAdapter adapter = new BorrowFromWelfareArrayAdapter(getBaseContext(), members);
        adapter.setMeetingId(meetingId);


        // Assign Adapter to ListView
        final ListView lvwMembers = (ListView)findViewById(R.id.lvwBorrowFromWelfareMembers);
        final TextView txtEmpty = (TextView)findViewById(R.id.txtMBorrowFromWelfareEmpty);

        Runnable runOnUiRunnable = new Runnable() {
            @Override
            public void run() {
                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        };

        runOnUiThread(runOnUiRunnable);

        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Do not invoke the event when in Read only Mode
                if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = members.get(position);
                    Meeting meeting = new MeetingRepo(getApplicationContext(), meetingId).getMeeting();
                    double outstandingAmount = new MeetingOutstandingWelfareRepo(getApplicationContext()).getMemberTotalWelfareOutstandingInCycle(meeting.getVslaCycle().getCycleId(), selectedMember.getMemberId());
                    if(outstandingAmount > 0){
                        Toast.makeText(getApplicationContext(), selectedMember.getFullName() + " has an outstanding welfare amount of " + outstandingAmount,Toast.LENGTH_LONG).show();
                    }else {
                        Intent i = new Intent(view.getContext(), AddBorrowFromWelfareMeetingActivity.class);

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
            }
        });
    }
}
