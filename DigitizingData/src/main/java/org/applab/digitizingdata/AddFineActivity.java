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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.FineType;
import org.applab.digitizingdata.domain.model.MeetingFine;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/15/13.
 */
public class AddFineActivity extends SherlockActivity {
    private int selectedMemberId;
    private int meetingId;
    private boolean selectedFinishButton = false;
    private MeetingFineRepo fineRepo;
    private int paymentStatus = 0;
    private String selectedFineTypeName;
    private MeetingFine fine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        TextView txtAmount = (TextView) findViewById(R.id.txtFMFineAmount);

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
        View customActionBarView = null;

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFinishButton = true;
                        saveMemberFine();
                        finish();
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

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("New Fine");

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
        final MenuInflater inflater = getSupportMenuInflater();
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
                //Toast.makeText(getBaseContext(), "You have successfully added a new member", Toast.LENGTH_LONG).show();
                //  return saveFineData();
            case R.id.mnuFFinished:
                selectedFinishButton = true;
                return saveMemberFine();
        }
        return true;
    }

    public boolean saveMemberFine() {
        boolean successFlg = false;
        if (validateData()) {

            if (fineRepo == null) {
                fineRepo = new MeetingFineRepo(AddFineActivity.this);
            }
            successFlg = fineRepo.saveMemberFine(meetingId, selectedMemberId, fine.getAmount(), fine.getFineTypeId(), paymentStatus);
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }


    protected boolean validateData() {
        try {

            fine = new MeetingFine();
            fineRepo = new MeetingFineRepo(getApplicationContext());

            //Validate: Fine Type
            Spinner cboFineType = (Spinner) findViewById(R.id.cboFMFineType);
            String dlgTitle = "Add Fine";
            if (cboFineType.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The fine type is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboFineType.requestFocus();
                return false;
            } else {
                //String selectedFineTypeName = cboFineType.getSelectedItem().toString().trim();

                /** TODO: REMOVE
                 * Meantime fix for QA time*/
                if (selectedFineTypeName.equalsIgnoreCase(getResources().getString(R.string.finetype_other))) {
                    fine.setFineTypeId(1);
                } else if (selectedFineTypeName.equalsIgnoreCase(getResources().getString(R.string.finetype_latecoming))) {
                    fine.setFineTypeId(2);
                } else if (selectedFineTypeName.equalsIgnoreCase(getResources().getString(R.string.finetype_disorder))) {
                    fine.setFineTypeId(3);
                } else {
                    fine.setFineTypeId(0);
                }
                fine.setFineTypeName(selectedFineTypeName);
            }


            // Validate: Fine Amount
            TextView txtMemberFineAmount = (TextView) findViewById(R.id.txtFMFineAmount);
            String memberFineAmount = txtMemberFineAmount.getText().toString().trim();
            if (memberFineAmount.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Fine Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtMemberFineAmount.requestFocus();
                return false;
            } else {
                double theFineAmount = Double.parseDouble(memberFineAmount);
                if (theFineAmount <= 0) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The Fine Amount must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
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
        // String[] fineTypeList = new String[]{"select fine type", "Other", "Latecoming", "Disorder"};
        // ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, fineTypeList) {
        /** ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item) {

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
         cboFMFineType.setAdapter(adapter);
         cboFMFineType.setOnItemSelectedListener(new CustomGenderSpinnerListener()); */

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
        cboFMFineType.setFocusableInTouchMode(true);
        cboFMFineType.setClickable(true);
    }

}