package org.applab.ledgerlink.helpers.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.DataRecoveryFactory;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Joseph Capito on 9/15/2015.
 */
public class DataRecoveryAsync extends AsyncTask<String, String, JSONObject> {

    protected int httpStatusCode;
    protected Context context;
    protected boolean isConnected;
    protected ProgressDialog progressDialog;
    protected VslaInfo vslaInfo;

    public DataRecoveryAsync(Context context){
        this.context = context;
        this.isConnected = false;
        this.progressDialog = null;
        this.vslaInfo = new VslaInfo();
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setTitle(context.getResources().getString(R.string.performing_data_recovery));
        progressDialog.setMessage(context.getResources().getString(R.string.please_wait));
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
            if(Connection.isNetworkConnected(context)) {
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
        }catch (ClientProtocolException e){
            return null;
        }catch (IOException e){
            return null;
        }catch (JSONException e){
            return null;
        }catch(Exception e){
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(String... values){

    }

    @Override
    protected void onPostExecute(JSONObject result){
        if(isConnected) {
            if(result != null) {
                DataRecoveryFactory.download(this.context, result);
                this.dismissProgressDialog();
            }
        }else{
            this.dismissProgressDialog();
            DialogMessageBox.show(this.context, context.getString(R.string.vsla_data_recovery), context.getString(R.string.vsla_data_details));
        }
    }

    protected void dismissProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
