package org.applab.digitizingdata;

import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import org.applab.digitizingdata.helpers.CustomFineTypeSpinnerListener;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.FineTypeCustomArrayAdapter;
import org.applab.digitizingdata.helpers.MembersCustomArrayAdapter;
import org.applab.digitizingdata.helpers.MembersFinesArrayAdapter;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.FineTypeRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.repo.MemberRepo;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Moses on 7/15/13.
 */
public class AddFineActivity extends SherlockActivity {
    private ActionBar actionBar;
    private Member selectedMember;
    private int selectedMemberId;
    private int meetingId;
    private boolean successAlertDialogShown = false;
    private boolean selectedFinishButton = false;
    private String dlgTitle = "Add Fine";
    MeetingFineRepo fineRepo;
    private boolean isEditAction;
    ArrayList<FineType> fineTypes = null;
    private int paymentStatus = 0;
    private AlertDialog alertDialog;
    FineType selectedFineType;
    private MeetingFine fine;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        if (getIntent().hasExtra("_memberId")) {
            this.selectedMemberId = getIntent().getIntExtra("_memberId", 0);
        }

        if(getIntent().hasExtra("_meetingId")) {
            this.meetingId = getIntent().getIntExtra("_meetingId", 0);
        }


        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        actionBar = getSupportActionBar();

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedFinishButton = true;
                        saveMemberFine();
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

        actionBar.setTitle("New Fine");

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


        setContentView(R.layout.activity_add_fine_member);
        TextView txtAmount = (TextView) findViewById(R.id.txtFFineAmount);

        CheckBox chkPaidStatus = (CheckBox) findViewById(R.id.chkFPaidStatus);

        chkPaidStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    paymentStatus = 1;
                } else {
                    paymentStatus = 0;
                }
            }
        });


        //Setup the Spinner Items
        Spinner cboFineType = (Spinner) findViewById(R.id.cboFineType);
        FineTypeCustomArrayAdapter adapter = new FineTypeCustomArrayAdapter(this, android.R.layout.simple_spinner_item,
                populateFineTypeList(), "fonts/roboto-regular.ttf");
        /**{

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
         }; */


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboFineType.setAdapter(adapter);


        // adapter.setMeetingId(meetingId);
        //  String[] genderList = new String[]{"Male", "Female"};
        //ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, genderList)


        cboFineType.setOnItemSelectedListener(new CustomFineTypeSpinnerListener());

        //Make the spinner selectable
        cboFineType.setFocusable(true);
        cboFineType.setFocusableInTouchMode(true);
        cboFineType.setClickable(true);

        clearDataFields();
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
        double theAmount = 0.0;
        Log.d("AddFineActivity.saveMemberFine", "Save Fine");

        //  try {

        /**  TextView txtFine = (TextView) findViewById(R.id.txtFFineAmount);
         String amount = txtFine.getText().toString().trim();
         if (amount.length() < 1) {
         alertDialog = Utils.createAlertDialogOk(AddFineActivity.this, "Fines", "The Fines Amount is required.", Utils.MSGBOX_ICON_EXCLAMATION);
         Log.d("AddFineActivity.saveMemberFine", "Show Dialog11");
         alertDialog.show();
         Log.d("AddFineActivity.saveMemberFine", "Show Dialog21");
         txtFine.requestFocus();
         return false;
         } else {
         theAmount = Double.parseDouble(amount);
         if (theAmount < 0.0) {
         alertDialog = Utils.createAlertDialogOk(AddFineActivity.this, "Fines", "The Fines Amount is invalid.", Utils.MSGBOX_ICON_EXCLAMATION);
         Log.d("AddFineActivity.saveMemberFine", "Show Dialog");
         alertDialog.show();
         Log.d("AddFineActivity.saveMemberFine", "Show Dialog2");
         txtFine.requestFocus();
         return false;
         }
         }

         Spinner cboFineType = (Spinner) findViewById(R.id.cboFineType);
         int fineTypeId = (int) cboFineType.getSelectedItemId();
         */
        if (validateData()) {

            if (fineRepo == null) {
                fineRepo = new MeetingFineRepo(AddFineActivity.this);
            }

            Log.d("AddFineActivity.saveMemberFine", "Meeting:" + String.valueOf(meetingId)+ " Amount: "+String.valueOf(fine.getAmount()) + " Member: "+String.valueOf(selectedMemberId)+ " FineType: "+String.valueOf(selectedFineType.getFineTypeId())+ " PaymentStatus: "+String.valueOf(paymentStatus));
            successFlg = fineRepo.saveMemberFine(meetingId, selectedMemberId, fine.getAmount(), selectedFineType.getFineTypeId(), paymentStatus);
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }
        Log.d("AddFineActivity.saveMemberFine", String.valueOf(successFlg));

        return successFlg;
    }


    protected boolean validateData() {
        try {
            /** if (null == fine) {
             return false;
             }*/
            fine = new MeetingFine();
            fineRepo = new MeetingFineRepo(getApplicationContext());

            //Validate: Fine Type
            Spinner cboFineType = (Spinner) findViewById(R.id.cboFineType);
            selectedFineType = (FineType) cboFineType.getSelectedItem();
            String fineTypeName = selectedFineType.getFineTypeName();
            // cboFineType.getSelectedItem().toString().trim();
            Log.d("Add Fine", selectedFineType.getFineTypeName());
            if (fineTypeName.equalsIgnoreCase("Please Select FineType")) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Fine Type is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();

                cboFineType.requestFocus();
                return false;
            } else {
                //  fineType = new FineType((int) fineType(), cboFineType.getSelectedItem().toString().trim());
                fine.setFineType(selectedFineType);
            }

            // Validate: Fine Amount
            TextView txtMemberFineAmount = (TextView) findViewById(R.id.txtFFineAmount);
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
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }


    private void clearDataFields() {
        // Clear the Fields
        TextView txtMemberFineAmount = (TextView) findViewById(R.id.txtFFineAmount);
        txtMemberFineAmount.setText("");

        Spinner cboFineType = (Spinner) findViewById(R.id.cboFineType);
        cboFineType.requestFocus();
    }

    private ArrayList<FineType> populateFineTypeList() {
        //Load the Main Menu
        FineTypeRepo fineTypeRepo = new FineTypeRepo(getApplicationContext());
        /**       fineTypes = fineTypeRepo.getAllFineTypes();
         */
        if (fineTypes == null) {
            fineTypes = new ArrayList<FineType>();
        }

        // Hardcode for now
        fineTypes.add(new FineType(0, "Select FineType"));
        fineTypes.add(new FineType(1, "Other"));
        fineTypes.add(new FineType(2, "Late Coming"));
        fineTypes.add(new FineType(3, "Disorder"));
        return fineTypes;
    }

}