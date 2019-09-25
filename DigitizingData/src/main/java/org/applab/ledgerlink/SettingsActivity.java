package org.applab.ledgerlink;

/**
 * Created by Moses on 3/20/14.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.DbBackupRestore;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_KEY_SERVER_URL = "prefServerUrl";
    public static final String PREF_KEY_HELP_LINE = "prefHelpLine";
    public static final String PREF_KEY_RUN_IN_TRAINING_MODE = "prefRunInTrainingMode";
    public static final String PREF_KEY_EXECUTION_MODE = "prefExecutionMode";
    public static final String PREF_KEY_REFRESH_TRAINING_DATA = "prefRefreshTrainingData";
    public static final String PREF_KEY_RESTORE_DATA = "prefRestoreData";
    public static final String PREF_KEY_BACKUP_DATA = "Backup Data";
    public static final String PREF_KEY_CONFIRM_TRAINING_MODE = "prefConfirmTrainingMode";
    public static final String PREF_KEY_TRAINING_PASSWORD = "prefTrainingPassword";
    public static final String PREF_KEY_TRAINING_OPTIONS_SECTION = "prefTrainingModeOptions";
    public static final String PREF_VALUE_EXECUTION_MODE_PROD = "1";
    public static final String PREF_VALUE_EXECUTION_MODE_TRAINING = "2";
    public static final String TITLE_EXECUTION_MODE_PROD = "Switch to Training Mode";
    public static final String TITLE_EXECUTION_MODE_TRAINING = "Switch to Actual VSLA Data";


    public android.support.v7.app.ActionBar actionBar;
    public DbBackupRestore dbBackupRestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        //actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        //assert actionBar != null;
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);


        addPreferencesFromResource(R.xml.preferences);
        refreshView();
    }

    // refreshes the view
    public void refreshView() {

        EditTextPreference prefServerUrl = (EditTextPreference) findPreference("prefServerUrl");
        prefServerUrl.setSummary(getString(R.string.set_internet_address_for_server_that_receive_data) + prefServerUrl.getText());

        EditTextPreference prefHelpLine = (EditTextPreference) findPreference("prefHelpLine");
        prefHelpLine.setSummary(getString(R.string.set_telephone_no_to_call_for_support) + prefHelpLine.getText());

        //If the user is in Production Mode and switches to Training Mode then update the title and summary accordingly
        ListPreference runInTrainingModePref = (ListPreference) findPreference(PREF_KEY_EXECUTION_MODE);

        String currentValue = runInTrainingModePref.getValue();
        if (currentValue.equalsIgnoreCase(PREF_VALUE_EXECUTION_MODE_PROD)) {
            runInTrainingModePref.setTitle(TITLE_EXECUTION_MODE_PROD);
            //runInTrainingModePref.setSummary("You are currently working on Actual VSLA Data. Switch to Training Data to learn how to use the application without destroying members' records.");
            runInTrainingModePref.setSummary(getString(R.string.currently_not_in_traininng_mode));
        } else {
            runInTrainingModePref.setTitle(TITLE_EXECUTION_MODE_TRAINING);
            runInTrainingModePref.setSummary(getString(R.string.training_mode_using_practice_data));
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        //  actionBar.setHomeButtonEnabled(true);

        refreshView();
        if (key.equals(PREF_KEY_REFRESH_TRAINING_DATA)) {
            //If the user is in Training Mode then Refresh the data immediately if value is TRUE
        } else if (key.equals(PREF_KEY_EXECUTION_MODE)) {
            restartApplication();

        }
        else if(key.equals(PREF_KEY_RESTORE_DATA)){
            //dbBackupRestore.exportDb();
            dbBackupRestore.importDb();

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
