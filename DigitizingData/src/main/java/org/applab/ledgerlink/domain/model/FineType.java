package org.applab.ledgerlink.domain.model;

/**
 * Created by Moses on 4/1/14.
 */
public class FineType {
    private int fineTypeId;
    private String fineTypeName;
    private String fineTypeDesc;
    private double defaultAmount;

    public FineType(){

    }
    public FineType(int fineTypeId, String fineTypeName) {
        this.fineTypeId = fineTypeId;
        this.fineTypeName = fineTypeName;
    }

    public int getFineTypeId() {
        return fineTypeId;
    }

    public void setFineTypeId(int fineTypeId) {
        this.fineTypeId = fineTypeId;
    }

    public String getFineTypeName() {
        return fineTypeName;
    }

    public void setFineTypeName(String fineTypeName) {
        this.fineTypeName = fineTypeName;
    }

    public String getFineTypeDesc() {
        return fineTypeDesc;
    }

    public void setFineTypeDesc(String fineTypeDesc) {
        this.fineTypeDesc = fineTypeDesc;
    }

    public double getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(double defaultAmount) {
        this.defaultAmount = defaultAmount;
    }
}
