package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.LongTaskRunner;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.SendDataRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/27/13.
 */
public class NewCycleActivity extends SherlockActivity {
    ActionBar actionBar;

    TextView txtStartDate;
    TextView txtEndDate;
    TextView viewClicked;
    protected boolean settingStartDate = false;
    protected static final int Date_dialog_id = 1;
    // date and time
    protected int mYear;
    protected int mMonth;
    protected int mDay;
    protected int mEndYear;
    protected int mEndMonth;
    protected int mEndDay;
    protected String dialogTitle = "New Cycle";
    protected AlertDialog alertDialog = null;
    protected boolean successAlertDialogShown = false;
    protected boolean isUpdateCycleAction = false;
    protected boolean multipleCyclesIndicator = false;
    LinearLayout linearLayout;

    protected VslaCycle selectedCycle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cycle);

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        if (getIntent().hasExtra("_isUpdateCycleAction")) {
            isUpdateCycleAction = getIntent().getBooleanExtra("_isUpdateCycleAction", false);
        }

        if (getIntent().hasExtra("_multipleCycles")) {
            multipleCyclesIndicator = getIntent().getBooleanExtra("_multipleCycles", false);
        }

        inflateCustombar();

        if (isUpdateCycleAction) {
            actionBar.setTitle("Edit Cycle");
        } else {
            actionBar.setTitle("New Cycle");
        }

        txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
        txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
        linearLayout = (LinearLayout) findViewById(R.id.sectionNCMiddleCycleStart);
        linearLayout.setVisibility(View.GONE);


        //Set onClick Listeners to load the DateDialog for startDate
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView) view;
                settingStartDate = true;
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set cycle start date");
                datePickerDialog.show();
            }
        });

        // Set onClick Listeners to load the DateDialog for endDate
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewClicked = (TextView) view;
                settingStartDate = false;
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewCycleActivity.this, mDateSetListener, mEndYear, mEndMonth, mEndDay);
                datePickerDialog.setTitle("Set cycle end date");
                datePickerDialog.show();
            }
        });

        if (isUpdateCycleAction) {
            // Setup the Fields by getting the current Cycle

            if (!getIntent().hasExtra("_cycleId")) {
                VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
                selectedCycle = repo.getCurrentCycle();
            } else if (getIntent().getIntExtra("_cycleId", 0) != 0) {
                //for concurrent cycles , if cycle id is passed then load the specified cycle
                VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
                selectedCycle = repo.getCycle(getIntent().getIntExtra("_cycleId", 0));
            } else {
                VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
                selectedCycle = repo.getCurrentCycle();
            }
            if (selectedCycle != null) {
                //displayMessageBox("Testing", "Cycle to Update Found", Utils.MSGBOX_ICON_INFORMATION);
                //Change the title in edit mode
                TextView lblNCHeader = (TextView) findViewById(R.id.lblNCHeader);
                lblNCHeader.setText("Edit the cycle beginning " + Utils.formatDate(selectedCycle.getStartDate(), "dd MMM yyyy") + " and ending " + Utils.formatDate(selectedCycle.getEndDate(), "dd MMM yyyy") + ".");
                //Populate Fields
                populateDataFields(selectedCycle);

                // Populate GSW fields
                // If cycle has no GSW data don't show GSW fields
                MeetingRepo meetingRepo = new MeetingRepo(getApplicationContext());
                Meeting dummyGSWMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();
                if (dummyGSWMeeting != null) {
                    if (selectedCycle.getCycleId() != dummyGSWMeeting.getVslaCycle().getCycleId()) {
                        //linearLayout = (LinearLayout) findViewById(R.id.sectionNCMiddleCycleStart);
                        linearLayout.setVisibility(View.GONE);
                    } else if (selectedCycle.getInterestAtSetup() == 0 && selectedCycle.getFinesAtSetup() == 0) {
                        linearLayout.setVisibility(View.GONE);
                    } else {
                        linearLayout.setVisibility(View.VISIBLE);
                        TextView lblNCMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblNCMiddleCycleInformationHeading);

                        TextView txtInterestCollectedSoFar = (TextView) findViewById(R.id.lblNCMiddleCycleInterestCollectedSoFar);
                        TextView txtFinesCollectedSoFar = (TextView) findViewById(R.id.lblNCMiddleCycleFinesCollectedSoFar);
                        txtInterestCollectedSoFar.setText(String.format("%.0f UGX", selectedCycle.getInterestAtSetup()));
                        txtFinesCollectedSoFar.setText(String.format("%.0f UGX", selectedCycle.getFinesAtSetup()));

                        if (isUpdateCycleAction) {
                            EditText txtNCInterestCorrectionComment = (EditText) findViewById(R.id.txtNCMiddleCycleInterestCorrectionComment);
                            if (null != selectedCycle.getInterestAtSetupCorrectionComment()) {
                                if (!selectedCycle.getInterestAtSetupCorrectionComment().trim().isEmpty()) {
                                    txtNCInterestCorrectionComment.setText(selectedCycle.getInterestAtSetupCorrectionComment());
                                }
                            }

                            EditText txtNCFinesCorrectionComment = (EditText) findViewById(R.id.txtNCMiddleCycleFinesCorrectionComment);
                            if (null != selectedCycle.getFinesAtSetupCorrectionComment()) {
                                if (!selectedCycle.getFinesAtSetupCorrectionComment().trim().isEmpty()) {
                                    txtNCFinesCorrectionComment.setText(selectedCycle.getFinesAtSetupCorrectionComment());
                                }
                            }
                        }

                        lblNCMiddleCycleInformationHeading.setText("This information was added after the cycle started. Here are the interest and fines collected by that day.");

                        if (dummyGSWMeeting != null) {
                            lblNCMiddleCycleInformationHeading.setText("This information was added on " + Utils.formatDate(dummyGSWMeeting.getMeetingDate(), "dd MMM yyyy") + " after the cycle started. Here are the interest and fines collected by that day.");
                        }

                    }


                }


                //Set the fields required by the DatePicker
                //TODO: Will find a way of setting defaults for both dates accordingly
                final Calendar c = Calendar.getInstance();
                c.setTime(selectedCycle.getStartDate());
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                c.setTime(selectedCycle.getEndDate());
                mEndYear = c.get(Calendar.YEAR);
                mEndMonth = c.get(Calendar.MONTH);
                mEndDay = c.get(Calendar.DAY_OF_MONTH);
            } else {
                TextView txtInstructions = (TextView) findViewById(R.id.lblNCHeader);
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
        } else {
            //displayMessageBox("Testing", "Cycle to Update NOT Found", MSGBOX_ICON_INFORMATION);
            //Setup the Default Date
            setupDefaultDates();

        }

        // Populate Max Shares Spinner
        buildMaxSharesSpinner();
    }

    /* inflates custom menu bar for review members */
    public void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        View customActionBarView = null;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_back_next_done, null);

        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Save this as long task
                        saveCycleData();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveCycleData();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);

                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i;
                        if (multipleCyclesIndicator) {
                            i = new Intent(getApplicationContext(), SelectCycle.class);
                        } else {
                            i = new Intent(getApplicationContext(), MainActivity.class);
                        }
                        i.putExtra("_isEndCycleAction", true);
                        startActivity(i);
                        finish();
                    }
                }
        );

        if (isUpdateCycleAction) {
            customActionBarView.findViewById(R.id.actionbar_next).setVisibility(View.GONE);
            customActionBarView.findViewById(R.id.actionbar_back).setVisibility(View.GONE);

            // Set to true to enable caret function; if designer decides otherwise set both to false
            actionBar.setHomeButtonEnabled(true);
            //actionBar.setDisplayHomeAsUpEnabled(false);

        } else {
            customActionBarView.findViewById(R.id.actionbar_back).setVisibility(View.GONE);
            customActionBarView.findViewById(R.id.actionbar_done).setVisibility(View.GONE);
        }

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

    }


    protected void setupDefaultDates() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay(txtStartDate, mYear, mMonth + 1, mDay);

        //Set Default End Date
        c.add(Calendar.YEAR, 1);
        //deduct 1 day
        c.add(Calendar.DATE, -1);
        mEndYear = c.get(Calendar.YEAR);
        mEndMonth = c.get(Calendar.MONTH);
        mEndDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay(txtEndDate, mEndYear, mEndMonth + 1, mEndDay);
    }

    //Event that is raised when the date has been set
    protected DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
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
    protected void updateDisplay() {
        if (viewClicked != null) {
            viewClicked.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(String.format("%02d", mDay))
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)
                    .toString());

            //Default the End Date to StartDate + 52 weeks
            if (settingStartDate && txtEndDate != null) {
                settingStartDate = false;

                String endDateString = new StringBuilder()
                        // Month is 0 based so add 1
                        .append(String.format("%02d", mDay))
                        .append("-")
                        .append(Utils.getMonthNameAbbrev(mMonth + 1))
                        .append("-")
                        .append(mYear + 1)
                        .toString();
                Date endDate = Utils.getDateFromString(endDateString, "dd-MMM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                //deduct 1 day
                cal.add(Calendar.DATE, -1);

                //Now have the new date
                Date newEndDate = cal.getTime();

                txtEndDate.setText(Utils.formatDate(newEndDate, "dd-MMM-yyyy"));
            }
        } else {
            //Not sure yet on what to do
        }
    }

    protected void updateDisplay(TextView theField) {
        if (theField != null) {
            theField.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(String.format("%02d", mDay))
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)
                    .toString());
        }
    }

    protected void updateDisplay(TextView theField, int theYear, int theMonth, int theDay) {
        if (theField != null) {
            theField.setText(new StringBuilder()
                    .append(String.format("%02d", theDay))
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(theMonth))
                    .append("-")
                    .append(theYear)
                    .toString());
        }
    }

    /**
     * @Override public boolean onCreateOptionsMenu(Menu menu) {
     * final MenuInflater inflater = getSupportMenuInflater();
     * inflater.inflate(R.menu.new_cycle, menu);
     * return true;
     * }
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
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
            /**  case R.id.mnuNCCancel:
             i = new Intent(getApplicationContext(), MainActivity.class);
             startActivity(i);
             return true;
             case R.id.mnuNCNext:
             //First Save the Cycle Dates
             //If successful move to next activity
             return saveCycleData();
             */
        }
        return true;

    }

    protected boolean saveCycleData() {
        boolean successFlg = false;

        VslaCycle cycle = new VslaCycle();

        if (selectedCycle != null) {
            cycle = selectedCycle;
        }

        if (validateData(cycle)) {
            final VslaCycle finalCycle = cycle;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    saveCycleDataToDb(finalCycle);
                }
            };
            LongTaskRunner.runLongTask(runnable, "Please wait...", "Saving cycle information...", NewCycleActivity.this);

            return true;
            //clearDataFields(); //Not needed now
        } else {
            //displayMessageBox(dialogTitle, "A problem occurred while capturing the Cycle Data. Please try again.", Utils.MSGBOX_ICON_EXCLAMATION);
            return false;
        }
    }

    private boolean saveCycleDataToDb(VslaCycle cycle) {
        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
        boolean retVal = false;
        if (cycle.getCycleId() != 0) {
            retVal = repo.updateCycle(cycle);
        } else {

            retVal = repo.addCycle(cycle);
        }
        boolean successFlg = false;
        if (retVal) {
            if (cycle.getCycleId() == 0) {
                //Set this new cycle as the selected one
                selectedCycle = cycle;
                //displayMessageBox(dialogTitle, "The New Cycle has been added Successfully.", Utils.MSGBOX_ICON_TICK);
            } else {
                //displayMessageBox("Update Cycle", "The Cycle has been updated Successfully.", Utils.MSGBOX_ICON_TICK);
            }

            String testJson = SendDataRepo.getVslaCycleJson(repo.getCurrentCycle());
            if (testJson.length() < 0) {
                return false;
            }
            MemberRepo memberRepo = new MemberRepo(getApplicationContext());

            String membersJson = SendDataRepo.getMembersJson(memberRepo.getAllMembers());
            if (membersJson.length() < 0) {
                return false;
            }

            if (isUpdateCycleAction) {
                return true;
            }

            //Pass on the flag indicating whether this is an Update operation
            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
            i.putExtra("_isUpdateCycleAction", isUpdateCycleAction);
            startActivity(i);
                /*
                if(null != alertDialog && alertDialog.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }
                */
            successFlg = true;
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    protected boolean validateData(VslaCycle cycle) {
        try {
            if (null == cycle) {
                return false;
            }

            // Validate: SharePrice
            TextView txtSharePrice = (TextView) findViewById(R.id.txtNCSharePrice);
            String sharePrice = txtSharePrice.getText().toString().trim();
            if (sharePrice.length() < 1) {
                displayMessageBox(dialogTitle, "The Share Price is required.", Utils.MSGBOX_ICON_EXCLAMATION);
                txtSharePrice.requestFocus();
                return false;
            } else {
                double theSharePrice = Double.parseDouble(sharePrice);
                if (theSharePrice <= 0.00) {
                    displayMessageBox(dialogTitle, "The Share Price must be positive.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtSharePrice.requestFocus();
                    return false;
                } else {
                    cycle.setSharePrice(theSharePrice);
                }
            }

            /**     String maxShareQty = txtMaxShareQty.getText().toString().trim();
             if (maxShareQty.length() < 1) {
             displayMessageBox(dialogTitle, "The Maximum Share Quantity is required.", Utils.MSGBOX_ICON_EXCLAMATION);
             txtMaxShareQty.requestFocus();
             return false;
             }
             else { */

            // Validate: MaxShareAmount
            Spinner cboMaxShareQty = (Spinner) findViewById(R.id.cboNCMaxShares);
            if (cboMaxShareQty.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dialogTitle, "The Maximum Share Quantity is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboMaxShareQty.requestFocus();
                return false;
            } else {
                String maxShareQty = cboMaxShareQty.getSelectedItem().toString().trim();
                int theMaxShareQty = Integer.valueOf(maxShareQty);
                if (theMaxShareQty <= 0) {
                    displayMessageBox(dialogTitle, "The Maximum Share Quantity must be positive.", Utils.MSGBOX_ICON_EXCLAMATION);
                    cboMaxShareQty.requestFocus();
                    return false;
                } else {
                    cycle.setMaxSharesQty(theMaxShareQty);
                }
            }

            // Validate: MaxStartShare
            cycle.setMaxStartShare(0.0); //Unlimited

            // Validate: StartDate
            TextView txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
            String startDate = txtStartDate.getText().toString().trim();
            Date dt = Utils.getDateFromString(startDate, Utils.DATE_FIELD_FORMAT);

            //if (dt.before(new Date())) {
            //    displayMessageBox(dialogTitle, "The Start Date must be today or in the future.", Utils.MSGBOX_ICON_EXCLAMATION);
            //    txtStartDate.requestFocus();
            //    return false;
            //}
            //else {
            cycle.setStartDate(dt);
            //}

            // Validate: EndDate
            TextView txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
            String endDate = txtEndDate.getText().toString().trim();
            Date dtEnd = Utils.getDateFromString(endDate, Utils.DATE_FIELD_FORMAT);

            if (dtEnd.before(cycle.getStartDate())) {
                displayMessageBox(dialogTitle, "The End Date must be after the Start Date", Utils.MSGBOX_ICON_EXCLAMATION);
                txtEndDate.requestFocus();
                return false;
            } else {
                cycle.setEndDate(dtEnd);
            }

            // Validate: MaxShareAmount
            TextView txtInterestRate = (TextView) findViewById(R.id.txtNCInterestRate);
            String interestRate = txtInterestRate.getText().toString().trim();
            if (interestRate.length() < 1) {
                displayMessageBox(dialogTitle, "The Interest Rate is required.", Utils.MSGBOX_ICON_EXCLAMATION);
                txtInterestRate.requestFocus();
                return false;
            } else {
                double theInterestRate = Double.parseDouble(interestRate);
                if (theInterestRate < 0.00) {
                    displayMessageBox(dialogTitle, "The Interest Rate should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtInterestRate.requestFocus();
                    return false;
                } else {
                    cycle.setInterestRate(theInterestRate);
                }
            }

            //Check that the Cycle Start Date does not overlap with the date the previous cycle ended
            //First, get the most recent cycle
            VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(getApplicationContext());
            VslaCycle mostRecentCycle = vslaCycleRepo.getMostRecentUnEndedCycle();

            if (null != mostRecentCycle) {
                if (isUpdateCycleAction && mostRecentCycle.getCycleId() == cycle.getCycleId()) {
                    //Fine
                } else {
                    //Check the Dates: use startDate vs DateEnded
                    if (cycle.getStartDate() == null) {
                        return false;
                    }
                    if (mostRecentCycle.getDateEnded() != null) {
                        if (cycle.getStartDate().before(mostRecentCycle.getDateEnded())) {
                            Utils.createAlertDialogOk(NewCycleActivity.this, dialogTitle,
                                    String.format("The start date of this cycle should be after the share-out date of the previous cycle, which was: %s.", Utils.formatDate(mostRecentCycle.getDateEnded())),
                                    Utils.MSGBOX_ICON_EXCLAMATION).show();
                            return false;
                        }
                    }
                }
            }

            //Validation specific to getting started wizard
            //Validate Amount of interest
            EditText txtInterestCollectedSoFar = (EditText) findViewById(R.id.txtNCInterestCollectedSoFar);
            String interest = txtInterestCollectedSoFar.getText().toString().trim();
            if (interest.length() < 1) {
                if (isUpdateCycleAction) {
                    cycle.setInterestAtSetup(cycle.getInterestAtSetup());
                } else {
                    cycle.setInterestAtSetup(0);
                }
            } else {
                double interestSoFar = Double.parseDouble(interest);
                if (interestSoFar < 0.00) {
                    displayMessageBox(dialogTitle, "Total amount of Interest collected in Current Cycle so far should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtInterestCollectedSoFar.requestFocus();
                    return false;
                } else {
                    cycle.setInterestAtSetup(interestSoFar);
                }
            }

            //Validate Amount of fines
            EditText txtFinesCollectedSoFar = (EditText) findViewById(R.id.txtNCFinesCollectedSoFar);
            String fines = txtFinesCollectedSoFar.getText().toString().trim();
            if (fines.length() < 1) {
                if (isUpdateCycleAction) {
                    cycle.setFinesAtSetup(cycle.getFinesAtSetup());
                } else {
                    cycle.setFinesAtSetup(0);
                }
            } else {
                double finesSoFar = Double.parseDouble(fines);
                if (finesSoFar < 0.00) {
                    displayMessageBox(dialogTitle, "Total amount of Fines collected in Current Cycle so far should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtFinesCollectedSoFar.requestFocus();
                    return false;
                } else {
                    cycle.setFinesAtSetup(finesSoFar);
                }
            }

            if (isUpdateCycleAction) {
                EditText txtNCInterestCorrectionComment = (EditText) findViewById(R.id.txtNCMiddleCycleInterestCorrectionComment);
                if (null != txtNCInterestCorrectionComment) {
                    if (!txtNCInterestCorrectionComment.getText().toString().isEmpty() && txtNCInterestCorrectionComment.isShown()) {
                        cycle.setInterestAtSetupCorrectionComment(txtNCInterestCorrectionComment.getText().toString());
                    }
                }

                EditText txtNCFinesCorrectionComment = (EditText) findViewById(R.id.txtNCMiddleCycleFinesCorrectionComment);
                if (null != txtNCFinesCorrectionComment) {
                    if (!txtNCFinesCorrectionComment.getText().toString().isEmpty() && txtNCFinesCorrectionComment.isShown()) {
                        cycle.setFinesAtSetupCorrectionComment(txtNCFinesCorrectionComment.getText().toString());
                    }
                }
            }


            //Set the Cycle as Active
            cycle.activate();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected void displayMessageBox(String title, String message, String icon) {
        alertDialog = new AlertDialog.Builder(NewCycleActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_EXCLAMATION)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_TICK)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_QUESTION)) {
            alertDialog.setIcon(R.drawable.phone);
        }

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                //Can I pass a method delegate? Or function pointer? for what to be executed?
                // Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                if (successAlertDialogShown) {
                    Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                    startActivity(i);
                    successAlertDialogShown = false;
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    protected void populateDataFields(final VslaCycle cycle) {
        // Clear Fields
        clearDataFields();

        if (cycle == null) {
            return;
        }

        try {
            // Now populate
            TextView txtSharePrice = (TextView) findViewById(R.id.txtNCSharePrice);
            final Spinner cboMaxShareQty = (Spinner) findViewById(R.id.cboNCMaxShares);
            TextView txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
            TextView txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
            TextView txtInterestRate = (TextView) findViewById(R.id.txtNCInterestRate);

            txtSharePrice.setText(Utils.formatRealNumber(cycle.getSharePrice()));
            //Fix... select spinner on post creation
            //fix for failure to select these values
            cboMaxShareQty.post(new Runnable() {
                @Override
                public void run() {
                    Utils.setSpinnerSelection(String.format("%.0f", cycle.getMaxSharesQty()), cboMaxShareQty); //format shares qty with no decimal points so that the Utils can select it correctly
                }
            });

            //cboMaxShareQty.setSelection(6 , true);
            txtStartDate.setText(Utils.formatDate(cycle.getStartDate(), "dd-MMM-yyyy"));
            txtEndDate.setText(Utils.formatDate(cycle.getEndDate(), "dd-MMM-yyyy"));
            txtInterestRate.setText(Utils.formatRealNumber(cycle.getInterestRate()));
        } catch (Exception ex) {

        }
    }

    protected void clearDataFields() {
        buildMaxSharesSpinner();
        try {
            // Now populate
            TextView txtSharePrice = (TextView) findViewById(R.id.txtNCSharePrice);
            Spinner cboMaxShareQty = (Spinner) findViewById(R.id.cboNCMaxShares);
            TextView txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
            TextView txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
            TextView txtInterestRate = (TextView) findViewById(R.id.txtNCInterestRate);

            txtSharePrice.setText("");
            cboMaxShareQty.setSelection(0);
            txtStartDate.setText("");
            txtEndDate.setText("");
            txtInterestRate.setText("");
        } catch (Exception ex) {
            Log.d("NewCycleActivity", "Initialization Failed!");
        }

    }

    /* Populates the max shares spinner  */
    public void buildMaxSharesSpinner() {

        Spinner cboNCMaxShares = (Spinner) findViewById(R.id.cboNCMaxShares);
        ArrayList<String> maxSharesArrayList = new ArrayList<String>();
        maxSharesArrayList.add("select number");
        for (int i = 1; i <= 100; i++) {
            maxSharesArrayList.add(i + "");
        }
        String[] maxSharesList = maxSharesArrayList.toArray(new String[maxSharesArrayList.size()]);
        maxSharesArrayList.toArray(maxSharesList);
        ArrayAdapter<CharSequence> maxSharesAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, maxSharesList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);
                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };

        maxSharesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboNCMaxShares.setAdapter(maxSharesAdapter);
        //cboNCMaxShares.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        // Make the spinner selectable
        cboNCMaxShares.setFocusable(true);
        cboNCMaxShares.setFocusableInTouchMode(true);
        cboNCMaxShares.setClickable(true);
    }

}