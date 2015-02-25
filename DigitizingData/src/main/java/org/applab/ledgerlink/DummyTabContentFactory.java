package org.applab.ledgerlink;

import android.content.Context;
import android.view.View;
import android.widget.TabHost.TabContentFactory;

class DummyTabContentFactory implements TabContentFactory{
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