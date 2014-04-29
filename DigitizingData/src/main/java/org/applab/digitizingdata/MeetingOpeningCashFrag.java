package org.applab.digitizingdata;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.util.HashMap;

public class MeetingOpeningCashFrag extends SherlockFragment {

    ActionBar actionBar = null;
    String meetingDate = null;
    int meetingId = 0;
    double totalCash = 0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_meeting_opening_cash, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        actionBar = getSherlockActivity().getSupportActionBar();
        String title = "Meeting";
        switch(Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMOCFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate);

        populateOpeningCash();
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_opening_cash, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                return false;
            case R.id.mnuSMDSend:
                return false;
            case R.id.mnuSMDCancel:
                return false;
            case R.id.mnuMCBFSave:
                return false;
            case R.id.mnuMOCFSave:
                TextView txtTotalCash = (TextView)getSherlockActivity().findViewById(R.id.txtMOCTotal);
                if(saveOpeningCash()) {
                    Toast.makeText(getSherlockActivity().getApplicationContext(), "Starting Cash has been saved", Toast.LENGTH_LONG).show();

                    txtTotalCash.setText(String.format("%,.0f UGX",totalCash));
                }
                else {
                    Toast.makeText(getSherlockActivity().getApplicationContext(), "Starting Cash not saved", Toast.LENGTH_LONG).show();

                    txtTotalCash.setText(String.format("%,.0f UGX",0));
                }
                return true;
            default:
                return false;
        }
    }

    public boolean saveOpeningCash(){
        boolean successFlg = false;
        double theCashFromBox = 0.0;
        double theCashFromBank = 0.0;
        double theFinesPaid = 0.0;

        try{
            TextView txtCashFromBox = (TextView)getSherlockActivity().findViewById(R.id.txtMOCBalBfBox);
            String amountBox = txtCashFromBox.getText().toString().trim();
            if (amountBox.length() < 1) {
                //Allow it to be Zero
                theCashFromBox = 0.0;
            }
            else {
                theCashFromBox = Double.parseDouble(amountBox);
                if (theCashFromBox < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting","The value for Cash from Box is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtCashFromBox.requestFocus();
                    return false;
                }
            }

            TextView txtCashFromBank = (TextView)getSherlockActivity().findViewById(R.id.txtMOCBalBfBank);
            String amountBank = txtCashFromBank.getText().toString().trim();
            if (amountBank.length() < 1) {
                //Allow it to be Zero
                theCashFromBank = 0.0;
            }
            else {
                theCashFromBank = Double.parseDouble(amountBank);
                if (theCashFromBank < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting","The value for Cash withdrawn from Bank is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtCashFromBank.requestFocus();
                    return false;
                }
            }

            TextView txtFinesPaid = (TextView)getSherlockActivity().findViewById(R.id.txtMOCFines);
            String amountFines = txtFinesPaid.getText().toString().trim();
            if (amountFines.length() < 1) {
                //Allow it to be Zero
                theFinesPaid = 0.0;
            }
            else {
                theFinesPaid = Double.parseDouble(amountFines);
                if (theFinesPaid < 0.00) {
                    Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting","The value for Fines Paid is invalid.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    txtFinesPaid.requestFocus();
                    return false;
                }
            }

            //Now Save
            MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());

            totalCash = theCashFromBox + theCashFromBank + theFinesPaid;
            successFlg = meetingRepo.updateOpeningCash(meetingId,theCashFromBox, theCashFromBank, theFinesPaid);
            return successFlg;
        }
        catch(Exception ex) {
            Log.e("Meeting.saveOpeningCash", ex.getMessage());
            return successFlg;
        }
    }

    private void populateOpeningCash(){
        MeetingRepo meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
        HashMap<String, Double> openingCash = meetingRepo.getMeetingOpeningCash(meetingId);

        TextView txtCashBox = (TextView)getSherlockActivity().findViewById(R.id.txtMOCBalBfBox);
        TextView txtCashBank = (TextView)getSherlockActivity().findViewById(R.id.txtMOCBalBfBank);
        TextView txtFinesPaid = (TextView)getSherlockActivity().findViewById(R.id.txtMOCFines);
        TextView txtCashTotal = (TextView)getSherlockActivity().findViewById(R.id.txtMOCTotal);

        if(null != openingCash) {
            txtCashBox.setText(String.format("%.0f", openingCash.get(MeetingSchema.COL_MT_CASH_FROM_BOX)));
            txtCashBank.setText(String.format("%.0f", openingCash.get(MeetingSchema.COL_MT_CASH_FROM_BANK)));
            txtFinesPaid.setText(String.format("%.0f", openingCash.get(MeetingSchema.COL_MT_CASH_FINES)));
            double cashTotal = 0.0;
            for(double value : openingCash.values()) {
                cashTotal += value;
            }
            txtCashTotal.setText(String.format("%,.0f UGX",cashTotal));
        }
    }
}
