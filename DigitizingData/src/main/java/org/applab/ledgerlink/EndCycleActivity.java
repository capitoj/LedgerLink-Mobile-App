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
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.VslaCycleRepo;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/27/13.
 */
public class EndCycleActivity extends ActionBarActivity{

    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private TextView txtShareOutDate;
    private boolean successAlertDialogShown = false;
    private boolean multipleCyclesIndicator = false;

    private VslaCycle selectedCycle;
    private ArrayList<VslaCycle> vslaCycles;

    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_end_cycle);

        if (getIntent().hasExtra("_multipleCycles")) {
            multipleCyclesIndicator = getIntent().getBooleanExtra("_multipleCycles", false);
        }

        inflateCustombar();

        // Solve the auto focus
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        /** actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);*/

        txtShareOutDate = (TextView) findViewById(R.id.txtECShareOutDate);
        TextView txtInstructions = (TextView) findViewById(R.id.lblECInstruction);

        //Set onClick Listeners to load the DateDialog for startDate
        txtShareOutDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(EndCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        //Setup the Fields by getting the current Cycle
        //VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());

        //Deal with the radio buttons
        // RadioGroup grpCycleDates = (RadioGroup)findViewById(R.id.grpECExistingCycles);

        //Retrieve all the active cycles
        ArrayList<VslaCycle> activeCycles = ledgerLinkApplication.getVslaCycleRepo().getActiveCycles();

        //Create radio buttons dynamically
        /**if(activeCycles != null) {
         for(VslaCycle cycle: activeCycles) {
         RadioButton radCycle = new RadioButton(this);
         String cycleDates = String.format("%s - %s", Utils.formatDate(cycle.getStartDate(), getString(R.string.date_format)),
         Utils.formatDate(cycle.getEndDate(), getString(R.string.date_format)));
         radCycle.setText(cycleDates);
         radCycle.setId(cycle.getCycleId());
         //radCycle.setTextColor();
         radCycle.setTextSize(16);
         radCycle.setTag(cycle); //Store the VslaCycle object in the Tag property of the radio button
         radCycle.setTextColor(txtInstructions.getTextColors());
         grpCycleDates.addView(radCycle);

         if(activeCycles.size() == 1) {
         radCycle.setChecked(true);
         grpCycleDates.setVisibility(View.GONE);
         }
         }
         } */

        /**  if(activeCycles != null && activeCycles.size()>0) {

         // multiple cycle indicator
         multipleCycles = true;

         }
         */

        //Populate Fields
        //  if(activeCycles.size() == 1) {
        //    if(selectedCycle == null) {
        //      selectedCycle = activeCycles.get(0);
        // }

        if (!getIntent().hasExtra("_cycleId")) {
            //repo = new VslaCycleRepo(getApplicationContext());
            selectedCycle = ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle();
        } else if (getIntent().getIntExtra("_cycleId", 0) != 0) {
            //for concurrent cycles , if cycle id is passed then load the specified cycle
            //repo = new VslaCycleRepo(getApplicationContext());
            selectedCycle = ledgerLinkApplication.getVslaCycleRepo().getCycle(getIntent().getIntExtra("_cycleId", 0));
        } else {
            //repo = new VslaCycleRepo(getApplicationContext());
            selectedCycle = ledgerLinkApplication.getVslaCycleRepo().getCurrentCycle();
        }
        if (selectedCycle != null) {
            //displayMessageBox("Testing", "Cycle to Update Found", Utils.MSGBOX_ICON_INFORMATION);
            //Change the title in edit mode
            txtInstructions.setText(getString(R.string.enter_share_out_date_for_cycle_begining) + Utils.formatDate(selectedCycle.getStartDate(), getString(R.string.date_format)) + " and ending " + Utils.formatDate(selectedCycle.getEndDate(), getString(R.string.date_format)) + ".");

            txtShareOutDate.setText(Utils.formatDate(selectedCycle.getEndDate(), "dd-MMM-yyyy"));

            //Setup the Default Date: Not sure what this is for. LOL!
            final Calendar c = Calendar.getInstance();
            c.setTime(selectedCycle.getEndDate());
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            updateDisplay(txtShareOutDate, mYear, mMonth + 1, mDay);

            /** txtInstructions.setText(new StringBuilder()
             .append("The current cycle end date is " + Utils.formatDate(selectedCycle.getEndDate(), "dd-MMM-yyyy"))
             .append(". If your cycle has ended, enter the share out amount.")
             .toString()
             ); */
        }
        // }
        /**   else {
         txtInstructions.setText(new StringBuilder()
         .append("There is more than one unfinished cycle. Select the cycle to end.")
         .toString()
         );
         }*/
        /**else {
         txtInstructions.setText(new StringBuilder()
         .append("There is no cycle that is currently running")
         .toString()
         );
         //Setup the Default Date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
         updateDisplay(txtShareOutDate, mYear, mMonth+1,mDay);

         return;
         }*/

        //Setup the Checked Listener
        /**  grpCycleDates.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
         {
         public void onCheckedChanged(RadioGroup group, int checkedId) {
         RadioButton radChecked = (RadioButton) findViewById(checkedId);
         selectedCycle = (VslaCycle)radChecked.getTag();
         //Toast.makeText(getApplicationContext(), "Selected VSLA Cycle is: " + Utils.formatDate(selectedCycle.getStartDate()),Toast.LENGTH_LONG).show();
         }
         }); */
    }

    /* inflates custom menu bar for review members */
    void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_back_next_done, null);
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i;
                        Log.d("EndCycle", String.valueOf(multipleCyclesIndicator));
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


        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCycleData();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_next).setVisibility(View.GONE);
        customActionBarView.findViewById(R.id.actionbar_back).setVisibility(View.GONE);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.end_cycle_allcaps);

        // Set to true to show caret and enable its function; if designer decides otherwise set both to false
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }


    //Event that is raised when the date has been set
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay(txtShareOutDate, mYear, mMonth + 1, mDay);
        }
    };

    @Override
    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog) {
        // TODO Auto-generated method stub
        super.onPrepareDialog(id, dialog);
        ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);

    }

    private void updateDisplay(TextView theField, int theYear, int theMonth, int theDay) {
        if (theField != null) {
            theField.setText(String.valueOf(theDay) + "-" + Utils.getMonthNameAbbrev(theMonth) + "-" + theYear);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.mnuEndCycleSave:
                return updateCycleData();
            case R.id.mnuEndCycleCancel:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
        }
        return true;
    }

    private boolean updateCycleData() {
        boolean successFlg = false;
        VslaCycle cycle = null;
        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());
        if (selectedCycle != null) {
            cycle = selectedCycle;
        }

        if (validateData(cycle)) {
            boolean retVal = false;
            if (cycle.getCycleId() != 0) {
                retVal = repo.updateCycle(cycle);
            }

            if (retVal) {
                AlertDialog dlg = Utils.createAlertDialog(EndCycleActivity.this, getString(R.string.cycle_ended_successfully), Utils.MSGBOX_ICON_TICK);

                // Setting OK Button for the Dialog Box
                dlg.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (successAlertDialogShown) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            successAlertDialogShown = false;
                        }
                    }
                });
                dlg.show();

                if (dlg.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }

                successFlg = true;
            } else {
                Utils.createAlertDialog(EndCycleActivity.this, getString(R.string.problem_occurred_while_attempting_to_close_cycle), Utils.MSGBOX_ICON_TICK).show();
            }
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    private boolean validateData(VslaCycle cycle) {
        try {
            if (null == cycle) {
                return false;
            }
            // Validate: EndDate
            TextView txtShareOutDate = (TextView) findViewById(R.id.txtECShareOutDate);
            String endDate = txtShareOutDate.getText().toString().trim();
            if (endDate == null || endDate.length() < 1) {
                Utils.createAlertDialog(EndCycleActivity.this, getString(R.string.share_out_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtShareOutDate.requestFocus();
                return false;
            }
            Date dt = Utils.getDateFromString(endDate, Utils.DATE_FIELD_FORMAT);

            //Share Amount
            double theShareOutAmount = 0.0;
            TextView txtShareOutAmount = (TextView) findViewById(R.id.txtECShareOutAmount);
            String shareOutAmount = txtShareOutAmount.getText().toString().trim();
            if (shareOutAmount.length() < 1) {
                Utils.createAlertDialog(EndCycleActivity.this, getString(R.string.share_out_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtShareOutAmount.requestFocus();
                return false;
            } else {
                theShareOutAmount = Double.parseDouble(shareOutAmount);
                if (theShareOutAmount < 0.00) {
                    Utils.createAlertDialog(EndCycleActivity.this, getString(R.string.share_out_amount_be_positive), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtShareOutAmount.requestFocus();
                    return false;
                }
            }
            cycle.end(dt, theShareOutAmount);

            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}