package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.actionbarsherlock.app.SherlockActivity;

import org.applab.ledgerlink.domain.model.DataRecovery;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.tasks.DataRecoveryAsync;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.JSONObject;


public class DataRecoveryActivity extends SherlockActivity {

    protected int clickIndex;
    protected DataRecovery dataRecovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_recovery);
        getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);

        this.clickIndex = 0;
        this.dataRecovery = new DataRecovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_data_recovery, menu);
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


    protected void switchLayoutView(){
        try {
            if (clickIndex == 0) {
                EditText txtDRVslaCode = (EditText)findViewById(R.id.txtDRVslaCode);
                if (txtDRVslaCode.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, "Data Recovery", "The Vsla Code is required");
                    txtDRVslaCode.requestFocus();
                    return;
                }
                dataRecovery.setVslaCode(txtDRVslaCode.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_passkey);
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
                txtDRPassKey.setText(dataRecovery.getPassKey());
                clickIndex++;
            } else if (clickIndex == 1) {
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
                if (txtDRPassKey.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, "Data Recovery", "The Pass Key is required");
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
                    DialogMessageBox.show(this, "Data Recovery", "The Vsla Name is required");
                    txtDRVslaName.requestFocus();
                    return;
                }
                dataRecovery.setVslaName(txtDRVslaName.getText().toString().trim());
                setContentView(R.layout.activity_data_recovery_contact_person);
                EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRContactPerson);
                txtDRContactPerson.setText(dataRecovery.getContactPerson());
                clickIndex++;
            } else if (clickIndex == 3) {
                EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRContactPerson);
                if (txtDRContactPerson.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, "Data Recovery", "The name of the Contact Person is required");
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
                    DialogMessageBox.show(this, "Data Recovery", "The Phone Number of the contact person is required");
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
                    DialogMessageBox.show(this, "Data Recovery", "The Position of the contact person is required");
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
                    DialogMessageBox.show(this, "Data Recovery", "The number of members in the Vsla Group is required");
                    txtDRNoOfMembers.requestFocus();
                    return;
                }
                dataRecovery.setNoOfMembers(Integer.valueOf(txtDRNoOfMembers.getText().toString().trim()));
                setContentView(R.layout.activity_data_recovery_vsla_region);
                EditText txtDRVslaRegion = (EditText)findViewById(R.id.txtDRVslaRegion);
                txtDRVslaRegion.setText(dataRecovery.getVslaRegion());
                clickIndex++;
            } else if (clickIndex == 7) {
                EditText txtDRVslaRegion = (EditText)findViewById(R.id.txtDRVslaRegion);
                if (txtDRVslaRegion.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, "Data Recovery", "The Region in which the Vsla was registered is required");
                    txtDRVslaRegion.requestFocus();
                    return;
                }
                dataRecovery.setVslaRegion(txtDRVslaRegion.getText().toString().trim());
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
                String warning = "This action will delete all the VSLA information in the phone. Are you sure you would like to proceed with the data recovery for " + dataRecovery.getVslaName();
                DialogMessageBox.show(this, "Warning!", warning, runnable);
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
            setContentView(R.layout.activity_data_recovery);
            EditText txtDRVslaCode = (EditText)findViewById(R.id.txtDRVslaCode);
            txtDRVslaCode.setText(dataRecovery.getVslaCode());
            clickIndex--;
        }else if(clickIndex == 2){
            setContentView(R.layout.activity_data_recovery_passkey);
            EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
            txtDRPassKey.setText(dataRecovery.getPassKey());
            clickIndex--;
        }else if(clickIndex == 3){
            setContentView(R.layout.activity_data_recovery_vsla_name);
            EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
            txtDRVslaName.setText(dataRecovery.getVslaName());
            clickIndex--;
        }else if(clickIndex == 4){
            setContentView(R.layout.activity_data_recovery_contact_person);
            EditText txtDRContactPerson = (EditText)findViewById(R.id.txtDRContactPerson);
            txtDRContactPerson.setText(dataRecovery.getContactPerson());
            clickIndex--;
        }else if(clickIndex == 5){
            setContentView(R.layout.activity_data_recovery_phone_number);
            EditText txtDRPhoneNumber = (EditText)findViewById(R.id.txtDRPhoneNumber);
            txtDRPhoneNumber.setText(dataRecovery.getPhoneNumber());
            clickIndex--;
        }else if(clickIndex == 6){
            setContentView(R.layout.activity_data_recovery_position_in_vsla);
            EditText txtDRPositionInVsla = (EditText)findViewById(R.id.txtDRPositionInVsla);
            txtDRPositionInVsla.setText(dataRecovery.getPositionInVsla());
            clickIndex--;
        }else if(clickIndex == 7){
            setContentView(R.layout.activity_data_recovery_no_of_members);
            EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
            txtDRNoOfMembers.setText(String.valueOf(dataRecovery.getNoOfMembers()));
            clickIndex--;
        }
    }
}
