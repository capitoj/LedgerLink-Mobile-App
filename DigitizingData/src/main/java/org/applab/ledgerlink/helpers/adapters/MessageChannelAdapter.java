package org.applab.ledgerlink.helpers.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.MessageChannel;

import java.util.List;

/**
 * Created by JCapito on 7/26/2016.
 */
public class MessageChannelAdapter extends ArrayAdapter<MessageChannel> {
    protected Context context;
    protected List<MessageChannel> messageChannelList;

    public MessageChannelAdapter(Context context, List<MessageChannel> messageChannelList){
        super(context, R.layout.item_view_chat_message, messageChannelList);
        this.context = context;
        this.messageChannelList = messageChannelList;
    }

    public View getView(int position, View view, ViewGroup viewGroup){
        View itemView = view;
        if(itemView == null){
            itemView = ((Activity)context).getLayoutInflater().inflate(R.layout.item_view_chat_message, viewGroup, false);
        }
        MessageChannel messageChannel = messageChannelList.get(position);
        TextView txtChatMesage = (TextView)itemView.findViewById(R.id.txtChatMessage);
        txtChatMesage.setText(messageChannel.getMessage());

        TextView txtDateTime = (TextView)itemView.findViewById(R.id.txtDateTime);
        txtDateTime.setText(messageChannel.getTimestamp());

        TextView txtChatUsername = (TextView)itemView.findViewById(R.id.txtChatUsername);
        String username = messageChannel.getFrom().equals("ug.barclays.com") ? "Barclays Support" : "Me";
        txtChatUsername.setText(username);

        return itemView;
    }
}
