package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;

import java.util.ArrayList;
import java.util.List;


public class EKeysTrainingActivity extends SherlockActivity {

    protected ListView listView;
    protected List<EkeysTraining> itemList;
    protected int moduleId;
    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ekeys_training);
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
        this.listView = (ListView)findViewById(R.id.LVEKTraining);
        this.moduleId = getIntent().getIntExtra("ModuleId", 0);
        this.context = this;

        this.populateListView();
    }

    protected void populateListView(){
        itemList = new ArrayList<EkeysTraining>();
        itemList.add(new EkeysTraining("Day 1: Introduction to eKeys"));
        itemList.add(new EkeysTraining("Day 2: Phone & Mobile Money Usage"));
        itemList.add(new EkeysTraining("Day 3: Airtel Weza Review"));
        itemList.add(new EkeysTraining("Day 4: eKeys Review"));
        itemList.add(new EkeysTraining("Day 5: Group Submission and Sensitization"));
        itemList.add(new EkeysTraining("Day 6: eKeys Assessment"));

        ArrayAdapter<EkeysTraining> adapter = new EkeysTrainingAdapter(this, itemList);
        listView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_ekeys_training, menu);
        setMenuAction(menu);
        return true;
    }

    protected  void setMenuAction(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.menuDRAccept);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String hashKey = Utils.RandomGenerator.getRandomString();
                int count = 0;
                for(int i = 0; i < listView.getAdapter().getCount(); i++){
                    View itemView = listView.getChildAt(i);
                    CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.CBLLTraining);
                    if(checkBox.isChecked()){
                        EkeysTraining ekeysTraining = itemList.get(i);
                        TrainingModuleResponseRepo trainingModuleResponseRepo = new TrainingModuleResponseRepo(context);
                        if(moduleId > 0){
                            trainingModuleResponseRepo.save(moduleId, ekeysTraining.getTraining(), "", hashKey);
                            count ++;
                        }
                    }
                }
                if(count > 0){
                    Toast.makeText(context, "The selected trainings have been saved successfully", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(context, "You have not selected any trainings", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case android.R.id.home : finish(); break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class EkeysTrainingAdapter extends ArrayAdapter<EkeysTraining>{
        private Context context;
        private List<EkeysTraining> itemList;

        public EkeysTrainingAdapter(Context context, List<EkeysTraining> itemList){
            super(context, R.layout.ledger_link_training_item, itemList);
            this.context = context;
            this.itemList = itemList;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){
            View itemView = view;
            if(itemView == null){
                itemView = ((Activity)context).getLayoutInflater().inflate(R.layout.ledger_link_training_item, viewGroup, false);
            }
            EkeysTraining ekeysTraining = itemList.get(position);

            CheckBox checkBox = (CheckBox)itemView.findViewById(R.id.CBLLTraining);
            checkBox.setChecked(false);
            checkBox.setText(ekeysTraining.getTraining());

            return itemView;
        }
    }

    private class EkeysTraining{
        private String training;
        private boolean status;

        public EkeysTraining(String training){
            this.training = training;
        }

        public void setTraining(String training){
            this.training = training;
        }

        public String getTraining(){
            return this.training;
        }

        public void setStatus(boolean status){
            this.status = status;
        }

        public boolean getStatus(){
            return this.status;
        }
    }
}
