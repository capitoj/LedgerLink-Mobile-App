package org.applab.digitizingdata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.helpers.LoansIssuedHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberLoanIssueRecord;
import org.applab.digitizingdata.helpers.MemberSavingRecord;
import org.applab.digitizingdata.helpers.SavingsArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/12/13.
 */
public class MemberLoansIssuedHistoryActivity extends SherlockListActivity {
    ActionBar actionBar;
    String meetingDate;
    int meetingId;
    int memberId;
    Meeting targetMeeting = null;
    MeetingRepo meetingRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    ArrayList<MemberLoanIssueRecord> loansIssued;
    int targetCycleId = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_loans_issued_history);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Loans");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView lblMeetingDate = (TextView)findViewById(R.id.lblMLIssuedHMeetingDate);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate);

        TextView lblFullNames = (TextView)findViewById(R.id.lblMLIssuedHFullNames);
        String fullNames = getIntent().getStringExtra("_names");
        lblFullNames.setText(fullNames);

        if(getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId",0);
        }

        if(getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId",0);
        }

        loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
        meetingRepo = new MeetingRepo(MemberLoansIssuedHistoryActivity.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);

        TextView txtOutstandingLoans = (TextView)findViewById(R.id.lblMLIssuedHOutstandingLoans);

        double outstandingLoans = 0.0;
        if(targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            outstandingLoans = loanIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetCycleId, memberId);
        }
        txtOutstandingLoans.setText(String.format("Total Balance: %,.0fUGX", outstandingLoans));

        populateLoanIssueHistory();

        TextView txtLILoanNo = (TextView)findViewById(R.id.txtMLIssuedHLoanNo);
        txtLILoanNo.setText("");
        txtLILoanNo.requestFocus();

    }

    private void populateLoanIssueHistory() {
        if(loanIssuedRepo == null) {
            loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
        }
        loansIssued = loanIssuedRepo.getLoansIssuedToMemberInCycle(targetCycleId, memberId);

        if(loansIssued == null) {
            loansIssued = new ArrayList<MemberLoanIssueRecord>();
        }

        //Now get the data via the adapter
        LoansIssuedHistoryArrayAdapter adapter = new LoansIssuedHistoryArrayAdapter(MemberLoansIssuedHistoryActivity.this, loansIssued);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.member_loans_issued_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect","loansIssued");
                upIntent.putExtra("_meetingDate",meetingDate);
                upIntent.putExtra("_meetingId", meetingId);

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
            case R.id.mnuMLIssuedHCancel:
                i = new Intent(MemberLoansIssuedHistoryActivity.this, MeetingActivity.class);
                i.putExtra("_tabToSelect","loansIssued");
                i.putExtra("_meetingDate",meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);

                return true;
            case R.id.mnuMLIssuedHSave:
                if(saveMemberLoan()){
                    i = new Intent(MemberLoansIssuedHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect","loansIssued");
                    i.putExtra("_meetingDate",meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
                return true;
        }
        return true;
    }

    public boolean saveMemberLoan(){
        int theLoanNo = 0;
        double theAmount = 0.0;

        try{
            TextView txtLoanNo = (TextView)findViewById(R.id.txtMLIssuedHLoanNo);
            TextView txtLoanAmount = (TextView)findViewById(R.id.txtMLIssuedHAmount);

            String loanNo = txtLoanNo.getText().toString().trim();
            if (loanNo.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue", "The Loan Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            }
            else {
                theLoanNo = Integer.parseInt(loanNo);
                if (theLoanNo <= 0.00) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue","The Loan Number is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanNo.requestFocus();
                    return false;
                }
            }

            String amount = txtLoanAmount.getText().toString().trim();
            if (amount.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue", "The Loan Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanAmount.requestFocus();
                return false;
            }
            else {
                theAmount = Double.parseDouble(amount);
                if (theAmount <= 0.00) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue","The Loan Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanAmount.requestFocus();
                    return false;
                }
            }

            //Now Save the data
            if(null == loanIssuedRepo){
                loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
            }

            //Further Validation of Uniqueness of the Loan Number
            if (!loanIssuedRepo.validateLoanNumber(theLoanNo)) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue","The Loan Number is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            }

            return loanIssuedRepo.saveMemberLoanIssue(meetingId, memberId, theLoanNo, theAmount);

        }
        catch(Exception ex) {
            Log.e("MemberLoansIssuedHistory.saveMemberLoan", ex.getMessage());
            return false;
        }
    }
}