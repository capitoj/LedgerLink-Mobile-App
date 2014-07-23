package org.applab.digitizingdata;

/**
 * Created by Moses on 3/20/14.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;

import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_KEY_SERVER_URL = "prefServerUrl";
    public static final String PREF_KEY_HELP_LINE = "prefHelpLine";
    public static final String PREF_KEY_RUN_IN_TRAINING_MODE = "prefRunInTrainingMode";
    public static final String PREF_KEY_EXECUTION_MODE = "prefExecutionMode";
    public static final String PREF_KEY_REFRESH_TRAINING_DATA = "prefRefreshTrainingData";
    public static final String PREF_KEY_CONFIRM_TRAINING_MODE = "prefConfirmTrainingMode";
    public static final String PREF_KEY_TRAINING_PASSWORD = "prefTrainingPassword";
    public static final String PREF_KEY_TRAINING_OPTIONS_SECTION = "prefTrainingModeOptions";
    public static final String PREF_VALUE_EXECUTION_MODE_PROD = "1";
    public static final String PREF_VALUE_EXECUTION_MODE_TRAINING = "2";
    public static final String TITLE_EXECUTION_MODE_PROD = "Switch to Training Mode";
    public static final String TITLE_EXECUTION_MODE_TRAINING = "Switch to Actual VSLA Data";

    public ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);


        addPreferencesFromResource(R.xml.preferences);
        refreshView();
    }

    // refreshes the view
    public void refreshView() {

        EditTextPreference prefServerUrl = (EditTextPreference) findPreference("prefServerUrl");
        prefServerUrl.setSummary("Set Internet address for the server that will receive data\n" + prefServerUrl.getText());

        EditTextPreference prefHelpLine = (EditTextPreference) findPreference("prefHelpLine");
        prefHelpLine.setSummary("Set telephone number to call for support\n" + prefHelpLine.getText());

        //If the user is in Production Mode and switches to Training Mode then update the title and summary accordingly
        ListPreference runInTrainingModePref = (ListPreference) findPreference(PREF_KEY_EXECUTION_MODE);

        String currentValue = runInTrainingModePref.getValue();
        if (currentValue.equalsIgnoreCase(PREF_VALUE_EXECUTION_MODE_PROD)) {
            runInTrainingModePref.setTitle(TITLE_EXECUTION_MODE_PROD);
            //runInTrainingModePref.setSummary("You are currently working on Actual VSLA Data. Switch to Training Data to learn how to use the application without destroying members' records.");
            runInTrainingModePref.setSummary("You are currently NOT in Training Mode. \nAny cycle or meeting information you record will affect the data for your group members. \nTraining Mode is a way to safely practice and explore Ledger Link without affecting your group's data. \nTap to switch to Training Mode to use practice data without affecting your group's data.");
        } else {
            runInTrainingModePref.setTitle(TITLE_EXECUTION_MODE_TRAINING);
            runInTrainingModePref.setSummary("You are in Training Mode using practice data. \nAny data you enter will not affect your group's data");
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
      //  actionBar.setHomeButtonEnabled(true);

        refreshView();
        if (key.equals(PREF_KEY_REFRESH_TRAINING_DATA)) {
            //If the user is in Training Mode then Refresh the data immediately if value is TRUE
        } else if (key.equals(PREF_KEY_EXECUTION_MODE)) {
            restartApplication();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void restartApplication() {

        if (null != getApplicationContext()) {

            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(
                    getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

}
