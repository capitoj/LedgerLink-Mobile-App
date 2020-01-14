package org.applab.ledgerlink;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.VslaCycleRepo;

/**
 * Created by Home on 13/01/2020.
 */

public class TestingActivity extends AppCompatActivity {

    LedgerLinkApplication ledgerLinkApplication;
    private VslaInfo vslaInfo = null;
    private Context context;
    private static int noOfMeetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());

        setContentView(R.layout.activity_testing);

        this.context = this;


        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

                        // For debugging purpose
                TextView txtVslaName = (TextView) findViewById(R.id.idVslaName);
                TextView txtNoMeeting = (TextView) findViewById(R.id.idNoMeeting);
                TextView txtNoMembers = (TextView) findViewById(R.id.idNoMembers);
                TextView txtNoCycle = (TextView) findViewById(R.id.idNoCycle);


                VslaInfo vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();
                String vslaName = vslaInfo.getVslaName();
                //VslaName
                txtVslaName.setText(vslaName);

                //No. of meeting
                VslaCycle recentCycle = new VslaCycleRepo(context).getMostRecentCycle();
                noOfMeetings = ledgerLinkApplication.getMeetingRepo().getAllMeetings(recentCycle.getCycleId()).size();
                txtNoMeeting.setText(Utils.formatNumber(noOfMeetings));

                //No.of Members
                int noOfMember = ledgerLinkApplication.getMemberRepo().getAllMembers().size();
                txtNoMembers.setText(Utils.formatNumber(noOfMember));

                //No. of complete Cycle
                int noOfCycle = ledgerLinkApplication.getVslaCycleRepo().getCompletedCycles().size();
                txtNoCycle.setText(Utils.formatNumber(noOfCycle));

                //end debugging code


    }

    public static int getNoOfMeeting() {

        return noOfMeetings;
    }

}
