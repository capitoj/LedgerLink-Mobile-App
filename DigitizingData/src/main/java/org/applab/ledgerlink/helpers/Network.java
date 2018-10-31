package org.applab.ledgerlink.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.repo.VslaInfoRepo;
import org.applab.ledgerlink.utils.Connection;
import org.json.JSONStringer;

import java.io.IOException;

/**
 * Created by Joseph Capito on 1/14/2016.
 */
public class Network {
    protected Context context;
    protected String phoneImei;
    protected String networkType;
    protected String networkOperator;
    protected static int httpStatusCode = 0;

    public Network(Context context){
        this.context = context;
    }

    public static boolean isConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return connected;
    }

    public String getPhoneImei() {
        try {
            if(phoneImei == null || phoneImei.length()<1){
                TelephonyManager tm = (TelephonyManager)this.context.getSystemService(Context.TELEPHONY_SERVICE);
                phoneImei = tm.getDeviceId();
            }
            return phoneImei;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public void setNetworkType(String networkType){
        this.networkType = networkType;
    }

    public String getNetworkType(){
        return this.networkType;
    }

    public String getOperator() {
        try {
            if(networkOperator == null || networkOperator.length()<1){
                TelephonyManager tm = (TelephonyManager)this.context.getSystemService(Context.TELEPHONY_SERVICE);
                if(tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                    networkOperator = tm.getNetworkOperatorName();
                    if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE){
                        this.setNetworkType("EDGE");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS){
                        this.setNetworkType("GPRS");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA){
                        this.setNetworkType("HSDPA");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA){
                        this.setNetworkType("HSPA");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP){
                        this.setNetworkType("HSPAP");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA){
                        this.setNetworkType("HSUPA");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                        this.setNetworkType("UMTS");
                    }
                    else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                        this.setNetworkType("LTE");
                    }
                    else {
                        this.setNetworkType("UNKNOWN");
                    }
                }
            }
            return networkOperator;
        }
        catch(Exception ex) {
            return null;
        }
    }

    public static JSONStringer getNetworkHeader(Context context, JSONStringer js){
        Network network = new Network(context);
        VslaInfo vslaInfo = new VslaInfoRepo(context).getVslaInfo();
        try{
            js.key("NetworkHeader")
            .object()
            .key("VslaCode").value(vslaInfo.getVslaCode())
            .key("PassKey").value(vslaInfo.getPassKey())
            .key("PhoneImei").value(network.getPhoneImei())
            .key("NetworkOperator").value(network.getOperator())
            .key("NetworkType").value(network.getNetworkType())
            .endObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return js;
    }

    public static String submitData(Context context, String uri, String jsonString){
        String result = null;
        try{
            if(Connection.isNetworkConnected(context)){
                final DefaultHttpClient httpClient = new DefaultHttpClient();
                final HttpPost httpPost = new HttpPost(uri);
                StringEntity se = new StringEntity(jsonString);
                httpPost.setEntity(se);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                ResponseHandler<String> rh = new ResponseHandler<String>() {
                    @Override
                    public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        httpStatusCode = httpResponse.getStatusLine().getStatusCode();
                        StringBuffer out = new StringBuffer();
                        byte[] b = EntityUtils.toByteArray(httpEntity);
                        out.append(new String(b, 0, b.length));
                        return out.toString();
                    }
                };
                String response = httpClient.execute(httpPost, rh);
                httpClient.getConnectionManager().shutdown();
                if (httpStatusCode == 200) {
                    result = response;
                }
            }else{
                Log.e("Network", "No Internet Connection Detected");
            }
        }catch (Exception e){
            Log.e("Network", e.getMessage());
        }
        return result;
    }
}
