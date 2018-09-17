package org.applab.ledgerlink.helpers;

import android.content.Context;

import org.applab.ledgerlink.domain.model.TrainingModuleResponse;
import org.applab.ledgerlink.repo.TrainingModuleResponseRepo;
import org.json.JSONStringer;

import java.util.List;

/**
 * Created by JCapito on 1/14/2016.
 */
public class TrainingModuleFactory {
    protected Context context;

    public TrainingModuleFactory(Context context){
        this.context = context;
    }

    protected JSONStringer getTrainingModules(JSONStringer js){
        TrainingModuleResponseRepo trainingModuleResponseRepo = new TrainingModuleResponseRepo(context);
        List<TrainingModuleResponse> itemList = trainingModuleResponseRepo.getModuleResponses();
        if(itemList.size() > 0){
            try{
                js.key("TrainingModuleSubmission").array();
                for(TrainingModuleResponse trainingModuleResponse : itemList){
                    js.object()
                            .key("ID").value(trainingModuleResponse.getID())
                            .key("ModuleId").value(trainingModuleResponse.getModuleId())
                            .key("Module").value(trainingModuleResponse.getModule())
                            .key("Training").value(trainingModuleResponse.getTraining())
                            .key("Comment").value(trainingModuleResponse.getComment())
                            .key("Date").value(Utils.formatDateToSqlite(trainingModuleResponse.getDate()))
                            .key("HashKey").value(trainingModuleResponse.getHashKey())
                            .endObject();
                }
                js.endArray();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return js;
    }

    public static JSONStringer getJSONOutput(Context context, JSONStringer js){
        TrainingModuleFactory factory = new TrainingModuleFactory(context);
        return factory.getTrainingModules(js);
    }
}
