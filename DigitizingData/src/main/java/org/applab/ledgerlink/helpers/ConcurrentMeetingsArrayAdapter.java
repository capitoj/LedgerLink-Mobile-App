package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.Meeting;

import java.util.ArrayList;


/**
 * Created by Moses on 3/11/14.
 */
public class ConcurrentMeetingsArrayAdapter extends ArrayAdapter<Meeting> {
    private final Context context;
    private final ArrayList<Meeting> values;
    int position;

    public ConcurrentMeetingsArrayAdapter(Context context, ArrayList<Meeting> values) {
        super(context, R.layout.row_meeting_with_cycle, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.row_meeting_with_cycle, parent, false);
        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment


            //Get the Widgets
            final TextView txtMeetingDate = (TextView) rowView.findViewById(R.id.txtMeetingDate);
            final TextView cycleInformationTextView = (TextView) rowView.findViewById(R.id.txtMeetingCycleSummary);

            //Assign Values to the Widgets
            String meetingDate = "";
            if (values.get(position).isGettingStarted()) {
                meetingDate = String.format("%s "+context.getResources().getString(R.string.migration), Utils.formatDate(values.get(position).getMeetingDate(), "dd MMM yyyy"));

            } else {
                meetingDate = String.format("%s ", Utils.formatDate(values.get(position).getMeetingDate(), "dd MMM yyyy"));
            }
            txtMeetingDate.setText(meetingDate);


            String cycleSummary = String.format("Cycle %s - %s", Utils.formatDate(values.get(position).getVslaCycle().getStartDate(), "dd MMM yyyy"), Utils.formatDate(values.get(position).getVslaCycle().getEndDate(), "dd MMM yyyy"));
            cycleInformationTextView.setText(cycleSummary);

            return rowView;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}