package org.applab.ledgerlink.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.applab.ledgerlink.domain.model.MessageChannel;
import org.applab.ledgerlink.helpers.ChatFactory;
import org.applab.ledgerlink.helpers.Network;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MessageChannelRepo;
import org.applab.ledgerlink.utils.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Created by JCapito on 8/6/2016.
 */
public class InboundChatService extends Service {

    protected boolean isRunning;
    protected  Thread backgroundThread;
    protected Context context;

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    protected Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getInboundChats();
            stopSelf();
        }
    };

    protected  void getInboundChats(){
        JSONStringer js = getJSONOutput();
        try {
            if(Connection.isNetworkConnected(context)) {
                String serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "outboundchats");
                String jsonString = Network.submitData(context, serverUri, String.valueOf(js));
                if(jsonString != null) {
                    JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("OutboundChatsResult");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getString("Message").length() > 0 || !jsonObject.getString("Message").equals("null")) {
                            MessageChannel messageChannel = new MessageChannel();
                            messageChannel.setMessage(jsonObject.getString("Message"));
                            messageChannel.setStatus(1);
                            messageChannel.setFrom(jsonObject.getString("Source"));
                            messageChannel.setTo(jsonObject.getString("Destination"));
                            MessageChannelRepo.addMessage(context, messageChannel);
                        }
                    }
                }
            }
        }catch (Exception e){
            Log.e("getInboundChats", e.getMessage());
        }

    }

    protected JSONStringer getJSONOutput(){
        JSONStringer js = new JSONStringer();
        try{
            js.object();
            js = Network.getNetworkHeader(context, js);
            js.endObject();
        }catch (Exception e){
            Log.e("getChat", e.getMessage());
        }
        return js;
    }

    @Override
    public void onCreate(){
        this.isRunning = false;
        this.context = this;
        this.backgroundThread = new Thread(runnable);
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
