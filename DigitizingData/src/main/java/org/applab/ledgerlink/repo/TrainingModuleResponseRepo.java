package org.applab.ledgerlink.repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.applab.ledgerlink.domain.model.TrainingModuleResponse;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by JCapito on 1/11/2016.
 */
public class TrainingModuleResponseRepo {
    protected Context context;

    public TrainingModuleResponseRepo(Context context){
        this.context = context;
    }

    public void save(int moduleID, String training, String comment, String hashKey){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "insert into TrainingModuleResponse (ModuleId, Training, Comment, TrainingDate, HashKey) values (?, ?, ?, ?, ?)";
        db.execSQL(sql, new String[]{String.valueOf(moduleID), training, comment, Utils.formatDateToSqlite(new Date()), hashKey});
        db.close();
    }

    public List<TrainingModuleResponse> getModuleResponses(){
        List<TrainingModuleResponse> itemList = new ArrayList<TrainingModuleResponse>();
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "select tmr._id, tmr.ModuleId, tm.Module, tmr.Training, tmr.Comment, tmr.TrainingDate, tmr.HashKey from TrainingModuleResponse tmr inner join TrainingModule tm on tm._id = tmr.ModuleId";
        Cursor cursor = db.rawQuery(sql, null);
        while(cursor.moveToNext()){
            TrainingModuleResponse trainingModuleResponse = new TrainingModuleResponse();
            trainingModuleResponse.setID(cursor.getInt(0));
            trainingModuleResponse.setModuleId(cursor.getInt(1));
            trainingModuleResponse.setModule(cursor.getString(2));
            trainingModuleResponse.setTraining(cursor.getString(3));
            trainingModuleResponse.setComment(cursor.getString(4));
            trainingModuleResponse.setDate(Utils.getDateFromSqlite(cursor.getString(5)));
            trainingModuleResponse.setHashKey(cursor.getString(6));
            itemList.add(trainingModuleResponse);
        }
        cursor.close();
        db.close();
        return itemList;
    }

    public void delete(){
        SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
        String sql = "delete from TrainingModuleResponse";
        db.execSQL(sql);
    }
}
