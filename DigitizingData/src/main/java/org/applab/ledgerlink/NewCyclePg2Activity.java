package org.applab.ledgerlink;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.MembersCustomArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.LongTaskRunner;

import java.util.ArrayList;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

/**
 * Created by Moses on 6/28/13.
 */
public class NewCyclePg2Activity extends ListActivity {
    ActionBar actionBar;
    ArrayList<Member> members = null;
    boolean isUpdateCycleAction = false;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_new_cycle_pg2);
        inflateCustombar();

        if(getIntent().hasExtra("_isUpdateCycleAction")) {
            isUpdateCycleAction = getIntent().getBooleanExtra("_isUpdateCycleAction",false);
        }

        if(isUpdateCycleAction) {
            actionBar.setTitle(R.string.edit_cycle);
        }
        else {
            actionBar.setTitle(R.string.new_cycle);
        }

        //Populate the Members
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(runnable, getString(R.string.please_wait), getString(R.string.loading_member_list), NewCyclePg2Activity.this );

    }

    /* inflates custom menu bar for review members */
    public void inflateCustombar() {

        Intent i = new Intent(getApplicationContext(), MainActivity.class);

        final LayoutInflater inflater = (LayoutInflater) ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_exit_enternext_done, null);
        customActionBarView.findViewById(R.id.actionbar_exit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                        //startActivity(i);
                        finish();
                        System.exit(0);
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isUpdateCycleAction) {
                            Utils._membersAccessedFromEditCycle = true;
                        }
                        else {
                            Utils._membersAccessedFromNewCycle = true;
                        }
                        Intent i = new Intent(getApplicationContext(), AddMemberActivity.class);
                        startActivity(i);
                    }
                }
        );


        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isUpdateCycleAction) {
                            Toast toast = Toast.makeText(getBaseContext(), R.string.successfully_edited_cycle, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.LEFT,0,0);
                            toast.show();
                        }
                        else {
                            Toast toast = Toast.makeText(getBaseContext(), R.string.successfully_started_new_cycle, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.LEFT,0,0);
                            toast.show();
                        }

                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                }
        );

        actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.new_cyccle_allcaps);
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
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuNCPg2Done:

                if(isUpdateCycleAction) {
                    Toast toast = Toast.makeText(getBaseContext(), R.string.successfully_edited_cycle, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.LEFT,0,0);
                    toast.show();
                }
                else {
                    Toast toast = Toast.makeText(getBaseContext(), R.string.successfully_started_new_cycle, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.LEFT,0,0);
                    toast.show();
                }

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case R.id.mnuNCPg2Add:
                //Toast.makeText(getBaseContext(), "You have successfully started a new cycle", Toast.LENGTH_LONG).show();
                if(isUpdateCycleAction) {
                    Utils._membersAccessedFromEditCycle = true;
                }
                else {
                    Utils._membersAccessedFromNewCycle = true;
                }
                i = new Intent(getApplicationContext(), AddMemberActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
         members = ledgerLinkApplication.getMemberRepo().getAllMembers();

        if(members == null) {
            members = new ArrayList<Member>();
        }

        // Get the data via the adapter; Pass on font type as well - Hard coded for now
        final MembersCustomArrayAdapter adapter = new MembersCustomArrayAdapter(getBaseContext(), members);

        //Assign Adapter to ListView
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                setListAdapter(adapter);
            }
        };
        runOnUiThread(runnable);


        // listening to single list item on click
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Launching new Activity on selecting single List Item
                Member selectedMember = (Member) members.get(position);
                Intent viewMember = new Intent(view.getContext(), MemberDetailsViewActivity.class);

                // Pass on data
                Bundle b = new Bundle();
                b.putInt("_id", selectedMember.getMemberId());
                b.putString("_names", selectedMember.getFullName());

                viewMember.putExtras(b);
                viewMember.putExtra("_caller","newCyclePg2");

                startActivity(viewMember);
                //Toast.makeText(view.getContext(), selectedMember.toString() + " is " + ((selectedMember.isActive()) ? "Active" : "Not Active"), Toast.LENGTH_LONG).show();
            }
        });
    }
}