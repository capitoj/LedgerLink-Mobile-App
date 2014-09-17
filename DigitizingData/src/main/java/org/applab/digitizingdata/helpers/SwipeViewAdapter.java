package org.applab.digitizingdata.helpers;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.applab.digitizingdata.R;

public class SwipeViewAdapter extends ArrayAdapter {
    String[] countries;

    Context context;

    public SwipeViewAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.countries = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate the swipeview layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.swipelistview, parent, false);
        row.setBackgroundColor(context.getResources().getColor(R.color.ledger_link_white));

// Find the textview and the button defined in the layout
        TextView textView = (TextView) row.findViewById(R.id.textView);
        Button button = (Button) row.findViewById(R.id.button);

// Get the item from the array
        String country = countries[position];

// Set the country as the textview label
        textView.setText(country);
        return row;
    }
}
