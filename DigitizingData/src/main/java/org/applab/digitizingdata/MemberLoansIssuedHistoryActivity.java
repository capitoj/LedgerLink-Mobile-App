package org.applab.digitizingdata;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.helpers.LoansIssuedHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberLoanIssueRecord;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;

import android.text.Editable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
    MeetingLoanRepaymentRepo loanRepaymentRepo = null;
    ArrayList<MemberLoanIssueRecord> loansIssued;
    int targetCycleId = 0;
    double interestRate = 0.0;
    EditText editTextInterestRate;
    TextView txtTotalLoanAmount;
    double theCurLoanAmount = 0.0;
    int currentLoanId = 0;
    boolean loanWasDeleted = false;
    TextView pastLoansHeading;

    //Date stuff
    TextView txtDateDue;
    TextView viewClicked;
    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    String dateString;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomActionBar();

        setContentView(R.layout.activity_member_loans_issued_history);

        // TextView lblMeetingDate = (TextView) findViewById(R.id.lblMLIssuedHMeetingDate);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        //lblMeetingDate.setText(meetingDate);

        TextView lblFullNames = (TextView) findViewById(R.id.lblMLIssuedHFullName);
        String fullNames = getIntent().getStringExtra("_names");
        lblFullNames.setText(fullNames);

        pastLoansHeading = (TextView) findViewById(R.id.lblMLIssuedPastLoans);

        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
        meetingRepo = new MeetingRepo(MemberLoansIssuedHistoryActivity.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);
        loanRepaymentRepo = new MeetingLoanRepaymentRepo((MemberLoansIssuedHistoryActivity.this));

        //TextView txtOutstandingLoans = (TextView)findViewById(R.id.lblMLIssuedHOutstandingLoans);

        double outstandingLoans = 0.0;
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            outstandingLoans = loanIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetCycleId, memberId);
        }
        //txtOutstandingLoans.setText(String.format("Total Balance: %,.0f UGX", outstandingLoans));

        populateLoanIssueHistory();

        TextView txtLILoanNo = (TextView) findViewById(R.id.txtMLIssuedHLoanNo);
        txtLILoanNo.setText("");
        txtLILoanNo.requestFocus();

        /**
         //Date stuff
         txtDateDue = (TextView)findViewById(R.id.txtMLIssuedHDateDue);
         viewClicked = txtDateDue;
         initializeDate();

         //Set onClick Listeners to load the DateDialog for MeetingDate
         txtDateDue.setOnClickListener( new View.OnClickListener() {
        @Override public void onClick(View view) {
        //I want the Event Handler to handle both startDate and endDate
        viewClicked = (TextView)view;
        DatePickerDialog datePickerDialog = new DatePickerDialog( MemberLoansIssuedHistoryActivity.this, mDateSetListener, mYear, mMonth, mDay);
        //TODO: Enable this feature in API 11 and above
        //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
        }
        });

         */

        //Setup the Default Date
        final Calendar c = Calendar.getInstance();
        if (null != targetMeeting) {
            c.setTime(targetMeeting.getMeetingDate());
        }
        //c.add(Calendar.MONTH, 1);
        c.add(Calendar.WEEK_OF_YEAR, 4);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        //updateDisplay();

        /**
         //Handle the Loan Interest Computation
         editTextInterestRate = (EditText)findViewById(R.id.txtMLIssuedHInterest);
         txtTotalLoanAmount = (TextView)findViewById(R.id.txtMLIssuedHTotal);
         */
        //First get the Interest Rate for the Current Cycle
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            interestRate = targetMeeting.getVslaCycle().getInterestRate();
        }

        EditText txtLILoanAmount = (EditText) findViewById(R.id.txtMLIssuedHAmount);
        txtLILoanAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Compute the Interest
                double theAmount = 0.0;
                try {
                    if (s.toString().length() <= 0) {
                        return;
                    }
                    theAmount = Double.parseDouble(s.toString());
                } catch (Exception ex) {
                    return;
                }

                double interestAmount = (interestRate * 0.01 * theAmount);
                // editTextInterestRate.setText(String.format("%.0f",interestAmount));

                double totalAmount = theAmount + interestAmount;
                // txtTotalLoanAmount.setText(String.format("%,.0f", totalAmount));

                //Have this value redundantly stored for future use
                theCurLoanAmount = theAmount;
            }
        });

        /**
         //Now deal with Loan Interest Manual Changes
         EditText txtLILoanInterestAmount = (EditText)findViewById(R.id.txtMLIssuedHInterest);
         txtLILoanInterestAmount.addTextChangedListener(new TextWatcher(){
        @Override public void afterTextChanged(Editable s) {

        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Compute the Interest
        double theInterestAmount = 0.0;
        try {
        if(s.toString().length() <= 0) {
        return;
        }
        theInterestAmount = Double.parseDouble(s.toString());
        }
        catch(Exception ex) {
        return;
        }

        double totalAmount = theInterestAmount + theCurLoanAmount;
        txtTotalLoanAmount.setText(String.format("%,.0f",totalAmount));
        }
        });
         */

        // Display Members current loan if any
        TextView txtLoanNo = (TextView) findViewById(R.id.txtMLIssuedHLoanNo);
        TextView txtLoanAmount = (TextView) findViewById(R.id.txtMLIssuedHAmount);
        TextView txtComment = (TextView) findViewById(R.id.txtMLIssuedHComment);

        /**TextView txtInterestAmount = (TextView)findViewById(R.id.txtMLIssuedHInterest);
         TextView txtTotalAmount = (TextView)findViewById(R.id.txtMLIssuedHTotal);
         TextView txtDateDue = (TextView)findViewById(R.id.txtMLIssuedHDateDue); */

        if (null == loanIssuedRepo) {
            loanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());
        }
        MeetingLoanIssued memberLoan = loanIssuedRepo.getLoanIssuedToMemberInMeeting(meetingId, memberId);
        if (null != memberLoan) {
            currentLoanId = memberLoan.getLoanId();
            txtLoanNo.setText(String.format("%d", memberLoan.getLoanNo()));
            txtLoanAmount.setText(String.format("%.0f", memberLoan.getPrincipalAmount()));
            txtComment.setText(String.format("%s", memberLoan.getComment()));
            /** txtInterestAmount.setText(String.format("%.0f",memberLoan.getInterestAmount()));
             txtDateDue.setText(Utils.formatDate(memberLoan.getDateDue(),"dd-MMM-yyyy"));

             //May consider just recomputing the total amount afresh
             txtTotalAmount.setText(String.format("%.0f",memberLoan.getLoanBalance())); */
        }
    }

    private void inflateCustomActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (saveMemberLoan()) {
                            if (currentLoanId > 0) {
                                if (loanWasDeleted) {
                                    Toast.makeText(MemberLoansIssuedHistoryActivity.this, "The Loan has been cancelled.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MemberLoansIssuedHistoryActivity.this, "The Loan has been edited successfully", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MemberLoansIssuedHistoryActivity.this, "New Loan issued successfully", Toast.LENGTH_LONG).show();
                            }
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "loansIssued");
                            i.putExtra("_meetingDate", meetingDate);
                            i.putExtra("_meetingId", meetingId);
                            startActivity(i);
                            finish();
                        }

                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", "loansIssued");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        startActivity(i);
                        finish();
                    }
                }
        );


        actionBar = getSupportActionBar();
        actionBar.setTitle("New Loans");

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        /**actionBar.setDisplayOptions(
         ActionBar.DISPLAY_SHOW_CUSTOM,
         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
         | ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView,
         new ActionBar.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT,
         ViewGroup.LayoutParams.MATCH_PARENT)); */
        // END_INCLUDE (inflate_set_custom_view)
    }

    private void populateLoanIssueHistory() {

        String loanProgressComment = "";

        if (loanIssuedRepo == null) {
            loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
        }
        loansIssued = loanIssuedRepo.getLoansIssuedToMemberInCycle(targetCycleId, memberId);

        if (loansIssued == null) {
            loansIssued = new ArrayList<MemberLoanIssueRecord>();
        }

        if (loansIssued.isEmpty()) {
            pastLoansHeading.setVisibility(View.INVISIBLE);
        }

        //Now get the data via the adapter
        LoansIssuedHistoryArrayAdapter adapter = new LoansIssuedHistoryArrayAdapter(MemberLoansIssuedHistoryActivity.this, loansIssued, "fonts/roboto-regular.ttf");

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
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "loansIssued");
                upIntent.putExtra("_meetingDate", meetingDate);
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
                i.putExtra("_tabToSelect", "loansIssued");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);

                return true;
            case R.id.mnuMLIssuedHSave:
                if (saveMemberLoan()) {
                    i = new Intent(MemberLoansIssuedHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect", "loansIssued");
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
                return true;
        }
        return true;
    }

    public boolean saveMemberLoan() {
        int theLoanNo = 0;
        double theAmount = 0.0;
        double theInterestAmount = 0.0;
        String theComment = "";
        Date theDateDue = null;

        try {
            TextView txtLoanNo = (TextView) findViewById(R.id.txtMLIssuedHLoanNo);
            TextView txtLoanAmount = (TextView) findViewById(R.id.txtMLIssuedHAmount);
            TextView txtComment = (TextView) findViewById(R.id.txtMLIssuedHComment);
            // TextView txtInterestAmount = (TextView)findViewById(R.id.txtMLIssuedHInterest);
            // TextView txtDateDue = (TextView)findViewById(R.id.txtMLIssuedHDateDue);

            String amount = txtLoanAmount.getText().toString().trim();
            if (amount.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanAmount.requestFocus();
                return false;
            } else {
                theAmount = Double.parseDouble(amount);
                //Loan Amounts for new loans must be positive
                if (theAmount < 0.00 && currentLoanId > 0) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanAmount.requestFocus();
                    return false;
                }
            }

            String loanNo = txtLoanNo.getText().toString().trim();
            if (loanNo.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            } else {
                theLoanNo = Integer.parseInt(loanNo);
                if (theLoanNo <= 0.00) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Number is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanNo.requestFocus();
                    return false;
                }
            }

            String comment = txtComment.getText().toString().trim();
            if (comment.length() < 1) {
                theComment = "";
            } else {
                theComment = comment;
            }

            //Interest Amount
            theInterestAmount = (interestRate * 0.01 * theAmount);
            /** String interestAmount = txtInterestAmount.getText().toString().trim();
             if (interestAmount.length() < 1) {
             //Not sure whether there would be more to do
             theInterestAmount = 0.0;
             }
             else {
             theInterestAmount = Double.parseDouble(interestAmount);
             if (theInterestAmount < 0.00) {
             Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue","The Interest Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
             txtLoanNo.requestFocus();
             return false;
             }
             }

             //Date Due: Check it against the meeting date, not calendar date
             //TODO: I will try using Date.CompareTo(date2)
             Date today = Calendar.getInstance().getTime();
             String dateDue = txtDateDue.getText().toString().trim();
             Date dtDateDue = Utils.getDateFromString(dateDue,Utils.DATE_FIELD_FORMAT);
             if (dtDateDue.before(targetMeeting.getMeetingDate())) {
             Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue","The due date has to be a future date.", Utils.MSGBOX_ICON_EXCLAMATION).show();
             txtDateDue.requestFocus();
             return false;
             }
             else {
             theDateDue = dtDateDue;
             }

             //Now Save the data
             if(null == loanIssuedRepo){
             loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
             }
             */
            //Further Validation of Uniqueness of the Loan Number
            if (!loanIssuedRepo.validateLoanNumber(theLoanNo, meetingId, memberId)) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Number is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            }

            //Determine whether to delete this current loan i.e. in case it is being edited and the amount is set to zero
            if (currentLoanId > 0 && theAmount <= 0) {
                //Mark flag to indicate that the loan was deleted
                loanWasDeleted = true;
                return loanIssuedRepo.deleteLoan(currentLoanId);
            }

            return loanIssuedRepo.saveMemberLoanIssue(meetingId, memberId, theLoanNo, theAmount, theInterestAmount, theDateDue, theComment);

        } catch (Exception ex) {
            ex.printStackTrace();
            //Log.e("MemberLoansIssuedHistory.saveMemberLoan", ex.getMessage());
            return false;
        }
    }

    //DATE
    //Event that is raised when the date has been set
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    @Override
    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog) {
        // TODO Auto-generated method stub
        super.onPrepareDialog(id, dialog);
        ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
    }

    //Displays the selected Date in the TextView
    private void updateDisplay() {
        if (viewClicked != null) {
            dateString = (new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mDay)
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)).toString();
            viewClicked.setText(dateString);
        } else {
            //Not sure yet on what to do
        }
    }

    private void initializeDate() {
        if (viewClicked != null) {
            Calendar c = Calendar.getInstance();
            if (null != targetMeeting) {
                c.setTime(targetMeeting.getMeetingDate());
            }
            c.add(Calendar.WEEK_OF_YEAR, 4);
            dateString = Utils.formatDate(c.getTime());
            viewClicked.setText(dateString);
        }
    }

    private void LoanDueDate() {
        // if(viewClicked != null) {
        Calendar c = Calendar.getInstance();
        if (null != targetMeeting) {
            c.setTime(targetMeeting.getMeetingDate());
        }
        c.add(Calendar.WEEK_OF_YEAR, 4);
        dateString = Utils.formatDate(c.getTime());
        //viewClicked.setText(dateString);
    }
}