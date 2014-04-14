package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.MiddleCycleMember;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.util.Calendar;

/**
 * Created by Moses on 7/15/13.
 */
public class GettingStartedWizardAddMemberActivity extends AddMemberActivity {
    private ActionBar actionBar;
    private Member selectedMember;
    private int selectedMemberId;
    private boolean successAlertDialogShown = false;
    private boolean selectedFinishButton = false;
    private String dlgTitle = "Add Member";
    MemberRepo repo;
    private boolean isEditAction;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().hasExtra("_isEditAction")){
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction",false);
        }
        if(getIntent().hasExtra("_id")){
            this.selectedMemberId = getIntent().getIntExtra("_id",0);
        }



        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        actionBar = getSupportActionBar();

        if(isEditAction) {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedFinishButton = true;
                            saveMemberData();
                            finish();
                        }
                    });
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

            actionBar.setTitle("Edit Member");
        }
        else {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_next_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedFinishButton = true;
                            saveMemberData();
                            finish();
                        }
                    });
            customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveMemberData();
                        }
                    });
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });


            actionBar.setTitle("New Member");
        }

        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)
        //if in getting started wizard.. use the getting started layout
        //else use the default layout


            setContentView(R.layout.activity_member_details_view_gettings_started_wizard);



        //Setup the Spinner Items
        Spinner cboGender = (Spinner)findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{"Male", "Female"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item,genderList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboGender.setAdapter(adapter);

        cboGender.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        //Make the spinner selectable
        cboGender.setFocusable(true);
        cboGender.setFocusableInTouchMode(true);
        cboGender.setClickable(true);

        clearDataFields();
        if(isEditAction){
            repo = new MemberRepo(getApplicationContext());
            selectedMember = repo.getMemberById(selectedMemberId);
            populateDataFields(selectedMember);
        }

        //Set the current stage of the wizard
        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_ADD_MEMBER);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, GettingsStartedWizardNewCycleActivity.class);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            case R.id.mnuAMNext:
                //Save member and add new member
                if(saveMemberData())
                {
                    clearDataFields();
                }
                return true;
            case R.id.mnuAMFinished:
                selectedFinishButton = true;
                return saveMemberData();
        }
        return true;
    }


    @Override
    protected boolean saveMemberData() {
        boolean successFlg = false;
        AlertDialog dlg = null;

        Member member = new Member();
        repo = new MemberRepo(getApplicationContext());
        if (selectedMember != null) {
            member = selectedMember;
        }

        if (validateGettingStartedMemberData(member)) {
            boolean retVal = false;
            if (member.getMemberId() != 0) {
                retVal = repo.updateMember(member);
            }
            else {
                retVal = repo.addMember(member);
            }
            if (retVal) {
                if (member.getMemberId() == 0) {
                    //Set this new entity as the selected one
                    //Due to this ensure empty fields are explicitly set to null or default value
                    //Otherwise they will assume the value of the selectedMember variable because it is not null
                    selectedMember = member;

                    if(selectedFinishButton) {
                        Toast toast = Toast.makeText(this,"The new member was added successfully.",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT,0,0);
                        toast.show();

                            Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                            startActivity(i);

                        Utils._membersAccessedFromNewCycle = false;
                        Utils._membersAccessedFromEditCycle = false;
                    }
                    else {
                        Toast toast = Toast.makeText(this,"The new member was added successfully. Add another member.",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT,0,0);
                        toast.show();
                        //Clear the Fields and keep adding new records
                        clearDataFields();
                    }

                    selectedFinishButton = false;


                }
                else {
                    Toast.makeText(this,"The member was updated successfully.",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    startActivity(i);

                }



                successFlg = true;
                //clearDataFields(); //Not needed now
            }
            else {
                dlg = Utils.createAlertDialogOk(this, "Add Member", "A problem occurred while adding the new member.", Utils.MSGBOX_ICON_TICK);
                dlg.show();
            }
        }
        else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }


    private boolean validateGettingStartedMemberData(Member member) {
        try {
            if(null == member) {
                return false;
            }

            //Validate common member information via super class
            if(! validateData(member)) {
                return false;
            }


            //Block to perform validation for getting started wizard


                int amountSavedSoFar = 0;
                int outstandingLoan = 0;

                TextView txtSavingsSoFar = (TextView)findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
                String savings = txtSavingsSoFar.getText().toString().trim();
                amountSavedSoFar = Integer.parseInt(savings);
                member.setSavingsOnSetup(amountSavedSoFar);

                TextView txtLoanAmount = (TextView)findViewById(R.id.txtMDVOutstandingLoanAmount);
                String loanAmount = txtLoanAmount.getText().toString().trim();
                outstandingLoan = Integer.parseInt(loanAmount);
                member.setOutstandingLoanOnSetup(outstandingLoan);






            return true;
        }
        catch (Exception ex){
            return false;
        }
    }

    private void populateDataFields(Member member) {
        try {

            clearDataFields();
            if (member == null) {
                return;
            }

            // Populate the Fields
            TextView txtMemberNo = (TextView)findViewById(R.id.txtAMMemberNo);
            txtMemberNo.setText(Utils.formatLongNumber(member.getMemberNo()));

            TextView txtSurname = (TextView)findViewById(R.id.txtAMSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView)findViewById(R.id.txtAMOtherNames);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }
            Spinner cboGender = (Spinner)findViewById(R.id.cboAMGender);
            if(member.getGender() != null) {
                if(member.getGender().startsWith("F") || member.getGender().startsWith("f")){
                    cboGender.setSelection(1);
                }
            }

            TextView txtOccupation = (TextView)findViewById(R.id.txtAMOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView)findViewById(R.id.txtAMPhoneNo);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(member.getPhoneNumber());
            }
            TextView txtAge = (TextView)findViewById(R.id.txtAMAge);
            //txtAge.setText(String.format("%d", 0));

            //TODO: I need to retrieve the Age from the DateOfBirth
            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);
            txtAge.setText(String.format("%d", computedAge));

            //TODO: When we allow members to take leave, we may be better allowing this field to be editable
            TextView txtCyclesCompleted = (TextView)findViewById(R.id.txtAMCycles);
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
        TextView txtMemberNo = (TextView)findViewById(R.id.txtAMMemberNo);
        txtMemberNo.setText(null);
        TextView txtSurname = (TextView)findViewById(R.id.txtAMSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView)findViewById(R.id.txtAMOtherNames);
        txtOtherNames.setText(null);
        //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
        //txtGender.setText(null);
        TextView txtOccupation = (TextView)findViewById(R.id.txtAMOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView)findViewById(R.id.txtAMPhoneNo);
        txtPhone.setText(null);
        TextView txtAge = (TextView)findViewById(R.id.txtAMAge);
        txtAge.setText(null);
        TextView txtCycles = (TextView)findViewById(R.id.txtAMCycles);
        txtCycles.setText(null);

        txtMemberNo.requestFocus();
    }

}