package org.applab.digitizingdata.datatransformation;

import java.util.Date;

/**
 * Created by Moses on 10/25/13.
 */
public class LoanDataTransferRecord {
    private int loanId;
    private int meetingId;
    private int memberId;
    private double principalAmount;
    private double interestAmount;
    private Date dateDue;
    private int loanNo;
    private double totalRepaid;
    private double loanBalance;
    private boolean isDefaulted;
    private boolean isCleared;
    private Date dateCleared;
    private String comments;
    private boolean isWrittenOff;

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

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

    public int getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(int loanNo) {
        this.loanNo = loanNo;
    }

    public double getTotalRepaid() {
        return totalRepaid;
    }

    public void setTotalRepaid(double totalRepaid) {
        this.totalRepaid = totalRepaid;
    }

    public double getLoanBalance() {
        return loanBalance;
    }

    public void setLoanBalance(double loanBalance) {
        this.loanBalance = loanBalance;
    }

    public boolean isDefaulted() {
        return isDefaulted;
    }

    public void setDefaulted(boolean defaulted) {
        isDefaulted = defaulted;
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isWrittenOff() {
        return isWrittenOff;
    }

    public void setWrittenOff(boolean writtenOff) {
        isWrittenOff = writtenOff;
    }
}
