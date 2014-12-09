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
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/28/13.
 */
public class MembersFinesArrayAdapter extends ArrayAdapter<Member> {
    private final Context context;
    private final ArrayList<Member> values;
    private int meetingId;
    private Meeting targetMeeting = null;
    private MeetingFineRepo fineRepo = null;
    private MeetingRepo meetingRepo = null;
    private final Typeface typeface;


    public MembersFinesArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_fines, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");

        meetingRepo = new MeetingRepo(getContext());
        fineRepo = new MeetingFineRepo(getContext());

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

            rowView = inflater.inflate(R.layout.row_member_fines, parent, false);

            if (null == fineRepo) {
                fineRepo = new MeetingFineRepo(getContext());
            }
            if (null == meetingRepo) {
                meetingRepo = new MeetingRepo(getContext());
            }

            //Get the Widgets
            final TextView txtFullName = (TextView) rowView.findViewById(R.id.txtFineFullName);
            final TextView txtTotalFines = (TextView)rowView.findViewById(R.id.txtFineTotal);

            // Set typeface
            txtFullName.setTypeface(typeface);
            txtTotalFines.setTypeface(typeface);

            //Assign Values to the Widgets
            Member member = values.get(position);
            txtFullName.setText(member.toString());

            //Get the Total SavingSchema
            targetMeeting = meetingRepo.getMeetingById(meetingId);
            double outstandingFines = 0.0;
            if(null != targetMeeting && null != targetMeeting.getVslaCycle()) {
                outstandingFines  = fineRepo.getMemberTotalFinesOutstandingInCycle(targetMeeting.getVslaCycle().getCycleId(), member.getMemberId());
            }

            txtTotalFines.setText(String.format("Outstanding Fines: %,.0f UGX", outstandingFines));
            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}