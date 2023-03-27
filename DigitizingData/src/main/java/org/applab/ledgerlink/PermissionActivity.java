package org.applab.ledgerlink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.PermissionHelper;

public class PermissionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_permission);

        if(PermissionHelper.requestForMultiplePermissions(this)){
            loadActivationScreen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(PermissionHelper.requestForMultiplePermissions(this)) {
            loadActivationScreen();
        }
    }

    protected void loadActivationScreen(){
        TextView lblVAHeader = (TextView)findViewById(R.id.lblVAHeader);
        lblVAHeader.setText("Please wait. Loading......");
        Intent i = new Intent(getApplicationContext(), ActivationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}
