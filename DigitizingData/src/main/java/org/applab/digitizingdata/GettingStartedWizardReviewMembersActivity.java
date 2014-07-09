package org.applab.digitizingdata;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.helpers.GettingStartedWizardMembersArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.*;

import java.util.ArrayList;
import java.util.Date;




/**
 * Created by Moses on 7/16/13.
 */
public class GettingStartedWizardReviewMembersActivity extends MembersListActivity {
    private ActionBar actionBar;
    private ArrayList<Member> members;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        inflateCustombar();
        setContentView(R.layout.activity_getting_started_wizard_review_members);

        TypefaceTextView reviewSubHeading = (TypefaceTextView) findViewById(R.id.lblRvwMembersSubHeading);
        SpannableStringBuilder reviewSubHeadingPart = new SpannableStringBuilder("Review and confirm that all information is correct. Press the member’s name to correct an entry. If you wish to review it later, you may ");
        SpannableString exitText = new SpannableString("exit ");
        exitText.setSpan(new StyleSpan(Typeface.BOLD), 0, exitText.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        reviewSubHeadingPart.append(exitText);
        reviewSubHeadingPart.append("and come back later.");

        reviewSubHeading.setText(reviewSubHeadingPart);


        //Load the summary information
        MeetingRepo meetingRepo = new MeetingRepo(getBaseContext());
        Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();



        //Set cycle start date in label
        Date startDate = dummyGettingStartedWizardMeeting.getMeetingDate();
        TextView lblRvwMembersDate = (TextView) findViewById(R.id.lblRvwMembersDate);
        lblRvwMembersDate.setText("As of "+Utils.formatDate(startDate));

        //Set savings in GSW meeting
        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(getBaseContext());
        TextView lblRvwMembersTotalSavings = (TextView) findViewById(R.id.lblRvwMembersTotalSavings);
        lblRvwMembersTotalSavings.setText(String.format("Total savings this cycle %,.0f %s",
                                            meetingSavingRepo.getTotalSavingsInMeeting(dummyGettingStartedWizardMeeting.getMeetingId()),
                                            getResources().getString(R.string.operating_currency)));

        //Set loans issued in GSW meeting
        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(getBaseContext());
        TextView lblRvwMembersTotalLoan = (TextView) findViewById(R.id.lblRvwMembersTotalLoan);
        lblRvwMembersTotalLoan.setText(String.format("Total loans outstanding %,.0f %s",
                meetingLoanIssuedRepo.getTotalLoansIssuedInMeeting(dummyGettingStartedWizardMeeting.getMeetingId()),
                getResources().getString(R.string.operating_currency)));





        //Populate the Members
        populateMembersList();
        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_REVIEW_MEMBERS);


    }

    /* inflates custom menu bar for review members */
    public void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_exit_enternext_next, null);
        customActionBarView.findViewById(R.id.actionbar_exit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                        //startActivity(i);
                        finish();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
        );


        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), GettingsStartedWizardNewCycleActivity.class);
                        i.putExtra("_isUpdateCycleAction", true);
                        i.putExtra("_isFromReviewMembers", true);
                        startActivity(i);
                        finish();
                    }
                }
        );

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("GET STARTED");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, GettingStartedWizardAddMemberActivity.class);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            case R.id.mnuMListDone:
                //Go to GSW cycle review page
                //which is infact the new cycle activity in update mode
                Intent i = new Intent(getApplicationContext(), GettingsStartedWizardNewCycleActivity.class);
                i.putExtra("_isUpdateCycleAction", true);
                startActivity(i);
                return true;
            case R.id.mnuMListAdd:
                i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    //Populate Members List
    @Override
    protected void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getApplicationContext());
        members = memberRepo.getAllMembers();
        if (members == null) {
            members = new ArrayList<Member>();
        }
        //Now get the data via the adapter
        final GettingStartedWizardMembersArrayAdapter adapter = new GettingStartedWizardMembersArrayAdapter(getBaseContext(), members);
        Log.d(getBaseContext().getPackageName(), members.size() + " members loaded");
        //Assign Adapter to ListView
        //Assign Adapter to ListView
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                setListAdapter(adapter);
                Utils.setListViewHeightBasedOnChildren(getListView());
                getListView().setDivider(null);
            }
        });

        // listening to single list item on click
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Launching new Activity on selecting single List Item
                Member selectedMember = members.get(position);
                Intent viewMember = new Intent(view.getContext(), GettingStartedWizardAddMemberActivity.class);
                // Pass on data
                Bundle b = new Bundle();
                b.putInt("_id", selectedMember.getMemberId());
                b.putString("_names", selectedMember.getFullName());
                viewMember.putExtras(b);
                viewMember.putExtra("_caller", "reviewMembers");
                viewMember.putExtra("_isEditAction", true);
                startActivity(viewMember);
                finish();

            }
        });

    }


}