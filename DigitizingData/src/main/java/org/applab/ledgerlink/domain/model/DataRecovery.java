package org.applab.ledgerlink.domain.model;

/**
 * Created by JCapito on 9/15/2015.
 */
public class DataRecovery {

    protected String vslaCode;
    protected String vslaName;
    protected String passKey;
    protected String contactPerson;
    protected String phoneNumber;
    protected String positionInVsla;
    protected int noOfMembers;
    protected String vslaRegion;

    public void setVslaCode(String vslaCode){
        this.vslaCode = vslaCode;
    }

    public String getVslaCode(){
        return this.vslaCode;
    }

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

    public void setContactPerson(String contactPerson){
        this.contactPerson = contactPerson;
    }

    public String getContactPerson(){
        return this.contactPerson;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }

    public void setPositionInVsla(String positionInVsla){
        this.positionInVsla = positionInVsla;
    }

    public String getPositionInVsla(){
        return this.positionInVsla;
    }

    public void setVslaRegion(String vslaRegion){
        this.vslaRegion = vslaRegion;
    }

    public String getVslaRegion(){
        return this.vslaRegion;
    }

    public void setNoOfMembers(int noOfMembers){
        this.noOfMembers = noOfMembers;
    }

    public int getNoOfMembers(){
        return this.noOfMembers;
    }
}
