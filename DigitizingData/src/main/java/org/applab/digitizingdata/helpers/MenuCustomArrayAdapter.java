package org.applab.digitizingdata.helpers;

/**
 * Created by Moses on 6/22/13.
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.applab.digitizingdata.R;

import java.util.ArrayList;


public class MenuCustomArrayAdapter<T> extends ArrayAdapter<MenuItem> {
    Context context;
    ArrayList<MenuItem> values;
    int position;
    boolean itemSelected[] = { false, false, false, false, false, false };

    public MenuCustomArrayAdapter(Context context, ArrayList<MenuItem> values) {
        super(context, R.layout.mainmenurowlayout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {


            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.mainmenurowlayout, parent, false);

            //Get the Widgets
            final TextView txtMenuName = (TextView)rowView.findViewById(R.id.txtMenuName);
            final ImageView imgMenuIcon = (ImageView)rowView.findViewById(R.id.imgMenuIcon);



            //Assign Values to the Widgets
            txtMenuName.setText(values.get(position).getMenuCaption());
            imgMenuIcon.setImageResource(R.drawable.view_sent_24);

            String selectedMenuName = values.get(position).getMenuName();

            if(selectedMenuName.equalsIgnoreCase("beginMeeting")) {
                imgMenuIcon.setImageResource(R.drawable.begin_meeting_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("sendData")) {
                imgMenuIcon.setImageResource(R.drawable.send_data_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("viewSentData")) {
                imgMenuIcon.setImageResource(R.drawable.view_sent_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("updateCycle")) {
                imgMenuIcon.setImageResource(R.drawable.edit_cycle_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("endCycle")) {
                imgMenuIcon.setImageResource(R.drawable.end_cycle_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("beginCycle")) {
                imgMenuIcon.setImageResource(R.drawable.new_cycle_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("reviewMembers")) {
                imgMenuIcon.setImageResource(R.drawable.members_24);
            }
            else if(selectedMenuName.equalsIgnoreCase("help")) {

            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
