package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.AdapterView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.LongTaskRunner;
import org.applab.digitizingdata.helpers.MembersArrayAdapter;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/16/13.
 */
public class MembersListActivity extends SherlockListActivity {
    private ActionBar actionBar;
    private ArrayList<Member> members;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_members_list);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Edit Members");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

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
        LongTaskRunner.runLongTask(populateMembers, "Please wait...", "Loading member list...", MembersListActivity.this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
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
                startActivity(i);
                return true;
        }
        return true;
    }

    //Populate Members List
    protected void populateMembersList() {
        //Load the Main Menu
        MemberRepo memberRepo = new MemberRepo(getApplicationContext());
        members = memberRepo.getAllMembers();

        if(members == null) {
            members = new ArrayList<Member>();
        }

        //Now get the data via the adapter
        final MembersArrayAdapter adapter = new MembersArrayAdapter(getBaseContext(), members, "fonts/roboto-regular.ttf");

        //Assign Adapter to ListView
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setListAdapter(adapter);
            }
        });


        // listening to single list item on click
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Launching new Activity on selecting single List Item
                Member selectedMember = members.get(position);
                Intent viewMember = new Intent(view.getContext(), MemberDetailsViewActivity.class);

                // Pass on data
                Bundle b = new Bundle();
                b.putInt("_id", selectedMember.getMemberId());
                b.putString("_names", selectedMember.getFullName());
                viewMember.putExtras(b);
                viewMember.putExtra("_caller","reviewMembers");
                viewMember.putExtra("_isEditAction",true);
                startActivity(viewMember);

            }
        });
    }
}