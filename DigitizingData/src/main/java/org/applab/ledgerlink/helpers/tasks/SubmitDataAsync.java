package org.applab.ledgerlink.helpers.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.SendMeetingDataActivity;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

/**
 * Created by Joseph Capito on 10/27/2015.
 */
public class SubmitDataAsync extends AsyncTask<String, String, JSONArray> {
    protected Context context;
    protected boolean isConnected;
    protected ProgressDialog progressDialog;
    protected int httpStatusCode;
    protected int meetingId;

    public SubmitDataAsync(Context context){
        this.context = context;
        this.isConnected = false;
        this.progressDialog = null;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setTitle("Performing Data Submission");
        progressDialog.setMessage("Please wait...");
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
                final DefaultHttpClient httpClient = new DefaultHttpClient();
                final HttpPost httpPost = new HttpPost(uri);
                StringEntity se = new StringEntity(params[1]);
                httpPost.setEntity(se);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                ResponseHandler<String> rh = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        httpStatusCode = httpResponse.getStatusLine().getStatusCode();
                        StringBuffer out = new StringBuffer();
                        byte[] b = EntityUtils.toByteArray(httpEntity);
                        out.append(new String(b, 0, b.length));
                        return out.toString();
                    }
                };
                String response = httpClient.execute(httpPost, rh);
                httpClient.getConnectionManager().shutdown();
                if (httpStatusCode == 200) {
                    result = new JSONArray(response);
                    //result = new JSONObject(response);
                }
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
        String dialogTitle = "Error Message";
        if(this.isConnected){
            try {
                if(jsonArray != null) {
                    Toast.makeText(this.context, "The meeting data was sent successfully", Toast.LENGTH_LONG).show();
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
                    DialogMessageBox.show(this.context, dialogTitle, "The remote server encountered an internal error. There was not response from the server");
                }
            }catch(Exception e){
                DialogMessageBox.show(this.context, dialogTitle, "Ledger Link has encountered an error. Kindly get in touch with your support agent " + e.getMessage());
            }
        } else {
            DialogMessageBox.show(this.context, dialogTitle, "The meeting information was not successfully submitted because the remote server could not be reached. Kindly check to ensure that you have an internet connection");
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
