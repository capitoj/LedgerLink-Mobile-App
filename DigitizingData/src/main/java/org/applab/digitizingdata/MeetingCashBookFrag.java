package org.applab.digitizingdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

public class MeetingCashBookFrag extends SherlockFragment {

    ActionBar actionBar = null;
    String meetingDate = null;
    int meetingId = 0;
    double cashToBank = 0.0;
    double cashToBox = 0.0;
    double totalCashInBox = 0.0;
    MeetingRepo meetingRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingLoanRepaymentRepo repaymentRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    MeetingFineRepo fineRepo = null;
    MeetingStartingCash startingCashDetails = null;
    MeetingStartingCash currentStartingCashDetails = null;
    private MeetingActivity parentActivity; //to access parent meeting activity
    EditText txtCashToBankAmount;

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
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = "Meeting";
        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                //  title = "Meeting";
                break;
        }

        meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
        savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
        loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
        repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
        fineRepo = new MeetingFineRepo(getSherlockActivity().getApplicationContext());


        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        /**    TextView lblMeetingDate = (TextView) getSherlockActivity().findViewById(R.id.lblMCBFMeetingDate);
         meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */

        parentActivity = (MeetingActivity) getSherlockActivity();
        populateCashBookFields();

    }

    @Override
    public void onPause() {
        super.onPause();
        //Save only if not in view only
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), "Values for past meeting cannot be modified at this time", Toast.LENGTH_LONG).show();
        }
        if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            updateCashBook();
            Toast.makeText(getSherlockActivity().getApplicationContext(), "The Cashbook balances have been saved successfully.", Toast.LENGTH_LONG).show();

        }
    }

    private void populateCashBookFields() {

        TextView lblTotalCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        TextView lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
        TextView lblActualStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblActualStartingCash);
        TextView lblCashDifference = (TextView) getSherlockActivity().findViewById(R.id.lblCashDifference);
        TextView lblCashBookComment = (TextView) getSherlockActivity().findViewById(R.id.lblCashBookComment);

        TextView lblSavings = (TextView) getSherlockActivity().findViewById(R.id.lblSavings);
        TextView lblLoanRepayments = (TextView) getSherlockActivity().findViewById(R.id.lblLoanRepayments);
        TextView lblFines = (TextView) getSherlockActivity().findViewById(R.id.lblFines);
        TextView lblNewLoans = (TextView) getSherlockActivity().findViewById(R.id.lblNewLoans);
        EditText txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);

        // Lock fields in read-only mode
        //Do not invoke the event when in Read only Mode
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
            txtCashToBankAmount.setEnabled(false);
            txtCashToBankAmount.setClickable(false);
            txtCashToBankAmount.setActivated(false);

            txtCashToBankAmount.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
            ((LinearLayout) txtCashToBankAmount.getParent()).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (parentActivity.isViewOnly()) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        }
        try {
            //Get the Cycle that contains this meeting
            Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);

            // Get the Cycle that contains previous meeting in order to get the expected starting Cash
            Meeting previousMeeting = null;
            if (null != meetingRepo) {
                previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), meetingId);
            }

            // Get expectedStartingCash
            double expectedStartingCash = 0.0;
            startingCashDetails = meetingRepo.getMeetingStartingCash(previousMeeting.getMeetingId());

            expectedStartingCash = startingCashDetails.getExpectedStartingCash();
            Log.d("MCB expstc", String.valueOf(expectedStartingCash));

            // Get today's actuals
            //startingCashDetails = meetingRepo.getMeetingActualStartingCashDetails(meetingId);
            currentStartingCashDetails = meetingRepo.getMeetingStartingCash(meetingId);
            double actualStartingCash = currentStartingCashDetails.getActualStartingCash();
            // expectedStartingCash = startingCashDetails.getExpectedStartingCash();
            cashToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(currentMeeting.getMeetingId());
            Log.d("MCB3 acts", String.valueOf(cashToBank));

            double totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            double totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            double totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            double totalFines = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);

            double totalLoanTopUps = currentStartingCashDetails.getLoanTopUps();

            double totalCashOut = totalLoansIssued;
            double totalCashIn = actualStartingCash + totalSavings + totalLoansRepaid + totalFines;
            totalCashInBox = actualStartingCash + totalSavings + totalLoansRepaid - totalLoansIssued + totalFines + totalLoanTopUps - cashToBank;

            Log.d("MCB acts", String.valueOf(totalCashInBox));

            String comment = "";
            if (!currentStartingCashDetails.getComment().isEmpty()) {
                comment = currentStartingCashDetails.getComment();
            }
            Log.d("MCB acts", String.valueOf(totalCashInBox));
            lblTotalCashInBox.setText(String.format("Total Cash In Box %,.0f UGX", totalCashInBox));
            lblExpectedStartingCash.setText(String.format("Expected Starting Cash %,.0f UGX", expectedStartingCash));
            lblActualStartingCash.setText(String.format("Actual Starting Cash %,.0f UGX", actualStartingCash));
            lblCashDifference.setText(String.format("Difference %,.0f UGX", expectedStartingCash - actualStartingCash));
            lblCashBookComment.setText(String.format("Comment %s", comment));

            lblSavings.setText(String.format("Savings %,.0f UGX", totalSavings));
            lblLoanRepayments.setText(String.format("Loan Repayment %,.0f UGX", totalLoansRepaid));
            lblFines.setText(String.format("Fines %,.0f UGX", totalFines));
            lblNewLoans.setText(String.format("New Loans %,.0f UGX", totalLoansIssued + totalLoanTopUps));

            txtCashToBankAmount.setText(String.format("%.0f", cashToBank));
        } catch (Exception ex) {

        } finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }

    private void updateCashBook() {
        if (parentActivity.isViewOnly()) {
            return;
        }
        double theCashToBank = 0.0;
        if (validate()) {
            txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
            theCashToBank = Double.valueOf(txtCashToBankAmount.getText().toString());

            Log.d("MCB2 acts", String.valueOf(totalCashInBox));
            Log.d("MCB3 acts", String.valueOf(theCashToBank));
            //cashToBox = totalCashInBox - cashToBank;

            // double cashSavedInBank = startingCashDetails.getCashSavedInBank();
            // cashToBank = cashToBank + cashSavedInBank;
            if (meetingRepo == null) {
                meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            }
            meetingRepo.updateCashBook(meetingId, totalCashInBox, theCashToBank);
        }
    }

    private boolean validate() {
        // Validate: Fine Amount
        txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
        String cashBook = txtCashToBankAmount.getText().toString().trim();
        if (cashBook.length() < 1) {
            return true;
        } else {
            double theCashToBank = Double.parseDouble(cashBook);
            if (theCashToBank < 0) {
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash Book Box must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtCashToBankAmount.requestFocus();
                return false;

            } else {
                return true;
            }
        }
    }
}
