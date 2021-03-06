package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.R;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingSavingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/9/13.
 */
public class MembersLoansIssuedArrayAdapter extends ArrayAdapter<Member> {
    private final Context context;
    private final ArrayList<Member> values;
    private int meetingId;
    private Meeting targetMeeting = null;
    private MeetingLoanIssuedRepo loansIssuedRepo = null;
    private MeetingSavingRepo savingRepo = null;
    private MeetingRepo meetingRepo = null;
    private final Typeface typeface;

    public MembersLoansIssuedArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_loans_issued, values);
        this.context = context;
        this.values = values;

        loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
        meetingRepo = new MeetingRepo(getContext());
        savingRepo = new MeetingSavingRepo(getContext());

        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_member_loans_issued, parent, false);

            if (null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }
            if (null == loansIssuedRepo) {
                loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
            }
            //Get the Widgets
            final TextView txtFullNames = (TextView) rowView.findViewById(R.id.txtRMLIssuedFullName);
            final TextView txtOutstanding = (TextView) rowView.findViewById(R.id.txtRMLIssuedOutstanding);
            final TextView txtComment = (TextView) rowView.findViewById(R.id.txtRMLComment);

            // final TextView txtLoanIssuedToday = (TextView)rowView.findViewById(R.id.txtRMLIssuedTodaysLoan);
            // final TextView txtTotalIssued = (TextView)rowView.findViewById(R.id.txtRMLIssuedTotals);
            // final TextView txtTotalSavings = (TextView)rowView.findViewById(R.id.txtRMLIssuedSavings);

            // Set Typeface
            txtComment.setTypeface(typeface);
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

            ArrayList<MeetingLoanIssued> loansIssued = new ArrayList<MeetingLoanIssued>();

            String comment = "";

            StringBuilder aggregate = new StringBuilder();
            double outstandingLoansByMember = 0.0;
            if (null != targetMeeting && null != targetMeeting.getVslaCycle()) {
               // outstandingLoansByMember = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
                //outstandingLoansByMember = loanIssued.getLoanBalance();
                //loansIssued = loansIssuedRepo.getOutstandingLoansListByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
                loansIssued = loansIssuedRepo.getOutstandingMemberLoans(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            if (loansIssued == null || loansIssued.size() == 0) {
                txtOutstanding.setText(context.getResources().getString(R.string.no_outstanding_loans));

            } else {
                double loanBalance = 0.0;
                for(MeetingLoanIssued loanIssue : loansIssued){
                    loanBalance += loanIssue.getLoanBalance();
                }
                if(loanBalance == 0.0){
                    txtOutstanding.setText(context.getResources().getString(R.string.no_outstanding_loans));
                }
                if(loanBalance > 0.0){
                    txtOutstanding.setText(String.format(context.getResources().getString(R.string.outstanding_loan)+"  %,.0f UGX", loanBalance));
                }

                /*
                for (MeetingLoanIssued loanIssue : loansIssued) {
                    if (loanIssue.getLoanBalance() == 0.0) {
                        txtOutstanding.setText("No outstanding loans");
                        continue;
                    }

                    if (loanIssue.getComment() == null || "".equals(loanIssue.getComment().trim())) {
                        txtOutstanding.setText(String.format("Outstanding loan  %,.0f UGX", loanIssue.getLoanBalance()));
                        txtComment.setVisibility(View.GONE);
                    } else {

                        comment = loanIssue.getComment();
                        txtOutstanding.setText(String.format("Outstanding loan  %,.0f UGX", loanIssue.getLoanBalance()));
                        txtComment.setText(comment);
                    }


                    txtOutstanding.setLineSpacing(0.0f, 1.5f);

                }*/

            }


            /**  double comment = 0.0;
             if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
             outstandingLoansByMember = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
             }
             txtOutstanding.setText(String.format("Outstanding Bal: %,.0fUGX", outstandingLoansByMember)); */


            double totalIssuedToMemberInMeeting = 0.0;
            if (null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalIssuedToMemberInMeeting = loansIssuedRepo.getTotalLoansIssuedToMemberInMeeting(targetMeeting.getMeetingId(), member.getMemberId());
            }
            //  txtLoanIssuedToday.setText(String.format("Today: %,.0fUGX", totalIssuedToMemberInMeeting));

            double totalLoansToMember = 0.0;
            if (null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalLoansToMember = loansIssuedRepo.getTotalLoansIssuedToMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            // txtTotalIssued.setText(String.format("Total Loans: %,.0fUGX", totalLoansToMember));

            double totalSavingsByMember = 0.0;
            if (null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                totalSavingsByMember = savingRepo.getMemberTotalSavingsInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }
            //  txtTotalSavings.setText(String.format("Total Savings: %,.0fUGX", totalSavingsByMember));

            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
