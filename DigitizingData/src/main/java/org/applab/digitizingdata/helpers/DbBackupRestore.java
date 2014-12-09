package org.applab.digitizingdata.helpers;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

public class DbBackupRestore {

    private static final String TAG = DbBackupRestore.class.getName();
    public static final String PACKAGE_NAME = "org.applab.digitizingdata";
    public static final String DATABASE_NAME = DatabaseHandler.DATABASE_NAME;
    private static final String DATABASE_TABLE = "entryTable";
    private static final String DATA_FOLDER = "LedgerLink";
    private static final String EXTERNAL_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DATA_FOLDER;

    /**
     * Directory that files are to be read from and written to *
     */
    private static final File DATABASE_DIRECTORY = new File(EXTERNAL_STORAGE_LOCATION, "ledgerlinkdb");

    /**
     * Contains: /data/data/com.example.app/databases/example.db *
     */
    private static final File DATA_DIRECTORY_DATABASE = new File(Environment.getExternalStorageDirectory(), "LedgerLinkBackup");
    /** new File(Environment.getDataDirectory() +
     "/data/" + PACKAGE_NAME +
     "/databases/" + DATABASE_NAME); */

    /**
     * Saves the application database to the
     * export directory under MyDb.db *
     */
    public static boolean exportDb() {
        if (!SdIsPresent()){
            Log.d(TAG, EXTERNAL_STORAGE_LOCATION);
            return false;
        }



        File dbFile = DATABASE_DIRECTORY;


        File exportDir = DATA_DIRECTORY_DATABASE;
        String filename = "ledgerlinkbackup.db";
        File file = new File(exportDir, filename);

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        try {
        /**    exportDir.createNewFile();
            copyFile(dbFile, exportDir); */
            file.createNewFile();
            copyFile(dbFile, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean importDb() {
        try {

            File dbFile = DATA_DIRECTORY_DATABASE;

            String filename = "ledgerlinkbackup.db";
            File file = new File(dbFile, filename);

            File importDir = DATABASE_DIRECTORY;


            if (!importDir.exists()) {
                importDir.mkdirs();
            }

            importDir.createNewFile();
            copyFile(file, importDir);

             return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Given an SQLite database file, this checks if the file
     * is a valid SQLite database and that it contains all the
     * columns represented by DbAdapter.ALL_COLUMN_KEYS *
     */
    public static boolean checkDbIsValid(File db) {
        try {
            SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase
                    (db.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            Cursor cursor = sqlDb.query(true, DATABASE_TABLE,
                    null, null, null, null, null, null, null
            );

            sqlDb.close();
            cursor.close();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Database valid but not the right type");
            e.printStackTrace();
            return false;
        } catch (SQLiteException e) {
            Log.d(TAG, "Database file is invalid.");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.d(TAG, "checkDbIsValid encountered an exception");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Returns whether an SD card is present and writable *
     */
    private static boolean SdIsPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}

