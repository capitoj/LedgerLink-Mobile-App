package org.applab.ledgerlink.domain.model;

/**
 * Created by JCapito on 9/15/2015.
 */
public class PassKeyReset {

    protected String vslaName;
    protected String passKey;
    protected String noOfMembers;
    private String noOfMeetings;
    private String noOfCyclesCompleted;

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

    public void setNoOfMembers(String noOfMembers){
        this.noOfMembers = noOfMembers;
    }

    public String getNoOfMembers(){
        return this.noOfMembers;
    }

    public void setNoOfMeetings(String noOfMeetings){
        this.noOfMeetings = noOfMeetings;
    }

    public String getNoOfMeetings(){
        return this.noOfMeetings;
    }

    public void setNoOfCyclesCompleted(String noOfCyclesCompleted){
        this.noOfCyclesCompleted = noOfCyclesCompleted;
    }

    public String getNoOfCyclesCompleted(){
        return this.noOfCyclesCompleted;
    }
}
