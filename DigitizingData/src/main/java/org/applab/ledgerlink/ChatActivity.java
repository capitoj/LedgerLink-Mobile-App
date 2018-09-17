package org.applab.ledgerlink;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.ledgerlink.domain.model.MessageChannel;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.adapters.MessageChannelAdapter;
import org.applab.ledgerlink.repo.MessageChannelRepo;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.service.UpdateChatService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ChatActivity extends SherlockActivity {

    protected Button btnSendChat;
    protected ListView listChatHistory;
    protected EditText txtChatBox;
    protected Context context;
    protected List<MessageChannel> messageChannelList;
    protected Intent alarm;
    protected AlarmManager alarmManager;
    protected PendingIntent pendingIntent;
    protected Timer timer;
    protected TimerTask timerTask;
    protected final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;
        messageChannelList = new ArrayList<MessageChannel>();

        btnSendChat = (Button)findViewById(R.id.btnSendChat);
        btnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChatMessage();
            }
        });


        listChatHistory = (ListView)findViewById(R.id.listChatHistory);
        ArrayAdapter<MessageChannel> adapter = new MessageChannelAdapter(context, messageChannelList);
        listChatHistory.setAdapter(adapter);

        txtChatBox = (EditText)findViewById(R.id.txtChatBox);

        this.populateChatHistory();

        //alarm = new Intent(context, UpdateChatReceiver.class);
        //this.startBackgroundService(context, alarm, 2000);
    }

    protected void startBackgroundService(Context context, Intent alarm, int interval){
        boolean alarmRunning = (PendingIntent.getBroadcast(context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmRunning){
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pendingIntent);
        }
    }

    protected boolean refreshChatHistory(){
        boolean isUpdated = false;
        try{
            if(MessageChannelRepo.getMessages(context).size() > listChatHistory.getCount()){
                int diff = MessageChannelRepo.getMessages(context).size() - listChatHistory.getCount();
                for(int i = listChatHistory.getCount(); i < (listChatHistory.getCount() + diff); i ++){
                    MessageChannel newMessageChannel = MessageChannelRepo.getMessageAtIndex(context, i);
                    if(newMessageChannel != null) {
                        messageChannelList.add(newMessageChannel);
                    }
                }
                ((ArrayAdapter<MessageChannel>) listChatHistory.getAdapter()).notifyDataSetChanged();
                isUpdated = true;
            }
        }catch (Exception e){
            Log.e("refreshChatHistory", e.getMessage());
        }
        return isUpdated;
    }


    protected void saveChatMessage(){
        String chatMessage = txtChatBox.getText().toString().trim();
        if(chatMessage.length() > 0){
            MessageChannel messageChannel = new MessageChannel();
            messageChannel.setMessage(chatMessage);
            VslaInfo vslaInfo = new VslaInfoRepo(context).getVslaInfo();
            messageChannel.setFrom(vslaInfo.getVslaCode());
            messageChannel.setTo("ug.barclays.com");
            int msgID = MessageChannelRepo.addMessage(context, messageChannel);
            if(msgID > 0) {
                refreshChatHistory();
                txtChatBox.setText("");
            }else{
                Toast.makeText(context, "Not Saved", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void populateChatHistory(){
        try {
            messageChannelList = MessageChannelRepo.getMessages(context);
        }catch(Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        ArrayAdapter<MessageChannel> adapter = new MessageChannelAdapter(context, messageChannelList);
        listChatHistory.setAdapter(adapter);
        listChatHistory.scrollTo(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            //case R.id.home: showPreviousWindow(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void showPreviousWindow(){
        Toast.makeText(this, "You have clicked back", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        startTimer();
    }

    protected void startTimer(){
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 5000, 10000);
    }

    protected void stopTimerTask(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    protected void initializeTimerTask(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new UpdateChatHistoryAsync().execute();
                    }
                });
            }
        };
    }

    @Override
    public void onPause(){
        super.onPause();
        stopTimerTask();
    }

    @Override
    public void onStop(){
        super.onStop();
        stopTimerTask();
    }

    protected class UpdateChatHistoryAsync extends AsyncTask<String, String, Boolean>{

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params){
            boolean isUpdated = false;
            try {
                isUpdated = refreshChatHistory();
            }catch (Exception e){
                Log.e("doInBackground", e.getMessage());
            }
            return isUpdated;
        }

        @Override
        protected void onProgressUpdate(String... values){

        }

        @Override
        protected void onPostExecute(Boolean isUpdated){

        }
    }

}
