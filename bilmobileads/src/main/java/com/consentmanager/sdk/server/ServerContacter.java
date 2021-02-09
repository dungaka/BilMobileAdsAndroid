package com.consentmanager.sdk.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
//import android.util.Log;

//import androidx.ads.identifier.AdvertisingIdClient;
//import androidx.ads.identifier.AdvertisingIdInfo;

import com.consentmanager.sdk.exceptions.CMPConsentToolNetworkException;
import com.consentmanager.sdk.exceptions.CMPConsentToolSettingsException;
import com.consentmanager.sdk.model.CMPConfig;
import com.consentmanager.sdk.storage.CMPPrivateStorage;
//import com.consentmanager.sdk.storage.CMPStorageConsentManager;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.gson.Gson;

import org.json.JSONObject;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.URL;
//import java.util.Date;


public class ServerContacter {
    public static ServerResponse getResponse(CMPConfig config, final Context context) {
        return proceedRequest(config, context);
    }

    public static ServerResponse getAndSaveResponse(CMPConfig config, final Context context) throws CMPConsentToolNetworkException {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected) {
            ServerResponse response = proceedRequest(config, context);
            CMPPrivateStorage.setConsentToolURL(context, response.getUrl());
            //  CMPPrivateStorage.setLastRequested(context, new Date());
            return response;
        } else {
            throw new CMPConsentToolNetworkException("Network is not available");
        }
    }

    private static ServerResponse proceedRequest(CMPConfig config, final Context context) {
        //                String consent = CMPStorageConsentManager.getConsentString(context);
        //                String idfa = ""; // getIdfaString(context);
        //                final String url_string = new ServerRequest(config, consent, idfa).getAsURL();
        //                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //                StrictMode.setThreadPolicy(policy);

        try {
            //                        URL url = new URL(url_string);
            //                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            //
            //                        String inputLine = in.readLine();
            //                        in.close();
            //                        StrictMode.enableDefaults();

            // My CMP
            //  String inputLine = "{\"message\":\"\",\"status\":1,\"regulation\":1,\"url\":\"http://static.vliplatform.com/plugins/appCMP/#cmpscreen\"}";
            //  String inputLine = "{\"message\":\"\",\"status\":1,\"regulation\":1,\"url\":\"http://10.0.2.2:8000\"}";
            String inputLine = "{\"message\":\"\",\"status\":1,\"regulation\":1,\"url\":\"https://static.vliplatform.com/plugins/appCMP/v2/\"}";
            JSONObject response = new JSONObject(inputLine);
            ServerResponse serverResponse = new ServerResponse();
            serverResponse.setStatus(response.getInt("status"));
            serverResponse.setRegulation(response.getInt("regulation"));
            serverResponse.setMessage(response.getString("message"));
            serverResponse.setUrl(response.getString("url"));
            return serverResponse;

            //  Gson g = new Gson();
            //  return g.fromJson(inputLine, ServerResponse.class);
        } catch (Exception e) {
            StrictMode.enableDefaults();
            throw new CMPConsentToolSettingsException(e.getMessage());
        }

    }

    private static String getIdfaString(Context context) {
//        if (AdvertisingIdClient.isAdvertisingIdProviderAvailable(context)) {
//            ListenableFuture<AdvertisingIdInfo> advertisingIdInfoListenableFuture =
//                    AdvertisingIdClient.getAdvertisingIdInfo(context);
//            try {
//                Log.d("HNL", advertisingIdInfoListenableFuture.get().getId());
//                return advertisingIdInfoListenableFuture.get().getId();
//            } catch (Exception e) {
//                return "";
//            }
//        }
        return "";
    }


}
