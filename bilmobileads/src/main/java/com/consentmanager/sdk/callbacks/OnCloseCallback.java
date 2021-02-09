package com.consentmanager.sdk.callbacks;

import com.consentmanager.sdk.activities.CMPConsentToolActivity;

/**
 * Provides a listener that will be called when {@link CMPConsentToolActivity} is finished after interacting with the WebView
 */
public interface OnCloseCallback {

    /**
     * Listener called when {@link CMPConsentToolActivity} is finished after interacting with the WebView
     */
    void onWebViewClosed();

}