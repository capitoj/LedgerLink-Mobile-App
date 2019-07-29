package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Document;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by JCapito on 6/17/2019.
 */

public class LanguageHelper {

    private String language;
    private Context context;
    public LanguageHelper(Context context, String language){
        this.context = context;
        this.language = language;
    }

    protected Document __getXmlDocument(){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
        }catch (ParserConfigurationException e){
            Log.e("GetTranslation", e.getMessage());
        }
        if(builder != null){
            String fileName = "/res/xml/english.xml";
            if(this.language.equals("SWAHILI")){
                fileName = "res/xml/swahili.xml";
            }else if(this.language.equals("KUKU")){
                fileName = "res/xml/kuku.xml";
            }
            File file = new File(fileName);
            try{
                document = builder.parse(file);
            }catch (Exception e){
                Log.e("GetTranslation", e.getMessage());
            }
            if(document != null){
                document.getDocumentElement().normalize();
            }
        }
        return document;
    }

    public static void getXmlDocument(Context context, String language){
        LanguageHelper languageHelper = new LanguageHelper(context, language);
        Document document = languageHelper.__getXmlDocument();
        Log.e("RootElementX", document.getDocumentElement().getNodeName());
    }
}
