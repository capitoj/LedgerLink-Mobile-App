package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaCycleRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class LoginActivity extends Activity {
    VslaInfoRepo vslaInfoRepo = null;
    VslaInfo vslaInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Test purposes

//        if(true)
//        {
//            Intent i = new Intent(getBaseContext(), GettingStartedWizardPageOne.class);
//
//            startActivity(i);
//            return;
//        }

        setContentView(R.layout.activity_login);

        //Check whether the VSLA has been Activated
        vslaInfoRepo = new VslaInfoRepo(LoginActivity.this);
        vslaInfo = vslaInfoRepo.getVslaInfo();

        //Determine whether to show the activation screen

        if(vslaInfo != null) {
            if(!vslaInfo.isActivated()) {
                if(vslaInfo.isOffline()) {
                    AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
                    ad.setTitle("Activation");
                    ad.setMessage("You are currently working Offline. Activate your VSLA in order to send meeting data to the bank or check the VSLA's bank account balance. Do you want to Activate now?");
                    ad.setPositiveButton(
                            "Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {

                            startMainMenuActivity();
                        }
                    }
                    );
                    ad.setNegativeButton(
                            "No", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int arg1) {
                        }
                    }
                    );
                    ad.show();
                }
                else {
                    //If it is not Activated and is not in Offline Mode, force activation
                    Intent i = new Intent(LoginActivity.this, ActivationActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }
            else {
                //If VSLA is Activated proceed with normal process
            }
        }
        else {
            //if VSLAInfo is NULL, force Activation
            //If it is not Activated and is not in Offline Mode, force activation
            Intent i = new Intent(LoginActivity.this, ActivationActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        TextView txtVslaName = (TextView) findViewById(R.id.lbl_vsla_name);
        String vslaName = "Not-Activated";
        if(vslaInfo != null) {
            vslaName = vslaInfo.getVslaName();
        }

        //TODO: Do not display the name for now: Will handle this after Activation stuff is done
        txtVslaName.setText(vslaName);


        // ---Button view---
        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView txtPassKey = (TextView)findViewById(R.id.edt_passkey);
                String passKey = txtPassKey.getText().toString().trim();

                if(passKey.equalsIgnoreCase(vslaInfo.getPassKey())) {
                    startMainMenuActivity();
                }
                else {
                    Utils.createAlertDialogOk(LoginActivity.this,"Security","The Pass Key is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);

        return true;
    }

    private void startMainMenuActivity() {
        boolean showGettingStartedWizard = false;
        //Determine whether to start the Getting Started Wizard or the Main Menu
        //For now just consider the Vsla Cycle. May be later we shall include a few members
        VslaCycleRepo cycleRepo = new VslaCycleRepo(getApplicationContext());
        if(null != cycleRepo.getMostRecentCycle()) {
            showGettingStartedWizard = true;
        }

        Intent mainMenu = null;
        if(showGettingStartedWizard) {
            mainMenu = new Intent(getBaseContext(), MainActivity.class);
        }
        else {
            mainMenu = new Intent(getBaseContext(), GettingStartedWizardPageOne.class);
        }
        
        mainMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainMenu);
        finish();
    }

    @Override
    public void onBackPressed() {
        //End the application
        finish();
    }
    
}
