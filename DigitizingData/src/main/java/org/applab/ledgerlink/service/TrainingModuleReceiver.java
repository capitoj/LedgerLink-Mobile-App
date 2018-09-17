package org.applab.ledgerlink.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Joseph Capito on 1/14/2016.
 */
public class TrainingModuleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        Intent background =new Intent(context, TrainingModuleService.class);
        context.startService(background);
    }
}
