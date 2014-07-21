package org.applab.digitizingdata;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.util.HashMap;

 public class MeetingStartingCashFrag extends SherlockFragment {

    ActionBar actionBar = null;
    String meetingDate = null;
    int meetingId = 0;
    double totalCash = 0.0;
    double expectedStartingCash = 0.0;
    double theCashFromBox = 0.0;
    double theCashFromBank = 0.0;
    double theFinesPaid = 0.0;
    String comment = "";
    boolean successFlg = false;
    private MeetingActivity parentActivity;
    TextView txtActualCashInBox;
    TextView txtActualCashInBoxComment;
    TextView lblExpectedStartingCash;
    TextView lblActualCashInBox;
    TextView lblCashTakenToBank;
    MeetingRepo meetingRepo;
    MeetingSavingRepo savingRepo;
    MeetingFineRepo fineRepo;
    MeetingLoanIssuedRepo loanIssuedRepo;
    MeetingLoanRepaymentRepo loanRepaymentRepo;
    ScrollView fragmentView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        fragmentView = (ScrollView) inflater.inflate(R.layout.frag_meeting_starting_cash, container, false);
        return fragmentView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

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
                //  title="Meeting";
                break;
        }

        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        parentActivity = (MeetingActivity) getSherlockActivity();
        populateStartingCash();
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
       // getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_starting_cash, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Save only if not in view only
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
        }
        saveStartingCash();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return false;
            /**  case R.id.mnuSMDSend:
             return false;
             case R.id.mnuSMDCancel:
             return false; */
            case R.id.mnuMCBFSave:
                return false;
            default:
                return false;
        }
    }

    private void populateStartingCash() {

        lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
        lblActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        lblCashTakenToBank = (TextView) getSherlockActivity().findViewById(R.id.lblCashTakenToBank);

        txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
        txtActualCashInBoxComment = (TextView) getSherlockActivity().findViewById(R.id.txtStartingCashComment);


        meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
        savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
        fineRepo = new MeetingFineRepo(getSherlockActivity().getApplicationContext());
        loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
        loanRepaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());


        //Get the Cycle that contains this meeting
        Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);

        // Initialize contributing metrics
        double totalSavings = 0.0;
        double totalLoansRepaid = 0.0;
        double totalLoansIssued = 0.0;
        double totalFines = 0.0;
        double cashTakenToBank = 0.0;
        double totalCashOutThisCycle = 0.0;
        double totalCashInThisCycle = 0.0;
        double netCashThisMeeting = 0.0;
        MeetingStartingCash startingCash = null;
        Meeting previousMeeting = null;


        // Get the Cycle that contains previous meeting in order to get the expected starting Cash
        if (null != meetingRepo) {
            previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId());
        }

        // If there is a previous meeting
        if (previousMeeting != null) {

            //Setup the Total Savings
            totalSavings = savingRepo.getTotalSavingsInCycle(previousMeeting.getVslaCycle().getCycleId());
            totalLoansRepaid = loanRepaymentRepo.getTotalLoansRepaidInCycle(previousMeeting.getVslaCycle().getCycleId());
            totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInCycle(previousMeeting.getVslaCycle().getCycleId());
            totalFines = fineRepo.getTotalFinesPaidInCycle(previousMeeting.getVslaCycle().getCycleId());
            totalCashOutThisCycle = totalLoansIssued;
            totalCashInThisCycle = totalSavings + totalLoansRepaid + totalFines;

            // Cash taken to bank in the previous meeting
            // cashTakenToBank = meetingRepo.getTotalCashToBankInCycle(previousMeeting.getVslaCycle().getCycleId());
            cashTakenToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(previousMeeting.getMeetingId());
        }

        /** If no previous meeting; i.e. fresh Start expected starting Cash = 0;
         *If GSW has recorded cash then the recorded cash should be shown here as a net
         */
        Log.d("MeetingStartingCashFragTryNet", String.valueOf(netCashThisMeeting));
        Log.d("MeetingStartingCashFragTryIn", String.valueOf(totalCashInThisCycle));
        Log.d("MeetingStartingCashFragTryOut", String.valueOf(totalCashOutThisCycle));
        netCashThisMeeting = savingRepo.getTotalSavingsInMeeting(meetingId) + loanRepaymentRepo.getTotalLoansRepaidInMeeting(meetingId) + fineRepo.getTotalFinesInMeeting(meetingId) - loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
        expectedStartingCash = totalCashInThisCycle - totalCashOutThisCycle - netCashThisMeeting;

        /**  try {
         // successFlg = meetingRepo.updateStartingCash(meetingId, theCashFromBox, expectedStartingCash, theCashFromBank, theFinesPaid, comment);
         Log.d("MeetingStartingCashFragTry", String.valueOf(startingCash.getExpectedStartingCash()));
         Log.d("MeetingStartingCashFragTry", String.valueOf(startingCash.getActualStartingCash()));

         } catch (Exception ex) {
         Log.e("Meeting.populateStartingCash", ex.getMessage());

         } */

        // If starting cash is already saved then prepopulate
        startingCash = meetingRepo.getMeetingStartingCash(meetingId);
        if (null != startingCash) {
            // lblExpectedStartingCash.setText(String.format("Expected Starting Cash %.0f UGX", (startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BOX) - startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BANK))));
            //expectedStartingCash = startingCash.get(MeetingSchema.COL_MT_CASH_SAVED_BOX);
            // expectedStartingCash = startingCash.getExpectedStartingCash();
            if ((int) (startingCash.getActualStartingCash()) > 0) {
                txtActualCashInBox.setText(String.format("%.0f", Double.valueOf(startingCash.getActualStartingCash())));
                txtActualCashInBoxComment.setText(String.format("%s", startingCash.getComment()));
            }
        }

        lblExpectedStartingCash.setText(String.format("Expected Starting: Cash %,.0f UGX", expectedStartingCash));
        lblActualCashInBox.setText(String.format("Total Cash in Box: %,.0f UGX", startingCash.getActualStartingCash()));
        lblCashTakenToBank.setText(String.format("Cash Taken to Bank: %,.0f UGX", cashTakenToBank));

    }

    public boolean saveStartingCash() {

        try {
            txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
            String amountBox = txtActualCashInBox.getText().toString().trim();
            if (amountBox.length() < 1) {
                //Allow it to be Zero
                //theCashFromBox = 0.0;
                // Assume its same as Expected
                theCashFromBox = expectedStartingCash;
            } else {
                theCashFromBox = Double.parseDouble(amountBox);
                if (theCashFromBox < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash from Box is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtActualCashInBox.requestFocus();
                    return false;
                }
            }

            txtActualCashInBoxComment = (TextView) getSherlockActivity().findViewById(R.id.txtStartingCashComment);
            String startingCashComment = txtActualCashInBoxComment.getText().toString().trim();
            if (startingCashComment.length() < 1) {

                // Allow it to be nothing
                comment = "";
            } else {
                comment = startingCashComment;
                if (theCashFromBank < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash withdrawn from Bank is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtActualCashInBoxComment.requestFocus();
                    return false;
                }
            }

            // Now Save
            MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            totalCash = theCashFromBox + theCashFromBank + theFinesPaid;
            successFlg = meetingRepo.updateStartingCash(meetingId, theCashFromBox, expectedStartingCash, theCashFromBank, theFinesPaid, comment);

            populateStartingCash();
            return successFlg;
        } catch (Exception ex) {
            Log.e("Meeting.saveStartingCash", ex.getMessage());
            return successFlg;
        }
    }
}
