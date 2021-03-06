package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.repo.MeetingRepo;
import org.applab.ledgerlink.repo.MeetingWelfareRepo;

import java.util.ArrayList;

/**
 * Created by JCapito on 9/25/2018.
 */

public class MembersWelfareArrayAdapter extends ArrayAdapter<Member> {
    private final Context context;
    private final ArrayList<Member> values;
    private int meetingId;
    private Meeting targetMeeting = null;
    private MeetingWelfareRepo meetingWelfareRepo = null;
    private MeetingRepo meetingRepo = null;
    private final Typeface typeface;

    public MembersWelfareArrayAdapter(Context context, ArrayList<Member> values){
        super(context, R.layout.row_member_welfare, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
        this.meetingRepo = new MeetingRepo(context);
        this.meetingWelfareRepo = new MeetingWelfareRepo(context);
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

            rowView = inflater.inflate(R.layout.row_member_welfare, parent, false);

            if (null == this.meetingWelfareRepo) {
                this.meetingWelfareRepo = new MeetingWelfareRepo(getContext());
            }
            if (null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }

            //Get the Widgets
            final TextView txtFullName = (TextView) rowView.findViewById(R.id.txtRMWelfareFullName);
            final TextView txtTodaysWelfare = (TextView)rowView.findViewById(R.id.txtRMSavTodaysWelfare);
            final TextView txtTotalWelfare = (TextView)rowView.findViewById(R.id.txtRMWelfareTotals);

            // Set typeface
            txtFullName.setTypeface(typeface);
            txtTodaysWelfare.setTypeface(typeface);
            txtTotalWelfare.setTypeface(typeface);

            //Assign Values to the Widgets
            Member member = values.get(position);
            txtFullName.setText(member.toString());

            //Get the Total welfare
            targetMeeting = meetingRepo.getMeetingById(meetingId);
            double totalWelfare = 0.0;
            double todaysWelfare = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                todaysWelfare = this.meetingWelfareRepo.getMemberWelfare(this.meetingId, member.getMemberId());
                totalWelfare = this.meetingWelfareRepo.getMemberTotalWelfareInCycle(this.targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
                Log.e("MemberWelfareInCycle", String.valueOf(totalWelfare));
            }
            txtTodaysWelfare.setText(String.format(context.getResources().getString(R.string.todays_welfare)+" %,.0f UGX", todaysWelfare));
            txtTotalWelfare.setText(String.format(context.getResources().getString(R.string.total_welfare_x)+" %,.0f UGX", totalWelfare));
            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}
