package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.repo.VslaCycleRepo;
import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.Member;

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
        super(context, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {

            // Populate the ListView Row with data.
            // Handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_members_getting_started_wizard_list, parent, false);

            // Get the Widgets
            final TextView txtFullName = (TextView)rowView.findViewById(R.id.txtMListFullName);
            final TextView txtSavings = (TextView)rowView.findViewById(R.id.txtMListTotalSavings);
            final TextView txtLoans = (TextView)rowView.findViewById(R.id.txtMListTotalLoans);

            // Set Typeface
            txtFullName.setTypeface(typeface);
            txtSavings.setTypeface(typeface);
            txtLoans.setTypeface(typeface);

            // Assign Values to the Widgets
            Member member = values.get(position);
            NumberFormat numberFormat = NumberFormat.getInstance();

            VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(context);
            VslaCycle mostRecentCycle = vslaCycleRepo.getMostRecentCycle();

            double numberOfStars = Math.floor(member.getSavingsOnSetup() / mostRecentCycle.getSharePrice());
            if(member != null) {
                txtFullName.setText(member.toString());
                txtSavings.setText(String.format(context.getResources().getString(R.string.savings_x)+" %s UGX - %.0f "+context.getResources().getString(R.string.star), numberFormat.format(member.getSavingsOnSetup()) , numberOfStars) + (numberOfStars>=2 ? "s" : ""));
                txtLoans.setText(String.format(context.getResources().getString(R.string.outstanding_loan)+" %s UGX",numberFormat.format(member.getOutstandingLoanOnSetup())));
            }
            else {
                txtFullName.setText("");
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
