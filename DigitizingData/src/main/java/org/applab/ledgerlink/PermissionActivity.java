package org.applab.ledgerlink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_permission);
        requestAppPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1: loadActivationScreen(grantResults); break;
        }
    }

    protected void loadActivationScreen(int[] grantResults){
        TextView lblVAHeader = (TextView)findViewById(R.id.lblVAHeader);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            lblVAHeader.setText("Please wait. Loading......");
            Intent i = new Intent(getApplicationContext(), ActivationActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }else{
            lblVAHeader.setText("Permission denied to read your External storage. Allow LedgerLink access to internal and external storage by turning on permissions within settings");
        }
    }

    protected void requestAppPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                1
        );
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_PHONE_STATE
                },
                2
        );
    }
}
