package org.applab.digitizingdata.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.repo.VslaCycleRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: John Mark
 * Date: 4/22/14
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class GettingStartedWizardMembersArrayAdapter extends MembersArrayAdapter {
    public GettingStartedWizardMembersArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, values,R.layout.row_members_getting_started_wizard_list);
        this.context = context;
        this.values = values;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_members_getting_started_wizard_list, parent, false);

            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtMListFullNames);
            final TextView txtSavings = (TextView)rowView.findViewById(R.id.txtMListTotalSavings);
            final TextView txtLoans = (TextView)rowView.findViewById(R.id.txtMListTotalLoans);

            //Assign Values to the Widgets
            Member memb = values.get(position);
            NumberFormat numberFormat = NumberFormat.getInstance();

            VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(context);
            VslaCycle mostRecentCycle = vslaCycleRepo.getMostRecentCycle();

            double numberOfStars = Math.floor(memb.getSavingsOnSetup() / mostRecentCycle.getSharePrice());
            if(memb != null) {
                txtFullNames.setText(memb.toString());
                txtSavings.setText(String.format("Savings %s UGX - %.0f Star", numberFormat.format(memb.getSavingsOnSetup()) , numberOfStars) + (numberOfStars>=2 ? "s" : ""));
                txtLoans.setText(String.format("Outstanding Loan %s UGX",numberFormat.format(memb.getOutstandingLoanOnSetup())));
            }
            else {
                txtFullNames.setText("");
                txtSavings.setText("");
                txtLoans.setText("");
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
