package org.applab.ledgerlink.repo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.applab.ledgerlink.helpers.DatabaseHandler;

/**
 * Created by John Maq on 1/15/2015.
 * Shared class by all Repo helper classes.
 * All repos will inherit from this class
 */
public class CommonRepo {
    private SQLiteDatabase db;

    protected Context context;


    public CommonRepo(Context context) {
        this.context = context;
    }

    public SQLiteDatabase getDb() {
        if(db == null)
        {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
        }
        else {
            if(!db.isOpen())
            {
                db = DatabaseHandler.getInstance(context).getWritableDatabase();
            }
        }
        return db;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }
}
