package org.applab.ledgerlink;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.applab.ledgerlink.helpers.Network;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.tasks.DataSubmissionAsync;
import org.applab.ledgerlink.utils.Connection;
import org.json.JSONObject;
import org.json.JSONStringer;


public class MODActivity extends ActionBarActivity{

    protected int clickIndex;
    protected Context context;
    protected EditText txtMODAmount;
    protected boolean hasResponse;
    protected TextView lblMODResult;
    protected MenuItem menuItem;
    protected double eligibleAmount;
    protected String hashKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtMODAmount = (EditText)findViewById(R.id.txtMODAmount);
        eligibleAmount = 0;
        clickIndex = 0;
        context = this;
        hasResponse = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mod, menu);
        setMenuAction(menu);
        return true;
    }

    protected void setMenuAction(Menu menu){
        menuItem = menu.findItem(R.id.menuDRAccept);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switchLayoutView();
                return true;
            }
        });
    }

    protected void switchLayoutView(){
        if(clickIndex == 0){
            if(txtMODAmount.getText().toString().length() == 0){
                Toast.makeText(context, R.string.please_enter_amount, Toast.LENGTH_LONG).show();
                return;
            }
            int amount = Integer.valueOf(txtMODAmount.getText().toString());
            if(Connection.isNetworkConnected(context)) {
                sendMODApplication(amount);
            }else{
                Toast.makeText(context, R.string.no_data_connection_detected, Toast.LENGTH_LONG).show();
            }
        }else if(clickIndex == 1){
            if(hashKey.length() > 0){
                if(Connection.isNetworkConnected(context)){
                    sendMODConfirmation(hashKey);
                }
            }
        }
    }

    protected void sendMODApplication(int amount){
        String serverUri = String.format("%s/%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "loanapplication", Utils.RandomGenerator.getRandomString());
        JSONStringer js = getMODJSONOutput(amount);
        new MODSubmissionAsync(context, getString(R.string.please_wait)).execute(serverUri, String.valueOf(js));
    }

    protected void sendMODConfirmation(String hashKey){
        String serverUri = String.format("%s/%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "loanconfirmation", Utils.RandomGenerator.getRandomString());
        JSONStringer js = getJSONOutput("HashKey", hashKey);
        new MODConfirmationAsync(context, getString(R.string.please_wait)).execute(serverUri, String.valueOf(js));
    }

    protected JSONStringer getJSONOutput(String key, String value){
        JSONStringer js = new JSONStringer();
        try{
            js.object();
            js = Network.getNetworkHeader(context, js);
            js.key(key).value(value);
            js.endObject();
        }catch (Exception e){
            Log.e("getJSONOutput", e.getMessage());
        }
        return js;
    }

    protected JSONStringer getMODJSONOutput(int amount){
        JSONStringer js = new JSONStringer();
        try{
            js.object();
            js = Network.getNetworkHeader(context, js);
            js.key("Amount").value(amount);
            js.endObject();
        }catch (Exception e){
            Log.e("getJSONOutput", e.getMessage());
        }
        return  js;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home : showPreviousWindow(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void showPreviousWindow(){
        finish();
    }

    protected class MODConfirmationAsync extends DataSubmissionAsync{
        public MODConfirmationAsync(Context context, String progressText){
            super(context, progressText);
        }

        @Override
        protected void onPostExecute(String result){
            if(result != null){
                try{
                    setContentView(R.layout.activity_mod_confirmation);
                    lblMODResult = (TextView) findViewById(R.id.lblMODResult);
                    JSONObject jsonObject = new JSONObject(result).getJSONObject("LoanConfirmationResult");
                    int status = jsonObject.getInt("Status");
                    if(status == 1){
                        lblMODResult.setText(jsonObject.getString("Message"));
                        menuItem.setEnabled(false);
                        menuItem.setVisible(false);
                    }
                }catch (Exception e){
                    Log.e("MODConfirmation", e.getMessage());
                }
                this.closeProgressDialog();
            }
        }
    }

    protected class MODSubmissionAsync extends  DataSubmissionAsync{

        public MODSubmissionAsync(Context context, String progressText){
            super(context, progressText);
        }

        @Override
        protected void onPostExecute(String result){

            if(result != null){
                try{
                    if (result.length() > 0) {
                        setContentView(R.layout.activity_mod_confirmation);
                        lblMODResult = (TextView) findViewById(R.id.lblMODResult);
                        JSONObject jsonObject = new JSONObject(result).getJSONObject("LoanApplicationResult");
                        if(jsonObject.getBoolean("Approved")){
                            clickIndex ++;
                            hashKey = jsonObject.getString("HashKey");
                        }else{
                            menuItem.setEnabled(false);
                            menuItem.setVisible(false);
                        }
                        lblMODResult.setText(jsonObject.getString("Message"));
                    }
                }catch (Exception e){
                    Log.e("MODSubmission", e.getMessage());
                }
            }
            this.closeProgressDialog();
        }
    }

}
