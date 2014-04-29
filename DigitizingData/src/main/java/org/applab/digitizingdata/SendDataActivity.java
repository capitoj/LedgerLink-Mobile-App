package org.applab.digitizingdata;

import android.app.Activity;
import android.os.Bundle;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;

/**
 * Created by Moses on 7/13/13.
 */
public class SendDataActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

    }
}