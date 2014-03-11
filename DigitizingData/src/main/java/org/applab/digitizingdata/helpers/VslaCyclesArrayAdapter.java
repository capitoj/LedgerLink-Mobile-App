package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.VslaCycle;

import java.util.ArrayList;

/**
 * Created by Moses on 3/11/14.
 */
public class VslaCyclesArrayAdapter extends ArrayAdapter<VslaCycle> {
    Context context;
    ArrayList<VslaCycle> values;
    int position;

    public VslaCyclesArrayAdapter(Context context, ArrayList<VslaCycle> values) {
        super(context, R.layout.row_vsla_cycle, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_vsla_cycle, parent, false);

            //Get the Widgets
            final TextView txtCycleStartEndDate = (TextView)rowView.findViewById(R.id.txtRVCStartEndDate);

            //Assign Values to the Widgets
            String cycleDates = String.format("%s - %s", Utils.formatDate(values.get(position).getStartDate()),
                    Utils.formatDate(values.get(position).getEndDate()));
            txtCycleStartEndDate.setText(cycleDates);

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}