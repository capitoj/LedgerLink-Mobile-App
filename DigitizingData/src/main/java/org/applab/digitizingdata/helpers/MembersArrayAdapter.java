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
public class MembersArrayAdapter extends ArrayAdapter<Member> {
    Context context;
    ArrayList<Member> values;
    int position;
    Typeface typeface;

    public MembersArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_members_main_list, values);
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

            View rowView = inflater.inflate(R.layout.row_members_main_list, parent, false);

            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtMListFullName);
            final TextView txtSavings = (TextView)rowView.findViewById(R.id.txtMListTotalSavings);
            //final TextView txtLoans = (TextView)rowView.findViewById(R.id.txtMListTotalLoans);

            // Set Typeface
            txtFullNames.setTypeface(typeface);
            txtSavings.setTypeface(typeface);

            //Assign Values to the Widgets
            Member memb = values.get(position);
            if(memb != null) {
                txtFullNames.setText(memb.toString());

                String phoneNo = memb.getPhoneNumber();
                if(null == phoneNo || phoneNo.trim().length() <= 0) {
                    phoneNo = "No Phone";
                }

                String occupation = memb.getOccupation();
                if(null == occupation || occupation.trim().length() <= 0) {
                    occupation = "No Occupation";
                }

                //txtSavings.setText(phoneNo);
                txtSavings.setText(occupation);
                //txtLoans.setText(memb.getOccupation());
            }
            else {
                txtFullNames.setText("");
                txtSavings.setText("");
                //txtLoans.setText("");
            }

            return rowView;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
