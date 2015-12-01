package org.applab.ledgerlink.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by JCapito on 9/21/2015.
 */
public class Connection {

    public static boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isAvailable());
    }
}
