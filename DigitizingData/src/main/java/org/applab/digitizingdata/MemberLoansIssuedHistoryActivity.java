package org.applab.digitizingdata;

import android.app.DatePickerDialog;
import android.app.Dialog;
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

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.LoansIssuedHistoryArrayAdapter;
import org.applab.digitizingdata.helpers.MemberLoanIssueRecord;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/12/13.
 */
public class MemberLoansIssuedHistoryActivity extends SherlockListActivity {
    private ActionBar actionBar;
    private String meetingDate;
    private int meetingId;
    private int memberId;
    private Meeting targetMeeting = null;
    VslaCycle cycle = null;
    private MeetingRepo meetingRepo = null;
    private MeetingLoanIssuedRepo loanIssuedRepo = null;
    private MeetingLoanRepaymentRepo loanRepaymentRepo = null;
    private VslaCycleRepo vslaCycleRepo = null;
    private ArrayList<MemberLoanIssueRecord> loansIssued;
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


        loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
        meetingRepo = new MeetingRepo(MemberLoansIssuedHistoryActivity.this);
        targetMeeting = meetingRepo.getMeetingById(meetingId);
        loanRepaymentRepo = new MeetingLoanRepaymentRepo((MemberLoansIssuedHistoryActivity.this));
        vslaCycleRepo = new VslaCycleRepo(MemberLoansIssuedHistoryActivity.this);

        //TextView txtOutstandingLoans = (TextView)findViewById(R.id.lblMLIssuedHOutstandingLoans);

        if (null == loanIssuedRepo) {
            loanIssuedRepo = new MeetingLoanIssuedRepo(getApplicationContext());
        }

        //Determine whether this is top up or edit on an existing Loan Repayment
        // Commented out for now as multiple loans are not yet enabled un comment to allow for issuing of multiple loans
        // MeetingLoanIssued memberLoan = loanIssuedRepo.getLoanIssuedToMemberInMeeting(meetingId, memberId);
        final MeetingLoanIssued memberLoan = loanIssuedRepo.getUnclearedLoanIssuedToMember(memberId);
        if (null != memberLoan) {
            // Flag that this is an edit operation
            isEditOperation = true;
            thePrincipalLoanAmount = memberLoan.getPrincipalAmount();
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
        populateLoanIssueHistory();

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
                                                         txtDateDue.setText("none");
                                                         lblCurrency.setVisibility(View.GONE);
                                                     } else {
                                                         lblCurrency.setVisibility(View.GONE);
                                                         updateDisplay();
                                                     }

                                                     double interestAmount = (interestRate * 0.01 * theAmount);
                                                     double totalAmount = theAmount + interestAmount;
                                                     if (isEditOperation) {

                                                         // Deal with topup
                                                         if (memberLoan.getMeeting().getMeetingId() != meetingId) {
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

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

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

        // END_INCLUDE (inflate_set_custom_view)
    }

    private void populateLoanIssueHistory() {

        if (loanIssuedRepo == null) {
            loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
        }

        loansIssued = loanIssuedRepo.getAllLoansIssuedToMember(memberId);

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
        double theBalance = 0.0;

        try {
            TextView txtLoanNo = (TextView) findViewById(R.id.txtMLIssuedHLoanNo);
            EditText txtLoanAmount = (EditText) findViewById(R.id.txtMLIssuedHAmount);
            TextView txtComment = (TextView) findViewById(R.id.txtMLIssuedHComment);
            TextView txtInterestAmount = (TextView) findViewById(R.id.txtMLIssuedHInterest);
            TextView txtDateDue = (TextView) findViewById(R.id.txtMLIssuedHDateDue);

            String amount = txtLoanAmount.getText().toString().trim();

            if (amount.length() < 1) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanAmount.requestFocus();
                return false;
            } else {
                theAmount = Double.parseDouble(amount);


                // Loan Amounts for new loans must be positive
                if (theAmount < 0.00 && currentLoanId > 0) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The Loan Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtLoanAmount.requestFocus();
                    return false;
                } else if (totalCashInBox > 0) {
                    if (theAmount > totalCashInBox) {
                        Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "You do not have enough money in the box to meet this loan amount.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                        txtLoanAmount.requestFocus();
                        return false;
                    }
                } else {
                    if (isEditOperation) {
                        if (theAmount < thePrincipalLoanAmount) {
                            Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "New Loan", "The new amount should be more than the original amount.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                            txtLoanAmount.requestFocus();
                            return false;
                        }


                    }
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

            // Interest Amount
            theInterestAmount = (interestRate * 0.01 * theAmount);
            String interestAmount = txtInterestAmount.getText().toString().trim();
            if (interestAmount.length() < 1) {

                // Not sure whether there would be more to do
                theInterestAmount = 0.0;
            } else {
                theInterestAmount = Double.parseDouble(interestAmount);
                if (theInterestAmount < 0.00) {
                    Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue", "The Interest Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtInterestAmount.requestFocus();
                    return false;
                }
            }

            //Date Due: Check it against the meeting date, not calendar date
            Date today = Calendar.getInstance().getTime();
            String dateDue = txtDateDue.getText().toString().trim();
            Date dtDateDue = Utils.getDateFromString(dateDue, Utils.DATE_FIELD_FORMAT);
            if (dtDateDue.before(targetMeeting.getMeetingDate()) || dtDateDue.equals(targetMeeting.getMeetingDate())) {
                Utils.createAlertDialogOk(MemberLoansIssuedHistoryActivity.this, "Loan Issue", "The due date has to be a future date.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtDateDue.requestFocus();
                return false;
            } else {
                theDateDue = dtDateDue;
            }

            //Now Save the data
            if (null == loanIssuedRepo) {
                loanIssuedRepo = new MeetingLoanIssuedRepo(MemberLoansIssuedHistoryActivity.this);
            }

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
                return loanIssuedRepo.deleteLoan(currentLoanId);
            }

            if (loanTopUp > 0.0) {
                boolean success = meetingRepo.updateTopUp(meetingId, loanTopUp);
            }

            return loanIssuedRepo.saveMemberLoanIssue(meetingId, memberId, theLoanNo, theAmount, theInterestAmount, theBalance, theDateDue, theComment, isEditOperation);
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