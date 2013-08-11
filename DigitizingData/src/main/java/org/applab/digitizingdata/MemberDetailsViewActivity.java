package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.Calendar;

/**
 * Created by Moses on 6/26/13.
 */
public class MemberDetailsViewActivity extends SherlockActivity {
    ActionBar actionBar;
    int selectedMemberId = -1;
    String selectedMemberNames = "VSLA Member";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_details_view);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Check for Extras in case this call was to edit a pledge
        Bundle b = getIntent().getExtras();


        if (b != null) {

            selectedMemberId = b.getInt("_id", 5);

            if(b.containsKey("_names")) {
                //getString(key, defValue) was added in API 12. Use getString(key), as this will return null if the key doesn't exist.
                String value =  b.getString("_names");
                selectedMemberNames = (null != value)? value : "Unknown Member";
            }
            //Toast.makeText(getBaseContext(),String.format("Member Names: %s",selectedMemberNames),Toast.LENGTH_LONG).show();
            actionBar.setTitle(selectedMemberNames);
        }


        MemberRepo repo = new MemberRepo(getApplicationContext());
        Member selectedMember = repo.getMemberById(selectedMemberId);
        populateDataFields(selectedMember);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.members_details_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuMDVEdit:
                editMember();
                return true;
            case R.id.mnuMDVDelete:
                return delete();
            case R.id.mnuMDVBack:
                navigateBack();
                return true;
        }
        return true;

    }

    private void populateDataFields(Member member) {
        try {

            clearDataFields();
            if (member == null) {
                return;
            }

            // Populate the Fields
            TextView txtMemberNo = (TextView)findViewById(R.id.txtMDVMemberNo);
            txtMemberNo.setText(Utils.formatLongNumber(member.getMemberNo()));
            TextView txtSurname = (TextView)findViewById(R.id.txtMDVSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView)findViewById(R.id.txtMDVOtherNames);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }
            TextView txtGender = (TextView)findViewById(R.id.txtMDVGender);
            if (member.getGender() != null) {
                txtGender.setText(member.getGender());
            }

            TextView txtOccupation = (TextView)findViewById(R.id.txtMDVOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView)findViewById(R.id.txtMDVPhone);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(member.getPhoneNumber());
            }
            TextView txtAge = (TextView)findViewById(R.id.txtMDVAge);

            //TODO: I need to retrieve the Age from the DateOfBirth
            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);
            txtAge.setText(String.format("%d", computedAge));

            //TODO: When we allow members to take leave, we may be better allowing this field to be editable
            TextView txtCyclesCompleted = (TextView)findViewById(R.id.txtMDVCyclesCompleted);
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(member.getDateOfAdmission());
            int cycles = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            txtCyclesCompleted.setText(String.format("%d", cycles));

        }
        finally {

        }

    }

    private void clearDataFields() {
        // Populate the Fields
        TextView txtMemberNo = (TextView)findViewById(R.id.txtMDVMemberNo);
        txtMemberNo.setText(null);
        TextView txtSurname = (TextView)findViewById(R.id.txtMDVSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView)findViewById(R.id.txtMDVOtherNames);
        txtOtherNames.setText(null);
        TextView txtGender = (TextView)findViewById(R.id.txtMDVGender);
        txtGender.setText(null);
        TextView txtOccupation = (TextView)findViewById(R.id.txtMDVOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView)findViewById(R.id.txtMDVPhone);
        txtPhone.setText(null);
        TextView txtAge = (TextView)findViewById(R.id.txtMDVAge);
        txtAge.setText(null);
        TextView txtCyclesCompleted = (TextView)findViewById(R.id.txtMDVCyclesCompleted);
        txtCyclesCompleted.setText(null);

        txtMemberNo.requestFocus();
    }

    private void navigateBack() {
        String caller = "reviewMembers";
        Intent i;

        if(getIntent().hasExtra("_caller")) {
            caller = getIntent().getStringExtra("_caller");
        }

        if(caller.equalsIgnoreCase("newCyclePg2")) {
            i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
            startActivity(i);
        }
        else {
            i = new Intent(getApplicationContext(), MembersListActivity.class);
            startActivity(i);
        }
    }

    private void editMember() {
        Intent i = new Intent(getApplicationContext() ,AddMemberActivity.class);
        i.putExtra("_id",selectedMemberId);
        i.putExtra("_isEditAction", true);
        startActivity(i);
    }

    private boolean delete() {
        final MemberRepo repo = new MemberRepo(getApplicationContext());
        final Member selMember = repo.getMemberById(selectedMemberId);
        if(selMember == null) {
            Utils.createAlertDialogOk(MemberDetailsViewActivity.this, "Remove Member", "System failed to retrieve the member's records.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(MemberDetailsViewActivity.this);
        ad.setTitle("Remove Member");
        ad.setMessage("Are you sure you want to remove this member?");
        ad.setPositiveButton(
            "Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    repo.deleteMember(selMember);
                    navigateBack();
                }
            }
        );
        ad.setNegativeButton(
            "No", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int arg1) {

                }
            }
        );
//        ad.setCancelable(true);
//        ad.setOnCancelListener(
//            new DialogInterface.OnCancelListener() {
//                public void onCancel(DialogInterface dialog) {
//                    eatenByGrue();
//                }
//            }
//        );
        ad.create().show();
        return true;
    }
}