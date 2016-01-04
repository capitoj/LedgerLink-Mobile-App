package org.applab.ledgerlink.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Joseph Capito on 12/10/2015.
 */
public class AlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent){
        Intent background = new Intent(context, BackgroundService.class);
        context.startService(background);
    }
}
