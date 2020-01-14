package org.applab.ledgerlink;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.fontutils.TypefaceTextView;
import org.applab.ledgerlink.helpers.Utils;

public class GettingStartedWizardPageOne  extends ActionBarActivity{
    LedgerLinkApplication ledgerLinkApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());


        setContentView(R.layout.activity_getting_started_wizard_page_1);

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.app_icon_back);

        //For test purposes, create vsla info if not exists
        /* VslaInfoRepo repo = new VslaInfoRepo(getBaseContext());
        if(!repo.vslaInfoExists()) {
           repo.saveVslaInfo("TESTVSLA", "TESTVSLA", "1234");
        }
        */

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_next, null);
        customActionBarView.findViewById(R.id.actionbar_next).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent stage = new Intent(getBaseContext(), GettingStartedWizardNewCycleActivity.class);
                        stage.putExtra("_isFromReviewMembers", false);
                        startActivity(stage);
                        finish();
                    }
                });

        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.get_started));
       /** actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE); */
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        actionBar.setDisplayShowCustomEnabled(true);


        VslaInfo vslaInfo = ledgerLinkApplication.getVslaInfoRepo().getVslaInfo();
        TextView savingsGroupName = (TextView) findViewById(R.id.txtNCP_header);
        if(!vslaInfo.isActivated()) {
          //If not activated, show message to avoid displaying "Offline Mode" as vsla name
            savingsGroupName.setText("");
        }
        else {

        savingsGroupName.setText(vslaInfo.getVslaName());
        }

        TypefaceTextView txtGSW_info = (TypefaceTextView) findViewById(R.id.txtGSW_info);
        SpannableStringBuilder txtGSWInfoText = new SpannableStringBuilder(getString(R.string.if_not_beginnign_of_cycle));
        SpannableString nextText = new SpannableString(getString(R.string.next_));
        nextText.setSpan(new StyleSpan(Typeface.BOLD), 0, nextText.length()-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtGSWInfoText.append(nextText);

        txtGSW_info.setText(txtGSWInfoText);

        ledgerLinkApplication.getVslaInfoRepo().updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_ONE);
    }



  /**  @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()) {
            case R.id.mnuNCNext:
                //First Save the Cycle Dates
                //If successful move to next activity
                //Next page
                Intent mainMenu = new Intent(getBaseContext(), GettingStartedWizardPageTwo.class);
                startActivity(mainMenu);
                finish();

        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.getting_started_wizard_page_one_menu, menu);
        return true;

    }
    */
}
