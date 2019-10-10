package org.applab.ledgerlink;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.EnhancedListView;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

public class MemberOutstandingWelfareHistoryActivity extends ListActivity {

    LedgerLinkApplication ledgerLinkApplication;
    private String meetingDate;
    private int memberId;
    private int meetingId;
    private int targetCycleId = 0;
    private EnhancedListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        //inflateActionBar();

        setContentView(R.layout.activity_member_outstanding_welfare_history);

        View actionBar = findViewById(R.id.memberOutstandingWelfareHistory);

        TextView actionBarActionBack = actionBar.findViewById(R.id.actionBack);

        actionBarActionBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                i.putExtra("_tabToSelect", getString(R.string.outstandingwelfare));
                i.putExtra("_meetingDate", meetingDate);
                i.putExtra("_meetingId", meetingId);
                //startActivity(i);
                finish();
            }
        });


        meetingDate = getIntent().getStringExtra("_meetingDate");

        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }

        if (getIntent().hasExtra("_memberId")) {
            memberId = getIntent().getIntExtra("_memberId", 0);
        }

        Meeting targetMeeting = ledgerLinkApplication.getMeetingRepo().getMeetingById(meetingId);

        if (targetMeeting != null && targetMeeting.getVslaCycle() != null) {
            targetCycleId = targetMeeting.getVslaCycle().getCycleId();
        }

        String fullName = getIntent().getStringExtra("_name");
        TextView lblOutstandingWelfareFullName = (TextView) findViewById(R.id.lblOutstandingWelfareFullName);
        lblOutstandingWelfareFullName.setText(1 + ". " + fullName);

        mListView = (EnhancedListView) findViewById(android.R.id.list);

        populateOutstandingWelfareHistory();
    }

    protected void populateOutstandingWelfareHistory(){
        MeetingOutstandingWelfare meetingOutstandingWelfare = ledgerLinkApplication.getMeetingOutstandingWelfareRepo().getOutstandingMemberWelfare(targetCycleId, memberId);
        if(meetingOutstandingWelfare == null){
            meetingOutstandingWelfare = new MeetingOutstandingWelfare();
        }
        ArrayList<MeetingOutstandingWelfare> meetingOutstandingWelfares = new ArrayList<>();
        meetingOutstandingWelfares.add(meetingOutstandingWelfare);

        MemberOutstandingWelfareHistoryAdapter adapter = new MemberOutstandingWelfareHistoryAdapter(getApplicationContext(), meetingOutstandingWelfares);
        mListView.setAdapter(adapter);
    }

    public class MemberOutstandingWelfareHistoryAdapter extends ArrayAdapter<MeetingOutstandingWelfare> {

        private Context context;
        private Typeface typeface;
        private ArrayList<MeetingOutstandingWelfare> meetingOutstandingWelfares;
        private boolean changedFromCode = false;
        String datePaid;

        public MemberOutstandingWelfareHistoryAdapter(Context context, ArrayList<MeetingOutstandingWelfare> meetingOutstandingWelfares) {

            super(context, R.layout.row_outstanding_welfare_history, meetingOutstandingWelfares);
            this.context = context;
            this.meetingOutstandingWelfares = meetingOutstandingWelfares;
            this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto-regular.ttf");
        }

        @Override
        public int getCount() {
            return this.meetingOutstandingWelfares.size();
        }

//        @Override
//        public Object getItem(int position) {
//            return this.meetingOutstandingWelfares.get(position);
//        }

//        @Override
//        public long getItemId(int position) {
//            return position;
//        }

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {

            final ViewHolder holder;

            if (rowView == null) {
                rowView = getLayoutInflater().inflate(R.layout.row_outstanding_welfare_history, parent, false);

                changedFromCode = true;
                holder = new ViewHolder();
                assert rowView != null;
                holder.txtOWExpectedDate = (TextView) rowView.findViewById(R.id.txtOutstandingMeetingExpectedDate);
                holder.txtOWAmount = (TextView) rowView.findViewById(R.id.txtMemberOutstandingWelfareAmount);
                holder.paidStatusCheckBox = (CheckBox) rowView.findViewById(R.id.chkMemberOutstandingWelfare);

                // Set typeface
                holder.txtOWExpectedDate.setTypeface(typeface);
                holder.txtOWAmount.setTypeface(typeface);

                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            //Assign Values to the Widgets
            final MeetingOutstandingWelfare meetingOutstandingWelfare = meetingOutstandingWelfares.get(position);

            RelativeLayout parentLayout = (RelativeLayout) holder.paidStatusCheckBox.getParent();
            parentLayout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if ((meetingOutstandingWelfare.getIsCleared() == 1) && (!(meetingOutstandingWelfare.getPaidInMeeting().getMeetingId() == meetingId))) {
                                                        Toast.makeText(context, R.string.selescted_outstanding_welfare_already_paid, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
            );


            // If fines were paid in previous meetings disable editing
            if ((meetingOutstandingWelfare.getIsCleared() == 1) && (meetingOutstandingWelfare.getPaidInMeeting().getMeetingId() != meetingId)) {
                holder.paidStatusCheckBox.setEnabled(false);
                holder.paidStatusCheckBox.setClickable(false);
                parentLayout.setEnabled(false);
            } else {
                holder.paidStatusCheckBox.setEnabled(true);
                holder.paidStatusCheckBox.setClickable(true);
                parentLayout.setEnabled(true);
            }

            holder.paidStatusCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                     @Override
                                                                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                                         if (!changedFromCode) {
                                                                             if (isChecked) {
                                                                                 meetingOutstandingWelfare.setIsCleared(1);
                                                                                 datePaid = meetingDate;
                                                                             } else {
                                                                                 meetingOutstandingWelfare.setIsCleared(0);
                                                                                 datePaid = null;
                                                                             }
                                                                             Date dateCleared = Utils.stringToDate(meetingDate, Utils.OTHER_DATE_FIELD_FORMAT);
                                                                             ledgerLinkApplication.getMeetingOutstandingWelfareRepo().updateMemberOutstandingWelfare(meetingOutstandingWelfare.getOutstandingWelfareId(), meetingId, meetingOutstandingWelfare.getIsCleared(), dateCleared);
                                                                         }
                                                                     }
                                                                 }
            );


            holder.position = position;

            if (meetingOutstandingWelfare.getExpectedDate() != null) {
                holder.txtOWExpectedDate.setText(String.format(getString(R.string.due_date_) + Utils.formatDate(meetingOutstandingWelfare.getExpectedDate(), Utils.OTHER_DATE_FIELD_FORMAT)));
            }
            holder.txtOWAmount.setText(String.format("%,.0f UGX", meetingOutstandingWelfare.getAmount()));
            holder.paidStatusCheckBox.setChecked(meetingOutstandingWelfare.getIsCleared() != 0);
            changedFromCode = false;

            return rowView;
        }

        private class ViewHolder {
            CheckBox paidStatusCheckBox;
            TextView txtOWAmount;
            TextView txtOWExpectedDate;
            int position;
        }
    }
}
