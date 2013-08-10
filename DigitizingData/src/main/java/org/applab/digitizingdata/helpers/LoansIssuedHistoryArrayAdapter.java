package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.applab.digitizingdata.R;

import java.util.ArrayList;

/**
 * Created by Moses on 7/30/13.
 */
public class LoansIssuedHistoryArrayAdapter extends ArrayAdapter<MemberLoanIssueRecord> {
    Context context;
    ArrayList<MemberLoanIssueRecord> values;
    int position;

    public LoansIssuedHistoryArrayAdapter(Context context, ArrayList<MemberLoanIssueRecord> values) {
        super(context, R.layout.row_loans_issued_history, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_loans_issued_history, parent, false);

            //Get the Widgets
            TextView txtMeetingDate = (TextView)rowView.findViewById(R.id.txtRLIHMeetingDate);
            TextView txtLoanNo = (TextView)rowView.findViewById(R.id.txtRLIHLoanNo);
            TextView txtAmount = (TextView)rowView.findViewById(R.id.txtRLIHAmount);
            TextView txtBalance = (TextView)rowView.findViewById(R.id.txtRLIHBalance);
            TextView txtDateCleared = (TextView)rowView.findViewById(R.id.txtRLIHDateCleared);

            //Assign Values to the Widgets
            MemberLoanIssueRecord loanRecord = values.get(position);
            if(loanRecord != null) {
                txtMeetingDate.setText(String.format("Issued On: %s",Utils.formatDate(loanRecord.getMeetingDate(),Utils.DATE_FIELD_FORMAT)));
                txtLoanNo.setText(String.format("Loan No: %d", loanRecord.getLoanNo()));
                txtAmount.setText(String.format("Amount: %,.0fUGX", loanRecord.getPrincipalAmount()));
                if(loanRecord.isCleared()) {
                    txtBalance.setText(String.format("Loan Cleared"));
                    txtDateCleared.setText(String.format("Cleared On: %s", Utils.formatDate(loanRecord.getDateCleared(),Utils.DATE_FIELD_FORMAT)));
                }
                else {
                    txtBalance.setText(String.format("Balance: %,.0fUGX", loanRecord.getBalance()));
                    //TODO: Confirm whether we capture Due Date for Loans and use it here
                    txtDateCleared.setText(String.format("Due On: "));                }

            }

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
