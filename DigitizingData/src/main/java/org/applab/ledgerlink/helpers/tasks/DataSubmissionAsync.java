package org.applab.ledgerlink.helpers.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.applab.ledgerlink.helpers.Network;

/**
 * Created by Joseph Capito on 1/14/2016.
 */
public class DataSubmissionAsync extends AsyncTask<String, String, String> {

    protected Context context;
    protected boolean isConnected;
    protected ProgressDialog progressDialog;
    protected int httpStatusCode;
    protected String progressText;
    protected boolean showProgressDialog;

    public DataSubmissionAsync(Context context){
        this.context = context;
        this.isConnected = false;
        this.progressDialog = null;
        this.progressText = "Performing Data Submission";
        this.showProgressDialog = false;
    }

    public DataSubmissionAsync(Context context, String progressText){
        this.context = context;
        this.isConnected = false;
        this.progressDialog = null;
        this.progressText = progressText;
        this.showProgressDialog = true;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setTitle(progressText);
        progressDialog.setMessage("Please wait...");
        progressDialog.setProgress(1);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(showProgressDialog)
            progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params){
        String result = null;
        try{
            result = Network.submitData(context, params[0], params[1]);
        }catch (Exception e){
            Log.e("DataSubmissionAsync", e.getMessage());
        }
        return result;
    }

    protected void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
