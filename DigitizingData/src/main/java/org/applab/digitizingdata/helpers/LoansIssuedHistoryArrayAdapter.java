package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/30/13.
 */
public class LoansIssuedHistoryArrayAdapter extends ArrayAdapter<MemberLoanIssueRecord> {
    Context context;
    ArrayList<MemberLoanIssueRecord> values;
    String loanRepaymentProgressComment = "";
    int position;
    Typeface typeface;
    String amount = "";
    String comment = "";
    String dateCleared = "";
    MeetingLoanRepaymentRepo loanRepaymentRepo;

    public LoansIssuedHistoryArrayAdapter(Context context, ArrayList<MemberLoanIssueRecord> values, String font) {
        super(context, R.layout.row_loans_issued_history, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), font);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_loans_issued_history, parent, false);

            //Get the Widgets

            TextView txtPastLoanSummary = (TextView) rowView.findViewById(R.id.txtRLIHPastLoanSummary);

            // TextView txtAmount = (TextView)rowView.findViewById(R.id.txtRLIHAmount);
            // TextView txtComment = (TextView)rowView.findViewById(R.id.txtRLIHComment);
            // TextView txtDateCleared = (TextView)rowView.findViewById(R.id.txtRLIHDateCleared);

            // Set Typeface
            txtPastLoanSummary.setTypeface(typeface);

            // txtAmount.setTypeface(typeface);
            // txtComment.setTypeface(typeface);
            // txtDateCleared.setTypeface(typeface); */

            /**
             * TODO: Add code to get the loan repayment comment for now we'll use "GOOD"
             if (loanRepaymentRepo == null) {
             loanRepaymentRepo = new MeetingLoanRepaymentRepo(MemberLoansIssuedHistoryActivity.this);
             }
             loansIssued = loanIssuedRepo.getMemberMostRecentLoanRepayment(targetCycleId, memberId);
             */
            loanRepaymentProgressComment = "GOOD";

            StringBuilder summary = new StringBuilder("");

            //Assign Values to the Widgets
            MemberLoanIssueRecord loanRecord = values.get(position);
            if (loanRecord != null) {
                // txtMeetingDate.setText(String.format("Issued On: %s",Utils.formatDate(loanRecord.getMeetingDate(),Utils.DATE_FIELD_FORMAT)));
                // txtLoanNo.setText(String.format("Loan No: %d", loanRecord.getLoanNo()));
                // txtAmount.setText(String.format("%,.0fUGX  ", loanRecord.getPrincipalAmount()));
                if (loanRecord.isCleared()) {
                    loanRepaymentProgressComment = "Excellent";
                    // dateCleared = txtComment.setText(String.format("Loan paid"));
                    // txtDateCleared.setText(String.format("paid %s ", Utils.formatDate(loanRecord.getDateCleared(),Utils.DATE_FIELD_FORMAT)));
                    txtPastLoanSummary.setText(String.format("%,.0fUGX  paid %s  %s", loanRecord.getPrincipalAmount(), Utils.formatDate(loanRecord.getDateCleared(), Utils.DATE_FIELD_FORMAT), loanRepaymentProgressComment));
                } else {
                    // txtComment.setText(loanRepaymentProgressComment);
                    //TODO: Confirm whether we capture Due Date for Loans and use it here
                    // txtDateCleared.setText(String.format("due %s ", Utils.formatDate(loanRecord.getDateDue(),Utils.DATE_FIELD_FORMAT)));                }
                    txtPastLoanSummary.setText(String.format("%,.0fUGX  due %s  %s", loanRecord.getPrincipalAmount(), Utils.formatDate(loanRecord.getDateDue(), Utils.DATE_FIELD_FORMAT), loanRepaymentProgressComment));

                }
            }

            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
