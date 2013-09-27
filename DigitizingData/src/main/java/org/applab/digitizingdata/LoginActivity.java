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
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class LoginActivity extends Activity {
    VslaInfoRepo vslaInfoRepo = null;
    VslaInfo vslaInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            Intent i = new Intent(getApplicationContext(), ActivationActivity.class);
                            startActivity(i);
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
                    startActivity(i);
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
            startActivity(i);
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
                    Intent mainMenu = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(mainMenu);
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
    
}
