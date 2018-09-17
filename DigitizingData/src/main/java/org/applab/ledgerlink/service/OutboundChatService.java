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
 * Created by JCapito on 8/1/2016.
 */
public class OutboundChatService extends Service {

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
            sendChat();
            stopSelf();
        }
    };

    protected void sendChat(){
        JSONStringer js = getJSONOutput();
        try{
            if(Connection.isNetworkConnected(context)) {
                String serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "inboundchats");
                String jsonString = Network.submitData(context, serverUri, String.valueOf(js));
                if(jsonString != null) {
                    JSONObject jObject = new JSONObject(jsonString).getJSONObject("InboundChatsResult");
                    int statusCode = jObject.getInt("StatusCode");
                    if (statusCode == 1) {
                        JSONObject jsonObject = new JSONObject(js.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("UnDeliveredMessages");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonData = jsonArray.getJSONObject(i);
                            MessageChannel messageChannel = new MessageChannelRepo(context, jsonData.getInt("MsgId")).getMessageChannel();
                            messageChannel.setStatus(1);
                            MessageChannelRepo.updateMessage(context, messageChannel);
                        }
                    }
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
            js = ChatFactory.getJSONOutput(context, js);
            js.endObject();
        }catch (Exception e){
            Log.e("sendChat", e.getMessage());
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
