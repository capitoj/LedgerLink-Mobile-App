package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
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
import org.applab.digitizingdata.helpers.GettingStartedWizardMembersArrayAdapter;
import org.applab.digitizingdata.helpers.MembersArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/16/13.
 */
public class GettingStartedWizardReviewMembersActivity extends MembersListActivity {
    private ActionBar actionBar;
    private ArrayList<Member> members;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_getting_started_wizard_review_members);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Edit Members");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Populate the Members
        populateMembersList();

        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_REVIEW_MEMBERS);

    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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

        if(members == null) {
            members = new ArrayList<Member>();
        }

        //Now get the data via the adapter
        GettingStartedWizardMembersArrayAdapter adapter = new GettingStartedWizardMembersArrayAdapter(getBaseContext(), members);

        Log.d(getBaseContext().getPackageName(), members.size() + " members loaded");

        //Assign Adapter to ListView
        setListAdapter(adapter);

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
                b.putString("_names", selectedMember.getFullNames());
                viewMember.putExtras(b);
                viewMember.putExtra("_caller","reviewMembers");
                viewMember.putExtra("_isEditAction",true);
                startActivity(viewMember);

            }
        });
    }


}