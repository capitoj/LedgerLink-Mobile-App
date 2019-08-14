package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.helpers.adapters.MemberLoanIssueAdapter;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MemberRepo;

import java.util.ArrayList;
import java.util.List;


public class MeetingMemberLoansIssueActivity extends ActionBarActivity{

    protected Member selectedMember;
    protected ListView listUnpaidLoans;
    protected ArrayAdapter<MeetingLoanIssued> adapter;
    protected List<MeetingLoanIssued> loansIssued;
    protected Meeting meeting;
    protected Double totalCashInBox = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_member_loans_issue);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int memberId = getIntent().getIntExtra("_memberId", 0);
        int meetingId = getIntent().getIntExtra("_meetingId", 0);
        MemberRepo memberRepo = new MemberRepo(this);
        selectedMember = memberRepo.getMemberById(memberId);
        this.getSupportActionBar().setTitle(selectedMember.getFullName());
        MeetingRepo meetingRepo = new MeetingRepo(this, meetingId);
        meeting = meetingRepo.getMeeting();
        this.init();
    }

    protected void init(){
        this.listUnpaidLoans = (ListView) findViewById(R.id.listUnpaidLoans);
        this.listUnpaidLoans.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MeetingLoanIssued meetingLoanIssued = loansIssued.get(position);
                if (getIntent().getStringExtra("_action").equals("loanissue")) {
                    Intent viewHistory = new Intent(view.getContext(), MemberLoansIssuedHistoryActivity.class);
                    viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                    viewHistory.putExtra("_names", selectedMember.toString());
                    viewHistory.putExtra("_meetingDate", meeting.getMeetingDate());
                    viewHistory.putExtra("_meetingId", meeting.getMeetingId());
                    viewHistory.putExtra("_totalCashInBox", totalCashInBox);
                    viewHistory.putExtra("_loanId", meetingLoanIssued.getLoanId());
                    viewHistory.putExtra("_action", 1);
                    startActivity(viewHistory);
                } else if (getIntent().getStringExtra("_action").equals("loanrepayment")) {
                    Intent viewHistory = new Intent(view.getContext(), MemberLoansRepaidHistoryActivity.class);
                    viewHistory.putExtra("_memberId", selectedMember.getMemberId());
                    viewHistory.putExtra("_names", selectedMember.getFullName());
                    viewHistory.putExtra("_meetingDate", meeting.getMeetingDate());
                    viewHistory.putExtra("_meetingId", meeting.getMeetingId());
                    viewHistory.putExtra("_viewingSentData", getIntent().getBooleanExtra("_viewingSentData", false));
                    viewHistory.putExtra("_loanId", meetingLoanIssued.getLoanId());
                    startActivity(viewHistory);
                }
            }
        });

        MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(this);
        this.loansIssued = loanIssuedRepo.getOutstandingMemberLoans(this.meeting.getVslaCycle().getCycleId(), this.selectedMember.getMemberId());
        this.adapter = new MemberLoanIssueAdapter(this, this.loansIssued);
        this.listUnpaidLoans.setAdapter(this.adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getIntent().getStringExtra("_action").equals("loanissue")) {
            getMenuInflater().inflate(R.menu.menu_meeting_member_loans_issue, menu);
            this.menuAction(menu);
        }
        return true;
    }

    protected void menuAction(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.action_new_loan);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_new_loan:
                        prepNewLoan();
                        break;
                }
                return true;
            }
        });
    }

    protected void prepNewLoan(){
        if(selectedMember.getMemberNo() > 0) {
            Intent viewHistory = new Intent(this, MemberLoansIssuedHistoryActivity.class);
            viewHistory.putExtra("_memberId", selectedMember.getMemberId());
            viewHistory.putExtra("_names", selectedMember.toString());
            viewHistory.putExtra("_meetingDate", meeting.getMeetingDate());
            viewHistory.putExtra("_meetingId", meeting.getMeetingId());
            viewHistory.putExtra("_totalCashInBox", totalCashInBox);
            viewHistory.putExtra("_action", 0);
            startActivity(viewHistory);
        }else{
            Toast.makeText(this, getString(R.string.cannot_issue_new_loan_to) + selectedMember.getFullName() + getString(R.string.does_not_have_member_no), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home : this.finish(); break;
        }
        return super.onOptionsItemSelected(item);
    }
}
