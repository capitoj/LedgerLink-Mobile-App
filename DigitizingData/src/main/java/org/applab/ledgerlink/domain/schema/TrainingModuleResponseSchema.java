package org.applab.ledgerlink.domain.schema;

/**
 * Created by JCapito on 1/7/2016.
 */
public class TrainingModuleResponseSchema {

    public static final String TBL_TRAINING_MODULE_RESPONSE = "TrainingModuleResponse";

    public static String getCreateTableScript(){
        return "CREATE TABLE IF NOT EXISTS TrainingModuleResponse(_id INTEGER PRIMARY KEY AUTOINCREMENT, ModuleId INTEGER, Training TEXT, Comment TEXT, TrainingDate TEXT, HashKey TEXT)";
    }

    public static String getDropTableScript(){
        return "DROP TABLE IF EXISTS TrainingModuleResponse";
    }

    public static String getTableName(){
        return TBL_TRAINING_MODULE_RESPONSE;
    }
}
