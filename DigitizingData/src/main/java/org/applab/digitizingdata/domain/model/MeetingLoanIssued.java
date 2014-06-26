package org.applab.digitizingdata.domain.model;

import java.util.Date;

/**
 * Created by Moses on 7/9/13.
 */
public class MeetingLoanIssued {
    private int loanId;
    private Meeting meeting;
    private Member member;
    private double principalAmount;
    private double interestAmount;
    private Date dateDue;
    private int loanNo;
    private double totalRepaid;
    private double loanBalance;
    private boolean isDefaulted;
    private boolean isCleared;
    private Date dateCleared;
    private String comment;
    private boolean isWrittenOff;

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

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isWrittenOff() {
        return isWrittenOff;
    }

    public void setWrittenOff(boolean writtenOff) {
        isWrittenOff = writtenOff;
    }
}
