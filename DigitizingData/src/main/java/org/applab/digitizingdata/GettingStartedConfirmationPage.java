package org.applab.digitizingdata;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class GettingStartedConfirmationPage extends SherlockActivity {


    ActionBar actionBar;
    boolean confirmed = false;
    private View customActionBarView;
    private double totalSavings = 0.0;
    private double expectedStartingCash = 0.0;
    boolean successFlg = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started_wizard_is_everything_correct);

        inflateCustombar();

        /**  actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(false); //Please leave this as false, it will be enabled on confirmation
         actionBar.setTitle("GET STARTED");
         */
        // Set instructions
        TypefaceTextView lblConfirmationText = (TypefaceTextView) findViewById(R.id.lblConfirmationText);

        SpannableString doneText = new SpannableString("done");
        // doneText.setSpan(new StyleSpan(Typeface.BOLD), 0, doneText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString backText = new SpannableString("back");
        // backText.setSpan(new StyleSpan(Typeface.BOLD), 0, backText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder instruction = new SpannableStringBuilder();
        instruction.append("If you are satisfied that all information is correct, please press <b> ");
        instruction.append(doneText);
        instruction.append("</b> otherwise press <b>");
        instruction.append(backText);
        instruction.append("</b> to revise information.");

        lblConfirmationText.setText(Html.fromHtml(instruction.toString()));

        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
        vslaInfoRepo.updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_CONFIRMATION);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {

            case android.R.id.home:
                if (confirmed) {
                    Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(mainActivity);

                    finish();
                    return true;
                }
                return true;
        }
        return true;

    }

    private boolean progressConfirmation() {

        // If already confirmed, load the main activity
        if (confirmed) {

        Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);
            startActivity(mainActivity);

            finish();
            return true;
        }
        // Finished wizard...
        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
        boolean updateStatus = vslaInfoRepo.updateGettingStartedWizardCompleteFlag(true);

        // Update GSW starting cash
        updateGSWStartingCash();

        if (updateStatus) {

            // Update text
            TextView heading = (TextView) findViewById(R.id.lblConfirmationHeading);
            heading.setText("Thank you!");

            TextView content = (TextView) findViewById(R.id.lblConfirmationText);
            content.setText("You have entered all information about your savings group and the current cycle. You may now use the phone at every meeting to enter savings and loan activity.");
            confirmed = true;

            // Enable home button
            actionBar.setDisplayHomeAsUpEnabled(true);


            //TODO: hide back menu button
            customActionBarView.findViewById(R.id.actionbar_back).setVisibility(View.GONE);
            customActionBarView.findViewById(R.id.actionbar_done).setVisibility(View.GONE);


        }
        return false;
    }

    // Update starting cash at end of GSW
    private void updateGSWStartingCash() {

        MeetingRepo meetingRepo = new MeetingRepo(getBaseContext());
        Meeting dummyGettingStartedWizardMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();

        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(getBaseContext());
        totalSavings = meetingSavingRepo.getTotalSavingsInMeeting(dummyGettingStartedWizardMeeting.getMeetingId());
        expectedStartingCash = dummyGettingStartedWizardMeeting.getVslaCycle().getFinesAtSetup() + dummyGettingStartedWizardMeeting.getVslaCycle().getInterestAtSetup() + totalSavings;

        // Save Starting cash values
        successFlg = meetingRepo.updateExpectedStartingCash(dummyGettingStartedWizardMeeting.getMeetingId(), expectedStartingCash);
    }

    /* Inflates custom menu bar for confirmation page */
    public void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_back_done, null);

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressConfirmation();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_back).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getBaseContext(), GettingStartedWizardReviewMembersActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
        );


        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("GET STARTED");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        /** actionBar.setDisplayOptions(
         ActionBar.DISPLAY_SHOW_CUSTOM,
         ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
         | ActionBar.DISPLAY_SHOW_TITLE
         ); */
        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //final MenuInflater inflater = getSupportMenuInflater();
        //inflater.inflate(R.menu.done_cancel, menu);
        Menu MENU = menu;

        return true;

    }

}
