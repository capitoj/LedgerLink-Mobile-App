package org.applab.digitizingdata;

import android.os.Bundle;
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
import org.applab.digitizingdata.repo.MeetingRepo;

public class MeetingStartingCashFrag extends SherlockFragment {

    private int meetingId = 0;
    private double expectedStartingCash = 0.0;
    private boolean successFlg = false;
    private MeetingActivity parentActivity;
    private TextView txtActualCashInBox;
    private TextView txtActualCashInBoxComment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getSherlockActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        TextView lblActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        TextView lblCashTakenToBank = (TextView) getSherlockActivity().findViewById(R.id.lblCashTakenToBank);
        txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
        txtActualCashInBoxComment = (TextView) getSherlockActivity().findViewById(R.id.txtStartingCashComment);


        MeetingRepo meetingRepo = parentActivity.ledgerLinkApplication.getMeetingRepo();

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
            if (!"".equals(startingCash.getComment())) {
                txtActualCashInBoxComment.setText(String.format("%s", startingCash.getComment()));
            }
        }

        // Get the Cycle that contains previous meeting in order to get the expected starting Cash
        if (null != meetingRepo) {
            previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), meetingId);
        }

        // If there is a previous meeting get expected starting cash from there
        if (previousMeeting != null) {
            targetMeetingId = previousMeeting.getMeetingId();
            if (targetMeetingId != -1) {
                previousClosingCash = meetingRepo.getMeetingStartingCash(targetMeetingId);

                if (previousClosingCash != null) {
                    expectedStartingCash = previousClosingCash.getExpectedStartingCash();
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

    boolean saveStartingCash() {
        txtActualCashInBox = (TextView) getSherlockActivity().findViewById(R.id.txtActualStartingCash);
        if (parentActivity.isViewOnly()) {
            return false;
        }
        double theCashFromBox = expectedStartingCash;
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
            double theCashFromBank = 0.0;
            String comment = "";
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
            double theFinesPaid = 0.0;
            double totalCash = theCashFromBox + theCashFromBank + theFinesPaid;
            successFlg = parentActivity.ledgerLinkApplication.getMeetingRepo().updateStartingCash(meetingId, theCashFromBox, theCashFromBank, theFinesPaid, comment);

            populateStartingCash();
            return successFlg;
        } catch (Exception ex) {
            ex.printStackTrace();
            return successFlg;
        }
    }
}