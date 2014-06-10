package org.applab.digitizingdata;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaInfoRepo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import com.actionbarsherlock.app.ActionBar;

public class ActivationActivity extends SherlockActivity {
    VslaInfoRepo vslaInfoRepo = null;
    HttpClient client;
    int httpStatusCode = 0; //To know whether the Request was successful
    boolean activationSuccessful = false;
    String targetVslaCode = null; //fake-fix
    String securityPasskey = null;
    ProgressDialog progressDialog = null;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_activation);

        actionBar = getSupportActionBar();

      //  TextView tvSwitchMode = (TextView)findViewById(R.id.lblVASwitchMode);

        ImageView imgVALogo = (ImageView)findViewById(R.id.imgVALogo);
        imgVALogo.setImageResource(R.drawable.ic_ledger_link_logo_original);
        imgVALogo.setLayoutParams(new RelativeLayout.LayoutParams((int)this.getResources().getDimension(R.dimen.logo_width), (int)this.getResources().getDimension(R.dimen.logo_height)));
        //If we are in training mode then show it using a custom View with distinguishable background
        //Assumed that the preferences have been set by now
        if(Utils.isExecutingInTrainingMode()) {
            actionBar.setTitle("TRAINING MODE");
            actionBar.setCustomView(R.layout.activity_main_training_mode);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);

            //Set the label of the link
          //  tvSwitchMode.setText("Switch To Actual VSLA Data");
           // tvSwitchMode.setTag("1"); //The Mode to switch to {1 Actual | 2 Training}
        }
        else {
            actionBar.hide();
            //Set the label of the link
          //  tvSwitchMode.setText("Switch To Training Mode");
           // tvSwitchMode.setTag("2"); //The Mode to switch to {1 Actual | 2 Training}
        }

        //Set the textview to be underline
        // tvSwitchMode.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

      /**  tvSwitchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences appPrefs = Utils.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefEditor = appPrefs.edit();
                if (Utils.isExecutingInTrainingMode()) {
                    prefEditor.putString(SettingsActivity.PREF_KEY_EXECUTION_MODE, SettingsActivity.PREF_VALUE_EXECUTION_MODE_PROD);
                    Toast.makeText(getApplicationContext(), "Execution switched to Actual VSLA Data. Please start the app again.", Toast.LENGTH_LONG).show();
                } else {
                    prefEditor.putString(SettingsActivity.PREF_KEY_EXECUTION_MODE, SettingsActivity.PREF_VALUE_EXECUTION_MODE_TRAINING);
                    Toast.makeText(getApplicationContext(), "Execution switched to Training Mode. Please start the app again.", Toast.LENGTH_LONG).show();
                }

                //Save the values
                //Can use apply() but would require API 9
                prefEditor.commit();

                finish();
            }
        }); */

        vslaInfoRepo = new VslaInfoRepo(ActivationActivity.this);
        // ---Button view---
        Button btnActivate = (Button)findViewById(R.id.btnVAActivate);
        btnActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveActivatedVslaInfo();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void activateVlsaUsingPostAsync(String request)
    {
        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,"vslas","activate");
        new PostTask(this).execute(uri, request);

        //Do the other stuff in the Async Task
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.activation, menu);
        return true;
    }

    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Launch preferences activity
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    private boolean saveOfflineVslaInfo() {
        try{
            TextView txtVslaCode = (TextView) findViewById(R.id.edtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);
            TextView txtConfirmPassKey = (TextView) findViewById(R.id.txtVAConfirmPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();
            String confirmPassKey = txtConfirmPassKey.getText().toString().trim();

            if(vslaCode.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Registration","The VSLA Code is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return false;
            }

            if(passKey.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Registration","The Pass Key is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return false;
            }

            if(!passKey.equalsIgnoreCase(confirmPassKey)) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Registration","The Pass Keys do not match.", Utils.MSGBOX_ICON_EXCLAMATION).show();
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
            TextView txtVslaCode = (TextView) findViewById(R.id.edtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);
            TextView txtConfirmPassKey = (TextView) findViewById(R.id.txtVAConfirmPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();
            String confirmPassKey = txtConfirmPassKey.getText().toString().trim();

            if(vslaCode.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Registration","The VSLA Code is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return;
            }

            if(passKey.length()<=0) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Registration","The Pass Key is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return;
            }

            if(!passKey.equalsIgnoreCase(confirmPassKey)) {
                Utils.createAlertDialogOk(ActivationActivity.this,"Registration","The Pass Keys do not match.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return;
            }

            //set target vslacode
            targetVslaCode = vslaCode;

            //Set target passkey: will be used later in AsyncPost
            securityPasskey = passKey;

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
                        progressDialog.setTitle("Registration");
                        progressDialog.setMessage("Sending registration");
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
            boolean retrievedVslaNameSavedSuccessfully = false; //Indicates that activation succeeded and vslaName retrieved was saved

            try {
                if(result != null) {
                    activationSuccessful = result.getBoolean("IsActivated");
                    vslaName = result.getString("VslaName");
                    passKey = result.getString("PassKey");
                }
                if(activationSuccessful && null != vslaName) {
                    retrievedVslaNameSavedSuccessfully = vslaInfoRepo.saveVslaInfo(targetVslaCode,vslaName, passKey);
                    if(retrievedVslaNameSavedSuccessfully) {
                        Toast.makeText(ActivationActivity.this, "Congratulations! Registration Completed Successfully.", Toast.LENGTH_LONG).show();
                        dismissProgressDialog();
                        Intent i = new Intent(getBaseContext(), LoginActivity.class);
                        i.putExtra("_wasCalledFromActivation", true);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                    else {
                        Toast.makeText(ActivationActivity.this, "Registration failed while writing retrieved VSLA Name on the local database. Try again later.", Toast.LENGTH_LONG).show();
                        dismissProgressDialog();
                    }
                }
                else {
                    //Process failed
                    Toast.makeText(getApplicationContext(), "Registration failed due to internet connection problems. Try again later.",Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                }

                //In case activation failed
                if(!retrievedVslaNameSavedSuccessfully) {
                    //Save the record offline
                    vslaInfoRepo.saveOfflineVslaInfo(targetVslaCode,securityPasskey);

                    //Then call the login activity
                    Intent i = new Intent(getBaseContext(), LoginActivity.class);
                    i.putExtra("_wasCalledFromActivation", true);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }
            catch(JSONException exJson) {
                //Process failed
                Toast.makeText(getApplicationContext(), "Registration failed due to invalid Data Format. Try again later.",Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }
            catch(Exception ex) {
                //Process failed
                Toast.makeText(getApplicationContext(), "Registration failed. Try again later.",Toast.LENGTH_LONG).show();
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
