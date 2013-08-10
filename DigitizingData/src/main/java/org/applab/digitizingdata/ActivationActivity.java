package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class ActivationActivity extends Activity {
    VslaInfoRepo vslaInfoRepo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        vslaInfoRepo = new VslaInfoRepo(ActivationActivity.this);
        // ---Button view---
        Button btnActivate = (Button)findViewById(R.id.btnVAActivate);
        btnActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Utils.createAlertDialogOk(ActivationActivity.this, "Activation", "There is no Secure Internet Connection to the Bank at this time. Please try again later.", Utils.MSGBOX_ICON_EXCLAMATION).show();

            }
        });

        // ---Button view---
        Button btnCancel = (Button)findViewById(R.id.btnVACancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(null == vslaInfoRepo) {
                    vslaInfoRepo = new VslaInfoRepo(ActivationActivity.this);
                }

                //Set Offline
                AlertDialog.Builder ad = new AlertDialog.Builder(ActivationActivity.this);
                ad.setTitle("Work Offline");
                ad.setMessage("When working Offline, you will not be able to send meeting data to the bank or check the VSLA's bank account balance. However, you can still Activate later. \nContinue?");
                ad.setPositiveButton(
                        "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                        if (saveOfflineVslaInfo()) {
                            Intent i = new Intent(getBaseContext(), LoginActivity.class);
                            startActivity(i);
                        }
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activation, menu);
        return true;
    }

    private boolean saveOfflineVslaInfo() {
        try{
            TextView txtVslaCode = (TextView) findViewById(R.id.txtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();

            if(vslaCode.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Activation","The VSLA Code is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return false;
            }

            if(passKey.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Activation","The Pass Key is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return false;
            }

            return vslaInfoRepo.saveOfflineVslaInfo(vslaCode,passKey);
        }
        catch(Exception ex) {
            return false;
        }
    }
    
}
