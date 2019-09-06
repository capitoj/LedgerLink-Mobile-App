package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.VslaCyclesArrayAdapter;

import java.util.ArrayList;


/**
 * Created by Moses on 7/4/13.
 */
public class SelectCycle extends ActionBarActivity{

    private VslaCycle selectedCycle = null;
    private RadioGroup grpCycleDates;
    private boolean isEndCycleAction;
    private boolean multipleCycles = false;
    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_select_cycle);

        if (getIntent().hasExtra("_isEndCycleAction")) {
            isEndCycleAction = getIntent().getBooleanExtra("_isEndCycleAction", false);
        }

        inflateCustomBar();
        //Setup the Fields by getting the current Cycle
        //Deal with the radio buttons
        //grpCycleDates = (RadioGroup)findViewById(R.id.grpMDExistingCycles);

        //Retrieve all the active cycles
        ListView cyclesListView = (ListView) findViewById(R.id.lstSelectCycleToEdit);
        TextView txtInstructions = (TextView) findViewById(R.id.lblMDMultipleCycles);

        ArrayList<VslaCycle> activeCycles = ledgerLinkApplication.getVslaCycleRepo().getActiveCycles();
        if (activeCycles != null && activeCycles.size() == 0) {
            // no cycles
            cyclesListView.setVisibility(View.GONE);
            txtInstructions.setText(R.string.no_active_cycles_to_modify);
            return;
        }


        final VslaCyclesArrayAdapter adapter = new VslaCyclesArrayAdapter(getBaseContext(), activeCycles);
        cyclesListView.setAdapter(adapter);
        cyclesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                selectedCycle = adapter.getItem(position);

                multipleCycles = true;
                if (isEndCycleAction) {

                    Intent i = new Intent(getApplicationContext(), EndCycleActivity.class);
                    i.putExtra("_multipleCycles", multipleCycles);
                    i.putExtra("_cycleId", selectedCycle.getCycleId());
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    i.putExtra("_isUpdateCycleAction", true);
                    i.putExtra("_multipleCycles", multipleCycles);
                    i.putExtra("_cycleId", selectedCycle.getCycleId());
                    startActivity(i);
                    finish();
                }
            }
        });
//        grpCycleDates.addView(cyclesListView);
        Utils.setListViewHeightBasedOnChildren(cyclesListView);


        //Create radio buttons dynamically
//        if(activeCycles != null) {
//            for(VslaCycle cycle: activeCycles) {
//                RadioButton radCycle = new RadioButton(this);
//                String cycleDates = String.format("%s - %s", Utils.formatDate(cycle.getStartDate(), getString(R.string.date_format)),
//                        Utils.formatDate(cycle.getEndDate(), getString(R.string.date_format)));
//                radCycle.setText(cycleDates);
//                radCycle.setId(cycle.getCycleId());
//                //radCycle.setTextColor();
//                radCycle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
//                radCycle.setTypeface(radCycle.getTypeface(), Typeface.BOLD);
//                //radCycle.setPadding(10,0,0,0);
//                radCycle.setTag(cycle); //Store the VslaCycle object in the Tag property of the radio button
//                //radCycle.setTextColor(txtMeetingDate.getTextColors());
//                grpCycleDates.addView(radCycle);
//
//                if(activeCycles.size() == 1) {
//                    radCycle.setChecked(true);
//                }
//            }
//        }


        if (activeCycles != null && activeCycles.size() > 0) {
            //Populate Fields
            if (activeCycles.size() == 1) {

                // Just launch the modifying or ending activity
                if (selectedCycle == null) {
                    selectedCycle = activeCycles.get(0);
                }

                if (isEndCycleAction) {
                    Intent i = new Intent(getApplicationContext(), EndCycleActivity.class);
                    i.putExtra("_cycleId", selectedCycle.getCycleId());
                    i.putExtra("_multipleCycles", multipleCycles);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    i.putExtra("_isUpdateCycleAction", true);
                    i.putExtra("_cycleId", selectedCycle.getCycleId());
                    i.putExtra("_multipleCycles", multipleCycles);
                    startActivity(i);
                    finish();
                }

            } else {
                multipleCycles = true;
            }
        } else {

        }

        //Setup the Checked Listener
//        grpCycleDates.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
//        {
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                RadioButton radChecked = (RadioButton) findViewById(checkedId);
//                selectedCycle = (VslaCycle)radChecked.getTag();
//
//                }
//        });

        String instructions = "";
        if (!isEndCycleAction) {
            instructions = getString(R.string.select_next_to_modify_cycle);
            if ((activeCycles != null ? activeCycles.size() : 0) > 1) {
                instructions = getString(R.string.more_than_one_cycle_currently_running);
            }
        } else {
            instructions = getString(R.string.select_next_to_end_the_cycle);
            if ((activeCycles != null ? activeCycles.size() : 0) > 1) {
                instructions = getString(R.string.more_than_one_unfinished_cycle);
            }
        }
        txtInstructions.setText(instructions);


    }

    private void inflateCustomBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_next, null);
        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedCycle == null) {
                            Toast.makeText(getApplicationContext(), R.string.select_cycle_to_modify, Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                        i.putExtra("_isUpdateCycleAction", true);
                        i.putExtra("_cycleId", selectedCycle.getCycleId());
                        startActivity(i);
                        finish();
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

        customActionBarView.findViewById(R.id.actionbar_next).setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        if (isEndCycleAction) {
            actionBar.setTitle(R.string.end_cycle_main);
        } else {
            actionBar.setTitle(R.string.end_cycle_main);
        }

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
        //final MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.meeting_definition, menu);
        //To use custom menu view
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