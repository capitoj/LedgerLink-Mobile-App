package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.R;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.repo.MeetingAttendanceRepo;
import org.applab.ledgerlink.repo.VslaCycleRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/5/13.
 */
public class MembersRollCallArrayAdapter extends ArrayAdapter<Member> {
    private final Context context;
    private final ArrayList<Member> values;
    private MeetingAttendanceRepo attendanceRepo ;
    private final VslaCycle currentCycle;
    private int meetingId;
    private boolean isFromCode = false;
    public boolean viewOnly = false;
private final Typeface typeface;
    //Use Class variable to hold state as it will be lost on scrolling
    private final boolean[] mCheckedState;

    public MembersRollCallArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_roll_call, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");

        //set array size to number of records in the adapter
        this.mCheckedState = new boolean[values.size()];

        attendanceRepo = new MeetingAttendanceRepo(getContext());
        VslaCycleRepo cycleRepo = new VslaCycleRepo(getContext());
        currentCycle = cycleRepo.getCurrentCycle();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.row_member_roll_call, parent, false);

        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment


            //Get the Widgets
            //final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMRCFullName);
            //final CheckBox chkAttendance = (CheckBox)rowView.findViewById(R.id.chkRMRCAttendance);
            final CheckBox chkAttendance = (CheckBox)rowView.findViewById(R.id.chkRMRCallAttendance);
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMRCallFullName);
            final TextView txtAttendance = (TextView)rowView.findViewById(R.id.txtRMRCallAttendance);

            // Set typeface
            txtFullNames.setTypeface(typeface);
            txtAttendance.setTypeface(typeface);

            //Assign Values to the Widgets
            txtFullNames.setText(values.get(position).toString());

            //Get Attendance
            final int memberId = values.get(position).getMemberId();
            if(attendanceRepo == null) {
                attendanceRepo = new MeetingAttendanceRepo(getContext());
            }

            int attended = attendanceRepo.getMemberAttendanceCountInCycle(currentCycle.getCycleId(),memberId, 1);
            int missed = attendanceRepo.getMemberAttendanceCountInCycle(currentCycle.getCycleId(),memberId, 0);

            txtAttendance.setText(String.format("Attended %d. Missed %d",attended, missed));

            //Do not invoke the event when in Read only Mode
            if(Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY || viewOnly) {
                chkAttendance.setClickable(false);
            }

            //Mark that the change is happening from Code
            isFromCode = true;
            mCheckedState[position] = attendanceRepo.getMemberAttendance(meetingId,values.get(position).getMemberId());

            //Set the value based on what was stored in the member variable
            chkAttendance.setChecked(mCheckedState[position]);
            isFromCode = false;

            //Have the module-level variable set here to hold the value coz it gets lost upon scrolling
            chkAttendance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if(!isFromCode) {
                        //when the user checked the checkbox store this value in the module-level variable
                        mCheckedState[position] = b;
                        int isPresent = 0;

                        if(b){
                            isPresent = 1;
                        }
                        Member selectedMember = (Member) values.get(position);
                        if(null != selectedMember) {
                            MeetingAttendanceRepo attendanceRepo = new MeetingAttendanceRepo(getContext());
                            attendanceRepo.saveMemberAttendance(meetingId,selectedMember.getMemberId(), isPresent );
                        }

                        //Repopulate the listView Attendance
                        //TODO: This will have to be Optimized for Performance
                        int attended = attendanceRepo.getMemberAttendanceCountInCycle(currentCycle.getCycleId(),memberId, 1);
                        int missed = attendanceRepo.getMemberAttendanceCountInCycle(currentCycle.getCycleId(),memberId, 0);

                        txtAttendance.setText(String.format("Attended %d. Missed %d",attended, missed));
                    }
                }
            });

            return rowView;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }
}
