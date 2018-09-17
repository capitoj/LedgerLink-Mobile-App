package org.applab.ledgerlink.domain.model;

/**
 * Created by JCapito on 7/26/2016.
 */
public class MessageChannel {

    protected int msgID;
    protected String from;
    protected String to;
    protected String timestamp;
    protected int status;
    protected String message;

    public void setMsgID(int msgID){
        this.msgID = msgID;
    }

    public int getMsgID(){
        return this.msgID;
    }

    public void setFrom(String from){
        this.from = from;
    }

    public String getFrom(){
        return this.from;
    }

    public void setTo(String to){
        this.to = to;
    }

    public String getTo(){
        return this.to;
    }

    public void setTimetamp(String timestamp){
        this.timestamp = timestamp;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public int getStatus(){
        return this.status;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
