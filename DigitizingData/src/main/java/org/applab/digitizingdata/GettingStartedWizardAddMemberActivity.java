package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Moses on 7/15/13.
 */
public class GettingStartedWizardAddMemberActivity extends AddMemberActivity {
    MemberRepo repo;
    private ActionBar actionBar;
    private Member selectedMember;
    private int selectedMemberId;
    private boolean successAlertDialogShown = false;
    private boolean selectedFinishButton = false;
    private String dlgTitle = "Add Member";
    private boolean isEditAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        if (getIntent().hasExtra("_isEditAction")) {
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction", false);
        }
        if (getIntent().hasExtra("_id")) {
            Log.d(getBaseContext().getPackageName(), "Member id " + getIntent().getIntExtra("_id", 0) + " to be loaded");
            this.selectedMemberId = getIntent().getIntExtra("_id", 0);
        }

        setContentView(R.layout.activity_member_details_view_gettings_started_wizard);

        // Set instructions
        TypefaceTextView lblAMInstruction = (TypefaceTextView) findViewById(R.id.lblAMInstruction);

        SpannableStringBuilder headingInstruction = new SpannableStringBuilder();

        SpannableStringBuilder plusText = new SpannableStringBuilder("+ ");
        plusText.setSpan(new StyleSpan(Typeface.BOLD), 0, plusText.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder nextText = new SpannableStringBuilder("next.");
        nextText.setSpan(new StyleSpan(Typeface.BOLD), 0, nextText.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        headingInstruction.append("Enter each member. Save and add another member by selecting ");
        headingInstruction.append(plusText);
        headingInstruction.append("and when you have entered all members, select ");
        headingInstruction.append(nextText);

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        actionBar = getSupportActionBar();

        if (isEditAction) {
            // actionBar.setTitle("Edit Member");
            actionBar.setTitle("GET STARTED");
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
            );

            headingInstruction = new SpannableStringBuilder("");
        } else {
            // actionBar.setTitle("New Member");
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back_next_done, null);
            customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            selectedFinishButton = false;
                            saveMemberData();
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(getApplicationContext(), GettingsStartedWizardNewCycleActivity.class);
                            i.putExtra("_isFromReviewMembers", false);
                            startActivity(i);
                            finish();
                        }
                    }
            );
        }
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //If member number is not set and names are blank, then allow to skip
                        Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
                        TextView txtAMSurname = (TextView) findViewById(R.id.txtAMSurname);
                        if (cboAMMemberNo.getSelectedItemPosition() == 0 && txtAMSurname.getText().length() == 0) {
                            //Skip to review members
                            Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                            startActivity(i);
                            finish();
                            return;
                        }
                        selectedFinishButton = true;
                        if (saveMemberData()) {
                            //finish();
                        }


                    }
                }
        );
        // actionBar.setTitle("New Member");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("GET STARTED");

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        lblAMInstruction.setText(headingInstruction);

        clearDataFields();
        if (isEditAction) {
            repo = new MemberRepo(getApplicationContext());
            selectedMember = repo.getMemberById(selectedMemberId);
            populateDataFields(selectedMember);
        } else {
            //Set the current stage of the wizard
            VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
            vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_ADD_MEMBER);
        }
    }

    private void buildGenderSpinner() {

        //Setup the Spinner Items
        Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{"select sex", "Male", "Female"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, genderList) {
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
    }

    /* Populates the member no spinner with available member numbers */
    private void buildMemberNoSpinner() {

        Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
        repo = new MemberRepo(getApplicationContext());
        ArrayList<String> memberNumberArrayList = new ArrayList<String>();
        memberNumberArrayList.add("select number");
        //If we have a selected member, then add the member number to the adapter
        if (selectedMember != null && selectedMember.getMemberNo() != 0) {
            memberNumberArrayList.add(selectedMember.getMemberNo() + "");
        }
        for (String mNo : repo.getListOfAvailableMemberNumbers(30)) {
            Log.d(getBaseContext().getPackageName(), "Member number found " + mNo);
            memberNumberArrayList.add(mNo);
        }
        String[] memberNumberList = memberNumberArrayList.toArray(new String[memberNumberArrayList.size()]);
        memberNumberArrayList.toArray(memberNumberList);
        ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, memberNumberList) {
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

        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMMemberNo.setAdapter(memberNoAdapter);
        cboAMMemberNo.setOnItemSelectedListener(new CustomGenderSpinnerListener());
        //Make the spinner selectable
        cboAMMemberNo.setFocusable(true);
        cboAMMemberNo.setFocusableInTouchMode(true);
        cboAMMemberNo.setClickable(true);
    }

    /* Populates the member age spinner  */
    private void buildAgeSpinner() {

        Spinner cboAMAge = (Spinner) findViewById(R.id.cboAMAge);
        repo = new MemberRepo(getApplicationContext());
        ArrayList<String> ageArrayList = new ArrayList<String>();
        ageArrayList.add("select age");
        for (int i = 16; i <= 80; i++) {
            ageArrayList.add(i + "");
        }
        String[] ageList = ageArrayList.toArray(new String[ageArrayList.size()]);
        ageArrayList.toArray(ageList);
        ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, ageList) {
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
        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMAge.setAdapter(memberNoAdapter);
        cboAMAge.setOnItemSelectedListener(new CustomGenderSpinnerListener());
        //Make the spinner selectable
        cboAMAge.setFocusable(true);
        cboAMAge.setFocusableInTouchMode(true);
        cboAMAge.setClickable(true);
    }

    /* Populates the member cycles completed spinner */
    private void buildCyclesCompletedSpinner() {

        Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
        repo = new MemberRepo(getApplicationContext());
        ArrayList<String> cyclesArrayList = new ArrayList<String>();
        cyclesArrayList.add("select number");
        for (int i = 0; i <= 20; i++) {
            cyclesArrayList.add(i + "");
        }
        String[] ageList = cyclesArrayList.toArray(new String[cyclesArrayList.size()]);
        cyclesArrayList.toArray(ageList);
        ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, ageList) {
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


        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMCycles.setAdapter(memberNoAdapter);
        cboAMCycles.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        // Make the spinner selectable
        cboAMCycles.setFocusable(true);
        cboAMCycles.setFocusableInTouchMode(true);
        cboAMCycles.setClickable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

     Intent i;
         switch(item.getItemId()) {
             case android.R.id.home:
                 Intent upIntent = new Intent(this, GettingsStartedWizardNewCycleActivity.class);
                 upIntent.putExtra("_isFromReviewMembers", false);
                 startActivity(upIntent);
                 finish();
                 /*if (NavUtils.shouldUpRecreateTask(this, upIntent)) {

                     // This activity is not part of the application's task, so
                     // create a new task
                     // with a synthesized back stack.

                     TaskStackBuilder
                             .from(this)
                             .addNextIntent(upIntent)
                             .addNextIntent(upIntent).startActivities();
                     finish();
                 } else {

                     // This activity is part of the application's task, so simply
                     // navigate up to the hierarchical parent activity.
                     NavUtils.navigateUpTo(this, upIntent);
                 } */

         }
         return true;
     /**
        switch(item.getItemId()) {
            case R.id.mnuAMNext:
                //TODO: If member number is nothing, allow proceeding without saving
                //Save member and add new member
                if(saveMemberData()) {
                    clearDataFields();
                }
                else {
                     Toast.makeText(this, "Failed to save member information", Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.mnuAMFinished:
                selectedFinishButton = true;
                return saveMemberData();
        }
        return true;
    */
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
        if (validateData(member)) {
            boolean retVal = false;
            if (member.getMemberId() != 0) {
                retVal = repo.updateGettingStartedWizardMember(member);
            } else {
                retVal = repo.addGettingStartedWizardMember(member);
            }
            if (retVal) {
                if (!isEditAction) {
                    //Set this new entity as the selected one
                    //Due to this ensure empty fields are explicitly set to null or default value
                    //Otherwise they will assume the value of the selectedMember variable because it is not null
                    selectedMember = member;
                    if (selectedFinishButton) {
                        Toast toast = Toast.makeText(this, "The new member was added successfully.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        Log.i(getBaseContext().getPackageName(), "Going to start Review members activity");
                        Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                        startActivity(i);
                        Utils._membersAccessedFromNewCycle = false;
                        Utils._membersAccessedFromEditCycle = false;
                        finish();
                    } else {
                        Toast toast = Toast.makeText(this, "The new member was added successfully. Add another member.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        //Clear the Fields and keep adding new records
                        //Set member to null to ensure member id is removed from list of member ids
                        selectedMember = null;
                        clearDataFields();
                    }
                    selectedFinishButton = false;


                } else {
                    Toast.makeText(this, "The member was updated successfully.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                    startActivity(i);
                    finish();
                }
                successFlg = true;
                //clearDataFields(); //Not needed now
            } else {
                dlg = Utils.createAlertDialogOk(this, "Add Member", "A problem occurred while adding the new member.", Utils.MSGBOX_ICON_TICK);
                dlg.show();
            }
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }
        return successFlg;
    }

    @Override
    protected boolean validateData(Member member) {

        try {
            if (null == member) {
                return false;
            }
            repo = new MemberRepo(getApplicationContext());
            // Validate: MemberNo
            Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
            if (cboAMMemberNo.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The member number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMMemberNo.requestFocus();
                return false;
            } else {
                String memberNo = cboAMMemberNo.getSelectedItem().toString().trim();
                int theMemberNo = Integer.parseInt(memberNo);
                member.setMemberNo(theMemberNo);
            }
            //Validate: Surname
            TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
            String surname = txtSurname.getText().toString().trim();
            if (surname.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Surname is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtSurname.requestFocus();
                return false;
            } else {
                member.setSurname(surname);
            }
            //Validate: OtherNames
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherNames);
            String otherNames = txtOtherNames.getText().toString().trim();
            if (otherNames.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "At least one other name is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOtherNames.requestFocus();
                return false;
            } else {
                member.setOtherNames(otherNames);
            }
            //Validate: Gender
            //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
            Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
            if (cboGender.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The sex is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboGender.requestFocus();
                return false;
            } else {
                String gender = cboGender.getSelectedItem().toString().trim();
                member.setGender(gender);
            }
            // Validate: Age
            Spinner cboAge = (Spinner) findViewById(R.id.cboAMAge);
            if (cboAge.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dlgTitle, "The age is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAge.requestFocus();
                return false;
            } else {
                String age = cboAge.getSelectedItem().toString().trim();
                Integer theAge = Integer.parseInt(age);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -theAge);
                member.setDateOfBirth(c.getTime());
            }
            //Validate: Occupation
            TextView txtOccupation = (TextView) findViewById(R.id.txtAMOccupation);
            String occupation = txtOccupation.getText().toString().trim();
            if (occupation.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Occupation is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOccupation.requestFocus();
                return false;
            } else {
                member.setOccupation(occupation);
            }
            //Validate: PhoneNumber
            TextView txtPhoneNo = (TextView) findViewById(R.id.txtAMPhoneNo);
            String phoneNo = txtPhoneNo.getText().toString().trim();
            if (phoneNo.length() < 1) {
                //Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Phone Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtPhoneNo.requestFocus();
                //return false;
                member.setPhoneNumber(null);
            } else {
                member.setPhoneNumber(phoneNo);
            }
            // Validate: Cycles Completed
            Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
            if (cboAMCycles.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dlgTitle, "The cycles completed field is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMCycles.requestFocus();
                return false;
            } else {
                String cycles = cboAMCycles.getSelectedItem().toString().trim();
                Integer theCycles = Integer.parseInt(cycles);
                if (theCycles > 100) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The number of completed cycles is too high.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    cboAMCycles.requestFocus();
                    return false;
                }
                member.setCyclesCompleted(theCycles);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -theCycles);
                member.setDateOfAdmission(c.getTime());
            }
            //Final Verifications
            //TODO: Trying to use Application context to ensure dialog box does not disappear
            if (!repo.isMemberNoAvailable(member.getMemberNo(), member.getMemberId())) {
                Utils.createAlertDialogOk(this, dlgTitle, "Another member is using this Member Number.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMMemberNo.requestFocus();
                return false;
            }
            int amountSavedSoFar = 0;
            int outstandingLoan = 0;
            TextView txtSavingsSoFar = (TextView) findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
            String savings = txtSavingsSoFar.getText().toString().trim();
            amountSavedSoFar = Integer.parseInt(savings);
            member.setSavingsOnSetup(amountSavedSoFar);
            TextView txtLoanAmount = (TextView) findViewById(R.id.txtMDVOutstandingLoanAmount);
            String loanAmount = txtLoanAmount.getText().toString().trim();
            outstandingLoan = Integer.parseInt(loanAmount);
            member.setOutstandingLoanOnSetup(outstandingLoan);
            return true;
        } catch (Exception ex) {
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
            Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
            Utils.setSpinnerSelection(member.getMemberNo() + "", cboAMMemberNo);
            TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherNames);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }
            Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
            Utils.setSpinnerSelection(member.getGender(), cboGender);
            TextView txtOccupation = (TextView) findViewById(R.id.txtAMOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView) findViewById(R.id.txtAMPhoneNo);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(member.getPhoneNumber());
            }
            //Set the age
            Spinner cboAMAge = (Spinner) findViewById(R.id.cboAMAge);
            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);
            Utils.setSpinnerSelection(computedAge + "", cboAMAge);
            calToday = Calendar.getInstance();
            //TODO: When we allow members to take leave, we may be better allowing this field to be editable
            //Set cycles
            Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(member.getDateOfAdmission());
            int cycles = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            Utils.setSpinnerSelection(cycles + "", cboAMCycles);
            Log.d(getBaseContext().getPackageName(), "Member savings and Loans are " + member.getSavingsOnSetup() + " and " + member.getOutstandingLoanOnSetup());
            //Populate fields for savings and loan at setup
            TextView txtSavingsSoFar = (TextView) findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
            txtSavingsSoFar.setText(String.format("%.0f", member.getSavingsOnSetup()));
            TextView txtLoanAmount = (TextView) findViewById(R.id.txtMDVOutstandingLoanAmount);
            txtLoanAmount.setText(String.format("%.0f", member.getOutstandingLoanOnSetup()));

        } finally {
        }

    }

    private void clearDataFields() {
        //Spinner items
        buildGenderSpinner();
        buildMemberNoSpinner();
        buildAgeSpinner();
        buildCyclesCompletedSpinner();
        // Populate the Fields
        Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
        //txtMemberNo.setText(null);
        TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherNames);
        txtOtherNames.setText(null);
        //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
        //txtGender.setText(null);
        TextView txtOccupation = (TextView) findViewById(R.id.txtAMOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView) findViewById(R.id.txtAMPhoneNo);
        txtPhone.setText(null);
        cboAMMemberNo.requestFocus();
    }

}