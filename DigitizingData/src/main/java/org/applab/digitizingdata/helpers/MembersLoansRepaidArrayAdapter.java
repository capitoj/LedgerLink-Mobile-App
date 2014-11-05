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
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/9/13.
 */
public class MembersLoansRepaidArrayAdapter extends ArrayAdapter<Member> {
    Context context;
    ArrayList<Member> values;
    int meetingId;
    Meeting targetMeeting = null;
    MeetingLoanRepaymentRepo loansRepaidRepo = null;
    MeetingLoanIssuedRepo loansIssuedRepo = null;
    MeetingRepo meetingRepo = null;
    Typeface typeface;

    public MembersLoansRepaidArrayAdapter(Context context, ArrayList<Member> values, String font) {
        super(context, R.layout.row_member_loans_repaid, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), font);

        meetingRepo = new MeetingRepo(getContext());
        loansRepaidRepo = new MeetingLoanRepaymentRepo(getContext());
        loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());

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

            View rowView = inflater.inflate(R.layout.row_member_loans_repaid, parent, false);

            if (null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }

            if (null == loansRepaidRepo) {
                loansRepaidRepo = new MeetingLoanRepaymentRepo(getContext());
            }

            if (null == loansIssuedRepo) {
                loansIssuedRepo = new MeetingLoanIssuedRepo(getContext());
            }

            //Get the Widgets
            final TextView txtFullName = (TextView) rowView.findViewById(R.id.txtRMLRepayFullName);
            final TextView txtBalance = (TextView) rowView.findViewById(R.id.txtRMLRepayBalance);
            /**final TextView txtRepaidToday = (TextView) rowView.findViewById(R.id.txtRMLRepayTodaysRepay); */
            final TextView txtDateDue = (TextView) rowView.findViewById(R.id.txtRMLRepayDateDue);

            // Set Typeface
            txtFullName.setTypeface(typeface);
            txtBalance.setTypeface(typeface);
            /**txtRepaidToday.setTypeface(typeface); */
            txtDateDue.setTypeface(typeface);

            //Assign Values to the Widgets
            Member selectedMember = values.get(position);
            txtFullName.setText(selectedMember.toString());

            //Get the Total
            targetMeeting = meetingRepo.getMeetingById(meetingId);

            //Get the Repayments in selected Meeting
            double repaymentInMeeting = 0.0;
            if (null != targetMeeting) {
                repaymentInMeeting = loansRepaidRepo.getTotalRepaymentByMemberInMeeting(targetMeeting.getMeetingId(), selectedMember.getMemberId());
            }
            // txtRepaidToday.setText(String.format("Paid Today: %,.0f UGX", repaymentInMeeting));

            //Get the Outstanding Loans in Cycle
            //TODO: In case multiple loans will be allowed, then we shall need to pass the LoanId or LoanNo
            // double outstandingLoan = 0.0;
            MeetingLoanIssued recentLoan = null;
            if (null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                //  outstandingLoan = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), selectedMember.getMemberId());
                recentLoan = loansIssuedRepo.getTotalOutstandingLoansByMemberInCycle(targetMeeting.getVslaCycle().getCycleId(), selectedMember.getMemberId());
            }

            // if (outstandingLoan == 0.0) {

            if ((recentLoan == null || recentLoan.getLoanBalance() == 0.0) && (recentLoan.getDateCleared().compareTo(targetMeeting.getMeetingDate()) < 0)) {
                txtBalance.setText("No outstanding loans");
                txtDateDue.setVisibility(View.GONE);
            } else {
                //txtBalance.setText(String.format("Outstanding loan %,.0f UGX", outstandingLoan));
                txtBalance.setText(String.format("Outstanding loan %,.0f UGX", recentLoan.getLoanBalance()));
                if (recentLoan == null || recentLoan.getLoanBalance() == 0.0) {
                    txtDateDue.setText("");
                } else {
                    txtDateDue.setText(String.format("Date Due %s", Utils.formatDate(recentLoan.getDateDue(), Utils.OTHER_DATE_FIELD_FORMAT)));
                }
            }

            //}
            //Date Due
            /** txtDateDue.setText("Date Due: -");
             if (null != targetMeeting) {
             MeetingLoanIssued recentLoan = loansIssuedRepo.getMostRecentLoanIssuedToMember(selectedMember.getMemberId());
             if (null != recentLoan) {
             txtDateDue.setText(String.format("Date Due: %s", Utils.formatDate(recentLoan.getDateDue())));
             }
             }*/

            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
