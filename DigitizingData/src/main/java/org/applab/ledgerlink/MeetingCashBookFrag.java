package org.applab.ledgerlink;

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

import org.applab.ledgerlink.business_rules.VslaMeeting;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingStartingCash;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;
import org.applab.ledgerlink.repo.MeetingWelfareRepo;

@SuppressWarnings("ALL")
public class MeetingCashBookFrag extends SherlockFragment {

    private int meetingId = 0;
    double cashToBox = 0.0;
    private double totalCashInBox = 0.0;
    private MeetingSavingRepo savingRepo = null;
    private MeetingLoanRepaymentRepo repaymentRepo = null;
    private MeetingLoanIssuedRepo loanIssuedRepo = null;
    private MeetingFineRepo fineRepo = null;
    MeetingStartingCash currentStartingCashDetails = null;
    private MeetingActivity parentActivity; //to access parent meeting activity
    private EditText txtCashToBankAmount;
    private EditText txtBankLoanRepayment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentActivity = (MeetingActivity) getSherlockActivity();
        savingRepo = parentActivity.ledgerLinkApplication.getMeetingSavingRepo();
        loanIssuedRepo = parentActivity.ledgerLinkApplication.getMeetingLoanIssuedRepo();
        repaymentRepo = parentActivity.ledgerLinkApplication.getMeetingLoanRepaymentRepo();
        fineRepo = parentActivity.ledgerLinkApplication.getMeetingFineRepo();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_meeting_cash_book, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        String meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
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
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);


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
            try {
                updateCashBook();
                Toast.makeText(getSherlockActivity().getApplicationContext(), "The Cashbook balances have been saved successfully.", Toast.LENGTH_LONG).show();
            }catch (Exception e){
                Log.e("UpdateCashBook", e.getMessage());
            }
        }
    }

    //Modified by Joseph Capito 28/01/2016
    private void populateCashBookFields() {

        TextView lblTotalCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        TextView lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
        TextView lblActualStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblActualStartingCash);
        TextView lblCashDifference = (TextView) getSherlockActivity().findViewById(R.id.lblCashDifference);
        TextView lblCashFromBank = (TextView) getSherlockActivity().findViewById(R.id.lblCashFromBank);
        TextView lblLoanFromBank = (TextView) getSherlockActivity().findViewById(R.id.lblLoanFromBank);
        TextView lblCashBookComment = (TextView) getSherlockActivity().findViewById(R.id.lblCashBookComment);

        TextView lblSavings = (TextView) getSherlockActivity().findViewById(R.id.lblSavings);
        TextView lblLoanRepayments = (TextView) getSherlockActivity().findViewById(R.id.lblLoanRepayments);
        TextView lblFines = (TextView) getSherlockActivity().findViewById(R.id.lblFines);
        TextView lblNewLoans = (TextView) getSherlockActivity().findViewById(R.id.lblNewLoans);
        TextView lblWelfare = (TextView) getSherlockActivity().findViewById(R.id.lblWelfare);
        EditText txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
        EditText txtBankLoanRepayment = (EditText) getSherlockActivity().findViewById(R.id.txtBankLoanRepayment);

        if(parentActivity.isViewOnly()){
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
            txtCashToBankAmount.setEnabled(false);
            txtCashToBankAmount.setClickable(false);
            txtCashToBankAmount.setActivated(false);
        }
        txtCashToBankAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parentActivity.isViewOnly()){
                    Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                }
            }
        });

        txtBankLoanRepayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(parentActivity.isViewOnly()){
                    Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                }
            }
        });
        try{
            //Get the cycle that contains this meeting
            MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext(), meetingId);
            Meeting currentMeeting = meetingRepo.getMeeting();
            if(currentMeeting != null){
                //Get the cycle that contains the previous meeting in order to get starting cash.
                double expectedStartingCash = 0.0;
                MeetingStartingCash startingCash = meetingRepo.getStartingCash();
                expectedStartingCash = startingCash.getExpectedStartingCash();

                MeetingSavingRepo savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
                double totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);

                MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
                double totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);

                MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
                double totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);

                MeetingFineRepo meetingFineRepo = new MeetingFineRepo(getSherlockActivity().getApplicationContext());
                double totalFines = meetingFineRepo.getTotalFinesPaidInThisMeeting(meetingId);

                MeetingWelfareRepo meetingWelfareRepo = new MeetingWelfareRepo(getSherlockActivity().getApplicationContext());
                double totalWalfare = meetingWelfareRepo.getTotalWelfareInMeeting(meetingId);

                double loanFromBank = currentMeeting.getLoanFromBank();



                double actualStartingCash = currentMeeting.getOpeningBalanceBox();

                double cashFromBank = currentMeeting.getOpeningBalanceBank();

                //Todo: The cash to bank is the agreed amount of money that is to be taken to the bank in the current meeting
                double cashToBank = currentMeeting.getClosingBalanceBank();

                double bankLoanRepayment = currentMeeting.getBankLoanRepayment();

                double totalCashInBox = VslaMeeting.getTotalCashInBox(getSherlockActivity().getApplicationContext(), this.meetingId);

                String comment = currentMeeting.getComment();
                lblTotalCashInBox.setText(String.format("Total Cash In Box %s UGX", totalCashInBox));
                lblExpectedStartingCash.setText(String.format("Expected Starting Cash %s UGX", expectedStartingCash));
                lblActualStartingCash.setText(String.format("Actual Starting Cash %s UGX", actualStartingCash));
                lblCashDifference.setText(String.format("Difference %s UGX", expectedStartingCash - actualStartingCash));
                lblCashBookComment.setText(String.format("Comment %s", comment));
                lblSavings.setText(String.format("Savings %s", totalSavings));
                lblLoanRepayments.setText(String.format("Loan Repayment %s UGX", totalLoansRepaid));
                lblFines.setText(String.format("Fines %s UGX", totalFines));
                lblWelfare.setText(String.format("Welfare %s UGX", totalWalfare));
                lblNewLoans.setText(String.format("New Loans %s UGX", totalLoansIssued));
                lblCashFromBank.setText(String.format("Cash From Bank %s UGX", cashFromBank));
                lblLoanFromBank.setText(String.format("Loan From Bank %s UGX", loanFromBank));
                txtCashToBankAmount.setText(String.valueOf((int)cashToBank));
                txtBankLoanRepayment.setText(String.valueOf((int)bankLoanRepayment));
            }

        }catch (Exception e){
            Log.e("populateCashBookFields", e.getMessage());
        }
    }

    private void updateCashBook() {
        if (parentActivity.isViewOnly()) {
            return;
        }
        double theCashToBank = 0.0;
        txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
        String cashToBank = txtCashToBankAmount.getText().toString();
        if(cashToBank.length() > 0) {
            theCashToBank = Double.valueOf(cashToBank);
            if(theCashToBank < 0){
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash Book Box must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtCashToBankAmount.requestFocus();
                return;
            }
        }
        double theBankLoanRepayment = 0.0;
        txtBankLoanRepayment = (EditText)getSherlockActivity().findViewById(R.id.txtBankLoanRepayment);
        String bankLoanRepayment = txtBankLoanRepayment.getText().toString();
        if(bankLoanRepayment.length() > 0){
            theBankLoanRepayment = Double.valueOf(bankLoanRepayment);
            if(theBankLoanRepayment < 0){
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash Book Box must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtBankLoanRepayment.requestFocus();
                return;
            }
        }
        MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext(), meetingId);
        meetingRepo.updateCashBook(totalCashInBox, theCashToBank, theBankLoanRepayment);
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
