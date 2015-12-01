package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by JCapito on 9/9/2015.
 */
public class CustomItemSelectedListener implements AdapterView.OnItemSelectedListener {

    protected Context context;

    public CustomItemSelectedListener(Context context){
        this.context = context;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        parent.setFocusableInTouchMode(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }
}
