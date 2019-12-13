package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by Moses on 6/25/13.
 */
public class VslaCycle {

    private int cycleId;
    private String cycleCode;
    private Date startDate;
    private Date endDate;
    private double sharePrice;
    private double maxSharesQty;
    private double maxStartShare;
    private double interestRate;
    private boolean isActive;
    private boolean isEnded;
    private Date dateEnded;
    private double sharedAmount;
    private double interestAtSetup;
    private double finesAtSetup;
    private double outstandingBankLoanAtSetup;
    private String interestAtSetupCorrectionComment;
    private String finesAtSetupCorrectionComment;
    private int typeOfInterest;

    public VslaCycle() {

    }

    public VslaCycle(int cycleId, String cycleCode, Date startDate, Date endDate, double sharePrice, double maxSharesQty, double maxStartShare, double interestRate) {
        this.cycleId = cycleId;
        this.cycleCode = cycleCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sharePrice = sharePrice;
        this.maxSharesQty = maxSharesQty;
        this.maxStartShare = maxStartShare;
        this.interestRate = interestRate;
        setInterestAtSetup(0);
        setFinesAtSetup(0);
        setOutstandingBankLoanAtSetup(0);
    }

    public VslaCycle(int cycleId, String cycleCode, Date startDate, Date endDate, double sharePrice, double maxSharesQty, double maxStartShare, double interestRate, double interestAtSetup, double finesAtSetup, double outstandingBankLoanAtSetup) {
        this.cycleId = cycleId;
        this.cycleCode = cycleCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sharePrice = sharePrice;
        this.maxSharesQty = maxSharesQty;
        this.maxStartShare = maxStartShare;
        this.interestRate = interestRate;
        setInterestAtSetup(interestAtSetup);
        setFinesAtSetup(finesAtSetup);
        setOutstandingBankLoanAtSetup(outstandingBankLoanAtSetup);
    }

    public VslaCycle(int cycleId) {
        this(cycleId, null, null, null, 0.0, 0.0, 0.0, 0.0);
    }

    public VslaCycle(int cycleId, Date startDate, Date endDate) {
        this(cycleId, null, startDate, endDate, 0.0, 0.0, 0.0, 0.0);
    }

    public int getCycleId() {
        return cycleId;
    }

    public void setCycleId(int cycleId) {
        this.cycleId = cycleId;
    }

    public String getCycleCode() {
        return cycleCode;
    }

    public void setCycleCode(String cycleCode) {
        this.cycleCode = cycleCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getSharePrice() {
        return sharePrice;
    }

    public void setSharePrice(double sharePrice) {
        this.sharePrice = sharePrice;
    }

    public double getMaxSharesQty() {
        return maxSharesQty;
    }

    public void setMaxSharesQty(double maxSharesQty) {
        this.maxSharesQty = maxSharesQty;
    }

    public double getMaxStartShare() {
        return maxStartShare;
    }

    public void setMaxStartShare(double maxStartShare) {
        this.maxStartShare = maxStartShare;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void end(Date dateEnded) {
        end(dateEnded, 0.0);
    }

    public void end(Date dateEnded, double sharedAmount) {
        isEnded = true;
        this.dateEnded = dateEnded;
        this.sharedAmount = sharedAmount;

        //Not very sure I need this
        this.deactivate();
    }

    public Date getDateEnded() {
        return dateEnded;
    }

    public double getSharedAmount() {
        return sharedAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getInterestAtSetup() {
        return interestAtSetup;
    }

    public void setInterestAtSetup(double interestAtSetup) {
        this.interestAtSetup = interestAtSetup;
    }

    public void setOutstandingBankLoanAtSetup(double outstandingBankLoanAtSetup){
        this.outstandingBankLoanAtSetup = outstandingBankLoanAtSetup;
    }

    public double getOutstandingBankLoanAtSetup(){
        return this.outstandingBankLoanAtSetup;
    }

    public String getInterestAtSetupCorrectionComment() {
        return interestAtSetupCorrectionComment;
    }

    public void setInterestAtSetupCorrectionComment(String interestAtSetupCorrectionComment) {
        this.interestAtSetupCorrectionComment = interestAtSetupCorrectionComment;
    }

    public double getFinesAtSetup() {
        return finesAtSetup;
    }

    public void setFinesAtSetup(double finesAtSetup) {
        this.finesAtSetup = finesAtSetup;
    }

    public String getFinesAtSetupCorrectionComment() {
        return finesAtSetupCorrectionComment;
    }

    public void setFinesAtSetupCorrectionComment(String finesAtSetupCorrectionComment) {
        this.finesAtSetupCorrectionComment = finesAtSetupCorrectionComment;
    }

    public void setTypeOfInterest(int typeOfInerest){
        this.typeOfInterest = typeOfInerest;
    }

    public int getTypeOfInterest(){
        return this.typeOfInterest;
    }

}