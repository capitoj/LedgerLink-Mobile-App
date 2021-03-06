package org.applab.ledgerlink;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private VslaInfo vslaInfo = null;
    private ActionBar actionBar;
    LedgerLinkApplication ledgerLinkApplication;
    private Utils.Size size;
    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        setContentView(R.layout.activity_dashboard);

        this.context = this;

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.ledger_link);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        //Retrieve VSLA Information
        vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();

        this.getScreenSize();

        //Display the main menu
        displayMainMenu();
        this.showNotificationForUnsentMeetings();
    }

    protected void showNotificationForUnsentMeetings(){
        MeetingRepo meetingRepo = new MeetingRepo(this);
        VslaCycle recentCycle = new VslaCycleRepo(getApplicationContext()).getMostRecentCycle();
        List<Meeting> pastMeetings = meetingRepo.getPastMeetings(recentCycle.getCycleId());
        if(pastMeetings.size() > 0){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            };
            //DialogMessageBox.show(this, "Alert", "You have " + String.valueOf(pastMeetings.size()) + " unsent meetings on your phone", runnable, true);
        }

    }

    private void getScreenSize(){
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int actionBarHeight = Build.VERSION.SDK_INT >= 16 ? TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics()) + 38 : TypedValue.complexToDimensionPixelSize(typedValue.data, getResources().getDisplayMetrics()) + 19;
        this.size = new Utils.Size(metrics.widthPixels, metrics.heightPixels - actionBarHeight);
    }

    @Override
    public void onResume() {
        super.onResume();

        //displayMainMenu();
    }

    private void displayMainMenu() {

        final ArrayList<VslaCycle> activeCycles = ledgerLinkApplication.getVslaCycleRepo().getActiveCycles();

        ImageView meeting = (ImageView) findViewById(R.id.meeting);
        meeting.setImageResource(R.drawable.app_icons_meeting);
        meeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(activeCycles.size() > 0)) {
                    DialogMessageBox.show(MainActivity.this, "Create a new cycle", "Please a new cycle to access this function");
                    return;
                }
                Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        ImageView cycle = (ImageView) findViewById(R.id.cycle);
        cycle.setImageResource(R.drawable.app_icons_cycle);
        cycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SelectCycleActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("_isEndCycleAction", false);
                startActivity(i);
            }
        });

        ImageView members = (ImageView) findViewById(R.id.members);
        members.setImageResource(R.drawable.app_icons_members);
        members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        ImageView shareout = (ImageView) findViewById(R.id.shareout);
        shareout.setImageResource(R.drawable.app_icons_share_out);
        shareout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(activeCycles.size() > 0)) {
                    DialogMessageBox.show(MainActivity.this, "Create a new cycle", "Please a new cycle to access this function");
                    return;
                }
                Intent i = new Intent(getApplicationContext(), ShareOutActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

//        ImageView newCycle = (ImageView) findViewById(R.id.newCycle);
//        newCycle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//            }
//        });

        ImageView sentData = (ImageView) findViewById(R.id.sent_data);
        sentData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ViewSentDataActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

//        ImageView endCycle = (ImageView) findViewById(R.id.endCycle);
//        endCycle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(getApplicationContext(), EndCycleActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                i.putExtra("_isEndCycleAction", true);
//                startActivity(i);
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.mnuMainSettings:
                // Launch preferences activity
                Intent setting = new Intent(this, SettingsActivity.class);
                startActivity(setting);
                break;
            case R.id.mnuMainProfile:
                loadVslaProfile();
                break;
            case R.id.mnuMainChat:
                loadChatWindow();
                break;
            case R.id.mnuMainMOD:
                loadMODWindow();
                break;
            case R.id.mnuChangePIN:
                Intent pin = new Intent(this, PassKeyResetActivity.class);
                startActivity(pin);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();

    }

    protected void loadMODWindow(){
        Intent intent = new Intent(context, MODActivity.class);
        startActivity(intent);
    }

    protected void loadChatWindow(){
        Intent intent = new Intent(context, ChatActivity.class);
        startActivity(intent);
    }

    protected void loadVslaProfile(){
        if(Connection.isNetworkConnected(context)) {
            Intent intent = new Intent(context, ProfileActivity.class);
            startActivity(intent);
        }else{
            DialogMessageBox.show(context, getString(R.string.connection_alert), getString(R.string.internet_connection_not_detected_vsla_profile_required_internet_connection));
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}


