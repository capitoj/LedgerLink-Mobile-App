package org.applab.digitizingdata.datatransformation;

import java.util.Date;

/**
 * Created by Moses on 10/25/13.
 */
public class RepaymentDataTransferRecord {

    private int repaymentId;
    private int memberId;
    private int meetingId;
    private int loanId;
    private double amount;
    private double balanceBefore;
    private double balanceAfter;
    private double interestAmount;
    private double rollOverAmount;
    private String comments;
    private Date lastDateDue;
    private Date nextDateDue;

    public int getRepaymentId() {
        return repaymentId;
    }

    public void setRepaymentId(int repaymentId) {
        this.repaymentId = repaymentId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public double getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(double interestAmount) {
        this.interestAmount = interestAmount;
    }

    public double getRollOverAmount() {
        return rollOverAmount;
    }

    public void setRollOverAmount(double rollOverAmount) {
        this.rollOverAmount = rollOverAmount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getLastDateDue() {
        return lastDateDue;
    }

    public void setLastDateDue(Date lastDateDue) {
        this.lastDateDue = lastDateDue;
    }

    public Date getNextDateDue() {
        return nextDateDue;
    }

    public void setNextDateDue(Date nextDateDue) {
        this.nextDateDue = nextDateDue;
    }
}
