package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by Moses on 8/1/13.
 */
public class VslaInfo {
    private String vslaName;
    private String vslaCode;
    private String passKey;
    private Date dateRegistered;
    private Date dateLinked;
    private String bankBranch;
    private String accountNo;
    private boolean isActivated;
    private Date dateActivated;
    private boolean isOffline;
    private boolean allowDataMigration;
    private boolean isDataMigrated;
    private Integer fiID;

    /*
    Extra variables for gettings started wizard
       isGettingStartedWizardComplete whether the wizard ran to completion
        gettingStartedWizardStage the last stage in the wizard 0 - Welcome page, 1 - Pass Key prompt
        2- Cycle info 3 - User Info
     */
    private boolean isGettingStartedWizardComplete;
    private int gettingStartedWizardStage;

    public boolean isAllowDataMigration() {
        return allowDataMigration;
    }

    public void setAllowDataMigration(boolean allowDataMigration) {
        this.allowDataMigration = allowDataMigration;
    }

    public boolean isDataMigrated() {
        return isDataMigrated;
    }

    public void setDataMigrated(boolean dataMigrated) {
        isDataMigrated = dataMigrated;
    }

    public String getVslaName() {
        return vslaName;
    }

    public void setVslaName(String vslaName) {
        this.vslaName = vslaName;
    }

    public String getVslaCode() {
        return vslaCode;
    }

    public void setVslaCode(String vslaCode) {
        this.vslaCode = vslaCode;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public Date getDateLinked() {
        return dateLinked;
    }

    public void setDateLinked(Date dateLinked) {
        this.dateLinked = dateLinked;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public Date getDateActivated() {
        return dateActivated;
    }

    public void setDateActivated(Date dateActivated) {
        this.dateActivated = dateActivated;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public boolean isGettingStartedWizardComplete() {
        return isGettingStartedWizardComplete;
    }

    public void setGettingStartedWizardComplete(boolean gettingStartedWizardComplete) {
        isGettingStartedWizardComplete = gettingStartedWizardComplete;
    }

    public int getGettingStartedWizardStage() {
        return gettingStartedWizardStage;
    }

    public void setGettingStartedWizardStage(int gettingStartedWizardStage) {
        this.gettingStartedWizardStage = gettingStartedWizardStage;
    }

    public void setFiID(Integer fiID){
        this.fiID = fiID;
    }

    public Integer getFiID(){
        return this.fiID;
    }
}
