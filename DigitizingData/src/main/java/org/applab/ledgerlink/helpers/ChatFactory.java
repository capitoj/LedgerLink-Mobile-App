package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.util.Log;

import org.applab.ledgerlink.domain.model.MessageChannel;
import org.applab.ledgerlink.repo.MessageChannelRepo;
import org.json.JSONStringer;

import java.util.List;

/**
 * Created by JCapito on 8/2/2016.
 */
public class ChatFactory {

    protected Context context;

    public ChatFactory(Context context){
        this.context = context;
    }

    protected JSONStringer getUnsentChats(JSONStringer js){
        List<MessageChannel> messageChannelList = MessageChannelRepo.getUnsentMessages(context);
        if(messageChannelList.size() > 0){
            try{
                js.key("UnDeliveredMessages").array();
                for(MessageChannel messageChannel : messageChannelList){
                    js.object()
                            .key("MsgId").value(messageChannel.getMsgID())
                            .key("Source").value(messageChannel.getFrom())
                            .key("Destination").value(messageChannel.getTo())
                            .key("Message").value(messageChannel.getMessage())
                            .key("Timestamp").value(messageChannel.getTimestamp())
                            .key("Status").value(messageChannel.getStatus())
                            .endObject();
                }
                js.endArray();
            }catch (Exception e){
                Log.e("getUnsentChats", e.getMessage());
            }
        }
        return js;
    }

    public static JSONStringer getJSONOutput(Context context, JSONStringer js){
        return new ChatFactory(context).getUnsentChats(js);
    }
}
