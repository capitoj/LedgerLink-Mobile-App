package org.applab.ledgerlink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.PassKeyReset;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.utils.DialogMessageBox;


/**
 * Created by Home on 20/09/2019.
 */

public class PassKeyResetActivity extends AppCompatActivity {
    protected int clickIndex;
    protected PassKeyReset forgotPassKey;
    LedgerLinkApplication ledgerLinkApplication;
    Meeting meeting;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passkey);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.app_icon_back);

        ledgerLinkApplication = (LedgerLinkApplication) getApplication();

        this.clickIndex = 0;
        this.forgotPassKey = new PassKeyReset();
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

//        //int vslacycle = ledgerLinkApplication.getVslaCycleRepo().getCyclesCount();


        //ArrayList<Meeting> meetings = ledgerLinkApplication.getMeetingRepo().getAllMeetingsOfCycle();
//        VslaCycle recentCycle = new VslaCycleRepo(context).getMostRecentCycle();
//        int noOfMeetings = meetingRepo.getAllMeetings(recentCycle.getCycleId()).size();
        //ArrayList<Meeting> meetingsInCycle = ledgerLinkApplication.getMeetingRepo().getAllMeetingsOfCycle(meeting.getVslaCycle().getCycleId(), MeetingRepo.MeetingOrderByEnum.ORDER_BY_MEETING_DATE);
       // int meetingsInCycle = ledgerLinkApplication.getMeetingRepo().getAllMeetingsOfCycle(meeting.getVslaCycle().getCycleId(), MeetingRepo.MeetingOrderByEnum.ORDER_BY_MEETING_DATE).size();


        try {
            // Get Vsla Name
            if (clickIndex == 0) {
                EditText txtDRVslaName = (EditText)findViewById(R.id.txtDRVslaName);
                if (txtDRVslaName.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_passkey), getString(R.string.vsla_name_required));
                    txtDRVslaName.requestFocus();
                    return;
                }
                forgotPassKey.setVslaName(txtDRVslaName.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_number_of_meeting);
                EditText txtDRNoOfMeetings = (EditText)findViewById(R.id.txtDRNoOfMeetings);
                txtDRNoOfMeetings.setText(forgotPassKey.getNoOfMeetings());
                clickIndex++;
            }
            // Get No. of Meetings
            else if (clickIndex == 1) {
                EditText txtDRNoOfMeetings = (EditText)findViewById(R.id.txtDRNoOfMeetings);
                if (txtDRNoOfMeetings.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_passkey), getString(R.string.no_of_meeting_required));
                    txtDRNoOfMeetings.requestFocus();
                    return;
                }
                forgotPassKey.setNoOfMeetings(txtDRNoOfMeetings.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_no_of_members);
                EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
                txtDRNoOfMembers.setText(forgotPassKey.getNoOfMembers());
                clickIndex++;
            }
            // Get No. of Members in Vsla Group
            else if (clickIndex == 2) {
                EditText txtDRNoOfMembers = (EditText)findViewById(R.id.txtDRNoOfMembers);
                if (txtDRNoOfMembers.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_passkey), getString(R.string.no_of_member_in_vsla_group_required));
                    txtDRNoOfMembers.requestFocus();
                    return;
                }
                forgotPassKey.setNoOfMembers(txtDRNoOfMembers.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_no_of_cycle);
                EditText txtDRNoOfCycle = (EditText)findViewById(R.id.txtDRNoOfCycle);
                txtDRNoOfCycle.setText(forgotPassKey.getNoOfCyclesCompleted());
                clickIndex++;
            }
            // Get No. of Cycle Completed
            else if (clickIndex == 3) {
                EditText txtDRNoOfCycle = (EditText)findViewById(R.id.txtDRNoOfCycle);
                if (txtDRNoOfCycle.getText().toString().trim().length() < 1) {
                    DialogMessageBox.show(this, getString(R.string.action_passkey), getString(R.string.no_of_complete_cycle_required));
                    txtDRNoOfCycle.requestFocus();
                    return;
                }

                // Check if the input details match the Vsla Info

                VslaInfo vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();
                //VslaCycle recentCycle = new VslaCycleRepo(context).getMostRecentCycle();
                String vslaName = vslaInfo.getVslaName(); // Get Vsla Name
                //int noOfMeetings = ledgerLinkApplication.getMeetingRepo().getAllMeetings(recentCycle.getCycleId()).size(); // Get No. of Meetings
                int noOfMeetings = TestingActivity.getNoOfMeeting();
                int noOfMembers = ledgerLinkApplication.getMemberRepo().getAllMembers().size(); // Get No. of Members
                int noOfCyclesCompleted = ledgerLinkApplication.getVslaCycleRepo().getCompletedCycles().size(); // Get No. of Completed Cycles

                if(!vslaName.equalsIgnoreCase(forgotPassKey.getVslaName()) ){
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.error_details_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return;
                }

                int resetNoMeeting = Integer.parseInt(forgotPassKey.getNoOfMeetings());
                if(noOfMeetings != resetNoMeeting){
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.error_details_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return;
                }

                int resetNoMembers = Integer.parseInt(forgotPassKey.getNoOfMembers());
                if(noOfMembers != resetNoMembers){
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.error_details_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return;
                }

                int resetNoCycleCompleted = Integer.parseInt(forgotPassKey.getNoOfCyclesCompleted());
                if(noOfCyclesCompleted != resetNoCycleCompleted){
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.error_details_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    return;
                }

                // Check if the input details match the Vsla Info
//                if ( !txtVslaName.equalsIgnoreCase(vslaName) && !(txtNoMeeting == noOfMeetings) && !(txtNoMembers == noOfMembers) && !(txtNoCycleCompleted == noOfCyclesCompleted)) {
//                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.error_details_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
////                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
////                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                    startActivity(intent);
//                    return;
//                }

                forgotPassKey.setNoOfCyclesCompleted(txtDRNoOfCycle.getText().toString().trim());
                setContentView(R.layout.activity_forgot_passkey_passkey);
                EditText txtDRPassKey = (EditText)findViewById(R.id.txtDRPassKey);
                txtDRPassKey.setText(forgotPassKey.getPassKey());
                clickIndex++;

            }
            // Reset Passkey
            else if (clickIndex == 4) {

                TextView txtPassKey = (TextView) findViewById(R.id.txtDRPassKey);
                TextView txtConfirmPassKey = (TextView) findViewById(R.id.txtDRComfirmPassKey);

                final String passKey = txtPassKey.getText().toString().trim();
                String confirmPassKey = txtConfirmPassKey.getText().toString().trim();

                if (passKey.length() <= 0) {
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.pass_key_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                    return;
                }

                //As per requirements, set the passkey length
                if (passKey.length() <= Integer.parseInt(getResources().getString(R.string.minimum_passkey_length))) {
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.passkey_atleast_five_digits_long), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                    return;
                }
                // Check if the passkey match
                if (confirmPassKey.length() <= 0) {
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.please_confirm_passkey), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                    return;
                } else if (!passKey.equalsIgnoreCase(confirmPassKey)) {
                    Utils.createAlertDialogOk(PassKeyResetActivity.this, getString(R.string.action_passkey), getString(R.string.passkeys_donot_match), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtPassKey.requestFocus();
                    return;
                }


                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        // update passkey
                        ledgerLinkApplication.getVslaInfoRepo().resetPassKey(passKey);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }
                };
                String warning = getString(R.string.action_reset_passkey);
                DialogMessageBox.show(this, getString(R.string.warning), warning, runnable);

            }

            // ---Button view---
//            Button btnReset = (Button) findViewById(R.id.btnReset);
//            btnReset.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//
//                }
//            });

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
       }

    }
}
