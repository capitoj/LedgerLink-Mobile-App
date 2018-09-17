package org.applab.ledgerlink.repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.MessageChannel;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by JCapito on 7/26/2016.
 */
public class MessageChannelRepo {
    protected Context context;
    protected MessageChannel messageChannel;
    protected int msgID;

    public MessageChannelRepo(Context context){
        this.context = context;
    }

    public MessageChannelRepo(Context context, int msgID){
        this.context = context;
        this.messageChannel = new MessageChannel();
        this.msgID = msgID;
        this.load();
    }

    protected void load(){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "select msg_id, source, destination, timestamp, message, status from MessageChannels where msg_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(msgID)});
        if(cursor.moveToNext()) {
            messageChannel.setMsgID(cursor.getInt(cursor.getColumnIndex("msg_id")));
            messageChannel.setTo(cursor.getString(cursor.getColumnIndex("destination")));
            messageChannel.setFrom(cursor.getString(cursor.getColumnIndex("source")));
            messageChannel.setMessage(cursor.getString(cursor.getColumnIndex("message")));
            messageChannel.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            messageChannel.setTimetamp(cursor.getString(cursor.getColumnIndex("timestamp")));
        }
        cursor.close();
        db.close();
    }

    public MessageChannel getMessageChannel(){
        return messageChannel;
    }

    protected int _addMessage(MessageChannel messageChannel){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "insert into MessageChannels (source, destination, timestamp, message, status) values (?, ?, ?, ?, ?)";
        String dateTime = String.valueOf(new java.sql.Timestamp(new Date().getTime()));
        db.execSQL(sql, new String[]{messageChannel.getFrom(), messageChannel.getTo(), dateTime, messageChannel.getMessage(), String.valueOf(messageChannel.getStatus())});
        db.close();
        return getLastInsertRowId();

    }

    protected int getLastInsertRowId(){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "select msg_id from MessageChannels order by msg_id desc limit 1";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToNext();
        int rowID = cursor.getInt(cursor.getColumnIndex("msg_id"));
        cursor.close();
        db.close();
        return rowID;
    }


    public static int addMessage(Context context, MessageChannel messageChannel){
        return new MessageChannelRepo(context)._addMessage(messageChannel);
    }

    public static void updateMessage(Context context, MessageChannel messageChannel){
        new MessageChannelRepo(context)._updateMessage(messageChannel);
    }

    protected void _updateMessage(MessageChannel messageChannel){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "update MessageChannels set status = ? where msg_id = ?";
        db.execSQL(sql, new String[]{String.valueOf(messageChannel.getStatus()), String.valueOf(messageChannel.getMsgID())});
    }

    protected List<MessageChannel> _getMessages(){
        List<MessageChannel> messageChannelList = new ArrayList();
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "select msg_id from MessageChannels";
        Cursor cursor = db.rawQuery(sql, null);
        while(cursor.moveToNext()){
            messageChannelList.add(new MessageChannelRepo(context, cursor.getInt(cursor.getColumnIndex("msg_id"))).getMessageChannel());
        }
        cursor.close();
        db.close();
        return messageChannelList;
    }

    protected List<MessageChannel> _getUnsentMessages(){
        List<MessageChannel> messageChannelList = new ArrayList();
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "select msg_id from MessageChannels where status = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(0)});
        while(cursor.moveToNext()){
            messageChannelList.add(new MessageChannelRepo(context, cursor.getInt(cursor.getColumnIndex("msg_id"))).getMessageChannel());
        }
        cursor.close();
        db.close();
        return messageChannelList;
    }

    protected MessageChannel _getMessageAtIndex(int index){
        MessageChannel messageChannel = null;
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "select msg_id from MessageChannels limit ?, 1";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(index)});
        if(cursor.moveToNext()){
            messageChannel = new MessageChannelRepo(context, cursor.getInt(cursor.getColumnIndex("msg_id"))).getMessageChannel();
        }
        cursor.close();
        db.close();
        return messageChannel;
    }

    public static List<MessageChannel> getUnsentMessages(Context context){
        return new MessageChannelRepo(context)._getUnsentMessages();
    }

    public static List<MessageChannel> getMessages(Context context){
        return new MessageChannelRepo(context)._getMessages();
    }

    public static MessageChannel getMessageAtIndex(Context context, int index){
        return new MessageChannelRepo(context)._getMessageAtIndex(index);
    }
}
