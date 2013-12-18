package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.model.VslaMiddleStartCycle;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.SendDataRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/27/13.
 */
public class GettingsStartedWizardNewCycleActivity extends SherlockActivity {
    ActionBar actionBar;

    TextView txtStartDate;
    TextView txtEndDate;
    TextView viewClicked;
    private boolean settingStartDate = false;
    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private String dialogTitle = "New Cycle";
    private AlertDialog alertDialog = null;
    private boolean successAlertDialogShown = false;
    private boolean isUpdateCycleAction = false;

    private VslaCycle selectedCycle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cycle_getting_started_wizard);

        if(getIntent().hasExtra("_isUpdateCycleAction")) {
            isUpdateCycleAction = getIntent().getBooleanExtra("_isUpdateCycleAction",false);
        }

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(isUpdateCycleAction) {
            actionBar.setTitle("Edit Cycle");
        }
        else {
            actionBar.setTitle("New Cycle");
        }

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
            VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
            selectedCycle = repo.getCurrentCycle();
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
                actionBar.setTitle("New Cycle");
            }
        }
        else {
            //displayMessageBox("Testing", "Cycle to Update NOT Found", MSGBOX_ICON_INFORMATION);
            //Setup the Default Date
            setupDefaultDates();

        }

    }

    private void setupDefaultDates() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay(txtStartDate, mYear, mMonth+1,mDay);

        //Set Default End Date
        c.add(Calendar.YEAR,1);
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay(txtEndDate,mYear,mMonth+1,mDay);
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

    //Displays the selected Date in the TextView
    private void updateDisplay() {
        if(viewClicked != null) {
            viewClicked.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(mDay)
                .append("-")
                .append(Utils.getMonthNameAbbrev(mMonth + 1))
                .append("-")
                .append(mYear)
                .toString());

            //Default the End Date to StartDate + 52 weeks
            if(settingStartDate && txtEndDate != null) {
                settingStartDate = false;
                txtEndDate.setText(new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mDay)
                        .append("-")
                        .append(Utils.getMonthNameAbbrev(mMonth + 1))
                        .append("-")
                        .append(mYear + 1)
                        .toString());
            }
        }
        else {
            //Not sure yet on what to do
        }
    }

    private void updateDisplay(TextView theField) {
        if(theField != null) {
            theField.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mDay)
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)
                    .toString());
        }
    }

    private void updateDisplay(TextView theField, int theYear, int theMonth, int theDay) {
        if(theField != null) {
            theField.setText(new StringBuilder()
                    .append(theDay)
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(theMonth))
                    .append("-")
                    .append(theYear)
                    .toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.new_cycle, menu);
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
            case R.id.mnuNCCancel:
                i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case R.id.mnuNCNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                return saveCycleData();
        }
        return true;

    }

    private boolean saveCycleData() {
        //Test navigation
        if(true)
        {
        Intent j = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
        j.putExtra("_isUpdateCycleAction", isUpdateCycleAction);
        startActivity(j);
        return true;
        }
        boolean successFlg = false;

        VslaMiddleStartCycle cycle = new VslaMiddleStartCycle();
        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());

        VslaCycle castedCycle = (VslaCycle) cycle;
        if (selectedCycle != null) {
            castedCycle = selectedCycle;
        }

        if (validateData(cycle)) {
            boolean retVal = false;
            if (cycle.getCycleId() != 0) {
                retVal = repo.updateMiddleStartCycle(cycle);
            }
            else {

                retVal = repo.addMiddleStartCycle(cycle);
            }
            if (retVal) {
                if (cycle.getCycleId() == 0) {
                    //Set this new cycle as the selected one
                    selectedCycle = cycle;
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
                //On success saving...
                //Go to New member defination activity
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

    private boolean validateData(VslaMiddleStartCycle cycle) {
        try {
            if(null == cycle) {
                return false;
            }

            // Validate: SharePrice
            TextView txtSharePrice = (TextView)findViewById(R.id.txtNCSharePrice);
            String sharePrice = txtSharePrice.getText().toString().trim();
            if (sharePrice.length() < 1) {
                displayMessageBox(dialogTitle, "The Share Price is required.", Utils.MSGBOX_ICON_EXCLAMATION);
                txtSharePrice.requestFocus();
                return false;
            }
            else {
                double theSharePrice = Double.parseDouble(sharePrice);
                if (theSharePrice <= 0.00) {
                    displayMessageBox(dialogTitle, "The Share Price must be positive.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtSharePrice.requestFocus();
                    return false;
                }
                else {
                    cycle.setSharePrice(theSharePrice);
                }
            }

            // Validate: MaxShareAmount
            TextView txtMaxShareQty = (TextView)findViewById(R.id.txtNCMaxShares);
            String maxShareQty = txtMaxShareQty.getText().toString().trim();
            if (maxShareQty.length() < 1) {
                displayMessageBox(dialogTitle, "The Maximum Share Quantity is required.", Utils.MSGBOX_ICON_EXCLAMATION);
                txtMaxShareQty.requestFocus();
                return false;
            }
            else {
                double theMaxShareQty = Double.parseDouble(maxShareQty);
                if (theMaxShareQty <= 0.00) {
                    displayMessageBox(dialogTitle, "The Maximum Share Quantity must be positive.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtMaxShareQty.requestFocus();
                    return false;
                }
                else {
                    cycle.setMaxSharesQty(theMaxShareQty);
                }
            }

            // Validate: MaxStartShare
            cycle.setMaxStartShare(0.0); //Unlimited

            // Validate: StartDate
            TextView txtStartDate = (TextView)findViewById(R.id.txtNCStartDate);
            String startDate = txtStartDate.getText().toString().trim();
            Date dt = Utils.getDateFromString(startDate,Utils.DATE_FIELD_FORMAT);

            //if (dt.before(new Date())) {
            //    displayMessageBox(dialogTitle, "The Start Date must be today or in the future.", Utils.MSGBOX_ICON_EXCLAMATION);
            //    txtStartDate.requestFocus();
            //    return false;
            //}
            //else {
                cycle.setStartDate(dt);
            //}

            // Validate: EndDate
            TextView txtEndDate = (TextView)findViewById(R.id.txtNCEndDate);
            String endDate = txtEndDate.getText().toString().trim();
            Date dtEnd = Utils.getDateFromString(endDate,Utils.DATE_FIELD_FORMAT);

            if (dtEnd.before(cycle.getStartDate())) {
                displayMessageBox(dialogTitle, "The End Date must be after the Start Date", Utils.MSGBOX_ICON_EXCLAMATION);
                txtEndDate.requestFocus();
                return false;
            }
            else {
                cycle.setEndDate(dtEnd);
            }

            // Validate: MaxShareAmount
            TextView txtInterestRate = (TextView)findViewById(R.id.txtNCInterestRate);
            String interestRate = txtInterestRate.getText().toString().trim();
            if (interestRate.length() < 1) {
                displayMessageBox(dialogTitle, "The Interest Rate is required.", Utils.MSGBOX_ICON_EXCLAMATION);
                txtInterestRate.requestFocus();
                return false;
            }
            else {
                double theInterestRate = Double.parseDouble(interestRate);
                if (theInterestRate < 0.00) {
                    displayMessageBox(dialogTitle, "The Interest Rate should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtInterestRate.requestFocus();
                    return false;
                }
                else {
                    cycle.setInterestRate(theInterestRate);
                }
            }



            //Validate Amount of interested collected so far
            TextView txtInterestCollected = (TextView)findViewById(R.id.txtNCInterestCollectedSoFar);
            String interestCollected = txtInterestCollected.getText().toString().trim();
            if (interestCollected.length() < 1) {
                displayMessageBox(dialogTitle, "The Interest Collcted Is Required", Utils.MSGBOX_ICON_EXCLAMATION);
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
                    cycle.setInterestReceived(theInterestCollected);
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
                    cycle.setFinesCollected(theFinesCollected);
                }
            }






            //Check that the Cycle Start Date does not overlap with the date the previous cycle ended
            //First, get the most recent cycle
            VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(getApplicationContext());
            VslaCycle mostRecentCycle = vslaCycleRepo.getMostRecentCycle();

            if(null != mostRecentCycle) {
                if(isUpdateCycleAction && mostRecentCycle.getCycleId() == cycle.getCycleId()){
                    //Fine
                }
                else {
                    //Check the Dates: use startDate vs DateEnded
                    if(cycle.getStartDate() == null) {
                        return false;
                    }
                    if(mostRecentCycle.getDateEnded() != null) {
                        if(cycle.getStartDate().before(mostRecentCycle.getDateEnded())) {
                            Utils.createAlertDialogOk(GettingsStartedWizardNewCycleActivity.this, dialogTitle,
                                    String.format("The start date of this cycle should be after the share-out date of the previous cycle, which was: %s.", Utils.formatDate(mostRecentCycle.getDateEnded())),
                                    Utils.MSGBOX_ICON_EXCLAMATION).show();
                            return false;
                        }
                    }
                }
            }

            //Set the Cycle as Active
            cycle.activate();






            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    private void displayMessageBox(String title, String message, String icon) {
        alertDialog = new AlertDialog.Builder(GettingsStartedWizardNewCycleActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_EXCLAMATION)) {
            alertDialog.setIcon(R.drawable.exclamation);
        }
        else if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_TICK)) {
            alertDialog.setIcon(R.drawable.tick);
        }
        else if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_QUESTION)) {
            alertDialog.setIcon(R.drawable.question);
        }

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                //Can I pass a method delegate? Or function pointer? for what to be executed?
                // Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                if(successAlertDialogShown) {
                    Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                    startActivity(i);
                    successAlertDialogShown = false;
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    private void populateDataFields(VslaCycle cycle) {
        //Clear Fields
        clearDataFields();

        if(cycle == null) {
            return;
        }

        try {
            //Now populate
            TextView txtSharePrice = (TextView)findViewById(R.id.txtNCSharePrice);
            TextView txtMaxShareQty = (TextView)findViewById(R.id.txtNCMaxShares);
            TextView txtStartDate = (TextView)findViewById(R.id.txtNCStartDate);
            TextView txtEndDate = (TextView)findViewById(R.id.txtNCEndDate);
            TextView txtInterestRate = (TextView)findViewById(R.id.txtNCInterestRate);

            txtSharePrice.setText(Utils.formatRealNumber(cycle.getSharePrice()));
            txtMaxShareQty.setText(Utils.formatRealNumber(cycle.getMaxSharesQty()));
            txtStartDate.setText(Utils.formatDate(cycle.getStartDate(),"dd-MMM-yyyy"));
            txtEndDate.setText(Utils.formatDate(cycle.getEndDate(),"dd-MMM-yyyy"));
            txtInterestRate.setText(Utils.formatRealNumber(cycle.getInterestRate()));
        }
        catch(Exception ex){

        }
    }

    private void clearDataFields() {
        try{
            //Now populate
            TextView txtSharePrice = (TextView)findViewById(R.id.txtNCSharePrice);
            TextView txtMaxShareQty = (TextView)findViewById(R.id.txtNCMaxShares);
            TextView txtStartDate = (TextView)findViewById(R.id.txtNCStartDate);
            TextView txtEndDate = (TextView)findViewById(R.id.txtNCEndDate);
            TextView txtInterestRate = (TextView)findViewById(R.id.txtNCInterestRate);

            txtSharePrice.setText("");
            txtMaxShareQty.setText("");
            txtStartDate.setText("");
            txtEndDate.setText("");
            txtInterestRate.setText("");
        }
        catch(Exception ex){

        }

    }

}