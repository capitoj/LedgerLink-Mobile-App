package org.applab.ledgerlink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;

/**
 * Created by Home on 13/01/2020.
 */

public class TestingActivity extends AppCompatActivity {

    private Context context;
    LedgerLinkApplication ledgerLinkApplication;
    private VslaInfo vslaInfo = null;
    private String targetVslaCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_testing);

        this.context = this;
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();


        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        VslaInfo vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();
        String vslacode = vslaInfo.getVslaCode(); // Get VslaCode
        final TextView txtPassCode = (TextView) findViewById(R.id.txtDREditVslaCode);
        txtPassCode.setText(String.valueOf(vslacode));

        // ---Button view---
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String editVslaCode = txtPassCode.getText().toString().trim();
                if (editVslaCode.length() <= 0) {
                    Utils.createAlertDialogOk(TestingActivity.this, getString(R.string.action_vsla_code), getString(R.string.vsla_code_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassCode.requestFocus();
                }else {
                    try{
                        // Update Vsla Code
                        ledgerLinkApplication.getVslaInfoRepo().editVslaCode(editVslaCode);
                    }catch (Exception e){
                        // Update failed
                        Toast.makeText(getApplicationContext(), getString(R.string.edit_failed_try_again_later), Toast.LENGTH_LONG).show();
                    }
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

    }


}
