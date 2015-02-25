package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingLoanIssued;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.repo.MeetingLoanIssuedRepo;
import org.applab.ledgerlink.repo.MeetingLoanRepaymentRepo;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/9/13.
 */
public class MembersLoansRepaidArrayAdapter extends ArrayAdapter<Member> {
    private final Context context;
    private final ArrayList<Member> values;
    private int meetingId;
    private MeetingLoanRepaymentRepo loansRepaidRepo = null;
    private MeetingLoanIssuedRepo loansIssuedRepo = null;
    private MeetingRepo meetingRepo = null;
    private final Typeface typeface;

    public MembersLoansRepaidArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_loans_repaid, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");

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
            Meeting targetMeeting = meetingRepo.getMeetingById(meetingId);


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
            // MemberLoanRepaid
            MemberLoanRepaymentRecord memberLoan;
            if (recentLoan == null) {
                txtBalance.setText("No outstanding loans");
                txtDateDue.setVisibility(View.GONE);
            } else {

                if ((recentLoan.getLoanBalance() == 0.0) && ((recentLoan.getDateCleared() != null ? recentLoan.getDateCleared().compareTo(targetMeeting.getMeetingDate()) : 0) < 0)) {
                    txtBalance.setText("No outstanding loans");
                    txtDateDue.setVisibility(View.GONE);
                } else {
                    // Get Member Loan Repayment Details
                    //txtBalance.setText(String.format("Outstanding loan %,.0f UGX", outstandingLoan));
                    txtBalance.setText(String.format("Outstanding loan %,.0f UGX", recentLoan.getLoanBalance()));

                    if (recentLoan.getLoanBalance() == 0.0) {
                        txtDateDue.setText("");
                    } else {
                        memberLoan = loansRepaidRepo.getMemberLoanRepaymentRecord(selectedMember.getMemberId());
                        if (memberLoan != null) {
                            txtDateDue.setText(String.format("Date Due %s", Utils.formatDate(memberLoan.getNextDateDue(), Utils.OTHER_DATE_FIELD_FORMAT)));
                        } else {
                            txtDateDue.setText(String.format("Date Due %s", Utils.formatDate(recentLoan.getDateDue(), Utils.OTHER_DATE_FIELD_FORMAT)));
                        }
                    }
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
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }
}
