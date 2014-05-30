package org.applab.digitizingdata;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;
import org.applab.digitizingdata.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Moses on 7/4/13.
 */
public class MeetingDefinitionActivity extends SherlockActivity {

    ActionBar actionBar;
    TextView txtMeetingDate;
    TextView viewClicked;
    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    String dateString;
    MeetingRepo repo;
    private ArrayList<Member> members;
    private MemberRepo memberRepo;
    private MeetingAttendanceRepo attendanceRepo;
    private Meeting previousMeeting; //The most recent meeting before this one
    private Meeting meetingOfSameDate = null;
    private boolean reloadedExistingMeeting = false; //Flag to determine whether some actions will be performed
    private VslaCycle selectedCycle = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_next_cancel, null);

        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //If Save Operation was successful, get the currently saved meeting
                        //TODO: I can avoid the trip to the database by making the new meeting variable be module-level
                        boolean retSetupMeeting = false;
                        if(saveMeetingDate()) {
                            //Get the Current Meeting ID
                            if(repo == null) {
                                repo = new MeetingRepo(MeetingDefinitionActivity.this);
                            }
                            Meeting currentMeeting = repo.getCurrentMeeting(selectedCycle.getCycleId());

                            retSetupMeeting = setupCurrentMeeting(currentMeeting);

                        }
                        //Otherwise check whether the date entered was actually for an existing meeting whose data has not yet been sent
                        else if(null != meetingOfSameDate) {
                            reloadedExistingMeeting = true;
                            retSetupMeeting = setupCurrentMeeting(meetingOfSameDate);

                        }
                        else{
                            return;
                        }

                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        return;
                    }
                });


        actionBar = getSupportActionBar();
        actionBar.setTitle("Meeting");
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)


        setContentView(R.layout.activity_meeting_definition);

        repo = new MeetingRepo(MeetingDefinitionActivity.this);

        //Setup the Fields by getting the current Cycle
        VslaCycleRepo cycleRepo = new VslaCycleRepo(getApplicationContext());

        //Deal with the radio buttons
        RadioGroup grpCycleDates = (RadioGroup)findViewById(R.id.grpMDExistingCycles);

        //Retrieve all the active cycles
        ArrayList<VslaCycle> activeCycles = cycleRepo.getActiveCycles();

        //Create radio buttons dynamically
        if(activeCycles != null) {
            for(VslaCycle cycle: activeCycles) {
                RadioButton radCycle = new RadioButton(this);
                String cycleDates = String.format("%s - %s", Utils.formatDate(cycle.getStartDate(), "dd MMM yyyy"),
                        Utils.formatDate(cycle.getEndDate(), "dd MMM yyyy"));
                radCycle.setText(cycleDates);
                radCycle.setId(cycle.getCycleId());
                //radCycle.setTextColor();
                radCycle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                radCycle.setTypeface(radCycle.getTypeface(), Typeface.BOLD);
                //radCycle.setPadding(10,0,0,0);
                radCycle.setTag(cycle); //Store the VslaCycle object in the Tag property of the radio button
                //radCycle.setTextColor(txtMeetingDate.getTextColors());
                grpCycleDates.addView(radCycle);

                if(activeCycles.size() == 1) {
                    radCycle.setChecked(true);
                }
            }
        }

        if(activeCycles != null && activeCycles.size()>0) {
            //Populate Fields
            if(activeCycles.size() == 1) {
                if(selectedCycle == null) {
                    selectedCycle = activeCycles.get(0);
                }

//                txtInstructions.setText(new StringBuilder()
//                        .append("The current cycle end date is " + Utils.formatDate(selectedCycle.getEndDate(), "dd-MMM-yyyy"))
//                        .append(". If your cycle has ended, enter the share out date.")
//                        .toString()
//                );
            }
            else {
//                txtInstructions.setText(new StringBuilder()
//                        .append("There is more than one cycle currently running.\n")
//                        .append("Select the cycle to end and enter the share out date.")
//                        .toString()
//                );
            }
        }
        else {
//            txtInstructions.setText(new StringBuilder()
//                    .append("There is no cycle that is currently running")
//                    .toString()
//            );
        }

        //Setup the Checked Listener
        grpCycleDates.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radChecked = (RadioButton) findViewById(checkedId);
                selectedCycle = (VslaCycle)radChecked.getTag();

                //Setup the Previous Meeting at this point so that it holds the meeting of the selected Cycle
                if(null != selectedCycle) {
                    previousMeeting = repo.getCurrentMeeting(selectedCycle.getCycleId());
                }
                //Toast.makeText(getApplicationContext(), "Selected VSLA Cycle is: " + Utils.formatDate(selectedCycle.getStartDate()),Toast.LENGTH_LONG).show();
            }
        });

        //TODO: This will be deprecated after the introduction of multi-cycle support
        if(null != selectedCycle) {
            previousMeeting = repo.getCurrentMeeting(selectedCycle.getCycleId());
        }

        //Reset the instruction text
        //StringBuilder sb = new StringBuilder("Ready to enter data for a meeting? ");
        //sb.append("Set the date and then select <b><i>next</i></b>. ");
        //sb.append("To return to the main menu without starting a new meeting, select <b><i>cancel</i></b>. ");
        //sb.append("If necessary, tap date to select a date in the past. You may not select a date in the future.");

        StringBuilder sb = new StringBuilder("View the date below and tap it to change if it is not the correct meeting date. (You may not select a date in the future.) Tap the arrow above to begin the meeting.");

        TextView txtInstructions = (TextView)findViewById(R.id.lblMDHeader);
        txtInstructions.setText(Html.fromHtml(sb.toString()));

        txtMeetingDate = (TextView)findViewById(R.id.txtMDMeetingDate);
        viewClicked = txtMeetingDate;
        initializeDate();

        //Set onClick Listeners to load the DateDialog for MeetingDate
        txtMeetingDate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView)view;
                DatePickerDialog datePickerDialog = new DatePickerDialog( MeetingDefinitionActivity.this, mDateSetListener, mYear, mMonth, mDay);
                //TODO: Enable this feature in API 11 and above
                //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });

        //Setup the Default Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay();

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
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.meeting_definition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuMDCancel:
                //setupTestDateTime("Africa/Casablanca");
                //i = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(i);

                finish();
                return true;
            case R.id.mnuMDSave:
                //If Save Operation was successful, get the currently saved meeting
                //TODO: I can avoid the trip to the database by making the new meeting variable be module-level
                if(saveMeetingDate()) {
                    //Get the Current Meeting ID
                    if(repo == null) {
                        repo = new MeetingRepo(MeetingDefinitionActivity.this);
                    }
                    Meeting currentMeeting = repo.getCurrentMeeting(selectedCycle.getCycleId());

                    return setupCurrentMeeting(currentMeeting);
                }
                //Otherwise check whether the date entered was actually for an existing meeting whose data has not yet been sent
                else if(null != meetingOfSameDate) {
                    reloadedExistingMeeting = true;
                    return setupCurrentMeeting(meetingOfSameDate);
                }
                else{
                    return false;
                }

        }
        return true;
    }

    private boolean setupCurrentMeeting(Meeting currentMeeting) {
        Intent i = null;
        if(null != currentMeeting) {
            //Setup Meeting
            memberRepo = new MemberRepo(MeetingDefinitionActivity.this);
            attendanceRepo = new MeetingAttendanceRepo(MeetingDefinitionActivity.this);
            if(memberRepo != null && attendanceRepo != null) {
                //Get the Members
                members = memberRepo.getAllMembers();

                //Preset Meeting Attendance to Absent if it is a NEW meeting
                if(!reloadedExistingMeeting) {
                    presetMeetingAttendance(currentMeeting.getMeetingId(), 0);

                    //TODO: Do the same for Savings, Loans etc
                }
            }

            //Indicate that current data view mode is CAPTURE
            Utils._meetingDataViewMode = Utils.MeetingDataViewMode.VIEW_MODE_CAPTURE;

            i = new Intent(getApplicationContext(), MeetingActivity.class);
            i.putExtra("_meetingDate",Utils.formatDate(currentMeeting.getMeetingDate(), "dd-MMM-yyyy"));
            i.putExtra("_meetingId",currentMeeting.getMeetingId());

            int previousMeetingId = 0;
            if(null != previousMeeting) {
                previousMeetingId = previousMeeting.getMeetingId();
            }
            i.putExtra("_previousMeetingId", previousMeetingId);
            startActivity(i);
            return true;
        }
        else {
            Utils.createAlertDialogOk(MeetingDefinitionActivity.this,"Begin Meeting","There was a problem while setting up the meeting",Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }

    }
    public void presetMeetingAttendance(int meetingId, int isPresent) {
        for(Member m: members) {
            attendanceRepo.saveMemberAttendance(meetingId, m.getMemberId(), isPresent);
        }
    }

    private boolean saveMeetingDate(){
        boolean successFlg = false;
        Meeting meeting = new Meeting();
        repo = new MeetingRepo(getApplicationContext());

        if(validateMeeting(meeting)) {
            return repo.addMeeting(meeting);
        }
        else{
            return false;
        }
    }

    private boolean validateMeeting(Meeting meeting){
        try {
            if(null == meeting) {
                return false;
            }

            //Set the Cycle
            //VslaCycleRepo cycleRepo = new VslaCycleRepo(getApplicationContext());
            //VslaCycle cycle = cycleRepo.getCurrentCycle();

            //Set Cycle to the selected one
            if(null != selectedCycle) {
                meeting.setVslaCycle(selectedCycle);
            }
            else {
                Utils.createAlertDialogOk(MeetingDefinitionActivity.this,"Begin Meeting", "The Current Cycle could not be determined. Please choose a cycle.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtMeetingDate.requestFocus();
                return false;
            }

            //Validate the Meeting Date
            TextView txtMeetingDate = (TextView)findViewById(R.id.txtMDMeetingDate);
            String meetingDate = txtMeetingDate.getText().toString().trim();
            Date dt = Utils.getDateFromString(meetingDate,Utils.DATE_FIELD_FORMAT);

            if (dt.after(new Date())) {
                Utils.createAlertDialogOk(MeetingDefinitionActivity.this,"Begin Meeting", "The Meeting date cannot be in the future.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtMeetingDate.requestFocus();
                return false;
            }
            else {
                meeting.setMeetingDate(dt);
            }

            //Before Proceeding determine whether to reload an existing meeting
            //as long as the meeting has not been closed i.e. data sent
            //Get the Most Recent Meeting
            if(null == repo) {
                repo = new MeetingRepo(MeetingDefinitionActivity.this);
            }
            Meeting mostRecent = repo.getMostRecentMeetingInCycle(selectedCycle.getCycleId());

            //First: Check whether a meeting with this date exists in the given vsla cycle
            meetingOfSameDate = null;
            meetingOfSameDate = repo.getMeetingByDate(meeting.getMeetingDate(), selectedCycle.getCycleId());
            if(null != meetingOfSameDate) {
                //Pull the Meeting and display it instead of saving a new meeting
                //cancel the save operation
               return false;
            }

            //Further Validations
            //check that meeting is with the boundaries of the current cycle
            if(meeting.getMeetingDate().before(selectedCycle.getStartDate()) || meeting.getMeetingDate().after(selectedCycle.getEndDate())) {
                Utils.createAlertDialogOk(MeetingDefinitionActivity.this,"Begin Meeting",
                        String.format("The Meeting Date has to be within the current cycle i.e. %s and %s",Utils.formatDate(selectedCycle.getStartDate()), Utils.formatDate(selectedCycle.getEndDate())),
                        Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtMeetingDate.requestFocus();
                return false;
            }

            //Ensure the current date is later than the date of the most recent meeting
            if(null != mostRecent) {
                if(meeting.getMeetingDate().before(mostRecent.getMeetingDate())) {
                    Utils.createAlertDialogOk(MeetingDefinitionActivity.this,"Begin Meeting",
                            String.format("The Meeting Date has to be after the date of the last meeting: %s", Utils.formatDate(mostRecent.getMeetingDate())),
                            Utils.MSGBOX_ICON_EXCLAMATION).show();
                    //txtMeetingDate.requestFocus();
                    return false;
                }
            }

            return true;
        }
        catch(Exception ex) {
            Log.e("MeetingDefinitionActivity.validateMeeting", ex.getMessage());
            return false;
        }
    }
}