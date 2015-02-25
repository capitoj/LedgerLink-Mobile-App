package org.applab.ledgerlink.helpers;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;

import java.util.Date;

/**
 * Created by Moses on 8/1/13.
 */
public class MemberLoanRepaymentRecord {
    private int repaymentId;
    private int loanId;
    private int loanNo;
    private Date meetingDate;
    private double amount;
    private double rolloverAmount;
    private String comments;
    private Meeting meeting;
    private Member member;
    private double balanceBefore;
    private double balanceAfter;
    private double interestAmount;
    private Date lastDateDue;
    private Date nextDateDue;

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

    public int getRepaymentId() {
        return repaymentId;
    }

    public void setRepaymentId(int repaymentId) {
        this.repaymentId = repaymentId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRolloverAmount() {
        return rolloverAmount;
    }

    public void setRolloverAmount(double rolloverAmount) {
        this.rolloverAmount = rolloverAmount;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
