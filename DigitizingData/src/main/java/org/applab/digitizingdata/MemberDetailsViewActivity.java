package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Moses on 6/26/13.
 */
public class MemberDetailsViewActivity extends SherlockActivity {
    ActionBar actionBar;
    int selectedMemberId = -1;
    String selectedMemberNames = "VSLA Member";
    MemberRepo repo;
    private Member selectedMember;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

       inflateCustomActionBar();


        setContentView(R.layout.activity_member_details_view);
        // Check for Extras in case this call was to edit a pledge
        Bundle b = getIntent().getExtras();


        if (b != null) {

            selectedMemberId = b.getInt("_id", 5);

            if(b.containsKey("_names")) {
                //getString(key, defValue) was added in API 12. Use getString(key), as this will return null if the key doesn't exist.
                String value =  b.getString("_names");
                selectedMemberNames = (null != value)? value : "Unknown Member";
            }
            //Toast.makeText(getBaseContext(),String.format("Member Names: %s",selectedMemberNames),Toast.LENGTH_LONG).show();
            actionBar.setTitle(selectedMemberNames);
        }


        repo = new MemberRepo(getApplicationContext());
        selectedMember = repo.getMemberById(selectedMemberId);
        populateDataFields(selectedMember);

    }

    private void inflateCustomActionBar(){

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back_edit_delete, null);
        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateBack();
                        finish();
                    }
                });
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

        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        // END_INCLUDE (inflate_set_custom_view)

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.members_details_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
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
            Spinner cboMDVMemberNo = (Spinner) findViewById(R.id.cboMDVMemberNo);
            Utils.setSpinnerSelection(member.getMemberNo() + "", cboMDVMemberNo);

            TextView txtSurname = (TextView)findViewById(R.id.txtMDVSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView)findViewById(R.id.txtMDVOtherName);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }

            Spinner cboGender = (Spinner) findViewById(R.id.cboMDVGender);
            Utils.setSpinnerSelection(member.getGender(), cboGender);

            TextView txtOccupation = (TextView)findViewById(R.id.txtMDVOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView)findViewById(R.id.txtMDVPhone);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(member.getPhoneNumber());
            }

            // Set the age
            Spinner cboMDVAge = (Spinner) findViewById(R.id.cboMDVAge);
            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);
            Utils.setSpinnerSelection(computedAge + "", cboMDVAge);
            calToday = Calendar.getInstance();

            // TODO: Might be better allowing this field to be editable; in case it is allowed for members to take leave
            // Set cycles
            Spinner cboMDVCyclesCompleted = (Spinner) findViewById(R.id.cboMDVCyclesCompleted);
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(member.getDateOfAdmission());
            int cycles = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            Utils.setSpinnerSelection(cycles + "", cboMDVCyclesCompleted);

        }
        finally {

        }

    }

    private void buildGenderSpinner() {

        //Setup the Spinner Items
        Spinner cboGender = (Spinner) findViewById(R.id.cboMDVGender);
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

        Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboMDVMemberNo);
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

        Spinner cboAMAge = (Spinner) findViewById(R.id.cboMDVAge);
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

        Spinner cboAMCycles = (Spinner) findViewById(R.id.cboMDVCyclesCompleted);
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


    private void clearDataFields() {
        // Spinner items
        buildGenderSpinner();
        buildMemberNoSpinner();
        buildAgeSpinner();
        buildCyclesCompletedSpinner();

        // Populate the Fields
        Spinner cboMDVMemberNo = (Spinner) findViewById(R.id.cboMDVMemberNo);

        // txtMemberNo.setText(null);
        TextView txtSurname = (TextView)findViewById(R.id.txtMDVSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView)findViewById(R.id.txtMDVOtherName);
        txtOtherNames.setText(null);

       // TextView txtGender = (TextView)findViewById(R.id.txtMDVGender);
       //txtGender.setText(null);

        TextView txtOccupation = (TextView)findViewById(R.id.txtMDVOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView)findViewById(R.id.txtMDVPhone);
        txtPhone.setText(null);
/**        TextView txtAge = (TextView)findViewById(R.id.txtMDVAge);
        txtAge.setText(null);
        TextView txtCyclesCompleted = (TextView)findViewById(R.id.txtMDVCyclesCompleted);
        txtCyclesCompleted.setText(null); */

        cboMDVMemberNo.requestFocus();
    }

    private void navigateBack() {
        String caller = "reviewMembers";
        Intent i;

        if(getIntent().hasExtra("_caller")) {
            caller = getIntent().getStringExtra("_caller");
        }

        if(caller.equalsIgnoreCase("newCyclePg2")) {
            i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
            startActivity(i);
        }
        else {
            i = new Intent(getApplicationContext(), MembersListActivity.class);
            startActivity(i);
        }
    }

    private void editMember() {
        Intent i = new Intent(getApplicationContext() ,AddMemberActivity.class);
        i.putExtra("_id",selectedMemberId);
        i.putExtra("_isEditAction", true);
        startActivity(i);
    }

    private boolean delete() {
        final MemberRepo repo = new MemberRepo(getApplicationContext());
        final Member selMember = repo.getMemberById(selectedMemberId);
        if(selMember == null) {
            Utils.createAlertDialogOk(MemberDetailsViewActivity.this, "Remove Member", "System failed to retrieve the member's records.", Utils.MSGBOX_ICON_EXCLAMATION).show();
            return false;
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(MemberDetailsViewActivity.this);
        ad.setTitle("Remove Member");
        ad.setMessage("Are you sure you want to remove this member?");
        ad.setPositiveButton(
            "Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    repo.deleteMember(selMember);
                    navigateBack();
                }
            }
        );
        ad.setNegativeButton(
            "No", new DialogInterface.OnClickListener(){
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