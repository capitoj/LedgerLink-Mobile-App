package org.applab.ledgerlink;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.fontutils.TypefaceTextView;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.repo.VslaInfoRepo;

import java.util.Calendar;
import java.util.Date;




/**
 * Created by Moses on 7/15/13.
 */
public class GettingStartedWizardAddMemberActivity extends AddMemberActivity {

    private TextView txtNCGSWLoanNextRepaymentDate;
    private TextView txtOutstandingWelfareDueDate;
    private TextView txtNCGSWLoanNumber;

    @Override
    protected void initializeActivity()
    {
        isGettingStartedMode = true;
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        if (getIntent().hasExtra("_isEditAction")) {
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction", false);
        }
        if (getIntent().hasExtra("_id")) {
            Log.d(getBaseContext().getPackageName(), "Member id " + getIntent().getIntExtra("_id", 0) + getString(R.string.to_be_loaded));
            this.selectedMemberId = getIntent().getIntExtra("_id", 0);
        }
        setContentView(R.layout.activity_member_details_view_gettings_started_wizard);

        this.buildOccupationSpinner();

        // Set instructions
        TypefaceTextView lblAMInstruction = (TypefaceTextView) findViewById(R.id.lblAMInstruction);
        SpannableStringBuilder headingInstruction = new SpannableStringBuilder();
        SpannableStringBuilder plusText = new SpannableStringBuilder("+ ");
        SpannableStringBuilder nextText = new SpannableStringBuilder(getString(R.string.done));
        headingInstruction.append(getString(R.string.enter_each_member));
        headingInstruction.append(plusText);
        headingInstruction.append(getString(R.string.or_press));
        headingInstruction.append(nextText);
        headingInstruction.append(getString(R.string.if_you_have_entered_all_members));

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);
        if (isEditAction) {

            actionBar.setTitle(getString(R.string.get_started_allcaps));
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
            );

            headingInstruction = new SpannableStringBuilder("");
        } else {
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

                            Intent i = new Intent(getApplicationContext(), GettingStartedWizardNewCycleActivity.class);
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

        txtOutstandingWelfareDueDate = (TextView) findViewById(R.id.txtOutstandingWelfareDueDate);

        txtNCGSWLoanNextRepaymentDate =  (TextView) findViewById(R.id.txtNCGSWLoanNextRepaymentDate);
        txtNCGSWLoanNumber =  (TextView) findViewById(R.id.txtNCGSWOutstandingLoanNumber);

        // Default to the next repayment date to a month from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);

        txtOutstandingWelfareDueDate.setText(Utils.formatDate(cal.getTime(), "dd-MM-yyyy"));

        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);

        txtOutstandingWelfareDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date nextOutstandingWelfareDueDate = Utils.stringToDate(txtOutstandingWelfareDueDate.getText().toString(), "dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextOutstandingWelfareDueDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingStartedWizardAddMemberActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_outstanding_welfare_due_date));
                datePickerDialog.show();
            }
        });


        txtNCGSWLoanNextRepaymentDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));

        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);


        txtNCGSWLoanNextRepaymentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // The Event Handler to handle both startDate and endDate
                Date nextRepaymentDate = Utils.stringToDate(txtNCGSWLoanNextRepaymentDate.getText().toString(), "dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextRepaymentDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingStartedWizardAddMemberActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_next_repayment_date));
                datePickerDialog.show();
            }
        });
        // actionBar.setTitle("New Member");
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.get_started_allcaps));

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

        lblAMInstruction.setText(Html.fromHtml(headingInstruction.toString()));
        cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
        clearDataFields();
        if (isEditAction) {
            selectedMember = ledgerLinkApplication.getMemberRepo().getMemberById(selectedMemberId);
            this.populateDataFields(selectedMember);
        } else {
            //Set the current stage of the wizard
            VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
            vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_ADD_MEMBER);
        }

        //To provide formatting for phone numbers
        final EditText txtAMPhoneNo = (EditText) findViewById(R.id.txtAMPhoneNo);
        Utils.setAsPhoneNumberInput(txtAMPhoneNo);
        //PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        //txtAMPhoneNo.addTextChangedListener(phoneNumberFormattingTextWatcher);

    }


    protected void updateDisplay() {
        if (viewClicked != null)
        {
            viewClicked.setText(String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth + 1) + "-" + mYear);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i;
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, GettingStartedWizardNewCycleActivity.class);
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
        if (selectedMember != null) {
            member = selectedMember;
        }
        if (validateData(member)) {
            boolean retVal = false;
            if (member.getMemberId() != 0) {
                retVal = ledgerLinkApplication.getMemberRepo().updateGettingStartedWizardMember(member);
            } else {
                retVal = ledgerLinkApplication.getMemberRepo().addGettingStartedWizardMember(member);
            }
            if (retVal) {
                if (!isEditAction) {
                    //Set this new entity as the selected one
                    //Due to this ensure empty fields are explicitly set to null or default value
                    //Otherwise they will assume the value of the selectedMember variable because it is not null
                    selectedMember = member;
                    if (selectedFinishButton) {
                        Toast toast = Toast.makeText(this, getString(R.string.new_member_added_successfully), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        Log.i(getBaseContext().getPackageName(), getString(R.string.going_to_start_rview_members_activity));
                        Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                        startActivity(i);
                        Utils._membersAccessedFromNewCycle = false;
                        Utils._membersAccessedFromEditCycle = false;
                        finish();
                    } else {
                        Toast toast = Toast.makeText(this, R.string.new_member_was_add_successfully_add_another_member, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        //Clear the Fields and keep adding new records
                        //Set member to null to ensure member id is removed from list of member ids
                        selectedMember = null;
                        clearDataFields();
                    }
                    selectedFinishButton = false;


                } else {
                    Toast.makeText(this, R.string.members_updated_successfully, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                    startActivity(i);
                    finish();
                }
                successFlg = true;
                //clearDataFields(); //Not needed now
            } else {
                dlg = Utils.createAlertDialogOk(this, getString(R.string.add_member), getString(R.string.problem_occurred_while_adding_new_member), Utils.MSGBOX_ICON_TICK);
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
            // Validate: MemberNo
            Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
            String dlgTitle = getString(R.string.add_member);
            if (cboAMMemberNo.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.member_no_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMMemberNo.setFocusableInTouchMode(true);
                cboAMMemberNo.requestFocus();
                return false;
            } else {
                cboAMMemberNo.setFocusableInTouchMode(false);
                String memberNo = cboAMMemberNo.getSelectedItem().toString().trim();
                int theMemberNo = Integer.parseInt(memberNo);
                member.setMemberNo(theMemberNo);
            }

            //Validate: Surname
            TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
            String surname = txtSurname.getText().toString().trim();
            if (surname.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.surname_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtSurname.requestFocus();
                return false;
            } else {
                member.setSurname(surname);
            }

            //Validate: OtherNames
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
            String otherNames = txtOtherNames.getText().toString().trim();
            if (otherNames.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.at_least_one_other_name_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOtherNames.requestFocus();
                return false;
            } else {
                member.setOtherNames(otherNames);
            }

            //Validate: Gender
            //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
            Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
            if (cboGender.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.sex_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboGender.setFocusableInTouchMode(true);
                cboGender.requestFocus();
                return false;
            } else {
                cboGender.setFocusableInTouchMode(false);
                String gender = cboGender.getSelectedItem().toString().trim();
                member.setGender(gender);
            }

            // Validate: Age
            Spinner cboAge = (Spinner) findViewById(R.id.cboAMAge);
            if (cboAge.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.age_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAge.setFocusableInTouchMode(true);
                cboAge.requestFocus();
                return false;
            } else {
                cboAge.setFocusableInTouchMode(false);
                String age = cboAge.getSelectedItem().toString().trim();
                Integer theAge = Integer.parseInt(age);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -theAge);
                member.setDateOfBirth(c.getTime());
            }

            //Validate: Occupation

            if(this.cboAMOccupation.getSelectedItemPosition() == 0){
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.occupation_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                this.cboAMOccupation.setFocusable(true);
                this.cboAMOccupation.requestFocus();
                return false;
            }
            cboAMOccupation.setFocusableInTouchMode(false);
            String occupation = this.cboAMOccupation.getSelectedItem().toString().trim();
            member.setOccupation(occupation);

            //Validate: PhoneNumber
            TextView txtPhoneNo = (TextView) findViewById(R.id.txtAMPhoneNo);
            String phoneNo = txtPhoneNo.getText().toString().trim();
            if (phoneNo.length() < 1) {
                //Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Phone Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtPhoneNo.requestFocus();
                //return false;
                member.setPhoneNumber(null);
            } else {
                member.setPhoneNumber(phoneNo.replaceAll(" ", "")); //remove smart formattings
            }

            // Validate: Cycles Completed
            Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
            if (cboAMCycles.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.cycles_completed_field_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMCycles.setFocusableInTouchMode(true);
                cboAMCycles.requestFocus();
                return false;
            } else {
                cboAMCycles.setFocusableInTouchMode(false);
                String cycles = cboAMCycles.getSelectedItem().toString().trim();
                Integer theCycles = Integer.parseInt(cycles);
                if (theCycles > 100) {
                    Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.no_of_completed_cycle_too_high), Utils.MSGBOX_ICON_EXCLAMATION).show();
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
            if (!ledgerLinkApplication.getMemberRepo().isMemberNoAvailable(member.getMemberNo(), member.getMemberId())) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.another_member_using_this_member_no), Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMMemberNo.requestFocus();
                return false;
            }


            //Validation specific to getting started wizard
            //Validate Amount of savings for this member
            EditText txtSavingsSoFar = (EditText) findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
            String savings = txtSavingsSoFar.getText().toString().trim();
            if (savings.length() < 1) {
//                displayMessageBox(dlgTitle, "Total Amount this Member has Saved in Current Cycle so far is Required", Utils.MSGBOX_ICON_EXCLAMATION);
//                txtSavingsSoFar.requestFocus();
//                return false;
                member.setSavingsOnSetup(0);
            } else {
                double amountSavedSoFar = Double.parseDouble(savings);
                if (amountSavedSoFar < 0.00) {
                    displayMessageBox(dlgTitle, getString(R.string.total_amount_member_has_saved_in_current_cycle_so_far));
                    txtSavingsSoFar.requestFocus();
                    return false;
                } else {
                    member.setSavingsOnSetup(amountSavedSoFar);
                }
            }

            EditText txtWelfareAmount = (EditText) findViewById(R.id.txtAMMWelfareCorrection);
            String welfareAmount = txtWelfareAmount.getText().toString().trim();
            if(welfareAmount.length() < 1){
                member.setWelfareOnSetup(0);
            }else{
                double totalWelfareAmount = Double.parseDouble(welfareAmount);
                if(totalWelfareAmount < 0.00){
                    displayMessageBox(dlgTitle, getString(R.string.total_amount_of_this_member_welfare_be_zero_and_above));
                    txtWelfareAmount.requestFocus();
                    return false;
                }else{
                    member.setWelfareOnSetup(totalWelfareAmount);
                }
            }

            EditText txtOutstandingWelfareAmount = (EditText) findViewById(R.id.txtAMMOutstandingWelfareCorrection);
            String outstandingWelfareAmount = txtOutstandingWelfareAmount.getText().toString().trim();
            if(outstandingWelfareAmount.length() < 1){
                member.setOutstandingWelfareOnSetup(0);
            }else{
                double totalOutstandingWelfare = Double.parseDouble(outstandingWelfareAmount);
                if(totalOutstandingWelfare < 0.00){
                    displayMessageBox(dlgTitle, getString(R.string.total_amount_of_this_member_welfare_be_zero_and_above));
                    txtOutstandingWelfareAmount.requestFocus();
                    return false;
                }else{
                    member.setOutstandingWelfareOnSetup(totalOutstandingWelfare);
                    if(totalOutstandingWelfare > 0 && txtOutstandingWelfareDueDate.getText().length() == 0){
                        displayMessageBox(dlgTitle, getString(R.string.outstanding_welfare_due_date_required));
                        return false;
                    }
                    member.setOutstandingWelfareDueDateOnSetup(Utils.getDateFromString(txtOutstandingWelfareDueDate.getText().toString(), "dd-MM-yyyy"));
                }
            }


            //Validate Amount of Loan outstanding for this member
            EditText txtLoanAmount = (EditText) findViewById(R.id.txtMDVOutstandingLoanAmount);
            String loanAmount = txtLoanAmount.getText().toString().trim();
            if (loanAmount.length() < 1) {
//                displayMessageBox(dlgTitle, "Total Amount of this Member's Regular Loan Outstanding is Required", Utils.MSGBOX_ICON_EXCLAMATION);
//                txtLoanAmount.requestFocus();
//                return false;
                member.setOutstandingLoanOnSetup(0);
            } else {
                double outstandingLoan = Double.parseDouble(loanAmount);
                if (outstandingLoan < 0.00) {
                    displayMessageBox(dlgTitle, getString(R.string.total_amount_of_member_regular_loan_outstanding_be_zero_and_above));
                    txtLoanAmount.requestFocus();
                    return false;
                } else {
                    member.setOutstandingLoanOnSetup(outstandingLoan);

                    //set the date of next repayment
                    if(outstandingLoan > 0 && txtNCGSWLoanNextRepaymentDate.getText().length()==0)
                    {
                        displayMessageBox(dlgTitle, getString(R.string.next_repayment_date_required_for_outstanding_loan));
                        txtNCGSWLoanNextRepaymentDate.requestFocus();
                        return false;

                    }
                    else {
                        member.setDateOfFirstRepayment(Utils.getDateFromString(txtNCGSWLoanNextRepaymentDate.getText().toString(), "dd-MM-yyyy"));
                    }

                    //set the loan number
                    if(outstandingLoan > 0.00 && txtNCGSWLoanNumber.getText().length()==0)
                    {
                        displayMessageBox(dlgTitle, getString(R.string.loan_member_required_for_outstanding_loan));
                        txtNCGSWLoanNumber.requestFocus();
                        return false;
                    }
                    int loanNo = Integer.valueOf(txtNCGSWLoanNumber.getText().toString().trim());
                    if(!this.validateLoanNumber(member, loanNo, outstandingLoan))
                        return false;

                    member.setOutstandingLoanNumberOnSetup(loanNo);
                }
            }

            /**
             int amountSavedSoFar = 0;
             int outstandingLoan = 0;
             TextView txtSavingsSoFar = (TextView) findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
             String savings = txtSavingsSoFar.getText().toString().trim();
             amountSavedSoFar = Integer.parseInt(savings);
             member.setSavingsOnSetup(amountSavedSoFar);
             TextView txtLoanAmount = (TextView) findViewById(R.id.txtMDVOutstandingLoanAmount);
             String loanAmount = txtLoanAmount.getText().toString().trim();
             outstandingLoan = Integer.parseInt(loanAmount);
             member.setOutstandingLoanOnSetup(outstandingLoan); */
            return true;

        } catch (
                Exception ex
                )
        {
            ex.printStackTrace();
            return false;
        }

    }

    @Override
    protected boolean validateLoanNumber(Member member, int loanNo, double outstandingLoan){
        return super.validateLoanNumber(member, loanNo, outstandingLoan);
    }

    protected void displayMessageBox(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(GettingStartedWizardAddMemberActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (Utils.MSGBOX_ICON_EXCLAMATION.equalsIgnoreCase(Utils.MSGBOX_ICON_EXCLAMATION)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (Utils.MSGBOX_ICON_EXCLAMATION.equalsIgnoreCase(Utils.MSGBOX_ICON_TICK)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (Utils.MSGBOX_ICON_EXCLAMATION.equalsIgnoreCase(Utils.MSGBOX_ICON_QUESTION)) {
            alertDialog.setIcon(R.drawable.phone);
        }

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                //Can I pass a method delegate? Or function pointer? for what to be executed?
                // Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                if (successAlertDialogShown) {
                    Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                    startActivity(i);
                    successAlertDialogShown = false;
                }
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void populateDataFields(Member member) {

        super.populateDataFields(member);

        TextView txtSavingsSoFar = (TextView) findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
        txtSavingsSoFar.setText(String.format("%.0f", member.getSavingsOnSetup()));
        TextView txtLoanAmount = (TextView) findViewById(R.id.txtMDVOutstandingLoanAmount);
        txtLoanAmount.setText(String.format("%.0f", member.getOutstandingLoanOnSetup()));
        TextView txtLoanNumber = (TextView) findViewById(R.id.txtNCGSWOutstandingLoanNumber);
        txtLoanNumber.setText(String.valueOf(member.getOutstandingLoanNumberOnSetup()));

        EditText txtAMMWelfareCorrection = (EditText) findViewById(R.id.txtAMMWelfareCorrection);
        txtAMMWelfareCorrection.setText(String.format("%.0f",member.getWelfareOnSetup()));

        EditText txtOutstandingWelfareAmount = (EditText) findViewById(R.id.txtAMMOutstandingWelfareCorrection);
        txtOutstandingWelfareAmount.setText(String.format("%.0f", member.getOutstandingWelfareOnSetup()));


        //populate the next repayment date
        if(member.getDateOfFirstRepayment() != null) {
            txtNCGSWLoanNextRepaymentDate.setText(Utils.formatDate(member.getDateOfFirstRepayment(), "dd-MMM-yyyy"));
        }
        else {
            txtNCGSWLoanNextRepaymentDate.setText(getString(R.string.none_main));
            txtNCGSWLoanNextRepaymentDate.setTextColor(getResources().getColor(R.color.ledger_link_light_blue));
        }

    }

    @Override
    protected void clearDataFields() {

        super.clearDataFields();

        // Clear GSW values
        TextView txtSavingsSoFar = (TextView) findViewById(R.id.txtMDVAmountSavedInCurrentCycle);
        txtSavingsSoFar.setText(null);
        TextView txtLoanAmount = (TextView) findViewById(R.id.txtMDVOutstandingLoanAmount);
        txtLoanAmount.setText(null);
        TextView txtLoanNumber = (TextView) findViewById(R.id.txtNCGSWOutstandingLoanNumber);
        txtLoanNumber.setText(null);

    }

}