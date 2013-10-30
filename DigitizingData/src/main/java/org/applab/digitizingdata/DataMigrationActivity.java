package org.applab.digitizingdata;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.repo.MemberRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataMigrationActivity extends Activity implements OnClickListener {

    private static final String TAG = "MainActivity";
    // DatabaseHandler dbHelper = null;
    File file = null;
    private Button btnexport = null;
    private Button btnimport = null;
    private Button btninsertdata = null;
    private Button btnsendmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_migration);

        Log.v(TAG, "onCreate called");

        btnexport = (Button) findViewById(R.id.button_export);
        btnexport.setOnClickListener(this);
        btninsertdata = (Button) findViewById(R.id.button_Insert);
        btninsertdata.setOnClickListener(this);
        btnsendmail = (Button) findViewById(R.id.button_sendmail);
        btnsendmail.setOnClickListener(this);
        btnimport = (Button) findViewById(R.id.button_import);
        btnimport.setOnClickListener(this);

        //   dbHelper = new DatabaseHandler(getApplicationContext());
    }

    @Override
    public void onClick(View arg0) {

        Log.v(TAG, "onClick called");
        if (arg0 == btnexport) {
            // Do Nothing
        }
        else if (arg0 == btnimport) {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            file = new File(exportDir, Utils.VSLA_DATA_MIGRATION_FILENAME);
            try {

                //If the meeting has not been created start with it
                //Retrieve the Current Cycle
                VslaCycleRepo cycleRepo = new VslaCycleRepo(getApplicationContext());
                VslaCycle mostRecentCycle = cycleRepo.getMostRecentCycle();
                if(null == mostRecentCycle) {
                    //Toast that there is no Cycle
                    Toast.makeText(getApplicationContext(),"There is no existing cycle.", Toast.LENGTH_LONG).show();
                    return;
                }

                //Get the Meeting if it is there
                MeetingRepo meetingRepo = new MeetingRepo(getApplicationContext());
                Meeting recentMeeting = meetingRepo.getMostRecentMeeting();
                if(null == recentMeeting) {
                    //Create a meeting
                    Meeting newMeeting = new Meeting();
                    Calendar calMeetingDate = Calendar.getInstance();
                    newMeeting.setMeetingDate(calMeetingDate.getTime());
                    newMeeting.setVslaCycle(mostRecentCycle);
                    newMeeting.setIsCurrent(true);

                    boolean meetingCreated = meetingRepo.addMeeting(newMeeting);
                    if(meetingCreated) {
                        //Retrieve the current Meeting
                        recentMeeting = meetingRepo.getMeetingByDate(newMeeting.getMeetingDate());
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
                    int migratedCount = 0;
                    int skippedCount = 0;
                    String skippedRows = "";
                    while ((nextLine = reader.readNext()) != null) {

                        if(0 >= i++) {
                            continue;
                        }
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
                        String loanBalance = nextLine[13].trim();
                        String loanDueDate = nextLine[14].trim();

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
                            MemberRepo memberRepo = new MemberRepo(getApplicationContext());
                            boolean memberNoAvailable = memberRepo.isMemberNoAvailable(member.getMemberNo(),member.getMemberId());

                            //Loop to get a Unique member No. Hope no Infinite Loops
                            while(memberNoAvailable == false) {
                                member.setMemberNo(member.getMemberNo() + 1);
                                memberNoAvailable = memberRepo.isMemberNoAvailable(member.getMemberNo(),member.getMemberId());
                            }
                            boolean memberAdded = memberRepo.addMember(member);
                            if(memberAdded) {
                                //retrieve the MemberId
                                recentMember = memberRepo.getMemberByMemberNo(member.getMemberNo());
                            }

                            //Continue only if we have added the member
                            if(null == recentMember) {
                                Toast.makeText(getApplicationContext(),String.format("Member on record %d could not be added.", i), Toast.LENGTH_LONG).show();
                                skippedCount++;
                                continue;
                            }

                            //Now deal with Savings
                            MeetingSavingRepo savingRepo = new MeetingSavingRepo(getApplicationContext());

                            if(totalSavings.length() > 0){
                                double theSavings = Double.parseDouble(totalSavings);
                                boolean savingsDone = savingRepo.saveMemberSaving(recentMeeting.getMeetingId(),recentMember.getMemberId(), theSavings);
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
                                    MeetingLoanIssuedRepo loansRepo = new MeetingLoanIssuedRepo(getApplicationContext());
                                    boolean loanIssued = loansRepo.saveMemberLoanIssue(recentMeeting.getMeetingId(),recentMember.getMemberId(),recentMember.getMemberNo(),thePrincipalAmount,0.0,dateDue);

                                    //Even if it fails continue. Can be done manually later
                                }
                            }

                            //Done with this member
                            Toast.makeText(getApplicationContext(),String.format("Member of record %d was migrated successfully.", i), Toast.LENGTH_LONG).show();

                            //Increment the migrated count
                            migratedCount++;
                        }
                        catch(Exception exMember){
                            Toast.makeText(getApplicationContext(),String.format("An error has occurred. Skipping member on record %d", i), Toast.LENGTH_LONG).show();
                            skippedCount++;
                            if(skippedCount > 1) {
                                skippedRows.concat(String.format(", %d", i));
                            }
                            else {
                                skippedRows.concat(String.format("%d", i));
                            }
                            continue;
                        }
                    }

                    //Final Results:
                    String result = String.format("Records Found: %d | Migrated: %d | Failed: %d", i, migratedCount, skippedCount);
                    String skippedRecs = String.format("The Skipped Records were: %s", skippedRows);

                    //TODO: Replace this with Actual TextView
                    Toast.makeText(getApplicationContext(),result, Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(),skippedRecs, Toast.LENGTH_LONG).show();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}