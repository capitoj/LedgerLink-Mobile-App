package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

public class MeetingCashBookFrag extends SherlockFragment {

    ActionBar actionBar = null;
    String meetingDate = null;
    int meetingId = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_meeting_cash_book, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = getSherlockActivity().getSupportActionBar();
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
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate);

        populateCashBookFields();
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_cash_book, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                return false;
            case R.id.mnuSMDSend:
                return false;
            case R.id.mnuSMDCancel:
                return false;
            case R.id.mnuMCBFSave:
                Toast.makeText(getSherlockActivity().getApplicationContext(), "The Cashbook balances have been saved successfully.", Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    private void populateCashBookFields() {
        MeetingRepo meetingRepo = null;
        MeetingSavingRepo savingRepo = null;
        MeetingLoanRepaymentRepo repaymentRepo = null;
        MeetingLoanIssuedRepo loanIssuedRepo = null;

        try {
            meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
            repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
            loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());


            TextView lblTotalCash = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFTotalCashIn);
            TextView lblOpeningCash = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFOpeningCash);
            TextView lblTotalSavings = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFSavings);
            TextView lblTotalLoansRepaid = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFLoansRepaid);
            TextView lblTotalLoansIssued = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFLoansIssued);
            TextView lblTotalCashOut = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFTotalCashOut);
            TextView lblTotalCashBalance = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFBalBd);

            double openingCash = meetingRepo.getMeetingTotalOpeningCash(meetingId);
            double totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            double totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            double totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);

            double totalCashOut = totalLoansIssued;
            double totalCashIn = openingCash + totalSavings + totalLoansRepaid;
            double cashBalance = totalCashIn - totalCashOut;

            lblTotalCash.setText(String.format("Total Collected: %,.0fUGX", totalCashIn));
            lblOpeningCash.setText(String.format("Starting Cash: %,.0fUGX", openingCash));
            lblTotalSavings.setText(String.format("Savings: %,.0fUGX", totalSavings));
            lblTotalLoansRepaid.setText(String.format("Loans Repaid: %,.0fUGX", totalLoansRepaid));
            lblTotalLoansIssued.setText(String.format("Loans Issued: %,.0fUGX", totalLoansIssued));
            lblTotalCashOut.setText(String.format("Total Issued: %,.0fUGX", totalCashOut));
            lblTotalCashBalance.setText(String.format("Cash Balance: %,.0fUGX", cashBalance));

        }
        catch (Exception ex) {

        }
        finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }
}
