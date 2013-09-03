package org.applab.digitizingdata.domain.model;

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

    public Date DateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public Meeting(int meetingId, VslaCycle vslaCycle, Date meetingDate, boolean isStartOfCycle, boolean isEndOfCycle) {
        this.meetingId = meetingId;
        this.vslaCycle = vslaCycle;
        this.meetingDate = meetingDate;
        this.isStartOfCycle = isStartOfCycle;
        this.isEndOfCycle = isEndOfCycle;
    }

    public Meeting() {

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
}
