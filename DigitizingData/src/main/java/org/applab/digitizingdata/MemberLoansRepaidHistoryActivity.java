package org.applab.digitizingdata;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.helpers.LoanRepaymentHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberLoanRepaymentRecord;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/13/13.
 */
public class MemberLoansRepaidHistoryActivity extends SherlockListActivity {
    ActionBar actionBar;
    String meetingDate;
    int meetingId;
    int memberId;
    int targetCycleId = 0;
    VslaCycle cycle = null;
    Meeting targetMeeting = null;
    MeetingRepo meetingRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    MeetingLoanRepaymentRepo loansRepaidRepo = null;
    VslaCycleRepo vslaCycleRepo = null;
    ArrayList<MemberLoanRepaymentRecord> loanRepayments;
    MeetingLoanIssued recentLoan = null;

    LinearLayout repaymentHistorySection;

    //Flags for Edit Operation
    boolean isEditOperation = false;

    //This is the repayment that is being edited
    MemberLoanRepaymentRecord repaymentBeingEdited = null;

    //Fields for Rollover calculation
    double interestRate = 0.0;
    EditText editTextInterestRate;
    TextView txtRolloverAmount;
    TextView txtLoanBalance;
    double theCurLoanBalanceAmount = 0.0;
    double theCurLoanRepayAmount = 0.0;

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

        setContentView(R.layout.activity_member_loans_repaid_history);

        /** TextView lblMeetingDate = (TextView)findViewById(R.id.lblMLRepayHMeetingDate);
         meetingDate = getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */

        TextView lblFullName = (TextView) findViewById(R.id.lblMLRepayHFullName);
        String fullName = getIntent().getStringExtra("_names");
        lblFullName.setText(fullName);

        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        meetingRepo = new MeetingRepo(MemberLoansRepaidHistoryActivity.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);
        loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansRepaidHistoryActivity.this);
        loansRepaidRepo = new MeetingLoanRepaymentRepo(MemberLoansRepaidHistoryActivity.this);
        vslaCycleRepo = new VslaCycleRepo(MemberLoansRepaidHistoryActivity.this);

        //Determine whether this is an edit operation on an existing Loan Repayment
        repaymentBeingEdited = loansRepaidRepo.getLoansRepaymentByMemberInMeeting(meetingId, memberId);
        if (null != repaymentBeingEdited) {
            //Flag that this is an edit operation
            isEditOperation = true;
        }

        // Get current cycle interest rate
        TextView lblInterestDesc = (TextView) findViewById(R.id.lblMLRepayHInterestDesc);
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            cycle = vslaCycleRepo.getCycle(targetCycleId);
            lblInterestDesc.setText(String.format("at %.0f%% every 30 days", cycle.getInterestRate()));
        }

        // Get Loan Number of currently running loan
        TextView lblLoanNo = (TextView) findViewById(R.id.lblMLRepayHLoanNo);
        TextView txtLoanNumber = (TextView) findViewById(R.id.txtMLRepayHLoanNo);
        TextView txtLoanAmountFld = (TextView) findViewById(R.id.txtMLRepayHAmount);
        TextView txtComment = (TextView) findViewById(R.id.txtMLRepayHComment);
        TextView txtBalance = (TextView) findViewById(R.id.txtMLRepayHBalance);
        final TextView txtNewInterest = (TextView) findViewById(R.id.txtMLRepayHInterest);
        TextView txtTotal = (TextView) findViewById(R.id.txtMLRepayHTotal);
        final TextView txtNewDateDue = (TextView) findViewById(R.id.txtMLRepayHDateDue);
        TextView lblInstruction = (TextView) findViewById(R.id.lblMLRepayHInstruction);
        final TextView lblMLRepayHLBCurrency = (TextView) findViewById(R.id.lblMLRepayHLBCurrency);
        final TextView lblMLRepayHCurrencyTotal = (TextView) findViewById(R.id.lblMLRepayHCurrencyTotal);
        repaymentHistorySection = (LinearLayout) findViewById(R.id.frmMLRepayHHistory);

        recentLoan = loanIssuedRepo.getMostRecentLoanIssuedToMember(memberId);
        StringBuilder sb = null;
        if (null != recentLoan) {
            //lblMLRepayHCurrencyTotal.setVisibility(View.GONE);
            //lblMLRepayHLBCurrency.setVisibility(View.GONE);
            txtLoanNumber.setText(String.format("%d", recentLoan.getLoanNo()));

            //Setup the Instruction
            sb = new StringBuilder("If a payment is NOT due and no payment is made, press <b>cancel</b>. If a payment is due but not made enter 0 (zero) for payment amount. After entering payment, review new balance.");
            lblInstruction.setText(Html.fromHtml(sb.toString()));

            // Now in case this is an edit operation populate the fields with the Repayment being edited
            if (null != repaymentBeingEdited && isEditOperation) {
                // Populate the fields
                txtLoanAmountFld.setText(String.format("%.0f", repaymentBeingEdited.getAmount()));
                txtComment.setText(repaymentBeingEdited.getComments());
                // txtLoanNumber.setText(String.format("%d", repaymentBeingEdited.getLoanNo()));

                // Add the rest of the fields
                txtNewDateDue.setText(Utils.formatDate(repaymentBeingEdited.getNextDateDue()));
                txtBalance.setText(String.format("%.0f UGX", repaymentBeingEdited.getBalanceAfter()));
                txtNewInterest.setText(String.format("%.0f", repaymentBeingEdited.getInterestAmount()));
                txtTotal.setText(String.format("%.0f UGX", repaymentBeingEdited.getRolloverAmount()));
            }
        } else {
            txtLoanNumber.setText(null);

            // Show that Member has No Loan
            lblInstruction.setText("Member does not have an outstanding loan.");

            // Remove the widgets for capturing Loans
            LinearLayout parent = (LinearLayout) lblInstruction.getParent();

            // Remove LoanNo
            LinearLayout frmLoanNo = (LinearLayout) findViewById(R.id.frmMLRepayHLoanNo);
            parent.removeView(frmLoanNo);

            // Remove Amount
            TextView lblAmount = (TextView) findViewById(R.id.lblMLRepayHAmount);
            parent.removeView(lblAmount);
            LinearLayout frmAmount = (LinearLayout) findViewById(R.id.frmMLRepayHAmount);
            parent.removeView(frmAmount);

            // Remove Balance
            TextView lblBalanceSection = (TextView) findViewById(R.id.lblMLRepayHSection1);
            parent.removeView(lblBalanceSection);
            TextView lblBalance = (TextView) findViewById(R.id.lblMLRepayHBalance);
            parent.removeView(lblBalance);
            LinearLayout frmBalance = (LinearLayout) findViewById(R.id.frmMLRepayHBalance);
            parent.removeView(frmBalance);

            //Remove Instruction2
            TextView lblInstruction2 = (TextView) findViewById(R.id.lblMLRepayHInstruction2);
            parent.removeView(lblInstruction2);

            // Remove Interest
            TextView lblInterest = (TextView) findViewById(R.id.lblMLRepayHInterest);
            parent.removeView(lblInterest);
            TextView lblInterestDescription = (TextView) findViewById(R.id.lblMLRepayHInterestDesc);
            parent.removeView(lblInterestDescription);
            LinearLayout frmInterest = (LinearLayout) findViewById(R.id.frmMLRepayHInterest);
            parent.removeView(frmInterest);

            // Remove Total
            TextView lblTotal = (TextView) findViewById(R.id.lblMLRepayHTotal);
            parent.removeView(lblTotal);
            LinearLayout frmTotal = (LinearLayout) findViewById(R.id.frmMLRepayHTotal);
            parent.removeView(frmTotal);

            // Remove Comment
            TextView lblComment = (TextView) findViewById(R.id.lblMLRepayHComment);
            parent.removeView(lblComment);
            parent.removeView(txtComment);

            // Remove Date Due
            TextView lblNewDateDue = (TextView) findViewById(R.id.lblMLRepayHDateDue);
            parent.removeView(lblNewDateDue);
            parent.removeView(txtNewDateDue);

            // Remove History Section
            LinearLayout frmHistory = (LinearLayout) findViewById(R.id.frmMLRepayHHistory);
            parent.removeView(frmHistory);

            //TextView lblHistorySection = (TextView) findViewById(R.id.lblMLRepayHSection2);
            //parent.removeView(lblHistorySection);
            //TextView lblMLRepayHCycleSpan = (TextView) findViewById(R.id.lblMLRepayHCycleSpan);
            //parent.removeView(lblMLRepayHCycleSpan);
        }

        //Handle the Date stuff only when the fields are visible
        if (null != recentLoan) {

            //Date stuff
            txtDateDue = (TextView) findViewById(R.id.txtMLRepayHDateDue);
            viewClicked = txtDateDue;

            //If it is not an edit operation then initialize the date. Otherwise, retain the date pulled from db
            if (!isEditOperation) {
                initializeDate();
            } else {
                final Calendar c = Calendar.getInstance();
                String dtNextDateDue = txtDateDue.getText().toString().trim();
                Date nextDateDue = Utils.getDateFromString(dtNextDateDue, Utils.DATE_FIELD_FORMAT);
                c.setTime(nextDateDue);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }

            //Set onClick Listeners to load the DateDialog for MeetingDate
            txtDateDue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //I want the Event Handler to handle both startDate and endDate
                    viewClicked = (TextView) view;
                    DatePickerDialog datePickerDialog = new DatePickerDialog(MemberLoansRepaidHistoryActivity.this, mDateSetListener, mYear, mMonth, mDay);

                    //TODO: Enable this feature in API 11 and above
                    //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                    datePickerDialog.show();
                }
            });

            // Setup the Default Date. Not sure whether I should block this off when editing a loan repayment
            if (!isEditOperation) {

                //TODO: Set the default Date to be MeetingDate + 1Month, instead of using today's date
                final Calendar c = Calendar.getInstance();
                if (null != targetMeeting) {
                    c.setTime(targetMeeting.getMeetingDate());
                }
                c.add(Calendar.WEEK_OF_YEAR, 4);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                updateDisplay();
            }
            //end of date stuff
        }

        //  TextView txtOutstandingLoans = (TextView) findViewById(R.id.lblMLRepayHOutstandingLoans);
        // TextView txtPrinciple = (TextView) findViewById(R.id.lblMLRepayHPrinciple);
        // final TextView txtInterestOnLoan = (TextView) findViewById(R.id.lblMLRepayHInterestOnLoan);

        TextView txtCycleSpan = (TextView) findViewById(R.id.lblMLRepayHCycleSpan);

        double outstandingLoans = 0.0;
        MeetingLoanIssued loanIssue = new MeetingLoanIssued();
        if ((recentLoan != null) && (targetMeeting != null && targetMeeting.getVslaCycle() != null)) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            cycle = vslaCycleRepo.getCycle(targetCycleId);
            txtCycleSpan.setText(String.format("Cycle %s to %s", Utils.formatDate(cycle.getStartDate(), Utils.DATE_FIELD_FORMAT), Utils.formatDate(cycle.getEndDate(), Utils.DATE_FIELD_FORMAT)));
            // outstandingLoans = loanIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetCycleId, memberId);
            // loanIssue = loanIssuedRepo.getOutstandingLoansByMemberInCycle(targetCycleId, memberId);
        }
        // txtOutstandingLoans.setText(String.format("Outstanding \t %,.0f UGX", outstandingLoans));
        //  if (loanIssue != null) {
        //   txtPrinciple.setText(String.format("Principle \t\t %,.0f UGX", loanIssue.getPrincipalAmount()));
        // txtInterestOnLoan.setText(String.format("Interest \t\t %,.0f UGX", loanIssue.getInterestAmount()));
        //}

        // Populate the History
        populateLoanRepaymentHistory();

        // TODO: Check this
        if (null != recentLoan) {
            TextView txtLRAmount = (TextView) findViewById(R.id.txtMLRepayHAmount);
            txtLRAmount.requestFocus();
        } else {
            if (null != lblLoanNo) {
                lblLoanNo.setFocusable(true);
                lblLoanNo.requestFocus();
            }
        }

        // Handle the Auto-calculation of Rollover Amount. If recentLoan is NULL means fields are hidden
        if (null == recentLoan) {
            return;
        }
        // Handle the Loan Interest Computation
        editTextInterestRate = (EditText) findViewById(R.id.txtMLRepayHInterest);
        txtRolloverAmount = (TextView) findViewById(R.id.txtMLRepayHTotal);
        txtLoanBalance = (TextView) findViewById(R.id.txtMLRepayHBalance);

        // First get the Interest Rate for the Current Cycle
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            interestRate = targetMeeting.getVslaCycle().getInterestRate();
        }

        EditText txtRepaymentAmount = (EditText) findViewById(R.id.txtMLRepayHAmount);
        txtRepaymentAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Compute the Interest
                double theRepayAmount = 0.0;
                try {
                    if (s.toString().length() <= 0) {
                        lblMLRepayHCurrencyTotal.setVisibility(View.VISIBLE);
                        lblMLRepayHLBCurrency.setVisibility(View.VISIBLE);
                        txtLoanBalance.setText("");
                        txtRolloverAmount.setText("");
                        return;
                    }
                    theRepayAmount = Double.parseDouble(s.toString());
                } catch (Exception ex) {
                    return;
                }

                //Compute the Balance
                if (isEditOperation && null != repaymentBeingEdited) {
                    theCurLoanBalanceAmount = repaymentBeingEdited.getBalanceBefore() - theRepayAmount;
                } else {
                    theCurLoanBalanceAmount = recentLoan.getLoanBalance() - theRepayAmount;
                }
                txtLoanBalance.setText(String.format("%,.0f UGX", theCurLoanBalanceAmount));
                double interestAmount = 0;

                // If meeting date is before loan due date then default interest to 0
                if (targetMeeting.getMeetingDate().before(recentLoan.getDateDue())) {
                    editTextInterestRate.setText("0");
                } else {
                    interestAmount = (interestRate * 0.01 * theCurLoanBalanceAmount);
                    editTextInterestRate.setText(String.format("%.0f", interestAmount));
                }
                double rolloverAmount = theCurLoanBalanceAmount + interestAmount;
                txtRolloverAmount.setText(String.format("%,.0f UGX", rolloverAmount));

                // If balance = 0, then next due date field should be blank
                if (theCurLoanBalanceAmount == 0) {
                    String dtNextDateDue = txtDateDue.getText().toString().trim();
                    Date nextDateDue = Utils.getDateFromString(dtNextDateDue, Utils.DATE_FIELD_FORMAT);
                    if (nextDateDue.after(targetMeeting.getMeetingDate())) {
                        txtDateDue.setText("none");
                    } else {
                        txtDateDue.setText("");
                    }
                    lblMLRepayHCurrencyTotal.setVisibility(View.GONE);
                    lblMLRepayHLBCurrency.setVisibility(View.GONE);
                    txtDateDue.setEnabled(false);
                    editTextInterestRate.setEnabled(false);
                } else if (theCurLoanBalanceAmount < 0) {
                    double zeroInterest = 0.0;
                    double zeroLoanBalance = 0.0;
                    editTextInterestRate.setText(String.format("%.0f", zeroInterest));
                    txtRolloverAmount.setText(String.format("%,.0f UGX overpayment", Math.abs(theCurLoanBalanceAmount)));
                    txtLoanBalance.setText(String.format("%,.0f UGX", zeroLoanBalance));
                    txtDateDue.setText("none");
                    lblMLRepayHCurrencyTotal.setVisibility(View.GONE);
                    lblMLRepayHLBCurrency.setVisibility(View.GONE);
                }else if (theCurLoanBalanceAmount > 0){
                    lblMLRepayHCurrencyTotal.setVisibility(View.GONE);
                    lblMLRepayHLBCurrency.setVisibility(View.GONE);
                    txtDateDue.setText("there there");
                   updateDisplay();
                }
                // Have this value redundantly stored for future use
                theCurLoanRepayAmount = theRepayAmount;
            }
        });

        // Now deal with Loan Interest Manual Changes
        EditText txtNewInterestAmount = (EditText) findViewById(R.id.txtMLRepayHInterest);
        txtNewInterestAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Compute the Interest
                double theInterestAmount = 0.0;
                try {
                    if (s.toString().length() <= 0) {
                        return;
                    }
                    theInterestAmount = Double.parseDouble(s.toString());
                } catch (Exception ex) {
                    return;
                }

                double rolloverAmount = theInterestAmount + theCurLoanBalanceAmount;
                txtRolloverAmount.setText(String.format("%,.0f UGX", rolloverAmount));
                if (rolloverAmount < 0) {
                    txtRolloverAmount.setText(String.format("%,.0f UGX overpayment", rolloverAmount));
                    txtDateDue.setText("none");
                }
            }
        });

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
                        if (recentLoan == null) {
                            //Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment","The member does not have an outstanding loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "loansRepaid");
                            i.putExtra("_meetingDate", meetingDate);
                            i.putExtra("_meetingId", meetingId);
                            startActivity(i);
                            finish();
                        } else if (saveMemberLoanRepayment()) {
                            Toast.makeText(MemberLoansRepaidHistoryActivity.this, "Loan Repayment entered successfully", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                            i.putExtra("_tabToSelect", "loansRepaid");
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
                        i.putExtra("_tabToSelect", "loansRepaid");
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        startActivity(i);
                        finish();
                    }
                }
        );


        actionBar = getSupportActionBar();
        actionBar.setTitle("Repayments");

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
        /**  actionBar.setDisplayOptions(
         ActionBar.DISPLAY_SHOW_CUSTOM,
         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
         | ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView,
         new ActionBar.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT,
         ViewGroup.LayoutParams.MATCH_PARENT)); */
        // END_INCLUDE (inflate_set_custom_view)
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
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", "loansRepaid");
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
            case R.id.mnuMLRepayHCancel:
                i = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                i.putExtra("_tabToSelect", "loansRepaid");
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);
                return true;
            case R.id.mnuMLRepayHSave:
                if (recentLoan == null) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The member does not have an outstanding loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return true;
                }
                if (saveMemberLoanRepayment()) {
                    i = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect", "loansRepaid");
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
                return true;
        }
        return true;
    }

    private void populateLoanRepaymentHistory() {
        if (loansRepaidRepo == null) {
            loansRepaidRepo = new MeetingLoanRepaymentRepo(MemberLoansRepaidHistoryActivity.this);
        }
        loanRepayments = loansRepaidRepo.getLoansRepaymentsByMemberInCycle(targetCycleId, memberId);

        if (loanRepayments == null) {
            loanRepayments = new ArrayList<MemberLoanRepaymentRecord>();
        }

        if (loanRepayments.isEmpty()) {
            repaymentHistorySection.setVisibility(View.GONE);
        }
        //Now get the data via the adapter
        LoanRepaymentHistoryArrayAdapter adapter = new LoanRepaymentHistoryArrayAdapter(MemberLoansRepaidHistoryActivity.this, loanRepayments, "fonts/roboto-regular.ttf");

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    public boolean saveMemberLoanRepayment() {
        double theAmount = 0.0;

        try {
            if (recentLoan == null) {
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The member does not have an outstanding loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                return false;
            }

            TextView txtLoanAmount = (TextView) findViewById(R.id.txtMLRepayHAmount);
            TextView txtComments = (TextView) findViewById(R.id.txtMLRepayHComment);
            TextView txtBalance = (TextView) findViewById(R.id.txtMLRepayHBalance);
            TextView txtInterest = (TextView) findViewById(R.id.txtMLRepayHInterest);
            TextView txtRollover = (TextView) findViewById(R.id.txtMLRepayHTotal);
            TextView txtNextDateDue = (TextView) findViewById(R.id.txtMLRepayHDateDue);

            String amount = txtLoanAmount.getText().toString().trim();
            if (amount.length() < 1) {

                // Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The Loan Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtLoanAmount.requestFocus();
                // return false;
                //if savings is blank, default to 0
                txtLoanAmount.setText("0");
                amount = "0";
            }

            theAmount = Double.parseDouble(amount);
            if (theAmount < 0.00) {
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The Loan Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanAmount.requestFocus();
                return false;
            }

            double newBalance = 0.0;
            if (isEditOperation && null != repaymentBeingEdited) {
                newBalance = repaymentBeingEdited.getBalanceBefore() - theAmount;
            } else {
                newBalance = recentLoan.getLoanBalance() - theAmount;
            }
            double theInterest = 0.0;

            String interest = txtInterest.getText().toString().trim();
            if (interest.length() < 1) {
                theInterest = 0.0;
            } else {
                theInterest = Double.parseDouble(interest);
                if (theInterest < 0.00) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The Interest Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtInterest.requestFocus();
                    return false;
                }
            }

            double theRollover = newBalance + theInterest;

            //Next Due Date
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();

            Calendar calNext = Calendar.getInstance();
            //calNext.add(Calendar.MONTH,1);
            calNext.add(Calendar.WEEK_OF_YEAR, 4);
            Date theDateDue = calNext.getTime();

            //Check the date against the Meeting Date, not calendar date
            String nextDateDue = txtNextDateDue.getText().toString().trim();
            Date dtNextDateDue = Utils.getDateFromString(nextDateDue, Utils.DATE_FIELD_FORMAT);
            if (dtNextDateDue.before(targetMeeting.getMeetingDate())) {
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Loan Issue", "The due date has to be a future date.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtNextDateDue.setFocusable(true);
                txtDateDue.requestFocus();
                return false;
            } else {
                theDateDue = dtNextDateDue;
            }

            String comments = txtComments.getText().toString().trim();

            //Now Save the data
            if (null == loansRepaidRepo) {
                loansRepaidRepo = new MeetingLoanRepaymentRepo(MemberLoansRepaidHistoryActivity.this);
            }

            //retrieve the LoanId and LoanNo of the most recent uncleared loan
            int recentLoanId = 0;
            double balanceBefore = 0.0;
            Date dtLastDateDue = null;
            if (null != recentLoan) {
                recentLoanId = recentLoan.getLoanId();

                //If this is an edit then get the values from the repayment being edited
                if (isEditOperation && null != repaymentBeingEdited) {
                    balanceBefore = repaymentBeingEdited.getBalanceBefore();
                    dtLastDateDue = repaymentBeingEdited.getLastDateDue();
                } else {
                    balanceBefore = recentLoan.getLoanBalance();
                    // Last Date Due for Transaction Tracking purposes. Get it from the recent Loan
                    dtLastDateDue = recentLoan.getDateDue();
                }
            } else {
                //check again: Do not save repayment if there is no existing loan
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The member has no Outstanding Loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                return false;
            }

            //Check Over-Payments
            if (theAmount > balanceBefore) {
                double overPayment = theAmount - balanceBefore;
                Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Overpayment", "Overpayment of " + String.valueOf(overPayment) + " UGX. Payment made must not exceed " + String.valueOf(balanceBefore) + " UGX.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                return false;
            }

            //If Amount is Zero, then ensure that the date is due before doing a rollover
            if (theAmount == 0) {
                if (targetMeeting.getMeetingDate().before(recentLoan.getDateDue())) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment", "The repayment amount of zero (0 UGX) is not allowed when the loan is not yet due.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return false;
                }
            }

            //If it is an editing of existing loan repayment, first undo the changes of the former one
            boolean undoSucceeded = false;
            if (isEditOperation && repaymentBeingEdited != null) {
                //Post a Reversal or just edit the figures
                undoSucceeded = loanIssuedRepo.updateMemberLoanBalances(recentLoan.getLoanId(), recentLoan.getTotalRepaid() - repaymentBeingEdited.getAmount(), repaymentBeingEdited.getBalanceBefore(), repaymentBeingEdited.getLastDateDue());
            }

            //If it was an edit operation and undo changes failed, then exit
            if (isEditOperation && !undoSucceeded) {
                return false;
            }

            //Otherwise, proceed
            //saveMemberLoanRepayment(int meetingId, int memberId, int loanId, double amount, double balanceBefore, String comments, double balanceAfter,double interestAmount, double rolloverAmount, Date lastDateDue, Date nextDateDue)//
            boolean saveRepayment = loansRepaidRepo.saveMemberLoanRepayment(meetingId, memberId, recentLoanId, theAmount, balanceBefore, comments, newBalance, theInterest, theRollover, dtLastDateDue, dtNextDateDue);
            if (saveRepayment) {
                //Also update the balances
                if (loanIssuedRepo == null) {
                    loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansRepaidHistoryActivity.this);
                }

                //TODO: Decide whether to update the Interest Paid also: and whether it will be Cummulative Interest or Just current Interest
                //updateMemberLoanBalances(int loanId, double totalRepaid, double balance, Date newDateDue)
                return loanIssuedRepo.updateMemberLoanBalances(recentLoan.getLoanId(), recentLoan.getTotalRepaid() + theAmount, theRollover, theDateDue);

            } else {
                //Saving failed
                return false;
            }
        } catch (Exception ex) {
            Log.e("MemberLoansRepaidHistory.saveMemberLoanRepayment", ex.getMessage());
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
            // c.add(Calendar.MONTH,1);
            c.add(Calendar.WEEK_OF_YEAR, 4);
            /** mYear = c.get(Calendar.YEAR);
             mMonth = c.get(Calendar.MONTH);
             mDay = c.get(Calendar.DAY_OF_MONTH); */
            dateString = Utils.formatDate(c.getTime());
            viewClicked.setText(dateString);
        }
    }
}
