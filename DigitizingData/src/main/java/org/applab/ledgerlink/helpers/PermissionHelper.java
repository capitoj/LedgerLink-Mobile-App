package org.applab.ledgerlink.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by JCapito on 10/19/2020.
 */

public class PermissionHelper{

    protected int REQUEST_ID_MULTIPLE_PERMISSIONS;
    protected String readExternalStorage;
    protected String writeExternalStorage;
    protected String readPhoneStage;
    protected String accessNetworkState;
    protected Activity activity;

    private boolean isReadExternalStorageGranted;
    private boolean isWriteExternalStorageGranted;
    private boolean isReadPhoneStateGranted;
    private boolean isReadNetworkStateGranted;

    public PermissionHelper(Activity activity){
        this.activity = activity;
        this.REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

        this.readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        this.writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        this.readPhoneStage = Manifest.permission.READ_PHONE_STATE;
        this.accessNetworkState = Manifest.permission.ACCESS_NETWORK_STATE;

        this.isReadExternalStorageGranted = false;
        this.isWriteExternalStorageGranted = false;
        this.isReadPhoneStateGranted = false;
        this.isReadNetworkStateGranted = false;
    }

    protected boolean __hasPermission(String permission){
        return  ActivityCompat.checkSelfPermission(this.activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean __requestForMultiplePermissions(){

        boolean hasAllPermissions = true;

        this.isReadExternalStorageGranted = ContextCompat.checkSelfPermission(this.activity, this.readExternalStorage) == PackageManager.PERMISSION_GRANTED;
        this.isWriteExternalStorageGranted = ContextCompat.checkSelfPermission(this.activity, this.writeExternalStorage) == PackageManager.PERMISSION_GRANTED;
        this.isReadPhoneStateGranted = ContextCompat.checkSelfPermission(this.activity, this.readPhoneStage) == PackageManager.PERMISSION_GRANTED;
        this.isReadNetworkStateGranted = ContextCompat.checkSelfPermission(this.activity, this.accessNetworkState) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<>();
        if(!__hasPermission(this.readExternalStorage)){
            permissionRequest.add(this.readExternalStorage);
        }
        if(!__hasPermission(this.writeExternalStorage)){
            permissionRequest.add(this.writeExternalStorage);
        }
        if(!__hasPermission(this.readPhoneStage)){
            permissionRequest.add(this.readPhoneStage);
        }

        if(!__hasPermission(this.accessNetworkState)){
            permissionRequest.add(this.accessNetworkState);
        }

        if(!permissionRequest.isEmpty()){
            String[] permissions = permissionRequest.toArray(new String[permissionRequest.size()]);
            ActivityCompat.requestPermissions(this.activity, permissions, this.REQUEST_ID_MULTIPLE_PERMISSIONS);
            hasAllPermissions = false;
        }
        return hasAllPermissions;
    }

    public static boolean hasPermission(Activity activity, String permission){
        return new PermissionHelper(activity).__hasPermission(permission);
    }

    public static boolean requestForMultiplePermissions(Activity activity){
        return new PermissionHelper(activity).__requestForMultiplePermissions();
    }

}
