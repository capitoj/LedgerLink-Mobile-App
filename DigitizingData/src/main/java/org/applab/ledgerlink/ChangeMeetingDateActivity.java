package org.applab.ledgerlink;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChangeMeetingDateActivity extends ActionBarActivity{
    private int meetingId = 0;
    LedgerLinkApplication ledgerLinkApplication;

    private TextView viewClicked;
    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private String dateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = new LedgerLinkApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_change_meeting_date);
        inflateCustomActionBar();


        if(getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        //Retrieve the target Meeting
        Meeting targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        //Manage Dates
        TextView txtMeetingDate = (TextView) findViewById(R.id.txtCMDMeetingDate);
        viewClicked = txtMeetingDate;

        //Set onClick Listeners to load the DateDialog for MeetingDate
        txtMeetingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(ChangeMeetingDateActivity.this, mDateSetListener, mYear, mMonth, mDay);
                //TODO: Enable this feature in API 11 and above
                //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        //Setup the Default Date to be the current meeting date
        final Calendar c = Calendar.getInstance();
        if (null != targetMeeting) {
            //txtMeetingDate.setText(String.format("%s", Utils.formatDate(targetMeeting.getMeetingDate())));
            c.setTime(targetMeeting.getMeetingDate());
        }

        //Set the date parts
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay();
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
                        boolean cannotBeModified = false;
                        Date oldMeetingDate = null;

                        Meeting targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);
                        if(null == targetMeeting) {
                            //The meeting was not retrieved.
                            //TODO: Figure out a better way of handling this.
                            finish();

                            return;
                        }

                        //Check whether there are Repayments attached to the target meeting
                        //will require a look into the repayments
                        if(ledgerLinkApplication.getMeetingLoanRepaymentRepo().getTotalLoansRepaidInMeeting(meetingId) > 0.0D) {
                            cannotBeModified = true;
                        }

                        //Check whether there are Loans attached to the target meeting
                        //will require a look into the loans issued
                        if(ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInMeeting(meetingId) > 0.0D) {
                            cannotBeModified = true;
                        }

                        //Only allow editing if the meeting is the most recent or it is a past meeting without data
                        //Check whether this meeting is the most recent meeting in the current cycle
                        //If not, then don't allow deleting coz it will mess up loans
                        //TODO: Figure out how to cascade Loan info when a meet date is edited
                        if(targetMeeting.getVslaCycle() != null) {
                            Meeting mostRecent = ledgerLinkApplication.getMeetingRepo().getMostRecentMeetingInCycle(targetMeeting.getVslaCycle().getCycleId());
                            if(mostRecent.getMeetingId() == meetingId) {
                                cannotBeModified = false;
                            }
                            else {
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.sorry_you_may_only_edit_day_of_recent_meeting) + "%s.", Utils.formatDate(mostRecent.getMeetingDate())), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        if(cannotBeModified) {
                            Toast.makeText(getApplicationContext(),getString(R.string.meeting_date_cannot_be_modified),Toast.LENGTH_LONG).show();
                            finish();

                        }
                        else{
                            //TODO: Cascade the date modification to loans and fines.
                            //get the Old Date for use to adjust Loans and Fines
                            oldMeetingDate = targetMeeting.getMeetingDate();
                            if(validateMeetingDate(targetMeeting)) {
                                ledgerLinkApplication.getMeetingRepo().updateMeetingDate(meetingId, targetMeeting.getMeetingDate());
                                Toast.makeText(getApplicationContext(),getString(R.string.meeting_date_been_modified),Toast.LENGTH_LONG).show();

                                Intent i = new Intent(getApplicationContext(),BeginMeetingActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }

                        }
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });


        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setTitle(getString(R.string.change_meeting_date));

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
        if(viewClicked != null) {
            dateString = (new StringBuilder()
                    // Month is 0 based so add 1
                    .append(String.format("%02d",mDay))
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)).toString();
            viewClicked.setText(dateString);
        }
        else {
            //Not sure yet on what to do
        }
    }

    private void initializeDate(){
        if(viewClicked != null) {
            dateString = Utils.formatDate(new Date());
            viewClicked.setText(dateString);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.delete_meeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private boolean validateMeetingDate(Meeting meeting){
        try {

            if(null == meeting) {
                return false;
            }

            //Validate the Meeting Date
            TextView txtMeetingDate = (TextView)findViewById(R.id.txtCMDMeetingDate);
            String meetingDate = txtMeetingDate.getText().toString().trim();
            Date dt = Utils.getDateFromString(meetingDate,Utils.DATE_FIELD_FORMAT);

            if (dt.after(new Date())) {
                Utils.createAlertDialogOk(ChangeMeetingDateActivity.this,getString(R.string.change_meeting_date), getString(R.string.meeting_date_cannot_be_in_future), Utils.MSGBOX_ICON_EXCLAMATION).show();
                return false;
            }
            else {
                meeting.setMeetingDate(dt);
            }

            //Further Validations
            //check that meeting is with the boundaries of the current cycle
            if(meeting.getMeetingDate().before(meeting.getVslaCycle().getStartDate()) || meeting.getMeetingDate().after(meeting.getVslaCycle().getEndDate())) {
                Utils.createAlertDialogOk(ChangeMeetingDateActivity.this,getString(R.string.change_meeting_date),
                        String.format(getString(R.string.meeting_date_within_current_cycle)+" i.e. %s" + " " + getString(R.string.and) + " " + "%s",Utils.formatDate(meeting.getVslaCycle().getStartDate()), Utils.formatDate(meeting.getVslaCycle().getEndDate())),
                        Utils.MSGBOX_ICON_EXCLAMATION).show();
                return false;
            }

            //Ensure the current date is between the boundary of the siblings of the target meeting
            Meeting predecessor = null;
            Meeting successor = null;
            ArrayList<Meeting> meetingsInCycle = ledgerLinkApplication.getMeetingRepo().getAllMeetingsOfCycle(meeting.getVslaCycle().getCycleId(), MeetingRepo.MeetingOrderByEnum.ORDER_BY_MEETING_DATE);

            int indexOfTargetMeeting = -1;
            //Get index of current meeting
            for(Meeting meetingX : meetingsInCycle) {
                if(meetingX.getMeetingId() == meeting.getMeetingId()) {
                    indexOfTargetMeeting = meetingsInCycle.indexOf(meetingX);
                    break;
                }
            }

            //get first sibling: ArrayLists start at 0
            //ND: The Meeting are sorted in Descending Order
            if(indexOfTargetMeeting > 0) {
                successor = meetingsInCycle.get(indexOfTargetMeeting - 1);
            }

            if(indexOfTargetMeeting < meetingsInCycle.size() - 1) {
                predecessor = meetingsInCycle.get(indexOfTargetMeeting + 1);
            }

            if(predecessor != null) {
                if(meeting.getMeetingDate().before(predecessor.getMeetingDate())) {
                    Utils.createAlertDialogOk(ChangeMeetingDateActivity.this,getString(R.string.change_meeting_date),
                            String.format(getString(R.string.meeting_date_later_than) + "%s",Utils.formatDate(predecessor.getMeetingDate())),
                            Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return false;
                }
            }

            if(successor != null) {
                if(meeting.getMeetingDate().after(successor.getMeetingDate())){
                    Utils.createAlertDialogOk(ChangeMeetingDateActivity.this,getString(R.string.change_meeting_date),
                            String.format(getString(R.string.meeting_date_earlier_than) + "%s",Utils.formatDate(successor.getMeetingDate())),
                            Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return false;
                }
            }


            return true;
        }
        catch(Exception ex) {
            Log.e("ChangeMeetingDateActivity.validateMeeting", ex.getMessage());
            return false;
        }
    }
}

