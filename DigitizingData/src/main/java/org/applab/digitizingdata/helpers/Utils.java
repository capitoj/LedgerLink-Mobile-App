package org.applab.digitizingdata.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Date;
import org.applab.digitizingdata.R;

/**
 * Created by Moses on 7/3/13.
 */
public class Utils {
    /*
    ** DATE UTILS
     */
    public static final String DATE_FIELD_FORMAT = "dd-MMM-yyyy";
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

    //SERVER Connection
    public static final String VSLA_SERVER_BASE_URL = "http://74.208.213.214:9905/DigitizingDataRestfulService.svc";

    //Used when sending Data
    private static String phoneImei;

    //TODO: will create an enum of CURRENT_VIEW_MODE
    public enum MeetingDataViewMode {
        VIEW_MODE_CAPTURE,
        VIEW_MODE_REVIEW,
        VIEW_MODE_READ_ONLY
    };

    public static MeetingDataViewMode  _meetingDataViewMode = MeetingDataViewMode.VIEW_MODE_CAPTURE;

    public enum MeetingActiveActionBarMenu {
        MENU_NONE,
        MENU_REVIEW_SEND,
        MENU_CASH_BOOK_TAB,
        MENU_START_CASH_TAB,
        MENU_SEND_DATA_TAB
    };

    public static MeetingActiveActionBarMenu  _meetingActiveActionBarMenu = MeetingActiveActionBarMenu.MENU_NONE;

    public static String getPhoneImei() {
        try {
            if(phoneImei == null || phoneImei.length()<1){
                TelephonyManager tm = (TelephonyManager)DatabaseHandler.databaseContext.getSystemService(Context.TELEPHONY_SERVICE);
                phoneImei = tm.getDeviceId();
            }
            return phoneImei;
        }
        catch(Exception ex) {
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
            SimpleDateFormat ft = new SimpleDateFormat (format);
            Date dt = ft.parse(date);
            return dt;
        }
        catch(Exception e) {
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
            SimpleDateFormat ft = new SimpleDateFormat (format);
            String dateString = ft.format(date);
            return dateString;
        }
        catch(Exception e) {
            return null;
        }
    }

    public static String formatDate(Date date) {
        return formatDate(date, DATE_FIELD_FORMAT);
    }

    public static String formatDateToSqlite(Date date) {
        //Formats the Date into format expected by SQLite Database
        return formatDate(date,"yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String formatRealNumber(double number) {
        try {
            NumberFormat nf = new DecimalFormat(REAL_FIELD_FORMAT);
            return nf.format(number);
        }
        catch(Exception ex){
            return null;
        }
    }

    public static String formatLongNumber(long number) {
        try {
            NumberFormat nf = new DecimalFormat(INTEGER_FIELD_FORMAT);
            return nf.format(number);
        }
        catch(Exception ex){
            return null;
        }
    }

    /**
     * Creates an alert dialog without buttons
     * @param context
     * @param title
     * @param message
     * @param icon
     * @return
     */
    public static AlertDialog createAlertDialog(Context context, String title, String message, String icon) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        if (icon.equalsIgnoreCase(MSGBOX_ICON_EXCLAMATION)) {
            //alertDialog.setIcon(R.drawable.exclamation);
        }
        else if (icon.equalsIgnoreCase(MSGBOX_ICON_TICK)) {
            //alertDialog.setIcon(R.drawable.tick);
        }
        else if (icon.equalsIgnoreCase(MSGBOX_ICON_QUESTION)) {
            //alertDialog.setIcon(R.drawable.question);
        }

        return alertDialog;

    }

    /**
     * Creates an alert dialog with an OK button
     * Will later see how to do a real Dialog Box with standard buttons
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
        }
        else if (icon.equalsIgnoreCase(MSGBOX_ICON_TICK)) {
            //alertDialog.setIcon(R.drawable.tick);
        }
        else if (icon.equalsIgnoreCase(MSGBOX_ICON_QUESTION)) {
            //alertDialog.setIcon(R.drawable.question);
        }

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return alertDialog;
    }

    /**
     * This method is a hack helps align a ListView within a scrolling screen
     * because a Listview cannot be placed inside a scroll view
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
}
