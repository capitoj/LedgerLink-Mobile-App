package org.applab.digitizingdata;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.MenuCustomArrayAdapter;
import org.applab.digitizingdata.helpers.MenuItem;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.SampleDataBuilderRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/13/13.
 */
public class MainActivity extends SherlockActivity {

    GridView gridView;
    ArrayList<MenuItem> mainMenuItemsGridArray = new ArrayList<MenuItem>();
    CustomGridViewAdapter customGridAdapter;
    ArrayList<MenuItem> mainMenuItems = null;
    VslaInfo vslaInfo = null;
    VslaInfoRepo vslaInfoRepo = null;
    ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Ledger Link");

        //Retrieve VSLA Information
        vslaInfoRepo = new VslaInfoRepo(getApplicationContext());
        if (vslaInfoRepo != null) {
            vslaInfo = vslaInfoRepo.getVslaInfo();
        }

        //Display the main menu
        displayMainMenu();

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
        mainMenuItemsGridArray.add(new MenuItem("reviewMembers", "REVIEW & EDIT MEMBERS", R.drawable.review_members));
        mainMenuItemsGridArray.add(new MenuItem("updateCycle", "REVIEW & EDIT CYCLE", R.drawable.edit_cycle));
        mainMenuItemsGridArray.add(new MenuItem("endCycle", "END CYCLE", R.drawable.end_cycle));
        mainMenuItemsGridArray.add(new MenuItem("beginCycle", "BEGIN NEW CYCLE", R.drawable.begin_new_cycle));


        //Display the Data Migration Menu if data has not yet been migrated
        /** if (null != vslaInfo) {
         if (vslaInfo.isDataMigrated()) {
         //Hide the dataMigration Menu
         } else {
         //Show the Data Migration Menu
         mainMenuItems.add(new MenuItem("dataMigration", "Data Migration", R.drawable.));
         }
         }  */

        gridView = (GridView) findViewById(R.id.grid);

        customGridAdapter = new CustomGridViewAdapter(this, R.layout.mainmenurowgrid, mainMenuItemsGridArray);
        gridView.setAdapter(customGridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id) {
                MenuItem selectedMenu = mainMenuItemsGridArray.get(position);
                String selectedMenuName = selectedMenu.getMenuName();

                if (selectedMenuName.equalsIgnoreCase("beginMeeting")) {
                    Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("sendData")) {
                    Intent i = new Intent(getApplicationContext(), SendMeetingDataActivity.class);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("viewSentData")) {
                    Intent i = new Intent(getApplicationContext(), ViewSentDataActivity.class);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("updateCycle")) {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    i.putExtra("_isUpdateCycleAction", true);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("endCycle")) {
                    Intent i = new Intent(getApplicationContext(), EndCycleActivity.class);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("beginCycle")) {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("reviewMembers")) {
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    startActivity(i);
                } else if (selectedMenuName.equalsIgnoreCase("dataMigration")) {
                    Intent i = new Intent(getApplicationContext(), DataMigrationActivity.class);
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

        public CustomGridViewAdapter(Context context, int layoutResourceId,
                                     ArrayList<MenuItem> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            MenuItemHolder holder = null;
            if (row == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
                row.setLayoutParams(new LayoutParams((int)((Activity)context).getResources().getDimension(R.dimen.view_width), (int) ((Activity)context).getResources().getDimension(R.dimen.view_height)));

                holder = new MenuItemHolder();
                holder.imageItem = (ImageView) row.findViewById(R.id.item_image);


                row.setTag(holder);

            } else {
                holder = (MenuItemHolder) row.getTag();
            }


            MenuItem item = data.get(position);
            try {
                holder.imageItem.setImageBitmap(convertToBitMap(item.getMenuImage()));
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
                ex.getMessage().toString();

            }
            return row;
        }

        class MenuItemHolder {
            ImageView imageItem;

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
        switch (item.getItemId()) {
            case R.id.mnuMainSettings:

                // Launch preferences activity
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }
}