package org.applab.digitizingdata.domain.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 6/25/13.
 */
public class Member {

    private int memberId;
    private int memberNo;
    private String globalId;
    private String surname;
    private String otherNames;
    private String gender;
    private String occupation;
    private Date dateOfBirth;
    private Date dateOfAdmission;
    private boolean isActive;
    private Date dateOfSeparation;
    private String phoneNumber;
    private int cyclesCompleted;
    private int creditGrade; //1.Good 2.Doubtful 3.Bad
    private double currentShareAmount;
    private double outstandingLoan;

    //Values for Getting started wizard
    private double savingsOnSetup;
    private double outstandingLoanOnSetup;

    @Override
    public String toString() {
        return String.format("%s. %s, %s", memberNo, surname, otherNames);
    }

    public String getFullNames() {
        return String.format("%s, %s", surname, otherNames);
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(int memberNo) {
        this.memberNo = memberNo;
    }

    public String getGlobalId() {
        return globalId;
    }

    public void setGlobalId(String globalId) {
        this.globalId = globalId;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getDateOfAdmission() {
        return dateOfAdmission;
    }

    public void setDateOfAdmission(Date dateOfAdmission) {
        this.dateOfAdmission = dateOfAdmission;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public Date getDateOfSeparation() {
        return dateOfSeparation;
    }

    public void setDateOfSeparation(Date dateOfSeparation) {
        this.dateOfSeparation = dateOfSeparation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCyclesCompleted() {
        try {
            Calendar calToday = Calendar.getInstance();
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(this.getDateOfAdmission());
            cyclesCompleted = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            return cyclesCompleted;
        }
        catch(Exception ex) {
            return 0;
        }
    }

    public void setCyclesCompleted(int cyclesCompleted) {
        this.cyclesCompleted = cyclesCompleted;
    }

    public int getCreditGrade() {
        return creditGrade;
    }

    public void setCreditGrade(int creditGrade) {
        this.creditGrade = creditGrade;
    }

    public double getCurrentShareAmount() {
        return currentShareAmount;
    }

    public void setCurrentShareAmount(double currentShareAmount) {
        this.currentShareAmount = currentShareAmount;
    }

    public double getOutstandingLoan() {
        return outstandingLoan;
    }

    public void setOutstandingLoan(double outstandingLoan) {
        this.outstandingLoan = outstandingLoan;
    }

    public Member(int memberId, int memberNo, String surname, String otherNames, String gender, String occupation, Date dateOfBirth, Date dateOfAdmission, boolean active, String phoneNumber, int cyclesCompleted, double currentShareAmount, double outstandingLoan) {
        this.memberId = memberId;
        this.memberNo = memberNo;
        this.surname = surname;
        this.otherNames = otherNames;
        this.gender = gender;
        this.occupation = occupation;
        this.dateOfBirth = dateOfBirth;
        this.dateOfAdmission = dateOfAdmission;
        isActive = active;
        this.phoneNumber = phoneNumber;
        this.cyclesCompleted = cyclesCompleted;
        this.currentShareAmount = currentShareAmount;
        this.outstandingLoan = outstandingLoan;
    }

    public Member(int memberNo, String surname, String otherNames, String gender, String occupation, Date dateOfBirth, Date dateOfAdmission, boolean active, String phoneNumber, int cyclesCompleted, double currentShareAmount, double outstandingLoan) {
        this(0, memberNo, surname, otherNames, null, null, null, null, false, null, 0, 0, 0);
    }

    public Member(int memberNo, String surname, String otherNames) {
        this(memberNo, surname, otherNames, null, null, null, null, false, null, 0, 0, 0);
    }
    public Member(int memberId, int memberNo, String surname, String otherNames) {
        this(memberId, memberNo, surname, otherNames, null, null, null, null, false, null, 0, 0, 0);
    }

    public Member(int memberNo) {
        this(memberNo,null,null,null,null,null,null, false, null,0,0,0);
    }

    public Member() {

    }

    public double getSavingsOnSetup() {
        return savingsOnSetup;
    }

    public void setSavingsOnSetup(double savingsOnSetup) {
        this.savingsOnSetup = savingsOnSetup;
    }

    public double getOutstandingLoanOnSetup() {
        return outstandingLoanOnSetup;
    }

    public void setOutstandingLoanOnSetup(double outstandingLoanOnSetup) {
        this.outstandingLoanOnSetup = outstandingLoanOnSetup;
    }
}
