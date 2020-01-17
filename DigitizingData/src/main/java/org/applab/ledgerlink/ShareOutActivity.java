package org.applab.ledgerlink;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class ShareOutActivity extends Activity {
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

        View actionBar = findViewById(R.id.actionBarShareOut);
        TextView actionBarActionBack = actionBar.findViewById(R.id.backAction);

        actionBarActionBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            }
        });

        refreshActivityView();


        Runnable populateShareOutList = new Runnable() {
            @Override
            public void run() {
                populateShareOutDetails();
            }
        };
        LongTaskRunner.runLongTask(populateShareOutList, getString(R.string.please_wait), getString(R.string.loading_member_list), ShareOutActivity.this);


    }


    private void refreshActivityView() {


        //populate the list
        populateShareOutList();
        //add LayoutParams
        //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }


    //Populate Share Out List
    protected void populateShareOutList() {

        //populate the share out details
        populateShareOutDetails();

        // Now get the data via the adapter
        members = ledgerLinkApplication.getMemberRepo().getActiveMembers();

        if (members == null) {
            members = new ArrayList<Member>();
        }

        //Now get the data via the adapter
        final ShareOutArrayAdapter adapter = new ShareOutArrayAdapter(getBaseContext(), members);

        // listening to single list item on click
        ListView shareOutListView = findViewById(R.id.lstShareOutList);
        shareOutListView.setAdapter(adapter);
        Utils.setListViewHeightBasedOnChildren(shareOutListView);
    }


    //Populate Share Out Details
    protected void populateShareOutDetails() {
        //to populate the share out details
        //Now get the data via the adapter

        final TextView txtNewShareValue = (TextView) findViewById(R.id.lblHeaderNewShareValue);
        TextView txtTotalSaving = (TextView) findViewById(R.id.lblHeaderTotalSavings);
        TextView txtTotalInterest = (TextView) findViewById(R.id.lblHeaderTotalInterest);
        TextView txtTotalFines = (TextView) findViewById(R.id.lblHeaderTotalFines);
        final EditText txtTotalEarnings = (EditText) findViewById(R.id.lblHeaderTotalEarnings);

        double totalSavings = ShareOutArrayAdapter.getTotalSaving();
        txtTotalSaving.setText(getString(R.string.total_savings) + " " + Utils.formatNumber(totalSavings) + " UGX");
        double totalInterest = ShareOutArrayAdapter.getTotalInterest();
        txtTotalInterest.setText(getString(R.string.total_interest) + " " + Utils.formatNumber(totalInterest) + " UGX");
        double totalFine = ShareOutArrayAdapter.getTotalFine();
        txtTotalFines.setText(getString(R.string.total_fines) + " " + Utils.formatNumber(totalFine) + " UGX");
        final double totalEarnings = ShareOutArrayAdapter.getTotalEarnings();
        txtTotalEarnings.setText(Utils.formatNumber(totalEarnings));
        double newShareValue = ShareOutArrayAdapter.getNewShareValue();
        txtNewShareValue.setText(getString(R.string.new_share_value) + " " + Utils.formatNumber(newShareValue) + " UGX");

        txtTotalEarnings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(MotionEvent.ACTION_UP == event.getAction()) {
                    //uDialogMessageBox.show(ShareOutActivity.this, "Warning", "Are you sure about this ShareOut Amount");

                    // Creating alert Dialog with one Button
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShareOutActivity.this);
                    // Get the layout inflater
                    final View customLayout = getLayoutInflater().inflate(R.layout.dialog_shareout, null);
                    alertDialog.setView(customLayout);

                    // Setting Dialog Title
                    //alertDialog.setTitle("Warning!");

                    // Setting Dialog Message
                    //alertDialog.setMessage("Are you sure you want to change the ShareOut Amount");
                    // final EditText input = new EditText(this);
                    //alertDialog.setView(input);

                    // Setting Positive "Okay" Button
                    alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int which) {
                                    // Write your code here to execute after dialog
                                    Toast.makeText(getApplicationContext(),"Password Matched", Toast.LENGTH_SHORT).show();
                                    // do something with the data coming from the AlertDialog

                                    // send data from the AlertDialog to the Activity
//                                    EditText editText = customLayout.findViewById(R.id.editText);
//                                    sendDialogDataToActivity(editText.getText().toString());
//                                    private void sendDialogDataToActivity(String data) {
//                                        Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
//                                    }
                                }
                            });
                    // Setting Negative "Cancel" Button
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write your code here to execute after dialog
                                    dialog.cancel();
                                }
                            });

                    // closed

                    // Showing Alert Message
                    alertDialog.show();
                }

                return true;
            }
        });

//        txtTotalEarnings.setOnClickListener(new View.OnClickListener() {
//            //@SuppressWarnings("deprecation")
//            public void onClick(final View view) {
//
//
//            }
//
//        });

//        txtTotalEarnings.setTag(false);
//        txtTotalEarnings.setOnFocusChangeListener(new View.OnFocusChangeListener(){
//            @Override
//            public void onFocusChange(View v, boolean hasFocus){
//                txtTotalEarnings.setTag(true);
//            }
//        });

        //txtTotalEarnings.setTag(false);
        //final boolean editingShareOutAmount = false;
//        txtTotalEarnings.addTextChangedListener(new TextWatcher() {
//
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Compute the New Members' ShareOut Amount
//                double editedTotalEarnings;
//                try {
//                    if (s.toString().length() <= 0) {
//
//                        return;
//                    }
//                    editedTotalEarnings = Double.parseDouble(s.toString());
//                } catch (Exception ex) {
//                    return;
//                }
//
//                double cycleNoOfStars = ShareOutArrayAdapter.getNoOfCycleStars();
//                double editedShareValue = editedTotalEarnings / cycleNoOfStars;
//                txtNewShareValue.setText(String.format("%,.0f UGX", editedShareValue));
//                txtTotalEarnings.setTag(true);
//
////                Runnable runnable = new Runnable() {
////                    @Override
////                    public void run() {
////                        Intent intent = new Intent(getApplicationContext(), ShareOutActivity.class);
////                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                        startActivity(intent);
////
////                    }
////                };
////
////                String warning = "Are you sure about this amount?";
////                DialogMessageBox.show(ShareOutActivity.this, getString(R.string.warning), warning, runnable);
//                DialogMessageBox.show(ShareOutActivity.this, "Warning", "Are you sure about this ShareOut Amount");
//
//            }
//
//          @Override
//          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//          }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Compute the New Members' ShareOut Amount
//                double editedTotalEarnings;
//                try {
//                    if (s.toString().length() <= 0) {
//                        return;
//                    }
//                    editedTotalEarnings = Double.parseDouble(s.toString());
//                } catch (Exception ex) {
//                    return;
//                }
//
//                double cycleNoOfStars = ShareOutArrayAdapter.getNoOfCycleStars();
//                double editedShareValue = editedTotalEarnings / cycleNoOfStars;
//                txtNewShareValue.setText(String.format("%,.0f UGX", editedShareValue));
//                txtTotalEarnings.setTag(true);
//
////                Runnable runnable = new Runnable() {
////                    @Override
////                    public void run() {
////                        Intent intent = new Intent(getApplicationContext(), ShareOutActivity.class);
////                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                        startActivity(intent);
////
////                    }
////                };
////
////                String warning = "Are you sure about this amount?";
////                DialogMessageBox.show(ShareOutActivity.this, getString(R.string.warning), warning, runnable);
//                DialogMessageBox.show(ShareOutActivity.this, "Warning", "Are you sure about this ShareOut Amount");
//
//            }
//        });

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
