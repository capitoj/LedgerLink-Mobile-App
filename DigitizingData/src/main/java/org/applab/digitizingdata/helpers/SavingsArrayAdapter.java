package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import org.applab.digitizingdata.R;

/**
 * Created by Moses on 7/28/13.
 */
public class SavingsArrayAdapter extends ArrayAdapter<MemberSavingRecord> {
    private final Context context;
    private final ArrayList<MemberSavingRecord> values;
    int position;
    private final Typeface typeface;

    public SavingsArrayAdapter(Context context, ArrayList<MemberSavingRecord> values) {
        super(context, R.layout.row_savings_history, values);
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

            rowView = inflater.inflate(R.layout.row_savings_history, parent, false);

            //Get the Widgets
            TextView txtMeetingDate = (TextView)rowView.findViewById(R.id.txtRSHMeetingDate);
            TextView txtAmount = (TextView)rowView.findViewById(R.id.txtRSHAmount);

			// Set typeface
			txtMeetingDate.setTypeface(typeface);
			txtAmount.setTypeface(typeface);
			
            //Assign Values to the Widgets
            MemberSavingRecord savingRecord = values.get(position);
            if(savingRecord != null) {
                txtMeetingDate.setText(Utils.formatDate(savingRecord.getMeetingDate(),Utils.DATE_FIELD_FORMAT));
                txtAmount.setText(String.format("\t%,.0f UGX", savingRecord.getAmount()));
            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}

