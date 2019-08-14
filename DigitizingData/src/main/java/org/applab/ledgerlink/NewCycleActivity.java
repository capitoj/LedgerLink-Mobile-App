package org.applab.ledgerlink;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.repo.SendDataRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/27/13.
 */
public class NewCycleActivity extends ActionBarActivity{
    ActionBar actionBar;

    TextView txtStartDate;
    TextView txtEndDate;
    TextView viewClicked;
    boolean settingStartDate = false;
    protected static final int Date_dialog_id = 1;
    // date and time
    int mYear;
    int mMonth;
    int mDay;
    int mEndYear;
    int mEndMonth;
    int mEndDay;
    final String dialogTitle = getString(R.string.new_cycle);
    private boolean successAlertDialogShown = false;
    boolean isUpdateCycleAction = false;
    private boolean multipleCyclesIndicator = false;
    private boolean isCycleValidated = false;

    LedgerLinkApplication ledgerLinkApplication;

    VslaCycle selectedCycle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
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
            actionBar.setTitle(getString(R.string.edit_cycle));
        } else {
            actionBar.setTitle(getString(R.string.new_cycle));
        }

        txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
        txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.sectionNCMiddleCycleStart);
        linearLayout.setVisibility(View.GONE);


        //Set onClick Listeners to load the DateDialog for startDate
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView) view;
                settingStartDate = true;
                DatePickerDialog datePickerDialog = new DatePickerDialog(NewCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_cycle_start_date));
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
                datePickerDialog.setTitle(getString(R.string.set_cycle_end_date));
                datePickerDialog.show();
            }
        });

        if (isUpdateCycleAction) {
            // Setup the Fields by getting the current Cycle

            if (!getIntent().hasExtra("_cycleId")) {
                selectedCycle = ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle();
            } else if (getIntent().getIntExtra("_cycleId", 0) != 0) {
                //for concurrent cycles , if cycle id is passed then load the specified cycle
                selectedCycle = ledgerLinkApplication.getVslaCycleRepo().getCycle(getIntent().getIntExtra("_cycleId", 0));
            } else {
                selectedCycle = ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle();
            }
            if (selectedCycle != null) {
                Log.e("LoanFromBankX", String.valueOf(selectedCycle.getOutstandingBankLoanAtSetup()));
                //displayMessageBox("Testing", "Cycle to Update Found", Utils.MSGBOX_ICON_INFORMATION);
                //Change the title in edit mode
                TextView lblNCHeader = (TextView) findViewById(R.id.lblNCHeader);
                lblNCHeader.setText(getString(R.string.edit_cycle_beginning) + Utils.formatDate(selectedCycle.getStartDate(), "dd MM yyyy") + " and ending " + Utils.formatDate(selectedCycle.getEndDate(), "dd MMM yyyy") + ".");
                //Populate Fields
                populateDataFields(selectedCycle);

                // Populate GSW fields
                // If cycle has no GSW data don't show GSW fields
                Meeting dummyGSWMeeting = ledgerLinkApplication.getMeetingRepo().getDummyGettingStartedWizardMeeting();
                if (dummyGSWMeeting != null) {
                    if (selectedCycle.getCycleId() != dummyGSWMeeting.getVslaCycle().getCycleId()) {
                        //linearLayout = (LinearLayout) findViewById(R.id.sectionNCMiddleCycleStart);
                        linearLayout.setVisibility(View.GONE);
                    } else if (selectedCycle.getInterestAtSetup() == 0 && selectedCycle.getFinesAtSetup() == 0) {
                        //linearLayout.setVisibility(View.GONE);
                    } else {

                        linearLayout.setVisibility(View.VISIBLE);
                        TextView lblNCMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblNCMiddleCycleInformationHeading);

                        TextView txtInterestCollectedSoFar = (TextView) findViewById(R.id.lblNCMiddleCycleInterestCollectedSoFar);
                        TextView txtFinesCollectedSoFar = (TextView) findViewById(R.id.lblNCMiddleCycleFinesCollectedSoFar);
                        TextView lblNCDisbursedOutstandingBankLoanAmountSoFar = (TextView) findViewById(R.id.lblNCDisbursedOutstandingBankLoanAmountSoFar);
                        txtInterestCollectedSoFar.setText(String.format("%.0f UGX", selectedCycle.getInterestAtSetup()));
                        txtFinesCollectedSoFar.setText(String.format("%.0f UGX", selectedCycle.getFinesAtSetup()));
                        lblNCDisbursedOutstandingBankLoanAmountSoFar.setText(String.format("%.0f UGX", dummyGSWMeeting.getLoanFromBank()));

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

                        lblNCMiddleCycleInformationHeading.setText(R.string.info_added_after_cycle_started);

                        if (dummyGSWMeeting != null) {
                            lblNCMiddleCycleInformationHeading.setText(getString(R.string.info_added_on) + Utils.formatDate(dummyGSWMeeting.getMeetingDate(), "dd MMM yyyy") + " after the cycle started. Here are the interest and fines collected by that day.");
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
                txtInstructions.setText(R.string.new_cycle_be_created
                );

                //setup default dates
                setupDefaultDates();

                //Convert it to New Cycle operation
                isUpdateCycleAction = false;
                actionBar.setTitle(getString(R.string.new_cycle));
            }
        } else {
            //displayMessageBox("Testing", "Cycle to Update NOT Found", MSGBOX_ICON_INFORMATION);
            //Setup the Default Date
            setupDefaultDates();

        }

        // Populate Max Shares Spinner
        //buildMaxSharesSpinner();
    }

    /* inflates custom menu bar for review members */
    void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        View customActionBarView = null;
        actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        actionBar.setDisplayHomeAsUpEnabled(true);

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_back_next_done, null);

        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Save this as long task
                        boolean savedStatus = saveCycleData(true);
                        if (savedStatus) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else {
                            if (isCycleValidated) {

                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        saveCycleData(false);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                };
                                EditText txtNCInterestRate = (EditText) findViewById(R.id.txtNCInterestRate);
                                int interestRate = Integer.valueOf(txtNCInterestRate.getText().toString().trim());
                                DialogMessageBox.show(NewCycleActivity.this, getString(R.string.warning_new_cycle), Utils.formatNumber(interestRate) + getString(R.string.entered_correct_interest_rate), runnable);
                            }
                        }
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
                        boolean savedStatus = saveCycleData(true);
                        if (savedStatus) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else {
                            if (isCycleValidated) {

                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        saveCycleData(false);
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                };
                                DialogMessageBox.show(NewCycleActivity.this, getString(R.string.warning_new_cycle), Utils.formatNumber(selectedCycle.getInterestRate()) + "% is high. Are you sure you entered the correct interest rate", runnable);
                            }
                        }
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


    void setupDefaultDates() {
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
    final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
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
        dialog.setTitle(R.string.set_cycle_date);

    }

    //Displays the selected Date in the TextView
    void updateDisplay() {
        if (viewClicked != null) {
            viewClicked.setText(String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth + 1) + "-" + mYear);

            //Default the End Date to StartDate + 52 weeks
            if (settingStartDate && txtEndDate != null) {
                settingStartDate = false;

                String endDateString = String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth + 1) + "-" + (mYear + 1);
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
            theField.setText(String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth + 1) + "-" + mYear);
        }
    }

    void updateDisplay(TextView theField, int theYear, int theMonth, int theDay) {
        if (theField != null) {
            theField.setText(String.format("%02d", theDay) + "-" + Utils.getMonthNameAbbrev(theMonth) + "-" + theYear);
        }
    }

    /**
     * @Override public boolean onCreateOptionsMenu(Menu menu) {
     * final MenuInflater inflater = getMenuInflater();
     * inflater.inflate(R.menu.new_cycle, menu);
     * return true;
     * }
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

    private boolean saveCycleData(boolean warnOnHighInterest) {
        boolean isCycleSaved = false;
        VslaCycle cycle = new VslaCycle();
        if(selectedCycle != null){
            cycle = selectedCycle;
        }
        if(validateData(cycle)){
            isCycleValidated = true;
            if(cycle.getInterestRate() > 10){
                if(warnOnHighInterest){
                    isCycleSaved = false;
                }else{
                    this.executeCycleTask(cycle);
                    isCycleSaved = true;
                }
            }else{
                this.executeCycleTask(cycle);
                isCycleSaved = true;
            }
        }
        return isCycleSaved;
    }

    private void executeCycleTask(VslaCycle cycle){
        final VslaCycle finalCycle = cycle;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean successflag = saveCycleDataToDb(finalCycle);
                if(successflag){

                }
            }
        };
        LongTaskRunner.runLongTask(runnable, getString(R.string.please_wait), getString(R.string.saving_cycle_info), NewCycleActivity.this);
    }

    protected void saveDummyMeetingData(VslaCycle cycle){
        Meeting dummyGSWMeeting = ledgerLinkApplication.getMeetingRepo().getDummyGettingStartedWizardMeeting();
        dummyGSWMeeting.setLoanFromBank(cycle.getOutstandingBankLoanAtSetup());
        ledgerLinkApplication.getMeetingRepo().updateMeeting(dummyGSWMeeting);
    }

    private boolean saveCycleDataToDb(VslaCycle cycle) {
        boolean retVal = false;
        if (cycle.getCycleId() != 0) {
            retVal = ledgerLinkApplication.getVslaCycleRepo().updateCycle(cycle);
            if(retVal){
                saveDummyMeetingData(cycle);
            }
        } else {
            retVal = ledgerLinkApplication.getVslaCycleRepo().addCycle(cycle);
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

            /*
            String testJson = SendDataRepo.getVslaCycleJson(ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle());
            if (testJson.length() < 0) {
                return false;
            }
            //MemberRepo memberRepo = new MemberRepo(getApplicationContext());

            String membersJson = SendDataRepo.getMembersJson(ledgerLinkApplication.getMemberRepo().getAllMembers());
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
            */
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

    boolean validateData(VslaCycle cycle) {
        try {
            if (null == cycle) {
                return false;
            }

            // Validate: SharePrice
            TextView txtSharePrice = (TextView) findViewById(R.id.txtNCSharePrice);
            String sharePrice = txtSharePrice.getText().toString().trim();
            if (sharePrice.length() < 1) {
                displayMessageBox(dialogTitle, getString(R.string.share_price_required));
                txtSharePrice.requestFocus();
                return false;
            } else {
                double theSharePrice = Double.parseDouble(sharePrice);
                if (theSharePrice <= 0.00) {
                    displayMessageBox(dialogTitle, getString(R.string.share_price_be_positive));
                    txtSharePrice.requestFocus();
                    return false;
                } else {
                    if(theSharePrice < 100){
                        displayMessageBox(dialogTitle, getString(R.string.share_price_not_less_than));
                        txtSharePrice.requestFocus();
                        return false;
                    }else if(theSharePrice > 100000){
                        displayMessageBox(dialogTitle, getString(R.string.share_price_more_than));
                        txtSharePrice.requestFocus();
                        return false;
                    }else {
                        cycle.setSharePrice(theSharePrice);
                    }
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
            EditText txtNCMaxShares = (EditText)findViewById(R.id.txtNCMaxShares);
            if(txtNCMaxShares.getText().toString().length() == 0){
                Utils.createAlertDialogOk(this, dialogTitle, getString(R.string.maximum_share_quantity_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtNCMaxShares.requestFocus();
                return false;
            }else{
                int theMaxShareQty = Integer.valueOf(txtNCMaxShares.getText().toString());
                if(theMaxShareQty <= 0){
                    displayMessageBox(dialogTitle, getString(R.string.maximum_share_quautity_be_positive));
                    txtNCMaxShares.requestFocus();
                    return false;
                }else{
                    cycle.setMaxSharesQty(theMaxShareQty);
                }
            }


            /*
            Spinner cboMaxShareQty = (Spinner) findViewById(R.id.cboNCMaxShares);
            if (cboMaxShareQty.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dialogTitle, "The Maximum Share Quantity is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboMaxShareQty.requestFocus();
                return false;
            } else {
                String maxShareQty = cboMaxShareQty.getSelectedItem().toString().trim();
                int theMaxShareQty = Integer.valueOf(maxShareQty);
                if (theMaxShareQty <= 0) {
                    displayMessageBox(dialogTitle, "The Maximum Share Quantity must be positive.");
                    cboMaxShareQty.requestFocus();
                    return false;
                } else {
                    cycle.setMaxSharesQty(theMaxShareQty);
                }
            }*/

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
                displayMessageBox(dialogTitle, getString(R.string.end_date_be_after_start_date));
                txtEndDate.requestFocus();
                return false;
            } else {
                cycle.setEndDate(dtEnd);
            }

            // Validate: MaxShareAmount
            TextView txtInterestRate = (TextView) findViewById(R.id.txtNCInterestRate);
            String interestRate = txtInterestRate.getText().toString().trim();
            if (interestRate.length() < 1) {
                displayMessageBox(dialogTitle, getString(R.string.interest_rate_requred));
                txtInterestRate.requestFocus();
                return false;
            } else {
                double theInterestRate = Double.parseDouble(interestRate);
                if (theInterestRate < 1 || theInterestRate > 20) {
                    displayMessageBox(dialogTitle, getString(R.string.interest_rate_btn_));
                    txtInterestRate.requestFocus();
                    return false;
                } else {
                    cycle.setInterestRate(theInterestRate);
                }
            }

            //Check that the Cycle Start Date does not overlap with the date the previous cycle ended
            //First, get the most recent cycle
             VslaCycle mostRecentCycle = ledgerLinkApplication.getVslaCycleRepo().getMostRecentUnEndedCycle();

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
                                    String.format(getString(R.string.start_date_after_share_out_date), Utils.formatDate(mostRecentCycle.getDateEnded())),
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
                    displayMessageBox(dialogTitle, getString(R.string.total_amount_of_interest_collected_so_far_be_zero_and_above));
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
                    displayMessageBox(dialogTitle, getString(R.string.total_aounmt_fine_collected_so_far_be_zero_and_above));
                    txtFinesCollectedSoFar.requestFocus();
                    return false;
                } else {
                    cycle.setFinesAtSetup(finesSoFar);
                }
            }

            //Validate outstanding bank loan
            EditText txtNCOutstandingGroupBankLoan = (EditText) findViewById(R.id.txtNCOutstandingGroupBankLoan);
            String outstandingBankLoan = txtNCOutstandingGroupBankLoan.getText().toString().trim();
            if(outstandingBankLoan.length() < 1){
                if(isUpdateCycleAction){
                    cycle.setOutstandingBankLoanAtSetup(cycle.getOutstandingBankLoanAtSetup());
                }else{
                    cycle.setOutstandingBankLoanAtSetup(0);
                }
            }else{
                double outstandingBankLoanSoFar = Double.parseDouble(outstandingBankLoan);
                if(outstandingBankLoanSoFar < 0.00){
                    displayMessageBox(dialogTitle, getString(R.string.total_amount_outstanding_bank_loan_be_zero_and_above));
                    txtNCOutstandingGroupBankLoan.requestFocus();
                    return false;
                }else{
                    cycle.setOutstandingBankLoanAtSetup(outstandingBankLoanSoFar);
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

    protected void displayMessageBox(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(NewCycleActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (Utils.MSGBOX_ICON_EXCLAMATION.equalsIgnoreCase(Utils.MSGBOX_ICON_EXCLAMATION)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (Utils.MSGBOX_ICON_EXCLAMATION.equalsIgnoreCase(Utils.MSGBOX_ICON_TICK)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (Utils.MSGBOX_ICON_EXCLAMATION.equalsIgnoreCase(Utils.MSGBOX_ICON_QUESTION)) {
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
            EditText txtNCMaxShares = (EditText)findViewById(R.id.txtNCMaxShares);
            //final Spinner cboMaxShareQty = (Spinner) findViewById(R.id.cboNCMaxShares);
            TextView txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
            TextView txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
            TextView txtInterestRate = (TextView) findViewById(R.id.txtNCInterestRate);

            txtSharePrice.setText(Utils.formatRealNumber(cycle.getSharePrice()));
            //Fix... select spinner on post creation
            //fix for failure to select these values
            /*
            cboMaxShareQty.post(new Runnable() {
                @Override
                public void run() {
                    Utils.setSpinnerSelection(String.format("%.0f", cycle.getMaxSharesQty()), cboMaxShareQty); //format shares qty with no decimal points so that the Utils can select it correctly
                }
            });*/

            //cboMaxShareQty.setSelection(6 , true);
            txtNCMaxShares.setText(Utils.formatRealNumber(cycle.getMaxSharesQty()));
            txtStartDate.setText(Utils.formatDate(cycle.getStartDate(), "dd-MMM-yyyy"));
            txtEndDate.setText(Utils.formatDate(cycle.getEndDate(), "dd-MMM-yyyy"));
            txtInterestRate.setText(Utils.formatRealNumber(cycle.getInterestRate()));
        } catch (Exception ex) {

        }
    }

    protected void clearDataFields() {
        //buildMaxSharesSpinner();
        try {
            // Now populate
            TextView txtSharePrice = (TextView) findViewById(R.id.txtNCSharePrice);
            //Spinner cboMaxShareQty = (Spinner) findViewById(R.id.cboNCMaxShares);
            EditText txtNCMaxShares = (EditText)findViewById(R.id.txtNCMaxShares);
            TextView txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
            TextView txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);
            TextView txtInterestRate = (TextView) findViewById(R.id.txtNCInterestRate);

            txtSharePrice.setText("");
            txtNCMaxShares.setText("");
            //cboMaxShareQty.setSelection(0);
            txtStartDate.setText("");
            txtEndDate.setText("");
            txtInterestRate.setText("");
        } catch (Exception ex) {
            Log.d(getString(R.string.newcycleactivity), getString(R.string.initialization_faile));
        }

    }

}