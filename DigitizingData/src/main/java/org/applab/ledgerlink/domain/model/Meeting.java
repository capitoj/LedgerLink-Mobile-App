package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by Moses on 7/3/13.
 */
public class Meeting {
    private int meetingId;
    private VslaCycle vslaCycle;
    private Date meetingDate;
    private boolean isStartOfCycle;
    private boolean isEndOfCycle;
    private boolean meetingDataSent;
    private Date dateSent;
    private boolean isCurrent;
    private double openingBalanceBox;
    private double openingBalanceBank;
    private double savings;
    private double loansRepaid;
    private double fines;
    private double loansIssued;
    private double otherExpenses;
    private double closingBalanceBank;
    private double closingBalanceBox;
    private boolean cashBookBalanced;
    private double loanFromBank;
    private double bankLoanRepayment;
    private String comment;
    private int noOfMeeting;

    //flag of whether this is a Getting started wizard meeting
    private boolean isGettingStarted;

    public Meeting(int meetingId, VslaCycle vslaCycle, Date meetingDate, boolean isStartOfCycle, boolean isEndOfCycle) {
        this.meetingId = meetingId;
        this.vslaCycle = vslaCycle;
        this.meetingDate = meetingDate;
        this.isStartOfCycle = isStartOfCycle;
        this.isEndOfCycle = isEndOfCycle;

        //Ensure getting started flag defaults to false
        setGettingStarted(false);
    }

    public Meeting() {
        //Ensure getting started flag defaults to false
        setGettingStarted(false);
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return this.comment;
    }

    public void setLoanFromBank(double amount){
        this.loanFromBank = amount;
    }

    public double getLoanFromBank(){
        return this.loanFromBank;
    }

    public void setBankLoanRepayment(double amount){
        this.bankLoanRepayment = amount;
    }

    public double getBankLoanRepayment(){
        return this.bankLoanRepayment;
    }

    public double getOpeningBalanceBox() {
        return openingBalanceBox;
    }

    public void setOpeningBalanceBox(double openingBalanceBox) {
        this.openingBalanceBox = openingBalanceBox;
    }

    public double getOpeningBalanceBank() {
        return openingBalanceBank;
    }

    public void setOpeningBalanceBank(double openingBalanceBank) {
        this.openingBalanceBank = openingBalanceBank;
    }

    public double getSavings() {
        return savings;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }

    public double getLoansRepaid() {
        return loansRepaid;
    }

    public void setLoansRepaid(double loansRepaid) {
        this.loansRepaid = loansRepaid;
    }

    public double getFines() {
        return fines;
    }

    public void setFines(double fines) {
        this.fines = fines;
    }

    public double getLoansIssued() {
        return loansIssued;
    }

    public void setLoansIssued(double loansIssued) {
        this.loansIssued = loansIssued;
    }

    public double getOtherExpenses() {
        return otherExpenses;
    }

    public void setOtherExpenses(double otherExpenses) {
        this.otherExpenses = otherExpenses;
    }

    public double getClosingBalanceBank() {
        return closingBalanceBank;
    }

    public void setClosingBalanceBank(double closingBalanceBank) {
        this.closingBalanceBank = closingBalanceBank;
    }

    public double getClosingBalanceBox() {
        return closingBalanceBox;
    }

    public void setClosingBalanceBox(double closingBalanceBox) {
        this.closingBalanceBox = closingBalanceBox;
    }

    public boolean isCashBookBalanced() {
        return cashBookBalanced;
    }

    public void setCashBookBalanced(boolean cashBookBalanced) {
        this.cashBookBalanced = cashBookBalanced;
    }

    public boolean isCurrent() {
        return this.isCurrent;
    }

    public void setIsCurrent(boolean value) {
        isCurrent = value;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public VslaCycle getVslaCycle() {
        return vslaCycle;
    }

    public void setVslaCycle(VslaCycle vslaCycle) {
        this.vslaCycle = vslaCycle;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public boolean isStartOfCycle() {
        return isStartOfCycle;
    }

    public void setStartOfCycle(boolean startOfCycle) {
        isStartOfCycle = startOfCycle;
    }

    public boolean isEndOfCycle() {
        return isEndOfCycle;
    }

    public void setEndOfCycle(boolean endOfCycle) {
        isEndOfCycle = endOfCycle;
    }

    public boolean isMeetingDataSent() {
        return meetingDataSent;
    }

    public void setMeetingDataSent(boolean meetingDataSent) {
        this.meetingDataSent = meetingDataSent;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public boolean sendMeetingData() {

        //Connect to HTTP and push the data
        this.setMeetingDataSent(true);
        this.setDateSent(new Date());
        return true;
    }

    public boolean getMeetingData() {
        return true;
    }

    public boolean isGettingStarted() {
        return isGettingStarted;
    }

    public void setGettingStarted(boolean gettingStarted) {
        isGettingStarted = gettingStarted;
    }

}
