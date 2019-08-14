package org.applab.ledgerlink;

import android.app.ListActivity;
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

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.MemberWelfareRecord;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.WelfareArrayAdapter;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

public class MemberWelfareHistoryActivity extends ListActivity {

    private String meetingDate;
    private int memberId;
    private int meetingId;
    private Meeting targetMeeting = null;
    private int targetCycleId = 0;
    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomActionBar();

        setContentView(R.layout.activity_member_welfare_history);

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
            double totalWelfare = ledgerLinkApplication.getMeetingWelfareRepo().getMemberTotalWelfareInCycle(this.targetCycleId, this.memberId);
            txtTotalSavings.setText(String.format("Total welfare  %,.0f UGX\n", totalWelfare));
        }

        //Fill-out the Welfare Amount in case it exists
        if(targetMeeting != null ) {
            double welfare = ledgerLinkApplication.getMeetingWelfareRepo().getMemberWelfare(this.targetMeeting.getMeetingId(), this.memberId);
            if(welfare > 0) {
                txtMSHAmount = (TextView)findViewById(R.id.txtMSHAmount);
                txtMSHAmount.setText(String.format("%.0f",welfare));
            }
        }

        populateWelfareHistory();


        txtMSHAmount.requestFocus();
    }

    private void inflateCustomActionBar() {

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(saveMemberWelfare(true)) {
                            Toast.makeText(MemberWelfareHistoryActivity.this, R.string.welfare_entered_successfully, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "welfare");
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
                        i.putExtra("_tabToSelect", "welfare");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        //startActivity(i);
                        finish();
                    }
                });


        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle(R.string.welfare);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    private void populateWelfareHistory() {
        ArrayList<MemberWelfareRecord> meetingWelfareRecords = ledgerLinkApplication.getMeetingWelfareRepo().getMemberWelfareHistoryInCyle(targetCycleId, memberId);

        if(meetingWelfareRecords == null) {
            meetingWelfareRecords = new ArrayList<MemberWelfareRecord>();
        }

        //Now get the data via the adapter

        WelfareArrayAdapter adapter = new WelfareArrayAdapter(MemberWelfareHistoryActivity.this, meetingWelfareRecords);
        Log.e("WelfareHistoryCount", String.valueOf(meetingWelfareRecords.size()));
        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.member_welfare_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                upIntent.putExtra("_tabToSelect", "welfare");
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
                i = new Intent(MemberWelfareHistoryActivity.this, MeetingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("_tabToSelect", "welfare");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                //startActivity(i);
                return true;
            case R.id.mnuMSHSave:
                if(saveMemberWelfare(true)) {
                    Toast.makeText(MemberWelfareHistoryActivity.this, getString(R.string.welfare_entered_successfully), Toast.LENGTH_LONG).show();
                    i = new Intent(MemberWelfareHistoryActivity.this, MeetingActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("_tabToSelect", getString(R.string.welfare_small_cap));
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
        }
        return true;
    }

    public boolean saveMemberWelfare(boolean showWarning){
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


//            if (theAmount < 0.0) {
//                Utils.createAlertDialogOk(MemberWelfareHistoryActivity.this, "Welfare","The Welfare Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
//                txtSaving.requestFocus();
//                return false;
//            }

            String comment = "";
            ledgerLinkApplication.getMeetingWelfareRepo().saveMemberWelfare(meetingId, memberId, theAmount, comment);
        }
        catch(Exception ex) {
            Log.e("saveMemberWelfare", ex.getMessage());
        }
        return true;
    }
}
