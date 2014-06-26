package org.applab.digitizingdata.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.FineType;

import java.util.ArrayList;

/**
 * Created by Moses on 7/28/13.
 */
public class FineTypeCustomArrayAdapter extends ArrayAdapter<FineType> {
    Context context;
    ArrayList<FineType> values;
    int position;
    Typeface typeface;
    private Activity activity;
    FineType tempValues = null;

    public FineTypeCustomArrayAdapter(Context context, int viewResourceId, ArrayList<FineType> values, String font) {
        super(context, viewResourceId, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), font);

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // This funtion called for each row ( Called data.size() times )
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        //LayoutInflater inflater = activity.getLayoutInflater();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.fine_type_spinner_row, parent, false);

        /***** Get each Model object from Arraylist ********/
        tempValues = null;
        tempValues = values.get(position);

        final TextView label = (TextView) row.findViewById(R.id.lblFineType);
        label.setTypeface(typeface);
        /** if(position==0){
         // Default selected Spinner item
         label.setText("Please select FineType");
         }
         else
         { */
        // Set values for spinner each row
        label.setText(tempValues.getFineTypeName());
        //}

        return row;
    }
}