package org.applab.digitizingdata.repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.domain.model.FineType;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.schema.FineTypeSchema;
import org.applab.digitizingdata.domain.schema.MemberSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Moses on 4/2/14.
 */
public class FineTypeRepo {

    static ArrayList<FineType> fineTypes;
    private Context context;

    public FineTypeRepo() {
    }

    public FineTypeRepo(Context context) {
        this.context = context;
    }


    public ArrayList<FineType> getAllFineTypes() {

        ArrayList<FineType> fineTypes = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            fineTypes = new ArrayList<FineType>();
            String columnList = FineTypeSchema.getColumnList();

            // Select All Query
            String selectQuery = String.format("SELECT %s FROM %s ORDER BY %s", columnList, FineTypeSchema.getTableName(),
                    MemberSchema.COL_M_MEMBER_NO);

            cursor = db.rawQuery(selectQuery, null);

            if(cursor == null){
                return new ArrayList<FineType>();
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    FineType fineType = new FineType();
                    fineType.setFineTypeId(cursor.getInt(cursor.getColumnIndex(FineTypeSchema.COL_FT_FINE_TYPE_ID)));
                    fineType.setFineTypeName(cursor.getString(cursor.getColumnIndex(FineTypeSchema.COL_FT_FINE_TYPE_NAME)));
                    fineType.setFineTypeDesc(cursor.getString(cursor.getColumnIndex(FineTypeSchema.COL_FT_FINE_TYPE_DESC)));

                    fineTypes.add(fineType);

                } while (cursor.moveToNext());
            }

            // return the list
            return fineTypes;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Log.e("FineTypeRepo.getAllFineTypes", ex.getMessage());
            return new ArrayList<FineType>();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }
}