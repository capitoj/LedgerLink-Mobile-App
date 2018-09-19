package org.applab.ledgerlink.repo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.ledgerlink.domain.model.FinancialInstitution;
import org.applab.ledgerlink.domain.schema.FinancialInstitutionSchema;
import org.applab.ledgerlink.helpers.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JCapito on 9/17/2018.
 */

public class FinancialInstitutionRepo {

    private Context context;
    private Boolean isLoaded;
    private int fiID;
    private FinancialInstitution financialInstitution;

    public FinancialInstitutionRepo(Context context, int fiID){
        this.context = context;
        this.fiID = fiID;
        this.isLoaded = false;
        this.financialInstitution = new FinancialInstitution();
        this.load();
    }

    public FinancialInstitutionRepo(Context context){
        this.context = context;
    }

    private void load(){
        if(!this.isLoaded){
            try{
                SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
                String sql = String.format("select * from %s where FIID = ?", FinancialInstitutionSchema.getTableName());
                Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(this.fiID)});
                if (cursor.getCount() > 0){
                    cursor.moveToNext();
                    this.isLoaded = true;
                    this.financialInstitution.setFiID(cursor.getInt(cursor.getColumnIndex(FinancialInstitutionSchema.COL_FI_FIID)));
                    this.financialInstitution.setName(cursor.getString(cursor.getColumnIndex(FinancialInstitutionSchema.COL_FI_NAME)));
                    this.financialInstitution.setCode(cursor.getString(cursor.getColumnIndex(FinancialInstitutionSchema.COL_FI_CODE)));
                    this.financialInstitution.setIpAddress(cursor.getString(cursor.getColumnIndex(FinancialInstitutionSchema.COL_FI_IP_ADDRESS)));
                }
                cursor.close();
                db.close();
            }catch(Exception e){
                Log.e("FinancialInstitution", e.getMessage());
            }
        }
    }

    public FinancialInstitution getFinancialInstitution(){
        return this.financialInstitution;
    }

    protected int getCountOfRecords(){
        int recordCount = 0;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("select count(*) as RecordCount from %s", FinancialInstitutionSchema.getTableName());
            Cursor cursor = db.rawQuery(sql, null);
            if(cursor.getCount() > 0){
                cursor.moveToNext();
                recordCount = cursor.getInt(cursor.getColumnIndex("RecordCount"));
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            Log.e("FinancialInstitution", e.getMessage());
        }
        return recordCount;
    }

    protected int getIDAtIndex(Integer index){
        int ID = 0;
        try{
            SQLiteDatabase db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sql = String.format("Select FIID from %s order by Name limit %d, 1", FinancialInstitutionSchema.getTableName(), index);
            Cursor cursor = db.rawQuery(sql, null);
            if(cursor.getCount() > 0){
                cursor.moveToNext();
                ID = cursor.getInt(cursor.getColumnIndex(FinancialInstitutionSchema.COL_FI_FIID));
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            Log.e("FinancialInstitution", e.getMessage());
        }
        return ID;
    }

    public static List<FinancialInstitution> getFinancialInstitutions(Context context){
        int recordCount = new FinancialInstitutionRepo(context).getCountOfRecords();
        List<FinancialInstitution> financialInstitutions = new ArrayList<FinancialInstitution>(recordCount);
        for(int i = 0; i < recordCount; i++){
            int FIID = new FinancialInstitutionRepo(context).getIDAtIndex(i);
            if(FIID > 0){
                FinancialInstitution financialInstitution = new FinancialInstitutionRepo(context, FIID).getFinancialInstitution();
                financialInstitutions.add(financialInstitution);
            }
        }
        return financialInstitutions;
    }
}
