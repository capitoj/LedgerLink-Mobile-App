package org.applab.ledgerlink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MemberRepo;
import org.applab.ledgerlink.utils.DialogMessageBox;

import java.util.Calendar;

/**
 * Created by Moses on 6/26/13.
 */
public class
MemberDetailsViewActivity extends AppCompatActivity{
    private ActionBar actionBar;
    private int selectedMemberId = -1;
    private String selectedMemberNames = "VSLA Member";
    protected Context context;

    LedgerLinkApplication ledgerLinkApplication;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        context = this;

        inflateCustomActionBar();


        setContentView(R.layout.activity_member_details_view);
        // Check for Extras in case this call was to edit a pledge
        Bundle b = getIntent().getExtras();


        if (b != null) {

            selectedMemberId = b.getInt("_id", 5);

            if (b.containsKey("_names")) {
                //getString(key, defValue) was added in API 12. Use getString(key), as this will return null if the key doesn't exist.
                String value = b.getString("_names");
                selectedMemberNames = (null != value) ? value : getString(R.string.unknown_member);
            }
            //Toast.makeText(getBaseContext(),String.format("Member Names: %s",selectedMemberNames),Toast.LENGTH_LONG).show();
            actionBar.setTitle(selectedMemberNames);
        }


        Member selectedMember = ledgerLinkApplication.getMemberRepo().getMemberById(selectedMemberId);
        populateDataFields(selectedMember);

    }

    private void inflateCustomActionBar() {

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back_edit_delete, null);
//        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        navigateBack();
//                        finish();
//                    }
//                });
        customActionBarView.findViewById(R.id.actionbar_edit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editMember();
                    }
                });
        customActionBarView.findViewById(R.id.actionbar_delete).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete();
                    }
                });


        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

         //Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
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
        inflater.inflate(R.menu.members_details_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
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
            TextView txtMemberNo = (TextView) findViewById(R.id.txtMDVMemberNo);
            txtMemberNo.setText(Utils.formatLongNumber(member.getMemberNo()));
            TextView txtSurname = (TextView) findViewById(R.id.txtMDVSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView) findViewById(R.id.txtMDVOtherNames);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }
            TextView txtGender = (TextView) findViewById(R.id.txtMDVGender);
            if (member.getGender() != null) {
                txtGender.setText(member.getGender());
            }

            TextView txtOccupation = (TextView) findViewById(R.id.txtMDVOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView) findViewById(R.id.txtMDVPhone);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(member.getPhoneNumber());
            }
            TextView txtAge = (TextView) findViewById(R.id.txtMDVAge);

            //TODO: I need to retrieve the Age from the DateOfBirth
            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);
            txtAge.setText(String.format("%d", computedAge));

            //TODO: When we allow members to take leave, we may be better allowing this field to be editable
            TextView txtCyclesCompleted = (TextView) findViewById(R.id.txtMDVCyclesCompleted);
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(member.getDateOfAdmission());
            int cycles = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            txtCyclesCompleted.setText(String.format("%d", cycles));

            //Load the middle start values
            TextView lblMDMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblMDMiddleCycleInformationHeading);
            TextView lblMDMiddleCycleSavings = (TextView) findViewById(R.id.lblMDMiddleCycleSavings);
            TextView lblMDMiddleCycleWelfare = (TextView) findViewById(R.id.lblMDMiddleCycleWelfare);
            TextView lblMDMiddleCycleLoansOutstanding = (TextView) findViewById(R.id.lblMDMiddleCycleLoansOutstanding);
            TextView lblMDMiddleCycleOutstandingWelfare = (TextView) findViewById(R.id.lblMDMiddleCycleOutstandingWelfare);

            lblMDMiddleCycleSavings.setText(String.format(getString(R.string.total_saving_x)+" %,.0f %s", member.getSavingsOnSetup(), getResources().getString(R.string.operating_currency)));
            lblMDMiddleCycleWelfare.setText(String.format(getString(R.string.total_welfare_x)+" %,.0f %s", member.getWelfareOnSetup(), getResources().getString(R.string.operating_currency)));
            lblMDMiddleCycleLoansOutstanding.setText(String.format(getString(R.string.loans_outstanding_x)+" %,.0f %s", member.getOutstandingLoanOnSetup(), getResources().getString(R.string.operating_currency)));
            lblMDMiddleCycleOutstandingWelfare.setText(String.format(getString(R.string.outstanding_welfare_x)+" %,.0f %s", member.getOutstandingWelfareOnSetup(), getResources().getString(R.string.operating_currency)));

            //Show the heading
            //Get the date of the dummy GSW meeting
            String pronoun = member.getGender().startsWith("F") || member.getGender().startsWith("f") ? getString(R.string.her) : getString(R.string.his) ;
            lblMDMiddleCycleInformationHeading.setText(getString(R.string.member_info_was_added) + pronoun + getString(R.string.total_savings_and_outstanding_loans));

            Meeting dummyGSWMeeting = ledgerLinkApplication.getMeetingRepo().getDummyGettingStartedWizardMeeting();
            if (dummyGSWMeeting != null) {
                lblMDMiddleCycleInformationHeading.setText(getString(R.string.member_info_was_added_on) + Utils.formatDate(dummyGSWMeeting.getMeetingDate(), getString(R.string.date_format)) +  getString(R.string.after_cycle_started_here_are) + pronoun + getString(R.string.total_savings_and_outstanding_loans));
            }


        } finally {

        }

    }

    private void clearDataFields() {
        // Populate the Fields
        TextView txtMemberNo = (TextView) findViewById(R.id.txtMDVMemberNo);
        txtMemberNo.setText(null);
        TextView txtSurname = (TextView) findViewById(R.id.txtMDVSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView) findViewById(R.id.txtMDVOtherNames);
        txtOtherNames.setText(null);
        TextView txtGender = (TextView) findViewById(R.id.txtMDVGender);
        txtGender.setText(null);
        TextView txtOccupation = (TextView) findViewById(R.id.txtMDVOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView) findViewById(R.id.txtMDVPhone);
        txtPhone.setText(null);
        TextView txtAge = (TextView) findViewById(R.id.txtMDVAge);
        txtAge.setText(null);
        TextView txtCyclesCompleted = (TextView) findViewById(R.id.txtMDVCyclesCompleted);
        txtCyclesCompleted.setText(null);

        txtMemberNo.requestFocus();
    }

    private void navigateBack() {
        String caller = getString(R.string.reviewmembers);
        Intent i;

        if (getIntent().hasExtra("_caller")) {
            caller = getIntent().getStringExtra("_caller");
        }

        if (caller.equalsIgnoreCase(getString(R.string.new_cycle_pg2))) {
            i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
            startActivity(i);
        } else {
            i = new Intent(getApplicationContext(), MembersListActivity.class);
            startActivity(i);
        }
    }

    private void editMember() {

        MemberRepo memberRepo = new MemberRepo(getApplicationContext());
        Member member = memberRepo.getMemberById(selectedMemberId);
        if(member.isActive()) {
            this.loadEditWindow();
        }else{
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    loadEditWindow();
                }
            };
            DialogMessageBox.show(context, getString(R.string.member_reactivation), getString(R.string.sure_you_like_reactivate) + member.getSurname() + " " + member.getOtherNames(), runnable);
        }
    }

    protected void loadEditWindow(){
        Intent i = new Intent(getApplicationContext(), AddMemberActivity.class);
        i.putExtra("_id", selectedMemberId);
        i.putExtra("_isEditAction", true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private boolean delete() {
        final Member selMember = ledgerLinkApplication.getMemberRepo().getMemberById(selectedMemberId);
        if (selMember == null) {
            Utils.createAlertDialogOk(MemberDetailsViewActivity.this, getString(R.string.remove_member), getString(R.string.system_failed_to_retrieve_member_records), Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(MemberDetailsViewActivity.this);
        ad.setTitle(R.string.archive_member);
        ad.setMessage(R.string.sure_you_would_like_to_archive_member);
        ad.setPositiveButton(
                R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        MemberRepo memberRepo = new MemberRepo(getApplicationContext(), selectedMemberId);
                        memberRepo.archiveMember();
                        //ledgerLinkApplication.getMemberRepo().deleteMember(selMember);
                        navigateBack();
                    }
                }
        );
        ad.setNegativeButton(
                R.string.no, new DialogInterface.OnClickListener() {
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