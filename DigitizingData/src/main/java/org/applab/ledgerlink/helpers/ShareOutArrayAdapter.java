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
import org.applab.ledgerlink.repo.MeetingFineRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;
import org.applab.ledgerlink.repo.MeetingWelfareRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 6/25/13.
 */
public class ShareOutArrayAdapter extends ArrayAdapter<Member> {
    Context context;
    ArrayList<Member> values;
    int position;
    Typeface typeface;

    public ShareOutArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_shareout_main_list, values);
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

            View rowView = inflater.inflate(R.layout.row_shareout_main_list, parent, false);

            //Get the Widgets
            final TextView txtFullNames = rowView.findViewById(R.id.txtMListFullName);
            final TextView txtWelfare = rowView.findViewById(R.id.txtMListTotalWelfare);
            final TextView txtShareOut = rowView.findViewById(R.id.txtMListShareOut);
            final TextView txtTotalSavings = rowView.findViewById(R.id.txtMListTotalSaving);
            final TextView txtTotalFine = rowView.findViewById(R.id.txtMListTotalFine);
            final TextView txtTotalRepaid = rowView.findViewById(R.id.txtMListTotalRepaid);
            final TextView txtTotalEarnings = rowView.findViewById(R.id.txtMListTotalEarnings);
            final TextView txtShareValue = rowView.findViewById(R.id.txtMListShareValue);
            final TextView txtCycleNoOfStar = rowView.findViewById(R.id.txtMListCycleNoOfStar);
            final TextView txtTotalMembersSavings = rowView.findViewById(R.id.txtMListTotalMembersSavings);
            final TextView txtMembersNoOfStars = rowView.findViewById(R.id.txtMListMembersNoOfStars);
            final TextView txtNewShareValue = rowView.findViewById(R.id.txtMListNewShareValue);

            // Set Typeface
            txtFullNames.setTypeface(typeface);
            txtWelfare.setTypeface(typeface);
            txtShareOut.setTypeface(typeface);

            //Assign Values to the Widgets
            Member memb = values.get(position);
            if(memb != null) {
                txtFullNames.setText(memb.toString());

                if(new VslaCycleRepo(context).getCurrentCycle() != null){
                    int targetCycleId = new VslaCycleRepo(context).getCurrentCycle().getCycleId();

                    double totalWelfare = new MeetingWelfareRepo(context).getMemberTotalWelfareInCycle(targetCycleId, memb.getMemberId());
                    txtWelfare.setText("Welfare : " + Utils.formatNumber(totalWelfare) + " UGX");

                    // total savings in cycle
                    double totalSavings = new MeetingSavingRepo(context).getTotalSavingsInCycle(targetCycleId);
                    txtTotalSavings.setText("Total Savings : " + Utils.formatNumber(totalSavings) + " UGX");
                    //total fines in cycle
                    double totalFine = new MeetingFineRepo(context).getTotalFinesPaidInCycle(targetCycleId);
                    txtTotalFine.setText("Total Fine : " + Utils.formatNumber(totalFine) + " UGX");
                    // total repaid in cycle
                    //double totalTotalRepaid = new MeetingLoanRepaymentRepo(context).getTotalLoansRepaidInCycle(targetCycleId);
                    //txtTotalRepaid.setText("Total Repaid : " + Utils.formatNumber(totalTotalRepaid) + " UGX");
                    // total interest collected in cycle
                    long totalTotalRepaid = (long) new MeetingLoanRepaymentRepo(context).getTotalInterestCollectedInCycle(targetCycleId);
                    txtTotalRepaid.setText("Total Interest Collected : " + Utils.formatNumber(totalTotalRepaid) + " UGX");
                    //total earnings
                    double totalEarnings = totalSavings + totalFine + totalTotalRepaid;
                    txtTotalEarnings.setText("Total Earnings : " + Utils.formatNumber(totalEarnings) + " UGX");
                    // Share value
                    double shareValue = new VslaCycleRepo(context).getCycle(targetCycleId).getSharePrice();
                    txtShareValue.setText("Share Value : " + Utils.formatNumber(shareValue) + " UGX");
                    //Cycle's No. of stars
                    double cycleNoOfStars = totalSavings / shareValue;
                    txtCycleNoOfStar.setText("Cycle No of stars : " + Utils.formatNumber(cycleNoOfStars) + " STARS");
                    // member's savings
                    double totalMembersSavings = new MeetingSavingRepo(context).getMemberTotalSavingsInCycle(targetCycleId, memb.getMemberId());
                    txtTotalMembersSavings.setText("Member's Savings : " + Utils.formatNumber(totalMembersSavings) + " UGX");
                    // member's no. of starsT
                    double membersNoOfStars = totalMembersSavings / shareValue;
                    txtMembersNoOfStars.setText("Member's No  of Stars : " + Utils.formatNumber(membersNoOfStars) + " STARS");
                    // New share value
                    double newShareValue = totalEarnings / cycleNoOfStars;
                    txtNewShareValue.setText("new Share Value : " + Utils.formatNumber(newShareValue) + " UGX");
                    // share out amount
                    double shareOutAmount = membersNoOfStars * newShareValue;
                    txtShareOut.setText("Share Out : " + Utils.formatNumber(shareOutAmount) + " UGX");
                }
            }
            else {
                txtFullNames.setText("");
                txtWelfare.setText("");
                txtShareOut.setText("");
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
