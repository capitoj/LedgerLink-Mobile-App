package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.FineHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberFineRecord;
import org.applab.digitizingdata.helpers.SwipeDetector;
import org.applab.digitizingdata.helpers.SwipeListView;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingRepo;

import java.util.ArrayList;


/**
 * Created by Moses on 7/7/13.
 */
public class MemberFinesHistoryActivity2 extends SherlockListActivity {
    ActionBar actionBar;
    String meetingDate;
    int memberId;
    int meetingId;
    private MeetingFineRepo fineRepo = null;
    Meeting targetMeeting = null;
    MeetingRepo meetingRepo = null;
    ArrayList<MemberFineRecord> fines;
    int targetCycleId = 0;
    boolean proceedWithSaving = false;
    boolean alertDialogShowing = false;
    SwipeDetector swipeDetector = new SwipeDetector();



    private enum ControlGroup {
        SWIPE_TO_DISMISS
    }

    private static final String PREF_UNDO_STYLE = "de.timroes.android.listviewdemo.UNDO_STYLE";
    private static final String PREF_SWIPE_TO_DISMISS = "de.timroes.android.listviewdemo.SWIPE_TO_DISMISS";
    private static final String PREF_SWIPE_DIRECTION = "de.timroes.android.listviewdemo.SWIPE_DIRECTION";
    private static final String PREF_SWIPE_LAYOUT = "de.timroes.android.listviewdemo.SWIPE_LAYOUT";

    /**private EnhancedListAdapter mAdapter;
    private EnhancedListView mListView;
    private DrawerLayout mDrawerLayout; */

    private Bundle mUndoStylePref;
    private Bundle mSwipeDirectionPref;

    FineHistoryArrayAdapter fineHistoryArrayAdapter;

    SwipeListView swipeListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateActionBar();

         setContentView(R.layout.activity_member_fines_history);

        /**    TextView lblMeetingDate = (TextView)findViewById(R.id.lblMSHMeetingDate);
         meetingDate = getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */

        TextView lblFullName = (TextView) findViewById(R.id.lblFineFullName);
        String fullName = getIntent().getStringExtra("_name");
        lblFullName.setText(fullName);

        //ListView swipeListView= (ListView) findViewById(android.R.id.list);

      SwipeListView swipeListView= (SwipeListView) findViewById(android.R.id.list);

       // swipeViewAdapter= new SwipeViewAdapter(this, R.layout.swipelistview, countries);

       // swipeListView.setAdapter(swipeViewAdapter);

        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        fineRepo = new MeetingFineRepo(MemberFinesHistoryActivity2.this);
        meetingRepo = new MeetingRepo(MemberFinesHistoryActivity2.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);

        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            double totalFines = fineRepo.getMemberTotalFinesInCycle(targetCycleId, memberId);
        }

        populateFineHistory();

    }

    private void inflateActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back, null);
        /** final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back_done, null);
         customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
         new View.OnClickListener() {
        @Override public void onClick(View v) {
        if(saveMemberFine()) {
        Toast.makeText(MemberFinesHistoryActivity.this,"New Fine entered successfully",Toast.LENGTH_LONG).show();
        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
        i.putExtra("_tabToSelect", "fines");
        i.putExtra("_meetingDate",meetingDate);
        i.putExtra("_meetingId",meetingId);
        startActivity(i);
        finish();
        }

        }
        }); */
        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", "fines");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        startActivity(i);
                        finish();
                    }
                }
        );


        actionBar = getSupportActionBar();
        actionBar.setTitle("Fines");

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

     /**   actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE
        );
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        ); */
        // END_INCLUDE (inflate_set_custom_view)
    }

    private void populateFineHistory() {
        if (fineRepo == null) {
            fineRepo = new MeetingFineRepo(MemberFinesHistoryActivity2.this);
        }
        fines = fineRepo.getMemberFineHistoryInCycle(targetCycleId, memberId);

        if (fines == null) {
            fines = new ArrayList<MemberFineRecord>();

        }


       /** swipeViewAdapter= new SwipeViewAdapter(this,R.layout.swipelistview, countries);

        swipeListView.setAdapter(swipeViewAdapter); */
        //Now get the data via the adapter
        FineHistoryArrayAdapter adapter = new FineHistoryArrayAdapter(MemberFinesHistoryActivity2.this, fines, "fonts/roboto-regular.ttf");

        //Assign Adapter to ListView
        setListAdapter(adapter);

  /**      getListView().setOnTouchListener(swipeDetector);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                       // view.setBackgroundColor(R.color.light_blue_bottom_right);
                    } else {

                    }
                }
            }

        });*/

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.member_fine_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "fines");
                upIntent.putExtra("_meetingDate", meetingDate);
                upIntent.putExtra("_meetingId", meetingId);

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MeetingActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuMFBack:
                i = new Intent(MemberFinesHistoryActivity2.this, MeetingActivity.class);
                i.putExtra("_tabToSelect", "fines");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);
                return true;
            /** case R.id.mnuMSHSave:

             if (saveMemberFine()) {
             Toast.makeText(MemberFinesHistoryActivity.this, "Fines entered successfully", Toast.LENGTH_LONG).show();
             i = new Intent(MemberFinesHistoryActivity.this, MeetingActivity.class);
             i.putExtra("_tabToSelect", "fines");
             i.putExtra("_meetingDate", meetingDate);
             i.putExtra("_meetingId", meetingId);
             startActivity(i);
             } */
        }
        return true;
    }

    public void onClick(View view) {
        Toast.makeText(this, "Deletion undone, OKAY?", Toast.LENGTH_LONG).show();
       // fineHistoryArrayAdapter.insert(itemToDelete, position);
        view.setVisibility(View.GONE);

    }

    private void setProceedWithSavingFlg(boolean value) {
        proceedWithSaving = value;
    }

    /**  public boolean saveMemberFine() {
     boolean successFlg = false;
     double theAmount = 0.0;

     try {
     TextView txtFine = (TextView) findViewById(R.id.txtMemberFineAmount);
     String amount = txtFine.getText().toString().trim();
     if (amount.length() < 1) {
     Utils.createAlertDialogOk(MemberFinesHistoryActivity.this, "Fines", "The Fines Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
     txtFine.requestFocus();
     return false;
     } else {
     theAmount = Double.parseDouble(amount);
     if (theAmount < 0.0) {
     Utils.createAlertDialogOk(MemberFinesHistoryActivity.this, "Fines", "The Fines Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
     txtFine.requestFocus();
     return false;
     }
     }

     Spinner cboFineType = (Spinner) findViewById(R.id.cboFineType);
     int fineTypeId = (int) cboFineType.getSelectedItemId();

     //Now save
     if (fineRepo == null) {
     fineRepo = new MeetingFineRepo(MemberFinesHistoryActivity.this);
     }
     //    successFlg = fineRepo.saveMemberFine(meetingId, memberId, theAmount, fineTypeId);

     return successFlg;
     } catch (Exception ex) {
     Log.e("MemberFineHistory.saveMemberFine", ex.getMessage());
     return successFlg;
     }
     }
     */

}