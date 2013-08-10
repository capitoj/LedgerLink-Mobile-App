package org.applab.digitizingdata.helpers;

import java.util.Date;

/**
 * Created by Moses on 8/1/13.
 */
public class MemberLoanRepaymentRecord {
    int repaymentId;
    int loanId;
    int loanNo;
    Date meetingDate;
    double amount;
    double rolloverAmount;
    String comments;

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
