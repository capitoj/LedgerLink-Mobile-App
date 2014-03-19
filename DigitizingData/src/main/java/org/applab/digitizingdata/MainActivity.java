package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.helpers.MenuCustomArrayAdapter;
import org.applab.digitizingdata.helpers.MenuItem;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/13/13.
 */
public class MainActivity extends SherlockActivity {
    ArrayList<MenuItem> mainMenuItems = null;
    VslaInfo vslaInfo = null;
    VslaInfoRepo vslaInfoRepo = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Retrieve VSLA Information
        vslaInfoRepo = new VslaInfoRepo(getApplicationContext());
        if(vslaInfoRepo != null) {
            vslaInfo = vslaInfoRepo.getVslaInfo();
        }

        //Display the main menu
        displayMainMenu();

    }

    @Override
    public void onResume() {
        super.onResume();

        displayMainMenu();
    }

    private void displayMainMenu() {
        //Load the Main Menu
        mainMenuItems = new ArrayList<MenuItem>();
        mainMenuItems.add(new MenuItem("beginMeeting", "Meeting"));
        mainMenuItems.add(new MenuItem("sendData", "Check & Send Data"));
        mainMenuItems.add(new MenuItem("viewSentData", "Sent Data"));
        mainMenuItems.add(new MenuItem("updateCycle", "Review & Edit Cycle"));
        mainMenuItems.add(new MenuItem("endCycle", "End Cycle"));
        mainMenuItems.add(new MenuItem("beginCycle", "Begin New Cycle"));
        mainMenuItems.add(new MenuItem("reviewMembers", "Review & Edit Members"));

        //Display the Data Migration Menu if data has not yet been migrated
        if(null != vslaInfo) {
            if(vslaInfo.isDataMigrated()) {
                //Hide the dataMigration Menu
            }
            else{
                //Show the Data Migration Menu
                mainMenuItems.add(new MenuItem("dataMigration", "Data Migration"));
            }
        }

        //mainMenuItems.add(new MenuItem("help", "Help"));

        ListView lvwMainMenu = (ListView)findViewById(R.id.menuList);
        MenuCustomArrayAdapter adapter = new MenuCustomArrayAdapter(this, mainMenuItems);

        //Assign Adapter to ListView
        lvwMainMenu.setAdapter(adapter);

        // listening to single list item on click
        lvwMainMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Launching new Activity on selecting single List Item
                MenuItem selectedMenu = (MenuItem)mainMenuItems.get(position);
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
}