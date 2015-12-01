package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by Moses on 7/9/13.
 */
public class MeetingLoanRepayment {
    private int repaymentId;
    private MeetingLoanIssued meetingLoanIssued;
    private Meeting meeting;
    private Member member;
    private double amount;
    private double balanceBefore;
    private double balanceAfter;
    private double interestAmount;
    private double rollOverAmount;
    private String comment;
    private Date lastDateDue;
    private Date nextDateDue;

    public MeetingLoanRepayment() {
    }

    public void setRepaymentId(int repaymentId) {
        this.repaymentId = repaymentId;
    }

    public int getRepaymentId() {
        return this.repaymentId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public double getBalanceBefore() {
        return this.balanceBefore;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public double getBalanceAfter() {
        return this.balanceAfter;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public void setLastDateDue(Date lastDateDue) {
        this.lastDateDue = lastDateDue;
    }

    public Date getLastDateDue() {
        return this.lastDateDue;
    }

    public void setNextDateDue(Date nextDateDue) {
        this.nextDateDue = nextDateDue;
    }

    public Date getNextDateDue() {
        return this.nextDateDue;
    }

    public void setInterestAmount(double interestAmount) {
        this.interestAmount = interestAmount;
    }

    public double getInterestAmount() {
        return this.interestAmount;
    }

    public void setRollOverAmount(double rollOverAmount) {
        this.rollOverAmount = rollOverAmount;
    }

    public double getRollOverAmount(){
        return this.rollOverAmount;
    }

    public void setMeeting(Meeting meeting){
        this.meeting = meeting;
    }

    public Meeting getMeeting(){
        return this.meeting;
    }

    public void setMember(Member member){
        this.member = member;
    }

    public Member getMember(){
        return this.member;
    }

    public void setMeetingLoanIssued(MeetingLoanIssued meetingLoanIssued){
        this.meetingLoanIssued = meetingLoanIssued;
    }

    public MeetingLoanIssued getMeetingLoanIssued(){
        return this.meetingLoanIssued;
    }

}
