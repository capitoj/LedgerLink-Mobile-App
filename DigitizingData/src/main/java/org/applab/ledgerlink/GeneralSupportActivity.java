package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.adapters.DropDownAdapter;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;


public class GeneralSupportActivity extends ActionBarActivity{

    protected Spinner spinner;
    protected EditText editText;
    protected Context context;
    protected int moduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_support);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.context = this;

        editText = (EditText)findViewById(R.id.EditGSComment);
        this.moduleId = getIntent().getIntExtra("ModuleId", 0);

        this.populateDropDown();
    }

    protected void populateDropDown(){
        spinner = (Spinner)findViewById(R.id.GSSPINNER);
        String[] supportList = new String[]{getString(R.string.select_item), getString(R.string.no_allcaps), getString(R.string.yes_allcaps)};
        ArrayAdapter<CharSequence> adapter = new DropDownAdapter(this, supportList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_general_support, menu);
        setMenuAction(menu);
        return true;
    }

    protected void setMenuAction(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.menuDRAccept);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String hashKey = Utils.RandomGenerator.getRandomString();
                String selectedTExt = spinner.getSelectedItem().toString();
                if(!selectedTExt.equals(getString(R.string.select_item))){
                    String comment = editText.getText().toString();
                    TrainingModuleResponseRepo trainingModuleResponseRepo = new TrainingModuleResponseRepo(context);
                    trainingModuleResponseRepo.save(moduleId, selectedTExt, comment, hashKey);
                    Toast.makeText(context, R.string.general_support_response_saved_successfully, Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(context, R.string.select_item_from_drop_down, Toast.LENGTH_SHORT).show();
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
}
