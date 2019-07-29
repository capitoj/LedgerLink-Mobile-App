package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.R;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingOutstandingWelfareRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;
import org.applab.ledgerlink.repo.MeetingWelfareRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class MembersArrayAdapter extends ArrayAdapter<Member> {
    Context context;
    ArrayList<Member> values;
    int position;
    Typeface typeface;

    public MembersArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_members_main_list, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_members_main_list, parent, false);

            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtMListFullName);
            final TextView txtMListOccupation = (TextView)rowView.findViewById(R.id.txtMListOccupation);
            final TextView txtStatus = (TextView)rowView.findViewById(R.id.txtMListStatus);
            final TextView txtSavings = (TextView)rowView.findViewById(R.id.txtMListTotalSavings);
            final TextView txtWelfare = (TextView)rowView.findViewById(R.id.txtMListTotalWelfare);
            final TextView txtOutstandingWelfare = (TextView)rowView.findViewById(R.id.txtMListTotalOutstandingWelfare);
            final TextView txtLoansOutstanding = (TextView)rowView.findViewById(R.id.txtMListTotalOutstandingLoans);

            // Set Typeface
            txtFullNames.setTypeface(typeface);
            txtMListOccupation.setTypeface(typeface);
            txtStatus.setTypeface(typeface);
            txtSavings.setTypeface(typeface);
            txtWelfare.setTypeface(typeface);
            txtOutstandingWelfare.setTypeface(typeface);
            txtLoansOutstanding.setTypeface(typeface);

            //Assign Values to the Widgets
            Member memb = values.get(position);
            if(memb != null) {
                txtFullNames.setText(memb.toString());

                String phoneNo = memb.getPhoneNumber();
                if(null == phoneNo || phoneNo.trim().length() <= 0) {
                    phoneNo = "No Phone";
                }

                String occupation = memb.getOccupation();
                if(null == occupation || occupation.trim().length() <= 0) {
                    occupation = "No Occupation";
                }else{
                    txtMListOccupation.setText(occupation);
                }

                String status = memb.isActive() ? "Active" : "Inactive";
                txtStatus.setText(status);

                //txtSavings.setText(phoneNo);
                if(new VslaCycleRepo(context).getCurrentCycle() != null){
                    int targetCycleId = new VslaCycleRepo(context).getCurrentCycle().getCycleId();
                    double totalSavings = new MeetingSavingRepo(context).getMemberTotalSavingsInCycle(targetCycleId, memb.getMemberId());
                    txtSavings.setText("Savings : " + Utils.formatNumber(totalSavings) + " UGX");

                    double totalWelfare = new MeetingWelfareRepo(context).getMemberTotalWelfareInCycle(targetCycleId, memb.getMemberId());
                    txtWelfare.setText("Welfare : " + Utils.formatNumber(totalWelfare) + " UGX");

                    double outstandingWelfare = new MeetingOutstandingWelfareRepo(context).getMemberTotalWelfareOutstandingInCycle(targetCycleId, memb.getMemberId());
                    txtOutstandingWelfare.setText("Outstanding Welfare : " + Utils.formatNumber(outstandingWelfare) + " UGX");

                    double outstandingLoans = new MeetingLoanIssuedRepo(context).getTotalLoansIssuedToMemberInCycle(targetCycleId, memb.getMemberId());
                    txtLoansOutstanding.setText("Outstanding Loans : " + Utils.formatNumber(outstandingLoans) + " UGX");
                }
            }
            else {
                txtFullNames.setText("");
                txtMListOccupation.setText("");
                //txtLoans.setText("");
            }

            return rowView;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
