package org.applab.ledgerlink.domain.schema;

import android.database.sqlite.SQLiteDatabase;

import org.applab.ledgerlink.repo.TrainingModuleRepo;

/**
 * Created by JCapito on 1/7/2016.
 */
public class TrainingModuleSchema {

    public static final String TBL_TRAINING_MODULE = "TrainingModule";
    public static String getCreateTableScript(){
        return "CREATE TABLE IF NOT EXISTS TrainingModule(_id INTEGER PRIMARY KEY AUTOINCREMENT, module TEXT)";
    }

    public static String getTableName(){
        return TBL_TRAINING_MODULE;
    }
}
