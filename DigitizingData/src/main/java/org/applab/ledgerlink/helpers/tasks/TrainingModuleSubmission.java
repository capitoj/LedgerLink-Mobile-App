package org.applab.ledgerlink.helpers.tasks;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;

/**
 * Created by Joseph Capito on 1/14/2016.
 */
public class TrainingModuleSubmission extends DataSubmissionAsync {

    public TrainingModuleSubmission(Context context){
        super(context);
    }

    @Override
    protected void onPostExecute(String result){
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }
}
