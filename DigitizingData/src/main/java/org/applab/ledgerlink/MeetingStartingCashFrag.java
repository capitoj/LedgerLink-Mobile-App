package org.applab.ledgerlink;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingStartingCash;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.nio.DoubleBuffer;

public class MeetingStartingCashFrag extends SherlockFragment {

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
        parentActivity = (MeetingActivity) getSherlockActivity();
        setHasOptionsMenu(true);
        context = getSherlockActivity().getApplicationContext();
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
                //  title="Meeting";
                break;
        }

        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        Log.e("PreviousMeeting", Integer.toString(meetingId));
        populateStartingCash();
    }

    /**
     * @Override public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
     * menu.clear();
     * // getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_starting_cash, menu);
     * super.onCreateOptionsMenu(menu, inflater);
     * }
     */

    @Override
    public void onPause() {
        super.onPause();
        //Save only if not in view only
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
        }
        if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            saveStartingCash();
        }
    }

    private void populateStartingCash() {

        TextView lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
        //TextView lblActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        TextView lblCashTakenToBank = (TextView) getSherlockActivity().findViewById(R.id.lblCashTakenToBank);
        txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
        txtCashFromBank = (EditText) getSherlockActivity().findViewById(R.id.txtCashFromBank);
        txtLoanFromBank = (EditText) getSherlockActivity().findViewById(R.id.txtLoanFromBank);
        txtActualCashInBoxComment = (TextView) getSherlockActivity().findViewById(R.id.txtStartingCashComment);
        MeetingRepo meetingRepo = new MeetingRepo(context, meetingId);


        // Lock fields in read-only mode
        //Do not invoke the event when in Read only Mode
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
            txtActualCashInBoxComment.setEnabled(false);
            txtActualCashInBoxComment.setClickable(false);
            //txtActualCashInBoxComment.setActivated(false);
            txtActualCashInBox.setEnabled(false);
            txtActualCashInBox.setClickable(false);
            //txtActualCashInBox.setActivated(false);

            txtActualCashInBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    }
                }
            });
            ((RelativeLayout) txtActualCashInBox.getParent()).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (parentActivity.isViewOnly()) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

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

            expectedStartingCash = startingCash.getExpectedStartingCash();

            /** If no previous meeting; i.e. fresh Start expected starting Cash = 0;
             *If GSW has recorded cash then the recorded cash should be shown here as a net

             targetMeetingId = currentMeeting.getMeetingId();

             if (targetMeetingId != -1) {
             startingCash = meetingRepo.getMeetingStartingCash(targetMeetingId);

             if (startingCash != null) {

             expectedStartingCash = startingCash.getExpectedStartingCash();
             Log.d("MSC exCah4", String.valueOf(expectedStartingCash));
             }
             }
             }*/
            Meeting previousMeeting = meetingRepo.getPreviousMeeting();
            double cashTakenToBank = previousMeeting.getClosingBalanceBank();

            lblExpectedStartingCash.setText(String.format("Expected Starting Cash In Box: %,.0f UGX", expectedStartingCash));
            //lblActualCashInBox.setText(String.format("Total Cash in Box: %,.0f UGX", actualStartingCash));
            lblCashTakenToBank.setText(String.format("Cash Taken to Bank: %,.0f UGX", cashTakenToBank));
        }
    }

    protected boolean saveStartingCash() {
        if(parentActivity.isViewOnly()) {
            return false;
        }
        txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);

        String cashFromBox = txtActualCashInBox.getText().toString();
        double theCashFromBox = 0.0;
        if(cashFromBox.length() < 1){
            //Assume that it is the same as the expected
            theCashFromBox = expectedStartingCash;
        }else{
            theCashFromBox = Double.parseDouble(cashFromBox);
            if(theCashFromBox < 0){
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash from Box is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtActualCashInBox.requestFocus();
                return false;
            }
        }

        txtCashFromBank = (EditText) getSherlockActivity().findViewById(R.id.txtCashFromBank);
        String cashFromBank = txtCashFromBank.getText().toString();
        double theCashFromBank = 0.0;
        if(cashFromBank.length() > 0){
            theCashFromBank = Double.parseDouble(cashFromBank);
            if(theCashFromBank < 0){
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash withdrawn from Bank is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtCashFromBank.requestFocus();
                return false;
            }
        }

        txtLoanFromBank = (EditText) getSherlockActivity().findViewById(R.id.txtLoanFromBank);
        String loanFromBank = txtLoanFromBank.getText().toString();
        double theLoanFromBank = 0.0;
        if(loanFromBank.length() > 0){
            theLoanFromBank = Double.parseDouble(loanFromBank);
            if(theLoanFromBank < 0){
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Loan from Bank is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtLoanFromBank.requestFocus();
                return false;
            }
        }

        txtActualCashInBoxComment = (TextView) getSherlockActivity().findViewById(R.id.txtStartingCashComment);
        String comment = txtActualCashInBoxComment.getText().toString();
        MeetingRepo meetingRepo = new MeetingRepo(context, meetingId);
        successFlg = meetingRepo.updateStartingCash(theCashFromBox, theCashFromBank, theLoanFromBank, comment);
        populateStartingCash();
        return successFlg;
    }
}