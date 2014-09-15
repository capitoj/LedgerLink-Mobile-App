package org.applab.digitizingdata.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;




/**
 * Created by John Mark on 6/18/2014.
 */
public class LongTaskRunner  extends AsyncTask<Runnable, Void, Void>
{

        private final WeakReference<Activity> activityWeakReference;
        ProgressDialog progressDialog;
        String title, info;
        public LongTaskRunner(Activity parentActivity, String title, String information) {
            super();
            this.title = title;
            this.info = information;
            this.activityWeakReference = new WeakReference<Activity>(parentActivity);
        }

        @Override
        protected Void doInBackground(Runnable ... runnables)
        {
            runnables[0].run();
            dismissProgressDialog();
            return null;
        }



        // can use UI thread here
        protected void onPreExecute() {
            Log.d("onpre", "on pre start");
            progressDialog = new ProgressDialog(activityWeakReference.get());
            progressDialog.setTitle(title);

            progressDialog.setMessage(info);
            progressDialog.setMax(10);
            progressDialog.setProgress(1);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }


        @Override
        protected void onPostExecute(Void result) {

            dismissProgressDialog();
        }

        private void dismissProgressDialog() {
            if (progressDialog != null) {
                progressDialog.dismiss();
                //set it to null
                progressDialog = null;
            }
        }


    public static void runLongTask(Runnable r, String title, String info, Activity parentActivity) {

        new LongTaskRunner(parentActivity, title, info).execute(r, null, null);
    }
    }
