package org.applab.ledgerlink;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;

import org.applab.ledgerlink.business_rules.VslaMeeting;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingStartingCash;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;

import java.nio.DoubleBuffer;

public class MeetingStartingCashFrag extends Fragment {

    private int meetingId = 0;
    private double expectedStartingCash = 0.0;
    private boolean successFlg = false;
    private MeetingActivity parentActivity;
    private TextView txtActualCashInBox;
    private TextView txtActualCashInBoxComment;
    private EditText txtCashFromBank;
    private EditText txtLoanFromBank;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getActivity();
        setHasOptionsMenu(true);
        context = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        ScrollView fragmentView = (ScrollView) inflater.inflate(R.layout.frag_meeting_starting_cash, container, false);
        return fragmentView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        String meetingDate = getActivity().getIntent().getStringExtra("_meetingDate");
        String title = getString(R.string.meeting);

        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = getString(R.string.send_data);
                break;
            case VIEW_MODE_READ_ONLY:
                title = getString(R.string.send_data);
                break;
            default:
                //  title="Meeting";
                break;
        }

        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        meetingId = getActivity().getIntent().getIntExtra("_meetingId", 0);
        Log.e(getString(R.string.previousmeeting), Integer.toString(meetingId));
        populateStartingCash();
    }

    /**
     * @Override public void onCreateOptionsMenu((Menu menu), com.actionbarsherlock.view.MenuInflater inflater) {
     * menu.clear();
     * // getActivity()().getMenuInflater().inflate(R.menu.meeting_starting_cash, menu);
     * super.onCreateOptionsMenu(menu, inflater);
     * }
     */

    @Override
    public void onPause() {
        super.onPause();
        //Save only if not in view only
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
        }
        if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            saveStartingCash();
        }
    }

    private void populateStartingCash() {

        TextView lblExpectedStartingCash = (TextView) getActivity().findViewById(R.id.lblExpectedStartingCash);
        //TextView lblActualCashInBox = (TextView) getActivity()().findViewById(R.id.lblTotalCashInBox);
        TextView lblCashTakenToBank = (TextView) getActivity().findViewById(R.id.lblCashTakenToBank);
        txtActualCashInBox = (TextView) getActivity().findViewById(R.id.txtActualStartingCash);
        txtCashFromBank = (EditText) getActivity().findViewById(R.id.txtCashFromBank);
        txtLoanFromBank = (EditText) getActivity().findViewById(R.id.txtLoanFromBank);
        txtActualCashInBoxComment = (TextView) getActivity().findViewById(R.id.txtStartingCashComment);
        MeetingRepo meetingRepo = new MeetingRepo(context, meetingId);


        // Lock fields in read-only mode
        //Do not invoke the event when in Read only Mode
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
            txtActualCashInBoxComment.setEnabled(false);
            txtActualCashInBoxComment.setClickable(false);
            //txtActualCashInBoxComment.setActivated(false);
            txtActualCashInBox.setEnabled(false);
            txtActualCashInBox.setClickable(false);
            //txtActualCashInBox.setActivated(false);

            txtActualCashInBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    }
                }
            });
            ((RelativeLayout) txtActualCashInBox.getParent()).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (parentActivity.isViewOnly()) {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        VslaCycle recentCycle = new VslaCycleRepo(context).getMostRecentCycle();
        Meeting previousMeeting = meetingRepo.getPreviousMeeting(recentCycle.getCycleId(), meetingId);
        Log.e("CurrentMeeting", String.valueOf(meetingId));

        expectedStartingCash = VslaMeeting.getTotalCashInBox(context, previousMeeting.getMeetingId());
        lblExpectedStartingCash.setText(String.format(getString(R.string.expected_starting_cash_inbox)+" %,.0f UGX", expectedStartingCash));

        VslaMeeting vslaMeeting = new VslaMeeting(context, previousMeeting.getMeetingId());

        lblCashTakenToBank.setText(String.format(getString(R.string.cash_taken_to_bank_x)+" %,.0f UGX", vslaMeeting.getCashSavedToBank()));

        // Get starting cash for current meeting
        MeetingStartingCash startingCash = meetingRepo.getStartingCash();
        if(startingCash != null){
            double actualStartingCash = startingCash.getActualStartingCash();
            double cashFromBank = startingCash.getCashSavedInBank();
            double loanFromBank = startingCash.getLoanFromBank();
            if(actualStartingCash > 0){
                txtActualCashInBox.setText(String.valueOf((int)actualStartingCash));
            }
            if(cashFromBank > 0){
                txtCashFromBank.setText(String.valueOf((int)cashFromBank));
            }
            if(loanFromBank > 0){
                txtLoanFromBank.setText(String.valueOf((int)loanFromBank));
            }
            txtActualCashInBoxComment.setText(startingCash.getComment());
        }
    }

    protected boolean saveStartingCash() {
        if(parentActivity.isViewOnly()) {
            return false;
        }
        txtActualCashInBox = (TextView) getActivity().findViewById(R.id.txtActualStartingCash);

        String cashFromBox = txtActualCashInBox.getText().toString();
        double theCashFromBox = 0.0;
        if(cashFromBox.length() < 1){
            //Assume that it is the same as the expected
            theCashFromBox = expectedStartingCash;
        }else{
            theCashFromBox = Double.parseDouble(cashFromBox);
            if(theCashFromBox < 0){
                Utils.createAlertDialogOk(getActivity().getBaseContext(), getString(R.string.meeting), getString(R.string.value_forcash_for_box_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtActualCashInBox.requestFocus();
                return false;
            }
        }

        txtCashFromBank = (EditText) getActivity().findViewById(R.id.txtCashFromBank);
        String cashFromBank = txtCashFromBank.getText().toString();
        double theCashFromBank = 0.0;
        if(cashFromBank.length() > 0){
            theCashFromBank = Double.parseDouble(cashFromBank);
            if(theCashFromBank < 0){
                Utils.createAlertDialogOk(getActivity().getBaseContext(), getString(R.string.meeting), getString(R.string.value_for_cash_withdrawn_bank_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtCashFromBank.requestFocus();
                return false;
            }
        }

        txtLoanFromBank = (EditText) getActivity().findViewById(R.id.txtLoanFromBank);
        String loanFromBank = txtLoanFromBank.getText().toString();
        double theLoanFromBank = 0.0;
        if(loanFromBank.length() > 0){
            theLoanFromBank = Double.parseDouble(loanFromBank);
            if(theLoanFromBank < 0){
                Utils.createAlertDialogOk(getActivity().getBaseContext(), getString(R.string.meeting), getString(R.string.value_for_loan_from_bank_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanFromBank.requestFocus();
                return false;
            }
        }

        txtActualCashInBoxComment = (TextView) getActivity().findViewById(R.id.txtStartingCashComment);
        String comment = txtActualCashInBoxComment.getText().toString();
        MeetingRepo meetingRepo = new MeetingRepo(context, meetingId);
        successFlg = meetingRepo.updateStartingCash(theCashFromBox, theCashFromBank, theLoanFromBank, comment);
        populateStartingCash();
        return successFlg;
    }
}