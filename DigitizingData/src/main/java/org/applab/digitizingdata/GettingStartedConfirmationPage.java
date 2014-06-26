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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.internal.view.menu.ActionMenuItemView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.VslaInfo;
import org.applab.digitizingdata.fontutils.TypefaceTextView;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.VslaInfoRepo;

public class GettingStartedConfirmationPage extends SherlockActivity {


    ActionBar actionBar;
    boolean confirmed = false;
    private View customActionBarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started_wizard_is_everything_correct);
        inflateCustombar();
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false); //Please leave this as false, it will be enabled on confirmation
        actionBar.setTitle("GET STARTED");

        // Set instructions
        TypefaceTextView lblConfirmationText = (TypefaceTextView) findViewById(R.id.lblConfirmationText);

        SpannableString doneText = new SpannableString("done ");
        doneText.setSpan(new StyleSpan(Typeface.BOLD), 0, doneText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString cancelText = new SpannableString("cancel ");
        cancelText.setSpan(new StyleSpan(Typeface.BOLD), 0, cancelText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder instruction = new SpannableStringBuilder();
        instruction.append("If you are satisfied that all information is correct, please tap ");
        instruction.append(doneText);
        instruction.append("otherwise tap ");
        instruction.append(cancelText);
        instruction.append(" to revise information.");

        lblConfirmationText.setText(instruction);

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

            //Enable home button
            actionBar.setDisplayHomeAsUpEnabled(true);


            //TODO: hide cancel menu button
            customActionBarView.findViewById(R.id.actionbar_cancel).setVisibility(View.GONE);
            customActionBarView.findViewById(R.id.actionbar_done).setVisibility(View.GONE);


        }
        return false;
    }


    /* inflates custom menu bar for confirmation page */
    public void inflateCustombar() {

        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        
        customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressConfirmation();
                    }
                }
        );

        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
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
