package org.applab.digitizingdata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import org.applab.digitizingdata.R;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Created by Moses on 6/27/13.
 */
public class MeetingActivity extends SherlockFragmentActivity implements ActionBar.TabListener{
    public static Context appContext;
    private ActionBar actionBar;
    boolean enableSendData = false;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        appContext = getApplicationContext();

        //ActionBar
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Meeting");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setTag("summary").setText("Summary").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("rollCall").setText("Roll Call").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("savings").setText("Savings").setTabListener(this));
        actionBar.addTab( actionBar.newTab().setTag("loansRepaid").setText("Loans Repaid").setTabListener(this));
        actionBar.addTab( actionBar.newTab().setTag("loansIssued").setText("New Loans").setTabListener(this));

        if(getIntent().hasExtra("_tabToSelect")) {
            String tabTag = getIntent().getStringExtra("_tabToSelect");
            if(tabTag.equalsIgnoreCase("savings")) {
                actionBar.selectTab(actionBar.getTabAt(2));
            }
            else if(tabTag.equalsIgnoreCase("loansRepaid")) {
                actionBar.selectTab(actionBar.getTabAt(3));
            }
            else if(tabTag.equalsIgnoreCase("loansIssued")) {
                actionBar.selectTab(actionBar.getTabAt(4));
            }
            else if(tabTag.equalsIgnoreCase("rollCall")) {
                actionBar.selectTab(actionBar.getTabAt(1));
            }
        }

        if(getIntent().hasExtra("_enableSendData")) {
            enableSendData = getIntent().getBooleanExtra("_enableSendData", false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(enableSendData) {
            final MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.meeting, menu);
            return true;
        }
        else {
            return false;
        }
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
            case R.id.mnuSMDSend:
                Toast.makeText(getBaseContext(), "Meeting Data has been Sent", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case R.id.mnuSMDCancel:
                //Toast.makeText(getBaseContext(), "You have successfully started a new cycle", Toast.LENGTH_LONG).show();
                i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        String selectedTag = (String)tab.getTag();
        SherlockFragment fragment;

        if(selectedTag.equalsIgnoreCase("summary")) {
            fragment = new MeetingSummaryFrag();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        }
        else if(selectedTag.equalsIgnoreCase("rollCall")) {
            fragment = new MeetingRollCallFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        else if(selectedTag.equalsIgnoreCase("savings")) {
            fragment = new MeetingSavingsFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        else if(selectedTag.equalsIgnoreCase("loansRepaid")) {
            fragment = new MeetingLoansRepaidFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        else if(selectedTag.equalsIgnoreCase("loansIssued")) {
            fragment = new MeetingLoansIssuedFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        else {
            //Not Sure what to do
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


}
