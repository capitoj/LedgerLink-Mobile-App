package org.applab.ledgerlink;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import org.applab.ledgerlink.domain.model.FinancialInstitution;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.FinancialInstitutionRepo;
import org.applab.ledgerlink.repo.SampleDataBuilderRepo;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.service.InboundChatReceiver;
import org.applab.ledgerlink.service.OutboundChatReceiver;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import android.view.Menu;
//import android.view.Menu;

public class LoginActivity extends AppCompatActivity {

    LedgerLinkApplication ledgerLinkApplication;
    private VslaInfo vslaInfo = null;
    private Context context;

    //variables for activating the VSLA
    HttpClient client;
    private int httpStatusCode = 0; //To know whether the Request was successful
    private boolean activationSuccessful = false;
    private String targetVslaCode = null; //fake-fix
    private ProgressDialog progressDialog = null;
    protected Intent alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_login);

        this.context = this;

        this.loadBackgroundService();
        
        // Android request permission modal
        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        //TextView versionText = (TextView) findViewById(R.id.txtVersionInfo);
        //versionText.setText(getApplicationContext().getResources().getString(R.string.about_version));
        //
        TextView ForgotPassKeyText = (TextView) findViewById(R.id.txtForgetPassKey);
        ForgotPassKeyText.setText(getApplicationContext().getResources().getString(R.string.forgot_passkey));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        //TODO: Setting of Preferences is done in the first Activity that is launched.
        //Load the default Shared Preferences
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        //Read some settings like Server URL
        Utils.configureDefaultApplicationPreferences(getApplicationContext());

        //Load Sample Trainng Data: Testing
        Runnable dataLoader = new Runnable() {
            @Override
            public void run() {
                if (Utils.isExecutingInTrainingMode()) {
                    SampleDataBuilderRepo.refreshTrainingData(getApplicationContext());
                }
            }
        };
        //Load this as long running task
        LongTaskRunner.runLongTask(dataLoader, getString(R.string.please_wait), getString(R.string.ledgerlink_refreshes_test_training_data), LoginActivity.this);


        //  TextView tvSwitchMode = (TextView)findViewById(R.id.lblSISwitchMode);

        ImageView imgVALogo = (ImageView) findViewById(R.id.imgVALogo);
        imgVALogo.setImageResource(R.drawable.ic_ledger_link_logo_original);
        imgVALogo.setLayoutParams(new RelativeLayout.LayoutParams((int) this.getResources().getDimension(R.dimen.login_logo_width), (int) this.getResources().getDimension(R.dimen.login_logo_height)));


        //If we are in training mode then show it using a custom View with distinguishable background
        //Assumed that the preferences have been set by now
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setTitle(R.string.training_mode);
            actionBar.setCustomView(R.layout.activity_main_training_mode);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);

            //Set the label of the link
            //         tvSwitchMode.setText("Switch To Actual VSLA Data");
            //       tvSwitchMode.setTag("1"); //The Mode to switch to {1 Actual | 2 Training}
        } else {
            actionBar.hide();
            //Set the label of the link
            //     tvSwitchMode.setText("Switch To Training Mode");
            //   tvSwitchMode.setTag("2"); //The Mode to switch to {1 Actual | 2 Training}
        }

        //Check whether the VSLA has been Activated
        vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();


        boolean wasCalledFromActivation = getIntent().getBooleanExtra("_wasCalledFromActivation", false);

        //Determine whether to show the not-activated status
        String notActivatedStatusMessage = "";

        if (vslaInfo != null) {
            if (!vslaInfo.isActivated()) {
                if (vslaInfo.isOffline()) {
                    if (wasCalledFromActivation) {
                        notActivatedStatusMessage = getString(R.string.unable_to_send_reg_network_problems);
                    } else {
                        notActivatedStatusMessage = getString(R.string.unable_to_send_reg_network_problems_last_time);
                    }
                } else {
                    //If it is not Activated and is not in Offline Mode, force activation
                    Intent i = new Intent(LoginActivity.this, ActivationActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            } else {
                //If VSLA is Activated proceed with normal process
            }
        } else {
            //if VSLAInfo is NULL, force Activation
            //If it is not Activated and is not in Offline Mode, force activation

            Intent i = new Intent(LoginActivity.this, ActivationActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }

        // Pass key Label
        TextView lblPasskey = (TextView) findViewById(R.id.lblPassKeyPrompt);

        // VSLA name placeholder
        TextView txtVslaName = (TextView) findViewById(R.id.lbl_vsla_name);
        String vslaName = "";

        // Activation Information placeholder
        //TextView activationLoginMsg = (TextView) findViewById(R.id.lblActivationLoginMsg);

        if (vslaInfo != null && vslaInfo.isActivated()) {
            vslaName = vslaInfo.getVslaName();
            txtVslaName.setText(vslaName);
            //activationLoginMsg.setVisibility(View.GONE);
        } else {
            txtVslaName.setVisibility(View.INVISIBLE);
            DialogMessageBox.show(this, getString(R.string.activation_message), getString(R.string.unable_to_send_reg_network_problems));
            //activationLoginMsg.setText(notActivatedStatusMessage);
            lblPasskey.setVisibility(View.INVISIBLE);
        }

        // ---Button view---
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView txtPassKey = (TextView) findViewById(R.id.edt_passkey);
                String passKey = txtPassKey.getText().toString().trim();

                if (passKey.equalsIgnoreCase(vslaInfo.getPassKey())) {
                    if (vslaInfo.isActivated()) {
                        startMainMenuActivity();
                    } else {
                        activateVslaAndSignIn();
                    }
                } else {
                    Utils.createAlertDialogOk(LoginActivity.this, getString(R.string.security), getString(R.string.passkey_invalid), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                }
            }
        });


        /** Change lanugage spinner**/

        Spinner spinnerLang = (Spinner) findViewById(R.id.spinner_lang);

        //spinnerLang.setOnItemSelectedListener(this);

        // Lanugage list
        List<String> languages = new ArrayList<String>();languages.add("Select Language");

        languages.add("English");
        languages.add("Acholi");
        languages.add("Arabic");
        languages.add("Bari");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, languages);
        // attaching data adapter to spinner
        spinnerLang.setAdapter(dataAdapter);

        //Make the spinner selectable
        spinnerLang.setFocusable(true);
        spinnerLang.setClickable(true);


        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 1) {
                    Toast.makeText(parent.getContext(),
                            "You have selected English", Toast.LENGTH_SHORT)
                            .show();
                    setLocale("en");
                }else if (pos == 2) {
                    Toast.makeText(parent.getContext(),
                            "You have selected Acholi", Toast.LENGTH_SHORT)
                            .show();
                    setLocale("ac");
                } else if (pos == 3) {
                    Toast.makeText(parent.getContext(),
                            "You have selected Arabic", Toast.LENGTH_SHORT)
                            .show();
                    setLocale("ar");
                }
                else if (pos == 4) {
                    Toast.makeText(parent.getContext(),
                            "You have selected Bari", Toast.LENGTH_SHORT)
                            .show();
                    setLocale("ba");
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {

                // TODO Auto-generated method stub

            }
        });

        /** Forgot Passkey OnClick**/
        ForgotPassKeyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PassKeyResetActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }


    /** Android request permission  **/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(LoginActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /** Change lanugage spinner**/

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, LoginActivity.class);
        startActivity(refresh);
        finish();
    }


    protected void loadBackgroundService(){
        /*
        alarm = new Intent(this.context, AlarmReceiver.class);
        this.startBackgroundService(this.context, alarm, 30000);

        alarm = new Intent(this.context, TrainingModuleReceiver.class);
        this.startBackgroundService(this.context, alarm, 15000);
        */

        alarm = new Intent(context, OutboundChatReceiver.class);
        this.startBackgroundService(context, alarm, 2000);

        alarm = new Intent(context, InboundChatReceiver.class);
        this.startBackgroundService(context, alarm, 2000);    }

    protected void startBackgroundService(Context context, Intent alarm, int interval){
        boolean alarmRunning = (PendingIntent.getBroadcast(context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmRunning){
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pendingIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Launch preferences activity
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.action_about:
                // Launch about dialog
                AboutDialog about = new AboutDialog(this);
                about.setTitle(R.string.about_ledgerlink);
                about.show();
                break;
            case R.id.action_recovery:
                // Launch Data Recovery
                this.launchDataRecovery();
                break;
            case R.id.action_training_modules:
                //Launch the training modules
                i = new Intent(this, TrainingModuleActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    protected void launchDataRecovery(){
        if(Connection.isNetworkConnected(this)) {
            Intent intent = new Intent(this, DataRecoveryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            DialogMessageBox.show(this, getString(R.string.connection_alert), getString(R.string.no_internet_connection_be_established));
        }
    }


    private void startMainMenuActivity() {
        boolean showGettingStartedWizard = false;
        //Determine whether to start the Getting Started Wizard or the Main Menu
        //For now just consider the Vsla Cycle. May be later we shall include a few members
        //Changed this condition to check the getting started wizard flag


        if (!vslaInfo.isGettingStartedWizardComplete()) {
            showGettingStartedWizard = true;
        }

        Intent mainMenu;
        if (showGettingStartedWizard) {
            mainMenu = new Intent(getBaseContext(), Utils.resolveGettingStartedWizardStage(vslaInfo.getGettingStartedWizardStage()));
            if (vslaInfo.getGettingStartedWizardStage() == Utils.GETTING_STARTED_PAGE_REVIEW_CYCLE) {
                //we are in review cycle mode
                mainMenu.putExtra("_isFromReviewMembers", true);
            }
        } else {
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

    private void activateVlsaUsingPostAsync(String request) {
        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(getApplicationContext());
        VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
        FinancialInstitutionRepo financialInstitutionRepo = new FinancialInstitutionRepo(getApplicationContext(), vslaInfo.getFiID());
        FinancialInstitution financialInstitution = financialInstitutionRepo.getFinancialInstitution();
//            String baseUrl = "http://127.0.0.1:82";
        String baseUrl = "http://" + financialInstitution.getIpAddress();
        String uri = String.format("%s/%s/%s", baseUrl, "DigitizingData", "activate");
        Log.e("ActivationX", uri);
//        String uri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "activate");
        new PostTask(this).execute(uri, request);

        //Do the other stuff in the Async Task
    }

    private void activateVslaAndSignIn() {
        try {
            if (vslaInfo == null) {
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
                    networkType = getString(R.string.unknown_allcaps);
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

            Log.e("ActivationData", jsonRequest.toString());
            activateVlsaUsingPostAsync(jsonRequest);
        } catch (Exception ex) {
        }
    }

    // The definition of our task class
    private class PostTask extends AsyncTask<String, Integer, JSONObject> {

        //Use a Weak Reference
        private final WeakReference<LoginActivity> loginActivityWeakReference;
        private String message = getString(R.string.please_wait);

        //Initialize the Weak reference in the constructor
        public PostTask(LoginActivity loginActivity) {
            this.loginActivityWeakReference = new WeakReference<LoginActivity>(loginActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (loginActivityWeakReference.get() != null && !loginActivityWeakReference.get().isFinishing()) {
                    if (null == progressDialog) {
                        progressDialog = new ProgressDialog(loginActivityWeakReference.get());
                        progressDialog.setTitle(getString(R.string.login));
                        progressDialog.setMessage(getString(R.string.logging_you_in));
                        progressDialog.setMax(10);
                        progressDialog.setProgress(1);
                        progressDialog.setCancelable(false);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                    }
                }
            } catch (Exception ex) {
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

                if (httpStatusCode == 200) //sucess
                {
                    result = new JSONObject(responseString);
                }

                return result;
            } catch (ClientProtocolException exClient) {
                return null;
            } catch (IOException exIo) {
                return null;
            } catch (JSONException exJson) {
                return null;
            } catch (Exception ex) {
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
                    retrievedVslaNameSavedSuccessfully = ledgerLinkApplication.getVslaInfoRepo().saveVslaInfo(targetVslaCode, vslaName, passKey, vslaInfo.getFiID());
                    if (retrievedVslaNameSavedSuccessfully) {
                        Toast.makeText(LoginActivity.this, getString(R.string.congs_reg_completed), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.reg_failed_while_writing_retrieved_vsla_name), Toast.LENGTH_LONG).show();
                    }
                } else {
                    //Process failed
                    Toast.makeText(getApplicationContext(), getString(R.string.reg_failed_due_to_internet), Toast.LENGTH_LONG).show();
                    dismissProgressDialog();
                }
            } catch (JSONException exJson) {
                //Process failed
                Toast.makeText(getApplicationContext(), getString(R.string.reg_failed_due_to_invalid_data_format), Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            } catch (Exception ex) {
                //Process failed
                Toast.makeText(getApplicationContext(), getString(R.string.reg_failed_try_again_later), Toast.LENGTH_LONG).show();
                dismissProgressDialog();
            }

            //Proceed to main Menu regardless
            finally {
                dismissProgressDialog();

                startMainMenuActivity();
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
