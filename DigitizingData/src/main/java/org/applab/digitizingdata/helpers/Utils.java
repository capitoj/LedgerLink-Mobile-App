package org.applab.digitizingdata.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.applab.digitizingdata.GettingStartedConfirmationPage;
import org.applab.digitizingdata.GettingStartedWizardAddMemberActivity;
import org.applab.digitizingdata.GettingStartedWizardNewCycleActivity;
import org.applab.digitizingdata.GettingStartedWizardPageOne;
import org.applab.digitizingdata.GettingStartedWizardPageTwo;
import org.applab.digitizingdata.GettingStartedWizardReviewMembersActivity;
import org.applab.digitizingdata.SettingsActivity;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Moses on 7/3/13.
 */
public class Utils {
    /*
    ** DATE UTILS
     */
    public static final String DATE_FIELD_FORMAT = "dd-MMM-yyyy";
    public static final String OTHER_DATE_FIELD_FORMAT = "dd MMM yyyy";
    public static final String REAL_FIELD_FORMAT = "#0";
    public static final String INTEGER_FIELD_FORMAT = "#0";

    //Constants for Alert Dialogs
    public static final String MSGBOX_ICON_INFORMATION = "INFORMATION";
    public static final String MSGBOX_ICON_EXCLAMATION = "EXCLAMATION";
    public static final String MSGBOX_ICON_QUESTION = "QUESTION";
    public static final String MSGBOX_ICON_TICK = "TICK";

    //Global Management of Tasks
    public static boolean _membersAccessedFromNewCycle = false;
    public static boolean _membersAccessedFromEditCycle = false;

    //Shared Preferences
    public static SharedPreferences sharedPreferences = null;

    //SERVER Connection
    public static String VSLA_SERVER_BASE_URL = "http://defaulturl.com/default";

    //Execution Mode: executeInTrainingMode
    private static boolean EXECUTING_IN_TRAINING_MODE = false;

    //Flag to determine whether training data should be refreshed
    private static boolean refreshDataFlg = false;

    //VSLA DATA MIGRATION
    public static final String VSLA_DATA_MIGRATION_FILENAME = "vslaMigrationInfo.csv";
    public static final String MISSING_NAME_MARKER = "MISSING";
    public static final int DEFAULT_MEMBER_AGE = 18;
    public static final String DEFAULT_MEMBER_OCCUPATION = "Peasant Farmer";

    //Used when sending Data
    private static String phoneImei;

    //A Defination of Getting started wizard stage indicators
    public static final int GETTING_STARTED_PAGE_ONE = 1;
    public static final int GETTING_STARTED_PAGE_PIN = 2;
    public static final int GETTING_STARTED_PAGE_NEW_CYCLE = 3;
    public static final int GETTING_STARTED_PAGE_ADD_MEMBER = 4;
    public static final int GETTING_STARTED_PAGE_REVIEW_MEMBERS = 5;
    public static final int GETTING_STARTED_PAGE_REVIEW_CYCLE = 6;
    public static final int GETTING_STARTED_PAGE_CONFIRMATION = 7;

    public static Date stringToDate(String dateString, String dateFormat) {

        try {
            return new SimpleDateFormat(dateFormat, Locale.ENGLISH).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();

        }
    }

    public static String formatAsPhoneNumber(String phoneNumber) {
        //Formats phone number as #### ### ###
        phoneNumber = phoneNumber.replaceAll(" ", ""); //first collapse all spaces
        if (phoneNumber.length() <= 4) {
            return phoneNumber;
        }
        if (phoneNumber.length() > 4 && phoneNumber.length() <= 7) {
            return insertPeriodically(phoneNumber, 4);
        }

        //length is greater than 7!
        phoneNumber = phoneNumber.substring(0, 4) + " " + insertPeriodically(phoneNumber.substring(4), 3);
        return phoneNumber;
    }

    //Calling this ensures that a phone number input is always formatted as a phone number
    public static void setAsPhoneNumberInput(final EditText editText) {
        TextWatcher phoneNumberTextWatcher = new TextWatcher() {
            String phoneText = "";

            @Override
            public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this); //remove it to prevent Stackover flow
                phoneText = formatAsPhoneNumber(editText.getText().toString());
                editText.setText("");
                editText.append(phoneText);
                editText.addTextChangedListener(this);

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        editText.addTextChangedListener(phoneNumberTextWatcher);

    }

    public static String insertPeriodically(
            String text, int period) {
        StringBuilder builder = new StringBuilder(
                text.length() + " ".length() * (text.length() / period) + 1);

        int index = 0;
        String prefix = "";
        while (index < text.length()) {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix);
            prefix = " ";
            builder.append(text.substring(index,
                    Math.min(index + period, text.length())));
            index += period;
        }
        return builder.toString();
    }


    //TODO: will create an enum of CURRENT_VIEW_MODE
    public enum MeetingDataViewMode {
        VIEW_MODE_CAPTURE,
        VIEW_MODE_REVIEW,
        VIEW_MODE_READ_ONLY
    }

    public static MeetingDataViewMode _meetingDataViewMode = MeetingDataViewMode.VIEW_MODE_CAPTURE;

    public enum MeetingActiveActionBarMenu {
        MENU_NONE,
        MENU_REVIEW_SEND,
        MENU_CASH_BOOK_TAB,
        MENU_START_CASH_TAB,
        MENU_SEND_DATA_TAB
    }

    public static MeetingActiveActionBarMenu _meetingActiveActionBarMenu = MeetingActiveActionBarMenu.MENU_NONE;

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        if (null == context) {
            return null;
        }

        if (null == sharedPreferences) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        return sharedPreferences;
    }

    /**
     * Setup the Defaults from the Shared Preferences
     *
     * @param context
     */
    public static void configureDefaultApplicationPreferences(Context context) {
        if (null == context) {
            return;
        }

        SharedPreferences preferences = getDefaultSharedPreferences(context);
        if (null == preferences) {
            return;
        }

        //Otherwise if all is ok continue
        VSLA_SERVER_BASE_URL = preferences.getString(SettingsActivity.PREF_KEY_SERVER_URL, "http://vsla.com/notset");
        EXECUTING_IN_TRAINING_MODE = Utils.getDefaultSharedPreferences(context).getString(SettingsActivity.PREF_KEY_EXECUTION_MODE, "1").equalsIgnoreCase(SettingsActivity.PREF_VALUE_EXECUTION_MODE_TRAINING);
        setRefreshDataFlag(Utils.getDefaultSharedPreferences(context).getBoolean(SettingsActivity.PREF_KEY_REFRESH_TRAINING_DATA, false));
    }

    public static void setRefreshDataFlag(boolean value) {
        refreshDataFlg = value;
    }

    public static boolean isRefreshDataFlgOn() {
        return refreshDataFlg;
    }

    public static void setExecutingInTrainingMode(boolean value) {
        EXECUTING_IN_TRAINING_MODE = value;
    }

    public static boolean isExecutingInTrainingMode() {
        return EXECUTING_IN_TRAINING_MODE;
    }

    public static String getPhoneImei() {
        try {
            if (phoneImei == null || phoneImei.length() < 1) {
                TelephonyManager tm = (TelephonyManager) DatabaseHandler.databaseContext.getSystemService(Context.TELEPHONY_SERVICE);
                phoneImei = tm.getDeviceId();
            }
            return phoneImei;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void setPhoneImei(String thePhoneImei) {
        phoneImei = thePhoneImei;
    }

    public static String getMonthNameAbbrev(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "Jan";

        }
    }

    public static Date getDateFromString(String date, String format) {
        try {
            SimpleDateFormat ft = new SimpleDateFormat(format);
            Date dt = ft.parse(date);
            return dt;
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }

        //Parameterized constructor :- Date(int year,int month,int day) creates a Date object and initialize it with given year+1900,  given month and given day.
    }

    public static Date getDateFromString(String date) {
        return getDateFromString(date, "yyyy-MM-dd");
    }

    public static Date getDateFromSqlite(String date) {
        return getDateFromString(date, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String formatDate(Date date, String format) {
        try {
            SimpleDateFormat ft = new SimpleDateFormat(format);
            String dateString = ft.format(date);
            return dateString;
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDate(Date date) {
        return formatDate(date, DATE_FIELD_FORMAT);
    }

    public static String formatDateToSqlite(Date date) {
        //Formats the Date into format expected by SQLite Database
        return formatDate(date, "yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String formatRealNumber(double number) {
        try {
            NumberFormat nf = new DecimalFormat(REAL_FIELD_FORMAT);
            return nf.format(number);
        } catch (Exception ex) {
            return null;
        }
    }


    //Given a GSW stage, returns the Activity class to launch
    public static Class resolveGettingStartedWizardStage(int stage) {
        switch (stage) {
            case GETTING_STARTED_PAGE_NEW_CYCLE:
                return GettingStartedWizardNewCycleActivity.class;
            case GETTING_STARTED_PAGE_ADD_MEMBER:
                return GettingStartedWizardAddMemberActivity.class;
            case GETTING_STARTED_PAGE_REVIEW_MEMBERS:
                return GettingStartedWizardReviewMembersActivity.class;
            case GETTING_STARTED_PAGE_REVIEW_CYCLE:
                return GettingStartedWizardNewCycleActivity.class;
            case GETTING_STARTED_PAGE_ONE:
                return GettingStartedWizardPageOne.class;
            case GETTING_STARTED_PAGE_PIN:
                return GettingStartedWizardPageTwo.class;
            case GETTING_STARTED_PAGE_CONFIRMATION:
                return GettingStartedConfirmationPage.class;
            default:
                return GettingStartedWizardPageOne.class;
        }
    }

    public static String formatLongNumber(long number) {
        try {
            NumberFormat nf = new DecimalFormat(INTEGER_FIELD_FORMAT);
            return nf.format(number);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String formatNumber(double number) {
        return String.format("%,.0f", number);
    }

    /**
     * Creates an alert dialog without buttons
     *
     * @param context
     * @param message
     * @param icon
     * @return
     */
    public static AlertDialog createAlertDialog(Context context, String message, String icon) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle("End Cycle");

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (icon.equalsIgnoreCase(MSGBOX_ICON_EXCLAMATION)) {
            //alertDialog.setIcon(R.drawable.exclamation);
        } else if (icon.equalsIgnoreCase(MSGBOX_ICON_TICK)) {
            //alertDialog.setIcon(R.drawable.tick);
        } else if (icon.equalsIgnoreCase(MSGBOX_ICON_QUESTION)) {
            //alertDialog.setIcon(R.drawable.question);
        }

        return alertDialog;

    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected());
    }

    /**
     * Creates an alert dialog with an OK button
     * Will later see how to do a real Dialog Box with standard buttons
     *
     * @param context
     * @param title
     * @param message
     * @param icon
     * @return
     */
    public static AlertDialog createAlertDialogOk(Context context, String title, String message, String icon) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (icon.equalsIgnoreCase(MSGBOX_ICON_EXCLAMATION)) {
            //alertDialog.setIcon(R.drawable.exclamation);
        } else if (icon.equalsIgnoreCase(MSGBOX_ICON_TICK)) {
            //alertDialog.setIcon(R.drawable.tick);
        } else if (icon.equalsIgnoreCase(MSGBOX_ICON_QUESTION)) {
            //alertDialog.setIcon(R.drawable.question);
        }

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return alertDialog;
    }


    /**
     * This method sets the selected option of a spinner from a given value *
     */
    public static void setSpinnerSelection(String value, Spinner spinner) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int spinnerPosition = adapter.getPosition(value);

        spinner.setSelection(spinnerPosition);
    }


    /**
     * This method is a hack helps align a ListView within a scrolling screen
     * because a Listview cannot be placed inside a scroll view
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public double getTotalCashInBox(int meetingId, Context context) {
        double totalSavings = 0.0;
        double totalLoansRepaid = 0.0;
        double totalLoansIssued = 0.0;
        double totalFines = 0.0;

        double loanTopUps = 0.0;
        double actualStartingCash = 0.0;

        double cashToBank = 0.0;

        Meeting currentMeeting = null;

        MeetingStartingCash startingCashDetails = null;

        MeetingRepo meetingRepo = new MeetingRepo(context);
        MeetingSavingRepo savingRepo = new MeetingSavingRepo(context);
        MeetingLoanRepaymentRepo repaymentRepo = new MeetingLoanRepaymentRepo(context);
        MeetingLoanIssuedRepo loanIssuedRepo = new MeetingLoanIssuedRepo(context);
        MeetingFineRepo fineRepo = new MeetingFineRepo(context);

        try {


            startingCashDetails = meetingRepo.getMeetingStartingCash(meetingId);

            totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            totalFines = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);

            loanTopUps = startingCashDetails.getLoanTopUps();
            actualStartingCash = startingCashDetails.getActualStartingCash();
            cashToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(currentMeeting.getMeetingId());

            return actualStartingCash + totalSavings + totalLoansRepaid - totalLoansIssued + totalFines + loanTopUps - cashToBank;

        } catch (Exception ex) {
            return 0.0;
        } finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
            return 0.0;
        }
    }


}
