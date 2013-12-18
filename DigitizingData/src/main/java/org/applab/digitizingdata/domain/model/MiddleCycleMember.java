package org.applab.digitizingdata.domain.model;

/**
 * Created with IntelliJ IDEA.
 * User: John Mark
 * Date: 12/9/13
 * Time: 2:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class MiddleCycleMember extends Member {
    private double savingsSoFar;
    private double outstandingLoan;

    public double getSavingsSoFar() {
        return savingsSoFar;
    }

    public void setSavingsSoFar(double savingsSoFar) {
        this.savingsSoFar = savingsSoFar;
    }

    public double getOutstandingLoan() {
        return outstandingLoan;
    }

    public void setOutstandingLoan(double outstandingLoan) {
        this.outstandingLoan = outstandingLoan;
    }
}
