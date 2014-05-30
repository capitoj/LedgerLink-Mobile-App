package org.applab.digitizingdata.helpers;

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

import com.actionbarsherlock.internal.nineoldandroids.animation.PropertyValuesHolder;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.MeetingFine;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.repo.MeetingAttendanceRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.net.ProtocolException;
import java.util.ArrayList;

/**
 * Created by Moses on 8/2/13.
 */
public class FineHistoryArrayAdapter extends ArrayAdapter<MemberFineRecord> {

    Context context;
    ArrayList<MemberFineRecord> values;
    int position;
    Typeface typeface;
    MeetingFineRepo finesRepo;
    VslaCycleRepo cycleRepo;
    VslaCycle currentCycle;

    public FineHistoryArrayAdapter(Context context, ArrayList<MemberFineRecord> values, String font) {
        super(context, R.layout.row_fines_history, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), font);

        finesRepo = new MeetingFineRepo(getContext());
        cycleRepo = new VslaCycleRepo(getContext());
        currentCycle = cycleRepo.getCurrentCycle();
    }

    static class ViewHolder {
        /**
         * protected TextView text;
         * protected CheckBox checkbox;
         */
        protected CheckBox chkMemberFineStatus;
        protected TextView lblFineMeetingDate;
        protected TextView lblFineType;
        protected TextView lblFineAmount;
        protected ViewGroup layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = null;
        try {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.row_fines_history, parent, false);

            //Assign Values to the Widgets
            final MemberFineRecord fineRecord = values.get(position);

            final CheckBox chkMemberFineStatus = (CheckBox) rowView.findViewById(R.id.chkMemberFine);

            chkMemberFineStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    final boolean isChecked = chkMemberFineStatus.isChecked();

                    if (isChecked) {
                        fineRecord.setStatus(1);
                    } else fineRecord.setStatus(0);

                    finesRepo.updateMemberFineStatus(fineRecord.getFineId(), fineRecord.getStatus());
                }
            });

            TextView lblFineMeetingDate = (TextView) rowView.findViewById(R.id.lblFineMeetingDate);
            TextView lblFineType = (TextView) rowView.findViewById(R.id.lblFineType);
            TextView lblFineAmount = (TextView) rowView.findViewById(R.id.txtMemberFineAmount);

            // Set typeface
            lblFineMeetingDate.setTypeface(typeface);
            lblFineAmount.setTypeface(typeface);
            lblFineType.setTypeface(typeface);


            if (fineRecord != null) {
                lblFineMeetingDate.setText(String.format(Utils.formatDate(fineRecord.getMeetingDate(), Utils.OTHER_DATE_FIELD_FORMAT)));
                lblFineAmount.setText(String.format("%,.0fUGX", fineRecord.getAmount()));
                lblFineType.setText("Change This text");
                chkMemberFineStatus.setChecked(fineRecord.getStatus() != 0);
            }

            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}