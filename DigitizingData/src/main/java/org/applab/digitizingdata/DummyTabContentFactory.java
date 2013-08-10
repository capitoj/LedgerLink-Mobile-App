package org.applab.digitizingdata;

import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

public class DummyTabContentFactory implements TabContentFactory{
    private Context mContext;

    public DummyTabContentFactory(Context context){
        mContext = context;
    }

    @Override
    public View createTabContent(String tag) {
        View v = new View(mContext);
        return v;
    }
}