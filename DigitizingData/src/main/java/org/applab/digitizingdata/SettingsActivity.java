package org.applab.digitizingdata;

/**
 * Created by Moses on 3/20/14.
 */
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.webkit.URLUtil;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_KEY_SERVER_URL = "prefServerUrl";
    public static final String PREF_KEY_HELP_LINE = "prefHelpLine";
    public static final String PREF_KEY_TRAINING_MODE = "prefRunInTrainingMode";
    public static final String PREF_KEY_EXECUTION_MODE = "prefExecutionMode";
    public static final String PREF_KEY_REFRESH_TRAINING_DATA = "prefRefreshTrainingData";
    public static final String PREF_KEY_CONFIRM_TRAINING_MODE = "prefConfirmTrainingMode";
    public static final String PREF_KEY_TRAINING_PASSWORD = "prefTrainingPassword";
    public static final String PREF_KEY_TRAINING_OPTIONS_SECTION = "prefTrainingModeOptions";
    public static final String PREF_EXECUTION_MODE_PROD = "1";
    public static final String PREF_EXECUTION_MODE_TRAINING = "2";
    public static final String TITLE_EXECUTION_MODE_PROD = "Switch to Training Mode";
    public static final String TITLE_EXECUTION_MODE_TRAINING = "Switch to Actual VSLA Data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_KEY_REFRESH_TRAINING_DATA)) {
            //If the user is in Training Mode then Refresh the data immediately if value is TRUE

        }
        else if (key.equals(PREF_KEY_EXECUTION_MODE)) {
            //If the user is in Production Mode and switches to Training Mode then update the title and summary accordingly
            Preference runInTrainingModePref = findPreference(key);

            String currentValue = sharedPreferences.getString(key, "0");
            if(currentValue.equalsIgnoreCase(PREF_EXECUTION_MODE_PROD)) {
                runInTrainingModePref.setTitle(TITLE_EXECUTION_MODE_PROD);
                runInTrainingModePref.setSummary("You are currently working on Actual VSLA Data. Switch to Training Data to learn how to use the application without destroying members' records.");
            }
            else {
                runInTrainingModePref.setTitle(TITLE_EXECUTION_MODE_TRAINING);
                runInTrainingModePref.setSummary("You are currently working on Training Data. Be sure to switch back to Actual VSLA Data to continue capturing meeting data.");
            }
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
}
