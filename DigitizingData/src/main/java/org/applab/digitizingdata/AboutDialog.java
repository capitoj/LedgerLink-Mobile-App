package org.applab.digitizingdata;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: John Mark
 * Date: 5/14/14
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class AboutDialog extends Dialog {
    private static Context mContext = null;
    public AboutDialog(Context context) {
        super(context);
        mContext = context;


}
    @Override
 public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.about);
        TextView tv = (TextView)findViewById(R.id.legal_text);
        tv.setText(getContext().getResources().getString(R.string.about_legal));

        StringBuilder aboutBuilder = new StringBuilder();
        aboutBuilder.append("<h3>LedgerLink</h3>");
        aboutBuilder.append(getContext().getResources().getString(R.string.about_version)+"<br>" +
                "Copyright 2014<br><b>www.applab.org</b><br><br>");

        tv = (TextView)findViewById(R.id.info_text);
        tv.setText(Html.fromHtml(aboutBuilder.toString()));



        tv.setLinkTextColor(Color.WHITE);
        Linkify.addLinks(tv, Linkify.ALL);


    }
}