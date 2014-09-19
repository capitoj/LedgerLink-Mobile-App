package org.applab.digitizingdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

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

   /** @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        // getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_starting_cash, menu);
        super.onCreateOptionsMenu(menu, inflater);
    } */

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

        // Lock fields in read-only mode
        //Do not invoke the event when in Read only Mode
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
            txtActualCashInBoxComment.setEnabled(false);
            txtActualCashInBoxComment.setClickable(false);
            txtActualCashInBoxComment.setActivated(false);
            txtActualCashInBox.setEnabled(false);
            txtActualCashInBox.setClickable(false);
            txtActualCashInBox.setActivated(false);

            txtActualCashInBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
            ((RelativeLayout) txtActualCashInBox.getParent()).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (parentActivity.isViewOnly()) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        }

        //Get the Cycle that contains this meeting
        Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);

        // Initialize contributing metrics
        double totalSavings = 0.0;
        double totalLoansRepaid = 0.0;
        double totalLoansIssued = 0.0;
        double totalFines = 0.0;
        double actualStartingCash = 0.0;
        double cashTakenToBank = 0.0;
        MeetingStartingCash startingCash = null;
        MeetingStartingCash previousClosingCash = null;
        Meeting previousMeeting = null;
        int targetMeetingId = -1;

        // Get starting cash for current meeting
        startingCash = meetingRepo.getMeetingStartingCash(meetingId);
        actualStartingCash = startingCash.getActualStartingCash();
        cashTakenToBank = startingCash.getCashSavedInBank();

        if (startingCash.getActualStartingCash() != 0.0) {
            if ((int) (startingCash.getActualStartingCash()) > 0) {
                txtActualCashInBox.setText(String.format("%.0f", Double.valueOf(actualStartingCash)));
            }
            if (!startingCash.getComment().isEmpty()) {
                txtActualCashInBoxComment.setText(String.format("%s", startingCash.getComment()));
            }
        }

        // Get the Cycle that contains previous meeting in order to get the expected starting Cash
        if (null != meetingRepo) {
            previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), meetingId);
        }

        Log.d("MSC exCah1", String.valueOf(expectedStartingCash));

        // If there is a previous meeting get expected starting cash from there
        if (previousMeeting != null) {
            targetMeetingId = previousMeeting.getMeetingId();
            Log.d("MSC exCah2", String.valueOf(expectedStartingCash) + String.valueOf(targetMeetingId));
            if (targetMeetingId != -1) {
                previousClosingCash = meetingRepo.getMeetingStartingCash(targetMeetingId);

                if (previousClosingCash != null) {
                    Log.d("MSC exCah3", String.valueOf(expectedStartingCash));
                    expectedStartingCash = previousClosingCash.getExpectedStartingCash();
                    Log.d("MSC exCah", String.valueOf(expectedStartingCash));
                }
            }

        } else {
            expectedStartingCash = startingCash.getExpectedStartingCash();
        }

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

        lblExpectedStartingCash.setText(String.format("Expected Starting Cash: %,.0f UGX", expectedStartingCash));
        lblActualCashInBox.setText(String.format("Total Cash in Box: %,.0f UGX", actualStartingCash));
        lblCashTakenToBank.setText(String.format("Cash Taken to Bank: %,.0f UGX", cashTakenToBank));

    }

    public boolean saveStartingCash() {
        txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
        if (parentActivity.isViewOnly()) {
            return false;
        }
        theCashFromBox = expectedStartingCash;
        try {

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
                    // Utils.createAlertDialogOk(this, "Meeting", "The member number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show());
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

            Log.d("MSC exCah", String.valueOf(expectedStartingCash));

            // Now Save
            MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            totalCash = theCashFromBox + theCashFromBank + theFinesPaid;
            successFlg = meetingRepo.updateStartingCash(meetingId, theCashFromBox, theCashFromBank, theFinesPaid, comment);

            populateStartingCash();
            return successFlg;
        } catch (Exception ex) {
            Log.e("Meeting.saveStartingCash", ex.getMessage());
            return successFlg;
        }
    }
}
