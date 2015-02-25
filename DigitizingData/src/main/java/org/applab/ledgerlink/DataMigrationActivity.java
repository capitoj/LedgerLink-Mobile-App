package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class DataMigrationActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainActivity";
    private TextView lblMigrationResult = null;
    private Button btnimport = null;
    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_data_migration);

        Log.v(TAG, "onCreate called");

        lblMigrationResult = (TextView)findViewById(R.id.lblMigrationResult);
        lblMigrationResult.setVisibility(View.GONE);
        btnimport = (Button) findViewById(R.id.button_import);
        btnimport.setOnClickListener(this);

        //   dbHelper = new DatabaseHandler(getApplicationContext());
    }

    @Override
    public void onClick(View arg0) {

        Log.v(TAG, "onClick called");
       if (arg0 == btnimport) {
           if(btnimport.getText().toString().equalsIgnoreCase("Finished")) {
               Intent mainMenu = new Intent(getApplicationContext(), MainActivity.class);
               startActivity(mainMenu);
               return;
           }
            File exportDir = new File(Environment.getExternalStorageDirectory(), "");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

           File file = new File(exportDir, Utils.VSLA_DATA_MIGRATION_FILENAME);
            try {

                //If the meeting has not been created start with it
                //Retrieve the Current Cycle
                 VslaCycle mostRecentCycle = ledgerLinkApplication.getVslaCycleRepo().getMostRecentCycle();
                if(null == mostRecentCycle) {
                    //Toast that there is no Cycle
                    Toast.makeText(getApplicationContext(),"There is no existing cycle.", Toast.LENGTH_LONG).show();
                    return;
                }

                //Get the Meeting if it is there
                 Meeting recentMeeting = ledgerLinkApplication.getMeetingRepo().getMostRecentMeeting();
                if(null == recentMeeting) {
                    //Create a meeting
                    Meeting newMeeting = new Meeting();
                    Calendar calMeetingDate = Calendar.getInstance();
                    newMeeting.setMeetingDate(calMeetingDate.getTime());
                    newMeeting.setVslaCycle(mostRecentCycle);
                    newMeeting.setIsCurrent(true);

                    boolean meetingCreated = ledgerLinkApplication.getMeetingRepo().addMeeting(newMeeting);
                    if(meetingCreated) {
                        //Retrieve the current Meeting
                        recentMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingByDate(newMeeting.getMeetingDate());
                    }

                    if(recentMeeting == null) {
                        //Toast that there are problems
                        Toast.makeText(getApplicationContext(),"Meeting could not be Created to setup the Loans and Savings", Toast.LENGTH_LONG).show();
                        return;
                    }

                }

                CSVReader reader = new CSVReader(new FileReader(file));
                String[] nextLine;
                try {
                    int i=0;
                    int dataRowNo = 0;
                    int migratedCount = 0;
                    int skippedCount = 0;
                    String skippedRows = "";
                    while ((nextLine = reader.readNext()) != null) {

                        if(0 >= i++) {
                            continue;
                        }

                        //increment the dataRow number
                        dataRowNo++;

                        // nextLine[] is an array of values from the line
                        int memberId = 0; //will be found later
                        String memberNo = nextLine[0].trim();
                        String surname = nextLine[1].trim();
                        String otherNames = nextLine[2].trim();
                        String gender = nextLine[3].trim();
                        String occupation = nextLine[4].trim();
                        String cyclesCompleted = nextLine[5].trim();
                        String phoneNo = nextLine[6].trim();
                        String age = nextLine[7].trim();
                        String totalSavings = nextLine[8].trim();
                        String meetingsPresent = nextLine[9].trim();
                        String meetingsAbsent = nextLine[10].trim();
                        String loanIssueAmount = nextLine[11].trim();
                        String loanIssueDate = nextLine[12].trim();
                        String loanDueDate = nextLine[13].trim();
                        String loanBalance = nextLine[14].trim();

                        //First Insert the Member Details
                        Member member = new Member();

                        try {
                            if(memberNo.length() > 0) {
                                member.setMemberNo(Integer.parseInt(memberNo));
                            }
                            else {
                                member.setMemberNo(i);
                            }

                            //Surname
                            if(surname.length() > 0) {
                                member.setSurname(surname);
                            }
                            else {
                                member.setSurname(Utils.MISSING_NAME_MARKER);
                            }

                            //Other Names
                            if(otherNames.length() > 0) {
                                member.setOtherNames(otherNames);
                            }
                            else {
                                member.setOtherNames(Utils.MISSING_NAME_MARKER);
                            }

                            //Gender
                            if(gender.length() > 0) {
                                member.setGender((gender.startsWith("M") ? "Male" : "Female"));
                            }
                            else {
                                member.setGender("Female");
                            }

                            //Date Of Birth
                            //Default Age: 18
                            Calendar d = Calendar.getInstance();

                            if (age.length() > 0) {
                                int theAge = Integer.parseInt(age);
                                d.add(Calendar.YEAR, -theAge);
                                member.setDateOfBirth(d.getTime());
                            }
                            else {
                                d.add(Calendar.YEAR, -Utils.DEFAULT_MEMBER_AGE);
                                member.setDateOfBirth(d.getTime());
                            }

                            //Occupation
                            if(occupation.length() > 0) {
                                member.setOccupation(occupation);
                            }
                            else {
                                member.setOccupation(Utils.DEFAULT_MEMBER_OCCUPATION);
                            }

                            //Cycles Completed
                            Calendar calCycles = Calendar.getInstance();

                            if (cyclesCompleted.length() > 0) {
                                int theCycles = Integer.parseInt(cyclesCompleted);
                                member.setCyclesCompleted(theCycles);
                                calCycles.add(Calendar.YEAR, -theCycles);
                                member.setDateOfAdmission(calCycles.getTime());
                            }
                            else {
                                member.setCyclesCompleted(0);
                                member.setDateOfAdmission(calCycles.getTime());
                            }

                            //Phone Number
                            if(phoneNo.length() > 0) {
                                member.setPhoneNumber(phoneNo);
                            }
                            else {
                                member.setPhoneNumber(null);
                            }

                            //Check the MemberNo to confirm that it doesn't exist, then add the member
                            Member recentMember = null;
                            boolean memberNoAvailable = ledgerLinkApplication.getMemberRepo().isMemberNoAvailable(member.getMemberNo(), member.getMemberId());

                            //Loop to get a Unique member No. Hope no Infinite Loops
                            while(!memberNoAvailable) {
                                member.setMemberNo(member.getMemberNo() + 1);
                                memberNoAvailable = ledgerLinkApplication.getMemberRepo().isMemberNoAvailable(member.getMemberNo(), member.getMemberId());
                            }
                            boolean memberAdded = ledgerLinkApplication.getMemberRepo().addMember(member);
                            if(memberAdded) {
                                //retrieve the MemberId
                                recentMember = ledgerLinkApplication.getMemberRepo().getMemberByMemberNo(member.getMemberNo());
                            }

                            //Continue only if we have added the member
                            if(null == recentMember) {
                                Toast.makeText(getApplicationContext(),String.format("Member on data record %d could not be migrated.", dataRowNo), Toast.LENGTH_SHORT).show();
                                skippedCount++;
                                continue;
                            }

                            //Now deal with Savings
                            String comment = "";

                            if(totalSavings.length() > 0){
                                double theSavings = Double.parseDouble(totalSavings);
                                boolean savingsDone = ledgerLinkApplication.getMeetingSavingRepo().saveMemberSaving(recentMeeting.getMeetingId(), recentMember.getMemberId(), theSavings, comment);
                            }

                            //Loan Issued
                            if(loanIssueAmount.length() > 0) {
                                double thePrincipalAmount = Double.parseDouble(loanIssueAmount);
                                double theLoanBalance = 0.0;

                                if(loanBalance.length() > 0) {
                                    theLoanBalance = Double.parseDouble(loanBalance);
                                }

                                if(thePrincipalAmount > 0 && theLoanBalance > 0) {
                                    Date dateIssued = Utils.getDateFromString(loanIssueDate,"yyyy-MM-dd");
                                    Date dateDue = Utils.getDateFromString(loanDueDate,"yyyy-MM-dd");
                                    boolean loanIssued = ledgerLinkApplication.getMeetingLoanIssuedRepo().saveMemberLoanIssue(recentMeeting.getMeetingId(), recentMember.getMemberId(), recentMember.getMemberNo(), thePrincipalAmount, theLoanBalance, dateDue);

                                    //Even if it fails continue. Can be done manually later
                                }
                            }

                            //Done with this member
                            Toast.makeText(getApplicationContext(),String.format("Member of data record %d was migrated successfully.", dataRowNo), Toast.LENGTH_SHORT).show();

                            //Increment the migrated count
                            migratedCount++;
                        }
                        catch(Exception exMember){
                            Toast.makeText(getApplicationContext(),String.format("An error has occurred. Skipping member on data record %d", dataRowNo), Toast.LENGTH_SHORT).show();
                            skippedCount++;
                            if(skippedCount > 1) {
                                skippedRows.concat(String.format(", %d", dataRowNo));
                            }
                            else {
                                skippedRows.concat(String.format("%d", dataRowNo));
                            }
                        }
                    }

                    //Final Results: Total Records in File include the HEADER row so reduce by 1
                    String result = String.format("Records Found: %d | Migrated: %d | Failed: %d", dataRowNo, migratedCount, skippedCount);

                    //in case some records were skipped
                    String skippedRecs = String.format("The Skipped Records were: %s", skippedRows);

                    //TODO: Replace this with Actual TextView
                    Toast.makeText(getApplicationContext(),result, Toast.LENGTH_SHORT).show();
                    lblMigrationResult.setVisibility(View.VISIBLE);
                    //btnimport.setVisibility(View.GONE);
                    lblMigrationResult.setText("Result:" + String.valueOf(result));

                    if(skippedCount > 0) {
                        Toast.makeText(getApplicationContext(),skippedRecs, Toast.LENGTH_SHORT).show();
                    }

                    // Flag that Data has already been Migrated
                    ledgerLinkApplication.getVslaInfoRepo().updateDataMigrationStatusFlag();

                    //TODO: Only do this if there were no records skipped
                    btnimport.setText("Finished");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
            catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(),String.format("The Data File: %s could not be located.", file), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}