package org.applab.ledgerlink.helpers.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Joseph Capito on 8/3/2015.
 */
public class DropDownAdapter extends ArrayAdapter<CharSequence>{

    String[] dropDownList;
    Context context;

    public DropDownAdapter(Context context, String[] dropDownList){
        super(context, android.R.layout.simple_spinner_item, dropDownList);
        this.context = context;
        this.dropDownList = dropDownList;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup){
        View itemView = super.getView(position, view, viewGroup);
        Typeface externalFont = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
        ((TextView) itemView).setTypeface(externalFont);
        return itemView;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup viewGroup){
        View itemView = super.getDropDownView(position, view, viewGroup);
        Typeface externalFont = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
        ((TextView) itemView).setTypeface(externalFont);
        return itemView;
    }

}
