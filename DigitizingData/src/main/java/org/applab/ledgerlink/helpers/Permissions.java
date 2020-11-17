package org.applab.ledgerlink.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v13.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JCapito on 10/19/2020.
 */

public class Permissions {

    protected Context context;
    protected int REQUEST_CODE = 13;
    protected String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
    protected String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    protected String readPhoneStage = Manifest.permission.READ_PHONE_STATE;

    protected Permissions(Context context){
        this.context = context;
    }

    protected boolean __hasPermission(String permission){
        return  ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    protected void __requestForMultiplePermissions(){

        List<String> permissionList = new ArrayList<>();
        if(!__hasPermission(readExternalStorage)){
            permissionList.add(readExternalStorage);
        }
        if(!__hasPermission(writeExternalStorage)){
            permissionList.add(writeExternalStorage);
        }
        if(!__hasPermission(readPhoneStage)){
            permissionList.add(readPhoneStage);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
//            ActivityCompat.requestPermissions(this.context,permissions,REQUEST_CODE);
        }
    }

    public boolean hasPermission(Context context, String permission){
        return new Permissions(context).__hasPermission(permission);
    }
}
