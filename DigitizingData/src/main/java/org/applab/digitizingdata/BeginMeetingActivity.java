package org.applab.digitizingdata;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Date;

public class BeginMeetingActivity extends SherlockActivity {
    ActionBar actionBar;
    MeetingRepo meetingRepo = null;
    ArrayList<Meeting> pastMeetings = null;
    VslaCycle recentCycle = null;
    VslaCycleRepo cycleRepo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_begin_meeting);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Meeting");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();

        //Check the past meetings that have not been sent
        cycleRepo = new VslaCycleRepo(getApplicationContext());
        meetingRepo = new MeetingRepo(getApplicationContext());

        recentCycle = cycleRepo.getMostRecentCycle();
        pastMeetings = meetingRepo.getAllMeetingsByDataSentStatus(false);

        //add LayoutParams
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.setMargins(left, top, right, bottom);

        //Get the Sections for Current Meeting and Past Meetings
        LinearLayout grpCurrentMeeting = (LinearLayout) findViewById(R.id.grpBMCurrentMeeting);
        LinearLayout grpPastMeetings = (LinearLayout) findViewById(R.id.grpBMPastMeetings);

        //Get the Instruction label to dynamically change the instructions based on unsent meetings
        TextView tvInstructionsHeader = (TextView) findViewById(R.id.lblBMHeader);
        StringBuilder sb = null;

        // if(pastMeetings.size() > 0) {
        if (null != pastMeetings) {
            if (pastMeetings.size() > 0) {

                //Setup the Instruction
                sb = new StringBuilder("Tap <b>Send</b> to send all data for all existing meetings or <b>Begin</b> to begin a new meeting.");
                sb.append("You will not be able to edit the current meeting after beginning a new meeting.");
                tvInstructionsHeader.setText(Html.fromHtml(sb.toString()));

                //Quick-fix for Determining the Most recent Meeting whose data is not sent
                Meeting mostRecentUnsentMeeting = pastMeetings.get(0);
                for (Meeting meeting : pastMeetings) {
                    if (mostRecentUnsentMeeting.getMeetingDate().compareTo(meeting.getMeetingDate()) <= 0) {
                        mostRecentUnsentMeeting = meeting;
                    }
                }

                //Display it
                TextView tvMostRecentUnsentMeeting = (TextView) findViewById(R.id.lblBMCurrentMeetingDate);
                tvMostRecentUnsentMeeting.setText(Utils.formatDate(mostRecentUnsentMeeting.getMeetingDate(), "dd MMM yyyy"));

                if (pastMeetings.size() > 1) {
                    for (Meeting meeting : pastMeetings) {
                        //Skip the Current meeting i.e. mostRecentUnsentMeeting coz it is already displayed
                        if (meeting.getMeetingId() == mostRecentUnsentMeeting.getMeetingId()) {
                            continue;
                        }
                        TextView tv = new TextView(this);
                        tv.setText(Utils.formatDate(meeting.getMeetingDate(), "dd MMM yyyy"));
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                        //tv.setTextColor(Color.parseColor("#0000FF"));
                        tv.setPadding(10, 2, 2, 2);
                        tv.setId(meeting.getMeetingId());
                        tv.setLayoutParams(params);
                        grpPastMeetings.addView(tv);
                    }

                    //If the Past Meetings are less than 4 i.e. 3 displayed in unsent past meetings  then hide the warning message
                    if (pastMeetings.size() < 4) {
                        TextView tvWarning = (TextView) findViewById(R.id.lblBMWarning);
                        grpPastMeetings.removeView(tvWarning);
                    }
                } else {
                    //Hide the Section for Past Meetings
                    LinearLayout parent = (LinearLayout) grpPastMeetings.getParent();
                    parent.removeView(grpPastMeetings);

                }
            } else {
                //Hide the Section for Past Meetings
                LinearLayout parent = (LinearLayout) grpPastMeetings.getParent();
                parent.removeView(grpPastMeetings);

                //Hide the Section for Current Meeting
                parent.removeView(grpCurrentMeeting);

                //Display the default Instructions
                sb = new StringBuilder("Tap <b>Begin</b> to begin a new meeting.");

            }
        }
        //Show the Instructions
        tvInstructionsHeader.setText(Html.fromHtml(sb.toString()));


    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.begin_meeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnuBMBegin) {
            Intent i = new Intent(getApplicationContext(), MeetingDefinitionActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
