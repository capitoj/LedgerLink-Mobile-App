package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.content.res.Resources;
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
 * Created by Moses on 7/25/13.
 */
public class AttendanceArrayAdapter extends ArrayAdapter<AttendanceRecord> {

    private final Context context;
    private final ArrayList<AttendanceRecord> values;
    int position;
    private final Typeface typeface;

    public AttendanceArrayAdapter(Context context, ArrayList<AttendanceRecord> values) {
        super(context, R.layout.row_attendance_history, values);
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

            rowView = inflater.inflate(R.layout.row_attendance_history, parent, false);

            // Get the Widgets
            TextView txtMeetingDate = (TextView)rowView.findViewById(R.id.txtRAHMeetingDate);
            TextView txtAttendance = (TextView)rowView.findViewById(R.id.txtRAHAttendance);
            TextView txtComments = (TextView)rowView.findViewById(R.id.txtRAHComments);

            // Set typeface
            txtMeetingDate.setTypeface(typeface);
            txtComments.setTypeface(typeface);
            txtAttendance.setTypeface(typeface);

            // Assign Values to the Widgets
            AttendanceRecord attendanceRecord = values.get(position);
            if(attendanceRecord != null) {
                if (attendanceRecord.getPresent()==0) {
                    txtComments.setText(attendanceRecord.getComment());
                    txtMeetingDate.setText(position + 1 + ". " + Utils.formatDate(attendanceRecord.getMeetingDate(), Utils.DATE_FIELD_FORMAT));
                    txtAttendance.setText(context.getResources().getString(R.string.absent));
                    // txtAttendance.setText((attendanceRecord.getPresent() == 1) ? "Present" : "Absent");
                }
            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
