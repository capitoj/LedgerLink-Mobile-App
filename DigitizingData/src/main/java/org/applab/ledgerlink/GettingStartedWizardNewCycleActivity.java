package org.applab.ledgerlink;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.fontutils.TypefaceTextView;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by Moses on 6/27/13.
 */
public class GettingStartedWizardNewCycleActivity extends NewCycleActivity {

    private boolean _isFromReviewMembers = false;
    protected boolean isCycleValidated = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("_isFromReviewMembers")) {
            _isFromReviewMembers = getIntent().getBooleanExtra("_isFromReviewMembers", false);

        }

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_new_cycle_getting_started_wizard);

        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
        selectedCycle = repo.getCurrentCycle();

        if (selectedCycle != null) {
            //isUpdateCycleAction = getIntent().getBooleanExtra("_isUpdateCycleAction",false);
            isUpdateCycleAction = true;
        }

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;

        actionBar = getSupportActionBar();
        if(_isFromReviewMembers) {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_exit_done, null);

            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveMiddleCycleData(true);
                        }
                    }
            );

            customActionBarView.findViewById(R.id.actionbar_exit).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                            System.exit(0);
                        }
                    }
            );
        }
        else {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_next, null);
            customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveMiddleCycleData(true);
                        }
                    }
            );
        }




        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.get_started_allcaps);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        // Populate Max Shares Spinner
        //super.buildMaxSharesSpinner();
        super.buildInterestTypeSpinner();

        txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
        txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);

        //Set onClick Listeners to load the DateDialog for startDate
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView) view;
                settingStartDate = true;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingStartedWizardNewCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_cycle_start_date));
                datePickerDialog.show();
            }
        });

        //Set onClick Listeners to load the DateDialog for endDate
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewClicked = (TextView) view;
                settingStartDate = false;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingStartedWizardNewCycleActivity.this, mDateSetListener, mEndYear, mEndMonth, mEndDay);
                datePickerDialog.setTitle(getString(R.string.set_cycle_end_date));
                datePickerDialog.show();
            }
        });

        TypefaceTextView headerText = (TypefaceTextView) findViewById(R.id.lblNCHeading);
        SpannableStringBuilder headingInstruction = new SpannableStringBuilder(getString(R.string.enter_all_cycle_info));
        SpannableString nextText = new SpannableString(getString(R.string.next_));
        nextText.setSpan(new StyleSpan(Typeface.BOLD), 0,nextText.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        headingInstruction.append(nextText);
        headerText.setText(headingInstruction);

        //The heading
        TextView lblNewCycleHeading = (TextView) findViewById(R.id.lblNewCycleHeading);
        //this should only be shown in review action
        lblNewCycleHeading.setVisibility(_isFromReviewMembers ? View.VISIBLE : View.GONE);

        if (isUpdateCycleAction) {
            //Setup the Fields by getting the current Cycle

            //Not from add members activity, change the labels
            if (_isFromReviewMembers) {
                //TextView heading = (TextView) findViewById(R.id.lblNewCycleHeading);
                //heading.setText("Review Cycle Information");
                lblNewCycleHeading.setText(R.string.review_cycle_info);
                headerText = (TypefaceTextView) findViewById(R.id.lblNCHeading);
                headerText.setText(R.string.review_and_confirm_all_info_correct_any_errors);
            }


            if (selectedCycle != null) {
                //displayMessageBox("Testing", "Cycle to Update Found", Utils.MSGBOX_ICON_INFORMATION);
                //Populate Fields
                populateDataFields(selectedCycle);

                //Set the fields required by the DatePicker
                final Calendar c = Calendar.getInstance();
                c.setTime(selectedCycle.getStartDate());
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                c.setTime(selectedCycle.getEndDate());
                mEndYear = c.get(Calendar.YEAR);
                mEndMonth = c.get(Calendar.MONTH);
                mEndDay = c.get(Calendar.DAY_OF_MONTH);

                //Set current GSW stage
                VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
                vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_REVIEW_CYCLE);
            } else {
                TextView txtInstructions = (TextView) findViewById(R.id.lblNCHeader);
                txtInstructions.setText(getString(R.string.new_cycle_be_created)
                );

                //setup default dates
                setupDefaultDates();

                //Convert it to New Cycle operation
                isUpdateCycleAction = false;
            }
        } else {
            //displayMessageBox("Testing", "Cycle to Update NOT Found", MSGBOX_ICON_INFORMATION);
            //Setup the Default Date
            setupDefaultDates();
            //Set the current stage of the wizard
            VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
            vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_NEW_CYCLE);
        }
    }


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
        /**final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.new_cycle, menu);
         //Hide cancel button since it does not exist in GSW
         MenuItem cancelItem = menu.findItem(R.id.mnuNCCancel);
         MenuItem doneItem = menu.findItem(R.id.mnuNCNext);
         cancelItem.setIcon(null);
         doneItem.setIcon(null);
         if(isUpdateCycleAction) {
         //menu.findItem(R.id.mnuNCCancel)
         doneItem.setTitle("done");
         cancelItem.setTitle("exit");
         }
         else {
         cancelItem.setVisible(false);
         }*/
        menu.clear();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, GettingStartedWizardPageOne.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {

                    // This activity is not part of the application's task, so
                    // create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, GettingStartedWizardPageOne.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {

                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }

        }
        return true;
    }

    private boolean saveMiddleCycleData(boolean warnOnHighInterest) {
        boolean successFlg = false;
        VslaCycle targetCycle = new VslaCycle();
        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
        if (selectedCycle != null) {
            targetCycle = selectedCycle;
        }

        if (validateGettingStartedData(targetCycle, warnOnHighInterest)) {
            boolean retVal = false;
            if (targetCycle.getCycleId() != 0) {
                retVal = repo.updateCycle(targetCycle);
            } else {
                retVal = repo.addCycle(targetCycle);
            }
            if (retVal) {
                if (targetCycle.getCycleId() == 0) {

                    //Set this new cycle as the selected one
                    //Retrieve the recently added cycle so as to get the id as well
                    VslaCycle cycle = repo.getMostRecentCycle();
                    cycle.setOutstandingBankLoanAtSetup(targetCycle.getOutstandingBankLoanAtSetup());
                    selectedCycle = cycle;

                    //TODO: Create Getting started wizard dummy meeting
                    retVal = repo.createGettingStartedDummyMeeting(selectedCycle);

                    if (!retVal) {
                        Log.d(getApplicationContext().getPackageName(), getString(R.string.failed_dummy_data_import_meeting));
                        Toast.makeText(this, R.string.error_occured_while_saving_info, Toast.LENGTH_LONG).show();
                    }

                    //displayMessageBox(dialogTitle, "The New Cycle has been added Successfully.", Utils.MSGBOX_ICON_TICK);
                } else {

                    //displayMessageBox("Update Cycle", "The Cycle has been updated Successfully.", Utils.MSGBOX_ICON_TICK);
                }
                /*
                String testJson = SendDataRepo.getVslaCycleJson(repo.getCurrentCycle());
                if (testJson.length() < 0) {
                    return false;
                }
                MemberRepo memberRepo = new MemberRepo(getApplicationContext());

                String membersJson = SendDataRepo.getMembersJson(memberRepo.getAllMembers());
                if (membersJson.length() < 0) {
                    return false;
                }
                */
                // Pass on the flag indicating whether this is an Update operation
                Intent i;
                if (isUpdateCycleAction && !_isFromReviewMembers) {

                    // Go to confirmation activity
                    i = new Intent(getApplicationContext(), GettingStartedConfirmationPage.class);
                    startActivity(i);
                    finish();
                } else {
                    i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                    i.putExtra("_isUpdateCycleAction", isUpdateCycleAction);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            } else {
                displayMessageBox(dialogTitle, getString(R.string.problem_while_capturing_cycle_data));
            }
        } else {
            if(isCycleValidated) {
                EditText txtNCInterestRate = (EditText) findViewById(R.id.txtNCInterestRate);
                String interestRate = txtNCInterestRate.getText().toString().trim();
                showDialogMsgBox(getString(R.string.warning), Utils.formatNumber(Double.parseDouble(interestRate)) + "% "+ getString(R.string.is_high_sure_this_correct_value));
                // displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
            }
        }

        return successFlg;
    }

    protected void showDialogMsgBox(String title, String message){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                saveMiddleCycleData(false);
            }
        };
        DialogMessageBox.show(this, title, message, runnable);
    }

    private boolean validateGettingStartedData(VslaCycle cycle, boolean warnOnHighInterest) {
        try {
            if (null == cycle) {
                return false;
            }
            //validate Data common to New Cycle
            if (validateData(cycle)) {
                isCycleValidated = true;
                if(cycle.getInterestRate() > 10){
                    if(warnOnHighInterest) {
                        return false;
                    }
                }
            }else{
                return false;
            }

            //Validation specific to getting started wizard
            //Validate Amount of interested collected so far
            TextView txtInterestCollected = (TextView) findViewById(R.id.txtNCInterestCollectedSoFar);
            String interestCollected = txtInterestCollected.getText().toString().trim();
            if (interestCollected.length() < 1) {
                //displayMessageBox(dialogTitle, "The Interest Collected Is Required", Utils.MSGBOX_ICON_EXCLAMATION);
                //txtInterestCollected.requestFocus();
                //return false;
                cycle.setInterestAtSetup(0);
            } else {
                double theInterestCollected = Double.parseDouble(interestCollected);
                if (theInterestCollected < 0.00) {
                    displayMessageBox(dialogTitle, getString(R.string.interest_collected_be_zero_and_above));
                    txtInterestCollected.requestFocus();
                    return false;
                } else {
                    cycle.setInterestAtSetup(theInterestCollected);
                }
            }


            //Validate Fines collected
            TextView txtFinesCollected = (TextView) findViewById(R.id.txtNCFinesCollectedSoFar);
            String finesCollected = txtFinesCollected.getText().toString().trim();
            if (finesCollected.length() < 1) {
//                displayMessageBox(dialogTitle, "The Fines Collected Field Is Required", Utils.MSGBOX_ICON_EXCLAMATION);
//                txtFinesCollected.requestFocus();
//                return false;
                cycle.setFinesAtSetup(0);
            } else {
                double theFinesCollected = Double.parseDouble(finesCollected);
                if (theFinesCollected < 0.00) {
                    displayMessageBox(dialogTitle, getString(R.string.fines_collected_be_zero_and_above));
                    txtFinesCollected.requestFocus();
                    return false;
                } else {
                    cycle.setFinesAtSetup(theFinesCollected);
                }
            }

            // Validate Interest Type data
            cboInterestType = (Spinner) findViewById(R.id.cboAMTypeOfInterest);
            if(cboInterestType.getSelectedItemPosition() == 0) {
                cycle.setTypeOfInterest(0);
                cboInterestType.setFocusableInTouchMode(true);
                cboInterestType.requestFocus();
            }else if(cboInterestType.getSelectedItemPosition() == 1){
                cycle.setTypeOfInterest(1);
                cboInterestType.setFocusableInTouchMode(true);
                cboInterestType.requestFocus();
            }

            //validate outstanding bank loan
            EditText txtNCOutstandingGroupBankLoan = (EditText) findViewById(R.id.txtNCOutstandingGroupBankLoan);
            String outstandingBankLoanSoFar = txtNCOutstandingGroupBankLoan.getText().toString().trim();
            if(outstandingBankLoanSoFar.length() < 1){
                cycle.setOutstandingBankLoanAtSetup(0);
            }else{
                double outstandingBankLoan = Double.parseDouble(outstandingBankLoanSoFar);
                if(outstandingBankLoan < 0.00){
                    displayMessageBox(dialogTitle, getString(R.string.outstanding_bank_loan_zero_and_above));
                    txtNCOutstandingGroupBankLoan.requestFocus();
                    return false;
                }else{
                    cycle.setOutstandingBankLoanAtSetup(outstandingBankLoan);
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected void populateDataFields(VslaCycle cycle) {
        // Clear Fields
        clearDataFields();

        if (cycle == null) {
            return;
        }
        super.populateDataFields(cycle);

        try {
            // Now populate specific to Getting started wizard
            EditText txtInterestCollectedSoFar = (EditText) findViewById(R.id.txtNCInterestCollectedSoFar);
            EditText txtFinesCollectedSoFar = (EditText) findViewById(R.id.txtNCFinesCollectedSoFar);
            EditText txtNCOutstandingGroupBankLoan = (EditText) findViewById(R.id.txtNCOutstandingGroupBankLoan);

            // TODO: Set the interest and fines for the middle start cycle
            txtInterestCollectedSoFar.setText(Utils.formatRealNumber(cycle.getInterestAtSetup()));
            txtFinesCollectedSoFar.setText(Utils.formatRealNumber(cycle.getFinesAtSetup()));
            txtNCOutstandingGroupBankLoan.setText(Utils.formatRealNumber(cycle.getOutstandingBankLoanAtSetup()));

        } catch (Exception ex) {

        }
    }

    protected void clearDataFields() {
        super.clearDataFields();
        buildInterestTypeSpinner();
        //buildMaxSharesSpinner();
        try {

            //Now Clear fields specific to GSWizard
            EditText txtInterestCollectedSoFar = (EditText) findViewById(R.id.txtNCInterestCollectedSoFar);
            EditText txtFinesCollectedSoFar = (EditText) findViewById(R.id.txtNCFinesCollectedSoFar);

            txtInterestCollectedSoFar.setText("");
            txtFinesCollectedSoFar.setText("");
        } catch (Exception ex) {

        }

    }

    protected void buildInterestTypeSpinner() {

        //Setup the Spinner Items
        cboInterestType = (Spinner) findViewById(R.id.cboAMTypeOfInterest);

        List<String> InterestTypes = new ArrayList<String>();
        InterestTypes.add("Flat Rate");
        InterestTypes.add("Reducing Rate");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, InterestTypes);
        // attaching data adapter to spinner
        cboInterestType.setAdapter(dataAdapter);

        //Make the spinner selectable
        cboInterestType.setFocusable(true);
        cboInterestType.setClickable(true);

        cboInterestType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {
//                    Toast.makeText(parent.getContext(),
//                            "You have selected Flat Rate", Toast.LENGTH_SHORT)
//                            .show();
                } else if (pos == 1) {
//                    Toast.makeText(parent.getContext(),
//                            "You have selected Reducing Rate", Toast.LENGTH_SHORT)
//                            .show();
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {

                // TODO Auto-generated method stub

            }
        });

    }


}