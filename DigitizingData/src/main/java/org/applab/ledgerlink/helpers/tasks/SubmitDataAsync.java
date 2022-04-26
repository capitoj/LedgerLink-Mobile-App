package org.applab.ledgerlink.helpers.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.R;
import org.applab.ledgerlink.SendMeetingDataActivity;
import org.applab.ledgerlink.helpers.Network;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;

/**
 * Created by Joseph Capito on 10/27/2015.
 */
public class SubmitDataAsync extends AsyncTask<String, String, JSONArray> {
    protected Context context;
    protected boolean isConnected;
    protected ProgressDialog progressDialog;
    protected int httpStatusCode;

    public SubmitDataAsync(Context context){
        this.context = context;
        this.isConnected = false;
        this.progressDialog = null;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setTitle(context.getResources().getString(R.string.performing_data_submission));
        progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
        progressDialog.setProgress(1);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    @Override
    protected JSONArray doInBackground(String... params){
        JSONArray result = null;
        String uri = params[0];
        try{
            if(Connection.isNetworkConnected(this.context)){
                this.isConnected = true;

                URL url = new URL(uri);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(params[1].getBytes("UTF-8"));
                outputStream.close();

                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("utf-8")), 8);

                String line;
                String jsonString = "";

                while((line = bufferedReader.readLine()) != null){
                    jsonString += line;
                }
                inputStream.close();
                result = new JSONArray(jsonString);
                httpURLConnection.disconnect();
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(String... values){

    }

    @Override
    protected void onPostExecute(JSONArray jsonArray){
        String dialogTitle = context.getResources().getString(R.string.warning);
        if(this.isConnected){
            try {
                if(jsonArray != null) {

                    Toast.makeText(this.context, context.getResources().getString(R.string.meeting_data_sent_successfully), Toast.LENGTH_LONG).show();
                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if(jsonObject.getInt("StatusCode") == 0){
                            int meetingId = jsonObject.getInt("MeetingId");
                            Calendar cal = Calendar.getInstance();
                            MeetingRepo meetingRepo = new MeetingRepo(this.context);
                            meetingRepo.updateDataSentFlag(meetingId, cal.getTime());
                        }
                    }
                    Intent intent = new Intent(this.context, SendMeetingDataActivity.class);
                    this.context.startActivity(intent);
                }else{
                    DialogMessageBox.show(this.context, dialogTitle, context.getResources().getString(R.string.no_response_from_server));
                }
            }catch(Exception e){
                DialogMessageBox.show(this.context, dialogTitle, context.getResources().getString(R.string.get_in_touch_with_support_agent)+ e.getMessage());
            }
        } else {
            DialogMessageBox.show(this.context, dialogTitle, context.getResources().getString(R.string.meeting_info_not_successfully_submitted));
        }
        this.dismissProgressDialog();
    }

    protected void dismissProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
