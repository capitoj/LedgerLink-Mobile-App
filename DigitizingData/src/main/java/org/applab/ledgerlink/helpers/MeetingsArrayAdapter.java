package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.R;

import java.util.ArrayList;




/**
 * Created by Moses on 3/11/14.
 */
class MeetingsArrayAdapter extends ArrayAdapter<Meeting> {
    private final Context context;
    private final ArrayList<Meeting> values;
    int position;

    public MeetingsArrayAdapter(Context context, ArrayList<Meeting> values) {
        super(context, R.layout.row_meeting, values);
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

            View rowView = inflater.inflate(R.layout.row_meeting, parent, false);

            //Get the Widgets
            final TextView txtMeetingDate = (TextView)rowView.findViewById(R.id.txtMeetingDate);

            //Assign Values to the Widgets
            String meetingDate = String.format("%s", Utils.formatDate(values.get(position).getMeetingDate()));
            txtMeetingDate.setText(meetingDate);

            return rowView;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}