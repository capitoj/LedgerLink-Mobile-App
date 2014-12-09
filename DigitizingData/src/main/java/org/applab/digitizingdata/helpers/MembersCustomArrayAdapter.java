package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.Member;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MembersCustomArrayAdapter extends ArrayAdapter<Member> {
    private final Context context;
    private final ArrayList<Member> values;
    int position;
    private final Typeface typeface;

    public MembersCustomArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.memberlistrowlayout, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.memberlistrowlayout, parent, false);

            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtMFullNames);
            final TextView txtOccupation = (TextView)rowView.findViewById(R.id.txtMOccupation);

            // Set Typeface
            txtFullNames.setTypeface(typeface);
            txtOccupation.setTypeface(typeface);

            //Assign Values to the Widgets
            txtFullNames.setText(values.get(position).toString());
            String occupation = values.get(position).getOccupation();
            if(null == occupation || occupation.trim().length() <= 0) {
                occupation = "No Occupation";
            }
            String phoneNo = values.get(position).getPhoneNumber();
            if(null == phoneNo || phoneNo.trim().length() <= 0) {
                phoneNo = "No Phone";
            }
            //txtPhoneNo.setText(phoneNo);
            txtOccupation.setText(occupation);

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
