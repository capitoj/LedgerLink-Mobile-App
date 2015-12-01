package org.applab.ledgerlink;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import au.com.bytecode.opencsv.CSVReader;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.fontutils.TypefaceTextView;
import org.applab.ledgerlink.helpers.GettingStartedWizardMembersArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.helpers.LongTaskRunner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Created by Moses on 7/16/13.
 */
public class GettingStartedWizardReviewMembersActivity extends MembersListActivity {
    private static final int PICKFILE_RESULT_CODE = 6006;
    private ArrayList<Member> members;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        inflateCustombar();
        setContentView(R.layout.activity_getting_started_wizard_review_members);

        TypefaceTextView reviewSubHeading = (TypefaceTextView) findViewById(R.id.lblRvwMembersSubHeading);
        SpannableStringBuilder reviewSubHeadingPart = new SpannableStringBuilder("Review and confirm that all information is correct. Press the member’s name to correct an entry. If you wish to review it later, you may ");
        SpannableString exitText = new SpannableString("exit ");
        exitText.setSpan(new StyleSpan(Typeface.BOLD), 0, exitText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        reviewSubHeadingPart.append(exitText);
        reviewSubHeadingPart.append("and come back later.");

        reviewSubHeading.setText(reviewSubHeadingPart);

        //Load the summary information
        Meeting dummyGettingStartedWizardMeeting = ledgerLinkApplication.getMeetingRepo().getDummyGettingStartedWizardMeeting();

        //Set cycle start date in label
        Date startDate = dummyGettingStartedWizardMeeting.getMeetingDate();
        TextView lblRvwMembersDate = (TextView) findViewById(R.id.lblRvwMembersDate);
        lblRvwMembersDate.setText("As of " + Utils.formatDate(startDate));



        //Set savings in GSW meeting
        double totalSavings = ledgerLinkApplication.getMeetingSavingRepo().getTotalSavingsInMeeting(dummyGettingStartedWizardMeeting.getMeetingId());
        TextView lblRvwMembersTotalSavings = (TextView) findViewById(R.id.lblRvwMembersTotalSavings);
        lblRvwMembersTotalSavings.setText(String.format("Total savings this cycle %,.0f %s", totalSavings,
                getResources().getString(R.string.operating_currency)));

        //Set loans issued in GSW meeting
        TextView lblRvwMembersTotalLoan = (TextView) findViewById(R.id.lblRvwMembersTotalLoan);
        lblRvwMembersTotalLoan.setText(String.format("Total loans outstanding %,.0f %s",
                ledgerLinkApplication.getMeetingLoanIssuedRepo().getTotalLoansIssuedInMeeting(dummyGettingStartedWizardMeeting.getMeetingId()),
                getResources().getString(R.string.operating_currency)));

        //Populate the Members
        populateMembersList();
        ledgerLinkApplication.getVslaInfoRepo().updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_REVIEW_MEMBERS);
    }

    /* inflates custom menu bar for review members */
    void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_exit_enternext_next, null);
        customActionBarView.findViewById(R.id.actionbar_exit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent i = new Intent(getApplicationContext(), GettingStartedWizardReviewMembersActivity.class);
                        //startActivity(i);
                        finish();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
        );


        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), GettingStartedWizardNewCycleActivity.class);
                        i.putExtra("_isUpdateCycleAction", true);
                        i.putExtra("_isFromReviewMembers", true);
                        startActivity(i);
                        finish();
                    }
                }
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("GET STARTED");
        actionBar.setHomeButtonEnabled(true);
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

        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.review_members, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, GettingStartedWizardAddMemberActivity.class);
                NavUtils.navigateUpTo(this, upIntent);
                return true;
            /**  case R.id.mnuMListDone:
             //Go to GSW cycle review page
             //which is infact the new cycle activity in update mode
             Intent i = new Intent(getApplicationContext(), GettingStartedWizardNewCycleActivity.class);
             i.putExtra("_isUpdateCycleAction", true);
             startActivity(i);
             return true; */
            case R.id.mnuMListAdd:
                Intent i = new Intent(getApplicationContext(), GettingStartedWizardAddMemberActivity.class);
                startActivity(i);
                return true;

            case R.id.mnuImportFromCsv:
                //attemptToImportFromCsv();
                startFileChooserForCsvImport();
                return true;
        }
        return true;
    }

    private void startFileChooserForCsvImport() {
        // Create the ACTION_GET_CONTENT Intent
        Intent getContentIntent = FileUtils.createGetContentIntent();

        Intent intent = Intent.createChooser(getContentIntent, "Choose CSV");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch(requestCode){
            case PICKFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    final Uri uri = data.getData();

                    // Get the File path from the Uri
                    String filePath = FileUtils.getPath(this, uri);
                    Log.d("File chosen", "Chosen file "+filePath);
                    attemptToImportFromCsv(filePath);
                }
                break;

        }
    }

    private void attemptToImportFromCsv(final String filename) {
        Runnable importer = new Runnable() {
            @Override
            public void run() {
                LongTaskRunner.runLongTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            importCsv(filename);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                "Importing members",
                        "Please wait while member information is imported from the CSV file",
                        GettingStartedWizardReviewMembersActivity.this);

            }
        };
        Utils.showDialogAndRunAction(this, "Import members from CSV?", "You are about to import member information from the file \""+filename+"\"\nPress continue to start", importer);
    }

    private void importCsv(String csvFile) {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.createAlertDialogOk(GettingStartedWizardReviewMembersActivity.this, "File missing", "CSV File \"LLMembers.csv\" wasn't found. Please place this file in the LedgerLink folder and try again", Utils.MSGBOX_ICON_EXCLAMATION).show();

                }
            });
            return;
        }
        String[] nextLine;
        int i = 0;
        int dataRowNo = 0;
        int migratedCount = 0;
        int skippedCount = 0;
        try {

            String skippedRows = "";
            while ((nextLine = reader.readNext()) != null) {

                if (0 >= i++) {
                    continue;
                }

                dataRowNo++;
                //increment the dataRow number


                String memberNo = nextLine[0].trim();
                String surname = nextLine[1].trim();
                String otherNames = nextLine[2].trim();
                String gender = nextLine[3].trim();
                String age = nextLine[4].trim();
                String occupation = nextLine[5].trim();
                String phoneNo = nextLine[6].trim();
                String cyclesCompleted = nextLine[7].trim();
                String totalSavings = nextLine[8].trim();
                String outsstandingLoanNumber = nextLine[9].trim();
                String outstandingLoanAmount = nextLine[10].trim();
                String loanDueDate = nextLine[11].trim();


                //First Insert the Member Details
                Member member = new Member();

                try {
                    if (memberNo.length() > 0) {
                        member.setMemberNo(Integer.parseInt(memberNo));
                    } else {
                        member.setMemberNo(i);
                    }

                    //Surname
                    if (surname.length() > 0) {
                        member.setSurname(surname);
                    } else {
                        member.setSurname(Utils.MISSING_NAME_MARKER);
                    }

                    //Other Names
                    if (otherNames.length() > 0) {
                        member.setOtherNames(otherNames);
                    } else {
                        member.setOtherNames(Utils.MISSING_NAME_MARKER);
                    }

                    //Gender
                    if (gender.length() > 0) {
                        member.setGender((gender.startsWith("M") ? "Male" : "Female"));
                    } else {
                        member.setGender("Female");
                    }

                    //Date Of Birth
                    //Default Age: 18
                    Calendar d = Calendar.getInstance();

                    if (age.length() > 0) {
                        int theAge = Integer.parseInt(age);
                        d.add(Calendar.YEAR, -theAge);
                        member.setDateOfBirth(d.getTime());
                    } else {
                        d.add(Calendar.YEAR, -Utils.DEFAULT_MEMBER_AGE);
                        member.setDateOfBirth(d.getTime());
                    }

                    //Occupation
                    if (occupation.length() > 0) {
                        member.setOccupation(occupation);
                    } else {
                        member.setOccupation(Utils.DEFAULT_MEMBER_OCCUPATION);
                    }

                    //Cycles Completed
                    Calendar calCycles = Calendar.getInstance();

                    if (cyclesCompleted.length() > 0) {
                        int theCycles = Integer.parseInt(cyclesCompleted);
                        member.setCyclesCompleted(theCycles);
                        calCycles.add(Calendar.YEAR, -theCycles);
                        member.setDateOfAdmission(calCycles.getTime());
                    } else {
                        member.setCyclesCompleted(0);
                        member.setDateOfAdmission(calCycles.getTime());
                    }

                    //Phone Number
                    if (phoneNo.length() > 0) {
                        member.setPhoneNumber(phoneNo);
                    } else {
                        member.setPhoneNumber(null);
                    }

                    if(totalSavings.length()>0) {
                        member.setSavingsOnSetup(new Double(totalSavings));
                    }
                    else {
                        member.setSavingsOnSetup(0);
                    }


                    if(outsstandingLoanNumber.length()>0) {
                        member.setOutstandingLoanNumberOnSetup(Integer.parseInt(outsstandingLoanNumber));
                    }
                    else {
                        member.setOutstandingLoanNumberOnSetup(0);
                    }


                    if(outstandingLoanAmount.length()>0) {
                        member.setOutstandingLoanOnSetup(Integer.parseInt(outstandingLoanAmount));
                    }
                    else {
                        member.setOutstandingLoanOnSetup(0);
                    }


                    if(loanDueDate.length()>0) {
                        member.setDateOfFirstRepayment(Utils.getDateFromString(loanDueDate, "dd/MM/yyyy"));
                    }
                    else {
                        member.setDateOfFirstRepayment(null);
                    }

                    boolean memberNoAvailable = ledgerLinkApplication.getMemberRepo().isMemberNoAvailable(member.getMemberNo(), member.getMemberId());
                    boolean memberAdded;
                    if (!memberNoAvailable) {
                        //Lets loadd this member id from db
                        Member existingMember = ledgerLinkApplication.getMemberRepo().getMemberByMemberNo(member.getMemberNo());
                        if(existingMember != null) {
                            member.setMemberId(existingMember.getMemberId());
                        }
                        memberAdded =  ledgerLinkApplication.getMemberRepo().updateGettingStartedWizardMember(member);
                    }
                    else {
                        memberAdded = ledgerLinkApplication.getMemberRepo().addGettingStartedWizardMember(member);
                    }


                    if (!memberAdded) {
                        //retrieve the MemberId
                        final int finalDataRowNo1 = dataRowNo;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.createAlertDialogOk(GettingStartedWizardReviewMembersActivity.this, "A record failed", "Data at row " + finalDataRowNo1 +" couldnt be imported. Correct it and perform import again", Utils.MSGBOX_ICON_EXCLAMATION).show();
                            }
                        });
                        return;
                    }

                    //Done with this member
                    Log.d("CSV import ", "Imported member "+memberNo);
                    //Toast.makeText(getApplicationContext(), String.format("Member of data record %d was migrated successfully.", dataRowNo), Toast.LENGTH_SHORT).show();

                    //Increment the migrated count
                    migratedCount++;
                } catch (Exception exMember) {
                    //Toast.makeText(getApplicationContext(), String.format("An error has occurred. Skipping member on data record %d", dataRowNo), Toast.LENGTH_SHORT).show();
                    skippedCount++;
                    if (skippedCount > 1) {
                        skippedRows.concat(String.format(", %d", dataRowNo));
                    } else {
                        skippedRows.concat(String.format("%d", dataRowNo));
                    }
                }
            }

            final int finalMigratedCount = migratedCount;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.createAlertDialogOk(GettingStartedWizardReviewMembersActivity.this, "Import completed", "Data import has been completed succesfully. " + finalMigratedCount +" members imported during this session.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    populateMembersList();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            final int finalDataRowNo = dataRowNo;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.createAlertDialogOk(GettingStartedWizardReviewMembersActivity.this, "Failed", "An error occured while importing a record. Last record processed was " + finalDataRowNo, Utils.MSGBOX_ICON_EXCLAMATION).show();

                }
            });

        }
    }

    //Populate Members List
    @Override
    protected void populateMembersList() {
        //Load the Main Menu
        members = ledgerLinkApplication.getMemberRepo().getAllMembers();
        if (members == null) {
            members = new ArrayList<Member>();
        }
        //Now get the data via the adapter
        final GettingStartedWizardMembersArrayAdapter adapter = new GettingStartedWizardMembersArrayAdapter(getBaseContext(), members);
        Log.d(getBaseContext().getPackageName(), members.size() + " members loaded");
        //Assign Adapter to ListView
        //Assign Adapter to ListView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                setListAdapter(adapter);
                Utils.setListViewHeightBasedOnChildren(getListView());
                getListView().setDivider(null);
            }
        });

        // listening to single list item on click
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Launching new Activity on selecting single List Item
                Member selectedMember = members.get(position);
                Intent viewMember = new Intent(view.getContext(), GettingStartedWizardAddMemberActivity.class);
                // Pass on data
                Bundle b = new Bundle();
                b.putInt("_id", selectedMember.getMemberId());
                b.putString("_names", selectedMember.getFullName());
                viewMember.putExtras(b);
                viewMember.putExtra("_caller", "reviewMembers");
                viewMember.putExtra("_isEditAction", true);
                startActivity(viewMember);
                finish();

            }
        });

    }
}