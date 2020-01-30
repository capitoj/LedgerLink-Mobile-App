package org.applab.ledgerlink.domain.model;

/**
 * Created by JCapito on 9/15/2015.
 */
public class PassKeyReset {

    protected String vslaName;
    protected String passKey;
    protected int noOfMembers;
    private int noOfMeetings;
    private int noOfCyclesCompleted;

    public void setVslaName(String vslaName){
        this.vslaName = vslaName;
    }

    public String getVslaName(){
        return this.vslaName;
    }

    public String getPassKey(){
        return this.passKey;
    }

    public void setPassKey(String passKey){
        this.passKey = passKey;
    }

    public void setNoOfMembers(int noOfMembers){
        this.noOfMembers = noOfMembers;
    }

    public int getNoOfMembers(){
        return this.noOfMembers;
    }

    public void setNoOfMeetings(int noOfMeetings){
        this.noOfMeetings = noOfMeetings;
    }

    public int getNoOfMeetings(){
        return this.noOfMeetings;
    }

    public void setNoOfCyclesCompleted(int noOfCyclesCompleted){
        this.noOfCyclesCompleted = noOfCyclesCompleted;
    }

    public int getNoOfCyclesCompleted(){
        return this.noOfCyclesCompleted;
    }
}
