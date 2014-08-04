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
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/30/13.
 */
public class LoansIssuedHistoryArrayAdapter extends ArrayAdapter<MemberLoanIssueRecord> {
    Context context;
    ArrayList<MemberLoanIssueRecord> values;
    String loanRepaymentProgressComment = "";
    int position;
    Typeface typefaceRegular;
    Typeface typefaceBold;
    String amount = "";
    String comment = "";
    String dateCleared = "";
    MeetingLoanRepaymentRepo loanRepaymentRepo;
    VslaCycleRepo vslaCycleRepo = null;
    VslaCycle cycle = null;
    VslaCycle cycle_j = null;

    public LoansIssuedHistoryArrayAdapter(Context context, ArrayList<MemberLoanIssueRecord> values, String font) {
        super(context, R.layout.row_loans_issued_history, values);
        this.context = context;
        this.values = values;
        this.typefaceRegular = Typeface.createFromAsset(context.getAssets(), font);
        this.typefaceBold = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-bold.ttf");

        loanRepaymentRepo = new MeetingLoanRepaymentRepo(getContext());
        vslaCycleRepo = new VslaCycleRepo(getContext());
        cycle = new VslaCycle();
        cycle_j = new VslaCycle();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_loans_issued_history, parent, false);


            //Get the Widgets
            //TextView txtPastLoanSummary = (TextView) rowView.findViewById(R.id.txtRLIHPastLoanSummary);

            TextView txtCycleSpan = (TextView) rowView.findViewById(R.id.txtRLIHCycleSpan);
            TextView txtLoanNo = (TextView) rowView.findViewById(R.id.txtRLIHLoanNo);
            TextView txtAmount = (TextView) rowView.findViewById(R.id.txtRLIHAmount);
            TextView txtComment = (TextView) rowView.findViewById(R.id.txtRLIHComment);
            TextView txtDateTaken = (TextView) rowView.findViewById(R.id.txtRLIHDateTaken);

            // Set Typeface
            //txtPastLoanSummary.setTypeface(typeface);

            txtCycleSpan.setTypeface(typefaceBold);
            txtLoanNo.setTypeface(typefaceBold);
            txtAmount.setTypeface(typefaceRegular);
            txtComment.setTypeface(typefaceRegular);
            txtDateTaken.setTypeface(typefaceRegular);

            StringBuilder summary = new StringBuilder("");

            // Assign Values to the Widgets
            MemberLoanIssueRecord loanRecord = values.get(position);
            if (loanRecord != null) {
                Log.d("LIHAA", String.valueOf(cycle_j.getCycleId()));
                cycle = vslaCycleRepo.getCycleByDate(loanRecord.getMeetingDate());
                if (cycle != null) {
                    txtCycleSpan.setText(String.format("Cycle %s to %s", Utils.formatDate(cycle.getStartDate(), Utils.DATE_FIELD_FORMAT), Utils.formatDate(cycle.getEndDate(), Utils.DATE_FIELD_FORMAT)));
                }

                Log.d("LIHAA2", String.valueOf(cycle.getCycleId()));
                // Only show for first
                if (cycle_j.getCycleId() == cycle.getCycleId()){
                    txtCycleSpan.setVisibility(View.GONE);
                }
                cycle_j = cycle;
                txtDateTaken.setText(String.format("%s", Utils.formatDate(loanRecord.getMeetingDate(), Utils.DATE_FIELD_FORMAT)));
                txtLoanNo.setText(String.format("%d", loanRecord.getLoanNo()));
                txtAmount.setText(String.format("%,.0fUGX", loanRecord.getPrincipalAmount()));

                if (loanRecord.isCleared()) {
                    txtComment.setText(String.format("%s", "Cleared"));
                } else {
                    txtComment.setText(String.format("%s", "Not cleared"));
                }

            }
            cycle_j = cycle;
            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
