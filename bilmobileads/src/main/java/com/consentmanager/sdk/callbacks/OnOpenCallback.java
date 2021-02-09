package com.consentmanager.sdk.callbacks;

import com.consentmanager.sdk.activities.CMPConsentToolActivity;

/**
 * Provides a listener that will be called when {@link CMPConsentToolActivity} opens the WebView
 */
public interface OnOpenCallback {

    /**
     * Listener called when {@link CMPConsentToolActivity} opens the WebView
     */
    void onWebViewOpened();

}