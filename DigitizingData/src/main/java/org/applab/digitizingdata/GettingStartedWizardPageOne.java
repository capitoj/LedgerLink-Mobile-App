package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class GettingStartedWizardPageOne  extends SherlockActivity {
    VslaInfoRepo vslaInfoRepo = null;
    VslaInfo vslaInfo = null;
    ActionBar actionBar;
    TextView savingsGroupName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_getting_started_wizard_page_1);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("GETTING STARTED");

        //For test purposes, create vsla info if not exists
        /* VslaInfoRepo repo = new VslaInfoRepo(getBaseContext());
        if(!repo.vslaInfoExists()) {
           repo.saveVslaInfo("TESTVSLA", "TESTVSLA", "1234");
        }
        */


        vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfo = vslaInfoRepo.getVslaInfo();

        savingsGroupName = (TextView)findViewById(R.id.txtNCP_header);
        savingsGroupName.setText(vslaInfo.getVslaName());

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {


            case R.id.mnuNCCancel:
                //exit app
                System.exit(0);
                return true;
            case R.id.mnuNCNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                //Next page
                Intent mainMenu = new Intent(getBaseContext(), GettingStartedWizardPageTwo.class);

                startActivity(mainMenu);

        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.new_cycle, menu);
        return true;

    }
    
}
