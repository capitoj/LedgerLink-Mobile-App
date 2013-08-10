package org.applab.digitizingdata.domain.model;

/**
 * Created by Moses on 7/9/13.
 */
public class MeetingLoanRepayment {
    private int repaymentId;
    private MeetingLoanIssued loanIssued;
    private Meeting meeting;
    private Member member;
    private double amountPaid;
    private double balanceBefore;
    private double balanceAfter;
    private double interestAmount;
    private double rollOverAmount;
    private String comments;
}
