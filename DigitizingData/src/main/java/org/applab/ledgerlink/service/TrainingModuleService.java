package org.applab.ledgerlink.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.applab.ledgerlink.domain.model.TrainingModuleResponse;
import org.applab.ledgerlink.helpers.Network;
import org.applab.ledgerlink.helpers.TrainingModuleFactory;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.tasks.TrainingModuleSubmission;
import org.applab.ledgerlink.repo.TrainingModuleRepo;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;
import org.applab.ledgerlink.utils.Connection;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.List;

/**
 * Created by Joseph Capito on 1/14/2016.
 */
public class TrainingModuleService extends Service {

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
            submitData();
            stopSelf();
        }
    };



    protected void submitData(){
        JSONStringer js = getJSONOutput();
        try {
            if(Connection.isNetworkConnected(context)) {
                String serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "submittrainingmodule");
                String jsonString = Network.submitData(context, serverUri, String.valueOf(js));
                JSONObject jObject = new JSONObject(jsonString).getJSONObject("SubmitTrainingModuleResponse");
                int statusCode = jObject.getInt("StatusCode");
                if (statusCode == 1) {
                    TrainingModuleResponseRepo trainingModuleResponseRepo = new TrainingModuleResponseRepo(context);
                    trainingModuleResponseRepo.delete();
                    TrainingModuleRepo trainingModuleRepo = new TrainingModuleRepo(context);
                    trainingModuleRepo.delete();
                } else {
                    Log.e("TrainingModuleService", "SubmitData - " + String.valueOf(statusCode));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected JSONStringer getJSONOutput(){
        JSONStringer js = new JSONStringer();
        try{
            js.object();
            js = Network.getNetworkHeader(context, js);
            js = TrainingModuleFactory.getJSONOutput(context, js);
            js.endObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

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
}
