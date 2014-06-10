package org.applab.digitizingdata;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.view.menu.ActionMenuItemView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class GettingStartedConfirmationPage extends SherlockActivity {


    private Menu MENU;
    ActionBar actionBar;
    boolean confirmed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started_wizard_is_everything_correct);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false); //Please leave this as false
        actionBar.setTitle("GET STARTED");

        // Set instructions
        TypefaceTextView lblConfirmationText = (TypefaceTextView) findViewById(R.id.lblConfirmationText);

        SpannableString doneText = new SpannableString("done ");
        doneText.setSpan(new StyleSpan(Typeface.BOLD), 0, doneText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString cancelText = new SpannableString("cancel ");
        cancelText.setSpan(new StyleSpan(Typeface.BOLD), 0, cancelText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder instruction = new SpannableStringBuilder();
        instruction.append("If you are satisfied that all information is correct, please press ");
        instruction.append(doneText);
        instruction.append("otherwise press ");
        instruction.append(cancelText);
        instruction.append(" to revise information.");

        lblConfirmationText.setText(instruction);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {

            case android.R.id.home:
                if (!confirmed) {
                    //Only navigate to main if confirmed
                    return true;
                }
                Intent upIntent = new Intent(this, GettingStartedWizardReviewMembersActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, GettingStartedWizardReviewMembersActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;

            case R.id.mnuAMCancel:
                Intent mainMenu = new Intent(getBaseContext(), GettingStartedWizardReviewMembersActivity.class);
                startActivity(mainMenu);
                return true;
            case R.id.mnuAMDone:
                //IF already confirmed, load the main activity
                if (confirmed) {
                    Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(mainActivity);

                    finish();
                    return true;
                }
                //Finished wizard...
                VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(this);
                boolean updateStatus = vslaInfoRepo.updateGettingStartedWizardCompleteFlag(true);

                if (updateStatus) {
                    //Update text
                    TextView heading = (TextView) findViewById(R.id.lblConfirmationHeading);
                    heading.setText("Thank You!");

                    TextView content = (TextView) findViewById(R.id.lblConfirmationText);
                    content.setText("You have entered all information about your savings group and the current cycle. You may now use the phone at every meeting to enter savings and loan activity.");
                    confirmed = true;


                    //TODO: hide cancel menu button
                    MENU.findItem(R.id.mnuAMCancel).setVisible(false);
                    MENU.findItem(R.id.mnuAMDone).setVisible(false);

                }


        }
        return true;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.done_cancel, menu);

        MENU = menu;

        return true;

    }

}
