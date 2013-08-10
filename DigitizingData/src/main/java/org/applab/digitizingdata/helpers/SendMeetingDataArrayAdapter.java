package org.applab.digitizingdata.helpers;

/**
 * Created by Moses on 6/22/13.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.Meeting;

import java.util.ArrayList;


public class SendMeetingDataArrayAdapter<T> extends ArrayAdapter<Meeting> {
    Context context;
    ArrayList<Meeting> values;
    int position;
    boolean itemSelected[] = { false, false, false, false, false, false };

    public SendMeetingDataArrayAdapter(Context context, ArrayList<Meeting> values) {
        super(context, R.layout.row_send_meeting_data, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {


            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_send_meeting_data, parent, false);

            //Get the Widgets
            final TextView txtSMDMeetingDate = (TextView)rowView.findViewById(R.id.txtSMDMeetingDate);

            //Assign Values to the Widgets
            txtSMDMeetingDate.setText(Utils.formatDate(values.get(position).getMeetingDate(),"dd-MMM-yyyy"));

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
