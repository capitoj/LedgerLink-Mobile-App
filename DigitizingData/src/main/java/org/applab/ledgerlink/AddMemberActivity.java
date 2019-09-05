package org.applab.ledgerlink;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.apache.commons.lang3.ArrayUtils;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.adapters.DropDownAdapter;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/15/13.
 */
public class AddMemberActivity extends ActionBarActivity{
    protected Member selectedMember;
    protected int selectedMemberId;
    protected boolean successAlertDialogShown = false;
    protected boolean selectedFinishButton = false;
    protected int meetingId;
    protected boolean isEditAction;

    protected TextView viewClicked;
    protected int mYear;
    protected int mMonth;
    protected int mDay;

    protected Spinner cboAMMemberNo;
    protected TextView txtSurname;
    protected TextView txtOtherNames;
    protected Spinner cboGender;
    protected Spinner cboAMAge;
    protected Spinner cboAMOccupation;
    protected TextView txtPhoneNo;
    protected Spinner cboAMCycles;
    protected TextView txtSavingsSoFar;
    protected TextView txtWelfareSoFar;
    protected TextView txtLoanAmount;
    protected TextView txtAMMLoanNextRepaymentDate;
    protected TextView txtAMMLoanNumber;
    protected TextView txtOutstandingWelfareDueDate;
    protected boolean isGettingStartedMode = false; //flags whether we are in wizard mode

    protected LedgerLinkApplication ledgerLinkApplication;

    //Event that is raised when the date has been set
    protected final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        initializeActivity();
    }

    //This method initializes this activity
    //It is overiden in GSW Add member activity so as to load the relevant layout
    protected void initializeActivity() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }
        if (getIntent().hasExtra("_id")) {
            this.selectedMemberId = getIntent().getIntExtra("_id", 0);
        }
        if (getIntent().hasExtra("_isEditAction")) {
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction", false);
        }

        MeetingRepo meetingRepo = new MeetingRepo(getApplicationContext());
        Meeting targetMeeting = meetingRepo.getMeetingById(meetingId);
        inflateCustomActionBar();
        // END_INCLUDE (inflate_set_custom_view)
        //if in getting started wizard.. use the getting started layout
        //else use the default layout
        setContentView(R.layout.activity_add_member);

        txtAMMLoanNextRepaymentDate = (TextView) findViewById(R.id.txtAMMLoanNextRepaymentDate);
        txtAMMLoanNumber = (TextView) findViewById(R.id.txtAMMOutstandingLoanNumber);

        txtSurname  = (TextView) findViewById(R.id.txtAMSurname);

        txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);

        this.buildOccupationSpinner();

        txtPhoneNo = (TextView) findViewById(R.id.txtAMPhoneNo);

        cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);

        txtSavingsSoFar = (TextView) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);

        txtWelfareSoFar = (TextView) findViewById(R.id.txtAMMWelfareCorrection);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);

        txtOutstandingWelfareDueDate = (TextView) findViewById(R.id.txtOutstandingWelfareDueDate);
        txtOutstandingWelfareDueDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddMemberActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_outstanding_welfare_due_date));
                datePickerDialog.show();
            }
        });

        txtLoanAmount = (TextView) findViewById(R.id.txtAMMiddleCycleLoansCorrection);

        clearDataFields();
        if (isEditAction) {
            selectedMember = ledgerLinkApplication.getMemberRepo().getMemberById(selectedMemberId);
            populateDataFields(selectedMember);
        }

        // For Middle start details
        if (!isEditAction) {

            // Hide comments & set heading
            TextView lblAMMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblAMMiddleCycleInformationHeading);
            lblAMMiddleCycleInformationHeading.setText(R.string.if_cycle_has_started_and_your_group_has_held_one_or_more_meetings_already);

            TextView lblAMMiddleCycleSavingsCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleSavingsCorrectionLabel);
            lblAMMiddleCycleSavingsCorrectionLabel.setVisibility(View.GONE);

            TextView lblAMMiddleCycleSavings = (TextView) findViewById(R.id.lblAMMiddleCycleSavings);
            lblAMMiddleCycleSavings.setVisibility(View.GONE);
//            EditText txtAMMiddleCycleSavingsCorrection = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);
//            txtAMMiddleCycleSavingsCorrection.setVisibility(View.GONE);

            TextView lblAMMiddleCycleSavingCorrectionCommentLabel = (TextView) findViewById(R.id.lblAMMiddleCycleSavingCorrectionCommentLabel);
            lblAMMiddleCycleSavingCorrectionCommentLabel.setVisibility(View.GONE);

            EditText txtAMMiddleCycleSavingsCorrectionComment = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrectionComment);
            txtAMMiddleCycleSavingsCorrectionComment.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoansCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleLoansCorrectionLabel);
            lblAMMiddleCycleLoansCorrectionLabel.setVisibility(View.GONE);

            EditText txtAMMiddleCycleLoansCorrectionComment = (EditText) findViewById(R.id.txtAMMiddleCycleLoansCorrectionComment);
            txtAMMiddleCycleLoansCorrectionComment.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoansCorrectionCommentLabel = (TextView) findViewById(R.id.lblAMMiddleCycleLoansCorrectionCommentLabel);
            lblAMMiddleCycleLoansCorrectionCommentLabel.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoans = (TextView) findViewById(R.id.lblAMMiddleCycleLoans);
            lblAMMiddleCycleLoans.setVisibility(View.GONE);

            TextView lblAMMiddleCycleWelfare = (TextView) findViewById(R.id.lblAMMiddleCycleWelfare);
            lblAMMiddleCycleWelfare.setVisibility(View.GONE);

            TextView lblAMMiddleCycleWelfareCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleWelfareCorrectionLabel);
            lblAMMiddleCycleWelfareCorrectionLabel.setVisibility(View.GONE);

            TextView lblAMMiddleCycleWelfareCorrectionCommentLabel = (TextView) findViewById(R.id.lblAMMiddleCycleWelfareCorrectionCommentLabel);
            lblAMMiddleCycleWelfareCorrectionCommentLabel.setVisibility(View.GONE);

            EditText txtAMMiddleCycleWelfareCorrectionComment = (EditText) findViewById(R.id.txtAMMiddleCycleWelfareCorrectionComment);
            txtAMMiddleCycleWelfareCorrectionComment.setVisibility(View.GONE);


            //Default next repayment date to a month from now

            txtAMMLoanNextRepaymentDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));

            mYear = cal.get(Calendar.YEAR);
            mMonth = cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);


        }

        txtAMMLoanNextRepaymentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // The Event Handler should handle both startDate and endDate
                Date nextRepaymentDate = Utils.stringToDate(txtAMMLoanNextRepaymentDate.getText().toString(), "dd-MMM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextRepaymentDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddMemberActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle(getString(R.string.set_next_payment_date));
                datePickerDialog.show();
            }
        });

        //To provide formatting for phone numbers
        final EditText txtAMPhoneNo = (EditText) findViewById(R.id.txtAMPhoneNo);
        Utils.setAsPhoneNumberInput(txtAMPhoneNo);
    }

    protected void showMiddleStartCycleValues(Member member) {
        //loads the Middle start cycle values for this member

        if (isGettingStartedMode) return; //this code shouldnt run in GSW mode hence this check

        //Load the middle start values
        TextView lblAMMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblAMMiddleCycleInformationHeading);
        TextView lblAMMiddleCycleSavings = (TextView) findViewById(R.id.lblAMMiddleCycleSavings);
        TextView lblAMMiddleCycleLoans = (TextView) findViewById(R.id.lblAMMiddleCycleLoans);
        TextView lblAMMiddleCycleWelfare = (TextView) findViewById(R.id.lblAMMiddleCycleWelfare);
        TextView lblAMMiddleCycleOutstandingWelfare = (TextView) findViewById(R.id.lblAMMiddleCycleOutstandingWelfare);

        txtAMMLoanNumber.setText(String.valueOf(member.getOutstandingLoanNumberOnSetup()));

        lblAMMiddleCycleSavings.setText(String.format("%,.0f %s", member.getSavingsOnSetup(), getResources().getString(R.string.operating_currency)));
        lblAMMiddleCycleLoans.setText(String.format("%,.0f %s", member.getOutstandingLoanOnSetup(), getResources().getString(R.string.operating_currency)));
        lblAMMiddleCycleWelfare.setText(String.format("%,.0f %s", member.getWelfareOnSetup(), getResources().getString(R.string.operating_currency)));
        lblAMMiddleCycleOutstandingWelfare.setText(String.format("%,.0f %s", member.getOutstandingWelfareOnSetup(), getResources().getString(R.string.operating_currency)));

        if(member.getOutstandingWelfareDueDateOnSetup() != null){
            txtOutstandingWelfareDueDate.setText(Utils.formatDate(member.getOutstandingWelfareDueDateOnSetup(), "dd-MM-yyyy"));
        }else{
            txtOutstandingWelfareDueDate.setText(R.string.none_main);
            txtAMMLoanNextRepaymentDate.setTextColor(getResources().getColor(R.color.ledger_link_light_blue));
        }

        // populate the next repayment date
        if (member.getDateOfFirstRepayment() != null) {
            txtAMMLoanNextRepaymentDate.setText(Utils.formatDate(member.getDateOfFirstRepayment(), "dd-MM-yyyy"));
        } else {
            txtAMMLoanNextRepaymentDate.setText(R.string.none_main);
            txtAMMLoanNextRepaymentDate.setTextColor(getResources().getColor(R.color.ledger_link_light_blue));
        }

        // Process comments if any
        EditText txtAMMiddleCycleSavingsComment = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrectionComment);
        if (null != member.getSavingsOnSetupCorrectionComment()) {
            if (!"".equals(member.getSavingsOnSetupCorrectionComment())) {
                txtAMMiddleCycleSavingsComment.setText(member.getSavingsOnSetupCorrectionComment());
            }
        }

        EditText txtAMMiddleCycleLoansComment = (EditText) findViewById(R.id.txtAMMiddleCycleLoansCorrectionComment);
        if (null != member.getOutstandingLoanOnSetupCorrectionComment()) {
            if (!"".equals(member.getOutstandingLoanOnSetupCorrectionComment())) {
                txtAMMiddleCycleLoansComment.setText(member.getOutstandingLoanOnSetupCorrectionComment());
            }
        }

        //Show the heading
        //Get the date of the dummy GSW meeting
        MeetingRepo meetingRepo = new MeetingRepo(getBaseContext());

        String pronoun = member.getGender().startsWith("F") || member.getGender().startsWith("f") ? getString(R.string.her) : getString(R.string.his);
        lblAMMiddleCycleInformationHeading.setText(getString(R.string.member_info_added_after_cycle_started) + pronoun + getString(R.string.total_saving_and_outstanding_loans_that_day));

        Meeting dummyGSWMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();
        if (dummyGSWMeeting != null) {
            lblAMMiddleCycleInformationHeading.setText(getString(R.string.member_info_was_added_on) + Utils.formatDate(dummyGSWMeeting.getMeetingDate(), getString(R.string.date_format)) + getString(R.string.after_cycle_started_here_are) + pronoun + getString(R.string.total_savind_and_outstanding_loans_that_day));
        }

        /** if meeting is after 2nd meeting, disable editing outstanding balance*/
        if(meetingRepo.getAllNonGSWMeetings().size()>2){
            TextView lblAMMiddleCycleSavingsCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleSavingsCorrectionLabel);
            lblAMMiddleCycleSavingsCorrectionLabel.setVisibility(View.GONE);
            LinearLayout linearLayout = (LinearLayout)txtAMMiddleCycleLoansComment.getParent();
            linearLayout.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoansCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleLoansCorrectionLabel);
            lblAMMiddleCycleLoansCorrectionLabel.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);

        }

    }

    protected void inflateCustomActionBar() {

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView;
        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        if (isEditAction) {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedFinishButton = true;
                            if (saveMemberData()) {
                                finish();
                            }
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }
            );

            actionBar.setTitle(R.string.edit_member_main);
        } else {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_next_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedFinishButton = true;
                            saveMemberData();
                            //finish();
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveMemberData();
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            finish();
                        }
                    }
            );


            actionBar.setTitle(R.string.new_member_main);
        }

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        AlertDialog dlg;

        Member member = new Member();
        if (selectedMember != null) {
            member = selectedMember;
        }

        if (validateData(member)) {
            boolean retVal;
            if (member.getMemberId() != 0) {
                retVal = ledgerLinkApplication.getMemberRepo().updateMember(member);
            } else {
                retVal = ledgerLinkApplication.getMemberRepo().addMember(member);

            }
            if (retVal) {
                if (member.getMemberId() == 0) {
                    //Set this new entity as the selected one
                    //Due to this ensure empty fields are explicitly set to null or default value
                    //Otherwise they will assume the value of the selectedMember variable because it is not null
                    selectedMember = member;

                    if (selectedFinishButton) {
                        Toast toast = Toast.makeText(this, R.string.new_member_added_successfully, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        if (Utils._membersAccessedFromNewCycle) {
                            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                            i.putExtra("_isUpdateCycleAction", false);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else if (Utils._membersAccessedFromEditCycle) {
                            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                            i.putExtra("_isUpdateCycleAction", true);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        } else {
                            Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                        Utils._membersAccessedFromNewCycle = false;
                        Utils._membersAccessedFromEditCycle = false;
                    } else {
                        Toast toast = Toast.makeText(this, R.string.new_member_added_successfully_add_another, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
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
                } else {
                    Toast.makeText(this, R.string.member_updated_successfully, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            } else {
                dlg = Utils.createAlertDialogOk(AddMemberActivity.this, getString(R.string.add_member_main), getString(R.string.problem_occurred_while_adding_new_member), Utils.MSGBOX_ICON_TICK);
                dlg.show();
            }
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    protected boolean validateData(Member member) {
        String dlgTitle = getString(R.string.add_member_main);
        if(cboAMMemberNo.getSelectedItemPosition() < 1){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.member_no_required_main), Utils.MSGBOX_ICON_EXCLAMATION).show();
            cboAMMemberNo.setFocusableInTouchMode(true);
            cboAMMemberNo.requestFocus();
            return false;
        }
        cboAMMemberNo.setFocusableInTouchMode(false);
        String memberNo = cboAMMemberNo.getSelectedItem().toString().trim();
        int theMemberNo = Integer.parseInt(memberNo);
        member.setMemberNo(theMemberNo);

        String surname = txtSurname.getText().toString().trim();
        if(surname.length() < 1){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.surname_required_main), Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtSurname.requestFocus();
            return false;
        }
        member.setSurname(surname);

        String otherNames = txtOtherNames.getText().toString().trim();
        if(otherNames.length() < 1){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.one_other_required_main), Utils.MSGBOX_ICON_EXCLAMATION).show();
            txtOtherNames.requestFocus();
            return false;
        }
        member.setOtherNames(otherNames);

        if (cboGender.getSelectedItemPosition() < 1) {
            cboGender.setFocusableInTouchMode(true);
            cboGender.requestFocus();
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.sex_required_main), Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }
        cboGender.setFocusableInTouchMode(false);
        String gender = cboGender.getSelectedItem().toString().trim();
        member.setGender(gender);

        if(cboAMAge.getSelectedItemPosition() == 0){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.age_required_main), Utils.MSGBOX_ICON_EXCLAMATION).show();
            cboAMAge.setFocusableInTouchMode(true);
            cboAMAge.requestFocus();
            return false;
        }
        cboAMAge.setFocusableInTouchMode(false);
        String age = cboAMAge.getSelectedItem().toString().trim();
        Integer theAge = Integer.parseInt(age);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -theAge);
        member.setDateOfBirth(c.getTime());

        if(cboAMOccupation.getSelectedItemPosition() == 0){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.occupation_required_main), Utils.MSGBOX_ICON_EXCLAMATION).show();
            cboAMOccupation.setFocusableInTouchMode(true);
            cboAMOccupation.requestFocus();
            return false;
        }
        cboAMOccupation.setFocusableInTouchMode(false);
        String occupation = cboAMOccupation.getSelectedItem().toString().trim();
        member.setOccupation(occupation);

        String phoneNo = txtPhoneNo.getText().toString().trim();
        if(phoneNo.length() < 1){
            member.setPhoneNumber(null);
        }else{
            member.setPhoneNumber(phoneNo.replaceAll(" ", "")); //remove smart formattings
        }

        if(cboAMCycles.getSelectedItemPosition() == 0){
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.cycles_completed_field_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
            cboAMCycles.setFocusableInTouchMode(true);
            cboAMCycles.requestFocus();
            return false;
        }
        String cycles = cboAMCycles.getSelectedItem().toString().trim();
        Integer theCycles = Integer.parseInt(cycles);
        if (theCycles > 100) {
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.number_of_completed_cycles_too_high), Utils.MSGBOX_ICON_EXCLAMATION).show();
            cboAMCycles.setFocusableInTouchMode(true);
            cboAMCycles.requestFocus();
            return false;
        }
        cboAMCycles.setFocusableInTouchMode(false);
        member.setCyclesCompleted(theCycles);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -theCycles);
        member.setDateOfAdmission(calendar.getTime());

        String savings = txtSavingsSoFar.getText().toString().trim();
        if(savings.length() < 1){
            if (isEditAction) {
                member.setSavingsOnSetup(member.getSavingsOnSetup());
            } else {
                member.setSavingsOnSetup(0);
            }
        }else{
            double amountSavedSoFar = Double.parseDouble(savings);
            if (amountSavedSoFar < 0.00) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.total_amount_member_has_saved_in_current_cycle_so_far), Utils.MSGBOX_ICON_EXCLAMATION);
                txtSavingsSoFar.requestFocus();
                return false;
            } else {
                member.setSavingsOnSetup(amountSavedSoFar);
            }
        }

        String welfare = txtWelfareSoFar.getText().toString().trim();
        if(welfare.length() < 1){
            if(isEditAction){
                member.setWelfareOnSetup(member.getWelfareOnSetup());
            }else{
                member.setWelfareOnSetup(0);
            }
        }else{
            try{
                double welfareSavedSoFar = Double.parseDouble(welfare);
                member.setWelfareOnSetup(welfareSavedSoFar);
            }catch (Exception e){
                Log.e(getString(R.string.welfare_on_setup), e.getMessage());
                member.setWelfareOnSetup(0);
            }
        }

        EditText txtOutstandingWelfareAmount = (EditText) findViewById(R.id.txtAMMOutstandingWelfareCorrection);
        String outstandingWelfareAmount = txtOutstandingWelfareAmount.getText().toString().trim();
        if(outstandingWelfareAmount.length() < 1){
            member.setOutstandingWelfareOnSetup(0);
        }else{
            double totalOutstandingWelfare = Double.parseDouble(outstandingWelfareAmount);
            if(totalOutstandingWelfare < 0.00){
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.total_amount_member_outstanding_welfare_be_zeor_and_above), Utils.MSGBOX_ICON_EXCLAMATION);
                txtOutstandingWelfareAmount.requestFocus();
                return false;
            }else{
                member.setOutstandingWelfareOnSetup(totalOutstandingWelfare);
                if(totalOutstandingWelfare > 0 && txtOutstandingWelfareDueDate.getText().length() == 0){
                    Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.outstanding_welfare_due_date_required), Utils.MSGBOX_ICON_EXCLAMATION);
                    return false;
                }else{
                    member.setOutstandingWelfareDueDateOnSetup(Utils.getDateFromString(txtOutstandingWelfareDueDate.getText().toString(), "dd-MM-yyyy"));
                }
            }
        }

        String loanAmount = txtLoanAmount.getText().toString().trim();
        if(loanAmount.length() < 1){
            if (isEditAction) {
                member.setOutstandingLoanOnSetup(member.getOutstandingLoanOnSetup());
                String loanNumber = txtAMMLoanNumber.getText().toString().trim();
                if(loanNumber.length() < 1){
                    txtAMMLoanNumber.setText(String.valueOf(member.getOutstandingLoanNumberOnSetup()));
                }
                loanNumber = txtAMMLoanNumber.getText().toString().trim();
                int loanNo = Integer.valueOf(loanNumber);
                boolean status = this.validateLoanNumber(member, loanNo, member.getOutstandingLoanOnSetup());
                if(!status){
                    return false;
                }
            } else {
                member.setOutstandingLoanOnSetup(0);
            }
        }else{
            double outstandingLoan = Double.parseDouble(loanAmount);
            if (outstandingLoan < 0.00){
                DialogMessageBox.show(this, dlgTitle, getString(R.string.total_amount_member_regular_loan_outstanding_be_zero_and_above));
                txtLoanAmount.requestFocus();
            }else{
                member.setOutstandingLoanOnSetup(outstandingLoan);

                //set the date of next repayment
                if (outstandingLoan > 0 && txtAMMLoanNextRepaymentDate.getText().length() == 0) {
                    DialogMessageBox.show(this, dlgTitle, getString(R.string.next_repayment_date_required_for_outstanding_loan));
                    txtAMMLoanNextRepaymentDate.requestFocus();
                    return false;
                }

                //set the loan number
                if (outstandingLoan > 0 && txtAMMLoanNumber.getText().length() == 0) {
                    DialogMessageBox.show(this, dlgTitle, getString(R.string.loan_number_required_for_outstanding_loan));
                    txtAMMLoanNumber.requestFocus();
                    return false;
                }else{
                    String loanNumber = txtAMMLoanNumber.getText().toString();
                    int loanNo = Integer.valueOf(loanNumber);
                    if (!this.validateLoanNumber(member, loanNo, outstandingLoan))
                        return false;
                }
            }
        }


        member.setOutstandingLoanNumberOnSetup(Utils.getAsNumberOrZeroIfNull(txtAMMLoanNumber.getText().toString().trim()));
        member.setDateOfFirstRepayment(Utils.getDateFromString(txtAMMLoanNextRepaymentDate.getText().toString(), "dd-MMM-yyyy"));

        validateMiddleCycleValues(member);
        //Final Verifications
        //TODO: Trying to use Application context to ensure dialog box does not disappear
        if (!ledgerLinkApplication.getMemberRepo().isMemberNoAvailable(member.getMemberNo(), member.getMemberId())) {
            Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.another_member_using_this_member_no), Utils.MSGBOX_ICON_EXCLAMATION).show();
            cboAMMemberNo.requestFocus();
            return false;
        }

        return true;
    }

    protected boolean validateLoanNumber(Member member, int loanNo, double outstandingLoan){
        String dlgTitle = getString(R.string.loan_error);
        VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(this);
        VslaCycle vslaCycle = vslaCycleRepo.getCurrentCycle();
        if(vslaCycle != null) {
            if (loanNo < 1 && outstandingLoan > 0) {
                DialogMessageBox.show(this, dlgTitle, getString(R.string.loan_no_cannot_be_zero));
                txtAMMLoanNumber.requestFocus();
                return false;
            }

            boolean hasLoanNo = MeetingLoanIssuedRepo.hasLoanNumber(this, vslaCycle.getCycleId(), loanNo);
            if (hasLoanNo) {
                if (outstandingLoan > 0.00 || outstandingLoan < 1) {
                    if (isEditAction) {
                        if (loanNo != member.getOutstandingLoanNumberOnSetup()) {
                            DialogMessageBox.show(this, dlgTitle, getString(R.string.loan_no) + loanNo + getString(R.string.already_exist));
                            txtAMMLoanNumber.requestFocus();
                            return false;
                        }
                    } else {
                        DialogMessageBox.show(this, dlgTitle, getString(R.string.loan_no) + loanNo + getString(R.string.already_exist));
                        txtAMMLoanNumber.requestFocus();
                        return false;
                    }
                }
            }
        }
        return true;
    }



    // TODO: Resolve Redundancy
    protected void validateMiddleCycleValues(Member member) {
        //If edit mode and not GSW, validate middle cycle start values
        if (!isGettingStartedMode) {
            //if there are corrections, set them

            TextView txtAMMiddleCycleSavingsCorrection = (TextView) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);
            TextView txtAMMiddleCycleLoansCorrection = (TextView) findViewById(R.id.txtAMMiddleCycleLoansCorrection);
            TextView txtAMMiddleCycleSavingsCorrectionComment = (TextView) findViewById(R.id.txtAMMiddleCycleSavingsCorrectionComment);
            EditText txtAMMiddleCycleWelfareCorrectionComment = (EditText) findViewById(R.id.txtAMMiddleCycleWelfareCorrectionComment);
            TextView txtAMMiddleCycleLoansCorrectionComment = (TextView) findViewById(R.id.txtAMMiddleCycleLoansCorrectionComment);

            if (txtAMMiddleCycleSavingsCorrection.getText().length() > 0) {
                member.setSavingsOnSetupCorrectionComment(txtAMMiddleCycleSavingsCorrection.getText().toString());
            }

            if (!txtAMMiddleCycleSavingsCorrectionComment.getText().toString().isEmpty()) {
                member.setSavingsOnSetupCorrectionComment(txtAMMiddleCycleSavingsCorrectionComment.getText().toString());
            }

            if(txtAMMiddleCycleWelfareCorrectionComment.getText().length() > 0 || !txtAMMiddleCycleWelfareCorrectionComment.getText().toString().isEmpty()){
                member.setWelfareOnSetupCorrectionComment(txtAMMiddleCycleWelfareCorrectionComment.getText().toString());
            }

            if (txtAMMiddleCycleLoansCorrection.getText().length() > 0) {
                member.setOutstandingLoanOnSetup(Double.parseDouble(txtAMMiddleCycleLoansCorrection.getText().toString()));
            }

            if (!txtAMMiddleCycleLoansCorrectionComment.getText().toString().isEmpty()) {
                Log.d("AMA", txtAMMiddleCycleLoansCorrectionComment.getText().toString());
                member.setOutstandingLoanOnSetupCorrectionComment(txtAMMiddleCycleLoansCorrectionComment.getText().toString());
            }

        }
    }

    protected void populateDataFields(final Member member) {
        try {

            clearDataFields();
            if (member == null) {
                return;
            }

            TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }

            Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
            if (member.getGender() != null) {
                if (member.getGender().startsWith("F") || member.getGender().startsWith("f")) {
                    cboGender.setSelection(2);
                } else if (member.getGender().startsWith("M") || member.getGender().startsWith("m")) {
                    cboGender.setSelection(1);
                }
            }

            if(member.getOccupation() != null){
                final Spinner cboAMOccupation = (Spinner) findViewById(R.id.cboAMOccupation);
                cboAMOccupation.post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.setSpinnerSelection(member.getOccupation(), cboAMOccupation);
                    }
                });
            }

            TextView txtPhone = (TextView) findViewById(R.id.txtAMPhoneNo);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(Utils.formatAsPhoneNumber(member.getPhoneNumber()));
            }

            // Set the age
            //cboAMAge = (Spinner) findViewById(R.id.cboAMAge);

            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            final int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);
            final Spinner cboAMAge = (Spinner) findViewById(R.id.cboAMAge);

            cboAMAge.post(new Runnable() {
                @Override
                public void run() {

                    Utils.setSpinnerSelection(computedAge + "", cboAMAge);
                }
            });

            calToday = Calendar.getInstance();

            // TODO: It may be preferable to allow this field to be editable if members are allowed to take leave
            // Set cycles
            final Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(member.getDateOfAdmission());
            final int cycles = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            cboAMCycles.post(new Runnable() {
                @Override
                public void run() {

                    Utils.setSpinnerSelection(cycles + "", cboAMCycles);
                }
            });

            //Load GSW values
            if (!isGettingStartedMode) {
                showMiddleStartCycleValues(member);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    protected void clearDataFields() {
        //Spinner items
        buildGenderSpinner();
        //This portion could take long so run it as long task
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                buildMemberNoSpinner();
            }
        };
        LongTaskRunner.runLongTask(runnable, getString(R.string.please_wait), getString(R.string.please_wait_amoment), AddMemberActivity.this);
        //buildMemberNoSpinner();

        buildAgeSpinner();
        buildCyclesCompletedSpinner();

        // Populate the Fields
        Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);

        TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
        txtOtherNames.setText(null);

        TextView txtPhone = (TextView) findViewById(R.id.txtAMPhoneNo);
        txtPhone.setText(null);

        /**TextView txtLoanNumber = (TextView) findViewById(R.id.txtAMMOutstandingLoanNumber);
         txtLoanNumber.setText(null); */

        cboAMMemberNo.requestFocus();
    }

    protected void updateDisplay() {
        if (viewClicked != null) {
            viewClicked.setText(String.format("%02d", mDay) + "-" + Utils.getMonthNameAbbrev(mMonth + 1) + "-" + mYear);
        }
    }


    protected void buildGenderSpinner() {

        //Setup the Spinner Items
        cboGender = (Spinner) findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{getString(R.string.select_sex), getString(R.string.male), getString(R.string.female)};
        ArrayAdapter<CharSequence> genderAdapter = new DropDownAdapter(this, genderList);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboGender.setAdapter(genderAdapter);
        cboGender.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });

        //Make the spinner selectable
        cboGender.setFocusable(true);
        cboGender.setClickable(true);
    }

    protected void buildOccupationSpinner(){
        cboAMOccupation = (Spinner) findViewById(R.id.cboAMOccupation);
        String[] professions = new String[]{getString(R.string.trader), getString(R.string.farmer)
                , getString(R.string.shopkeeper), getString(R.string.fish_monger), getString(R.string.soldier), getString(R.string.traditional_healer)
                , getString(R.string.welder), getString(R.string.medical_doctor), getString(R.string.student), getString(R.string.mason), getString(R.string.builder), getString(R.string.boda_rider), getString(R.string.driver), getString(R.string.teacher)
                , getString(R.string.pastor), getString(R.string.butcher), getString(R.string.painter), getString(R.string.nurse)
                , getString(R.string.barber), getString(R.string.hair_dresser), getString(R.string.other), getString(R.string.accountant), getString(R.string.engineer)
                , getString(R.string.police_officer), getString(R.string.security_guard), getString(R.string.banker), getString(R.string.architect)};
        Arrays.sort(professions);

        professions = ArrayUtils.addAll(new String[]{getString(R.string.select_profecssion)}, professions);

        ArrayAdapter<CharSequence> occupationAdapter = new DropDownAdapter(this, professions);
        occupationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMOccupation.setAdapter(occupationAdapter);
        cboAMOccupation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
        cboAMOccupation.setFocusable(true);
        cboAMOccupation.setClickable(true);
    }

    /* Populates the member no spinner with available member numbers */
    protected void buildMemberNoSpinner() {
        cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
        final ArrayList<String> memberNumberArrayList = new ArrayList<String>();
        memberNumberArrayList.add(getString(R.string.select_number));
        //If we have a selected member, then add the member number to the adapter
        if (selectedMember != null && selectedMember.getMemberNo() != 0) {
            memberNumberArrayList.add(selectedMember.getMemberNo() + "");
        }

        for (String mNo : ledgerLinkApplication.getMemberRepo().getListOfAvailableMemberNumbers()) {
            Log.d(getBaseContext().getPackageName(), getString(R.string.member_no_found) + mNo);
            memberNumberArrayList.add(mNo);
        }

        String[] memberNumberList = memberNumberArrayList.toArray(new String[memberNumberArrayList.size()]);
        memberNumberArrayList.toArray(memberNumberList);
        final ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, memberNumberList) {
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

        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                cboAMMemberNo.setAdapter(memberNoAdapter);
                // Populate the Fields
                if (selectedMember != null) {
                    cboAMMemberNo.post(new Runnable() {
                        @Override
                        public void run() {

                            Utils.setSpinnerSelection(selectedMember.getMemberNo() + "", cboAMMemberNo);
                        }
                    });
                }
            }
        };
        runOnUiThread(runnable);

        cboAMMemberNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
        //Make the spinner selectable
        cboAMMemberNo.setFocusable(true);
        cboAMMemberNo.setClickable(true);
    }

    /* Populates the member age spinner  */
    protected void buildAgeSpinner() {

        cboAMAge = (Spinner) findViewById(R.id.cboAMAge);
        ArrayList<String> ageArrayList = new ArrayList<String>();
        ageArrayList.add(getString(R.string.select_age));
        for (int i = 12; i <= 80; i++) {
            ageArrayList.add(i + "");
        }
        String[] ageList = ageArrayList.toArray(new String[ageArrayList.size()]);
        ageArrayList.toArray(ageList);
        ArrayAdapter<CharSequence> memberNoAdapter = new DropDownAdapter(this, ageList);
        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMAge.setAdapter(memberNoAdapter);
        cboAMAge.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
        //Make the spinner selectable
        cboAMAge.setFocusable(true);
        cboAMAge.setClickable(true);
    }

    /* Populates the member cycles completed spinner */
    private void buildCyclesCompletedSpinner() {

        cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
        ArrayList<String> cyclesArrayList = new ArrayList<String>();
        cyclesArrayList.add(getString(R.string.select_number));
        for (int i = 0; i <= 20; i++) {
            cyclesArrayList.add(i + "");
        }
        String[] cycleList = cyclesArrayList.toArray(new String[cyclesArrayList.size()]);
        cyclesArrayList.toArray(cycleList);
        ArrayAdapter<CharSequence> cyclesAdapter = new DropDownAdapter(this, cycleList);
        cyclesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMCycles.setAdapter(cyclesAdapter);

        // Make the spinner selectable
        cboAMCycles.setFocusable(true);
        cboAMCycles.setClickable(true);
        cboAMCycles.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
    }


}