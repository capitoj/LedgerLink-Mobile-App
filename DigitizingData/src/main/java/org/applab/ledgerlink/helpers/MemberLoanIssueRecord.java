package org.applab.ledgerlink.helpers;

import java.util.Date;

/**
 * Created by Moses on 7/29/13.
 */
public class MemberLoanIssueRecord {
    private int loanId;
    private int loanNo;
    private Date meetingDate;
    private double principalAmount;
    private double interestAmount;
    private double balance;
    private double totalRepaid;
    private boolean isCleared;
    private Date dateCleared;
    private Date dateDue;
    private String lastRepaymentComment;

    public double getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(double interestAmount) {
        this.interestAmount = interestAmount;
    }

    public Date getDateDue() {
        return dateDue;
    }

    public void setDateDue(Date dateDue) {
        this.dateDue = dateDue;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(int loanNo) {
        this.loanNo = loanNo;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getTotalRepaid() {
        return totalRepaid;
    }

    public void setTotalRepaid(double totalRepaid) {
        this.totalRepaid = totalRepaid;
    }

    public boolean isCleared() {
        return isCleared;
    }

    public void setCleared(boolean cleared) {
        isCleared = cleared;
    }

    public Date getDateCleared() {
        return dateCleared;
    }

    public void setDateCleared(Date dateCleared) {
        this.dateCleared = dateCleared;
    }

    public String getLastRepaymentComment() {
        return lastRepaymentComment;
    }

    public void setLastRepaymentComment(String lastRepaymentComment) {
        this.lastRepaymentComment = lastRepaymentComment;
    }


}
