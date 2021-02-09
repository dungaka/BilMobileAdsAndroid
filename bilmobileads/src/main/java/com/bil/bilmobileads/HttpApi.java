package com.bil.bilmobileads;

import android.os.AsyncTask;

import com.bil.bilmobileads.entity.DataResponse;
import com.bil.bilmobileads.interfaces.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

class HttpApi<T> extends AsyncTask<Object, Void, DataResponse<T>> {

    private final ResultCallback resultCallback;
    private final String api;

    public HttpApi(String api, ResultCallback resultCalback) {
        this.resultCallback = resultCalback;
        this.api = api;
    }

    @Override
    protected DataResponse<T> doInBackground(Object... object) {
        try {
            URL url = new URL(Constants.URL_PREFIX + this.api);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setRequestProperty("Content-Type", "application/json");
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setConnectTimeout(Constants.TIME_REQUEST_ADUNIT);
            httpCon.connect();

            int httpResult = httpCon.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                StringBuilder builder = new StringBuilder();
                InputStream is = httpCon.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                httpCon.disconnect();

                String result = builder.toString();
                JSONObject response = new JSONObject(result);

                return new DataResponse(response);
            } else if (httpResult >= HttpURLConnection.HTTP_BAD_REQUEST) {
                StringBuilder builder = new StringBuilder();
                InputStream is = httpCon.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                httpCon.disconnect();

                String result = builder.toString();
                JSONObject response = new JSONObject(result);

                return new DataResponse(response);
            }
        } catch (MalformedURLException e) {
            return new DataResponse(e);
        } catch (UnsupportedEncodingException e) {
            return new DataResponse(e);
        } catch (ProtocolException e) {
            return new DataResponse(e);
        } catch (IOException e) {
            return new DataResponse(e);
        } catch (JSONException e) {
            return new DataResponse(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(DataResponse jsonObject) {
        super.onPostExecute(jsonObject);

        if (jsonObject.error != null) {
            this.resultCallback.failure(jsonObject.error);
        } else {
            this.resultCallback.success(jsonObject.data);
        }
    }
}