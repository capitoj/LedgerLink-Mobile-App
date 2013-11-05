package org.applab.digitizingdata.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.applab.digitizingdata.MemberDetailsViewActivity;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.util.ArrayList;

/**
 * Created by Moses on 7/5/13.
 */
public class MembersRollCallArrayAdapter extends ArrayAdapter<Member> {
    Context context;
    ArrayList<Member> values;
    MeetingAttendanceRepo attendanceRepo ;
    VslaCycleRepo cycleRepo;
    VslaCycle currentCycle;
    private int meetingId;
    private boolean isFromCode = false;

    //Use Class variable to hold state as it will be lost on scrolling
    private final boolean[] mCheckedState;

    public MembersRollCallArrayAdapter(Context context, ArrayList<Member> values) {
        super(context, R.layout.row_member_roll_call_2, values);
        this.context = context;
        this.values = values;

        //set array size to number of records in the adapter
        this.mCheckedState = new boolean[values.size()];

        attendanceRepo = new MeetingAttendanceRepo(getContext());
        cycleRepo = new VslaCycleRepo(getContext());
        currentCycle = cycleRepo.getCurrentCycle();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        try {
            //Here I populate the ListView Row with data.
            //I will handle the itemClick event in the ListView view on the actual fragment
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.row_member_roll_call_2, parent, false);

            //Get the Widgets
            //final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMRCFullNames);
            //final CheckBox chkAttendance = (CheckBox)rowView.findViewById(R.id.chkRMRCAttendance);
            final CheckBox chkAttendance = (CheckBox)rowView.findViewById(R.id.chkRMRCallAttendance);
            final TextView txtFullNames = (TextView)rowView.findViewById(R.id.txtRMRCallFullNames);
            final TextView txtAttendance = (TextView)rowView.findViewById(R.id.txtRMRCallAttendance);

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
            if(Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
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
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return null;
        }
    }

    public void setMeetingId(int meetingId) {
        this.meetingId = meetingId;
    }
}
