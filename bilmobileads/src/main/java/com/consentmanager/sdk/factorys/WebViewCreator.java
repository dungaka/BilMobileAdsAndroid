package com.consentmanager.sdk.factorys;

import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;

public class WebViewCreator {
    private WebView webview;
    private Context context;
    private static WebViewCreator instance;

    private WebViewCreator(Context context) {
        this.context = context;
    }

    public static WebViewCreator initialise(Context context) {
        if (instance == null) {
            instance = new WebViewCreator(context);
        }
        return instance;
    }

    public WebView getWebView() {
        if (webview == null) {
            webview = this.createWebView();
        }
        return this.webview;
    }

    private WebView createWebView() {
        //create and style the WebView
        WebView view = new WebView(this.context);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        view.setLayoutParams(layoutParams);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setDomStorageEnabled(true);

        return view;
    }
}
