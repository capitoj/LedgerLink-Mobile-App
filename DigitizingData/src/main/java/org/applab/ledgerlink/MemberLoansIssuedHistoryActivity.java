package org.applab.ledgerlink;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.TextWatcher;
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
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LoansIssuedHistoryArrayAdapter;
import org.applab.ledgerlink.helpers.MemberLoanIssueRecord;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

/**
 * Created by Moses on 7/12/13.
 */
public class MemberLoansIssuedHistoryActivity extends ListActivity {
    private String meetingDate;
    private int meetingId;
    private int memberId;
    private Meeting targetMeeting = null;
    VslaCycle cycle = null;
    private EditText txtInterestAmount;
    private TextView txtTotalLoanAmount;
    private TextView lblCurrency;
    int targetCycleId = 0;
    private double interestRate = 0.0;
    private double theCurLoanAmount = 0.0;
    private double thePrincipalLoanAmount = 0.0;
    private int currentLoanId = 0;
    private boolean loanWasDeleted = false;
    private LinearLayout issuedHistorySection;
    private boolean isEditOperation = false;
    private double loanTopUp = 0.0;
    private double totalCashInBox = 0.0;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomActionBar();

        setContentView(R.layout.activity_member_loans_issued_history);

        // TextView lblMeetingDate = (TextView) findViewById(R.id.lblMLIssuedHMeetingDate);
        meetingDate = getIntent().getStringExtra("_meetingDate");
        //lblMeetingDate.setText(meetingDate);

        TextView lblFullNames = (TextView) findViewById(R.id.lblMLIssuedHFullName);
        String fullNames = getIntent().getStringExtra("_names");
        lblFullNames.setText(fullNames);

        issuedHistorySection = (LinearLayout) findViewById(R.id.frmMLIssuedHHistory);

        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        if (getIntent().hasExtra("_totalCashInBox")) {
            totalCashInBox = getIntent().getDoubleExtra("_totalCashInBox", 0);
        }

        targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        //TextView txtOutstandingLoans = (TextView)findViewById(R.id.lblMLIssuedHOutstandingLoans);

        //Determine whether this is top up or edit on an existing Loan Repayment
        // Commented out for now as multiple loans are not yet enabled un comment to allow for issuing of multiple loans
        // MeetingLoanIssued memberLoan = loanIssuedRepo.getLoanIssuedToMemberInMeeting(meetingId, memberId);
        final MeetingLoanIssued memberLoan = ledgerLinkApplication.getMeetingLoanIssuedRepo().getMemberLoan(getIntent().getIntExtra("_loanId", 0)); //.getUnclearedLoanIssuedToMember(memberId);
        if(getIntent().getIntExtra("_action", 0) == 1) {
            if (null != memberLoan) {
                // Flag that this is an edit operation
                isEditOperation = true;
                thePrincipalLoanAmount = memberLoan.getPrincipalAmount();
            }
        }

        TextView lblInterestDesc = (TextView) findViewById(R.id.lblMLIssuedHInterestDesc);
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            //targetCycleId = targetMeeting.getVslaCycle().getCycleId();
            //outstandingLoans = loanIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetCycleId, memberId);
            //cycle = vslaCycleRepo.getCycle(targetCycleId);
            // Get current cycle interest rate
            //lblInterestDesc.setText(String.format("at %.0f%% every 30 days", cycle.getInterestRate()));
            lblInterestDesc.setText(String.format("at %.0f%% every 30 days", targetMeeting.getVslaCycle().getInterestRate()));
        }

        if(getIntent().getIntExtra("_action", 0) == 1) {
            populateLoanIssueHistory();
        }

        TextView txtLILoanNo = (TextView) findViewById(R.id.txtMLIssuedHLoanNo);
        txtLILoanNo.setText("");
        txtLILoanNo.requestFocus();

        lblCurrency = (TextView) findViewById(R.id.lblMLIssuedHCurrencyTotal);
        final EditText txtLoanAmount = (EditText) findViewById(R.id.txtMLIssuedHAmount);

        //TextView txtDateDue = (TextView)findViewById(R.id.txtMLIssuedHDateDue);

        //Date stuff
        txtDateDue = (TextView) findViewById(R.id.txtMLIssuedHDateDue);
        viewClicked = txtDateDue;
        initializeDate();

        //Set onClick Listeners to load the DateDialog for MeetingDate
        txtDateDue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(MemberLoansIssuedHistoryActivity.this, mDateSetListener, mYear, mMonth, mDay);
                //TODO: Enable this feature in API 11 and above
                //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });


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
        updateDisplay();


        //Handle the Loan Interest Computation
        txtInterestAmount = (EditText) findViewById(R.id.txtMLIssuedHInterest);
        txtTotalLoanAmount = (TextView) findViewById(R.id.txtMLIssuedHTotal);

        //First get the Interest Rate for the Current Cycle
        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            interestRate = targetMeeting.getVslaCycle().getInterestRate();
        }

        txtLoanAmount.addTextChangedListener(new TextWatcher() {
                                                 @Override
                                                 public void afterTextChanged(Editable s) {

                                                 }

                                                 @Override
                                                 public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                 }

                                                 @Override
                                                 public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                     // Compute the Interest
                                                     double theAmount = 0.0;
                                                     try {
                                                         if (s.toString().length() <= 0) {
                                                             lblCurrency.setVisibility(View.VISIBLE);
                                                             txtTotalLoanAmount.setText("");
                                                             return;
                                                         }
                                                         theAmount = Double.parseDouble(s.toString());
                                                     } catch (Exception ex) {
                                                         return;
                                                     }

                                                     if (theAmount == 0.0) {
                                                         txtDateDue.setText(getString(R.string.none_main));
                                                         lblCurrency.setVisibility(View.GONE);
                                                     } else {
                                                         lblCurrency.setVisibility(View.GONE);
                                                         updateDisplay();
                                                     }

                                                     double interestAmount = (interestRate * 0.01 * theAmount);
                                                     double totalAmount = theAmount + interestAmount;
                                                     if (isEditOperation) {

                                                         // Deal with topup
                                                         if ((memberLoan != null ? memberLoan.getMeeting().getMeetingId() : 0) != meetingId) {
                                                             if (theAmount > memberLoan.getPrincipalAmount()) {
                                                                 loanTopUp = theAmount - memberLoan.getPrincipalAmount();
                                                                 interestAmount = (interestRate * 0.01 * loanTopUp) + memberLoan.getInterestAmount();
                                                                 /** Roll over?
                                                                  * if(memberLoan.getDateDue().compareTo((Utils.getDateFromString(meetingDate, Utils.DATE_FIELD_FORMAT))) <= 0) {
                                                                  interestAmount = (interestRate * 0.01 * loanTopUp) + memberLoan.getInterestAmount();

                                                                  } */
                                                                 totalAmount = (interestRate * 0.01 * loanTopUp) + memberLoan.getLoanBalance() + loanTopUp;

                                                             }
                                                             if (memberLoan.getPrincipalAmount() == theAmount) {
                                                                 interestAmount = memberLoan.getInterestAmount();
                                                                 totalAmount = memberLoan.getLoanBalance();
                                                                 txtDateDue.setText(Utils.formatDate(memberLoan.getDateDue(), "dd-MMM-yyyy"));
                                                             }
                                                         }


                                                     }

                                                     // Set new values
                                                     txtInterestAmount.setText(String.format("%.0f", interestAmount));
                                                     txtTotalLoanAmount.setText(String.format("%.0f UGX", totalAmount));

                                                     //Have this value redundantly stored for future use
                                                     theCurLoanAmount = theAmount;
                                                     // Store for later validation
                                                 }
                                             }
        );


        // Now deal with Loan Interest Manual Changes
        txtInterestAmount.addTextChangedListener(new

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

                                                                 double totalAmount = theInterestAmount + theCurLoanAmount;
                                                                 txtTotalLoanAmount.setText(String.format("%.0f UGX", totalAmount));
                                                             }
                                                         }
        );


        // Display Members current loan if any
       TextView txtComment = (TextView) findViewById(R.id.txtMLIssuedHComment);

        if(getIntent().getIntExtra("_action", 0) == 1) {
            if (null != memberLoan) {
                lblCurrency.setVisibility(View.GONE);
                currentLoanId = memberLoan.getLoanId();
                txtLILoanNo.setText(String.format("%d", memberLoan.getLoanNo()));
                txtLoanAmount.setText(String.format("%.0f", memberLoan.getPrincipalAmount()));
                txtComment.setText(String.format("%s", memberLoan.getComment()));
                txtInterestAmount.setText(String.format("%.0f", memberLoan.getInterestAmount()));
                txtDateDue.setText(Utils.formatDate(memberLoan.getDateDue(), "dd-MMM-yyyy"));

                // May consider just recomputing the total amount afresh
                //txtTotalAmount.setText(String.format("%.0f UGX",memberLoan.getLoanBalance()));
                txtTotalLoanAmount.setText(String.format("%.0f UGX", memberLoan.getLoanBalance()));
            }
        }
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
                        if (saveMemberLoan()) {
                            if (currentLoanId > 0) {
                                if (loanWasDeleted) {
                                    Toast.makeText(MemberLoansIssuedHistoryActivity.this, R.string.loan_been_cancelled, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MemberLoansIssuedHistoryActivity.this, R.string.loan_edited_successfully, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MemberLoansIssuedHistoryActivity.this, R.string.loan_issued_successfully, Toast.LENGTH_LONG).show();
                            }
                            goBackToLoansIssuedFragment();
                        }

                    }
                }
        );
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goBackToLoansIssuedFragment();
                    }
                }
        );


        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setTitle(R.string.new_loans);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        // END_INCLUDE (inflate_set_custom_view)
    }

    //Closes this fragment and goes back to the loans issued fragment
    private void goBackToLoansIssuedFragment() {
        /*
        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
        i.putExtra("_tabToSelect", "loansIssued");
        i.putExtra("_meetingDate", meetingDate);
        i.putExtra("_meetingId", meetingId);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(i);
        finish();*/
        Intent i = new Intent(this, MeetingMemberLoansIssueActivity.class);
        i.putExtra("_memberId", this.memberId);
        i.putExtra("_meetingId", this.meetingId);
        i.putExtra("_action", getString(R.string.loanissue));
        i.putExtra("_meetingDate", meetingDate);
        i.putExtra("_totalCashInBox", totalCashInBox);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void populateLoanIssueHistory() {

        ArrayList<MemberLoanIssueRecord> loansIssued = ledgerLinkApplication.getMeetingLoanIssuedRepo().getAllLoansIssuedToMember(memberId);

        if (loansIssued == null) {
            loansIssued = new ArrayList<MemberLoanIssueRecord>();
        }

        if (loansIssued.isEmpty()) {
            issuedHistorySection.setVisibility(View.GONE);
        }

        // Now get the data via the adapter
        LoansIssuedHistoryArrayAdapter adapter = new LoansIssuedHistoryArrayAdapter(MemberLoansIssuedHistoryActivity.this, loansIssued);

        //Assign Adapter to ListView
        setListAdapter(adapter);

        //Hack to ensure all Items in the List View are visible
        Utils.setListViewHeightBasedOnChildren(getListView());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.member_loans_issued_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MeetingActivity.class);
                upIntent.putExtra("_tabToSelect", getString(R.string.loanissued));
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
                i.putExtra("_tabToSelect", getString(R.string.loanissued));
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                startActivity(i);

                return true;
            case R.id.mnuMLIssuedHSave:
                if (saveMemberLoan()) {
                    i = new Intent(MemberLoansIssuedHistoryActivity.this, MeetingActivity.class);
                    i.putExtra("_tabToSelect", getString(R.string.loanissued));
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
        double theBalance = 0.0;

        try {
            TextView txtLoanNo = (TextView) findViewById(R.id.txtMLIssuedHLoanNo);
            EditText txtLoanAmount = (EditText) findViewById(R.id.txtMLIssuedHAmount);
            TextView txtComment = (TextView) findViewById(R.id.txtMLIssuedHComment);
            TextView txtInterestAmount = (TextView) findViewById(R.id.txtMLIssuedHInterest);
            TextView txtDateDue = (TextView) findViewById(R.id.txtMLIssuedHDateDue);

            String amount = txtLoanAmount.getText().toString().trim();

            if (amount.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.loan_amount_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanAmount.requestFocus();
                return false;
            } else {
                theAmount = Double.parseDouble(amount);


                // Loan Amounts for new loans must be positive
                if (theAmount < 0.00 && currentLoanId > 0) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.loan_amount_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanAmount.requestFocus();
                    return false;
                } else if (totalCashInBox > 0) {
                    if (theAmount > totalCashInBox) {
                        Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.no_enough_money_inbox_for_loan), Utils.MSGBOX_ICON_EXCLAMATION).show();
                        txtLoanAmount.requestFocus();
                        return false;
                    }
                } else {
                    if (isEditOperation) {
                        if (theAmount < thePrincipalLoanAmount) {
                            Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.new_amount_be_more_than_original_amount), Utils.MSGBOX_ICON_EXCLAMATION).show();
                            txtLoanAmount.requestFocus();
                            return false;
                        }


                    }
                }
            }
            String loanNo = txtLoanNo.getText().toString().trim();
            if (loanNo.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.loan_no_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            }

            theLoanNo = Integer.valueOf(loanNo);
            if(theLoanNo < 0.00){
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.loan_no_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            }

            MeetingRepo meetingRepo = new MeetingRepo(this, meetingId);
            int cycleID = meetingRepo.getMeeting().getVslaCycle().getCycleId();
            boolean hasLoanNo = MeetingLoanIssuedRepo.hasLoanNumber(this, cycleID, theLoanNo);
            if(hasLoanNo && theAmount > 0.00){
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.new_loan), getString(R.string.loan_no) + String.valueOf(theLoanNo) + " already exists.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanNo.requestFocus();
                return false;
            }

            String comment = txtComment.getText().toString().trim();
            if (comment.length() < 1) {
                theComment = "";
            } else {
                theComment = comment;
            }

            // Interest Amount
            theInterestAmount = (interestRate * 0.01 * theAmount);
            String interestAmount = txtInterestAmount.getText().toString().trim();
            if (interestAmount.length() < 1) {

                // Not sure whether there would be more to do
                theInterestAmount = 0.0;
            } else {
                theInterestAmount = Double.parseDouble(interestAmount);
                if (theInterestAmount < 0.00) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.loan_issue), getString(R.string.interest_amount_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtInterestAmount.requestFocus();
                    return false;
                }
            }

            //Date Due: Check it against the meeting date, not calendar date
            Date today = Calendar.getInstance().getTime();
            String dateDue = txtDateDue.getText().toString().trim();
            Date dtDateDue = Utils.getDateFromString(dateDue, Utils.DATE_FIELD_FORMAT);
            if (dtDateDue.before(targetMeeting.getMeetingDate()) || dtDateDue.equals(targetMeeting.getMeetingDate())) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, getString(R.string.loan_issue), getString(R.string.due_date_be_future_date), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtDateDue.requestFocus();
                return false;
            } else {
                theDateDue = dtDateDue;
            }

            //Now Save the data

            if (isEditOperation) {
                theBalance = Double.valueOf(txtTotalLoanAmount.getText().toString().trim().split(" ")[0]);
            } else {
                theBalance = theInterestAmount + theAmount;
            }
            /** Commented out because designer says too; uncomment if designer decides otherwise
             //Further Validation of Uniqueness of the Loan Number
             if (!loanIssuedRepo.validateLoanNumber(theLoanNo, meetingId, memberId)) {
             Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Number is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
             txtLoanNo.requestFocus();
             return false;
             } */

            // Determine whether to delete this current loan i.e. in case it is being edited and the amount is set to zero
            if (currentLoanId > 0 && theAmount <= 0) {
                //Mark flag to indicate that the loan was deleted
                loanWasDeleted = true;
                return ledgerLinkApplication.getMeetingLoanIssuedRepo().deleteLoan(currentLoanId);
            }

            if (loanTopUp > 0.0) {
                boolean success = ledgerLinkApplication.getMeetingRepo().updateTopUp(meetingId, loanTopUp);
            }

            return ledgerLinkApplication.getMeetingLoanIssuedRepo().saveMemberLoanIssue(meetingId, memberId, theLoanNo, theAmount, theInterestAmount, theBalance, theDateDue, theComment, isEditOperation);
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