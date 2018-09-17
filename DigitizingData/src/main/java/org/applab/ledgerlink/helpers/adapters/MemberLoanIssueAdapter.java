package org.applab.ledgerlink.helpers.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;

import java.util.List;

/**
 * Created by JCapito on 3/30/2016.
 */
public class MemberLoanIssueAdapter extends ArrayAdapter<MeetingLoanIssued> {

    protected Context context;
    protected List<MeetingLoanIssued> outstandingLoansList;

    public MemberLoanIssueAdapter(Context context, List<MeetingLoanIssued> outstandingLoansList){
        super(context, R.layout.row_member_loans_issued_2, outstandingLoansList);
        this.context = context;
        this.outstandingLoansList = outstandingLoansList;
    }

    public View getView(int position, View view, ViewGroup viewGroup){
        View itemView = view;
        if(itemView == null){
            itemView = ((Activity)context).getLayoutInflater().inflate(R.layout.row_member_loans_issued_2, viewGroup, false);
        }
        MeetingLoanIssued meetingLoanIssued = this.outstandingLoansList.get(position);

        TextView txtRMLNumber = (TextView)itemView.findViewById(R.id.txtRMLNumber);
        txtRMLNumber.setText(String.valueOf(meetingLoanIssued.getLoanNo()));

        TextView txtRMLIssuedOutstanding = (TextView)itemView.findViewById(R.id.txtRMLIssuedOutstanding);
        txtRMLIssuedOutstanding.setText(String.format("%,.0f", meetingLoanIssued.getLoanBalance()));

        return  itemView;
    }
}
