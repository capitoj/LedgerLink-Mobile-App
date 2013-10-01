package org.applab.digitizingdata.helpers;

import java.util.Date;

/**
 * Created by Moses on 7/29/13.
 */
public class MemberLoanIssueRecord {
    int loanId;
    int loanNo;
    Date meetingDate;
    double principalAmount;
    double interestAmount;
    double balance;
    double totalRepaid;
    boolean isCleared;
    Date dateCleared;
    Date dateDue;

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
}
