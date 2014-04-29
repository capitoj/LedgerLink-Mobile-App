package org.applab.digitizingdata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.MenuItem;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.util.ArrayList;

/**
 *
 *
 */
public class MainMenuActivity extends SherlockActivity {

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
        //gridLayout = (GridLayout) findViewById(R.id.grid);
        Log.d("HERE", String.valueOf(mainMenuItemsGridArray.size()));

        customGridAdapter = new CustomGridViewAdapter(this, R.layout.mainmenurowgrid, mainMenuItemsGridArray);
        gridView.setAdapter(customGridAdapter);

        gridView.getCount();

        gridView.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id)
            {
                Toast.makeText(getBaseContext(),
                        "pic" + (position + 1) +" selected",
                        Toast.LENGTH_SHORT).show();
                MenuItem selectedMenu = mainMenuItems.get(position);
                String selectedMenuName = selectedMenu.getMenuName();

                if(selectedMenuName.equalsIgnoreCase("beginMeeting")) {
                    Intent i = new Intent(getApplicationContext(), BeginMeetingActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("sendData")) {
                    Intent i = new Intent(getApplicationContext(), SendMeetingDataActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("viewSentData")) {
                    Intent i = new Intent(getApplicationContext(), ViewSentDataActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("updateCycle")) {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    i.putExtra("_isUpdateCycleAction", true);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("endCycle")) {
                    Intent i = new Intent(getApplicationContext(), EndCycleActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("beginCycle")) {
                    Intent i = new Intent(getApplicationContext(), NewCycleActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("reviewMembers")) {
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("dataMigration")) {
                    Intent i = new Intent(getApplicationContext(), DataMigrationActivity.class);
                    startActivity(i);
                }
                else if(selectedMenuName.equalsIgnoreCase("help")) {

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

                holder = new MenuItemHolder();
               // holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
                holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
                row.setTag(holder);

            } else {
                holder = (MenuItemHolder) row.getTag();
            }


            MenuItem item = data.get(position);
            //holder.txtTitle.setText(item.getMenuCaption());
            // holder.imageItem.setImageBitmap(convertToBitMap(item.getMenuImage()));

            Drawable draw = getResources().getDrawable(item.getMenuImage());
            holder.imageItem.setImageDrawable(draw);

            //holder.imageItem.setImageBitmap(item.getImageResource());
        /**    if ((item.getMenuCaption() == "Meeting") || (item.getMenuCaption() == "Sent Data")) {
                row.setBackgroundResource(R.drawable.gradient_light_blue_start);
            }
            if ((item.getMenuCaption() == "Review & Edit Members") || (item.getMenuCaption() == "Review & Edit Cycle")) {
                row.setBackgroundResource(R.drawable.gradient_light_blue_mid);
            }
            if ((item.getMenuCaption() == "End Cycle") || (item.getMenuCaption() == "Begin New Cycle")) {
                row.setBackgroundResource(R.drawable.light_blue_bottom_left_background);
            }
         */

            return row;
        }

        class MenuItemHolder {
            TextView txtTitle;
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
