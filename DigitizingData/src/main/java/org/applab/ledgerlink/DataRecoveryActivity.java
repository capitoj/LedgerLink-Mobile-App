package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.applab.ledgerlink.domain.model.DataRecovery;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.adapters.DropDownAdapter;
import org.applab.ledgerlink.helpers.tasks.DataRecoveryAsync;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONObject;


public class DataRecoveryActivity extends AppCompatActivity {

    protected int clickIndex;
    protected DataRecovery dataRecovery;
    protected Spinner cboDRVslaRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_recovery);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.clickIndex = 0;
        this.dataRecovery = new DataRecovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_recovery, menu);
        setMenuAction(menu);
        return true;
    }

    protected void setMenuAction(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.menuDRAccept);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switchLayoutView();
                return true;
            }
        });
    }

    protected Spinner buildVslaRegions(){
        cboDRVslaRegion = (Spinner) findViewById(R.id.cboDRVslaRegion);
        String[] vslaRegions = new String[]{getString(R.string.none), getString(R.string.busia), getString(R.string.bugiri), getString(R.string.iganga), getString(R.string.namayingo)};
        ArrayAdapter<CharSequence> regionAdapter = new DropDownAdapter(this, vslaRegions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboDRVslaRegion.setAdapter(regionAdapter);
        cboDRVslaRegion.setFocusable(true);
        cboDRVslaRegion.setClickable(true);
        return cboDRVslaRegion;
    }

    protected void switchLayoutView(){
        try {
            if (clickIndex == 0) {
                EditText txtDRVslaCode = (EditText)findViewById(R.id.txtDRVslaCode);
                if (txtDRVslaCode.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.vsla_code_required));
                    txtDRVslaCode.requestFocus();
                    return;
                }
                dataRecovery.setVslaCode(txtDRVslaCode.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_passkey);
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRNoOfMeetings);
                txtDRPassKey.setText(dataRecovery.getPassKey());
                clickIndex++;
            } else if (clickIndex == 1) {
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRNoOfMeetings);
                if (txtDRPassKey.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.pass_key_required));
                    txtDRPassKey.requestFocus();
                    return;
                }
                dataRecovery.setPassKey(txtDRPassKey.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_vsla_name);
                EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
                txtDRVslaName.setText(dataRecovery.getVslaName());
                clickIndex++;
            } else if (clickIndex == 2) {
                EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
                if (txtDRVslaName.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.vsla_name_required));
                    txtDRVslaName.requestFocus();
                    return;
                }
                dataRecovery.setVslaName(txtDRVslaName.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_contact_person);
                EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRNoOfCycle);
                txtDRContactPerson.setText(dataRecovery.getContactPerson());
                clickIndex++;
            } else if (clickIndex == 3) {
                EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRNoOfCycle);
                if (txtDRContactPerson.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.name_contact_person_required));
                    txtDRContactPerson.requestFocus();
                    return;
                }
                dataRecovery.setContactPerson(txtDRContactPerson.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_phone_number);
                EditText txtDRPhoneNumber = (EditText)findViewById(R.id.txtDRPhoneNumber);
                txtDRPhoneNumber.setText(dataRecovery.getPhoneNumber());
                clickIndex++;
            } else if (clickIndex == 4) {
                EditText txtDRPhoneNumber = (EditText)findViewById(R.id.txtDRPhoneNumber);
                if (txtDRPhoneNumber.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.phone_no_contact_person_required));
                    txtDRPhoneNumber.requestFocus();
                    return;
                }
                dataRecovery.setPhoneNumber(txtDRPhoneNumber.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_position_in_vsla);
                EditText txtDRPositionInVsla = (EditText)findViewById(R.id.txtDRPositionInVsla);
                txtDRPositionInVsla.setText(dataRecovery.getPositionInVsla());
                clickIndex++;
            } else if (clickIndex == 5) {
                EditText txtDRPositionInVsla = (EditText)findViewById(R.id.txtDRPositionInVsla);
                if (txtDRPositionInVsla.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.position_contact_person_required));
                    txtDRPositionInVsla.requestFocus();
                    return;
                }
                dataRecovery.setPositionInVsla(txtDRPositionInVsla.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_no_of_members);
                EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
                String noOfMembers = dataRecovery.getNoOfMembers() != 0 ? String.valueOf(dataRecovery.getNoOfMembers()) : "";
                txtDRNoOfMembers.setText(noOfMembers);
                clickIndex++;
            } else if (clickIndex == 6) {
                EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
                if (txtDRNoOfMembers.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.no_of_member_in_vsla_group_required));
                    txtDRNoOfMembers.requestFocus();
                    return;
                }
                dataRecovery.setNoOfMembers(Integer.valueOf(txtDRNoOfMembers.getText().toString().trim()));
                setContentView(R.layout.activity_data_recovery_vsla_region);
                this.buildVslaRegions();
                Utils.setSpinnerSelection(dataRecovery.getVslaRegion(), cboDRVslaRegion);
                clickIndex++;
            } else if (clickIndex == 7) {
                String region = cboDRVslaRegion.getSelectedItem().toString();
                if(region.toLowerCase().equals(getString(R.string.none))){
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.region_which_vsla_registered));
                    cboDRVslaRegion.requestFocus();
                    return;
                }
                dataRecovery.setVslaRegion(region);

                //Submit to remote server
                JSONObject jObject = new JSONObject();
                jObject.put("VslaCode", dataRecovery.getVslaCode());
                jObject.put("VslaName", dataRecovery.getVslaName());
                jObject.put("PassKey", dataRecovery.getPassKey());
                jObject.put("Region", dataRecovery.getVslaRegion());
                jObject.put("ContactPerson", dataRecovery.getContactPerson());
                jObject.put("PositionInVsla", dataRecovery.getPositionInVsla());
                jObject.put("PhoneNumber", dataRecovery.getPhoneNumber());
                jObject.put("NumberOfMembers", dataRecovery.getNoOfMembers());
                JSONObject mObject = new JSONObject();
                mObject.put("RecoveryInfo", jObject);
                final String jsonRequest = mObject.toString();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        String serverUri = String.format("%s/%s/%s", Utils.VSLA_SERVER_BASE_URL, "vslas", "datarecovery");
                        new DataRecoveryAsync(DataRecoveryActivity.this).execute(serverUri, jsonRequest);
                    }
                };
                String warning = getString(R.string.action_will_delete_all_vsla_info) + dataRecovery.getVslaName();
                DialogMessageBox.show(this, getString(R.string.warning), warning, runnable);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home : showPreviousWindow(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected  void showPreviousWindow(){
        if(clickIndex == 0){
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(clickIndex == 1){
            EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRNoOfMeetings);
            dataRecovery.setPassKey(txtDRPassKey.getText().toString());

            setContentView(R.layout.activity_data_recovery);
            EditText txtDRVslaCode = (EditText)findViewById(R.id.txtDRVslaCode);
            txtDRVslaCode.setText(dataRecovery.getVslaCode());
            clickIndex--;
        }else if(clickIndex == 2){
            EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
            dataRecovery.setVslaName(txtDRVslaName.getText().toString());

            setContentView(R.layout.activity_data_recovery_passkey);
            EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRNoOfMeetings);
            txtDRPassKey.setText(dataRecovery.getPassKey());
            clickIndex--;
        }else if(clickIndex == 3){
            EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRNoOfCycle);
            dataRecovery.setContactPerson(txtDRContactPerson.getText().toString());

            setContentView(R.layout.activity_data_recovery_vsla_name);
            EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
            txtDRVslaName.setText(dataRecovery.getVslaName());
            clickIndex--;
        }else if(clickIndex == 4){
            EditText txtDRPhoneNumber = (EditText)findViewById(R.id.txtDRPhoneNumber);
            dataRecovery.setPhoneNumber(txtDRPhoneNumber.getText().toString());

            setContentView(R.layout.activity_data_recovery_contact_person);
            EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRNoOfCycle);
            txtDRContactPerson.setText(dataRecovery.getContactPerson());
            clickIndex--;
        }else if(clickIndex == 5){
            EditText txtDRPositionInVsla = (EditText)findViewById(R.id.txtDRPositionInVsla);
            dataRecovery.setPositionInVsla(txtDRPositionInVsla.getText().toString());

            setContentView(R.layout.activity_data_recovery_phone_number);
            EditText txtDRPhoneNumber = (EditText)findViewById(R.id.txtDRPhoneNumber);
            txtDRPhoneNumber.setText(dataRecovery.getPhoneNumber());
            clickIndex--;
        }else if(clickIndex == 6){
            EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
            dataRecovery.setNoOfMembers(Integer.valueOf(txtDRNoOfMembers.getText().toString()));

            setContentView(R.layout.activity_data_recovery_position_in_vsla);
            EditText txtDRPositionInVsla = (EditText)findViewById(R.id.txtDRPositionInVsla);
            txtDRPositionInVsla.setText(dataRecovery.getPositionInVsla());
            clickIndex--;
        }else if(clickIndex == 7){
            cboDRVslaRegion = (Spinner)findViewById(R.id.cboDRVslaRegion);
            String region = cboDRVslaRegion.getSelectedItem().toString();
            dataRecovery.setVslaRegion(region);

            setContentView(R.layout.activity_data_recovery_no_of_members);
            EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
            txtDRNoOfMembers.setText(String.valueOf(dataRecovery.getNoOfMembers()));
            clickIndex--;
        }
    }
}
