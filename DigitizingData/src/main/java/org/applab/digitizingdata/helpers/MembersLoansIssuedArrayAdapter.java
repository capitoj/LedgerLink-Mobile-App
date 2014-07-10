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

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/9/13.
 */
public class MembersLoansIssuedArrayAdapter extends ArrayAdapter<Member>  {
    Context context;
    ArrayList<Member> values;
    int meetingId;
    Meeting targetMeeting = null;
    MeetingLoanIssuedRepo loansIssuedRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingRepo meetingRepo = null;
    Typeface typeface;

    public MembersLoansIssuedArrayAdapter(Context context, ArrayList<Member> values, String font) {
        super(context, R.layout.row_member_loans_issued, values);
        this.context = context;
        this.values = values;

        loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
        meetingRepo = new MeetingRepo(getContext());
        savingRepo = new MeetingSavingRepo(getContext());

        this.typeface = Typeface.createFromAsset(context.getAssets(), font);
    }

    public void setMeetingId(int meetingId){
        this.meetingId = meetingId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_member_loans_issued, parent, false);

            if(null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }
            if(null == loansIssuedRepo) {
                loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
            }
            //Get the Widgets
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMLIssuedFullName);
            final TextView txtOutstanding = (TextView)rowView.findViewById(R.id.txtRMLIssuedOutstanding);
          //  final TextView txtComment = (TextView)rowView.findViewById(R.id.txtRMLComment);

            // final TextView txtLoanIssuedToday = (TextView)rowView.findViewById(R.id.txtRMLIssuedTodaysLoan);
           // final TextView txtTotalIssued = (TextView)rowView.findViewById(R.id.txtRMLIssuedTotals);
            // final TextView txtTotalSavings = (TextView)rowView.findViewById(R.id.txtRMLIssuedSavings);

            // Set Typeface
           // txtComment.setTypeface(typeface);
            txtFullNames.setTypeface(typeface);
            txtOutstanding.setTypeface(typeface);

            /**txtLoanIssuedToday.setTypeface(typeface);
            txtTotalIssued.setTypeface(typeface);
            txtTotalSavings.setTypeface(typeface); */

            //Assign Values to the Widgets
            Member member = values.get(position);
            txtFullNames.setText(member.toString());

            //Get the Total
            targetMeeting = meetingRepo.getMeetingById(meetingId);

            ArrayList<MeetingLoanIssued> loansIssued =  new ArrayList<MeetingLoanIssued>();

            String comment = " ";

           // TODO: Change this code when time allows; it's weak!

            StringBuilder aggregate = new StringBuilder();
            double outstandingLoansByMember = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                //outstandingLoansByMember = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
                //outstandingLoansByMember = loanIssued.getLoanBalance();
               loansIssued = loansIssuedRepo.getOutstandingLoansListByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            if (loansIssued == null || loansIssued.size()==0){
                txtOutstanding.setText("No outstanding loans");
            }
            else {
                for(MeetingLoanIssued loanIssue : loansIssued){
                    if(loanIssue.getComment()== null || loanIssue.getComment().trim().length()==0){
                        aggregate.append(String.format("Outstanding loan  %,.0f UGX", loanIssue.getLoanBalance()));
                    }
                    else{
                        comment = loanIssue.getComment();
                        aggregate.append(String.format("Outstanding loan  %,.0f UGX \n%s", loanIssue.getLoanBalance(), comment));
                    }

                }

               txtOutstanding.setText(aggregate.toString());
                txtOutstanding.setLineSpacing(0.0f, 1.5f);
               // txtOutstanding.setText(String.format("Outstanding loan  %,.0f UGX", outstandingLoansByMember));

            }


          /**  double comment = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                outstandingLoansByMember = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            txtOutstanding.setText(String.format("Outstanding Bal: %,.0fUGX", outstandingLoansByMember)); */


            double totalIssuedToMemberInMeeting = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalIssuedToMemberInMeeting = loansIssuedRepo.getTotalLoansIssuedToMemberInMeeting(targetMeeting.getMeetingId(), member.getMemberId());
            }
          //  txtLoanIssuedToday.setText(String.format("Today: %,.0fUGX", totalIssuedToMemberInMeeting));

            double totalLoansToMember = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalLoansToMember = loansIssuedRepo.getTotalLoansIssuedToMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
           // txtTotalIssued.setText(String.format("Total Loans: %,.0fUGX", totalLoansToMember));

            double totalSavingsByMember = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalSavingsByMember = savingRepo.getMemberTotalSavingsInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
          //  txtTotalSavings.setText(String.format("Total Savings: %,.0fUGX", totalSavingsByMember));

            return rowView;
        }
        catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
