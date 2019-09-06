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
    public static double totalSavings;
    public static double totalInterest;
    public static double totalFine;
    public static double totalEarnings;
    public static double newShareValue;
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
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_shareout_main_list, parent, false);

            //Get the Widgets
            final TextView txtFullNames = rowView.findViewById(R.id.txtMListFullName);
            final TextView txtWelfare = rowView.findViewById(R.id.txtMListTotalWelfare);
            final TextView txtShareOut = rowView.findViewById(R.id.txtMListShareOut);
            //final TextView txtTotalSavings = rowView.findViewById(R.id.txtMListTotalSaving);
            //final TextView txtTotalFine = rowView.findViewById(R.id.txtMListTotalFine);
            //final TextView txtTotalRepaid = rowView.findViewById(R.id.txtMListTotalRepaid);
            //final TextView txtTotalEarnings = rowView.findViewById(R.id.txtMListTotalEarnings);
            //final TextView txtShareValue = rowView.findViewById(R.id.txtMListShareValue);
            //final TextView txtCycleNoOfStar = rowView.findViewById(R.id.txtMListCycleNoOfStar);
            //final TextView txtTotalMembersSavings = rowView.findViewById(R.id.txtMListTotalMembersSavings);
            final TextView txtMembersNoOfStars = rowView.findViewById(R.id.txtMListMembersNoOfStars);
            //final TextView txtNewShareValue = rowView.findViewById(R.id.txtMListNewShareValue);

            // Set Typeface
            txtFullNames.setTypeface(typeface);
            txtWelfare.setTypeface(typeface);
            txtShareOut.setTypeface(typeface);

            //Assign Values to the Widgets
            Member memb = values.get(position);
            if(memb != null && memb.isActive()) {
                txtFullNames.setText(memb.toString());

                if(new VslaCycleRepo(context).getCurrentCycle() != null){
                    int targetCycleId = new VslaCycleRepo(context).getCurrentCycle().getCycleId();
                    // total  members welfare
                    double totalWelfare = new MeetingWelfareRepo(context).getMemberTotalWelfareInCycle(targetCycleId, memb.getMemberId());
                    txtWelfare.setText(context.getResources().getString(R.string.welfare_asof) + Utils.formatNumber(totalWelfare) + " UGX");
                    // total savings in cycle
                    totalSavings = new MeetingSavingRepo(context).getTotalSavingsInCycle(targetCycleId);
                    //total fines in cycle
                    totalFine = new MeetingFineRepo(context).getTotalFinesPaidInCycle(targetCycleId);
                    // total interest collected in cycle
                    totalInterest = new MeetingLoanRepaymentRepo(context).getTotalInterestCollectedInCycle(targetCycleId);
                    //total earnings
                    totalEarnings = totalSavings + totalFine + totalInterest;
                    // Share value
                    double shareValue = new VslaCycleRepo(context).getCycle(targetCycleId).getSharePrice();
                    //Cycle's No. of stars
                    double cycleNoOfStars = totalSavings / shareValue;
                    // member's savings
                    double totalMembersSavings = new MeetingSavingRepo(context).getMemberTotalSavingsInCycle(targetCycleId, memb.getMemberId());
                    // member's no. of stars
                    int membersNoOfStars = (int) (totalMembersSavings / shareValue);
                    txtMembersNoOfStars.setText(context.getResources().getString(R.string.stars_saved) + Utils.formatNumber(membersNoOfStars) + context.getResources().getString(R.string.stars));
                    // New share value
                    newShareValue = totalEarnings / cycleNoOfStars;
                    // share out amount
                    double shareOutAmount = membersNoOfStars * newShareValue;
                    txtShareOut.setText(context.getResources().getString(R.string.share_out_x) + Utils.formatNumber(shareOutAmount) + " UGX");
                }
            }
            else {
                txtFullNames.setText("");
                txtWelfare.setText("");
                txtShareOut.setText("");
                txtMembersNoOfStars.setText("");
            }

            return rowView;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }


    }

    public static double getTotalSaving() {

        return totalSavings;
    }

    public static double getTotalInterest() {

        return totalInterest;
    }

    public static double getTotalFine() {

        return totalFine;
    }

    public static double getTotalEarnings() {

        return totalEarnings;
    }

    public static double getNewShareValue() {
        return newShareValue;
    }
}
