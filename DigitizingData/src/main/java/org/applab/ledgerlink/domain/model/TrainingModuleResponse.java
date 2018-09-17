package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by JCapito on 1/14/2016.
 */
public class TrainingModuleResponse {
    protected int ID;
    protected int moduleId;
    protected String module;
    protected String training;
    protected String comment;
    protected Date date;
    protected String hashKey;

    public void setID(int ID){
        this.ID = ID;
    }

    public int getID(){
        return this.ID;
    }

    public void setModuleId(int  moduleId){
        this.moduleId = moduleId;
    }

    public int getModuleId(){
        return this.moduleId;
    }

    public void setModule(String module){
        this.module = module;
    }

    public String getModule(){
        return this.module;
    }

    public void setTraining(String training){
        this.training = training;
    }

    public String getTraining(){
        return this.training;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return this.comment;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public Date getDate(){
        return this.date;
    }

    public void setHashKey(String hashKey){
        this.hashKey = hashKey;
    }

    public String getHashKey(){
        return this.hashKey;
    }
}
