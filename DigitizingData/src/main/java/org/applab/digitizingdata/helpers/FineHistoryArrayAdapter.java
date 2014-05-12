package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import org.applab.digitizingdata.R;

import java.util.ArrayList;

/**
 * Created by Moses on 8/2/13.
 */
public class FineHistoryArrayAdapter extends ArrayAdapter<MemberFineRecord> {

    Context context;
    ArrayList<MemberFineRecord> values;
    int position;
    Typeface typeface;

    public FineHistoryArrayAdapter(Context context, ArrayList<MemberFineRecord> values, String font) {
        super(context, R.layout.row_fines_history, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), font);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_fines_history, parent, false);

            CheckBox chkMemberFine = (CheckBox) rowView.findViewById(R.id.chkMemberFine);
            TextView lblFineMeetingDate = (TextView) rowView.findViewById(R.id.lblFineMeetingDate);
            TextView lblFineType = (TextView) rowView.findViewById(R.id.lblFineType);
            TextView lblFineAmount = (TextView) rowView.findViewById(R.id.txtMemberFineAmount);

            // Set typeface
            lblFineMeetingDate.setTypeface(typeface);
            lblFineAmount.setTypeface(typeface);
            lblFineType.setTypeface(typeface);

            //Assign Values to the Widgets
            MemberFineRecord fineRecord = values.get(position);
            if(fineRecord != null) {
                lblFineMeetingDate.setText(String.format("Date: %s", Utils.formatDate(fineRecord.getMeetingDate(), Utils.DATE_FIELD_FORMAT)));
                lblFineAmount.setText(String.format("%,.0fUGX", fineRecord.getAmount()));
                lblFineType.setText("Change This Text");
                // if(fineRecord.getFineStatus()!=0){
                chkMemberFine.setChecked(fineRecord.getStatus() != 0);
            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}


