package org.applab.ledgerlink;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
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
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.LoanRepaymentHistoryArrayAdapter;
import org.applab.ledgerlink.helpers.MemberLoanRepaymentRecord;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.fontutils.TypefaceManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

/**
 * Created by Moses on 7/13/13.
 */
public class MemberLoansRepaidHistoryActivity extends ListActivity {
    protected int loanId;
    private String meetingDate;
    private int meetingId;
    private int memberId;
    private int targetCycleId = 0;
    private Meeting targetMeeting = null;
    private MeetingLoanIssued recentLoan = null;

    private LinearLayout repaymentHistorySection;

    //Flags for Edit Operation
    private boolean isEditOperation = false;

    //This is the repayment that is being edited
    private MemberLoanRepaymentRecord repaymentBeingEdited = null;

    //Fields for Rollover calculation
    private double interestRate = 0.0;
    private EditText editTextInterestRate;
    private TextView txtRolloverAmount;
    private TextView txtLoanBalance;
    private double theCurLoanBalanceAmount = 0.0;
    private double theCurLoanRepayAmount = 0.0;

    //Date stuff
    private TextView txtDateDue;
    private TextView viewClicked;
    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private String dateString;

    LedgerLinkApplication ledgerLinkApplication;
    private TextView lblLoanNo;
    private TextView txtLoanNumber;
    private TextView txtLoanAmountFld;
    private TextView txtComment;
    private TextView txtBalance;
    private TextView txtNewInterest;
    private TextView txtTotal;
    private TextView txtNewDateDue;
    private TextView lblInstruction;
    private TextView lblMLRepayHLBCurrency;
    private TextView lblMLRepayHCurrencyTotal;
    private TextView lblMLRepayHInstruction;
    private TextView txtLoanAmount;
    private TextView txtComments;
    private TextView txtInterest;
    private TextView txtRollover;
    private TextView txtNextDateDue;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomActionBar();

        setContentView(R.layout.activity_member_loans_repaid_history);

        getFieldsFromLayout();

        /** TextView lblMeetingDate = (TextView)findViewById(R.id.lblMLRepayHMeetingDate);

         lblMeetingDate.setText(meetingDate); */
        try {
            meetingDate = getIntent().getStringExtra("_meetingDate");
            Log.e("MeetingDateException", meetingDate);
        }catch(Exception e){
            Log.e("MeetingdDateException", e.getMessage());
        }

        TextView lblFullName = (TextView) findViewById(R.id.lblMLRepayHFullName);
        String fullName = getIntent().getStringExtra("_names");
        lblFullName.setText(fullName);

        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        if(getIntent().hasExtra("_loanId")){
            this.loanId = getIntent().getIntExtra("_loanId", 0);
        }
        targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);
        recentLoan = ledgerLinkApplication.getMeetingLoanIssuedRepo().getMemberLoan(this.loanId);
    }

    private void getFieldsFromLayout() {
        lblLoanNo = (TextView) findViewById(R.id.lblMLRepayHLoanNo);
        txtLoanNumber = (TextView) findViewById(R.id.txtMLRepayHLoanNo);
        txtLoanAmountFld = (TextView) findViewById(R.id.txtMLRepayHAmount);
        txtComment = (TextView) findViewById(R.id.txtMLRepayHComment);
        txtBalance = (TextView) findViewById(R.id.txtMLRepayHBalance);
        txtNewInterest = (TextView) findViewById(R.id.txtMLRepayHInterest);
        txtTotal = (TextView) findViewById(R.id.txtMLRepayHTotal);
        txtNewDateDue = (TextView) findViewById(R.id.txtMLRepayHDateDue);
        lblInstruction = (TextView) findViewById(R.id.lblMLRepayHInstruction);
        lblMLRepayHLBCurrency = (TextView) findViewById(R.id.lblMLRepayHLBCurrency);
        lblMLRepayHCurrencyTotal = (TextView) findViewById(R.id.lblMLRepayHCurrencyTotal);
        lblMLRepayHInstruction = (TextView) findViewById(R.id.lblMLRepayHInstruction);

        txtLoanAmount = (TextView) findViewById(R.id.txtMLRepayHAmount);
        txtComments = (TextView) findViewById(R.id.txtMLRepayHComment);
        txtInterest = (TextView) findViewById(R.id.txtMLRepayHInterest);
        txtRollover = (TextView) findViewById(R.id.txtMLRepayHTotal);
        txtNextDateDue = (TextView) findViewById(R.id.txtMLRepayHDateDue);
    }

    @Override
    public void onResume() {
        super.onResume();
        showContent();
    }

    private void showContent() {
        if(isViewingSentData())
        {
            //Show sent data as per Nov wireframe V1.4 Page 24
            showSentDataContent();
            return;
        }

        //Determine whether this is an edit operation on an existing Loan Repayment
        repaymentBeingEdited = ledgerLinkApplication.getMeetingLoanRepaymentRepo().getLoansRepaymentByLoanInMeeting(this.loanId, this.meetingId); // ledgerLinkApplication.getMeetingLoanRepaymentRepo().getLoansRepaymentByMemberInMeeting(meetingId, memberId);
        if (null != repaymentBeingEdited) {
            //Flag that this is an edit operation
            isEditOperation = true;
        }

        // Get current cycle interest rate
        TextView lblInterestDesc = (TextView) findViewById(R.id.lblMLRepayHInterestDesc);

        // Get the Interest Rate for the Current Cycle
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            interestRate = targetMeeting.getVslaCycle().getInterestRate();
            lblInterestDesc.setText(String.format("at %.0f%% "+ getString(R.string.every_30_days), interestRate));
        }

        LinearLayout frmLoanRecord = (LinearLayout) findViewById(R.id.frmMLRepayHLoanRecord);
        // Get Loan Number of currently running loan

        repaymentHistorySection = (LinearLayout) findViewById(R.id.frmMLRepayHHistory);

        recentLoan = ledgerLinkApplication.getMeetingLoanIssuedRepo().getMemberLoan(getIntent().getIntExtra("_loanId", 0)); //.getAllMostRecentLoanIssuedToMember(memberId);
        StringBuilder sb = null;
        if (null != recentLoan) {
            txtLoanNumber.setText(String.format("%d", recentLoan.getLoanNo()));

            //Setup the Instruction
            sb = new StringBuilder(getString(R.string.if_payment_not_due_and_no_payment_made));
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

            if ((recentLoan.getDateCleared() != null) && (recentLoan.getDateCleared().compareTo((Utils.getDateFromString(meetingDate, Utils.OTHER_DATE_FIELD_FORMAT))) < 0)) {
                txtLoanNumber.setText(null);

                // Show that Member has No Loan
                lblInstruction.setText(R.string.member_doesnot_have_outstanding_loan);

                // Remove the widgets for capturing Loans
                frmLoanRecord.setVisibility(View.GONE);

            }

        } else {
            txtLoanNumber.setText(null);

            // Show that Member has No Loan
            lblInstruction.setText(R.string.member_doesnot_have_outstanding_loan);

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
            String dtNextDateDue = txtDateDue.getText().toString().trim();
            viewClicked = txtDateDue;

            // If it is not an edit operation then initialize the date. Otherwise, retain the date pulled from db
            if (!isEditOperation) {
                // If loan repayment is due
                Log.e("DateException", recentLoan.getDateDue() + " " + meetingDate);
//                if (recentLoan.getDateDue().compareTo((Utils.getDateFromString(meetingDate, Utils.OTHER_DATE_FIELD_FORMAT))) <= 0) {
//                    initializeDate();
//                }
                if (recentLoan.getDateDue().compareTo(targetMeeting.getMeetingDate()) <= 0) {
                    initializeDate();
                }

            } else {
                // String dtNextDateDue = txtDateDue.getText().toString().trim();
                final Calendar c = Calendar.getInstance();
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
                if (recentLoan.getDateDue().compareTo(targetMeeting.getMeetingDate()) <= 0) {

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
                } else {
                    txtNewDateDue.setText(Utils.formatDate(recentLoan.getDateDue()));

                }
            }
            //end of date stuff
        }

        //  TextView txtOutstandingLoans = (TextView) findViewById(R.id.lblMLRepayHOutstandingLoans);
        // TextView txtPrinciple = (TextView) findViewById(R.id.lblMLRepayHPrinciple);
        // final TextView txtInterestOnLoan = (TextView) findViewById(R.id.lblMLRepayHInterestOnLoan);

        TextView txtCycleSpan = (TextView) findViewById(R.id.lblMLRepayHCycleSpan);

        double outstandingLoans = 0.0;
        MeetingLoanIssued loanIssue = new MeetingLoanIssued();
        if ((recentLoan != null) && (targetMeeting != null && targetMeeting.getVslaCycle() != null))

        {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            VslaCycle cycle = ledgerLinkApplication.getVslaCycleRepo().getCycle(targetCycleId);
            txtCycleSpan.setText(String.format(getString(R.string.cycle)+" %s" +getString(R.string.to)+" %s", Utils.formatDate(cycle.getStartDate(), Utils.DATE_FIELD_FORMAT), Utils.formatDate(cycle.getEndDate(), Utils.DATE_FIELD_FORMAT)));
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
        if (null != recentLoan)

        {
            TextView txtLRAmount = (TextView) findViewById(R.id.txtMLRepayHAmount);
            txtLRAmount.requestFocus();
        } else

        {
            if (null != lblLoanNo) {
                lblLoanNo.setFocusable(true);
                lblLoanNo.requestFocus();
            }
        }

        // Handle the Auto-calculation of Rollover Amount. If recentLoan is NULL means fields are hidden
        if (null == recentLoan)

        {
            return;
        }
        // Handle the Loan Interest Computation
        editTextInterestRate = (EditText)

                findViewById(R.id.txtMLRepayHInterest);

        txtRolloverAmount = (TextView)

                findViewById(R.id.txtMLRepayHTotal);

        txtLoanBalance = (TextView)

                findViewById(R.id.txtMLRepayHBalance);

        EditText txtRepaymentAmount = (EditText) findViewById(R.id.txtMLRepayHAmount);
        txtRepaymentAmount.addTextChangedListener(new

          TextWatcher() {
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
                  Toast.makeText(MemberLoansRepaidHistoryActivity.this, String.valueOf(theCurLoanBalanceAmount), Toast.LENGTH_SHORT).show();
                  /*
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
                  } else if (theCurLoanBalanceAmount > 0) {
                      lblMLRepayHCurrencyTotal.setVisibility(View.GONE);
                      lblMLRepayHLBCurrency.setVisibility(View.GONE);
                      if (recentLoan.getDateDue().after(targetMeeting.getMeetingDate())) {
                          txtDateDue.setText(Utils.formatDate(recentLoan.getDateDue()));
                      } else {
                          initializeDate();
                      }
                  }
                  // Have this value redundantly stored for future use
                  theCurLoanRepayAmount = theRepayAmount;
                  */
              }
          }
        );

        // Now deal with Loan Interest Manual Changes
        EditText txtNewInterestAmount = (EditText) findViewById(R.id.txtMLRepayHInterest);
        txtNewInterestAmount.addTextChangedListener(new

            TextWatcher() {
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
                        txtRolloverAmount.setText(String.format("%,.0f UGX "+ getString(R.string.overpayment), rolloverAmount));
                        txtDateDue.setText(getString(R.string.none));
                    }
                }
            }
        );
    }


    private void showSentDataContent() {
        lblMLRepayHInstruction.setText(Utils.formatDate(targetMeeting.getMeetingDate())); //Set date in comments field
    }

    private void inflateCustomActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (recentLoan == null) {
                            //Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, "Repayment","The member does not have an outstanding loan.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                            Intent i = new Intent(getApplicationContext(), MeetingMemberLoansIssueActivity.class);
                            i.putExtra("_tabToSelect", getString(R.string.loansrepaid));
                            i.putExtra("_meetingDate", meetingDate);
                            i.putExtra("_meetingId", meetingId);
                            //startActivity(i);
                            finish();
                        } else if (saveMemberLoanRepayment()) {
                            Toast.makeText(MemberLoansRepaidHistoryActivity.this, R.string.loan_repayment_entered_successfully, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MeetingMemberLoansIssueActivity.class);
                            i.putExtra("_tabToSelect", getString(R.string.loansrepaid));
                            i.putExtra("_meetingDate", meetingDate);
                            i.putExtra("_meetingId", meetingId);
                            i.putExtra("_action", getString(R.string.loanrepayment));
                            i.putExtra("_memberId", memberId);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            //finish();
                        }

                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect",  getString(R.string.loansrepaid));
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        //startActivity(i);
                        finish();
                    }
                }
        );


        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setTitle(R.string.repayments);

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
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.member_loans_repaid_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect",  getString(R.string.loansrepaid));
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
                i.putExtra("_tabToSelect",  getString(R.string.loansrepaid));
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);
                return true;
            case R.id.mnuMLRepayHSave:
                if (recentLoan == null) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.repayment), getString(R.string.member_does_not_have_outstanding_loan), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return true;
                }
                if (saveMemberLoanRepayment()) {
                    i = new Intent(MemberLoansRepaidHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect",  getString(R.string.loansrepaid));
                    i.putExtra("_meetingDate", meetingDate);
                    i.putExtra("_meetingId", meetingId);
                    startActivity(i);
                }
                return true;
        }
        return true;
    }

    private void populateLoanRepaymentHistory() {
        ArrayList<MemberLoanRepaymentRecord> loanRepayments = ledgerLinkApplication.getMeetingLoanRepaymentRepo().getLoansRepaymentsByMemberInCycle(targetCycleId, memberId, loanId);

        if (loanRepayments == null) {
            loanRepayments = new ArrayList<MemberLoanRepaymentRecord>();
        }

        if (loanRepayments.isEmpty()) {
            repaymentHistorySection.setVisibility(View.GONE);
        }
        //Now get the data via the adapter
        LoanRepaymentHistoryArrayAdapter adapter = new LoanRepaymentHistoryArrayAdapter(MemberLoansRepaidHistoryActivity.this, loanRepayments);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }

    @SuppressWarnings("WeakerAccess")
    public boolean saveMemberLoanRepayment() {
        if (recentLoan == null) {
            Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.repayment), getString(R.string.member_does_not_have_outstanding_loan), Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }
        double theAmount = 0.0;
        String amount = txtLoanAmount.getText().toString().trim();
        if(amount.length() < 1){
            txtLoanAmount.setText("0");
            amount = "0";
        }
        try{
            theAmount = Double.parseDouble(amount);
        }catch(Exception ex){
            Log.e("SaveMemberLoanRepayment", ex.getMessage());
        }
        theAmount = Double.parseDouble(amount);
        if (theAmount < 0.00) {
            Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.repayment), getString(R.string.loan_amount_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
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
        try{
            theInterest = Double.parseDouble(interest);
        }catch(Exception ex){
            Log.e("SaveMemberLoanRepayment", ex.getMessage());
        }
        if (theInterest < 0.00) {
            Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.repayment), getString(R.string.interet_amount_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtInterest.requestFocus();
            return false;
        }

        double theRollover = newBalance + theInterest;

        //Next Due Date
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();

        Calendar calNext = Calendar.getInstance();
        //calNext.add(Calendar.MONTH,1);
        calNext.add(Calendar.WEEK_OF_YEAR, 4);
        Date theDateDue = calNext.getTime();

        // Check the date against the Meeting Date, not calendar date
        String nextDateDue = txtNextDateDue.getText().toString().trim();
        Date dtNextDateDue = Utils.getDateFromString(nextDateDue, Utils.DATE_FIELD_FORMAT);
        if (dtNextDateDue.before(targetMeeting.getMeetingDate())) {
            Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.loan_issue), getString(R.string.due_date_be_future_date), Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtNextDateDue.setFocusable(true);
            txtDateDue.requestFocus();
            return false;
        } else {
            theDateDue = dtNextDateDue;
        }

        String comments = txtComments.getText().toString().trim();

        //Now Save the data
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
            // check again: Do not save repayment if there is no existing loan
            Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.repayment), getString(R.string.member_no_outstanding_loan), Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }

        //Check Over-Payments
        if (theAmount > balanceBefore) {
            double overPayment = theAmount - balanceBefore;
            Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.overpayment), getString(R.string.overpayment_of) + String.valueOf(overPayment) + getString(R.string.ugx_payment_not_exceed) + String.valueOf(balanceBefore) + getString(R.string.ugx_), Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }

        //If Amount is Zero, then ensure that the date is due before doing a rollover
        if (theAmount == 0) {
            if (targetMeeting.getMeetingDate().before(recentLoan.getDateDue())) {
                if(repaymentBeingEdited.getAmount() == 0) {
                    Utils.createAlertDialogOk(MemberLoansRepaidHistoryActivity.this, getString(R.string.repayment), getString(R.string.repayment_amount_zero_not_allowed), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return false;
                }
            }
        }

        // If it is an editing of existing loan repayment, first undo the changes of the former one
        boolean undoSucceeded = false;
        if (isEditOperation && repaymentBeingEdited != null) {
            // Post a Reversal or just edit the figures
            undoSucceeded = ledgerLinkApplication.getMeetingLoanIssuedRepo().updateMemberLoanBalancesWithMeetingDate(recentLoan.getLoanId(), recentLoan.getTotalRepaid() - repaymentBeingEdited.getAmount(), repaymentBeingEdited.getBalanceBefore(), repaymentBeingEdited.getLastDateDue(), meetingDate);
        }

        Log.e("UndoSucceeded", String.valueOf(undoSucceeded));
        if (isEditOperation && !undoSucceeded) {
            return false;
        }

        boolean saveRepayment = ledgerLinkApplication.getMeetingLoanRepaymentRepo().saveMemberLoanRepayment(meetingId, memberId, recentLoanId, theAmount, balanceBefore, comments, newBalance, theInterest, theRollover, dtLastDateDue, dtNextDateDue);
        Log.e("SaveRepaymentM", String.valueOf(saveRepayment));
        boolean updateFlag = false;
        if (saveRepayment) {
            Log.e("SaveRepaymentX", String.valueOf(saveRepayment));
            //Also update the balances
            //TODO: Decide whether to update the Interest Paid also: and whether it will be Cummulative Interest or Just current Interest
            //updateMemberLoanBalances(int loanId, double totalRepaid, double balance, Date newDateDue)
            // return loanIssuedRepo.updateMemberLoanBalancesWithMeetingDate(recentLoan.getLoanId(), recentLoan.getTotalRepaid() + theAmount, theRollover, theDateDue, meetingDate);
            updateFlag = ledgerLinkApplication.getMeetingLoanIssuedRepo().updateMemberLoanBalancesWithMeetingDate(recentLoan.getLoanId(), recentLoan.getTotalRepaid() + theAmount, theRollover, recentLoan.getDateDue(), meetingDate);

        }

        return updateFlag;
    }

    //Indicates that we are viewing sent data
    public boolean isViewingSentData() {
        if (getIntent().hasExtra("_viewingSentData")) {
            return getIntent().getBooleanExtra("_viewingSentData", false);
        }
        return false;
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
