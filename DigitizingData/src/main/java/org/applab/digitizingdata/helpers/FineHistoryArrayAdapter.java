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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.applab.digitizingdata.R;
import org.applab.digitizingdata.domain.model.VslaCycle;
import org.applab.digitizingdata.repo.FineTypeRepo;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.VslaCycleRepo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 8/2/13.
 */
public class FineHistoryArrayAdapter extends ArrayAdapter<MemberFineRecord> {

    Context context;
    ArrayList<MemberFineRecord> values;
    int position;
    Typeface typeface;
    FineTypeRepo fineTypeRepo;
    MeetingFineRepo finesRepo;
    VslaCycleRepo cycleRepo;
    VslaCycle currentCycle;
    String datePaid;

    public FineHistoryArrayAdapter(Context context, ArrayList<MemberFineRecord> values, String font) {
        super(context, R.layout.row_fines_history, values);
        this.context = context;
        this.values = values;
        this.typeface = Typeface.createFromAsset(context.getAssets(), font);

        finesRepo = new MeetingFineRepo(getContext());
        fineTypeRepo = new FineTypeRepo(getContext());
        cycleRepo = new VslaCycleRepo(getContext());
        currentCycle = cycleRepo.getCurrentCycle();
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

            RelativeLayout parentLayout = (RelativeLayout) chkMemberFineStatus.getParent();
            parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chkMemberFineStatus.toggle();
                }
            });

            chkMemberFineStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                               @Override
                                                               public void onCheckedChanged(CompoundButton buttonView,
                                                                                            boolean isChecked) {

                                                                   if (isChecked) {
                                                                       fineRecord.setStatus(1);
                                                                       Date date = new Date();
                                                                       datePaid = Utils.formatDateToSqlite((date));
                                                                   } else {
                                                                       fineRecord.setStatus(0);
                                                                       datePaid = "";
                                                                   }

                                                                   finesRepo.updateMemberFineStatus(fineRecord.getFineId(), fineRecord.getStatus(), datePaid);
                                                               }
                                                           }
            );

            final TextView txtFineMeetingDate = (TextView) rowView.findViewById(R.id.txtFineMeetingDate);
            final TextView txtFineType = (TextView) rowView.findViewById(R.id.lblFineType);
            final TextView txtFineAmount = (TextView) rowView.findViewById(R.id.txtMemberFineAmount);

            // Set typeface
            txtFineMeetingDate.setTypeface(typeface);
            txtFineAmount.setTypeface(typeface);
            txtFineType.setTypeface(typeface);


            if (fineRecord != null)

            {
                txtFineMeetingDate.setText(String.format(Utils.formatDate(fineRecord.getMeetingDate(), Utils.OTHER_DATE_FIELD_FORMAT)));
                txtFineAmount.setText(String.format("%,.0fUGX", fineRecord.getAmount()));

                /** TODO: REMOVE and find a better way how
                 * Meantime fix for QA time */
                switch (fineRecord.getFineTypeId()) {
                    case 1:
                        fineRecord.setFineTypeName(context.getResources().getString(R.string.finetype_other));
                        break;
                    case 2:
                        fineRecord.setFineTypeName(context.getResources().getString(R.string.finetype_latecoming));
                        break;
                    case 3:
                        fineRecord.setFineTypeName(context.getResources().getString(R.string.finetype_disorder));
                        break;
                    default:
                        fineRecord.setFineTypeName("Unknown");
                }
                txtFineType.setText(fineRecord.getFineTypeName());
                chkMemberFineStatus.setChecked(fineRecord.getStatus() != 0);
            }

            return rowView;
        } catch (Exception ex) {
            Log.e("Errors:", "getView:> " + ((ex.getMessage() == null) ? "Generic Exception" : ex.getMessage()));
            return rowView;
        }
    }
}