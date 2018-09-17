package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.ledgerlink.domain.model.TrainingModule;
import org.applab.ledgerlink.repo.TrainingModuleRepo;

import java.util.ArrayList;
import java.util.List;


public class TrainingModuleActivity extends SherlockActivity {

    protected ListView listView;
    protected List<TrainingModule> itemList;
    protected Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_module);
        this.context = this;

        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

        this.listView = (ListView)findViewById(R.id.lvTModules);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TrainingModule trainingModule = itemList.get(i);
                switch (trainingModule.getID()){
                    case 1 : showLedgerLinkTraining(trainingModule.getID()); break;
                    case 2 : showEkeysTraining(trainingModule.getID()); break;
                    case 3 : showGeneralSupport(trainingModule.getID()); break;
                }
            }
        });

        this.populateListView();
    }

    protected void showGeneralSupport(int moduleId){
        Intent intent = new Intent(this, GeneralSupportActivity.class);
        intent.putExtra("ModuleId", moduleId);
        startActivity(intent);
    }

    protected void showEkeysTraining(int moduleId){
        Intent intent = new Intent(this, EKeysTrainingActivity.class);
        intent.putExtra("ModuleId", moduleId);
        startActivity(intent);
    }

    protected void showLedgerLinkTraining(int moduleId){
        Intent intent = new Intent(this, LedgerLinkTrainingActivity.class);
        intent.putExtra("ModuleId", moduleId);
        startActivity(intent);
    }

    protected void populateListView(){
        TrainingModuleRepo trainingModuleRepo = new TrainingModuleRepo(context);
        itemList = trainingModuleRepo.getModules();
        ArrayAdapter<TrainingModule> adapter = new TrainingModuleAdapter(this, itemList);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_training_module, menu);
        return true;
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

    private class TrainingModuleAdapter extends ArrayAdapter<TrainingModule>{
        protected Context context;
        protected List<TrainingModule> itemList;

        public TrainingModuleAdapter(Context context, List<TrainingModule> itemList){
            super(context, R.layout.training_module_item, itemList);
            this.context = context;
            this.itemList = itemList;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){
            View itemView = view;
            if(itemView == null){
                itemView = ((Activity)context).getLayoutInflater().inflate(R.layout.training_module_item, viewGroup, false);
            }
            TrainingModule trainingModule = itemList.get(position);
            TextView textView = (TextView)itemView.findViewById(R.id.textViewItem);
            textView.setText(trainingModule.getModule());

            return itemView;
        }
    }
}
