package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.R;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.helpers.VslaCyclesArrayAdapter;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/27/13.
 */
public class EndCycleActivity extends SherlockActivity {

    public static final int Date_dialog_id = 1;
    // date and time
    private int mYear;
    private int mMonth;
    private int mDay;
    private TextView txtShareOutDate;
    private TextView txtInstructions;
    private boolean successAlertDialogShown = false;

    private VslaCycle selectedCycle;
    private ActionBar actionBar;
    private ArrayList<VslaCycle> vslaCycles;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_end_cycle);

        inflateCustombar();

        // Solve the auto focus
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

       /** actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        txtShareOutDate = (TextView)findViewById(R.id.txtECShareOutDate);
        txtInstructions = (TextView)findViewById(R.id.lblECInstruction);

        //Set onClick Listeners to load the DateDialog for startDate
        txtShareOutDate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog( EndCycleActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        //Setup the Fields by getting the current Cycle
        VslaCycleRepo repo = new VslaCycleRepo(getApplicationContext());

        //Deal with the radio buttons
        RadioGroup grpCycleDates = (RadioGroup)findViewById(R.id.grpECExistingCycles);

        //Retrieve all the active cycles
        ArrayList<VslaCycle> activeCycles = repo.getActiveCycles();

        //Create radio buttons dynamically
        if(activeCycles != null) {
            for(VslaCycle cycle: activeCycles) {
                RadioButton radCycle = new RadioButton(this);
                String cycleDates = String.format("%s - %s", Utils.formatDate(cycle.getStartDate(), "dd MMM yyyy"),
                        Utils.formatDate(cycle.getEndDate(), "dd MMM yyyy"));
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
        }

        if(activeCycles != null && activeCycles.size()>0) {
            //Populate Fields
            if(activeCycles.size() == 1) {
                if(selectedCycle == null) {
                    selectedCycle = activeCycles.get(0);
                }
                txtShareOutDate.setText(Utils.formatDate(selectedCycle.getEndDate(),"dd-MMM-yyyy"));

                //Setup the Default Date: Not sure what this is for. LOL!
                final Calendar c = Calendar.getInstance();
                c.setTime(selectedCycle.getEndDate());
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                updateDisplay(txtShareOutDate, mYear, mMonth+1,mDay);

                txtInstructions.setText(new StringBuilder()
                        .append("The current cycle end date is " + Utils.formatDate(selectedCycle.getEndDate(), "dd-MMM-yyyy"))
                        .append(". If your cycle has ended, enter the share out date.")
                        .toString()
                );
            }
            else {
                txtInstructions.setText(new StringBuilder()
                        .append("There is more than one cycle currently running.\n")
                        .append("Select the cycle to end and enter the share out date.")
                        .toString()
                );
            }
        }
        else {
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
        }

        //Setup the Checked Listener
        grpCycleDates.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radChecked = (RadioButton) findViewById(checkedId);
                selectedCycle = (VslaCycle)radChecked.getTag();
                //Toast.makeText(getApplicationContext(), "Selected VSLA Cycle is: " + Utils.formatDate(selectedCycle.getStartDate()),Toast.LENGTH_LONG).show();
            }
        });
    }

    /* inflates custom menu bar for review members */
    public void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
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
                        updateCycleData();
                    }
                }
        );

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("END CYCLE");
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
            updateDisplay(txtShareOutDate,mYear,mMonth+1,mDay);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                AlertDialog dlg = Utils.createAlertDialog(EndCycleActivity.this, "End Cycle", "The cycle has been ended successfully.", Utils.MSGBOX_ICON_TICK);

                // Setting OK Button for the Dialog Box
                dlg.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(successAlertDialogShown) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            successAlertDialogShown = false;
                        }
                    }
                });
                dlg.show();

                if(dlg.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }

                successFlg = true;
            }
            else {
                Utils.createAlertDialog(EndCycleActivity.this, "End Cycle", "A problem occurred while attempting to close the cycle.", Utils.MSGBOX_ICON_TICK).show();
            }
        }
        else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    private boolean validateData(VslaCycle cycle) {
        try {
            if(null == cycle) {
                return false;
            }
            // Validate: EndDate
            TextView txtShareOutDate = (TextView)findViewById(R.id.txtECShareOutDate);
            String endDate = txtShareOutDate.getText().toString().trim();
            if(endDate == null || endDate.length()<1) {
                Utils.createAlertDialog(EndCycleActivity.this,"End Cycle","The Share out Date is required.",Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtShareOutDate.requestFocus();
                return false;
            }
            Date dt = Utils.getDateFromString(endDate,Utils.DATE_FIELD_FORMAT);

            //Share Amount
            double theShareOutAmount = 0.0;
            TextView txtShareOutAmount = (TextView)findViewById(R.id.txtECShareOutAmount);
            String shareOutAmount = txtShareOutAmount.getText().toString().trim();
            if (shareOutAmount.length() < 1) {
                Utils.createAlertDialog(EndCycleActivity.this,"End Cycle","The Share out Amount is required.",Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtShareOutAmount.requestFocus();
                return false;
            }
            else {
                theShareOutAmount = Double.parseDouble(shareOutAmount);
                if (theShareOutAmount < 0.00) {
                    Utils.createAlertDialog(EndCycleActivity.this,"End Cycle","The Share out Amount should be positive.",Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtShareOutAmount.requestFocus();
                    return false;
                }
            }
            cycle.end(dt,theShareOutAmount);

            return true;
        }
        catch (Exception ex){
            return false;
        }
    }
}