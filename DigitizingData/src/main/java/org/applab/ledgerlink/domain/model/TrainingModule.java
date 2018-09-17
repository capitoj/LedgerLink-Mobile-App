package org.applab.ledgerlink.domain.model;

import java.util.Date;

/**
 * Created by JCapito on 1/7/2016.
 */
public class TrainingModule {

    protected String module;
    protected int ID;
    protected String comment;

    public TrainingModule(String module){
        this.module = module;
    }
    public TrainingModule(int ID, String module){
        this.module = module;
        this.ID = ID;
    }

    public void setModule(String module){
        this.module = module;
    }

    public String getModule(){
        return this.module;
    }

    public void setID(int ID){
        this.ID = ID;
    }

    public int getID(){
        return this.ID;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public  String getComment(){
        return this.comment;
    }
}
