package org.applab.ledgerlink;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingOutstandingWelfareRepo;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.util.Calendar;
import java.util.Date;

public class AddBorrowFromWelfareMeetingActivity extends ActionBarActivity{

    LedgerLinkApplication ledgerLinkApplication;
    private int selectedMemberId;
    private int meetingId;
    private MeetingOutstandingWelfare meetingOutstandingWelfare;
    private boolean selectedFinishButton = false;
    private Meeting meeting = null;

    private TextView txtOutstandingWelfareDueDate;

    int mYear;
    int mMonth;
    int mDay;

    protected TextView viewClicked;

    protected final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    protected void updateDisplay() {
        if (viewClicked != null) {
            viewClicked.setText(String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth) + "-" + mYear);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        if (getIntent().hasExtra("_memberId")) {
            this.selectedMemberId = getIntent().getIntExtra("_memberId", 0);
        }

        if (getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
            if(this.meetingId != 0){
                this.meeting = new MeetingRepo(ledgerLinkApplication, this.meetingId).getMeeting();
            }
        }

        String fullName = getIntent().getStringExtra("_name");

        inflateCustomActionBar();

        setContentView(R.layout.activity_add_borrow_from_welfare_meeting);

        TextView lblMemberBorrowFromWelfareFullName = (TextView) findViewById(R.id.lblMemberBorrowFromWelfareFullName);
        lblMemberBorrowFromWelfareFullName.setText(fullName);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);

        txtOutstandingWelfareDueDate = (TextView) findViewById(R.id.txtOutstandingWelfareDueDate);
        txtOutstandingWelfareDueDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));
        txtOutstandingWelfareDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date nextOutstandingWelfareDueDate = Utils.stringToDate(txtOutstandingWelfareDueDate.getText().toString(), "dd-MMM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextOutstandingWelfareDueDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddBorrowFromWelfareMeetingActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_the_outstanding_welfare_due_date));
                datePickerDialog.show();
            }
        });

        populateMemberOutstandingWelfare();
    }

    protected void populateMemberOutstandingWelfare(){
        int meetingOutstandingWelfareId = new MeetingOutstandingWelfareRepo(ledgerLinkApplication).getOutstandingWelfareId(this.meeting.getVslaCycle().getCycleId(), this.selectedMemberId);
        if(meetingOutstandingWelfareId > 0){
            this.meetingOutstandingWelfare = new MeetingOutstandingWelfareRepo(ledgerLinkApplication, meetingOutstandingWelfareId).getMeetingOutstandingWelfare();
            EditText txtIssueMemberWelfareAmount = (EditText) findViewById(R.id.txtIssueMemberWelfareAmount);
            txtIssueMemberWelfareAmount.setText(Utils.formatRealNumber(meetingOutstandingWelfare.getAmount()));
            txtOutstandingWelfareDueDate.setText(Utils.formatDate(meetingOutstandingWelfare.getExpectedDate()));

        }
    }

    private void inflateCustomActionBar() {

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView;

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFinishButton = true;
                        Member member = new Member();
                        member.setMemberId(selectedMemberId);
                        if(saveMemberOutstandingWelfare(member)){
                            finish();
                        }
                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        // actionbar with logo
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle(R.string.borrow_from_welfare);

        // Set to false to remove caret and disable its function; if designer decides otherwise set both to true
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    protected boolean saveMemberOutstandingWelfare(Member member){
        boolean saveOutstandingWelfare = false;
        if(validateData(member)){
            ledgerLinkApplication.getMeetingOutstandingWelfareRepo().saveMemberOutstandingWelfare(this.meetingOutstandingWelfare);
            saveOutstandingWelfare = true;
        }
        return saveOutstandingWelfare;
    }

    protected boolean validateData(Member member){
        if(member == null){
            return false;
        }
        if(this.meetingOutstandingWelfare == null) {
            this.meetingOutstandingWelfare = new MeetingOutstandingWelfare();
        }

        String dlgTitle = getString(R.string.borrow_from_welfare_main);

        EditText txtIssueMemberWelfareAmount = (EditText) findViewById(R.id.txtIssueMemberWelfareAmount);
        String outstandingWelfareAmount = txtIssueMemberWelfareAmount.getText().toString().trim();
        if(outstandingWelfareAmount.length() == 0){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.welfare_amount_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtIssueMemberWelfareAmount.requestFocus();
            return false;
        }else{
            double disbursedWelfareAmount = Double.parseDouble(outstandingWelfareAmount);
            if(disbursedWelfareAmount < 1){
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.welfare_amount_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtIssueMemberWelfareAmount.requestFocus();
                return false;
            }else{
                this.meetingOutstandingWelfare.setAmount(disbursedWelfareAmount);
                this.meetingOutstandingWelfare.setMember(member);
                Meeting issuedInMeeting = new Meeting();
                issuedInMeeting.setMeetingId(this.meetingId);
                this.meetingOutstandingWelfare.setMeeting(issuedInMeeting);

                Meeting paidInMeeting = new Meeting();
                paidInMeeting.setMeetingId(0);
                this.meetingOutstandingWelfare.setPaidInMeeting(paidInMeeting);
                this.meetingOutstandingWelfare.setIsCleared(0);
                Date outstandingDueDate = Utils.stringToDate(txtOutstandingWelfareDueDate.getText().toString(), "dd-MMM-yyyy");
                this.meetingOutstandingWelfare.setExpectedDate(outstandingDueDate);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.mnuFFinished:
                return false;
        }
        return true;
    }
}
