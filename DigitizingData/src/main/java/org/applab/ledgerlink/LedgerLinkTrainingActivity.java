package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.TrainingModuleRepo;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;

import java.util.ArrayList;
import java.util.List;


public class LedgerLinkTrainingActivity extends SherlockActivity {

    protected ListView listView;
    protected List<LedgerLinkTraining> itemList;
    protected Context context;
    protected int moduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_link_training);

        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

        this.context = this;
        this.moduleId = getIntent().getIntExtra("ModuleId", 0);

        this.listView = (ListView)findViewById(R.id.LVLLTraining);

        this.populateListView();
    }

    protected void populateListView(){
        itemList = new ArrayList<LedgerLinkTraining>();
        itemList.add(new LedgerLinkTraining("Day 1: Introduction to Ledger Link"));
        itemList.add(new LedgerLinkTraining("Day 2: Smart Phone Usage"));
        itemList.add(new LedgerLinkTraining("Day 3: Ledger Link Review"));
        itemList.add(new LedgerLinkTraining("Day 4: Sending Data"));
        itemList.add(new LedgerLinkTraining("Day 5: Data Migration"));
        itemList.add(new LedgerLinkTraining("Day 6: Ledger Link Assessment"));

        ArrayAdapter<LedgerLinkTraining> adapter = new LedgerLinkTrainingAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_ledger_link_training, menu);
        setMenuAction(menu);
        return true;
    }

    protected void setMenuAction(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.menuDRAccept);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String hashKey = Utils.RandomGenerator.getRandomString();
                int count = 0;
                for(int i = 0; i < listView.getAdapter().getCount(); i ++){
                    View itemView = listView.getChildAt(i);
                    CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.CBLLTraining);
                    if(checkBox.isChecked()){
                        LedgerLinkTraining ledgerLinkTraining = itemList.get(i);
                        TrainingModuleResponseRepo trainingModuleResponseRepo = new TrainingModuleResponseRepo(context);
                        if(moduleId > 0) {
                            trainingModuleResponseRepo.save(moduleId, ledgerLinkTraining.getTraining(), "", hashKey);
                            count ++;
                        }
                    }
                }
                if(count == 0){
                    Toast.makeText(context, "You have not selected any trainings", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, "The selected trainings have been saved successfully", Toast.LENGTH_LONG).show();
                    finish();
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

    private class LedgerLinkTrainingAdapter extends ArrayAdapter<LedgerLinkTraining>{
        private Context context;
        private List<LedgerLinkTraining> itemList;

        public LedgerLinkTrainingAdapter(Context context, List<LedgerLinkTraining> itemList){
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
            LedgerLinkTraining ledgerLinkTraining = itemList.get(position);

            CheckBox checkBox = (CheckBox)itemView.findViewById(R.id.CBLLTraining);
            checkBox.setChecked(false);
            checkBox.setText(ledgerLinkTraining.getTraining());

            return  itemView;
        }
    }

    private class LedgerLinkTraining{
        private String training;
        private boolean status;

        public  LedgerLinkTraining(){}

        public LedgerLinkTraining(String training){
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
