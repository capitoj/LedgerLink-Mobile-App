package org.applab.digitizingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.digitizingdata.R;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/15/13.
 */
public class AddMemberActivity extends SherlockActivity {
    private ActionBar actionBar;
    private Member selectedMember;
    private int selectedMemberId;
    private boolean successAlertDialogShown = false;
    private boolean selectedFinishButton = false;
    private String dlgTitle = "Add Member";
    private int meetingId;
   private MeetingFineRepo fineRepo;
    private MeetingRepo meetingRepo;
    MemberRepo repo;
    Meeting targetMeeting;
    private boolean isEditAction;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        if(getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }
        if(getIntent().hasExtra("_id")){
            this.selectedMemberId = getIntent().getIntExtra("_id",0);
        }
        if(getIntent().hasExtra("_isEditAction")){
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction", false);
        }


        meetingRepo = new MeetingRepo(getApplicationContext());
        targetMeeting = meetingRepo.getMeetingById(meetingId);


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


        setContentView(R.layout.activity_add_member);



        //Setup the Spinner Items
        Spinner cboGender = (Spinner)findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{"Male", "Female"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, genderList)

        {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }

        };
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
            case R.id.mnuAMNext:
                //Toast.makeText(getBaseContext(), "You have successfully added a new member", Toast.LENGTH_LONG).show();
                return saveMemberData();
            case R.id.mnuAMFinished:
                selectedFinishButton = true;
                return saveMemberData();
        }
        return true;
    }

    protected boolean saveMemberData() {
        boolean successFlg = false;
        AlertDialog dlg = null;

        Member member = new Member();
        repo = new MemberRepo(getApplicationContext());
        if (selectedMember != null) {
            member = selectedMember;
        }

        if (validateData(member)) {
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
                        if(Utils._membersAccessedFromNewCycle) {
                            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                            i.putExtra("_isUpdateCycleAction", false);
                            startActivity(i);
                        }
                        else if(Utils._membersAccessedFromEditCycle) {
                            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                            i.putExtra("_isUpdateCycleAction", true);
                            startActivity(i);
                        }
                        else {
                            Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                            startActivity(i);
                        }
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

                    /*
                    dlg = Utils.createAlertDialog(AddMemberActivity.this,"Add Member","The new member was added successfully.", Utils.MSGBOX_ICON_TICK);
                    // Setting OK Button
                    dlg.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(successAlertDialogShown) {
                                if(selectedFinishButton) {
                                    if(Utils._membersAccessedFromNewCycle) {
                                        Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                                        i.putExtra("_isUpdateCycleAction", false);
                                        startActivity(i);
                                    }
                                    else if(Utils._membersAccessedFromEditCycle) {
                                        Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                                        i.putExtra("_isUpdateCycleAction", true);
                                        startActivity(i);
                                    }
                                    else {
                                        Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                                        startActivity(i);
                                    }
                                    Utils._membersAccessedFromNewCycle = false;
                                    Utils._membersAccessedFromEditCycle = false;
                                }
                                else {
                                    //Clear the Fields and keep adding new records
                                    clearDataFields();
                                }

                                selectedFinishButton = false;
                                successAlertDialogShown = false;
                            }
                        }
                    });
                    dlg.show();
                    */
                }
                else {
                    Toast.makeText(this,"The member was updated successfully.",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    startActivity(i);

                    /*
                    dlg = Utils.createAlertDialog(AddMemberActivity.this,"Edit Member","The member was updated successfully.", Utils.MSGBOX_ICON_TICK);
                    // Setting OK Button
                    dlg.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(successAlertDialogShown) {
                                Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                                startActivity(i);
                                successAlertDialogShown = false;
                            }
                        }
                    });
                    dlg.show();
                    */
                }

                /*
                if(dlg.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }
                */

                successFlg = true;
                //clearDataFields(); //Not needed now
            }
            else {
                dlg = Utils.createAlertDialogOk(AddMemberActivity.this, "Add Member", "A problem occurred while adding the new member.", Utils.MSGBOX_ICON_TICK);
                dlg.show();
            }
        }
        else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    protected boolean validateData(Member member) {
        try {
            if(null == member) {
                return false;
            }
            repo = new MemberRepo(getApplicationContext());
            // Validate: MemberNo
            TextView txtMemberNo = (TextView)findViewById(R.id.txtAMMemberNo);
            String memberNo = txtMemberNo.getText().toString().trim();
            if (memberNo.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Member Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtMemberNo.requestFocus();
                return false;
            }
            else {
                int theMemberNo = Integer.parseInt(memberNo);
                if (theMemberNo <= 0) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The Member Number must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtMemberNo.requestFocus();
                    return false;
                }
                else {
                    member.setMemberNo(theMemberNo);
                }
            }

            //Validate: Surname
            TextView txtSurname = (TextView)findViewById(R.id.txtAMSurname);
            String surname = txtSurname.getText().toString().trim();
            if(surname.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Surname is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtSurname.requestFocus();
                return false;
            }
            else {
                member.setSurname(surname);
            }

            //Validate: OtherNames
            TextView txtOtherNames = (TextView)findViewById(R.id.txtAMOtherNames);
            String otherNames = txtOtherNames.getText().toString().trim();
            if(otherNames.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "At least one other name is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOtherNames.requestFocus();
                return false;
            }
            else {
                member.setOtherNames(otherNames);
            }

            //Validate: Gender
            //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
            Spinner cboGender = (Spinner)findViewById(R.id.cboAMGender);
            //String gender = txtGender.getText().toString().trim();
            String gender = cboGender.getSelectedItem().toString().trim();
            if(gender.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Sex is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboGender.requestFocus();
                return false;
            }
            else {
                member.setGender(gender);
            }

            // Validate: Age
            TextView txtAge = (TextView)findViewById(R.id.txtAMAge);
            String age = txtAge.getText().toString().trim();
            if (age.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Age is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtAge.requestFocus();
                return false;
            }
            else {
                int theAge = Integer.parseInt(age);
                if (theAge <= 0) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The Age must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtAge.requestFocus();
                    return false;
                }
                else if(theAge > 120) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The Age is too high.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtAge.requestFocus();
                    return false;
                }
                else {
                    //Get the DateOfBirth from the Age
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.YEAR, -theAge);
                    member.setDateOfBirth(c.getTime());
                }
            }

            //Validate: Occupation
            TextView txtOccupation = (TextView)findViewById(R.id.txtAMOccupation);
            String occupation = txtOccupation.getText().toString().trim();
            if(occupation.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Occupation is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOccupation.requestFocus();
                return false;
            }
            else {
                member.setOccupation(occupation);
            }

            //Validate: PhoneNumber
            TextView txtPhoneNo = (TextView)findViewById(R.id.txtAMPhoneNo);
            String phoneNo = txtPhoneNo.getText().toString().trim();
            if(phoneNo.length() < 1) {
                //Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Phone Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtPhoneNo.requestFocus();
                //return false;
                member.setPhoneNumber(null);
            }
            else {
                member.setPhoneNumber(phoneNo);
            }

            // Validate: Cycles Completed
            TextView txtCycles = (TextView)findViewById(R.id.txtAMCycles);
            String cycles = txtCycles.getText().toString().trim();
            int theCycles = 0;
            member.setCyclesCompleted(0);
            if (cycles.length() < 1) {
//                Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Number of Completed Cycles is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
//                txtCycles.requestFocus();
//                return false;

            }
            else {
                theCycles = Integer.parseInt(cycles);
                if (theCycles < 0) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The number of cycles must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtCycles.requestFocus();
                    return false;
                }
                else if(theCycles > 100) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The number of completed cycles is too high.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtCycles.requestFocus();
                    return false;
                }
                else {
                    member.setCyclesCompleted(theCycles);

                    //Get the Date of Admission
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.YEAR, -theCycles);
                    member.setDateOfAdmission(c.getTime());
                }
            }






            //Final Verifications
            //TODO: Trying to use Application context to ensure dialog box does not disappear
            if(!repo.isMemberNoAvailable(member.getMemberNo(),member.getMemberId())) {
                Utils.createAlertDialogOk(this, dlgTitle, "Another member is using this Member Number.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtMemberNo.requestFocus();
                return false;
            }

            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
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