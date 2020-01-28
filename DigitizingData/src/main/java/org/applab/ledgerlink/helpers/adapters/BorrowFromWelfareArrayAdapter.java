package org.applab.ledgerlink.helpers.adapters;

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
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingOutstandingWelfareRepo;
import org.applab.ledgerlink.repo.MeetingRepo;

import java.util.ArrayList;

/**
 * Created by JCapito on 11/12/2018.
 */

public class BorrowFromWelfareArrayAdapter extends ArrayAdapter<Member> {

    private Meeting targetMeeting = null;
    private final Context context;
    private final ArrayList<Member> values;
    private int meetingId;
    private final Typeface typeface;
    private MeetingRepo meetingRepo;
    private MeetingOutstandingWelfareRepo meetingOutstandingWelfareRepo;

    public BorrowFromWelfareArrayAdapter(Context context, ArrayList<Member> values){
        super(context, R.layout.row_member_borrow_from_welfare, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");

        meetingRepo = new MeetingRepo(getContext());
        meetingOutstandingWelfareRepo = new MeetingOutstandingWelfareRepo(this.context);
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_member_loans_repaid, parent, false);
            if (null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }

            //Get the Widgets
            final TextView txtFullName = (TextView) rowView.findViewById(R.id.txtRMLRepayFullName);
            final TextView txtTotalFines = (TextView) rowView.findViewById(R.id.txtRMLRepayBalance);
            final TextView txtDueDate = (TextView) rowView.findViewById(R.id.txtRMLRepayDateDue);

            // Set typeface
            txtFullName.setTypeface(typeface);
            txtTotalFines.setTypeface(typeface);
            txtDueDate.setTypeface(typeface);

            //Assign Values to the Widgets
            Member member = values.get(position);
            txtFullName.setText(member.toString());

            //Get the Total SavingSchema
            targetMeeting = meetingRepo.getMeetingById(meetingId);
            double outstandingWelfare = 0.0;
            if (null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                MeetingOutstandingWelfare meetingOutstandingWelfare = meetingOutstandingWelfareRepo.getOutstandingMemberWelfare(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
                if(meetingOutstandingWelfare.getOutstandingWelfareId() > 1){
                    outstandingWelfare = meetingOutstandingWelfare.getAmount();
                    txtDueDate.setText(String.format(context.getResources().getString(R.string.date_due)+" %s", Utils.formatDate(meetingOutstandingWelfare.getExpectedDate(), Utils.OTHER_DATE_FIELD_FORMAT)));
                }else{
                    txtDueDate.setText("Due Date: None");
                }
            }

            txtTotalFines.setText(String.format(context.getResources().getString(R.string.outstanding_welfare_x)+" %,.0f UGX", outstandingWelfare));
            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
