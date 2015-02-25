package org.applab.ledgerlink.helpers;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Moses on 9/27/13.
 */
public class AsyncHttpPostTask extends AsyncTask<AsyncHttpPostInputParam, Void, String> {

    public AsyncHttpPostTask(){
    }

    @Override
    protected void onPreExecute()
    {

    }

    @Override
    protected void onPostExecute(String headerCode)
    {
        super.onPostExecute(headerCode);
        //progressDialog.dismiss();
    }

    @Override
    protected String doInBackground(AsyncHttpPostInputParam... postData)
    {
        String result = "";
        int responseCode = 0;
        try
        {
            //This seems to be a param array
            String request = postData[0].getJsonData();
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(postData[0].getHttpUrl());

            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(request);

            //sets the post request as the resulting string
            httpPost.setEntity(se);
            //sets a request header so the page receving the request
            //will know what to do with it
            //httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            int executeCount = 0;
            HttpResponse response;
            do
            {
                //progressDialog.setMessage("Establishing Communication... ("+(executeCount+1)+"/5)");
                // Execute HTTP Post Request
                executeCount++;
                response = client.execute(httpPost);
                responseCode = response.getStatusLine().getStatusCode();
                // If you want to see the response code, you can Log it
                // out here by calling:
                // Log.d("256 Design", "statusCode: " + responseCode)
            } while (executeCount < 5 && responseCode == 408);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String line;
            while ((line = rd.readLine()) != null)
            {
                result = line.trim();
            }
             int id = Integer.parseInt(result);
        }
        catch (Exception e) {
            responseCode = 408;
            e.printStackTrace();
        }
        return "Test";
    }

}
