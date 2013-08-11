package org.applab.digitizingdata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.helpers.LoanRepaymentHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.LoansIssuedHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberLoanIssueRecord;
import org.applab.digitizingdata.helpers.MemberLoanRepaymentRecord;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/13/13.
 */
public class MemberLoansRepaidHistoryActivity extends SherlockListActivity {
    ActionBar actionBar;
    String meetingDate;
    int meetingId;
    int memberId;
    int targetCycleId = 0;
    Meeting targetMeeting = null;
    MeetingRepo meetingRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    MeetingLoanRepaymentRepo loansRepaidRepo = null;
    ArrayList<MemberLoanRepaymentRecord> loanRepayments;
    MeetingLoanIssued recentLoan = null;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_loans_repaid_history);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Repayments");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView lblMeetingDate = (TextView)findViewById(R.id.lblMLRepayHMeetingDate);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate);

        TextView lblFullNames = (TextView)findViewById(R.id.lblMLRepayHFullNames);
        String fullNames = getIntent().getStringExtra("_names");
        lblFullNames.setText(fullNames);

        if(getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId",0);
        }

        if(getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId",0);
        }

        meetingRepo = new MeetingRepo(MemberLoansRepaidHistoryActivity.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);
        loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansRepaidHistoryActivity.this);
        loansRepaidRepo = new MeetingLoanRepaymentRepo(MemberLoansRepaidHistoryActivity.this);

        //Get Loan Number of currently running loan
        TextView txtLoanNumber = (TextView)findViewById(R.id.txtMLRepayHLoanNo);
        recentLoan = loanIssuedRepo.getMostRecentLoanIssuedToMember(memberId);
        if(null != recentLoan) {
            txtLoanNumber.setText(String.format("%d",recentLoan.getLoanNo()));

            //If there is an existing Repayment for this meeting, show the amount
            
        }
        else {
            txtLoanNumber.setText(null);

            //Show that Member has No Loan
            TextView lblLoanNo = (TextView)findViewById(R.id.lblMLRepayHLoanNo);
            lblLoanNo.setText("Member does not have an outstanding loan.");

            //Remove the widgets for capturing Loans
            LinearLayout parent = (LinearLayout)lblLoanNo.getParent();

            //Remove LoanNo
            LinearLayout frmLoanNo = (LinearLayout)findViewById(R.id.frmMLRepayHLoanNo);
            parent.removeView(frmLoanNo);

            //Remove Amount
            TextView lblAmount = (TextView)findViewById(R.id.lblMLRepayHAmount);
            parent.removeView(lblAmount);
            LinearLayout frmAmount = (LinearLayout)findViewById(R.id.frmMLRepayHAmount);
            parent.removeView(frmAmount);

            //Remove Comment
            TextView lblComment = (TextView)findViewById(R.id.lblMLRepayHComment);
            parent.removeView(lblComment);

            TextView txtComment = (TextView)findViewById(R.id.txtMLRepayHComment);
            parent.removeView(txtComment);

        }

        TextView txtOutstandingLoans = (TextView)findViewById(R.id.lblMLRepayHOutstandingLoans);

        double outstandingLoans = 0.0;
        if(targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            outstandingLoans = loanIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetCycleId, memberId);
        }
        txtOutstandingLoans.setText(String.format("Total Balance: %,.0fUGX", outstandingLoans));

        //Populate the History
        populateLoanRepaymentHistory();

        //TODO: Check this
        TextView txtLRAmount = (TextView)findViewById(R.id.txtMLRepayHAmount);
        txtLRAmount.requestFocus();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.member_loans_repaid_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect","loansRepaid");
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
            case R.id.mnuMLRepayHCancel:
                i = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                i.putExtra("_tabToSelect","loansRepaid");
                i.putExtra("_meetingDate",meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);
                return true;
            case R.id.mnuMLRepayHSave:
                if(recentLoan == null) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment","The member has no Outstanding Loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return false;
                }
                if(saveMemberLoanRepayment()) {
                    i = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect","loansRepaid");
                    i.putExtra("_meetingDate",meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
                return true;
        }
        return true;
    }

    private void populateLoanRepaymentHistory() {
        if(loansRepaidRepo == null) {
            loansRepaidRepo = new MeetingLoanRepaymentRepo(MemberLoansRepaidHistoryActivity.this);
        }
        loanRepayments = loansRepaidRepo.getLoansRepaymentsByMemberInCycle(targetCycleId, memberId);

        if(loanRepayments == null) {
            loanRepayments = new ArrayList<MemberLoanRepaymentRecord>();
        }

        //Now get the data via the adapter
        LoanRepaymentHistoryArrayAdapter adapter = new LoanRepaymentHistoryArrayAdapter(MemberLoansRepaidHistoryActivity.this, loanRepayments);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    public boolean saveMemberLoanRepayment(){
        double theAmount = 0.0;

        try{

            TextView txtLoanAmount = (TextView)findViewById(R.id.txtMLRepayHAmount);
            TextView txtComments = (TextView)findViewById(R.id.txtMLRepayHComment);

            String amount = txtLoanAmount.getText().toString().trim();
            if (amount.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The Loan Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanAmount.requestFocus();
                return false;
            }
            else {
                theAmount = Double.parseDouble(amount);
                if (theAmount <= 0.00) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment","The Loan Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanAmount.requestFocus();
                    return false;
                }
            }

            String comments = txtComments.getText().toString().trim();

            //Now Save the data
            if(null == loansRepaidRepo){
                loansRepaidRepo = new MeetingLoanRepaymentRepo(MemberLoansRepaidHistoryActivity.this);
            }

            //retrieve the LoanId and LoanNo of the most recent uncleared loan
            int recentLoanId = 0;
            double balanceBefore = 0.0;
            if(null != recentLoan) {
                recentLoanId = recentLoan.getLoanId();
                balanceBefore = recentLoan.getLoanBalance();
            }
            else {
                //check again: Do not save repayment if there is no existing loan
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment","The member has no Outstanding Loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                return false;

            }
            boolean saveRepayment = loansRepaidRepo.saveMemberLoanRepayment(meetingId, memberId, recentLoanId, theAmount, balanceBefore, comments);
            if(saveRepayment) {
                //Also update the balances
                if (loanIssuedRepo == null) {
                    loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansRepaidHistoryActivity.this);
                }

                return loanIssuedRepo.updateMemberLoanBalances(recentLoan.getLoanId(),recentLoan.getTotalRepaid() + theAmount, recentLoan.getLoanBalance() - theAmount);

            }
            else {
                //Saving failed
                return false;
            }
        }
        catch(Exception ex) {
            Log.e("MemberLoansRepaidHistory.saveMemberLoanRepayment", ex.getMessage());
            return false;
        }
    }
}
