package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

import java.util.Calendar;
import java.util.Date;




/**
 * Created by Moses on 7/15/13.
 */
public class GettingStartedWizardAddMemberActivity extends AddMemberActivity {
    private MemberRepo repo;
    private int selectedMemberId;
    private boolean successAlertDialogShown = false;
    private boolean selectedFinishButton = false;
    private AlertDialog alertDialog = null;
    private boolean isEditAction;

    private TextView viewClicked;
    private int mYear;
    private int mMonth;
    private int mDay;

    private TextView txtNCGSWLoanNextRepaymentDate;
    private TextView txtNCGSWLoanNumber;

    // Event that is raised when the date has been set
    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//
//
//        super.onCreate(savedInstanceState);
//        initializeActivity();
//    }

    @Override
    protected void initializeActivity()
    {
        isGettingStartedMode = true;
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
        SpannableStringBuilder nextText = new SpannableStringBuilder("done");
        headingInstruction.append("Enter each member. Save and add another member by pressing <b>");
        headingInstruction.append(plusText);
        headingInstruction.append("</b> or press <b>");
        headingInstruction.append(nextText);
        headingInstruction.append("</b> if you have entered all members.");

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        ActionBar actionBar = getSupportActionBar();
        if (isEditAction) {

            actionBar.setTitle("GET STARTED");
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
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

        txtNCGSWLoanNextRepaymentDate =  (TextView) findViewById(R.id.txtNCGSWLoanNextRepaymentDate);
        txtNCGSWLoanNumber =  (TextView) findViewById(R.id.txtNCGSWOutstandingLoanNumber);

        // Default to the next repayment date to a month from now
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        txtNCGSWLoanNextRepaymentDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));

        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH);
        mDay = cal.get(Calendar.DAY_OF_MONTH);


        txtNCGSWLoanNextRepaymentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // The Event Handler to handle both startDate and endDate
                Date nextRepaymentDate = Utils.stringToDate(txtNCGSWLoanNextRepaymentDate.getText().toString(), "dd-MMM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextRepaymentDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(GettingStartedWizardAddMemberActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set the next repayment date");
                datePickerDialog.show();
            }
        });
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

        lblAMInstruction.setText(Html.fromHtml(headingInstruction.toString()));
        cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
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

        //To provide formatting for phone numbers
        final EditText txtAMPhoneNo = (EditText) findViewById(R.id.txtAMPhoneNo);
        Utils.setAsPhoneNumberInput(txtAMPhoneNo);
        //PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        //txtAMPhoneNo.addTextChangedListener(phoneNumberFormattingTextWatcher);

    }




    protected void updateDisplay() {
        if (viewClicked != null)
        {
            viewClicked.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(String.format("%02d", mDay))
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)
                    .toString());
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

                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

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
            String dlgTitle = "Add Member";
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
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
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
                member.setPhoneNumber(phoneNo.replaceAll(" ", "")); //remove smart formattings
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
                    displayMessageBox(dlgTitle, "Total Amount this Member has Saved in Current Cycle so far should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtSavingsSoFar.requestFocus();
                    return false;
                } else {
                    member.setSavingsOnSetup(amountSavedSoFar);
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
                    displayMessageBox(dlgTitle, "Total Amount of this Member's Regular Loan Outstanding should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtLoanAmount.requestFocus();
                    return false;
                } else {
                    member.setOutstandingLoanOnSetup(outstandingLoan);

                    //set the date of next repayment
                    if(outstandingLoan > 0 && txtNCGSWLoanNextRepaymentDate.getText().length()==0)
                    {
                        displayMessageBox(dlgTitle, "The next repayment date is required for the outstanding loan", Utils.MSGBOX_ICON_EXCLAMATION);
                        txtNCGSWLoanNextRepaymentDate.requestFocus();
                        return false;

                    }
                    else {
                        member.setDateOfFirstRepayment(Utils.getDateFromString(txtNCGSWLoanNextRepaymentDate.getText().toString(), "dd-MMM-yyyy"));
                    }

                    //set the loan number
                    if(outstandingLoan > 0 && txtNCGSWLoanNumber.getText().length()==0)
                    {
                        displayMessageBox(dlgTitle, "The loan number is required for the outstanding loan", Utils.MSGBOX_ICON_EXCLAMATION);
                        txtNCGSWLoanNumber.requestFocus();
                        return false;
                    }
                    else {
                        member.setOutstandingLoanNumberOnSetup(Integer.valueOf(txtNCGSWLoanNumber.getText().toString().trim()));
                    }

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

    protected void displayMessageBox(String title, String message, String icon) {
        alertDialog = new AlertDialog.Builder(GettingStartedWizardAddMemberActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_EXCLAMATION)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_TICK)) {
            alertDialog.setIcon(R.drawable.phone);
        } else if (icon.equalsIgnoreCase(Utils.MSGBOX_ICON_QUESTION)) {
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


        //populate the next repayment date
        if(member.getDateOfFirstRepayment() != null) {
            txtNCGSWLoanNextRepaymentDate.setText(Utils.formatDate(member.getDateOfFirstRepayment(), "dd-MMM-yyyy"));
        }
        else {
            txtNCGSWLoanNextRepaymentDate.setText("none");
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