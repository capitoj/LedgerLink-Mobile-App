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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;




/**
 * Created by Moses on 7/4/13.
 */
public class EditCycleSelectCycle extends SherlockActivity {

    ActionBar actionBar;

    private VslaCycle selectedCycle = null;
    private RadioGroup grpCycleDates;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        inflateCustomBar();



        setContentView(R.layout.activity_meeting_definition);

        //Setup the Fields by getting the current Cycle
        VslaCycleRepo cycleRepo = new VslaCycleRepo(getApplicationContext());

        //Deal with the radio buttons
        grpCycleDates = (RadioGroup)findViewById(R.id.grpMDExistingCycles);

        TextView lblMDHeading = (TextView)findViewById(R.id.lblMDHeading);
        lblMDHeading.setVisibility(View.GONE);

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


            }
            else {

            }
        }
        else {

        }

        //Setup the Checked Listener
        grpCycleDates.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radChecked = (RadioButton) findViewById(checkedId);
                selectedCycle = (VslaCycle)radChecked.getTag();

                }
        });
        TextView txtInstructions = (TextView)findViewById(R.id.lblMDMultipleCycles);
        String instructions = "Select next to modify the cycle";
        if(activeCycles.size()>1) {
            instructions = "You have more than one active cycle. Please choose the cycle you want to modify";
        }
        txtInstructions.setText(instructions);


        TextView txtDateInstructions = (TextView)findViewById(R.id.lblMDHeader);
        txtDateInstructions.setVisibility(View.GONE);
        TextView txtMeetingDate = (TextView) findViewById(R.id.txtMDMeetingDate);
        txtMeetingDate.setVisibility(View.GONE);


    }

    private void inflateCustomBar()
    {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_next, null);
        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(selectedCycle == null) {
                            Toast.makeText(getApplicationContext(), "Please select the cycle you wish to modify", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                        i.putExtra("_isUpdateCycleAction", true);
                        i.putExtra("_cycleId", selectedCycle.getCycleId());
                        startActivity(i);
                        finish();
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
        actionBar.setTitle("EDIT CYCLE");

        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

      /**  actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)); */
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //final MenuInflater inflater = getSupportMenuInflater();
        //inflater.inflate(R.menu.meeting_definition, menu);
        //To use custom menu view
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


        }
        return true;
    }

}