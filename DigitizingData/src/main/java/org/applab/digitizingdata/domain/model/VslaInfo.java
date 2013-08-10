package org.applab.digitizingdata.domain.model;

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
}
