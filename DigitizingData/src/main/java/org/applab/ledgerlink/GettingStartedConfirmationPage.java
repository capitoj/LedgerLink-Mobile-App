package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.fontutils.TypefaceTextView;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.repo.MeetingSavingRepo;

public class GettingStartedConfirmationPage extends AppCompatActivity {


    private ActionBar actionBar;
    private boolean confirmed = false;
    private View customActionBarView;

    LedgerLinkApplication ledgerLinkApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ledgerLinkApplication = (LedgerLinkApplication) getApplication();
        setContentView(R.layout.activity_getting_started_wizard_is_everything_correct);

        inflateCustombar();

        /**  actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(false); //Please leave this as false, it will be enabled on confirmation
         actionBar.setTitle("GET STARTED");
         */
        // Set instructions
        TypefaceTextView lblConfirmationText = (TypefaceTextView) findViewById(R.id.lblConfirmationText);

        SpannableString doneText = new SpannableString(getString(R.string.done));
        // doneText.setSpan(new StyleSpan(Typeface.BOLD), 0, doneText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString backText = new SpannableString(getString(R.string.back));
        // backText.setSpan(new StyleSpan(Typeface.BOLD), 0, backText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder instruction = new SpannableStringBuilder();
        instruction.append(getString(R.string.if_you_satisfied_that_all_info_correct));
        instruction.append(doneText);
        instruction.append(getString(R.string.otherwise_press));
        instruction.append(backText);
        instruction.append(getString(R.string.to_revise_info));

        lblConfirmationText.setText(Html.fromHtml(instruction.toString()));

        ledgerLinkApplication.getVslaInfoRepo().updateGettingStartedWizardStage(Utils.GETTING_STARTED_PAGE_CONFIRMATION);


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
        boolean updateStatus = ledgerLinkApplication.getVslaInfoRepo().updateGettingStartedWizardCompleteFlag();

        // Update GSW starting cash
        updateGSWStartingCash();

        if (updateStatus) {

            // Update text
            TextView heading = (TextView) findViewById(R.id.lblConfirmationHeading);
            heading.setText(R.string.thank_you);

            TextView content = (TextView) findViewById(R.id.lblConfirmationText);
            content.setText(R.string.you_have_entered_all_info);
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

        Meeting dummyGettingStartedWizardMeeting = ledgerLinkApplication.getMeetingRepo().getDummyGettingStartedWizardMeeting();

        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(getBaseContext());
        double totalSavings = meetingSavingRepo.getTotalSavingsInMeeting(dummyGettingStartedWizardMeeting.getMeetingId());
        double expectedStartingCash = dummyGettingStartedWizardMeeting.getVslaCycle().getFinesAtSetup() + dummyGettingStartedWizardMeeting.getVslaCycle().getInterestAtSetup() + totalSavings;

        // Save Starting cash values
        boolean successFlg = ledgerLinkApplication.getMeetingRepo().updateExpectedStartingCash(dummyGettingStartedWizardMeeting.getMeetingId(), expectedStartingCash);
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
        actionBar.setTitle(getString(R.string.get_started_allcaps));
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
        //final MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.done_cancel, menu);

        return true;

    }

}
