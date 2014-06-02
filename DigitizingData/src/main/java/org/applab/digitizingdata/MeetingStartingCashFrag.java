package org.applab.digitizingdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.helpers.Utils;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_meeting_starting_cash, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = getSherlockActivity().getSupportActionBar();
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = String.format("Meeting    %s", meetingDate);

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
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        /** TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMOCFMeetingDate);
         lblMeetingDate.setText(meetingDate); */

        populateStartingCash();
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_starting_cash, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
            case R.id.mnuMOCFSave:
                saveStartingCash();
                /**  // TextView txtTotalCash = (TextView)getSherlockActivity().findViewById(R.id.txtMOCTotal);
                 TextView txtTotalCash = (TextView)getSherlockActivity().findViewById(R.id.txtActualStartingCash);
                 if(saveStartingCash()) {
                 Toast.makeText(getSherlockActivity().getApplicationContext(), "Starting Cash has been saved", Toast.LENGTH_LONG).show();

                 // txtTotalCash.setText(String.format("%,.0f UGX",totalCash));
                 txtTotalCash.setText(String.format("4 Mar 2014 Total Cash In Box %,.0f UGX", totalCash));
                 }
                 else {
                 Toast.makeText(getSherlockActivity().getApplicationContext(), "Starting Cash not saved", Toast.LENGTH_LONG).show();

                 //txtTotalCash.setText(String.format("%,.0f UGX",0));
                 txtTotalCash.setText(String.format("4 Mar 2014 Total Cash In Box %,.0f UGX", 0));
                 } */
                return true;
            default:
                return false;
        }
    }

    public boolean saveStartingCash() {
        boolean successFlg = false;
        double theCashFromBox = 0.0;
        double theCashFromBank = 0.0;
        double theFinesPaid = 0.0;
        String comment = "";

        try {
            TextView txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
            String amountBox = txtActualCashInBox.getText().toString().trim();
            if (amountBox.length() < 1) {
                //Allow it to be Zero
                theCashFromBox = 0.0;
            } else {
                theCashFromBox = Double.parseDouble(amountBox);
                if (theCashFromBox < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash from Box is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtActualCashInBox.requestFocus();
                    return false;
                }
            }

            TextView txtStartingCashComment = (TextView) getSherlockActivity().findViewById(R.id.txtStartingCashComment);
            String startingCashComment = txtStartingCashComment.getText().toString().trim();
            if (startingCashComment.length() < 1) {
                // Allow it to be Zero
                comment = "";
            } else {
                comment = startingCashComment;
                if (theCashFromBank < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash withdrawn from Bank is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtStartingCashComment.requestFocus();
                    return false;
                }
            }

            /** TextView txtCashFromBank = (TextView)getSherlockActivity().findViewById(R.id.txtMOCBalBfBank);
             String amountBank = txtCashFromBank.getText().toString().trim();
             if (amountBank.length() < 1) {
             //Allow it to be Zero
             theCashFromBank = 0.0;
             }
             else {
             theCashFromBank = Double.parseDouble(amountBank);
             if (theCashFromBank < 0.00) {
             Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting","The value for Cash withdrawn from Bank is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
             txtCashFromBank.requestFocus();
             return false;
             }
             }

             TextView txtFinesPaid = (TextView)getSherlockActivity().findViewById(R.id.txtMOCFines);
             String amountFines = txtFinesPaid.getText().toString().trim();
             if (amountFines.length() < 1) {
             //Allow it to be Zero
             theFinesPaid = 0.0;
             }
             else {
             theFinesPaid = Double.parseDouble(amountFines);
             if (theFinesPaid < 0.00) {
             Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting","The value for Fines Paid is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
             txtFinesPaid.requestFocus();
             return false;
             }
             } */


            //Now Save
            MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());

            totalCash = theCashFromBox + theCashFromBank + theFinesPaid;
            successFlg = meetingRepo.updateStartingCash(meetingId, theCashFromBox, theCashFromBank, theFinesPaid, comment);
            return successFlg;
        } catch (Exception ex) {
            Log.e("Meeting.saveStartingCash", ex.getMessage());
            return successFlg;
        }
    }

    private void populateStartingCash() {
        MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());

        TextView lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
        TextView lblActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        TextView lblCashTakenToBank = (TextView) getSherlockActivity().findViewById(R.id.lblCashTakenToBank);

        MeetingSavingRepo savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
        MeetingLoanIssuedRepo loansIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
        MeetingLoanRepaymentRepo loansRepaidRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());

        //Get the Cycle that contains this meeting
        Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);

        // Get the Cycle that contains previous meeting inorder to get the expected starting Cash
        Meeting previousMeeting = null;
        if (null != meetingRepo) {
            previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId());
        }


        // HashMap<String, Double> startingCash = meetingRepo.getMeetingStartingCash(meetingId);
        HashMap<String, Double> startingCash = meetingRepo.getMeetingStartingCash(previousMeeting.getMeetingId());


        if (null != startingCash) {

            // lblExpectedStartingCash.setText(String.format("Expected Starting Cash %.0f UGX", (startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BOX) - startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BANK))));
            double expectedStartingCash = meetingRepo.getMeetingTotalExpectedStartingCash(previousMeeting.getMeetingId());//meetingRepo.getMeetingTotalExpectedStartingCash(meetingId);
            lblExpectedStartingCash.setText(String.format("Expected Starting Cash %.0f UGX", expectedStartingCash));
            lblActualCashInBox.setText(String.format("Total Cash in Box %.0f UGX", startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BOX)));
            lblCashTakenToBank.setText(String.format("Cash Taken to Bank %.0f UGX", startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BANK)));

            /**  txtCashBox.setText(String.format("%.0f", startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BOX)));
             txtCashBank.setText(String.format("%.0f", startingCash.get(MeetingSchema.COL_MT_CASH_FROM_BANK)));
             txtFinesPaid.setText(String.format("%.0f", startingCash.get(MeetingSchema.COL_MT_CASH_FINES)));
             double cashTotal = 0.0;
             for(double value : startingCash.values()) {
             cashTotal += value;
             }
             txtCashTotal.setText(String.format("%,.0f UGX",cashTotal)); */
        }
        /** }
         else{
         // Assume all values are 0
         lblExpectedStartingCash.setText(String.format("Expected Starting Cash %.0f UGX", 0));
         lblActualCashInBox.setText(String.format("Total Cash in Box %.0f UGX", 0));
         lblCashTakenToBank.setText(String.format("Cash Taken to Bank %.0f UGX", 0));

         } */
    }
}
