package org.applab.ledgerlink.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by JCapito on 8/2/2016.
 */
public class OutboundChatReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent){
        Intent background = new Intent(context, OutboundChatService.class);
//        context.startService(background);
    }
}
