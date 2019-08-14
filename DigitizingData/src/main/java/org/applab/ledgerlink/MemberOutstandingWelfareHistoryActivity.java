package org.applab.ledgerlink;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.MeetingOutstandingWelfare;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.EnhancedListView;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.VslaCycleRepo;

import java.util.ArrayList;
import java.util.Date;

import static org.applab.ledgerlink.service.UpdateChatService.getActivity;

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
        inflateActionBar();

        setContentView(R.layout.activity_member_outstanding_welfare_history);

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
        TextView lblOutstandingWelfareFullName = (TextView)findViewById(R.id.lblOutstandingWelfareFullName);
        lblOutstandingWelfareFullName.setText(1 + ". " + fullName);

        mListView = (EnhancedListView) findViewById(android.R.id.list);

        populateOutstandingWelfareHistory();
    }

    protected void populateOutstandingWelfareHistory(){
        ArrayList<MeetingOutstandingWelfare> meetingOutstandingWelfares = ledgerLinkApplication.getMeetingOutstandingWelfareRepo().getMemberOutstandingWelfareHistory(targetCycleId, memberId);
        if(meetingOutstandingWelfares == null){
            meetingOutstandingWelfares = new ArrayList<MeetingOutstandingWelfare>();
        }

        MemberOutstandingWelfareHistoryAdapter adapter = new MemberOutstandingWelfareHistoryAdapter(getApplicationContext(), meetingOutstandingWelfares);
        mListView.setAdapter(adapter);
    }

    private void inflateActionBar() {
        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) ((ActionBarActivity)getActivity()).getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back, null);
        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), MeetingActivity.class);
                        i.putExtra("_tabToSelect", getString(R.string.outstandingwelfare));
                        i.putExtra("_meetingDate", meetingDate);
                        i.putExtra("_meetingId", meetingId);
                        //startActivity(i);
                        finish();
                    }
                }
        );


        ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }
        actionBar.setTitle(R.string.fines_smallcaps);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);

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

            if(meetingOutstandingWelfare.getExpectedDate() != null) {
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
