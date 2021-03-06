package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.R;

import java.util.ArrayList;

/**
 * Created by Moses on 8/2/13.
 */
public class LoanRepaymentHistoryArrayAdapter extends ArrayAdapter<MemberLoanRepaymentRecord> {

    private final Context context;
    private final ArrayList<MemberLoanRepaymentRecord> values;
    int position;
    private final Typeface typeface;

    public LoanRepaymentHistoryArrayAdapter(Context context, ArrayList<MemberLoanRepaymentRecord> values) {
        super(context, R.layout.row_loan_repayment_history, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_loan_repayment_history, parent, false);

            //Get the Widgets
            // TextView txtPaymentListing = (TextView) rowView.findViewById(R.id.txtRLRHPaymentListing);

            TextView txtMeetingDate = (TextView) rowView.findViewById(R.id.txtRLRHMeetingDate);
            TextView txtLoanNo = (TextView) rowView.findViewById(R.id.txtRLRHLoanNo);
            TextView txtAmount = (TextView) rowView.findViewById(R.id.txtRLRHAmount);

            // TextView txtRollover = (TextView)rowView.findViewById(R.id.txtRLRHRollover);
            TextView txtComments = (TextView) rowView.findViewById(R.id.txtRLRHComments);

            // Set typeface
            //txtPaymentListing.setTypeface(typeface);
            txtMeetingDate.setTypeface(typeface);
            txtLoanNo.setTypeface(typeface);
            txtAmount.setTypeface(typeface);
            // txtRollover.setTypeface(typeface);
            txtComments.setTypeface(typeface);

            //Assign Values to the Widgets
            MemberLoanRepaymentRecord repaymentRecord = values.get(position);
            if (repaymentRecord != null) {
                //txtPaymentListing.setText(String.format("%d\t %,.0f UGX\t %s\t",repaymentRecord.getLoanNo(), repaymentRecord.getAmount(), Utils.formatDate(repaymentRecord.getMeetingDate(), Utils.DATE_FIELD_FORMAT)));
                if (repaymentRecord.getComments().isEmpty()) {
                    txtComments.setVisibility(View.GONE);
                }
                txtComments.setText(String.format("%s", repaymentRecord.getComments()));
                txtMeetingDate.setText(String.format("%s", Utils.formatDate(repaymentRecord.getMeetingDate(), Utils.DATE_FIELD_FORMAT)));
                txtLoanNo.setText(String.format("%d", repaymentRecord.getLoanNo()));
                txtAmount.setText(String.format("%,.0f UGX", repaymentRecord.getAmount()));
                //txtRollover.setText(String.format("Rollover: %,.0f UGX", repaymentRecord.getRolloverAmount()));
            }

            return rowView;
        } catch (
                Exception ex
                )

        {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}


