package org.applab.ledgerlink.helpers;

/**
 * Created by Moses on 9/27/13.
 */
public class AsyncHttpPostInputParam {
    private String httpUrl;
    private String jsonData;

    public AsyncHttpPostInputParam(String httpUrl, String jsonData) {
        this.httpUrl = httpUrl;
        this.jsonData = jsonData;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
