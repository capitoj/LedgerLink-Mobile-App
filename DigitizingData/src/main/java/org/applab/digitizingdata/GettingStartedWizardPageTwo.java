package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;

public class GettingStartedWizardPageTwo extends SherlockActivity {
    private VslaInfo vslaInfo = null;
    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        setContentView(R.layout.activity_getting_started_wizard_passcode_validation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);


        actionBar.setTitle("Get Started");

        vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();

        TextView savingsGroupName = (TextView) findViewById(R.id.txtNCP_header);
        if(!vslaInfo.isActivated()) {
            //If not activated, show message to avoid displaying "Offline Mode" as vsla name
            savingsGroupName.setText("(not yet activated)");
        }
        else {

            savingsGroupName.setText(vslaInfo.getVslaName());
        }


        ledgerLinkApplication.getVslaInfoRepo().updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_PIN);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case R.id.mnuAMCancel:
                //Go back to GSW start page
                i =  new Intent(getApplicationContext(), GettingStartedWizardPageOne.class);
                startActivity(i);
                finish();
                break;
            case R.id.mnuAMNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                //validate passkey
                validatePassKey();

        }
        return true;

    }



    void validatePassKey()
    {
        TextView txtPassKey = null;
        try {
            txtPassKey = (TextView)findViewById(R.id.txtGSW_passkey);
            String passKey = txtPassKey.getText().toString().trim();


            if(passKey.equalsIgnoreCase(vslaInfo.getPassKey())) {
                //Decide which activity to launch, from the current Getting started wizard stage
                //Intent stage = new Intent(getBaseContext(), Utils.resolveGettingStartedWizardStage(vslaInfo.getGettingStartedWizardStage()));
                Intent stage = new Intent(getBaseContext(), GettingStartedWizardNewCycleActivity.class);
                startActivity(stage);
                finish();
            }
            else {
                Utils.createAlertDialogOk(this, "Security", "The Pass Key is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
            }
        }
        catch(Exception ex) {
            Utils.createAlertDialogOk(this, "Security", "The Pass Key could not be validated.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            if (txtPassKey != null) {
                txtPassKey.requestFocus();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.cancel_next, menu);

        //menu.findItem(R.id.mnuNCCancel).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS, MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;

    }
    
}
