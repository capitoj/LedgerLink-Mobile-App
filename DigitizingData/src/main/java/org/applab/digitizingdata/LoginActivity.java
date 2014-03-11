package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaCycleRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class LoginActivity extends Activity {
    VslaInfoRepo vslaInfoRepo = null;
    VslaInfo vslaInfo = null;
    boolean wasCalledFromActivation = false;

    //variables for activating the VSLA
    HttpClient client;
    int httpStatusCode = 0; //To know whether the Request was successful
    boolean activationSuccessful = false;
    String targetVslaCode = null; //fake-fix
    ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //Check whether the VSLA has been Activated
        vslaInfoRepo = new VslaInfoRepo(LoginActivity.this);
        vslaInfo = vslaInfoRepo.getVslaInfo();

        wasCalledFromActivation = getIntent().getBooleanExtra("_wasCalledFromActivation", false);

        //Determine whether to show the not-activated status
        String notActivatedStatusMessage = "";

        if(vslaInfo != null) {
            if(!vslaInfo.isActivated()) {
                if(vslaInfo.isOffline()) {
                    if(wasCalledFromActivation) {
                        notActivatedStatusMessage = "We weren't able to send your registration because of a network problem. We've saved it and will try to send it later.";
                    }
                    else {
                        notActivatedStatusMessage = "We weren't able to send your registration last time because of a network problem. We've saved it and will try to send it when you sign in now.";
                    }
                }
                else {
                    //If it is not Activated and is not in Offline Mode, force activation
                    Intent i = new Intent(LoginActivity.this, ActivationActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }
            else {
                //If VSLA is Activated proceed with normal process
            }
        }
        else {
            //if VSLAInfo is NULL, force Activation
            //If it is not Activated and is not in Offline Mode, force activation
            Intent i = new Intent(LoginActivity.this, ActivationActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        TextView txtVslaName = (TextView) findViewById(R.id.lbl_vsla_name);
        String vslaName = notActivatedStatusMessage;
        if(vslaInfo != null && vslaInfo.isActivated()) {
            vslaName = vslaInfo.getVslaName();
        }
        else {
            //Change font size
            txtVslaName.setTextSize(18);
        }
        txtVslaName.setText(vslaName);


        // ---Button view---
        Button btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView txtPassKey = (TextView)findViewById(R.id.edt_passkey);
                String passKey = txtPassKey.getText().toString().trim();

                if(passKey.equalsIgnoreCase(vslaInfo.getPassKey())) {
                    if(vslaInfo.isActivated()) {
                        startMainMenuActivity();
                    }
                    else {
                        activateVslaAndSignIn();
                    }
                }
                else {
                    Utils.createAlertDialogOk(LoginActivity.this,"Security","The Pass Key is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    private void startMainMenuActivity() {
        boolean showGettingStartedWizard = false;
        //Determine whether to start the Getting Started Wizard or the Main Menu
        //For now just consider the Vsla Cycle. May be later we shall include a few members
        VslaCycleRepo cycleRepo = new VslaCycleRepo(getApplicationContext());
        if(null == cycleRepo.getMostRecentCycle()) {
            showGettingStartedWizard = true;
        }

        Intent mainMenu = null;
        if(showGettingStartedWizard) {
            mainMenu = new Intent(getBaseContext(), GettingStartedWizardPageOne.class);
        }
        else {
            mainMenu = new Intent(getBaseContext(), MainActivity.class);
        }
        
        mainMenu.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainMenu);
        finish();
    }

    @Override
    public void onBackPressed() {
        //End the application
        finish();
    }

    private void activateVlsaUsingPostAsync(String request)
    {
        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL,"vslas","activate");
        new PostTask(this).execute(uri,request);

        //Do the other stuff in the Async Task
    }

    private void activateVslaAndSignIn() {
        try{
            if(vslaInfo == null) {
                return;
            }
            //set target vslacode
            targetVslaCode = vslaInfo.getVslaCode();

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
                    .key("VslaCode").value(vslaInfo.getVslaCode())
                    .key("PassKey").value(vslaInfo.getPassKey())
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
        private final WeakReference<LoginActivity> loginActivityWeakReference;
        private String message = "Please wait...";

        //Initialize the Weak reference in the constructor
        public PostTask(LoginActivity loginActivity) {
            this.loginActivityWeakReference = new WeakReference<LoginActivity>(loginActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (loginActivityWeakReference.get() != null && !loginActivityWeakReference.get().isFinishing()) {
                    if(null == progressDialog) {
                        progressDialog = new ProgressDialog(loginActivityWeakReference.get());
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
                        Toast.makeText(LoginActivity.this, "Congratulations! Registration Completed Successfully.", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Registration failed while writing retrieved VSLA Name on the local database. Try again later.", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    //Process failed
                    Toast.makeText(getApplicationContext(), "Registration failed due to internet connection problems. Try again later.",Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
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

            //Proceed to main Menu regardless
            finally{
                dismissProgressDialog();

                startMainMenuActivity();
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
