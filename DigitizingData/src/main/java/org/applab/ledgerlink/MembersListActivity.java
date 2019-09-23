package org.applab.ledgerlink;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.MembersArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Moses on 7/16/13.
 */
public class MembersListActivity extends ListActivity {
    private ArrayList<Member> members;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_members_list);

       // ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            //actionBar.setIcon(R.drawable.icon_training_mode);
        }

//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(R.string.members);
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);

        //Populate the Members
        //Run this as long running task

        Runnable populateMembers = new Runnable()
        {
            @Override
            public void run()
            {
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(populateMembers, getString(R.string.please_wait), getString(R.string.loading_member_list), MembersListActivity.this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.members_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            /** case R.id.mnuMListDone:
             Intent i = new Intent(getApplicationContext(), MainActivity.class);
             startActivity(i);
             return true; */
            case R.id.mnuMListAdd:
                Intent i = new Intent(getApplicationContext(), AddMemberActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
        }
        return true;
    }

    //Populate Members List
    void populateMembersList() {
        //Load the Main Menu
        members = ledgerLinkApplication.getMemberRepo().getAllMembers();

        if(members == null) {
            members = new ArrayList<Member>();
        }

        //Now get the data via the adapter
        final MembersArrayAdapter adapter = new MembersArrayAdapter(getBaseContext(), members);

        //Assign Adapter to ListView
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {setListAdapter(adapter);
            }
        });


        // listening to single list item on click
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Launching new Activity on selecting single List Item
                Member selectedMember = members.get(position);
                Intent viewMember = new Intent(view.getContext(), MemberDetailsViewActivity.class);
                viewMember.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Pass on data
                Bundle b = new Bundle();
                b.putInt("_id", selectedMember.getMemberId());
                b.putString("_names", selectedMember.getFullName());
                viewMember.putExtras(b);
                viewMember.putExtra("_caller",getString(R.string.reviewmembers));
                viewMember.putExtra("_isEditAction",true);
                startActivity(viewMember);

            }
        });
    }
}