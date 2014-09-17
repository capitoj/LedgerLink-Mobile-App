package org.applab.digitizingdata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

        populateCashBookFields();
        parentActivity = (MeetingActivity) getSherlockActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Save only if not in view only
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), "Values for past meeting cannot be modified at this time", Toast.LENGTH_LONG).show();
        }

        //updateCashBook();
        Toast.makeText(getSherlockActivity().getApplicationContext(), "The Cashbook balances have been saved successfully.", Toast.LENGTH_LONG).show();
    }

    private void populateCashBookFields() {

        try {
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


            //Get the Cycle that contains this meeting
            Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);

            // Get the Cycle that contains previous meeting in order to get the expected starting Cash
            Meeting previousMeeting = null;
            if (null != meetingRepo) {
                previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), meetingId);
            }


            startingCashDetails = meetingRepo.getMeetingActualStartingCashDetails(meetingId);
            double expectedStartingCash = 0.0;

            double totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            double totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            double totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            double totalFines = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);

            double actualStartingCash = startingCashDetails.getActualStartingCash();
            expectedStartingCash = startingCashDetails.getExpectedStartingCash();
            cashToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(currentMeeting.getMeetingId());

            double totalCashOut = totalLoansIssued;
            double totalCashIn = actualStartingCash + totalSavings + totalLoansRepaid + totalFines;
            totalCashInBox = actualStartingCash + totalSavings + totalLoansRepaid - totalLoansIssued + totalFines - cashToBank;


            String comment = "";
            if (null != startingCashDetails.getComment()) {
                comment = startingCashDetails.getComment();
            }

            lblTotalCashInBox.setText(String.format("Total Cash In Box %,.0f UGX", totalCashInBox));
            lblExpectedStartingCash.setText(String.format("Expected Starting Cash %,.0f UGX", expectedStartingCash));
            lblActualStartingCash.setText(String.format("Actual Starting Cash %,.0f UGX", actualStartingCash));
            lblCashDifference.setText(String.format("Difference %,.0f UGX", expectedStartingCash - actualStartingCash));
            lblCashBookComment.setText(String.format("Comment %s", comment));

            lblSavings.setText(String.format("Savings %,.0f UGX", totalSavings));
            lblLoanRepayments.setText(String.format("Loan Repayment %,.0f UGX", totalLoansRepaid));
            lblFines.setText(String.format("Fines %,.0f UGX", totalFines));
            lblNewLoans.setText(String.format("New Loans %,.0f UGX", totalLoansIssued));

            txtCashToBankAmount.setText(String.format("%.0f", cashToBank));
        } catch (Exception ex) {

        } finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }

    private void updateCashBook() {

        if (validate()) {
            txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
            cashToBank = Double.valueOf(txtCashToBankAmount.getText().toString());

            //cashToBox = totalCashInBox - cashToBank;

            // double cashSavedInBank = startingCashDetails.getCashSavedInBank();
            // cashToBank = cashToBank + cashSavedInBank;
            if (meetingRepo == null) {
                meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            }
            meetingRepo.updateCashBook(meetingId, cashToBox, cashToBank);
        }
    }

    private boolean validate() {
        double theCashToBank = 0.0;
        // Validate: Fine Amount
        txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
        String cashBook = txtCashToBankAmount.getText().toString().trim();
        if (cashBook.length() < 1) {
            return true;
        } else {
            theCashToBank = Double.parseDouble(cashBook);
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
