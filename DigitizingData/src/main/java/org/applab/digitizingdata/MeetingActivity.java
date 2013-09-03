package org.applab.digitizingdata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.helpers.Utils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
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

        String title = "Meeting";
        switch(Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setTag("rollCall").setText("Register").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("summary").setText("Summary").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("openingCash").setText("Starting Cash").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setTag("savings").setText("Savings").setTabListener(this));
        actionBar.addTab( actionBar.newTab().setTag("loansRepaid").setText("Loans Repaid").setTabListener(this));
        actionBar.addTab( actionBar.newTab().setTag("loansIssued").setText("New Loans").setTabListener(this));
        actionBar.addTab( actionBar.newTab().setTag("cashBook").setText("Cash Book").setTabListener(this));
        actionBar.addTab( actionBar.newTab().setTag("sendData").setText("Send Data").setTabListener(this));


        if(getIntent().hasExtra("_tabToSelect")) {
            String tabTag = getIntent().getStringExtra("_tabToSelect");
            if(tabTag.equalsIgnoreCase("savings")) {
                actionBar.selectTab(actionBar.getTabAt(3));
            }
            else if(tabTag.equalsIgnoreCase("loansRepaid")) {
                actionBar.selectTab(actionBar.getTabAt(4));
            }
            else if(tabTag.equalsIgnoreCase("loansIssued")) {
                actionBar.selectTab(actionBar.getTabAt(5));
            }
            else if(tabTag.equalsIgnoreCase("rollCall")) {
                actionBar.selectTab(actionBar.getTabAt(0));
            }
        }

        if(getIntent().hasExtra("_enableSendData")) {
            enableSendData = getIntent().getBooleanExtra("_enableSendData", false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //If(enableData)
        if(Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_REVIEW) {
            final MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.meeting, menu);
            return true;
        }
        else {
            return false;
        }
    }

//    @Override
//    public boolean onPrepareOptionsMenu (Menu menu) {
//        //Determine what menu to display
//        menu.clear();
//        final MenuInflater inflater = getSupportMenuInflater();
//        switch(Utils._meetingActiveActionBarMenu) {
//            case MENU_NONE:
//                break;
//            case MENU_START_CASH_TAB:
//                //getMenuInflater().inflate(R.menu.secondmenu, menu);
//                break;
//            case MENU_CASH_BOOK_TAB:
//                inflater.inflate(R.menu.meeting_cash_book, menu);
//                break;
//            case MENU_SEND_DATA_TAB:
//                break;
//            case MENU_REVIEW_SEND:
//                inflater.inflate(R.menu.meeting, menu);
//                break;
//            default:
//                break;
//        }
//        return super.onPrepareOptionsMenu(menu);
//    }

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
            case R.id.mnuMSDFSend:
                Toast.makeText(getBaseContext(), "Meeting Data has been Sent", Toast.LENGTH_LONG).show();
                i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case R.id.mnuSMDCancel:
                //Toast.makeText(getBaseContext(), "You have successfully started a new cycle", Toast.LENGTH_LONG).show();
                i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case R.id.mnuMCBFSave:
                return false;
            default:
                return false;
        }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        String selectedTag = (String)tab.getTag();
        SherlockFragment fragment;

        mActionMode = null;

        if(selectedTag.equalsIgnoreCase("summary")) {
            fragment = new MeetingSummaryFrag();
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment).commit();
        }
        else if(selectedTag.equalsIgnoreCase("rollCall")) {
            fragment = new MeetingRollCallFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);
        }
        else if(selectedTag.equalsIgnoreCase("openingCash")) {
            fragment = new MeetingOpeningCashFrag();
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
        else if(selectedTag.equalsIgnoreCase("cashBook")) {
            fragment = new MeetingCashBookFrag();
            fragmentTransaction.replace(android.R.id.content, fragment);

            //Create an Context Action Bar Menu
            //mActionMode = MeetingActivity.this.startActionMode(cashBookActionModeCallback);
        }
        else if(selectedTag.equalsIgnoreCase("sendData")) {
            fragment = new MeetingSendDataFrag();
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


    protected Object mActionMode;
    private ActionMode.Callback cashBookActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            // Assumes that you have "cash_book.xml" menu resources
            inflater.inflate(R.menu.meeting_cash_book, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.mnuMCBFSave:
                    Toast.makeText(MeetingActivity.this, "Selected menu",
                            Toast.LENGTH_LONG).show();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };
}
