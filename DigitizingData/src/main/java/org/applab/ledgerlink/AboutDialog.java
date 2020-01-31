package org.applab.ledgerlink;

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
    public AboutDialog(Context context) {
        super(context);
        Context mContext = context;


}
    @Override
 public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.about);
        TextView tv = (TextView)findViewById(R.id.legal_text);
        tv.setText(getContext().getResources().getString(R.string.about_legal));

        tv = (TextView)findViewById(R.id.info_text);
        tv.setText(Html.fromHtml("<h3>LedgerLink</h3>" + getContext().getResources().getString(R.string.about_version) + "<br>" +
                "Copyright 2014<br><b>www.applab.org</b><br><br>"));
        tv.setLinkTextColor(Color.BLUE);
        Linkify.addLinks(tv, Linkify.ALL);


    }
}