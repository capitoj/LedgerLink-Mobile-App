package org.applab.digitizingdata;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.*;

import java.util.Calendar;

/**
 * Created by Moses on 6/27/13.
 */
public class GettingsStartedWizardNewCycleActivity extends NewCycleActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cycle_getting_started_wizard);

        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
        selectedCycle = repo.getCurrentCycle();

        if(selectedCycle != null) {
            //isUpdateCycleAction = getIntent().getBooleanExtra("_isUpdateCycleAction",false);
            isUpdateCycleAction = true;
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle("GETTING STARTED");

        txtStartDate = (TextView)findViewById(R.id.txtNCStartDate);
        txtEndDate = (TextView)findViewById(R.id.txtNCEndDate);

        //Set onClick Listeners to load the DateDialog for startDate
        txtStartDate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView)view;
                settingStartDate = true;
                DatePickerDialog datePickerDialog = new DatePickerDialog( GettingsStartedWizardNewCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set cycle start date");
                datePickerDialog.show();
            }
        });

        //Set onClick Listeners to load the DateDialog for endDate
        txtEndDate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewClicked = (TextView)view;
                settingStartDate = false;
                DatePickerDialog datePickerDialog = new DatePickerDialog( GettingsStartedWizardNewCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set cycle end date");
                datePickerDialog.show();
            }
        });

        if(isUpdateCycleAction) {
            //Setup the Fields by getting the current Cycle
            TextView heading = (TextView) findViewById(R.id.lblNCHeading);
            heading.setText("Review Cycle Information");

            TextView headerText = (TextView) findViewById(R.id.lblNCHeader);
            heading.setText("Review and confirm that all information is correct. Correct any errors");



            if(selectedCycle != null) {
                //displayMessageBox("Testing", "Cycle to Update Found", Utils.MSGBOX_ICON_INFORMATION);
                //Populate Fields
                populateDataFields(selectedCycle);

                //Set the fields required by the DatePicker
                //TODO: Will find a way of setting defaults for both dates accordingly
                final Calendar c = Calendar.getInstance();
                c.setTime(selectedCycle.getStartDate());
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }
            else {
                TextView txtInstructions = (TextView)findViewById(R.id.lblNCHeader);
                txtInstructions.setText(new StringBuilder()
                        .append("There is no cycle that is currently running. A New Cycle will be created.")
                        .toString()
                );

                //setup default dates
                setupDefaultDates();

                //Convert it to New Cycle operation
                isUpdateCycleAction = false;
            }
        }
        else {
            //displayMessageBox("Testing", "Cycle to Update NOT Found", MSGBOX_ICON_INFORMATION);
            //Setup the Default Date
            setupDefaultDates();
            //Set the current stage of the wizard
            VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
            vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_NEW_CYCLE);
        }



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
        //TODO work on this to display the proper date depending on what is clicked
        super.onPrepareDialog(id, dialog);
        ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
        dialog.setTitle("Set cycle date");

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.new_cycle, menu);
        //Hide cancel button since it does not exist in GSW
        menu.findItem(R.id.mnuNCCancel).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
           /* case R.id.mnuNCCancel:
                //On cancel go back to PageTwo of GSW
                i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;*/
            case R.id.mnuNCNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                return saveMiddleCycleData();
        }
        return true;

    }



    private boolean  saveMiddleCycleData() {
        boolean successFlg = false;

        VslaCycle cycle = new VslaCycle();
        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
        if (selectedCycle != null) {
            cycle = selectedCycle;
        }

        if (validateGettingStartedData(cycle)) {
            boolean retVal = false;
            if (cycle.getCycleId() != 0) {
                retVal = repo.updateCycle(cycle);
            }
            else {

                retVal = repo.addCycle(cycle);
            }
            if (retVal) {
                if (cycle.getCycleId() == 0) {
                    //Set this new cycle as the selected one
                    //Retrieve the recently added cycle so as to get the id as well
                    cycle = repo.getMostRecentCycle();
                    selectedCycle = cycle;
                    //TODO: create Getting started wizard dummy meeting
                    retVal = createGettingStartedDummyMeeting(selectedCycle);

                    if(retVal) {
                        Toast.makeText(this, "Dummy meeting has been created", Toast.LENGTH_LONG).show(); ;
                    }
                    else {
                        Toast.makeText(this, "We failed to create the dummy meeting", Toast.LENGTH_LONG).show(); ;
                    }
                    //displayMessageBox(dialogTitle, "The New Cycle has been added Successfully.", Utils.MSGBOX_ICON_TICK);
                }
                else {
                    //displayMessageBox("Update Cycle", "The Cycle has been updated Successfully.", Utils.MSGBOX_ICON_TICK);
                }

                String testJson = SendDataRepo.getVslaCycleJson(repo.getCurrentCycle());
                if(testJson.length() < 0) {
                    return false;
                }
                MemberRepo memberRepo = new MemberRepo(getApplicationContext());

                String membersJson = SendDataRepo.getMembersJson(memberRepo.getAllMembers());
                if(membersJson.length() < 0) {
                    return false;
                }

                //Pass on the flag indicating whether this is an Update operation
                Intent i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                i.putExtra("_isUpdateCycleAction", isUpdateCycleAction);
                startActivity(i);
                /*
                if(null != alertDialog && alertDialog.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }
                */
                successFlg = true;
                //clearDataFields(); //Not needed now
            }
            else {
                displayMessageBox(dialogTitle, "A problem occurred while capturing the Cycle Data. Please try again.", Utils.MSGBOX_ICON_EXCLAMATION);
            }
        }
        else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    //Creates the dummy meeting to be used to getting started wizard
    private boolean createGettingStartedDummyMeeting(VslaCycle currentCycle) {

        Meeting meeting = new Meeting();
        meeting.setGettingStarted(true);
        meeting.setIsCurrent(true);
        meeting.setVslaCycle(currentCycle);
        meeting.setMeetingDate(currentCycle.getStartDate());

        MeetingRepo repo = new MeetingRepo(this);
        return repo.addMeeting(meeting);

    }


    private boolean validateGettingStartedData(VslaCycle cycle) {
        try {
            if(null == cycle) {
                return false;
            }

            //validate Data common to New Cycle
            if(! validateData(cycle))
            {
                return false;
            }

            //Validation specific to getting started wizard
            //Validate Amount of interested collected so far
            TextView txtInterestCollected = (TextView)findViewById(R.id.txtNCInterestCollectedSoFar);
            String interestCollected = txtInterestCollected.getText().toString().trim();
            if (interestCollected.length() < 1) {
                displayMessageBox(dialogTitle, "The Interest Collected Is Required", Utils.MSGBOX_ICON_EXCLAMATION);
                txtInterestCollected.requestFocus();
                return false;
            }
            else {
                double theInterestCollected = Double.parseDouble(interestCollected);
                if (theInterestCollected < 0.00) {
                    displayMessageBox(dialogTitle, "The Interest Collected should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtInterestCollected.requestFocus();
                    return false;
                }
                else {
                    cycle.setInterestAtSetup(theInterestCollected);
                }
            }







            //Validate Fines collected
            TextView txtFinesCollected = (TextView)findViewById(R.id.txtNCFinesCollectedSoFar);
            String finesCollected = txtFinesCollected.getText().toString().trim();
            if (finesCollected.length() < 1) {
                displayMessageBox(dialogTitle, "The Fines Collected Field Is Required", Utils.MSGBOX_ICON_EXCLAMATION);
                txtFinesCollected.requestFocus();
                return false;
            }
            else {
                double theFinesCollected = Double.parseDouble(finesCollected);
                if (theFinesCollected < 0.00) {
                    displayMessageBox(dialogTitle, "The Fines Collected should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtFinesCollected.requestFocus();
                    return false;
                }
                else {
                    cycle.setFinesAtSetup(theFinesCollected);
                }
            }








            return true;
        }
        catch (Exception ex){
            return false;
        }
    }



    @Override
    protected void populateDataFields(VslaCycle cycle) {
        //Clear Fields
        clearDataFields();

        if(cycle == null) {
            return;
        }
        super.populateDataFields(cycle);

        try {
            //Now populate  specific to Getting started wizard
            TextView txtInterestCollecteSoFar = (TextView)findViewById(R.id.txtNCInterestCollectedSoFar);
            TextView txtFinesCollectedSoFar = (TextView)findViewById(R.id.lblNCFinesCollectedSoFar);

            //TODO: Set the interest and fines for the middle start cycle
            txtInterestCollecteSoFar.setText(Utils.formatRealNumber(cycle.getMaxSharesQty()));
            txtFinesCollectedSoFar.setText(Utils.formatRealNumber(cycle.getMaxSharesQty()));

        }
        catch(Exception ex){

        }
    }

    protected void clearDataFields() {
        super.clearDataFields();
        try{
            //Now Clear fields specific to GSWizard
            TextView txtInterestCollecteSoFar = (TextView)findViewById(R.id.txtNCInterestCollectedSoFar);
            TextView txtFinesCollectedSoFar = (TextView)findViewById(R.id.lblNCFinesCollectedSoFar);

            txtInterestCollecteSoFar.setText("");
            txtFinesCollectedSoFar.setText("");
        }
        catch(Exception ex){

        }

    }

}