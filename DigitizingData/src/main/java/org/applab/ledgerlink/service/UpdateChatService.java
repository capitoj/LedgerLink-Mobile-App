package org.applab.ledgerlink.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.applab.ledgerlink.ChatActivity;
import org.applab.ledgerlink.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by JCapito on 8/24/2016.
 */

public class UpdateChatService extends Service {

    protected Intent intent;
    protected boolean isRunning;
    protected  Thread backgroundThread;
    protected Context context;
    protected ListView listChatHistory;
    protected Context chatContext;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    protected Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateChatHistory();
            stopSelf();
        }
    };

    protected void updateChatHistory(){
        if(isActive(context)){
            try {
                getActivity();
            }catch(Exception e){
                Log.e("updateChatHistory", e.getMessage());
            }
        }
    }

    protected boolean isActive(Context context){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for(ActivityManager.RunningTaskInfo task: tasks){
            if(task.topActivity.getClassName().equalsIgnoreCase("org.applab.ledgerlink.ChatActivity")){
                return true;
            }
        }
        return false;
    }

    public static Activity getActivity() {
        try {
            Class updateChatServiceClass = Class.forName("org.applab.ledgerlink.ChatActivity");
            Object activityThread = updateChatServiceClass.getMethod("onCreate").invoke(null);
            Field listChatHistoryField = updateChatServiceClass.getField("listChatHistory");
            listChatHistoryField.setAccessible(true);
            Map<Object, Object> activities = (Map<Object, Object>)listChatHistoryField.get(activityThread);
            if(activities != null){
                Log.d("UpdateChatHistory", String.valueOf(activities.size()));
            }

        }catch (Exception e){
            Log.e("getActivity", e.getMessage());
        }
        return null;
    }

    @Override
    public void onCreate(){
        this.isRunning = false;
        this.context = this;
        this.backgroundThread = new Thread(runnable);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(!this.isRunning){
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }
}
