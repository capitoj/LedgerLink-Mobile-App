package org.applab.ledgerlink.domain.schema;

/**
 * Created by Joseph Capito on 7/26/2016.
 */
public class MessageChannelsSchema {
    public final static String TBL_MESSAGE_CHANNELS = "MessageChannels";

    public static String getCreateTableScript(){
        return "CREATE TABLE IF NOT EXISTS " + TBL_MESSAGE_CHANNELS + " (msg_id INTEGER PRIMARY KEY AUTOINCREMENT,  source TEXT, destination TEXT, message TEXT, timestamp TEXT, status INTEGER)";
    }

    public static String getDropTableScript(){
        return "DROP TABLE IF EXISTS " + TBL_MESSAGE_CHANNELS;
    }

    public static String getTableName(){
        return TBL_MESSAGE_CHANNELS;
    }
}
