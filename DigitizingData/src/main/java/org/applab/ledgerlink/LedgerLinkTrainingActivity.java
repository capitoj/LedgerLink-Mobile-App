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

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.TrainingModuleRepo;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;

import java.util.ArrayList;
import java.util.List;


public class LedgerLinkTrainingActivity extends ActionBarActivity{

    protected ListView listView;
    protected List<LedgerLinkTraining> itemList;
    protected Context context;
    protected int moduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_link_training);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.context = this;
        this.moduleId = getIntent().getIntExtra("ModuleId", 0);

        this.listView = (ListView)findViewById(R.id.LVLLTraining);

        this.populateListView();
    }

    protected void populateListView(){
        itemList = new ArrayList<LedgerLinkTraining>();
        itemList.add(new LedgerLinkTraining(getString(R.string.intor_to_ledgerlink)));
        itemList.add(new LedgerLinkTraining(getString(R.string.smarrt_phone_usage)));
        itemList.add(new LedgerLinkTraining(getString(R.string.ledgerlink_review)));
        itemList.add(new LedgerLinkTraining(getString(R.string.sending_data)));
        itemList.add(new LedgerLinkTraining(getString(R.string.data_migation)));
        itemList.add(new LedgerLinkTraining(getString(R.string.ledgerlink_assessment)));

        ArrayAdapter<LedgerLinkTraining> adapter = new LedgerLinkTrainingAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ledger_link_training, menu);
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
                    Toast.makeText(context, R.string.not_selected_any_trainings, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, R.string.seleced_trainings_saved_successfully, Toast.LENGTH_LONG).show();
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
