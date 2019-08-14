package org.applab.ledgerlink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.w3c.dom.Text;

import java.io.IOException;


public class ProfileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        String serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "getprofile");
        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
        VslaInfo vslaInfo = vslaInfoRepo.getVslaInfo();
        if(vslaInfo != null) {
            JSONStringer js = new JSONStringer();
            try {
                js.object()
                        .key("VslaCode").value(vslaInfo.getVslaCode())
                        .endObject();
                new ProfileAsync(this).execute(serverUri, js.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ProfileAsync extends AsyncTask<String, String, JSONObject>{
        protected Context context;
        protected boolean isConnected;
        protected ProgressDialog progressDialog;
        protected int httpStatusCode;

        public ProfileAsync(Context context){
            this.context = context;
            this.isConnected = false;
            this.progressDialog = null;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(this.context);
            progressDialog.setTitle(getString(R.string.retrieving_vsla_profile));
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setProgress(1);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... params){
            JSONObject result = null;
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
                        result = new JSONObject(response);
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
        protected void onPostExecute(JSONObject jsonObject){
            if(isConnected){
                if(jsonObject != null){
                    try {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("GetProfileResult");
                        String vslaName = jsonObject1.getString("VslaName");
                        TextView txtVslaName = (TextView)findViewById(R.id.profileVslaName);
                        txtVslaName.setText(getString(R.string.vsla_name_profile) + vslaName);

                        String vslaCode = jsonObject1.getString("VslaCode");
                        TextView txtVslaCode = (TextView)findViewById(R.id.profileVslaCode);
                        txtVslaCode.setText(getString(R.string.vsla_code_profile) + vslaCode);

                        String contactPerson = jsonObject1.getString("ContactPerson");
                        TextView txtContactPerson = (TextView)findViewById(R.id.profileContactPerson);
                        txtContactPerson.setText(getString(R.string.contact_person_profile) + contactPerson);

                        String phoneNumber = jsonObject1.getString("PhoneNumber");
                        TextView txtPhoneNumber = (TextView)findViewById(R.id.profilePhoneNumber);
                        txtPhoneNumber.setText(getString(R.string.phone_number_profile) + phoneNumber);

                        String region = jsonObject1.getString("Region");
                        TextView txtVslaRegion = (TextView)findViewById(R.id.profileVslaRegion);
                        txtVslaRegion.setText(getString(R.string.region_profile) + region);

                        String positionInVsla = jsonObject1.getString("PositionInVsla");
                        TextView txtPositionInVsla = (TextView)findViewById(R.id.profilePositionInVsla);
                        txtPositionInVsla.setText(getString(R.string.position_vsla_profile) + positionInVsla);

                    }catch(Exception e){
                        e.printStackTrace();
                        DialogMessageBox.show(this.context, getString(R.string.internal_error), getString(R.string.ledger_link_suffered_inernal_error));
                    }
                }
            }else{
                DialogMessageBox.show(context, getString(R.string.error_msg), getString(R.string.not_connected_to_internet));
            }
            dismissProgressDialog();
        }

        protected void dismissProgressDialog(){
            if(progressDialog != null){
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }
}
