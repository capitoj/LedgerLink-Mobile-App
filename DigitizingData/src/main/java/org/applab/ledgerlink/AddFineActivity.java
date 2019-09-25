package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.domain.model.MeetingFine;
import org.applab.ledgerlink.fontutils.TypefaceManager;

/**
 * Created by Moses on 7/15/13.
 */
public class AddFineActivity extends ActionBarActivity{
    private int selectedMemberId;
    private int meetingId;
    private boolean selectedFinishButton = false;
    LedgerLinkApplication ledgerLinkApplication;
    private int paymentStatus = 0;
    private String selectedFineTypeName;
    private MeetingFine fine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        if (getIntent().hasExtra("_memberId")) {
            this.selectedMemberId = getIntent().getIntExtra("_memberId", 0);
        }

        if (getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        String fullName = getIntent().getStringExtra("_name");

        inflateCustomActionBar();

        setContentView(R.layout.activity_add_fine_member);

        TextView lblFullName = (TextView) findViewById(R.id.txtFMFullName);
        lblFullName.setText(fullName);

        final CheckBox chkPaidStatus = (CheckBox) findViewById(R.id.chkFMPaidStatus);

        LinearLayout parentLayout = (LinearLayout) chkPaidStatus.getParent();
        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chkPaidStatus.toggle();
            }
        });

        chkPaidStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    paymentStatus = 1;
                } else {
                    paymentStatus = 0;
                }
            }
        });

        buildFineTypeSpinner();

        clearDataFields();
    }

    private void inflateCustomActionBar() {

        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView;

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFinishButton = true;
                        if(saveMemberFine())
                        {
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

        // actionbar with logo
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle(R.string.new_fine_main);

        // Set to false to remove caret and disable its function; if designer decides otherwise set both to true
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
        /**actionBar.setTitle("New Fine");

         actionBar.setDisplayOptions(
         ActionBar.DISPLAY_SHOW_CUSTOM,
         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
         | ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView,
         new ActionBar.LayoutParams(
         ViewGroup.LayoutParams.MATCH_PARENT,
         ViewGroup.LayoutParams.MATCH_PARENT)); */
        // END_INCLUDE (inflate_set_custom_view)
        //if in getting started wizard.. use the getting started layout
        //else use the default layout
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_fine, menu);
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
            case R.id.mnuFCancel:
            case R.id.mnuFFinished:
                selectedFinishButton = true;
                return saveMemberFine();
        }
        return true;
    }

    public boolean saveMemberFine() {
        boolean successFlg = false;
        if (validateData()) {

            successFlg = ledgerLinkApplication.getMeetingFineRepo().saveMemberFine(meetingId, selectedMemberId, fine.getAmount(), fine.getFineTypeId(), paymentStatus);
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }


    protected boolean validateData() {
        try {

            fine = new MeetingFine();
            //Validate: Fine Type
            Spinner cboFineType = (Spinner) findViewById(R.id.cboFMFineType);
            String dlgTitle = getString(R.string.add_fine_main);
            if (cboFineType.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.fine_type_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboFineType.setFocusableInTouchMode(true);
                cboFineType.requestFocus();
                return false;
            } else {
                /** TODO: REMOVE
                 * Meantime fix for QA time
                 * */

                if (selectedFineTypeName.equalsIgnoreCase(getResources().getString(R.string.finetype_other))) {
                    fine.setFineTypeId(1);
                } else if (selectedFineTypeName.equalsIgnoreCase(getResources().getString(R.string.finetype_latecoming))) {
                    fine.setFineTypeId(2);
                } else if (selectedFineTypeName.equalsIgnoreCase(getResources().getString(R.string.finetype_disorder))) {
                    fine.setFineTypeId(3);
                } else {
                    fine.setFineTypeId(0);
                }
                cboFineType.setFocusableInTouchMode(false);
                fine.setFineTypeName(selectedFineTypeName);
            }

            // Validate: Fine Amount
            TextView txtMemberFineAmount = (TextView) findViewById(R.id.txtFMFineAmount);
            String memberFineAmount = txtMemberFineAmount.getText().toString().trim();
            if (memberFineAmount.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.fine_amount_required), Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtMemberFineAmount.requestFocus();
                return false;
            } else {
                double theFineAmount = Double.parseDouble(memberFineAmount);
                if (theFineAmount <= 0) {
                    Utils.createAlertDialogOk(this, dlgTitle, getString(R.string.fine_amount_must_be_positive), Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtMemberFineAmount.requestFocus();
                    return false;

                } else {
                    fine.setAmount(theFineAmount);
                }
            }
            return true;
        } catch (
                Exception ex
                )

        {
            ex.printStackTrace();
            return false;
        }

    }


    private void clearDataFields() {

        buildFineTypeSpinner();

        // Clear the Fields
        TextView txtMemberFineAmount = (TextView) findViewById(R.id.txtFMFineAmount);
        txtMemberFineAmount.setText("");

        Spinner cboFineType = (Spinner) findViewById(R.id.cboFMFineType);
        cboFineType.requestFocus();
    }

    /* Populates the Fine Type spinner */
    protected void buildFineTypeSpinner() {

        Spinner cboFMFineType = (Spinner) findViewById(R.id.cboFMFineType);
        cboFMFineType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedFineTypeName = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Make the spinner selectable
        cboFMFineType.setFocusable(true);
        cboFMFineType.setFocusableInTouchMode(false);
        cboFMFineType.setClickable(true);
        cboFMFineType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                view.setFocusableInTouchMode(false);
            }
        });
    }

}