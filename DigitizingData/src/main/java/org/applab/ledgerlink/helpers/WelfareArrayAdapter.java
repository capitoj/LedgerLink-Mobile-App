package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.R;

import java.util.ArrayList;

/**
 * Created by JCapito on 9/27/2018.
 */

public class WelfareArrayAdapter extends ArrayAdapter<MemberWelfareRecord> {

    private final Context context;
    private final ArrayList<MemberWelfareRecord> values;
    int position;
    private final Typeface typeface;

    public WelfareArrayAdapter(Context context, ArrayList<MemberWelfareRecord> values){
        super(context, R.layout.row_welfare_history, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_welfare_history, parent, false);

            //Get the Widgets
            TextView txtMeetingDate = (TextView)rowView.findViewById(R.id.txtRWHMeetingDate);
            TextView txtAmount = (TextView)rowView.findViewById(R.id.txtRWHAmount);

            // Set typeface
            txtMeetingDate.setTypeface(typeface);
            txtAmount.setTypeface(typeface);

            //Assign Values to the Widgets
            MemberWelfareRecord welfareRecord = values.get(position);
            if(welfareRecord != null) {
                txtMeetingDate.setText(Utils.formatDate(welfareRecord.getMeetingDate(),Utils.DATE_FIELD_FORMAT));
                txtAmount.setText(String.format("\t%,.0f UGX", welfareRecord.getAmount()));
            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
