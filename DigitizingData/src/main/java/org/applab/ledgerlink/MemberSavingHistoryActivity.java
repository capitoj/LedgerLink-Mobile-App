package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
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
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.MemberSavingRecord;
import org.applab.ledgerlink.helpers.SavingsArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;

/**
 * Created by Moses on 7/7/13.
 */
public class MemberSavingHistoryActivity extends SherlockListActivity {
    private String meetingDate;
    private int memberId;
    private int meetingId;
    private Meeting targetMeeting = null;
    private int targetCycleId = 0;
    boolean alertDialogShowing = false;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomActionBar();

        setContentView(R.layout.activity_member_saving_history);

      /**  TextView lblMeetingDate = (TextView)findViewById(R.id.lblMSHMeetingDate);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate); */

        TextView lblFullNames = (TextView)findViewById(R.id.lblMSHFullNames);
        String fullNames = getIntent().getStringExtra("_names");
        lblFullNames.setText(fullNames);

        if(getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId",0);
        }

        if(getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId",0);
        }

        targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        TextView txtTotalSavings = (TextView)findViewById(R.id.lblMSHTotalSavings);
        TextView txtMSHAmount = (TextView)findViewById(R.id.txtMSHAmount);

        if(targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            double totalSavings = ledgerLinkApplication.getMeetingSavingRepo().getMemberTotalSavingsInCycle(targetCycleId, memberId);
            txtTotalSavings.setText(String.format("Total Savings  %,.0f UGX\n", totalSavings));
        }

        //Fill-out the Savings Amount in case it exists
        if(targetMeeting != null ) {
            double saving = ledgerLinkApplication.getMeetingSavingRepo().getMemberSaving(targetMeeting.getMeetingId(), memberId);
            if(saving > 0) {
                txtMSHAmount = (TextView)findViewById(R.id.txtMSHAmount);
                txtMSHAmount.setText(String.format("%.0f",saving));
            }
        }

        populateSavingHistory();


        txtMSHAmount.requestFocus();
    }

    private void inflateCustomActionBar() {

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(saveMemberSaving()) {
                            Toast.makeText(MemberSavingHistoryActivity.this, "Savings entered successfully", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "savings");
                            i.putExtra("_meetingDate", meetingDate);
                            i.putExtra("_meetingId", meetingId);
                            //startActivity(i);
                            finish();
                        }

                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", "savings");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        //startActivity(i);
                        finish();
                    }
                });


        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("SAVINGS");
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
       /** actionBar.setTitle("Savings");

        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)); */
    }

    private void populateSavingHistory() {
         ArrayList<MemberSavingRecord> savings = ledgerLinkApplication.getMeetingSavingRepo().getMemberSavingHistoryInCycle(targetCycleId, memberId);

        if(savings == null) {
            savings = new ArrayList<MemberSavingRecord>();
        }

        //Now get the data via the adapter
        SavingsArrayAdapter adapter = new SavingsArrayAdapter(MemberSavingHistoryActivity.this, savings);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.member_saving_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "savings");
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
            case R.id.mnuMSHCancel:
                i = new Intent(MemberSavingHistoryActivity.this, MeetingActivity.class);
                i.putExtra("_tabToSelect", "savings");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                //startActivity(i);
                return true;
            case R.id.mnuMSHSave:

                if(saveMemberSaving()) {
                    Toast.makeText(MemberSavingHistoryActivity.this,"Savings entered successfully",Toast.LENGTH_LONG).show();
                    i = new Intent(MemberSavingHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect", "savings");
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
        }
        return true;
    }

    private void setProceedWithSavingFlg(boolean value) {
        boolean proceedWithSaving = value;
    }

    public boolean saveMemberSaving(){
        boolean successFlg = false;
        double theAmount = 0.0;

        try{
            TextView txtSaving = (TextView)findViewById(R.id.txtMSHAmount);
            String amount = txtSaving.getText().toString().trim();
            if (amount.length() < 1) {
                //Utils.createAlertDialogOk(MemberSavingHistoryActivity.this, "Savings","The Savings Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtSaving.requestFocus();
                //return false;
                //if savings is blank, default to 0
                txtSaving.setText("0");
                amount = "0";
            }


                theAmount = Double.parseDouble(amount);


            if (theAmount < 0.0) {
                    Utils.createAlertDialogOk(MemberSavingHistoryActivity.this, "Savings","The Savings Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtSaving.requestFocus();
                    return false;
                }
            else if(theAmount>0 && theAmount < targetMeeting.getVslaCycle().getSharePrice()) {
                //Check if saving amount is less than price of a single star (share)
                Utils.createAlertDialogOk(MemberSavingHistoryActivity.this,
                        "Savings","The amount entered "+Utils.formatNumber(theAmount)+" is less than one star, please enter atleast "+
                        Utils.formatNumber(targetMeeting.getVslaCycle().getSharePrice())+" "+ getResources().getString(R.string.operating_currency),
                        Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtSaving.requestFocus();
                return false;
            }



            /*//Validate that the savings is within the range of Shares
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                //Check whether the Savings amount exceeds the total savings allowed per meeting
                //OMM: Android does not support synchronous modal dialog boxes: They are all asynchronous
                double maxSavingAmount = targetMeeting.getVslaCycle().getMaxSharesQty() * targetMeeting.getVslaCycle().getSharePrice();
                if(theAmount > maxSavingAmount && maxSavingAmount != 0){
                    AlertDialog.Builder ad = new AlertDialog.Builder(MemberSavingHistoryActivity.this);
                    ad.setTitle("Savings");
                    ad.setMessage(String.format("The savings amount of %,.0f %s is more than the maximum allowed amount of %,.0f %s per meeting. \nDo you want to accept it?",
                            theAmount, "UGX",maxSavingAmount, "UGX"));
                    ad.setNegativeButton(
                            "No", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int arg1) {
                            proceedWithSaving = false;
                        }
                    });
                    ad.setPositiveButton(
                            "Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {

                            proceedWithSaving = true;
                        }
                    });
                    ad.show();
                }

                //This will not work because android does not support synchronous modal dialogs
                if(!proceedWithSaving) {
                    return false;
                }

                //Check that Savings is a multiple of Share Price
                double sharePrice = targetMeeting.getVslaCycle().getSharePrice();
                if(sharePrice > 0) {
                    if((theAmount / sharePrice) < 1 || ((int)theAmount % (int)sharePrice) != 0){
                        AlertDialog.Builder ad = new AlertDialog.Builder(MemberSavingHistoryActivity.this);
                        ad.setTitle("Savings");
                        ad.setMessage(String.format("The savings amount of %,.0f %s is not a multiple of the share price. \nDo you want to accept it?",
                                theAmount, "UGX"));
                        ad.setNegativeButton(
                                "No", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int arg1) {
                                proceedWithSaving = false;
                            }
                        });
                        ad.setPositiveButton(
                                "Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {

                                proceedWithSaving = true;
                            }
                        });
                        //ad.show();
                        ad.create().show();
                    }
                }
            }

            if(!proceedWithSaving) {
                return false;
            }
            */

            //Now save


            String comment = "";
            successFlg = ledgerLinkApplication.getMeetingSavingRepo().saveMemberSaving(meetingId, memberId, theAmount, comment);

            return successFlg;
        }
        catch(Exception ex) {
            Log.e("MemberSavingHistory.saveMemberSaving", ex.getMessage());
            return false;
        }
    }



}