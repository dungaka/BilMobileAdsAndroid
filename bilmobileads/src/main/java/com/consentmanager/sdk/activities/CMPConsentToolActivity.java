package com.consentmanager.sdk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.consentmanager.sdk.CMPConsentTool;
import com.consentmanager.sdk.callbacks.OnCloseCallback;
import com.consentmanager.sdk.callbacks.OnNetworkExceptionCallback;
import com.consentmanager.sdk.exceptions.CMPConsentToolNetworkException;
import com.consentmanager.sdk.exceptions.CMPConsentToolSettingsException;
import com.consentmanager.sdk.factorys.ModalLayout;
import com.consentmanager.sdk.model.CMPConfig;
import com.consentmanager.sdk.model.CMPSettings;
import com.consentmanager.sdk.model.ConsentStringDecoderV1;
import com.consentmanager.sdk.model.ConsentStringDecoderV2;
import com.consentmanager.sdk.factorys.WebViewCreator;
import com.consentmanager.sdk.server.ServerContacter;
import com.consentmanager.sdk.server.ServerResponse;
import com.consentmanager.sdk.storage.CMPPrivateStorage;
import com.consentmanager.sdk.storage.CMPStorageConsentManager;
import com.consentmanager.sdk.storage.CMPStorageV1;

import java.util.Calendar;
import java.util.Date;

public class CMPConsentToolActivity extends Activity {
    private static final String CMP_SETTINGS_EXTRA = "cmp_settings";

    private static OnCloseCallback onCloseCallback;
    private static OnNetworkExceptionCallback onNetworkCallback;

    private CMPSettings cmpSettings;

    /**
     * Used to start the Activity where the consentToolUrl is loaded into WebView
     *
     * @param cmpSettings   setup the needed data to initialize consent tool api.
     *                      In case no prior consent can be found, it executes the
     *                      procedure of getting consent from the user, which means
     *                      loading the consentToolUrl provided by this object.
     * @param context       context
     * @param closeCallback listener called when CMPConsentToolActivity is closed after interacting with WebView
     */
    public static void openCmpConsentToolView(CMPSettings cmpSettings, Context context, OnCloseCallback closeCallback, OnNetworkExceptionCallback networkCallback) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected) {
            Intent cmpConsentToolIntent = new Intent(context, CMPConsentToolActivity.class);
            cmpConsentToolIntent.putExtra(CMP_SETTINGS_EXTRA, cmpSettings);
            onCloseCallback = closeCallback;
            onNetworkCallback = networkCallback;

            context.startActivity(cmpConsentToolIntent);
        } else {
            if (networkCallback != null) {
                networkCallback.onErrorOccur("The Network is not reachable to show the WebView");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // My CMP: Reject
        String consentStr = CMPStorageConsentManager.getConsentString(CMPConsentTool.getInstance().getContext());
        if (consentStr.isEmpty()) {
            Date now = new Date();

            CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
            //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
            CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
            CMPStorageConsentManager.setConsentString(CMPConsentToolActivity.this, null);

            // Set Time Ask Again After Rejected: 365d
            Calendar add14Day = Calendar.getInstance();
            add14Day.setTime(now);
            add14Day.add(Calendar.DATE, 365);
            CMPPrivateStorage.setLastRequested(CMPConsentToolActivity.this, add14Day.getTime());

            // Close Webview
            if (onCloseCallback != null) {
                onCloseCallback.onWebViewClosed();
                onCloseCallback = null;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cmpSettings = CMPSettings.getInstance(this);

        if (cmpSettings == null) {
            CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
            //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
            CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
            finish();
            return;
        }

        CMPConfig config;
        try {
            config = CMPConfig.getInstance(this);
        } catch (CMPConsentToolSettingsException e) {
            e.printStackTrace();
            CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
            //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
            CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
            finish();
            return;
        }
        if (TextUtils.isEmpty(cmpSettings.getConsentToolUrl())) {
            CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
            //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
            CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
            try {
                ServerResponse response = ServerContacter.getAndSaveResponse(config, this);
                if (response.getUrl() == null) {
                    CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
                    //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
                    CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
                    finish();
                    return;
                }
            } catch (CMPConsentToolNetworkException errorNetworkException) {
                if (onNetworkCallback != null) {
                    onNetworkCallback.onErrorOccur(errorNetworkException.getMessage());
                }
            }
        }
        createLayout();

        handleConsentSettings();

        setupWebViewClient();
    }


    /**
     * Creates a WebView and puts it to a new Layer, without displaying it.
     */
    private void createLayout() {
        // create the Layout with the WebView and add it to the ContentView
        LinearLayout linearLayout = ModalLayout.initialise(this).getLayout();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        WebViewCreator.initialise(this).getWebView().loadUrl(cmpSettings.getConsentToolUrl());

        //remove Parent link, if set.
        if (linearLayout.getParent() != null) {
            ((ViewGroup) linearLayout.getParent()).removeView(linearLayout);
        }

        //Set Layer to Screen.
        setContentView(linearLayout, layoutParams);
    }

    private void handleConsentSettings() {
        Uri uri;

        if (!TextUtils.isEmpty(cmpSettings.getConsentString())) {
            uri = Uri.parse(cmpSettings.getConsentToolUrl()).buildUpon().appendQueryParameter("code64", cmpSettings.getConsentString()).build();
            cmpSettings.setConsentToolUrl(uri.toString());
        } else {
            String consentString = CMPStorageV1.getConsentString(this);

            if (!TextUtils.isEmpty(consentString)) {
                uri = Uri.parse(cmpSettings.getConsentToolUrl()).buildUpon().appendQueryParameter("code64", consentString).build();
                cmpSettings.setConsentToolUrl(uri.toString());
                cmpSettings.setConsentString(consentString);
            }
        }
    }

    private void setupWebViewClient() {
        GDPRWebViewClient gdprWebViewClient = new GDPRWebViewClient();
        WebViewCreator.initialise(this).getWebView().setWebViewClient(gdprWebViewClient);
    }

    private class GDPRWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            handleReceivedConsentString(url);
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            handleReceivedConsentString(String.valueOf(request.getUrl()));
            return true;
        }

        private void handleWebViewInteraction(String url) {
            handleReceivedConsentString(url);
        }

        private void handleReceivedConsentString(String url) {
            System.out.println(url);
            if (url == null) return;

            // My CMP
            String[] values = new String[0];
            if (url.indexOf("consent://") != -1) {
                values = url.split("consent://");
            }
            if (values.length > 0) {
                String consentStr = values[1];

                // Accepted
                CMPStorageConsentManager.setConsentString(CMPConsentToolActivity.this, consentStr);
                proceedConsentString(consentStr);

                // Set Time Ask Again After Accepted
                Date now = new Date();
                Calendar add365Day = Calendar.getInstance();
                add365Day.setTime(now);
                add365Day.add(Calendar.DATE, 365);
                CMPPrivateStorage.setLastRequested(CMPConsentToolActivity.this, add365Day.getTime());

                // Close Webview
                if (onCloseCallback != null) {
                    onCloseCallback.onWebViewClosed();
                    onCloseCallback = null;
                }
                finish();

                //                //encode String Base 64:
                //                String fullString = new String(Base64.decode(values[1], Base64.DEFAULT));
                //                String[] splits = fullString.split("#");
                //                if (splits.length > 3) {
                //                    proceedConsentString(splits[0]);
                //                    proceedConsentManagerValues(splits);
                //                } else {
                //                    CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
                //                    //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
                //                    CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
                //                }
            } else {
                CMPStorageV1.clearConsents(CMPConsentToolActivity.this);
                //  CMPStorageV2.clearConsents(CMPConsentToolActivity.this);
                CMPStorageConsentManager.clearConsents(CMPConsentToolActivity.this);
            }
        }

        private void proceedConsentManagerValues(String[] splits) {
            CMPStorageConsentManager.setPurposesString(CMPConsentToolActivity.this, splits[1]);
            CMPStorageConsentManager.setVendorsString(CMPConsentToolActivity.this, splits[2]);
            CMPStorageConsentManager.setUSPrivacyString(CMPConsentToolActivity.this, splits[3]);
        }

        private void proceedConsentString(String value) {
            CMPStorageV1.setConsentString(CMPConsentToolActivity.this, value);
            if (value.substring(0, 1).equals("B")) {
                ConsentStringDecoderV1 consentStringDecoder = new ConsentStringDecoderV1(CMPConsentToolActivity.this);
                consentStringDecoder.processConsentString(value);
            } else if (value.substring(0, 1).equals("C")) {
                ConsentStringDecoderV2 consentStringDecoder = new ConsentStringDecoderV2(CMPConsentToolActivity.this);
                consentStringDecoder.processConsentString(value);
            }
        }
    }

}