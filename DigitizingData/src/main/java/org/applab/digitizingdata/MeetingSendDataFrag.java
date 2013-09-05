package org.applab.digitizingdata;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.helpers.Utils;

public class MeetingSendDataFrag extends SherlockFragment {

    ActionBar actionBar = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_meeting_send_data, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        TextView txtStatus = (TextView)getSherlockActivity().findViewById(R.id.lblMSDFragStatus);
        TextView txtInstructions = (TextView)getSherlockActivity().findViewById(R.id.lblMSDFragInstructions);

        if(isNetworkConnected(getSherlockActivity().getApplicationContext())) {
            txtStatus.setText("Data Network Is Available");
            txtInstructions.setText("You can send the meeting data now by tapping the Send button above.");
        }
        else {
            txtStatus.setText("No Data Network");
            txtInstructions.setText("Move to a place with data network to send the meeting data. You can send the data later by selecting Check & Send Data from the main menu.");
        }

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected());
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        if(isNetworkConnected(getSherlockActivity().getApplicationContext())) {
            getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_send_data, menu);
        }
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
            case R.id.mnuMSDFSend:
                return false;
            default:
                return false;
        }
    }
}
