package org.applab.ledgerlink.repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.applab.ledgerlink.domain.model.TrainingModule;
import org.applab.ledgerlink.domain.schema.TrainingModuleSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JCapito on 1/7/2016.
 */
public class TrainingModuleRepo {
    protected Context context;
    public TrainingModuleRepo(Context context){
        this.context = context;
    }

    public List<TrainingModule> getModules(){
        List<TrainingModule> itemList = new ArrayList<TrainingModule>();
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sqlQuery = "select _id, module from TrainingModule";
        Cursor cursor = db.rawQuery(sqlQuery, null);
        while (cursor.moveToNext()){
            TrainingModule trainingModule = new TrainingModule(cursor.getInt(0), cursor.getString(1));
            itemList.add(trainingModule);
        }
        cursor.close();
        db.close();
        return itemList;
    }

    public void delete(){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "delete from TrainingModule";
        db.execSQL(sql);
    }
}
