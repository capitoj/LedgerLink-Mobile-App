package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by Joseph Capito on 9/7/2015.
 */
public class CustomSpinnerListener implements AdapterView.OnItemSelectedListener {

    protected Context context;
    public CustomSpinnerListener(Context context){
        this.context = context;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        //Toast.makeText(this.context, String.valueOf(position), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }
}

