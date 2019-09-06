package org.applab.ledgerlink.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.applab.ledgerlink.R;

/**
 * Created by Joseph Capito on 7/31/2015.
 */
public class DialogMessageBox implements DialogInterface.OnClickListener {

    protected AlertDialog.Builder builder;
    protected AlertDialog alertDialog;
    protected Runnable runBtnPositive;

    public DialogMessageBox(Context context, String title, String message){
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", this);

        alertDialog = builder.create();
    }

    public DialogMessageBox(Context context, String title, String message, Runnable runBtnPositive){
        this.runBtnPositive = runBtnPositive;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(context.getResources().getString(R.string.cancel_dialog), this);
        builder.setPositiveButton(context.getResources().getString(R.string.continue_dialog), this);

        alertDialog = builder.create();
    }

    public DialogMessageBox(Context context, String title, String message, Runnable runBtnPositive, boolean hideCancel){
        this.runBtnPositive = runBtnPositive;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        if(!hideCancel)
            builder.setNegativeButton(context.getResources().getString(R.string.cancel_dialog), this);

        builder.setPositiveButton(context.getResources().getString(R.string.continue_dialog), this);

        alertDialog = builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which){
        switch (which){
            case DialogInterface.BUTTON_NEGATIVE : this.setNegativeButton(); break;
            case DialogInterface.BUTTON_POSITIVE : this.setPositiveButton(); break;
        }
    }

    protected void setPositiveButton(){
        if(this.runBtnPositive != null)
            runBtnPositive.run();
    }

    protected void setNegativeButton(){

    }

    protected AlertDialog getAlertDialog(){
        return this.alertDialog;
    }

    public static void show(Context context, String title, String message){
        DialogMessageBox dialogMessageBox = new DialogMessageBox(context, title, message);
        AlertDialog alertDialog = dialogMessageBox.getAlertDialog();
        alertDialog.show();
    }

    public static void show(Context context, String title, String message, Runnable btnPositive) {
        DialogMessageBox dialogMessageBox = new DialogMessageBox(context, title, message, btnPositive);
        AlertDialog alertDialog = dialogMessageBox.getAlertDialog();
        alertDialog.show();
    }

    public static void show(Context context, String title, String message, Runnable btnPositive, boolean hideCancel) {
        DialogMessageBox dialogMessageBox = new DialogMessageBox(context, title, message, btnPositive, hideCancel);
        AlertDialog alertDialog = dialogMessageBox.getAlertDialog();
        alertDialog.show();
    }
}
