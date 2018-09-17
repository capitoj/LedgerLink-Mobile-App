package org.applab.ledgerlink;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.MenuItem;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.utils.Connection;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moses on 6/13/13.
 * Modified by Joseph Capito 10/12/2015
 */
public class MainActivity extends SherlockActivity {

    private final ArrayList<MenuItem> mainMenuItemsGridArray = new ArrayList<MenuItem>();
    private CustomGridViewAdapter customGridAdapter;
    ArrayList<MenuItem> mainMenuItems = null;
    private VslaInfo vslaInfo = null;
    private ActionBar actionBar;
    LedgerLinkApplication ledgerLinkApplication;
    private Utils.Size size;
    protected Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        setContentView(R.layout.main_menu);

        this.context = this;

        actionBar = getSupportActionBar();
        actionBar.setTitle("Ledger Link");

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
        List<Meeting> pastMeetings = meetingRepo.getPastMeetings();
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


        //set grid view item
        mainMenuItemsGridArray.add(new MenuItem("beginMeeting", "MEETING", R.drawable.meeting));
        mainMenuItemsGridArray.add(new MenuItem("viewSentData", "SENT DATA", R.drawable.sent_data));
        mainMenuItemsGridArray.add(new MenuItem("reviewMembers", "EDIT MEMBERS", R.drawable.members));
        mainMenuItemsGridArray.add(new MenuItem("updateCycle", "EDIT CYCLE", R.drawable.edit_cycle));
        mainMenuItemsGridArray.add(new MenuItem("endCycle", "END CYCLE", R.drawable.end_cycle));
        mainMenuItemsGridArray.add(new MenuItem("beginCycle", "BEGIN NEW CYCLE", R.drawable.new_cycle));


        //Display the Data Migration Menu if data has not yet been migrated
        /** if (null != vslaInfo) {
         if (vslaInfo.isDataMigrated()) {
         //Hide the dataMigration Menu
         } else {
         //Show the Data Migration Menu
         mainMenuItems.add(new MenuItem("dataMigration", "Data Migration", R.drawable.));
         }
         }  */

        GridView gridView = (GridView) findViewById(R.id.grid);

        customGridAdapter = new CustomGridViewAdapter(this, mainMenuItemsGridArray, size);
        gridView.setAdapter(customGridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id) {
                MenuItem selectedMenu = mainMenuItemsGridArray.get(position);
                String selectedMenuName = selectedMenu.getMenuName();

                if (selectedMenuName.equalsIgnoreCase("beginMeeting")) {
                    Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("sendData")) {
                    Intent i = new Intent(getApplicationContext(), SendMeetingDataActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("viewSentData")) {
                    Intent i = new Intent(getApplicationContext(), ViewSentDataActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("updateCycle")) {
                    //Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    //i.putExtra("_isUpdateCycleAction", true);
                    //For multiple active cycles, show activity to allow selecting
                    Intent i = new Intent(getApplicationContext(), SelectCycle.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("_isEndCycleAction", false);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("endCycle")) {
                    //Intent i = new Intent(getApplicationContext(), EndCycleActivity.class);
                    Intent i = new Intent(getApplicationContext(), SelectCycle.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("_isEndCycleAction", true);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("beginCycle")) {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("reviewMembers")) {
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("dataMigration")) {
                    Intent i = new Intent(getApplicationContext(), DataMigrationActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("help")) {

                }
            }
        });

    }

    private Bitmap convertToBitMap(int imageResource) {
        return BitmapFactory.decodeResource(this.getResources(), imageResource);
    }

    public class CustomGridViewAdapter extends ArrayAdapter<MenuItem> {
        Context context;
        int layoutResourceId;
        ArrayList<MenuItem> data = new ArrayList<MenuItem>();
        private Utils.Size size;

        public CustomGridViewAdapter(Context context, ArrayList<MenuItem> data, Utils.Size size) {
            super(context, R.layout.mainmenurowgrid, data);
            this.layoutResourceId = R.layout.mainmenurowgrid;
            this.context = context;
            this.data = data;
            this.size = size;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            MenuItemHolder holder = null;
            if (row == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                row.setLayoutParams(new LayoutParams(this.size.getWidth()/2, this.size.getHeight()/3));
                //row.setLayoutParams(new LayoutParams((int)((Activity)context).getResources().getDimension(R.dimen.view_width), (int) ((Activity)context).getResources().getDimension(R.dimen.view_height)));

                holder = new MenuItemHolder();
                holder.textDescription = (TextView)row.findViewById(R.id.menu_item_desc);
                holder.imageItem = (ImageView) row.findViewById(R.id.item_image);

                row.setTag(holder);

            } else {
                holder = (MenuItemHolder) row.getTag();
            }

            MenuItem item = data.get(position);
            try {
                holder.imageItem.setImageBitmap(convertToBitMap(item.getMenuImage()));
                holder.textDescription.setText(item.getMenuCaption());
                if(item.getMenuName().equalsIgnoreCase("beginMeeting")){
                    row.setBackgroundColor(((Activity)context).getResources().getColor(R.color.light_blue_top_left));
                }
                if(item.getMenuName().equalsIgnoreCase("viewSentData")){
                    row.setBackgroundColor(((Activity)context).getResources().getColor(R.color.light_blue_top_right));
                }
                if(item.getMenuName().equalsIgnoreCase("reviewMembers")){
                    row.setBackgroundColor(((Activity)context).getResources().getColor(R.color.light_blue_mid_top_left));
                }
                if(item.getMenuName().equalsIgnoreCase("updateCycle")){
                    row.setBackgroundColor(((Activity)context).getResources().getColor(R.color.light_blue_mid_top_right));
                }
                if(item.getMenuName().equalsIgnoreCase("endCycle")){
                    row.setBackgroundColor(((Activity)context).getResources().getColor(R.color.light_blue_mid_bottom_left));
                }
                if(item.getMenuName().equalsIgnoreCase("beginCycle")){
                    row.setBackgroundColor(((Activity)context).getResources().getColor(R.color.light_blue_mid_bottom_right));
                }
            } catch (Exception ex) {
                Log.e("MainActivity.getView ", ex.getMessage().toString());


            }
            return row;
        }

        class MenuItemHolder {
            ImageView imageItem;
            TextView textDescription;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.mnuMainSettings:

                // Launch preferences activity
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
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
        }
        return true;
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
            DialogMessageBox.show(context, "Connection Alert", "An internet connection could not be detected. Retrieval of the VSLA Profile will require an internet connection");
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