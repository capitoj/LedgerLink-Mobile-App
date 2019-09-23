package org.applab.ledgerlink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.domain.model.FinancialInstitution;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Network;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.adapters.DropDownAdapter;
import org.applab.ledgerlink.repo.FinancialInstitutionRepo;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;


public class ActivationActivity extends AppCompatActivity {
    LedgerLinkApplication ledgerLinkApplication;
    HttpClient client;
    private int httpStatusCode = 0; //To know whether the Request was successful
    private boolean activationSuccessful = false;
    private String targetVslaCode = null; //fake-fix
    private String securityPasskey = null;
    private ProgressDialog progressDialog = null;
    private Spinner dropdownFinancialInstitution = null;
    private FinancialInstitution targetFinancialInstitution = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_activation);

        //TextView versionText = (TextView) findViewById(R.id.txtVersionInfo);
        //versionText.setText(getApplicationContext().getResources().getString(R.string.about_version));


        //ActionBar actionBar = getSupportActionBar();

        this.buildFinancialInstitutionSpinner();

        ImageView imgVALogo = (ImageView) findViewById(R.id.imgVALogo);
        imgVALogo.setImageResource(R.drawable.ic_ledger_link_logo_original);
        imgVALogo.setLayoutParams(new RelativeLayout.LayoutParams((int) this.getResources().getDimension(R.dimen.logo_width), (int) this.getResources().getDimension(R.dimen.logo_height)));

        Button btnActivate = (Button) findViewById(R.id.btnVAActivate);
        btnActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveActivatedVslaInfo();
            }
        });

    }

    protected void getSelectedFinancialInstitution(){
        List<FinancialInstitution> listFinancialInstutions = FinancialInstitutionRepo.getFinancialInstitutions(this);
        if(this.dropdownFinancialInstitution.getSelectedItemPosition() > 0){
            int selectedIndex = this.dropdownFinancialInstitution.getSelectedItemPosition() - 1;
            this.targetFinancialInstitution = listFinancialInstutions.get(selectedIndex);
        }

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

    private void activateVlsaUsingPostAsync(String request) {

        if(Network.isConnected(getApplicationContext())) {
            VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(getApplicationContext());
            VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
            FinancialInstitutionRepo financialInstitutionRepo = new FinancialInstitutionRepo(getApplicationContext(), vslaInfo.getFiID());
            FinancialInstitution financialInstitution = financialInstitutionRepo.getFinancialInstitution();
//            String baseUrl = "http://127.0.0.1:82";
            String baseUrl = "http://" + financialInstitution.getIpAddress();
            String uri = String.format("%s/%s/%s", baseUrl, getString(R.string.digitizingdata), getString(R.string.activate));
//            String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "activate");
            new PostTask(this).execute(uri, request);
        }else{
            this.saveOfflineVslaInfo();
        }

        //Do the other stuff in the Async Task
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activation, menu);
        return true;
    }

    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent i = null;
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                // Launch preferences activity
//                i = new Intent(this, SettingsActivity.class);
//                startActivity(i);
//                break;
//            case R.id.action_recovery:
//                this.launchDataRecovery();
//                break;
//        }
        return true;
    }

    protected void launchDataRecovery(){
        if(Connection.isNetworkConnected(this)) {
            Intent intent = new Intent(this, DataRecoveryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            DialogMessageBox.show(this, getString(R.string.connection_alert), getString(R.string.no_internet_connection));
        }
    }

    protected void buildFinancialInstitutionSpinner(){
        this.dropdownFinancialInstitution = (Spinner)findViewById(R.id.listVAFinanicalInstitution);
        List<FinancialInstitution> listFinancialInstutions = FinancialInstitutionRepo.getFinancialInstitutions(this);
        String[] financialInstitutions = new String[listFinancialInstutions.size()];
        for(int i = 0; i < listFinancialInstutions.size(); i++){
            financialInstitutions[i] = listFinancialInstutions.get(i).getName();
        }
        Arrays.sort(financialInstitutions);
        financialInstitutions = ArrayUtils.addAll(new String[]{getString(R.string.select_financial_institution)}, financialInstitutions);

        ArrayAdapter<CharSequence> financialInstitutionAdapter = new DropDownAdapter(this, financialInstitutions);
        financialInstitutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.dropdownFinancialInstitution.setAdapter(financialInstitutionAdapter);
        this.dropdownFinancialInstitution.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
        this.dropdownFinancialInstitution.setFocusable(true);
        this.dropdownFinancialInstitution.setClickable(true);
    }

    private boolean saveOfflineVslaInfo() {
        this.getSelectedFinancialInstitution();

        try {
            TextView txtVslaCode = (TextView) findViewById(R.id.txtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);
            TextView txtConfirmPassKey = (TextView) findViewById(R.id.txtVAConfirmPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();
            String confirmPassKey = txtConfirmPassKey.getText().toString().trim();

            if (vslaCode.length() <= 0) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.vsla_code_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return false;
            }

            if (passKey.length() <= 0) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.pass_key_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return false;
            }

            //As per requirements, set the passkey length
            if (passKey.length() <= Integer.parseInt(getResources().getString(R.string.minimum_passkey_length))) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.passkey_atleast_five_digits_long), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return false;
            }

            if (!passKey.equalsIgnoreCase(confirmPassKey)) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.passkeys_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return false;
            }

            return ledgerLinkApplication.getVslaInfoRepo().saveOfflineVslaInfo(vslaCode, passKey, this.targetFinancialInstitution.getFiID());
        } catch (Exception ex) {
            return false;
        }
    }

    private void saveActivatedVslaInfo() {
        try {

            this.getSelectedFinancialInstitution();

            TextView txtVslaCode = (TextView) findViewById(R.id.txtVAVslaCode);
            TextView txtPassKey = (TextView) findViewById(R.id.txtVAPassKey);
            TextView txtConfirmPassKey = (TextView) findViewById(R.id.txtVAConfirmPassKey);

            String vslaCode = txtVslaCode.getText().toString().trim();
            String passKey = txtPassKey.getText().toString().trim();
            String confirmPassKey = txtConfirmPassKey.getText().toString().trim();

            if (vslaCode.length() <= 0) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.vsla_code_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtVslaCode.requestFocus();
                return;
            }

            if (passKey.length() <= 0) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.pass_key_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return;
            }

            //As per requirements, set the passkey length
            if (passKey.length() < Integer.parseInt(getResources().getString(R.string.minimum_passkey_length))) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.passkey_should_be_atleast) + getResources().getString(R.string.minimum_passkey_length) + getString(R.string.digits_long), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtPassKey.requestFocus();
                return;
            }

            if (!passKey.equalsIgnoreCase(confirmPassKey)) {
                Utils.createAlertDialogOk(ActivationActivity.this, getString(R.string.Registation_main), getString(R.string.passkeys_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
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

            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            imei = tm.getDeviceId();

            //if there is a SIM Card then get details
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                imsi = tm.getSubscriberId();
                simSerialNo = tm.getSimSerialNumber();
                simOperatorName = tm.getSimOperatorName();
                networkOperatorName = tm.getNetworkOperatorName();
                networkOperator = tm.getNetworkOperator();
                msisdn = tm.getLine1Number();

                if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
                    networkType = "EDGE";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS) {
                    networkType = "GPRS";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) {
                    networkType = "HSDPA";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA) {
                    networkType = "HSPA";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP) {
                    networkType = "HSPAP";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA) {
                    networkType = "HSUPA";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS) {
                    networkType = "UMTS";
                } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                    networkType = "LTE";
                } else {
                    networkType = getString(R.string.unknown);
                }
            }

            //Build JSON input string
            JSONStringer js = new JSONStringer();
            String jsonRequest = js
                    .object()
                    .key(getString(R.string.phone_imei)).value(imei)
                    .key(getString(R.string.sim_imsi)).value(imsi)
                    .key(getString(R.string.sim_serial_no)).value(simSerialNo)
                    //.key("simOperatorName").value(simOperatorName)
                    .key(getString(R.string.network_operator_name)).value(networkOperatorName)
                    //.key("networkOperator").value(networkOperator)
                    .key(getString(R.string.network_type)).value(networkType)
                    //.key("msisdn").value(msisdn)
                    .key(getString(R.string.vsla_code_main)).value(vslaCode)
                    .key(getString(R.string.passkey_main)).value(passKey)
                    .endObject()
                    .toString();


            activateVlsaUsingPostAsync(jsonRequest);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // The definition of our task class
    private class PostTask extends AsyncTask<String, Integer, JSONObject> {

        //Use a Weak Reference
        private final WeakReference<ActivationActivity> activationActivityWeakReference;
        private String message = getString(R.string.please_wait);
        private Integer selectedIndex;

        //Initialize the Weak reference in the constructor
        public PostTask(ActivationActivity activationActivity) {
            this.activationActivityWeakReference = new WeakReference<ActivationActivity>(activationActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (activationActivityWeakReference.get() != null && !activationActivityWeakReference.get().isFinishing()) {
                    if (null == progressDialog) {
                        progressDialog = new ProgressDialog(activationActivityWeakReference.get());
                        progressDialog.setTitle(getString(R.string.Registation_main));
                        progressDialog.setMessage(getString(R.string.sending_registration));
                        progressDialog.setMax(10);
                        progressDialog.setProgress(1);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                assert progressDialog != null;
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
                Log.d("URI", getString(R.string.url_is) + params[0]);
                Log.d(getString(R.string.request), getString(R.string.request_is) + params[1]);
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
                Log.d(getString(R.string.Registation_main), getString(R.string.response_is) + responseString);
                // close the connection
                httpClient.getConnectionManager().shutdown();

                if (httpStatusCode == 200) //sucess
                {
                    result = new JSONObject(responseString);
                }

                return result;
            } catch (ClientProtocolException exClient) {
                exClient.printStackTrace();
                return null;
            } catch (IOException exIo) {
                exIo.printStackTrace();
                return null;
            } catch (JSONException exJson) {
                exJson.printStackTrace();
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
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
                if (result != null) {
                    activationSuccessful = result.getBoolean(getString(R.string.is_activated_main));
                    vslaName = result.getString(getString(R.string.vsla_name_main));
                    passKey = result.getString(getString(R.string.passkey_main));
                }
                if (activationSuccessful && null != vslaName) {
                    retrievedVslaNameSavedSuccessfully = ledgerLinkApplication.getVslaInfoRepo().saveVslaInfo(targetVslaCode, vslaName, passKey, targetFinancialInstitution.getFiID());
                    if (retrievedVslaNameSavedSuccessfully) {
                        Toast.makeText(ActivationActivity.this, R.string.congs_reg_completed, Toast.LENGTH_LONG).show();
                        dismissProgressDialog();
                        Intent i = new Intent(getBaseContext(), LoginActivity.class);
                        i.putExtra(getString(R.string._wasCalledFromActivation), true);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(ActivationActivity.this, R.string.reg_failed_while_writing_retrieved_vsla_name, Toast.LENGTH_LONG).show();
                        dismissProgressDialog();
                    }
                } else {
                    //Process failed
                    Toast.makeText(getApplicationContext(), R.string.reg_failed_due_to_internet, Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                }

                //In case activation failed
                if (!retrievedVslaNameSavedSuccessfully) {
                    //Save the record offline
                    ledgerLinkApplication.getVslaInfoRepo().saveOfflineVslaInfo(targetVslaCode, securityPasskey, targetFinancialInstitution.getFiID());

                    //Then call the login activity
                    Intent i = new Intent(getBaseContext(), LoginActivity.class);
                    i.putExtra(getString(R.string._wasCalledFromActivation), true);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            } catch (JSONException exJson) {
                //Process failed
                exJson.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.reg_failed_due_to_invalid_data_format, Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            } catch (Exception ex) {
                ex.printStackTrace();
                //Process failed
                Toast.makeText(getApplicationContext(), R.string.reg_failed_try_again_later, Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }
        }

        //Dismisses the currently showing progress dialog
        private void dismissProgressDialog() {
            if (progressDialog != null) {
                progressDialog.dismiss();
                //set it to null
                progressDialog = null;
            }
        }
    }

}
