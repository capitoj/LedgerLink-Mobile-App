package org.applab.digitizingdata;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;




/**
 * Created by Moses on 6/27/13.
 */
public class GettingsStartedWizardNewCycleActivity extends NewCycleActivity {

    protected boolean _isFromReviewMembers = false;


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
                            saveMiddleCycleData();
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
                            saveMiddleCycleData();
                        }
                    }
            );
        }




        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("GET STARTED");

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        // Populate Max Shares Spinner
        super.buildMaxSharesSpinner();

        txtStartDate = (TextView) findViewById(R.id.txtNCStartDate);
        txtEndDate = (TextView) findViewById(R.id.txtNCEndDate);

        //Set onClick Listeners to load the DateDialog for startDate
        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //I want the Event Handler to handle both startDate and endDate
                viewClicked = (TextView) view;
                settingStartDate = true;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingsStartedWizardNewCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set cycle start date");
                datePickerDialog.show();
            }
        });

        //Set onClick Listeners to load the DateDialog for endDate
        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewClicked = (TextView) view;
                settingStartDate = false;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingsStartedWizardNewCycleActivity.this, mDateSetListener, mEndYear, mEndMonth, mEndDay);
                datePickerDialog.setTitle("Set cycle end date");
                datePickerDialog.show();
            }
        });

        TypefaceTextView headerText = (TypefaceTextView) findViewById(R.id.lblNCHeading);
        SpannableStringBuilder headingInstruction = new SpannableStringBuilder("Enter all cycle information then press ");
        SpannableString nextText = new SpannableString("next.");
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
                lblNewCycleHeading.setText("Review Cycle information");
                headerText = (TypefaceTextView) findViewById(R.id.lblNCHeading);
                headerText.setText("Review and confirm that all information is correct. Correct any errors.");
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
                txtInstructions.setText("There is no cycle that is currently running. A New Cycle will be created."
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
        /**final MenuInflater inflater = getSupportMenuInflater();
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

    private boolean saveMiddleCycleData() {
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
            } else {

                retVal = repo.addCycle(cycle);
            }
            if (retVal) {
                if (cycle.getCycleId() == 0) {

                    //Set this new cycle as the selected one
                    //Retrieve the recently added cycle so as to get the id as well
                    cycle = repo.getMostRecentCycle();
                    selectedCycle = cycle;

                    //TODO: Create Getting started wizard dummy meeting
                    retVal = repo.createGettingStartedDummyMeeting(selectedCycle);


                    if (!retVal) {
                        Log.d(getApplicationContext().getPackageName(), "Failed to create the dummy data import meeting");
                        Toast.makeText(this, "An error occured while saving the information", Toast.LENGTH_LONG).show();
                    }

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

                // Pass on the flag indicating whether this is an Update operation
                Intent i;
                if (isUpdateCycleAction && _isFromReviewMembers) {

                    // Go to confirmation activity
                    i = new Intent(getApplicationContext(), GettingStartedConfirmationPage.class);
                    startActivity(i);
                    finish();
                } else {
                    i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                    i.putExtra("_isUpdateCycleAction", isUpdateCycleAction);
                    startActivity(i);
                    finish();
                }
            } else {
                displayMessageBox(dialogTitle, "A problem occurred while capturing the Cycle Data. Please try again.", Utils.MSGBOX_ICON_EXCLAMATION);
            }
        } else {

            // displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }


    private boolean validateGettingStartedData(VslaCycle cycle) {
        try {
            if (null == cycle) {
                return false;
            }

            //validate Data common to New Cycle
            if (!validateData(cycle)) {
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
                    displayMessageBox(dialogTitle, "The Interest Collected should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
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
                    displayMessageBox(dialogTitle, "The Fines Collected should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtFinesCollected.requestFocus();
                    return false;
                } else {
                    cycle.setFinesAtSetup(theFinesCollected);
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
            EditText txtInterestCollecteSoFar = (EditText) findViewById(R.id.txtNCInterestCollectedSoFar);
            EditText txtFinesCollectedSoFar = (EditText) findViewById(R.id.txtNCFinesCollectedSoFar);

            //TODO: Set the interest and fines for the middle start cycle
            txtInterestCollecteSoFar.setText(Utils.formatRealNumber(cycle.getInterestAtSetup()));
            txtFinesCollectedSoFar.setText(Utils.formatRealNumber(cycle.getFinesAtSetup()));

        } catch (Exception ex) {

        }
    }

    protected void clearDataFields() {
        super.clearDataFields();
        buildMaxSharesSpinner();
        try {
            //Now Clear fields specific to GSWizard
            EditText txtInterestCollecteSoFar = (EditText) findViewById(R.id.txtNCInterestCollectedSoFar);
            EditText txtFinesCollectedSoFar = (EditText) findViewById(R.id.txtNCFinesCollectedSoFar);

            txtInterestCollecteSoFar.setText("");
            txtFinesCollectedSoFar.setText("");
        } catch (Exception ex) {

        }

    }

    /* Populates the max shares spinner  */
    /**private void buildMaxSharesSpinner() {

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
                ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

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
        cboNCMaxShares.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        // Make the spinner selectable
        cboNCMaxShares.setFocusable(true);
        cboNCMaxShares.setFocusableInTouchMode(true);
        cboNCMaxShares.setClickable(true);
    } */

}