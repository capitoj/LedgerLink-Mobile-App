package org.applab.ledgerlink.domain.model;

/**
 * Created by JCapito on 9/17/2018.
 */

public class FinancialInstitution {

    private int fiID;
    private String name;
    private String code;
    private String ipAddress;

    public void setFiID(int fiID){
        this.fiID = fiID;
    }

    public int getFiID(){
        return this.fiID;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setCode(String code){
        this.code = code;
    }

    public String getCode(){
        return this.code;
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public String getIpAddress(){
        return this.ipAddress;
    }
}
