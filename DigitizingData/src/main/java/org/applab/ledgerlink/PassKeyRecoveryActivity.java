package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.applab.ledgerlink.domain.model.DataRecovery;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.utils.DialogMessageBox;

/**
 * Created by Home on 20/09/2019.
 */

public class PassKeyRecoveryActivity extends AppCompatActivity {
    protected int clickIndex;
    protected DataRecovery forgotPassKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passkey);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_icon_back);

        this.clickIndex = 0;
        this.forgotPassKey = new DataRecovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_passkey_recovery, menu);
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
                EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
                if (txtDRVslaName.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.vsla_name_required));
                    txtDRVslaName.requestFocus();
                    return;
                }
                forgotPassKey.setVslaCode(txtDRVslaName.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_number_of_meeting);
                EditText txtDRNoOfMeetings = (EditText)findViewById(R.id.txtDRNoOfMeetings);
                txtDRNoOfMeetings.setText(forgotPassKey.getPassKey());
                clickIndex++;
            } else if (clickIndex == 1) {
                EditText txtDRNoOfMeetings = (EditText)findViewById(R.id.txtDRNoOfMeetings);
                if (txtDRNoOfMeetings.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.no_of_meeting_required));
                    txtDRNoOfMeetings.requestFocus();
                    return;
                }
                forgotPassKey.setPassKey(txtDRNoOfMeetings.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_no_of_members);
                EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
                txtDRNoOfMembers.setText(forgotPassKey.getVslaName());
                clickIndex++;
            } else if (clickIndex == 2) {
                EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
                if (txtDRNoOfMembers.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.no_of_member_in_vsla_group_required));
                    txtDRNoOfMembers.requestFocus();
                    return;
                }
                forgotPassKey.setVslaName(txtDRNoOfMembers.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_no_of_cycle);
                EditText txtDRNoOfCycle = (EditText)findViewById(R.id.txtDRNoOfCycle);
                txtDRNoOfCycle.setText(forgotPassKey.getContactPerson());
                clickIndex++;
            } else if (clickIndex == 3) {
                EditText txtDRNoOfCycle = (EditText)findViewById(R.id.txtDRNoOfCycle);
                if (txtDRNoOfCycle.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.no_of_complete_cycle_required));
                    txtDRNoOfCycle.requestFocus();
                    return;
                }
                forgotPassKey.setContactPerson(txtDRNoOfCycle.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_passkey);
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
                txtDRPassKey.setText(forgotPassKey.getPhoneNumber());
                clickIndex++;
            } else if (clickIndex == 4) {
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
                if (txtDRPassKey.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_recovery), getString(R.string.pass_key_required));
                    txtDRPassKey.requestFocus();
                    return;

                } else if (txtDRPassKey.length() <= Integer.parseInt(getResources().getString(R.string.minimum_passkey_length))) {
                    Utils.createAlertDialogOk(PassKeyRecoveryActivity.this, getString(R.string.Registation_main), getString(R.string.passkey_atleast_five_digits_long), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtDRPassKey.requestFocus();
                    return;
                }

                forgotPassKey.setPhoneNumber(txtDRPassKey.getText().toString().trim());

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // update passkey
                    }
                };
                String warning = getString(R.string.action_will_delete_all_vsla_info) + forgotPassKey.getVslaName();
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
            EditText txtDRNoOfMeetings = (EditText)findViewById(R.id.txtDRNoOfMeetings);
            forgotPassKey.setPassKey(txtDRNoOfMeetings.getText().toString());

            setContentView(R.layout.activity_forgot_passkey);
            EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
            txtDRVslaName.setText(forgotPassKey.getVslaCode());
            clickIndex--;
        }else if(clickIndex == 2){
            EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
            forgotPassKey.setVslaName(txtDRNoOfMembers.getText().toString());

            setContentView(R.layout.activity_forgot_passkey_number_of_meeting);
            EditText txtDRNoOfMeetings = (EditText)findViewById(R.id.txtDRNoOfMeetings);
            txtDRNoOfMeetings.setText(forgotPassKey.getPassKey());
            clickIndex--;
        }else if(clickIndex == 3){
            EditText txtDRNoOfCycle = (EditText)findViewById(R.id.txtDRNoOfCycle);
            forgotPassKey.setContactPerson(txtDRNoOfCycle.getText().toString());

            setContentView(R.layout.activity_forgot_passkey_no_of_members);
            EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
            txtDRNoOfMembers.setText(forgotPassKey.getVslaName());
            clickIndex--;
        }else if(clickIndex == 4){
            EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
            forgotPassKey.setPhoneNumber(txtDRPassKey.getText().toString());

            setContentView(R.layout.activity_forgot_passkey_no_of_cycle);
            EditText txtDRNoOfCycle = (EditText)findViewById(R.id.txtDRNoOfCycle);
            txtDRNoOfCycle.setText(forgotPassKey.getContactPerson());
            clickIndex--;
        }
    }
}
