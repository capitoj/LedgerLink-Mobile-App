package org.applab.digitizingdata.domain.model;

/**
 * Created with IntelliJ IDEA.
 * User: John Mark
 * Date: 12/9/13
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class VslaMiddleStartCycle extends VslaCycle {
    private double interestReceived;
    private double finesCollected;

    public double getInterestReceived() {
        return interestReceived;
    }

    public void setInterestReceived(double interestReceived) {
        this.interestReceived = interestReceived;
    }

    public double getFinesCollected() {
        return finesCollected;
    }

    public void setFinesCollected(double finesCollected) {
        this.finesCollected = finesCollected;
    }
}
