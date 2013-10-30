package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.SendDataRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;

public class ActivationActivity extends Activity {
    VslaInfoRepo vslaInfoRepo = null;
    HttpClient client;
    int httpStatusCode = 0; //To know whether the Request was successful
    boolean activationSuccessful = false;
    String targetVslaCode = null; //fake-fix

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        vslaInfoRepo = new VslaInfoRepo(ActivationActivity.this);
        // ---Button view---
        Button btnActivate = (Button)findViewById(R.id.btnVAActivate);
        btnActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveActivatedVslaInfo();
            }
        });

        // ---Button view---
        Button btnCancel = (Button)findViewById(R.id.btnVACancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(null == vslaInfoRepo) {
                    vslaInfoRepo = new VslaInfoRepo(ActivationActivity.this);
                }

                //Set Offline
                AlertDialog.Builder ad = new AlertDialog.Builder(ActivationActivity.this);
                ad.setTitle("Work Offline");
                ad.setMessage("When working Offline, you will not be able to send meeting data to the bank or check the VSLA's bank account balance. However, you can still Activate later. \nContinue?");
                ad.setPositiveButton(
                        "Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                        if (saveOfflineVslaInfo()) {
                            Intent i = new Intent(getBaseContext(), LoginActivity.class);
                            startActivity(i);
                        }
                    }
                }
                );
                ad.setNegativeButton(
                        "No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int arg1) {

                    }
                }
                );
                ad.show();
            }
        });
    }

    private String activateVlsa(String vslaCode, String sourceImei)
    {
        String vslaName = null;
        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,vslaCode,sourceImei);
        try {
            HttpGet get = new HttpGet(uri);
            HttpResponse response = client.execute(get);
            int status = response.getStatusLine().getStatusCode();

            if(status == 200) //sucess
            {
                HttpEntity e = response.getEntity();
                String data = EntityUtils.toString(e);
                JSONObject result = new JSONObject(data);
                vslaName = result.getString("ActivateVslaForDigitizingDataResult");
                return vslaName;
            }
            else
            {
                return vslaName;
            }
        }
        catch(ClientProtocolException exClient) {
            return null;
        }
        catch(IOException exIo) {
            return null;
        }
        catch(JSONException exJson) {
            return null;
        }
        catch(Exception ex) {
            return null;
        }
    }

    private void activateVlsaUsingPostAsync(String request)
    {
        String vslaName = null;
        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,"vslas","activate");
        new PostTask().execute(uri,request);

        //Do the other stuff in the Async Task
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activation, menu);
        return true;
    }

    private boolean saveOfflineVslaInfo() {
        try{
            TextView txtVslaCode = (TextView) findViewById(R.id.txtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();

            if(vslaCode.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Activation","The VSLA Code is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return false;
            }

            if(passKey.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Activation","The Pass Key is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return false;
            }

            return vslaInfoRepo.saveOfflineVslaInfo(vslaCode,passKey);
        }
        catch(Exception ex) {
            return false;
        }
    }

    private void saveActivatedVslaInfo() {
        try{
            TextView txtVslaCode = (TextView) findViewById(R.id.txtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();

            if(vslaCode.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Activation","The VSLA Code is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return;
            }

            if(passKey.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Activation","The Pass Key is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return;
            }

            //set target vslacode
            targetVslaCode = vslaCode;

            //Get IMEI
            String imei = null;
            String imsi = null;
            String simSerialNo = null;
            String networkOperatorName = null;
            String networkOperator = null;
            String networkType = null;
            String simOperatorName = null;
            String msisdn = null;

            TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            imei = tm.getDeviceId();

            //if there is a SIM Card then get details
            if(tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                imsi = tm.getSubscriberId();
                simSerialNo = tm.getSimSerialNumber();
                simOperatorName = tm.getSimOperatorName();
                networkOperatorName = tm.getNetworkOperatorName();
                networkOperator = tm.getNetworkOperator();
                msisdn = tm.getLine1Number();

                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE){
                    networkType = "EDGE";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS){
                    networkType = "GPRS";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA){
                    networkType = "HSDPA";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA){
                    networkType = "HSPA";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP){
                    networkType = "HSPAP";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA){
                    networkType = "HSUPA";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                    networkType = "UMTS";
                }
                else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                    networkType = "LTE";
                }
                else {
                    networkType = "UNKNOWN";
                }
            }

            //Build JSON input string
            JSONStringer js = new JSONStringer();
            String jsonRequest = js
                .object()
                    .key("PhoneImei").value(imei)
                    .key("SimImsi").value(imsi)
                    .key("SimSerialNo").value(simSerialNo)
                    //.key("simOperatorName").value(simOperatorName)
                    .key("NetworkOperatorName").value(networkOperatorName)
                    //.key("networkOperator").value(networkOperator)
                    .key("NetworkType").value(networkType)
                    //.key("msisdn").value(msisdn)
                    .key("VslaCode").value(vslaCode)
                    .key("PassKey").value(passKey)
                .endObject()
                .toString();


            activateVlsaUsingPostAsync(jsonRequest);
        }
        catch(Exception ex) {
            return;
        }
    }

    // The definition of our task class
    private class PostTask extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //displayProgressBar("Downloading...");
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject result = null;
            String uri = params[0];
            try {
                //instantiates httpclient to make request
                DefaultHttpClient httpClient = new DefaultHttpClient();

                //url with the post data
                HttpPost httpPost = new HttpPost(uri);

                //passes the results to a string builder/entity
                StringEntity se = new StringEntity(params[1]);

                //sets the post request as the resulting string
                httpPost.setEntity(se);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Response handler
                ResponseHandler<String> rh = new ResponseHandler<String>() {
                    // invoked when client receives response
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

                        // get response entity
                        HttpEntity entity = response.getEntity();
                        httpStatusCode = response.getStatusLine().getStatusCode();

                        // read the response as byte array
                        StringBuffer out = new StringBuffer();
                        byte[] b = EntityUtils.toByteArray(entity);

                        // write the response byte array to a string buffer
                        out.append(new String(b, 0, b.length));
                        return out.toString();
                    }
                };

                String responseString = httpClient.execute(httpPost, rh);

                // close the connection
                httpClient.getConnectionManager().shutdown();

                if(httpStatusCode == 200) //sucess
                {
                    result = new JSONObject(responseString);
                }

                return result;
            }
            catch(ClientProtocolException exClient) {
                return null;
            }
            catch(IOException exIo) {
                return null;
            }
            catch(JSONException exJson) {
                return null;
            }
            catch(Exception ex) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //updateProgressBar(values[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            String vslaName = null;
            String passKey = null;
            super.onPostExecute(result);

            try {
                if(result != null) {
                    activationSuccessful = result.getBoolean("IsActivated");
                    vslaName = result.getString("VslaName");
                    passKey = result.getString("PassKey");
                }
                if(activationSuccessful && null != vslaName) {
                    boolean retVal = vslaInfoRepo.saveVslaInfo(targetVslaCode,vslaName, passKey);
                    if(retVal) {
                        Toast.makeText(ActivationActivity.this, "Congratulations! Activation Completed Successfully.", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(i);
                    }
                }
                else {
                    Utils.createAlertDialogOk(ActivationActivity.this, "Activation Failed", "There is no Secure Internet Connection to the Bank at this time. Please try again later.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                }
            }
            catch(JSONException exJson) {
                Utils.createAlertDialogOk(ActivationActivity.this, "Activation Failed", "There was a problem during the activation. Please try again later.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            }
            catch(Exception ex) {
                Utils.createAlertDialogOk(ActivationActivity.this, "Activation Failed", "There was a problem during the activation. Please try again later.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            }
        }
    }
    
}
