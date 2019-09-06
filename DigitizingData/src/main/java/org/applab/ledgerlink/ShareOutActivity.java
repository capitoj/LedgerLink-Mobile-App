package org.applab.ledgerlink;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.ShareOutArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;


/**
 * Created by Moses on 7/16/13.
 */
public class ShareOutActivity extends AppCompatActivity {
    private ArrayList<Member> members;
    Context context;
    int meetingId;

    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        setContentView(R.layout.activity_shareout_list);
        refreshActivityView();


        Runnable populateShareOutList = new Runnable()
        {
            @Override
            public void run()
            {
                populateShareOutDetails();
            }
        };
        LongTaskRunner.runLongTask(populateShareOutList, getString(R.string.please_wait), getString(R.string.loading_member_list), ShareOutActivity.this);


    }


    private void refreshActivityView() {


        //populate the list
        populateShareOutList();
        //add LayoutParams
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }


    //Populate Share Out List
    protected void populateShareOutList() {

        //populate the share out details
        populateShareOutDetails();

        // Now get the data via the adapter
        members = ledgerLinkApplication.getMemberRepo().getActiveMembers();

        if(members == null) {
            members = new ArrayList<Member>();
        }

        //Now get the data via the adapter
        final ShareOutArrayAdapter adapter = new ShareOutArrayAdapter(getBaseContext(), members);

        // listening to single list item on click
        ListView shareOutListView = (ListView) findViewById(R.id.lstShareOutList);
        shareOutListView.setAdapter(adapter);
        Utils.setListViewHeightBasedOnChildren(shareOutListView);
    }


    //Populate Share Out Details
    protected void populateShareOutDetails() {
        //to populate the share out details
        //Now get the data via the adapter

        TextView txtTotalSaving = (TextView) findViewById(R.id.lblHeaderTotalSavings);
        TextView txtTotalInterest = (TextView) findViewById(R.id.lblHeaderTotalInterest);
        TextView txtTotalFines = (TextView) findViewById(R.id.lblHeaderTotalFines);
        TextView txtTotalEarnings = (TextView) findViewById(R.id.lblHeaderTotalEarnings);
        TextView txtNewShareValue = (TextView) findViewById(R.id.lblHeaderNewShareValue);

        double totalSavings = ShareOutArrayAdapter.getTotalSaving();
        txtTotalSaving.setText(getString(R.string.total_savings)  + Utils.formatNumber(totalSavings) + " UGX");
        double totalInterest = ShareOutArrayAdapter.getTotalInterest();
        txtTotalInterest.setText(getString(R.string.total_interest) + Utils.formatNumber(totalInterest) + " UGX");
        double totalFine = ShareOutArrayAdapter.getTotalFine();
        txtTotalFines.setText(getString(R.string.total_fines) + Utils.formatNumber(totalFine) + " UGX");
        double totalEarnings = ShareOutArrayAdapter.getTotalEarnings();
        txtTotalEarnings.setText(getString(R.string.total_earnings) + Utils.formatNumber(totalEarnings) + " UGX");
        double newShareValue = ShareOutArrayAdapter.getNewShareValue();
        txtNewShareValue.setText(getString(R.string.new_share_value) + Utils.formatNumber(newShareValue) + " UGX");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //final MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.begin_meeting, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
 // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //NOT necessary since we are not using custom view

        return true;

    }


}
