package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.SendDataRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class ActivationActivity extends Activity {
    VslaInfoRepo vslaInfoRepo = null;
    HttpClient client;
    int httpStatusCode = 0; //To know whether the Request was successful
    boolean activationSuccessful = false;
    String targetVslaCode = null; //fake-fix
    ProgressDialog progressDialog = null;

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

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onDestroy();
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
        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,"vslas","activate");
        new PostTask(this).execute(uri,request);

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

        //Use a Weak Reference
        private final WeakReference<ActivationActivity> activationActivityWeakReference;
        private String message = "Please wait...";

        //Initialize the Weak reference in the constructor
        public PostTask(ActivationActivity activationActivity) {
            this.activationActivityWeakReference = new WeakReference<ActivationActivity>(activationActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (activationActivityWeakReference.get() != null && !activationActivityWeakReference.get().isFinishing()) {
                    if(null == progressDialog) {
                        progressDialog = new ProgressDialog(activationActivityWeakReference.get());
                        progressDialog.setTitle("Phone Activation");
                        progressDialog.setMessage("Activating the VSLA Phone for Ledger Link. Please wait...");
                        progressDialog.setMax(10);
                        progressDialog.setProgress(1);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                    }
                }
            }
            catch(Exception ex) {
                progressDialog.setMessage(ex.getMessage());
            }
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
            progressDialog.setProgress(values[0]);
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
                        dismissProgressDialog();
                        Intent i = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(i);
                    }
                }
                else {
                    //Process failed
                    Toast.makeText(getApplicationContext(), "Activation failed due to internet connection problems. Try again later.",Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                }
            }
            catch(JSONException exJson) {
                //Process failed
                Toast.makeText(getApplicationContext(), "Activation failed due to invalid Data Format. Try again later.",Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }
            catch(Exception ex) {
                //Process failed
                Toast.makeText(getApplicationContext(), "Activation failed. Try again later.",Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }
        }

        //Dismisses the currently showing progress dialog
        private void dismissProgressDialog() {
            if(progressDialog != null) {
                progressDialog.dismiss();
                //set it to null
                progressDialog = null;
            }
        }
    }
    
}
