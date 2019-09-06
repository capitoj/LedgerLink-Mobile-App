package org.applab.ledgerlink.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.applab.ledgerlink.LoginActivity;
import org.applab.ledgerlink.MainActivity;
import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.TrainingModuleRepo;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

/**
 * Created by JCapito on 12/10/2015.
 */
public class BackgroundService extends Service {

    private boolean isRunning;
    private Thread backgroundThread;
    private Context context;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        this.isRunning = false;
        this.context = this;
        this.backgroundThread = new Thread(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            launchNotification();
            stopSelf();
        }
    };

    @Override
    public void onDestroy(){
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

    protected void launchNotification(){
        MeetingRepo meetingRepo = new MeetingRepo(this.context);
        VslaCycle recentCycle = new VslaCycleRepo(this.context).getMostRecentCycle();
        int noOfMeetings = meetingRepo.getPastMeetings(recentCycle.getCycleId()).size();
        if(noOfMeetings > 0) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, new Intent(this.context, LoginActivity.class), 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
            builder.setContentTitle(getString(R.string.ledger_link));
            builder.setContentText(String.format(getString(R.string.you_have)+" %s"+ getString(R.string.unsent) +"%s", noOfMeetings, noOfMeetings > 1 ? getString(R.string.meetings_unsent) : getString(R.string.meeting_unsent)));
            builder.setSmallIcon(R.drawable.ic_ledgerlink_icona);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, builder.build());
        }
    }
}
